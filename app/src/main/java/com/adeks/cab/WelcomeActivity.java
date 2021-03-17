package com.adeks.cab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    TextInputLayout email;
    TextInputLayout password;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
    }

    public void openRegisterActivity(View view) {
        Intent startRegisterActiity = new Intent(this, SignUpActivity.class);
        startActivity(startRegisterActiity);
    }

    public void signIn(View view) {
        String emailStr = email.getEditText().getText().toString();
        String passwordStr = email.getEditText().getText().toString();
        signIn(emailStr,passwordStr);
    }

    private void signIn(String emailStr, String passwordStr) {
        mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "signIn: Sign In successful");
                    } else {
                        Toast.makeText(this, "Login not successful", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "signIn: Sign In UNSUCCESSFUL"+task.getException().getMessage());
                    }
                });
    }
}