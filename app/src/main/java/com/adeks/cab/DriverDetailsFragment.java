package com.adeks.cab;

import android.content.Intent;
import android.location.Location;
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

import com.adeks.cab.models.Driver;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import mumayank.com.airlocationlibrary.AirLocation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomerDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverDetailsFragment extends Fragment {
    private static final String TAG = "DriverDetailsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Driver driver;
    private TextView distance;
    private String userKey;

    public DriverDetailsFragment() {
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
    public static DriverDetailsFragment newInstance(String param1, String param2) {
        DriverDetailsFragment fragment = new DriverDetailsFragment();
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
        setHasOptionsMenu(true);
        driver = DriverDetailsFragmentArgs.fromBundle(getArguments()).getDriverDetails();
        userKey = DriverDetailsFragmentArgs.fromBundle(getArguments()).getUserKey();
        Log.d(TAG, "onCreate: The Driver is :" + driver.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CircleImageView driverImage = view.findViewById(R.id.driverinfo_image);
        TextView driverName = view.findViewById(R.id.driverinfo_name);
        TextView car = view.findViewById(R.id.driverinfo_car);
        TextView phone = view.findViewById(R.id.driverinfo_phone);
        distance = view.findViewById(R.id.driverinfo_distance);
        calculateDistance();
        ImageView callDriverImg = view.findViewById(R.id.call_driver);
        ImageView cancelDriverImg = view.findViewById(R.id.cancel_driver);
        callDriverImg.setOnClickListener( v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+driver.getPhone()));//change the number
            startActivity(callIntent);
        });

        cancelDriverImg.setOnClickListener( v -> {
           cancelRide();
            NavHostFragment.findNavController(this)
                    .popBackStack(R.id.orderDetailsFragment,false);
        });

        if (!driver.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(driver.getImageUrl())
                    .into(driverImage);
        }
        driverName.setText(driver.getName());
        car.setText(driver.getCarDetails());
        phone.setText(driver.getPhone());
    }

    private void cancelRide() {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("rides").child(driver.getId()).child(userKey);
        rideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null){
                    rideRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calculateDistance() {
        String driverUid = driver.getId();
        String customerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference startRef = FirebaseDatabase.getInstance().getReference().child("customer requests").child(customerUid).child("start").child("latLng");
        final double[] longitude = new double[1];
        final double[] latitude = new double[1];
        startRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    HashMap<String, Double> location = (HashMap<String, Double>) snapshot.getValue();
                    longitude[0] = location.get("longitude");
                    latitude[0] = location.get("latitude");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("user").child("drivers").child(driverUid).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    HashMap<String, Double> location = (HashMap<String, Double>) snapshot.getValue();
                    double lat = location.get("lat");
                    double longtd = location.get("log");

                    Location start = new Location("");
                    start.setLatitude(latitude[0]);
                    start.setLatitude(longitude[0]);

                    Location driver = new Location("");
                    driver.setLatitude(lat);
                    driver.setLongitude(longtd);
                    float distanceBtwUsers = start.distanceTo(driver);
                    distance.setText(distanceBtwUsers + "m from start location");
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
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.FirstFragment, false);
        }
        return true;
    }
}