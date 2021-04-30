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


    TextInputLayout email;
    TextInputLayout password;
    TextInputLayout confirmPassword;
    CircularProgressIndicator progressIndicator;




    private FirebaseAuth mAuth;
    private DatabaseReference customerDatabaseRef;
    private String onlineCustomerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
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

        String passwordString = password.getEditText().getText().toString();
        String confirmPasswordStr = confirmPassword.getEditText().getText().toString();
        if (passwordString.equals(confirmPasswordStr)) {
            mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            onlineCustomerId = mAuth.getCurrentUser().getUid();
                            customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                    .child("users").child("customers").child(onlineCustomerId);
                            customerDatabaseRef.setValue(true);
                            gotoSignInActivity();
                            Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT)
                                    .show();

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressIndicator.setVisibility(View.INVISIBLE);
                    });
        } else {
            Toast.makeText(this, "Your password is not the same as confirm password", Toast.LENGTH_SHORT).show();
        }

    }

    private void gotoSignInActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }
}