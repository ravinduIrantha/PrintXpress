package com.printxpress.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.printxpress.app.R;
import com.printxpress.app.databinding.FragmentOrderDetailBinding;
import com.printxpress.app.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderDetailFragment extends Fragment {

    private FragmentOrderDetailBinding binding;
    private DatabaseReference mDatabase;
    private String orderId;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getUid();

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            loadOrderDetails(orderId);
        }

        binding.buttonTrackOrder.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            Navigation.findNavController(view).navigate(R.id.action_detail_to_track, bundle);
        });

        binding.buttonEditOrder.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            Navigation.findNavController(view).navigate(R.id.action_detail_to_editOrder, bundle);
        });

        binding.buttonCancelOrder.setOnClickListener(v -> confirmCancellation());
    }

    private void loadOrderDetails(String id) {
        if (userId == null) return;

        mDatabase.child("orders").child(userId).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                Order order = snapshot.getValue(Order.class);
                if (order != null) {
                    String category = order.getCategory() != null ? order.getCategory() : "Item";
                    String displayTitle = category;
                    if (category.equalsIgnoreCase("Cards") || category.equalsIgnoreCase("Business Cards")) {
                        displayTitle = "Card";
                    } else if (category.endsWith("s") && !category.equalsIgnoreCase("T-Shirts")) {
                        displayTitle = category.substring(0, category.length() - 1);
                    }
                    
                    binding.textOrderDetailHeader.setText(displayTitle + " Order");
                    binding.textOrderIDSubHeader.setText("Order #" + (id.length() > 6 ? id.substring(0, 6) : id));

                    binding.textOrderStatusDetail.setText(order.getStatus());
                    binding.textOrderQuantity.setText("Quantity: " + order.getQuantity());
                    binding.textOrderInstructions.setText(order.getInstructions());
                    binding.textOrderTotal.setText("Rs. " + String.format("%.2f", order.getTotalAmount()));
                    binding.textOrderSpecs.setText("Material: " + order.getMaterial() + ", Size: " + order.getSize());

                    checkEligibility(order);

                    binding.textOrderProductName.setText(order.getCategory());

                    if (order.getProductId() != null) {
                        mDatabase.child("products").child(order.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot pSnapshot) {
                                if (binding == null) return;
                                if (pSnapshot.exists()) {
                                    String productName = pSnapshot.child("name").getValue(String.class);
                                    if (productName != null) {
                                        binding.textOrderProductName.setText(productName);
                                    }
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
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkEligibility(Order order) {
        long hoursInMillis = 6 * 60 * 60 * 1000;
        boolean isRecent = (System.currentTimeMillis() - order.getCreatedAt()) < hoursInMillis;
        boolean isProcessing = "Processing".equals(order.getStatus());

        if (isRecent && isProcessing) {
            binding.buttonEditOrder.setVisibility(View.VISIBLE);
            binding.buttonCancelOrder.setVisibility(View.VISIBLE);
        } else {
            binding.buttonEditOrder.setVisibility(View.GONE);
            binding.buttonCancelOrder.setVisibility(View.GONE);
        }
    }

    private void confirmCancellation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.delete_yes, (dialog, which) -> cancelOrder())
                .setNegativeButton(R.string.delete_no, null)
                .show();
    }

    private void cancelOrder() {
        if (userId == null || orderId == null) return;

        mDatabase.child("orders").child(userId).child(orderId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), R.string.order_deleted_msg, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
