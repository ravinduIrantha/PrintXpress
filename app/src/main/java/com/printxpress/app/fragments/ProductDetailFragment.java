package com.printxpress.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.printxpress.app.R;
import com.printxpress.app.databinding.FragmentProductDetailBinding;
import com.printxpress.app.models.Product;
import com.printxpress.app.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private DatabaseReference mDatabase;
    private Product product;
    private String productId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            loadProductDetails(productId);
        }

        binding.buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadProductDetails(String id) {
        mDatabase.child("products").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    product = snapshot.getValue(Product.class);
                    if (product != null) {
                        binding.textProductNameDetail.setText(product.getName());
                        binding.textProductPriceDetail.setText(String.format(Locale.getDefault(), "Rs. %.2f", product.getPrice()));
                        binding.textProductDescDetail.setText(product.getDescription());

                        Object imageSource = product.getImageUrl();
                        if (product.getImageUrl() != null && !product.getImageUrl().startsWith("http") && getContext() != null) {
                            int resId = getContext().getResources().getIdentifier(
                                    product.getImageUrl(), "drawable", getContext().getPackageName());
                            if (resId != 0) {
                                imageSource = resId;
                            }
                        }

                        Glide.with(ProductDetailFragment.this).load(imageSource).into(binding.imageProductDetail);

                        if (product.getMaterials() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, product.getMaterials());
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerMaterial.setAdapter(adapter);
                        }

                        if (product.getSizes() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, product.getSizes());
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerSize.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void placeOrder() {
        if (product == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("productId", productId);
        bundle.putString("category", product.getCategory());
        
        if (binding.spinnerSize.getSelectedItem() != null) {
            bundle.putString("initialSize", binding.spinnerSize.getSelectedItem().toString());
        }
        if (binding.spinnerMaterial.getSelectedItem() != null) {
            bundle.putString("initialMaterial", binding.spinnerMaterial.getSelectedItem().toString());
        }

        String qty = binding.editQuantity.getText().toString();
        bundle.putString("initialQuantity", qty);

        Navigation.findNavController(requireView()).navigate(R.id.action_productDetail_to_upload, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
