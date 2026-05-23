package com.printxpress.app.fragments;

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

    private void trackOrder(String id) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        mDatabase.child("orders").child(userId).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    updateUI(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateUI(String status) {
        int activeColor = ContextCompat.getColor(getContext(), R.color.primary);
        int inactiveColor = ContextCompat.getColor(getContext(), android.R.color.darker_gray);

        if ("Processing".equals(status)) {
            binding.checkProcessing.setColorFilter(activeColor);
            binding.checkPrinting.setColorFilter(inactiveColor);
            binding.checkReady.setColorFilter(inactiveColor);
        } else if ("Printing".equals(status)) {
            binding.checkProcessing.setColorFilter(activeColor);
            binding.checkPrinting.setColorFilter(activeColor);
            binding.checkReady.setColorFilter(inactiveColor);
        } else if ("Ready".equals(status)) {
            binding.checkProcessing.setColorFilter(activeColor);
            binding.checkPrinting.setColorFilter(activeColor);
            binding.checkReady.setColorFilter(activeColor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
