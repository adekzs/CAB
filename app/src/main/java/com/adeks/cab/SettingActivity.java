package com.adeks.cab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
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

public class SettingActivity extends AppCompatActivity {

    private CircleImageView circularImageView;
    private TextInputLayout nameEditLayout, phoneInputLayout;
    private ImageView saveButton, closeButton;
    private TextView profileChangeBtn;
    private String checker = "";

    private CircularProgressIndicator circularProgress;

    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRef;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Picture");
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child("customers");

        circularImageView = findViewById(R.id.profile_image);
        nameEditLayout = findViewById(R.id.name);
        phoneInputLayout = findViewById(R.id.phone);


        saveButton = findViewById(R.id.save_btn);
        closeButton = findViewById(R.id.close_btn);

        profileChangeBtn = findViewById(R.id.change_picture_txt);
        circularProgress = findViewById(R.id.circular_prog);



    }

    public void gotoMapActivity(View view) {
        Intent mapActivityIntent = new Intent(this, CustomersMapActivity.class);
        startActivity(mapActivityIntent);
    }

    public void saveDetails(View view) {
        if (checker.equals("clicked")) {
            validateEditTexts();
        } else {
            validateAndSaveOnlyInformation();
        }

    }

    private void validateAndSaveOnlyInformation() {
        if (TextUtils.isEmpty(nameEditLayout.getEditText().getText().toString()) ||
                TextUtils.isEmpty(phoneInputLayout.getEditText().getText().toString())) {
            Toast.makeText(this, "Please fill in all fields",Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name", nameEditLayout.getEditText().toString());
            userMap.put("phone", phoneInputLayout.getEditText().toString());

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            Intent mapActivityIntent = new Intent(SettingActivity.this, CustomersMapActivity.class);
            startActivity(mapActivityIntent);
        }
    }

    public void changePicture(View view) {
        checker = "clicked";
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(SettingActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            circularImageView.setImageURI(imageUri);
        } else {
            startActivity(new Intent(this, CustomersMapActivity.class));
            Toast.makeText(this, "Error, Try Again Later", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateEditTexts() {
        if (TextUtils.isEmpty(nameEditLayout.getEditText().getText().toString()) ||
                TextUtils.isEmpty(phoneInputLayout.getEditText().getText().toString())) {
            Toast.makeText(this, "Please fill in all fields",Toast.LENGTH_SHORT).show();
        } else if (checker.equals("checked")) {
            uploadProfilePicture();
        }
    }

    private void uploadProfilePicture() {
        if (imageUri != null) {
            circularProgress.setVisibility(View.VISIBLE);
            final StorageReference fileRef = storageProfilePicsRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    } else {
                        return fileRef.getDownloadUrl();
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", mAuth.getCurrentUser().getUid());
                        userMap.put("name", nameEditLayout.getEditText().toString());
                        userMap.put("phone", phoneInputLayout.getEditText().toString());
                        userMap.put("image", myUrl);

                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        circularProgress.setVisibility(View.INVISIBLE);
                        Intent mapActivityIntent = new Intent(SettingActivity.this, CustomersMapActivity.class);
                        startActivity(mapActivityIntent);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    String name = snapshot.child("name").getValue().toString();
                    nameEditLayout.getEditText().setText(name);
                    String phone = snapshot.child("phone").getValue().toString();
                    phoneInputLayout.getEditText().setText(phone);

                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get()
                                .load(image)
                                .into(circularImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}