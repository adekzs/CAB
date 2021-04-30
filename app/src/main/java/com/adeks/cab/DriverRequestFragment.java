package com.adeks.cab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.adeks.cab.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverRequestFragment extends DialogFragment {

    private static final String TAG = "DriverRequestFragment";
    private User user;
    FirebaseAuth mAuth;
    DatabaseReference ridersRef;
    DatabaseReference availableDriversRef;
    DatabaseReference workingDriversRef;
    private CircleImageView image;
    private TextView start;
    private TextView stop;

    public static  DriverRequestFragment newInstance (User user) {
        DriverRequestFragment frag = new DriverRequestFragment();
        Bundle args = new Bundle();
        args.putParcelable("User Details",user);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            getArgumentValues(savedInstanceState);
        }
        mAuth = FirebaseAuth.getInstance();
        ridersRef = FirebaseDatabase.getInstance().getReference().child("rides").child(mAuth.getCurrentUser().getUid());
    }

    private void getArgumentValues(Bundle savedInstanceState) {
        user = (User) getArguments().get("User Details");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView =  inflater.inflate(R.layout.accept_request_dialog,container,false);
        dialogView.findViewById(R.id.accept_ride_btn).setOnClickListener(v -> {
            ridersRef.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Log.d(TAG, "onDataChange: customer Values:" + snapshot.getValue().toString());
                        Map<String, Map<String, String>> value = (Map<String, Map<String, String>>) snapshot.getValue();
                        value.forEach((k, v) -> {
                            Log.d(TAG, "onDataChange:  the key is " + k + "The value of user is" + v.get("user"));
                            if (k.equals(user.getKey())) {
                                ridersRef.child(user.getKey()).child("isAccepted").setValue("true");
                            } else {
                                ridersRef.child(k).child("isAccepted").setValue("false");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        dialogView.findViewById(R.id.cancel_ride_btn).setOnClickListener(v -> {
            ridersRef.child(user.getKey()).setValue(null);
        });
        dialogView.findViewById(R.id.close_dialog).setOnClickListener(v -> {
            dismiss();
        });

        image = dialogView.findViewById(R.id.user_image_view);
        start = dialogView.findViewById(R.id.start_detailstv);
        stop = dialogView.findViewById(R.id.stop_detailstv);

        if (!user.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(user.getImageUrl())
                    .into(image);
        }
        start.setText(user.getStart().getName());
        stop.setText(user.getStop().getName());
        return dialogView;
    }


}
