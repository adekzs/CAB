package com.adeks.cab;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.adeks.cab.dummy.DummyContent;
import com.adeks.cab.models.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A fragment representing a list of Items.
 */
public class DriversFragment extends Fragment {

    private static final String TAG = "DriversFragment";

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference availableDriversRef;
    DatabaseReference driversRef;
    DatabaseReference workingDriversRef;
    DatabaseReference rideAvailableRef;
    List<Driver> availDrivers;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MyItemRecyclerViewAdapter myAdapter;
    private String startLat;
    private String startLong;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DriversFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DriversFragment newInstance(int columnCount) {
        DriversFragment fragment = new DriversFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drivers_list, container, false);


        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true);
        startLat = DriversFragmentArgs.fromBundle(getArguments()).getStartLatitude();
        startLong = DriversFragmentArgs.fromBundle(getArguments()).getStartLongitude();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        availableDriversRef = FirebaseDatabase.getInstance().getReference().child("available drivers");
        driversRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers");


       /* availableDriversRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getChildrenCount() > 0) {
                    HashMap<String,Object> list = (HashMap<String, Object>) snapshot.getValue();
                    list.forEach((k,v) -> {
                        Log.d(TAG, "onDataChange: The key is: "+ k + "and the value is "+v);
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        availDrivers = new ArrayList<>();
        availableDriversRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildAdded: " +snapshot.toString());
                String key = snapshot.getKey();
                String value = (String) snapshot.getValue();

                FirebaseDatabase.getInstance().getReference().child("users").child("drivers")
                        .child(value).child("profile").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null && snapshot.getChildrenCount() > 0) {
                            String name = snapshot.child("name").getValue().toString();
                            String phone = snapshot.child("phone").getValue().toString();
                            String car = snapshot.child("car").getValue().toString();
                            String image;
                            if (snapshot.hasChild("image")) {
                                image = snapshot.child("image").getValue().toString();
                            } else {
                                image = "";
                            }
                            final String[] latitude = new String[1];
                            final String[] longitude = new String[1];
                            driversRef.child(value).child("location").addValueEventListener(new ValueEventListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot != null && snapshot.getChildrenCount() > 0) {
                                        String lat = snapshot.child("lat").getValue().toString();
                                        String log = snapshot.child("log").getValue().toString();
                                        Log.d(TAG, "onDataChange: Latitude is"+ lat + "longitude is"+ log);
                                        latitude[0] = lat;
                                        longitude[0] = log;
                                        Driver driver = new Driver(key, value, name,latitude[0],longitude[0],car, image,phone );
                                        if (!availDrivers.contains(driver)) {
                                            availDrivers.add(driver);
                                            availDrivers.sort(new Comparator<Driver>() {
                                                @Override
                                                public int compare(Driver o1, Driver o2) {
                                                    Location driver1 = new Location("");
                                                    driver1.setLatitude(Double.parseDouble(o1.getLatitude()));
                                                    driver1.setLongitude(Double.parseDouble(o1.getLongitude()));

                                                    Location driver2 = new Location("");
                                                    driver2.setLatitude(Double.parseDouble(o2.getLatitude()));
                                                    driver2.setLongitude(Double.parseDouble(o2.getLongitude()));

                                                    Location start = new Location("");
                                                    start.setLatitude(Double.parseDouble(startLat));
                                                    start.setLongitude(Double.parseDouble(startLong));
                                                    return (int) (driver1.distanceTo(start) - driver2.distanceTo(start));
                                                }
                                            });
                                        }
                                        Log.d(TAG, "onDataChange: Driver is :"+ driver.toString());
                                        Log.d(TAG, "onDataChange: set size is:"+ availDrivers.size());
                                        if(myAdapter != null) {
                                            myAdapter.notifyDataSetChanged();
                                        } else {
                                            myAdapter = new MyItemRecyclerViewAdapter(availDrivers, getContext());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Optional<Driver> driverRemoved = availDrivers.stream().filter(driver -> driver.getKey().equals(snapshot.getKey()))
                        .findFirst();
                driverRemoved.ifPresent(driver -> availDrivers.remove(driver));
                if (myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            myAdapter = new MyItemRecyclerViewAdapter(availDrivers, getContext());
            recyclerView.setAdapter(myAdapter);
        }
        return view;
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
    public void onStart() {
        super.onStart();
    }
}