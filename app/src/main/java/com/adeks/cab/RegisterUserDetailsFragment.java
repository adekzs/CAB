package com.adeks.cab;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * This fragment is used to setup the details of
 * the driver/user.
 * */
public class RegisterUserDetailsFragment extends Fragment {


    private Boolean isDriver;
    private TextInputLayout carLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout nameLayout;
    private String name;
    private String phone;
    private String car;
    private CircleImageView userImage;
    private String checker;
    private Button save;
    private Button cancel;
    private Uri imageUri;
    private CircularProgressIndicator circleProgressBar;
    private StorageReference storageProfilePicsRef;
    private StorageTask uploadTask;
    FirebaseAuth mAuth;
    private String myUrl;
    private DatabaseReference databaseReference;

    public RegisterUserDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Picture");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_register_user_details, container, false);
        nameLayout = view.findViewById(R.id.name);
        phoneLayout = view.findViewById(R.id.phone);
        carLayout = view.findViewById(R.id.car);
        save = view.findViewById(R.id.save_details);
        cancel = view.findViewById(R.id.cancel_details);
        isDriver = RegisterDriverFragmentArgs.fromBundle(getArguments()).getIsDriver();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(isDriver ? "drivers" : "customers");

        circleProgressBar = view.findViewById(R.id.circular_prog);
        TextView displayText = view.findViewById(R.id.user_tv);
        getUserInformation();
        if (isDriver) {
            displayText.setText("Driver Details");
            car = carLayout.getEditText().getText().toString();
        } else {
            displayText.setText("User Details");
            carLayout.setVisibility(View.INVISIBLE);
        }
        userImage = view.findViewById(R.id.profile_image);
        userImage.setOnClickListener(v -> {
            chooseImage();
        });

        view.findViewById(R.id.change_picture_txt).setOnClickListener(v -> chooseImage());
        return view;
    }

    private void chooseImage() {
        checker = "clicked";
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(getContext(), RegisterUserDetailsFragment.this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        save.setOnClickListener(v -> {
            getFieldDetails();
            if (!TextUtils.isEmpty(checker) && checker.equals("clicked") && validateNonEmptyFields()) {
                uploadProfileAndDetails();
            } else if (validateNonEmptyFields()) {
                uploadDetails();
            } else {
                Toast.makeText(getContext(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(v -> {
            if (isDriver) {
                NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                        .navigate(R.id.action_registerUserDetailsFragment_to_customerRequestFragment);
            } else {
                NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                        .navigate(R.id.action_registerUserDetailsFragment_to_orderDetailsFragment);
            }
        });
    }

    private void getFieldDetails() {
        name = nameLayout.getEditText().getText().toString();
        phone = phoneLayout.getEditText().getText().toString();
        if (isDriver) {
            car = carLayout.getEditText().getText().toString();
        }
    }

    private void uploadDetails() {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", mAuth.getCurrentUser().getUid());
        userMap.put("name", name);
        userMap.put("phone", phone);
        userMap.put("image", myUrl);
        if (isDriver) {
            userMap.put("car", car);
        }
        databaseReference.child(mAuth.getCurrentUser().getUid()).child("profile").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (isDriver) {
                        NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                                .navigate(R.id.action_registerUserDetailsFragment_to_customerRequestFragment);
                    } else {
                        NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                                .navigate(R.id.action_registerUserDetailsFragment_to_orderDetailsFragment);
                    }
                } else {
                    Toast.makeText(getContext(), "Try Again",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadProfileAndDetails() {
        if (imageUri != null) {
            circleProgressBar.setVisibility(View.VISIBLE);
            final StorageReference fileRef = storageProfilePicsRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                } else {
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    myUrl = downloadUrl.toString();

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", mAuth.getCurrentUser().getUid());
                    userMap.put("name", name);
                    userMap.put("phone", phone);
                    userMap.put("image", myUrl);
                    if (isDriver) {
                        userMap.put("car", car);
                    }
                    databaseReference.child(mAuth.getCurrentUser().getUid()).child("profile").updateChildren(userMap);
                    circleProgressBar.setVisibility(View.INVISIBLE);
                    if (isDriver) {
                        NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                                .navigate(R.id.action_registerUserDetailsFragment_to_customerRequestFragment);
                    } else {
                        NavHostFragment.findNavController(RegisterUserDetailsFragment.this)
                                .navigate(R.id.action_registerUserDetailsFragment_to_orderDetailsFragment);
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Image is not selected", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean validateNonEmptyFields() {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            return false;
        }
        if (isDriver) {
            return !TextUtils.isEmpty(car);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            userImage.setImageURI(imageUri);
        } else {
            Toast.makeText(getContext(), "Error, Try Again Later", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    String name = snapshot.child("name").getValue().toString();
                    nameLayout.getEditText().setText(name);
                    String phone = snapshot.child("phone").getValue().toString();
                    phoneLayout.getEditText().setText(phone);
                    if (isDriver) {
                        car = snapshot.child("car").getValue().toString();
                    }
                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get()
                                .load(image)
                                .into(userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}