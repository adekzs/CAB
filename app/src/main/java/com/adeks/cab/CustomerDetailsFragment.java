package com.adeks.cab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adeks.cab.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomerDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerDetailsFragment extends Fragment {

    private static final String TAG = "CustomerDetailsFragment";
    FirebaseAuth mAuth;
    DatabaseReference availableDrivers;
    DatabaseReference workingDrivers;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private User user;
    private String driverKey;

    public CustomerDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerDetailsFragment newInstance(String param1, String param2) {
        CustomerDetailsFragment fragment = new CustomerDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);

        Log.d(TAG, "onCreate: The user is :" + user.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = CustomerDetailsFragmentArgs.fromBundle(getArguments()).getCustomerDetails();
        driverKey = CustomerDetailsFragmentArgs.fromBundle(getArguments()).getDriverKey();
        CircleImageView customerImage = view.findViewById(R.id.cust_image);
        TextView name = view.findViewById(R.id.cust_name);
        TextView phone = view.findViewById(R.id.cust_phone);
        TextView start  = view.findViewById(R.id.user_start);
        TextView stop = view.findViewById(R.id.user_stop);
        ImageView callCust = view.findViewById(R.id.call_user);
        ImageView cancelRide = view.findViewById(R.id.cancel_ride);

        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(user.getImageUrl())
                    .into(customerImage);
        }
        name.setText(user.getName());
        phone.setText(user.getPhone());
        start.setText("Start: "+user.getStart().getName());
        stop.setText("Stop: " +user.getStop().getName());

        callCust.setOnClickListener( v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+user.getPhone()));//change the number
            startActivity(callIntent);
        });
        cancelRide.setOnClickListener( v -> {
            cancelRide();
            NavHostFragment.findNavController(this)
                    .popBackStack(R.id.customerRequestFragment, false);
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.action_customerDetailsFragment_to_customerRequestFragment);
        });
    }

    private void cancelRide() {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("rides").child(mAuth.getCurrentUser().getUid()).child(user.getKey());
        rideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    rideRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                Toast.makeText(getContext(), "Signed out",Toast.LENGTH_SHORT).show();
                cancelRide();
                FirebaseDatabase.getInstance().getReference().child("available drivers").child(driverKey).setValue(null);
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.FirstFragment, false);
        }
        return true;
    }


}