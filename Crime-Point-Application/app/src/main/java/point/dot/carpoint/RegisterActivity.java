package point.dot.carpoint;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

//registration class
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail, mPassword, mConfirmPassword, mUsername, mPhone;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_window);

        mEmail = findViewById(R.id.RegisterEmailEditText);
        mPassword = findViewById(R.id.RegisterPasswrodEditText);
        mConfirmPassword = findViewById(R.id.RegisterConfirmPasswordEditText);

        mUsername = findViewById(R.id.UserNameEditText);
        mPhone = findViewById(R.id.PhoneNumberEditText);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.RegisterWindowButton).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
        }
    }

    private void registerUser() {
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmpassword = mConfirmPassword.getText().toString().trim();
        final String username = mUsername.getText().toString().trim();
        final String phone = mPhone.getText().toString().trim();

        //simple checks
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError(getString(R.string.input_error_email));
            mEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mPassword.setError(getString(R.string.input_error_password));
            mPassword.requestFocus();
            return;
        }

        if (confirmpassword.isEmpty()) {
            mPassword.setError(getString(R.string.doesnt_match));
            mPassword.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            mUsername.setError(getString(R.string.input_error_username));
            mUsername.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()) {
                            User user = new User(
                                    email,
                                    username,
                                    phone
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent(RegisterActivity.this, LoginWindow.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();

                                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_success), Toast.LENGTH_LONG).show();
                                    } else {
                                        //failure message
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.RegisterWindowButton:
                registerUser();
                break;
        }
    }

}
