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
import com.adeks.cab.models.Place;
import com.adeks.cab.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import mumayank.com.airlocationlibrary.AirLocation;

/**
 * A fragment representing a list of Items.
 */
public class CustomerRequestFragment extends Fragment {

    private static final String TAG = "CustomerRequestFragment";
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference availableDriversRef;
    DatabaseReference workingDriversRef;
    DatabaseReference rideAvailableRef;
    DatabaseReference customerRequestsRef;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private ArrayList<String> customerIds;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private AirLocation airLocation;
    private List<User> users;
    private CustomerRequestViewAdapter2 customerRequestViewAdapter;
    private String driverKey;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CustomerRequestFragment() {
    }


    public static CustomerRequestFragment newInstance(int columnCount) {
        CustomerRequestFragment fragment = new CustomerRequestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        availableDriversRef = FirebaseDatabase.getInstance().getReference()
                .child("available drivers");
        workingDriversRef = FirebaseDatabase.getInstance().getReference()
                .child("working drivers");
        rideAvailableRef = FirebaseDatabase.getInstance().getReference()
                .child("rides").child(currentUser.getUid());
        customerRequestsRef = FirebaseDatabase.getInstance().getReference()
                .child("customer requests");
        users = new ArrayList<>();
//        rideAvailableRef.addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "onDataChange: customer Values:"+ snapshot.toString());
//                if (snapshot != null ) {
//                    Log.d(TAG, "onDataChange: customer Values:"+ snapshot.getValue().toString());
//                    Map<String, Map<String, String>> value = (Map<String, Map<String, String>>) snapshot.getValue();
//                    value.forEach((k,v) -> {
//                        Log.d(TAG, "onDataChange:  the key is "+ k + "The value of user is"+v.get("user"));
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        rideAvailableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    users.clear();
                    if (customerRequestViewAdapter != null) {
                        customerRequestViewAdapter.notifyDataSetChanged();
                    } else {
                        customerRequestViewAdapter = new CustomerRequestViewAdapter2(users, getContext(), driverKey);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Log.d(TAG, "onChildAdded: The snapshot is " + snapshot.toString());
                    String key = snapshot.getKey();
                    Map<String, String> value = (Map<String, String>) snapshot.getValue();
                    String customerRequestId = value.get("user");
                    if (customerRequestId != null) {
                        customerRequestsRef.child(customerRequestId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot != null && snapshot.getChildrenCount() > 0) {
                                    HashMap<String, Object> val = (HashMap<String, Object>) snapshot.getValue();
                                    Log.d(TAG, "onDataChange: value of customer request is" + val.toString());
                                    HashMap<String, Object> startMap = (HashMap<String, Object>) val.get("start");
                                    HashMap<String, Object> stopMap = (HashMap<String, Object>) val.get("stop");
                                    HashMap<String, Double> locationStart = (HashMap<String, Double>) startMap.get("latLng");
                                    HashMap<String, Double> locationStop = (HashMap<String, Double>) stopMap.get("latLng");
                                    Place start = new Place((String) startMap.get("id"), (String) startMap.get("name"), new LatLng(locationStart.get("latitude"), locationStart.get("longitude")));
                                    Place stop = new Place((String) stopMap.get("id"), (String) stopMap.get("name"), new LatLng(locationStop.get("latitude"), locationStop.get("longitude")));
                                    FirebaseDatabase.getInstance().getReference().child("users").child("customers")
                                            .child(customerRequestId).child("profile").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                HashMap<String, String> value = (HashMap<String, String>) snapshot.getValue();
                                                String name = value.get("name");
                                                String phone = value.get("phone");
                                                String imageurl = value.containsKey("image") ? value.get("image") : "";
                                                User user = new User(key, customerRequestId, name, imageurl, phone, start, stop);
                                                if (!users.contains(user)) {
                                                    Log.d(TAG, "onDataChange: user is" + user);
                                                    users.add(user);
                                                    if (customerRequestViewAdapter != null) {
                                                        customerRequestViewAdapter.notifyDataSetChanged();
                                                    } else {
                                                        customerRequestViewAdapter = new CustomerRequestViewAdapter2(users, getContext(), driverKey);
                                                    }
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
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot != null ) {
                    String key = snapshot.getKey();
                    Optional<User> useRemoved = users.stream().filter(user -> user.getKey().equals(key))
                            .findFirst();
                    useRemoved.ifPresent(user -> {
                        users.remove(useRemoved);
                    });
                    if (customerRequestViewAdapter != null) {
                        customerRequestViewAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        rideAvailableRef.addChildEventListener(childListener);



        airLocation = new AirLocation(getActivity(), new AirLocation.Callback() {
            @Override
            public void onSuccess(ArrayList<Location> arrayList) {
                if (arrayList != null && arrayList.size() == 1) {
                    HashMap<String, Object> location = new HashMap<>();
                    location.put("lat", arrayList.get(0).getLatitude());
                    location.put("log", arrayList.get(0).getLongitude());
                    FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(currentUser.getUid())
                            .child("location").setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onSuccess: arraylist size is" + arrayList.size());
                                Log.d(TAG, "onSuccess() returned: " + arrayList.toString());
                            } else {
                                Log.d(TAG, "onComplete: not successful " + task.getException().getMessage());
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onSuccess: arraylist size is" + arrayList.size());
                }
            }

            @Override
            public void onFailure(AirLocation.LocationFailedEnum locationFailedEnum) {
                Log.d(TAG, "onFailure: getting location failed " + locationFailedEnum.toString());
            }
        }, false, 30000, "Permission is needed");
        airLocation.start();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_request_list, container, false);
        driverKey = availableDriversRef.child("available drivers").push().getKey();
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            customerRequestViewAdapter = new CustomerRequestViewAdapter2(users, context, driverKey);
            recyclerView.setAdapter(customerRequestViewAdapter);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ValueEventListener checkisNull  = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null && snapshot.getChildrenCount() > 0) {
                    snapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                        @Override
                        public void accept(DataSnapshot dataSnapshot) {
                            String key = dataSnapshot.getKey();
                            String rideId = (String) dataSnapshot.getValue();
                            Log.d(TAG, "accept: value is: " + dataSnapshot.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        availableDriversRef.child(driverKey).setValue(currentUser.getUid());
        availableDriversRef.child("driver rides").child(currentUser.getUid()).addValueEventListener(checkisNull);
    }

    @Override
    public void onStop() {
        super.onStop();
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
                FirebaseDatabase.getInstance().getReference().child("available drivers").child(driverKey).setValue(null);
                mAuth.signOut();
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.FirstFragment, false);
//                NavHostFragment.findNavController(this)
//                        .navigate(R.id.action_customerRequestFragment_to_FirstFragment);
        }
        return true;
    }



}