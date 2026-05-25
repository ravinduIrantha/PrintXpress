package com.printxpress.app.fragments;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.printxpress.app.R;
import com.printxpress.app.databinding.FragmentTrackOrderBinding;
import com.printxpress.app.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackOrderFragment extends Fragment {

    private FragmentTrackOrderBinding binding;
    private DatabaseReference mDatabase;
    private String orderId;
    private Order currentOrder;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentOrder != null) {
                updateUI(currentOrder);
            }
            refreshHandler.postDelayed(this, 1000); // Refresh every second
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            trackOrder(orderId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void trackOrder(String id) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        mDatabase.child("orders").child(userId).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    currentOrder = snapshot.getValue(Order.class);
                    updateUI(currentOrder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateUI(Order order) {
        if (order == null || getContext() == null) return;

        int activeColor = ContextCompat.getColor(getContext(), R.color.primary);
        int inactiveColor = ContextCompat.getColor(getContext(), android.R.color.darker_gray);

        String status = order.getStatus();
        long now = System.currentTimeMillis();
        long sixHoursInMillis = 6 * 60 * 60 * 1000;

        // 1. Ready Step: Highlight if status is Ready OR past scheduled date
        boolean isReady = "Ready".equals(status) || (order.getScheduledDate() != null && now > order.getScheduledDate());

        // 2. Printing Step: Highlight if status is Printing (or further) OR > 6 hours since creation
        boolean timeThresholdPassed = order.getCreatedAt() != null && (now - order.getCreatedAt() > sixHoursInMillis);
        boolean isPrinting = isReady || "Printing".equals(status) || timeThresholdPassed;

        // Apply colors
        binding.checkProcessing.setColorFilter(activeColor); // Always highlighted
        binding.checkPrinting.setColorFilter(isPrinting ? activeColor : inactiveColor);
        binding.checkReady.setColorFilter(isReady ? activeColor : inactiveColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
