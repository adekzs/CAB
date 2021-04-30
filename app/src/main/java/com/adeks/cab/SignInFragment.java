package com.adeks.cab;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";
    private Boolean isDriver;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isDriver = SignInFragmentArgs.fromBundle(getArguments()).getIsDriver();
        TextView tv = view.findViewById(R.id.text_detail);
        tv.setText(isDriver ? "Driver Sign In" : "User Sign In");
        Button signIn = view.findViewById(R.id.signin_fragment);
        Button register = view.findViewById(R.id.register);
        TextInputLayout emailLayout = view.findViewById(R.id.email);
        TextInputLayout passwordLayout = view.findViewById(R.id.password);

        signIn.setOnClickListener(v -> {
            String emailString = emailLayout.getEditText().getText().toString();
            String password = passwordLayout.getEditText().getText().toString();
            if (!(TextUtils.isEmpty(emailString) || TextUtils.isEmpty(password))) {
                mAuth.signInWithEmailAndPassword(emailString, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (isDriver) {
                                        NavHostFragment.findNavController(SignInFragment.this)
                                                .navigate(R.id.action_SecondFragment_to_customerRequestFragment);
                                    } else {
                                        NavHostFragment.findNavController(SignInFragment.this)
                                                .navigate(R.id.action_SecondFragment_to_orderDetailsFragment);
                                    }
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    task.getException().printStackTrace();
                                    Log.d(TAG, "onComplete: Error message is" +task.getException());
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Fields should not be empty",Toast.LENGTH_SHORT).show();
            }
        });
        register.setOnClickListener(v -> {
            SignInFragmentDirections.ActionSecondFragmentToRegisterDriverFragment action = SignInFragmentDirections.
                    actionSecondFragmentToRegisterDriverFragment(isDriver);
            NavHostFragment.findNavController(SignInFragment.this)
                    .navigate(action);
        });
    }
}