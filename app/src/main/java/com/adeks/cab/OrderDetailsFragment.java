package com.adeks.cab;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderDetailsFragment extends Fragment {
    private static final String TAG = "OrderDetailsFragment";
    FirebaseAuth mAuth;
    private DatabaseReference customerRequestRef;
    private FirebaseUser currentUser;

    private static final int ARC_START = 1;
    private static final int ARC_STOP = 2;
    private TextView startTextView;
    private TextView stopTextView;
    private LatLngBounds latLngBounds;
    private Place startPlace;
    private Place stopPlace;


    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    public static OrderDetailsFragment newInstance() {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
         mAuth = FirebaseAuth.getInstance();
         currentUser = mAuth.getCurrentUser();
         customerRequestRef = FirebaseDatabase.getInstance().getReference().child("customer requests").child(currentUser.getUid());
        latLngBounds = new LatLngBounds(new LatLng(7.29276, 5.124115), new LatLng(7.310719, 5.149935));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyC25W6TLS3-DklpZHbs-BgBoaH4zzJGeow");
        PlacesClient placesClient = Places.createClient(getActivity().getApplicationContext());
        Button startBtn = view.findViewById(R.id.start_btn);
        Button destBtn = view.findViewById(R.id.destination_btn);
        startTextView = view.findViewById(R.id.start_tv);
        stopTextView = view.findViewById(R.id.stop_tv);
        Button orderCabBtn = view.findViewById(R.id.order_btn);
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields)
                .setLocationRestriction(RectangularBounds.newInstance( new LatLng(7.29276, 5.124115 ),
                        new LatLng(7.310719, 5.149935)
                       ))
                .build(getActivity().getApplicationContext());
        startBtn.setOnClickListener((v) -> startActivityForResult(intent,ARC_START));
        destBtn.setOnClickListener((v) -> startActivityForResult(intent, ARC_STOP));

        orderCabBtn.setOnClickListener(v -> {
            if (startPlace == null || stopPlace == null) {
                Toast.makeText(getContext(), "Destinations should be selected ", Toast.LENGTH_SHORT).show();
            } else {
                HashMap<String, Object> customerPlaces = new HashMap<>();
                customerPlaces.put("start",startPlace);
                customerPlaces.put("stop", stopPlace);
                customerRequestRef.updateChildren(customerPlaces).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Ride created",Toast.LENGTH_SHORT).show();
                            String lat = String.valueOf(startPlace.getLatLng().latitude);
                            String longtd = String.valueOf(startPlace.getLatLng().longitude);
                            OrderDetailsFragmentDirections.ActionOrderDetailsFragmentToDriversFragment action = OrderDetailsFragmentDirections.actionOrderDetailsFragmentToDriversFragment(lat, longtd);
                            NavHostFragment.findNavController(OrderDetailsFragment.this)
                                    .navigate(action);
                        } else {
                            Toast.makeText(getContext(), "Error creating Ride",Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Ride creation was not successful "+ task.getException().getMessage());
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ARC_START && data != null) {
            if (resultCode == RESULT_OK) {
                startPlace = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + startPlace.getName() + ", " + startPlace.getId() + ", " + startPlace.getLatLng());
                if (latLngBounds.contains(startPlace.getLatLng())) {
                    startTextView.setText(startPlace.getName());
                }else {
                    Toast.makeText(getContext(),"Choose a place within FUTA",Toast.LENGTH_SHORT).show();
                    startPlace = null;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        } else if (requestCode == ARC_STOP && data != null) {
            if (resultCode == RESULT_OK) {
                stopPlace = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + stopPlace.getName() + ", " + stopPlace.getId() +", " + stopPlace.getLatLng());
                if (latLngBounds.contains(stopPlace.getLatLng())) {
                    stopTextView.setText(stopPlace.getName());
                } else {
                    Toast.makeText(getContext(),"Choose a place within FUTA",Toast.LENGTH_SHORT).show();
                    stopPlace = null;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.FirstFragment, false);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}