package com.adeks.cab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
/**
 * This is the first fragment that every user sees
 * after signing into the application
 *
 * It gives the user two choices either to sign in as a user
 * or as a driver
 * */
public class MainFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
        view.findViewById(R.id.order_cab).setOnClickListener(v -> {
            MainFragmentDirections.ActionFirstFragmentToSecondFragment action =
                    MainFragmentDirections.actionFirstFragmentToSecondFragment(false);
            NavHostFragment.findNavController(MainFragment.this)
                    .navigate(action);
        });
        view.findViewById(R.id.driver_signin).setOnClickListener((v) -> {
            MainFragmentDirections.ActionFirstFragmentToSecondFragment action =
                    MainFragmentDirections.actionFirstFragmentToSecondFragment(true);
            NavHostFragment.findNavController(MainFragment.this)
                    .navigate(action);
        });
    }
}