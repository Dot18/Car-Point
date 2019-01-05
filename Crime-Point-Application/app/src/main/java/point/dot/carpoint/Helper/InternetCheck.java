package point.dot.carpoint.Helper;

import android.os.AsyncTask;

import java.net.InetSocketAddress;
import java.net.Socket;

//this class checks if user has the internet or net. Then we used it in Image Labelling.
public class InternetCheck extends AsyncTask<Void,Void,Boolean> {

    public interface Consumer {
        void accept (boolean internet);
    }

    Consumer consumer;

    public InternetCheck(Consumer consumer) {
        this.consumer = consumer;
        execute();
    }

    @Override
    //requests to google.com, timeout is 1.5 sec
    protected Boolean doInBackground(Void... voids) {

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com",80),1500);
            socket.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        consumer.accept(aBoolean);
    }
}
