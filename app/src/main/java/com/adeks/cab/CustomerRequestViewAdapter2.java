package com.adeks.cab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adeks.cab.dummy.DummyContent.DummyItem;
import com.adeks.cab.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CustomerRequestViewAdapter2 extends RecyclerView.Adapter<CustomerRequestViewAdapter2.ViewHolder> {

    private final List<User> mValues;
    Context context;

    public CustomerRequestViewAdapter2(List<User> items,Context context) {
        mValues = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_customer_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.user = mValues.get(position);
        holder.custName.setText(mValues.get(position).getName());
        holder.custStopPoint.setText("Stop: " + mValues.get(position).getStart().getName());
        holder.custStartPoint.setText("Start: " + mValues.get(position).getStop().getName());
        String imageUrl = mValues.get(position).getImageUrl();
        if (imageUrl != null || TextUtils.isEmpty(imageUrl)) {
            Picasso.get()
                    .load(imageUrl)
                    .into(holder.custImage);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        public User user;
        public final View mView;
        public final TextView custName;
        public final TextView custStartPoint;
        public final TextView custStopPoint;
        public final CircleImageView custImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            custName = view.findViewById(R.id.cust_name);
            custStartPoint = view.findViewById(R.id.cust_start);
            custStopPoint = view.findViewById(R.id.cust_stop);
            custImage = view.findViewById(R.id.customer_pic);
            mView.setOnClickListener(v -> {
                FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                DriverRequestFragment dialog =  DriverRequestFragment.newInstance(user);
                dialog.show(fragmentManager, "driver request");
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + custName.getText() + "'";
        }


    }
}