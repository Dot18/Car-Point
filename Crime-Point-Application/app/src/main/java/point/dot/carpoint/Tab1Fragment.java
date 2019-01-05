package point.dot.carpoint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

//first tab "sell a car" where user has to post his announcement and follow next-next steps
public class Tab1Fragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private Button PostButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment,container,false);
        PostButton = (Button) view.findViewById(R.id.PostButton);

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), CarSellingPage.class);
                startActivity(intent);
                Toast.makeText(getActivity(), "Please take a picture of your car and then press RECOGNISE IMAGE Button",Toast.LENGTH_LONG).show();

            }
        });
        return view;
    }
}