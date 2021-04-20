package com.adeks.cab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationrequest;
    private LatLng customerPickUpLocation;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String customerID;
    Marker driverMarker;
    private DatabaseReference customerDatabaseRef;
    private DatabaseReference driverAvailableRef;
    private DatabaseReference driversRef;
    private DatabaseReference driverLocationRef;

    private boolean isLocationEnabled;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private int radius = 1;
    private Button callCab;
    private boolean driverFound = false, requestType = false;
    private String driverFoundId;

    private ValueEventListener driverLocationRefListener;
    private GeoQuery geoQuery;
    private Marker pickUpMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = currentUser.getUid();
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers working");

        callCab = findViewById(R.id.call_cab_btn);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void getLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnabled = true;
                    requestLocationUpdates();
                    updateUI();
                }
        }
    }

    private void updateUI() {
        try {
            if (isLocationEnabled) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        locationrequest = new LocationRequest();
        locationrequest.setInterval(1000);
        locationrequest.setFastestInterval(1000);
        locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationrequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void callCab(View view) {
        if (requestType) {
           cancelCab();
            callCab.setText("Call a cab");
        } else {
            requestType = true;
            GeoFire geoFire = new GeoFire(customerDatabaseRef);
            geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

            customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
            callCab.setText("Finding Closest driver...");
            getClosestDriver();
        }



    }

    private void cancelCab() {
        requestType = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);

        if (driverFound) {
            driversRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child("drivers").child(driverFoundId).child("customerRideId");
            driversRef.removeValue();
            driverFoundId = null;
        }
        driverFound = false;
        radius = 1;
        GeoFire geoFire = new GeoFire(customerDatabaseRef);
        geoFire.removeLocation(customerID);
        if (pickUpMarker != null) {
            pickUpMarker.remove();
        }
        if (driverMarker != null){
            driverMarker.remove();
        }
    }

    private void getClosestDriver() {
        GeoFire geoFire = new GeoFire(driverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude,customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundId = key;

                    driversRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundId);
                    HashMap<String,Object> driverMap = new HashMap<>();
                    driverMap.put("customerRideId",customerID);
                    driversRef.updateChildren(driverMap);

                    gettingDriverLocation();
                    callCab.setText("Looking for driver location");
                    Toast.makeText(CustomersMapActivity.this, "Driver found with id"+ key, LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius += 1;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingDriverLocation() {
        driverLocationRefListener = driverLocationRef.child(driverFoundId).child("l")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && requestType) {
                        List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                        double locationLat = 0;
                        double locationLng = 0;
                        callCab.setText("Driver found");
                        if (driverLocationMap.get(0) != null) {
                            locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                        }
                        if (driverLocationMap.get(1) != null) {
                            locationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                        }
                        LatLng driverLatLng = new LatLng(locationLat,locationLng);
                        if (driverMarker != null){
                            driverMarker.remove();
                        }
                        Location location2 = new Location("");
                        location2.setLatitude(driverLatLng.latitude);
                        location2.setLongitude(driverLatLng.longitude);

                        Location location1 = new Location("");
                        location1.setLatitude(customerPickUpLocation.latitude);
                        location1.setLongitude(customerPickUpLocation.longitude);

                        double distance = location1.distanceTo(location2);
                        if (distance < 90) {
                            callCab.setText("Driver is around");
                        } else {
                            callCab.setText("Driver found "+ distance);
                        }
                        driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    public void logoutCustomer(View view) {
        logoutCustomer();
    }

    private void logoutCustomer() {
        cancelCab();
        mAuth.signOut();
        gotoSignInActivity();
    }

    private void gotoSignInActivity() {
        Intent sigInIntent = new Intent(this, SignInActivity.class);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sigInIntent);
    }
}