package com.printxpress.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.printxpress.app.R;
import com.printxpress.app.adapters.CategoryAdapter;
import com.printxpress.app.adapters.ProductAdapter;
import com.printxpress.app.databinding.FragmentCategoryBinding;
import com.printxpress.app.models.Category;
import com.printxpress.app.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private DatabaseReference mDatabase;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList;
    private List<Category> categoryList;
    private String categoryName;
    private String searchQuery;
    private ValueEventListener categoriesListener;
    private com.google.firebase.database.Query currentProductsQuery;
    private ValueEventListener productsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        setupRecyclerViews();
        
        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName");
            searchQuery = getArguments().getString("searchQuery");
        }
        
        loadCategories();
        
        if (searchQuery != null && !searchQuery.isEmpty()) {
            binding.textCategoryTitle.setText("Search Results for \"" + searchQuery + "\"");
            loadSearchResults(searchQuery);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            binding.textCategoryTitle.setText(categoryName);
            loadCategoryProducts(categoryName);
        } else {
            binding.textCategoryTitle.setText("Explore Categories");
            loadAllProducts();
        }
    }

    private void setupRecyclerViews() {
        // Products RecyclerView
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, product -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_category_to_productDetail, bundle);
        });
        binding.recyclerCategoryProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCategoryProducts.setAdapter(productAdapter);

        // Categories Filter RecyclerView
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            searchQuery = null; // Clear search when selecting a category
            categoryName = category.getName();
            binding.textCategoryTitle.setText(categoryName);
            loadCategoryProducts(categoryName);
        });
        binding.recyclerCategoriesFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCategoriesFilter.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        categoriesListener = mDatabase.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                categoryList.clear();
                
                // Optional: Add an "All" category if needed
                // categoryList.add(new Category("all", "All", "ic_all")); 

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void removeProductsListener() {
        if (currentProductsQuery != null && productsListener != null) {
            currentProductsQuery.removeEventListener(productsListener);
        }
    }

    private void loadCategoryProducts(String name) {
        removeProductsListener();
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        currentProductsQuery = mDatabase.child("products").orderByChild("category").equalTo(name);
        productsListener = currentProductsQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        handleDataChange(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleCancelled();
                    }
                });
    }

    private void loadSearchResults(String query) {
        removeProductsListener();
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        currentProductsQuery = mDatabase.child("products");
        productsListener = currentProductsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                binding.progressBarCategory.setVisibility(View.GONE);
                productList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    if (product != null && product.getName() != null && 
                            product.getName().toLowerCase().contains(query.toLowerCase())) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleCancelled();
            }
        });
    }

    private void loadAllProducts() {
        removeProductsListener();
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        currentProductsQuery = mDatabase.child("products");
        productsListener = currentProductsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleDataChange(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleCancelled();
            }
        });
    }

    private void handleDataChange(DataSnapshot snapshot) {
        if (binding == null) return;
        binding.progressBarCategory.setVisibility(View.GONE);
        productList.clear();
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            Product product = dataSnapshot.getValue(Product.class);
            if (product != null) {
                productList.add(product);
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void handleCancelled() {
        if (binding == null) return;
        binding.progressBarCategory.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabase != null && categoriesListener != null) {
            mDatabase.child("categories").removeEventListener(categoriesListener);
        }
        removeProductsListener();
        binding = null;
    }
}
