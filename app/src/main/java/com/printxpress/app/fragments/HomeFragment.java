package com.printxpress.app.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.Navigation;
import com.printxpress.app.R;
import com.printxpress.app.adapters.CategoryAdapter;
import com.printxpress.app.adapters.ProductAdapter;
import com.printxpress.app.databinding.FragmentHomeBinding;
import com.printxpress.app.models.Category;
import com.printxpress.app.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference mDatabase;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categoryList;
    private List<Product> productList;
    private ValueEventListener categoriesListener;
    private ValueEventListener productsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ INIT FIREBASE DB
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // ✅ SAFE USER CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (email != null) {
                String name = email.split("@")[0];
                binding.textWelcome.setText("Welcome, " + name + "!");
            } else {
                binding.textWelcome.setText("Welcome!");
            }

        } else {
            binding.textWelcome.setText("Welcome!");
        }

        setupRecyclerViews(view);
        loadCategories();
        loadFeaturedProducts();

        binding.viewAllCategories.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.action_home_to_category));

        setupSearch();
    }

    private void setupSearch() {
        binding.searchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = binding.searchView.getText().toString().trim();
                if (!query.isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("searchQuery", query);
                    Navigation.findNavController(requireView()).navigate(R.id.action_home_to_category, bundle);
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews(View view) {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Bundle bundle = new Bundle();
            bundle.putString("categoryName", category.getName());
            Navigation.findNavController(view).navigate(R.id.action_home_to_category, bundle);
        });
        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCategories.setAdapter(categoryAdapter);

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getId());
            Navigation.findNavController(view).navigate(R.id.action_home_to_productDetail, bundle);
        });
        binding.recyclerFeatured.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerFeatured.setAdapter(productAdapter);
    }

    private void loadCategories() {
        categoriesListener = mDatabase.child("categories").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadFeaturedProducts() {
        productsListener = mDatabase.child("products").limitToFirst(10).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                productList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabase != null) {
            if (categoriesListener != null) {
                mDatabase.child("categories").removeEventListener(categoriesListener);
            }
            if (productsListener != null) {
                mDatabase.child("products").removeEventListener(productsListener);
            }
        }
        binding = null;
    }
}
