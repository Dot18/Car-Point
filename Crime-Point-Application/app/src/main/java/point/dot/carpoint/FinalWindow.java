package point.dot.carpoint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//just final window where we can go back to Home activity
public class FinalWindow extends AppCompatActivity {

    Button backHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_window);

        backHomeButton = (Button)findViewById(R.id.BackToHomeButton);

        backHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinalWindow.this, MainWindow.class);
                startActivity(intent);
            }
        });
    }
}
