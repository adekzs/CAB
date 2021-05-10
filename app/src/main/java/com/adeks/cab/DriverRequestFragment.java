package com.adeks.cab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.adeks.cab.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

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
    private String driverKey;

    public static  DriverRequestFragment newInstance (User user, String driverKey) {
        DriverRequestFragment frag = new DriverRequestFragment();
        Bundle args = new Bundle();
        args.putParcelable("User Details",user);
        args.putString("driver key", driverKey);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            getArgumentValues(savedInstanceState);
        }
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        ridersRef = FirebaseDatabase.getInstance().getReference().child("rides").child(mAuth.getCurrentUser().getUid());
        availableDriversRef = FirebaseDatabase.getInstance().getReference().child("available drivers").child(driverKey);
        workingDriversRef = FirebaseDatabase.getInstance().getReference().child("working drivers").child(driverKey);
    }

    private void getArgumentValues(Bundle savedInstanceState) {
        user = (User) getArguments().get("User Details");
        driverKey = getArguments().getString("driver key");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView =  inflater.inflate(R.layout.accept_request_dialog,container,false);
        dialogView.findViewById(R.id.accept_ride_btn).setOnClickListener(v -> {
            ridersRef.child(user.getKey()).child("isAccepted").setValue("true");
            placeDriverInWorkinNode();
            CustomerRequestFragmentDirections.ActionCustomerRequestFragmentToCustomerDetailsFragment action = CustomerRequestFragmentDirections.actionCustomerRequestFragmentToCustomerDetailsFragment(user, driverKey);
//            Navigation.findNavController(dialogView).navigate(action);
            NavHostFragment.findNavController(this).navigate(action);
            dismiss();
//            ridersRef.addValueEventListener(new ValueEventListener() {
//                @RequiresApi(api = Build.VERSION_CODES.N)
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.getValue() != null) {
//                        Log.d(TAG, "onDataChange: customer Values:" + snapshot.getValue().toString());
//                        Map<String, Map<String, String>> value = (Map<String, Map<String, String>>) snapshot.getValue();
//                        value.forEach((k, v) -> {
//                            Log.d(TAG, "onDataChange:  the key is " + k + "The value of user is" + v.get("user"));
//                            if (k.equals(user.getKey())) {
//                                ridersRef.child(user.getKey()).child("isAccepted").setValue("true");
//                            } else {
//                                ridersRef.child(k).child("isAccepted").setValue("false");
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });

        });
        dialogView.findViewById(R.id.cancel_ride_btn).setOnClickListener(v -> {
            ridersRef.child(user.getKey()).child("isAccepted").setValue("false");
            dismiss();
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

    public void placeDriverInWorkinNode() {
        availableDriversRef.setValue(null);
        workingDriversRef.setValue(mAuth.getCurrentUser().getUid());
    }


}
