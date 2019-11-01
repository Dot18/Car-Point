import json
import os
import shutil
import win32com.client
import pdfplumber
import time
"""
1.获得所有文件
2.迁移pdf文件
3.将不是pdf文件,转换成pdf文件
"""


class FileOperation():
    def __init__(self, folder_path):
        self.folder_path = folder_path
        self.files = []

    def get_all_files(self):
        for root, dirs, files in os.walk(self.folder_path):
            for file in files:
                self.files.append(os.path.join(os.getcwd(), root, file))
        return self.files

    def move_files(self):
        for n in self.files:
            file_name = os.path.basename(n)
            old_path = n
            if '.pdf' in n:
                target = os.path.join(os.getcwd(), 'pdfs', file_name)
                shutil.copy(old_path, target)
            elif '.docx' in n:
                target = os.path.join(
                    os.getcwd(), 'pdfs', file_name).replace('.docx', '.pdf')
                TransformationFile.word_to_pdf(old_path, target)
            elif '.doc' in n:
                target = os.path.join(
                    os.getcwd(), 'pdfs', file_name).replace('.doc', '.pdf')
                TransformationFile.word_to_pdf(old_path, target)
            elif 'pptx' in n:
                target = os.path.join(
                    os.getcwd(), 'pdfs', file_name).replace('.pptx', '.pdf')
                TransformationFile.ppt_to_pdf(old_path, target)
            elif 'ppt' in n:
                target = os.path.join(
                    os.getcwd(), 'pdfs', file_name).replace('.ppt', '.pdf')
                TransformationFile.ppt_to_pdf(old_path, target)


class TransformationFile():
    @classmethod
    def word_to_pdf(cls, old_path, new_path):
        try:
            word = win32com.client.Dispatch('Word.Application')
            doc = word.Documents.Open(old_path)
            doc.SaveAs(new_path, FileFormat=17)
            doc.Close()
            word.Quit()
        except Exception as e:
            log_error(old_path,'is broken.',e)
            #print(old_path, ' is broken')

    @classmethod
    def ppt_to_pdf(cls, old_path, new_path):
        try:
            powerpoint = win32com.client.Dispatch('Powerpoint.Application')
            ppt = powerpoint.Presentations.Open(old_path)
            ppt.SaveAs(new_path, FileFormat=32)
            ppt.Close()
            powerpoint.Quit()
        except Exception as e:
            def log_error(*args,**kwargs):
                format = '%H:%M:%S'
                value = time.localtime(int(time.time()))
                dt = time.strftime(format,value)
                with open('error.txt', 'a', encoding='utf-8') as f:
                    print(dt, *args, file=f, **kwargs)
            log_error(old_path,'is broken.',e)
            #print(old_path, ' is broken')


class FetchData():
    @ classmethod
    def scrape_text(cls, files):
        def get_page_text(page):
            text = page.extract_text()
            return text

        def get_page_tables(page):
            table = page.extract_tables()
            return table

        pdf_dict = {}
        for f in files:
            pdf = pdfplumber.open(f)
            file_name = os.path.basename(f)
            pdf_dict[file_name] = {}

            for i, page in enumerate(pdf.pages):
                pagename = 'page '+str(i+1)
                pdf_dict[file_name][pagename] = {}

                # gets page text
                page_text = get_page_text(page)
                pdf_dict[file_name][pagename]['text'] = page_text

                # gets page tables
                page_tables = get_page_tables(page)
                pdf_dict[file_name][pagename]['table'] = page_tables
        return pdf_dict


class Search():
    def __init__(self, data, key_word, strict=False, english_model=True):
        self.data = data
        self.key_word = key_word
        self.strict = strict
        self.english_model = english_model
        self.content = []

    def search_key_word(self):
        """
        {
            # 文件名
            "file_name":"",
            # 页数
            "page":"",
            # 句子
            "word":"",
            # 全文
            "total_text":"",
            # 行数
            "line":"",
            # 段落
            "content":"",
        }
        """
        data = self.data
        key_word = self.key_word

        info = []
        for file in data:
            for pagename in data[file]:
                page_obj = data[file][pagename]
                text = page_obj['text']
                table = page_obj['table']
                text_query = self.pick_word_from_text(
                    text, key_word, 'text', file, pagename)
                table_query = self.pick_word_from_text(
                    table, key_word, 'table', file, pagename)
                info += text_query
                info += table_query
        return info

    def pick_word_from_text(self, text, key_word, text_type, file, pagename):
        """
        接受一小段text 判断关键词是否在其中

        1. 由pdf读取的text 可以直接将根据"\n"把一段话切成几行。
        由pdf读取的table是一个多维数组，首先要将其变成一维数据
        2.
        """
        if text is None:
            return[]
        try:
            if text_type == 'table':
                split_page_list = flatten(text)
                belong_content_list = []
                for n in text:
                    one_array = flatten(text)
                    one_array_with_str = [str(n) for n in one_array]
                    belong_content_list.append('    '.join(one_array_with_str))
                belong_content = '\n'.join(belong_content_list)
            else:
                split_page_list = text.split('\n')
                belong_content_list = text.split('.\n')
        except Exception as e:
            log_error("file:",file)
            log_error("pagename:",pagename)
            log_error("text:",text)
            log_error("text_type:",text_type)
            log_error(e)
            split_page_list=[]
            belong_content_list=[]

        text_list = []
        total_text = ""
        status = False
        for index, line in enumerate(split_page_list):
            if line is not None:
                total_text += line.strip()
                line_num = index+1
                if self.check_is_in(line):
                    status = True
                    line_obj = {
                        'file': file,
                        'pagename': pagename,
                        'line_num': line_num,
                        'line': line,
                        'text_type': text_type,
                        'content': '',
                    }

                    if text_type == 'table':
                        line_obj.update({'content': belong_content})
                    else:
                        for n in belong_content_list:
                            if line in n:
                                line_obj.update({'content': n})

                    text_list.append(line_obj)

        # 每一行不存在的情况下 需要将文字拼成一块 再次匹配
        if not status:
            if key_word in total_text:
                line_obj = {
                    'file': file,
                    'pagename': pagename,
                    'line_num': '10000',
                    'line': line,
                    'text_type': text_type,
                }
                text_list.append(line_obj)
        return text_list

    def check_is_in(self, text):
        """
        判断关键词是否在text中
        """
        key_word = self.key_word
        if self.strict:
            return key_word in text
        else:
            # 在英文 不严格的情况下,英文需要根据空格进行切分,中文则不用
            if self.english_model:
                key_word_list = key_word.split(' ')
                for element in key_word_list:
                    if element in text:
                        return True
            else:
                for element in key_word:
                    if element in text:
                        return True
            return False


class Files():
    def __init__(self, folder_path):
        self.folder_path = folder_path

    def get_folder_files(self):
        path = self.folder_path
        files = os.listdir(path)
        return files

    def is_update(self):
        """
        是否需要更新
        """
        path = 'files.txt'
        storage_files = load('files.txt')
        current_files = self.get_folder_files()
        if storage_files == current_files:
            return True
        else:
            save(current_files, path)
            # 删除原来pdfs里面的文件
            self.del_file()
            return False

    def del_file(self,path_data='pdfs'):
        for i in os.listdir(path_data):
            file_data = os.path.join(path_data , i)
            if os.path.isfile(file_data) == True:
                os.remove(file_data)



def save(data, path):
    """
    data 是 dict 或者 list
    path 是保存文件的路径
    """
    s = json.dumps(data, indent=2, ensure_ascii=False)
    with open(path, 'w+', encoding='utf-8') as f:
        # log('save', path, s, data)
        f.write(s)


def load(path):
    if not os.path.exists(path):
        save({}, path)
    with open(path, 'r', encoding='utf-8') as f:
        s = f.read()
        # log('load', s)
        return json.loads(s)


def flatten(a):
    """
    将多维数组转变为一维数组
    """
    if not isinstance(a, (list, )):
        return [a]
    else:
        b = []
        for item in a:
            b += flatten(item)
    return b


def replace_word(search_keywords, text, strict=True, english_model=True):
    if strict:
        key_word = search_keywords
        key_word_with_color = "\033[1;31;40m《{}》\033[0m".format(
            search_keywords)
        text = text.replace(key_word, key_word_with_color)
    else:
        if english_model:
            key_word = search_keywords
            key_word_list = key_word.split(' ')
            key_word_with_color = text
            for element in key_word_list:
                text = text.replace(
                    element, "\033[1;31;40m《{}》\033[0m".format(element))
        else:
            key_word = search_keywords
            key_word_with_color = text
            for element in key_word:
                text = text.replace(
                    element, "\033[1;31;40m《{}》\033[0m".format(element))
    return text


def get_inputs():
    """
    获得输入
    """
    search_keywords = input("search_keywords:")
    strict_model = input("is_strict 1:strict_model 0:not_strict_model:")
    english_model = True
    if strict_model in ['0', '1']:
        strict_model_dict = {
            '0': False,
            '1': True,
        }
        return search_keywords, strict_model_dict[strict_model], english_model
    else:
        print('strict_model type must in 1 or 2,please retry:')
        get_inputs()


def read_data(folder_path):
    files = Files(folder_path)
    status = files.is_update()

    if status:
        # 与之前保持一致，不用变动
        data = load('data.txt')
    else:
        # 对于数据进行更新
        data = get_pdf_data()
        save(data, 'data.txt')
    return data


def search_word(data, search_keywords, strict, english_model):
    """
    data:读取到的数据
    search_keywords:关键词
    strict：是否严格
    english_model：是否是英语模式(默认为True)
    """
    # search_keywords = 'of default'
    # print("search_keywords:", search_keywords)
    # strict = True
    # english_model = True
    search = Search(data, search_keywords, strict, english_model)
    result = search.search_key_word()
    if result:
        for n in result:
            file = n['file']
            pagename = n['pagename']
            content = n['content']
            print('file_name:', file)
            print('page_name:', pagename)
            print(replace_word(search_keywords, content, strict))
            print('-'*50)
    else:
        print('query doesnt exist')
    return result


def get_pdf_data():
    folder_path = 'files'
    files_obj = FileOperation(folder_path)
    files_obj.get_all_files()
    files_obj.move_files()

    folder_path = 'pdfs'
    files_obj = FileOperation(folder_path)
    files = files_obj.get_all_files()
    pdf_data = FetchData.scrape_text(files)
    return pdf_data

def log_error(*args,**kwargs):
    format = '%H:%M:%S'
    value = time.localtime(int(time.time()))
    dt = time.strftime(format,value)
    with open('error.txt', 'a', encoding='utf-8') as f:
        print(dt, *args, file=f, **kwargs)

if __name__ == "__main__":
    folder_path = 'files'
    data = read_data(folder_path)
    search_keywords, strict, english_model = get_inputs()
    reuslt = search_word(data, search_keywords, strict, english_model)
