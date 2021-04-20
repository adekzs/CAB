package com.adeks.cab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.EnumMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    TextInputLayout firstName;
    TextInputLayout lastName;
    TextInputLayout email;
    TextInputLayout password;
    TextInputLayout confirmPassword;
    CircularProgressIndicator progressIndicator;




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
        mAuth = FirebaseAuth.getInstance();


        progressIndicator = findViewById(R.id.indicator);
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
        progressIndicator.setVisibility(View.VISIBLE);
        String emailString = email.getEditText().getText().toString();
//        String fNameString = firstName.getEditText().getText().toString();
//        String lNameString = lastName.getEditText().getText().toString();
        String passwordString = password.getEditText().getText().toString();
//        String confirmPasswordStr = confirmPassword.getEditText().getText().toString();

        mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        gotoSignInActivity();
                        Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT)
                            .show();

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this,     "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                    progressIndicator.setVisibility(View.INVISIBLE);
                });


    }

    private void gotoSignInActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }
}