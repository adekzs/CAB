package com.adeks.cab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.EnumMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    TextInputLayout firstName;
    TextInputLayout lastName;
    TextInputLayout email;
    TextInputLayout password;
    TextInputLayout confirmPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
        firstName = findViewById(R.id.f_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.conf_password);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUp(View view) {
        String emailString = email.getEditText().getText().toString();
        String fNameString = firstName.getEditText().getText().toString();
        String lNameString = lastName.getEditText().getText().toString();
        String passwordString = password.getEditText().getText().toString();
        String confirmPasswordStr = confirmPassword.getEditText().getText().toString();

        mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: created user");
                        FirebaseUser user  = mAuth.getCurrentUser();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }
}