package com.printxpress.app.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.Navigation;
import com.printxpress.app.R;
import com.printxpress.app.adapters.OrderAdapter;
import com.printxpress.app.databinding.FragmentOrderBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private DatabaseReference mDatabase;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private final java.util.Map<String, String> orderStatusMap = new java.util.HashMap<>();
    private boolean isInitialLoad = true;
    private ValueEventListener ordersListener;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        setupRecyclerView(view);
        loadOrders();
    }

    private void setupRecyclerView(View view) {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, order -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", order.getId());
            Navigation.findNavController(view).navigate(R.id.action_order_to_detail, bundle);
        });
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        binding.progressBarOrders.setVisibility(View.VISIBLE);
        ordersListener = mDatabase.child("orders").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        binding.progressBarOrders.setVisibility(View.GONE);
                        orderList.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Order order = postSnapshot.getValue(Order.class);
                            if (order != null) {
                                String id = postSnapshot.getKey();
                                order.setId(id);
                                orderList.add(order);

                                // Check for status changes
                                String newStatus = order.getStatus();
                                if (!isInitialLoad && orderStatusMap.containsKey(id)) {
                                    String oldStatus = orderStatusMap.get(id);
                                    if (newStatus != null && !newStatus.equals(oldStatus)) {
                                        String displayId = (id != null) ? (id.length() > 6 ? id.substring(0, 6) : id) : "Unknown";
                                        NotificationHelper.showOrderNotification(requireContext(), 
                                                "Order Status Update", 
                                                "Order #" + displayId + " is now " + newStatus);
                                    }
                                }
                                orderStatusMap.put(id, newStatus);
                            }
                        }
                        isInitialLoad = false;
                        // Reverse list to show newest first if needed, or sort by timestamp
                        Collections.reverse(orderList);
                        orderAdapter.notifyDataSetChanged();

                        if (orderList.isEmpty()) {
                            binding.textNoOrders.setVisibility(View.VISIBLE);
                        } else {
                            binding.textNoOrders.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (binding == null) return;
                        binding.progressBarOrders.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabase != null && userId != null && ordersListener != null) {
            mDatabase.child("orders").child(userId).removeEventListener(ordersListener);
        }
        binding = null;
    }
}
