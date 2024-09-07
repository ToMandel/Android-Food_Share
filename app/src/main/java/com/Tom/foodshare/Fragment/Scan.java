package com.Tom.foodshare.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.Tom.foodshare.Activity.Camera;
import com.Tom.foodshare.Activity.Login;
import com.Tom.foodshare.Class.User;
import com.Tom.foodshare.R;

public class Scan extends Fragment {
    public User user;
    private ProgressBar progressBar;
    private boolean isBackPressed = false;  // Flag to track back button press

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_scan, container, false);

        progressBar = view.findViewById(R.id.scan_progresBar);

        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(Login.LOGIN_USER_KEY);
        }

        // Check if user is null
        if (this.user == null) {
            Toast.makeText(requireContext(), "User data is missing. Please log in again.", Toast.LENGTH_LONG).show();
            // Optionally, navigate back to login or show an error screen
            return view;
        }

        // Proceed to start the Camera activity
        Intent camera_intent = new Intent(requireContext(), Camera.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Login.LOGIN_USER_KEY, user);
        camera_intent.putExtras(bundle);
        startActivity(camera_intent);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                // Ensure context is not null and back button was not pressed before showing Toast
                if (isAdded() && getContext() != null && !isBackPressed) {
                    Toast.makeText(requireContext(), "Items Added Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isBackPressed = false;  // Reset flag when the fragment is resumed
    }

    @Override
    public void onPause() {
        super.onPause();
        isBackPressed = true;  // Set the flag when the fragment is paused (user navigates away)
    }
}
