package com.adeks.cab;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adeks.cab.models.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *This Adapter is for displaying all available Drivers on the
 * Customer or user about to order a cab
 *
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "MyItemRecyclerViewAdapt";
    private final List<Driver> mValues;
    private final Context context;

    public MyItemRecyclerViewAdapter(List<Driver> items, Context context) {
        mValues = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_drivers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.driver = mValues.get(position);
        holder.uid = mValues.get(position).getId();
        holder.driverKey = mValues.get(position).getKey();
        holder.driverName.setText(mValues.get(position).getName());
        holder.driverCar.setText(mValues.get(position).getCarDetails());
        holder.driverDistance.setText("0 metres");
        String imageUrl = mValues.get(position).getImageUrl();
        if (imageUrl != null && !TextUtils.isEmpty(imageUrl)) {
            Picasso.get()
                    .load(imageUrl)
                    .into(holder.driverImage);
        }
//        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Driver driver;
        public final View mView;
        public String driverKey;
        public String uid;
        public final TextView driverName;
        public final TextView driverCar;
        public final TextView driverDistance;
        private final CircleImageView driverImage;
        DatabaseReference driverRidesRef;
        FirebaseAuth mAuth;
        ProgressDialog mProgressDialog;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAuth = FirebaseAuth.getInstance();
            driverCar = view.findViewById(R.id.driver_car);
            driverName = view.findViewById(R.id.driver_name);
            driverDistance = view.findViewById(R.id.driver_distance);
            driverImage = view.findViewById(R.id.driver_pic);

            view.setOnClickListener(v ->{
                driverRidesRef = FirebaseDatabase.getInstance().getReference().child("rides")
                        .child(uid);
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setTitle("Ride");
                mProgressDialog.setMessage("Waiting for Driver to accept");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();
                mProgressDialog.setCancelable(true);
                String childKey = driverRidesRef.push().getKey();
                driverRidesRef.child(childKey).child("user").setValue(mAuth.getCurrentUser().getUid());
                mProgressDialog.setOnCancelListener(dialog -> {
                    driverRidesRef.child(childKey).setValue(null);
                    dialog.dismiss();
                    Toast.makeText(context, "Driver canceled",Toast.LENGTH_SHORT).show();
                });
                driverRidesRef.child(childKey).child("isAccepted").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null ){
                            Log.d(TAG, "onDataChange: snapshot is" + snapshot.toString());
                            String value = (String) snapshot.getValue();
                            if (value != null ) {
                                if (value.equals("true")) {
                                    mProgressDialog.dismiss();
                                    DriversFragmentDirections.ActionDriversFragmentToDriverDetailsFragment action = DriversFragmentDirections.actionDriversFragmentToDriverDetailsFragment(driver, childKey);
                                    Navigation.findNavController(mView).navigate(action);
                                } else if (value.equals("false")) {
                                    driverRidesRef.child(childKey).setValue(null);
                                    mProgressDialog.dismiss();
                                    Toast.makeText(context, "Driver cancelled request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + driverName.getText() + "'";
        }
    }
}