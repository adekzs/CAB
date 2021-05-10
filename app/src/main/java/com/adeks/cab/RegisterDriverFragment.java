package com.adeks.cab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 *This Fragment is used to register both new user and driver
 * depending on the user selection.
 * Use the {@link RegisterDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterDriverFragment extends Fragment {


    FirebaseAuth mAuth;
    public RegisterDriverFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterDriverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterDriverFragment newInstance(String param1, String param2) {
        RegisterDriverFragment fragment = new RegisterDriverFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_driver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Boolean isDriver = RegisterDriverFragmentArgs.fromBundle(getArguments()).getIsDriver();
        TextInputLayout emailLayout = view.findViewById(R.id.email);
        TextInputLayout passwordLayout = view.findViewById(R.id.password);
        TextInputLayout confirmPasswordLayout = view.findViewById(R.id.conf_password);

        MaterialButton registerBtn = view.findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(v -> {
            String emailString = emailLayout.getEditText().getText().toString();
            String passwordString = passwordLayout.getEditText().getText().toString();
            String confirmPasswordString = confirmPasswordLayout.getEditText().getText().toString();

            if (!(TextUtils.isEmpty(emailString) || TextUtils.isEmpty(passwordString) || TextUtils.isEmpty(confirmPasswordString))) {
                if (passwordString.equals(confirmPasswordString)) {
                    mAuth.createUserWithEmailAndPassword(emailString,passwordString)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
//                                        if (isDriver) {
//                                            String driverId = mAuth.getCurrentUser().getUid();
//                                            DatabaseReference driverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users")
//                                                    .child("drivers").child(driverId);
//
//
//                                        } else {
//                                            String customerId = mAuth.getCurrentUser().getUid();
//                                            DatabaseReference driverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users")
//                                                    .child("customers").child(customerId);
//                                        }
                                        RegisterDriverFragmentDirections.ActionRegisterDriverFragmentToRegisterUserDetailsFragment action = RegisterDriverFragmentDirections.
                                                actionRegisterDriverFragmentToRegisterUserDetailsFragment(isDriver);
                                        NavHostFragment.findNavController(RegisterDriverFragment.this)
                                                .navigate(action);
                                    } else {
                                        Toast.makeText(getContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getContext(),"Password and confirm password are not equal", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Fields Should not be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

}