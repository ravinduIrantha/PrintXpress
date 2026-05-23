package com.printxpress.app.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.printxpress.app.R;
import com.printxpress.app.databinding.FragmentPrintBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.models.Product;
import com.printxpress.app.utils.NotificationHelper;
import com.printxpress.app.utils.PriceCalculator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrintFragment extends Fragment {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final String[] CATEGORIES = {"Cards", "Flyers", "Posters", "Banners", "Stickers", "T-Shirts", "Mugs"};
    private static final String[][] SIZES = {
            {"Standard (3.5\" × 2\")", "Square (2.5\" × 2.5\")", "Mini (3.5\" × 1.5\")"},
            {"A4 (210 × 297 mm)", "A5 (148 × 210 mm)", "A6 (105 × 148 mm)", "DL (99 × 210 mm)"},
            {"A3 (297 × 420 mm)", "A2 (420 × 594 mm)", "A1 (594 × 841 mm)", "A0 (841 × 1189 mm)"},
            {"2ft × 4ft", "3ft × 6ft", "4ft × 8ft", "Custom Size"},
            {"Small (5 × 5 cm)", "Medium (10 × 10 cm)", "Large (15 × 15 cm)", "Custom Size"},
            {"XS", "S", "M", "L", "XL", "XXL"},
            {"11 oz", "15 oz"}
    };
    private static final String[][] MATERIALS = {
            {"Glossy", "Matte", "Uncoated", "Premium Silk"},
            {"Glossy", "Matte", "Uncoated"},
            {"Glossy", "Matte", "Canvas"},
            {"Vinyl", "Fabric", "Mesh"},
            {"Glossy Vinyl", "Matte Vinyl", "Clear Vinyl"},
            {"100% Cotton", "100% Polyester", "Cotton-Poly Blend"},
            {"Ceramic", "Enamel"}
    };

    // ── Fields ────────────────────────────────────────────────────────────────

    private FragmentPrintBinding binding;
    private DatabaseReference mDatabase;
    private Uri selectedImageUri;
    private String existingBase64;
    private String orderId;
    private String productId;
    private String userId;
    private double currentBasePrice = 0.0;
    private long minScheduledTimestamp = 0;
    private long selectedScheduledTimestamp = 0;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private String userAddress = null;

    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (binding != null) {
                        binding.layoutUploadHint.setVisibility(View.GONE);
                        binding.imagePreview.setImageTintList(null);
                        Glide.with(this).load(selectedImageUri).into(binding.imagePreview);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrintBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getUid();

        setupDropdowns();
        setupListeners();

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            productId = getArguments().getString("productId");
            String cat = getArguments().getString("category");
            String size = getArguments().getString("initialSize");
            String mat = getArguments().getString("initialMaterial");

            if (orderId != null) {
                loadOrderData();
            } else if (productId != null) {
                loadProductData(productId, size, mat);
            } else if (cat != null) {
                preFillCategory(cat, size, mat);
            }
        }
    }

    private void setupDropdowns() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.spinnerCategory.setAdapter(catAdapter);

        binding.spinnerCategory.setOnItemClickListener((parent, v, position, id) -> {
            populateSizeDropdown(position, null);
            populateMaterialDropdown(position, null);
            updatePrice();
        });
    }

    private void setupListeners() {
        binding.cardSelectImage.setOnClickListener(v -> pickMedia.launch("image/*"));
        binding.buttonUploadOrder.setOnClickListener(v -> submitOrder());
        
        binding.tvLearnMore.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_print_to_learnMore);
        });

        binding.editQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { updatePrice(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.spinnerSize.setOnItemClickListener((parent, v, position, id) -> updatePrice());
        binding.spinnerMaterial.setOnItemClickListener((parent, v, position, id) -> updatePrice());
        binding.radioGroupDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDelivery) {
                checkAddressAvailability();
            }
            updatePrice();
        });
        binding.editScheduledDate.setOnClickListener(v -> showDatePicker());
    }

    private void loadOrderData() {
        if (orderId == null || userId == null) return;
        mDatabase.child("orders").child(userId).child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;
                Order order = snapshot.getValue(Order.class);
                if (order != null) {
                    preFillFromOrder(order);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadProductData(String pId, String initialSize, String initialMat) {
        mDatabase.child("products").child(pId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    currentBasePrice = product.getPrice();
                    preFillCategory(product.getCategory(), initialSize, initialMat);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void preFillCategory(String category, String size, String material) {
        binding.spinnerCategory.setText(category, false);
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) {
                populateSizeDropdown(i, size);
                populateMaterialDropdown(i, material);
                break;
            }
        }
        updatePrice();
    }

    private void preFillFromOrder(Order order) {
        binding.textUploadTitle.setText(R.string.edit_order);
        binding.textUploadSubtitle.setText(R.string.edit_order_subtitle);
        binding.buttonUploadOrder.setText(R.string.save_changes);

        if (order.getDesignBase64() != null && !order.getDesignBase64().isEmpty()) {
            existingBase64 = order.getDesignBase64();
            byte[] decodedString = Base64.decode(existingBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            binding.imagePreview.setImageTintList(null);
            binding.imagePreview.setImageBitmap(decodedByte);
            binding.layoutUploadHint.setVisibility(View.GONE);
        }

        preFillCategory(order.getCategory(), order.getSize(), order.getMaterial());
        binding.editQuantity.setText(String.valueOf(order.getQuantity()));
        binding.instructionsEditText.setText(order.getInstructions());
        if ("Home Delivery".equals(order.getDeliveryType())) {
            binding.radioDelivery.setChecked(true);
        } else {
            binding.radioPickup.setChecked(true);
        }
        updatePrice();
    }

    private void populateSizeDropdown(int catIndex, String currentSize) {
        String[] sizes = SIZES[catIndex];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sizes);
        binding.spinnerSize.setAdapter(adapter);
        binding.spinnerSize.setText(currentSize != null ? currentSize : sizes[0], false);
    }

    private void populateMaterialDropdown(int catIndex, String currentMaterial) {
        String[] materials = MATERIALS[catIndex];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, materials);
        binding.spinnerMaterial.setAdapter(adapter);
        binding.spinnerMaterial.setText(currentMaterial != null ? currentMaterial : materials[0], false);
    }

    private void updatePrice() {
        if (binding == null) return;
        
        String category = binding.spinnerCategory.getText().toString();
        String size = binding.spinnerSize.getText().toString();
        String material = binding.spinnerMaterial.getText().toString();
        String qtyStr = binding.editQuantity.getText() != null ? binding.editQuantity.getText().toString() : "1";
        
        int qty = 1;
        try { qty = Integer.parseInt(qtyStr); } catch (Exception ignored) {}
        if (qty < 1) qty = 1;

        boolean isDelivery = binding.radioDelivery.isChecked();

        double total = PriceCalculator.calculateTotal(category, size, material, qty, isDelivery);
        binding.textEstimatedTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", total));

        updateSchedulingLogic(category, qty, isDelivery);
    }

    private void updateSchedulingLogic(String category, int qty, boolean isDelivery) {
        int baseDays = 2; // Default
        switch (category) {
            case "Cards": baseDays = 2; break;
            case "Posters": case "Flyers": baseDays = 1; break;
            case "Banners": baseDays = 3; break;
            case "T-Shirts": case "Mugs": baseDays = 4; break;
            case "Stickers": baseDays = 2; break;
        }

        // Quantity Logic: +1 day per X units
        int qtyBuffer = 0;
        if (category.equals("Cards") || category.equals("Flyers") || category.equals("Stickers")) {
            qtyBuffer = qty / 500;
        } else {
            qtyBuffer = qty / 50;
        }

        int deliveryBuffer = isDelivery ? 2 : 0;
        int totalDaysNeeded = baseDays + qtyBuffer + deliveryBuffer;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, totalDaysNeeded);
        minScheduledTimestamp = cal.getTimeInMillis();

        binding.textEarliestDateHint.setText("Earliest possible: " + dateFormatter.format(cal.getTime()));

        // Reset selected if it's now invalid
        if (selectedScheduledTimestamp < minScheduledTimestamp) {
            selectedScheduledTimestamp = minScheduledTimestamp;
            binding.editScheduledDate.setText(dateFormatter.format(cal.getTime()));
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedScheduledTimestamp > 0) cal.setTimeInMillis(selectedScheduledTimestamp);

        android.app.DatePickerDialog picker = new android.app.DatePickerDialog(requireContext(), 
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedScheduledTimestamp = selected.getTimeInMillis();
                    binding.editScheduledDate.setText(dateFormatter.format(selected.getTime()));
                }, 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        
        picker.getDatePicker().setMinDate(minScheduledTimestamp);
        picker.show();
    }

    private void checkAddressAvailability() {
        if (userId == null) return;
        
        mDatabase.child("users").child(userId).child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;
                userAddress = snapshot.getValue(String.class);
                if (userAddress == null || userAddress.trim().isEmpty()) {
                    showMissingAddressDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showMissingAddressDialog() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Delivery Address Required")
                .setMessage("Please provide a home address in your profile to use the Home Delivery option.")
                .setPositiveButton("Add Address", (dialog, which) -> {
                    Navigation.findNavController(requireView()).navigate(R.id.action_print_to_address);
                })
                .setNegativeButton("Pickup Instead", (dialog, which) -> {
                    binding.radioPickup.setChecked(true);
                })
                .setCancelable(false)
                .show();
    }

    private void submitOrder() {
        if (userId == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        String instructions = binding.instructionsEditText.getText() != null ? binding.instructionsEditText.getText().toString().trim() : "";
        if (selectedImageUri == null && existingBase64 == null && instructions.isEmpty()) {
            binding.instructionsLayout.setError("Instructions mandatory if no image provided");
            return;
        }
        binding.instructionsLayout.setError(null);

        String category = binding.spinnerCategory.getText().toString();
        String size = binding.spinnerSize.getText().toString();
        String material = binding.spinnerMaterial.getText().toString();
        String qtyStr = binding.editQuantity.getText() != null ? binding.editQuantity.getText().toString() : "";

        if (category.isEmpty() || size.isEmpty() || material.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.radioDelivery.isChecked() && (userAddress == null || userAddress.trim().isEmpty())) {
            showMissingAddressDialog();
            return;
        }

        binding.progressBarUpload.setVisibility(View.VISIBLE);
        binding.buttonUploadOrder.setEnabled(false);

        new Thread(() -> {
            try {
                String base64 = existingBase64;
                if (selectedImageUri != null) {
                    base64 = encodeImageToBase64(selectedImageUri);
                }

                Map<String, Object> order = new HashMap<>();
                order.put("category", category);
                order.put("size", size);
                order.put("material", material);
                order.put("quantity", Integer.parseInt(qtyStr));
                order.put("instructions", instructions);
                order.put("designBase64", base64);
                order.put("deliveryType", binding.radioDelivery.isChecked() ? "Home Delivery" : "Pickup");
                order.put("status", "Processing");
                order.put("userId", userId);
                order.put("productId", productId);
                order.put("createdAt", System.currentTimeMillis());
                order.put("scheduledDate", selectedScheduledTimestamp);
                
                String totalStr = binding.textEstimatedTotal.getText().toString().replace("Rs. ", "");
                order.put("totalAmount", Double.parseDouble(totalStr));

                if (orderId != null) {
                    mDatabase.child("orders").child(userId).child(orderId).updateChildren(order)
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded() || binding == null) return;
                                NotificationHelper.showOrderNotification(requireContext(), "Order Updated", "Your order for " + category + " has been updated.");
                                handleSuccess(R.string.order_updated_msg);
                            })
                            .addOnFailureListener(this::handleFailure);
                } else {
                    mDatabase.child("orders").child(userId).push().setValue(order)
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded() || binding == null) return;
                                NotificationHelper.showOrderNotification(requireContext(), "Order Placed", "Your order for " + category + " has been successfully placed.");
                                handleSuccess(R.string.submit_for_printing);
                            })
                            .addOnFailureListener(this::handleFailure);
                }

            } catch (Exception e) {
                handleFailure(e);
            }
        }).start();
    }

    private void handleSuccess(int msgRes) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (!isAdded() || binding == null) return;
            binding.progressBarUpload.setVisibility(View.GONE);
            Toast.makeText(getContext(), msgRes, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        });
    }

    private void handleFailure(Exception e) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (!isAdded() || binding == null) return;
            binding.progressBarUpload.setVisibility(View.GONE);
            binding.buttonUploadOrder.setEnabled(true);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String encodeImageToBase64(Uri uri) throws Exception {
        InputStream is = requireContext().getContentResolver().openInputStream(uri);
        Bitmap bm = BitmapFactory.decodeStream(is);
        int max = 1024;
        if (bm.getWidth() > max || bm.getHeight() > max) {
            float r = Math.min((float) max / bm.getWidth(), (float) max / bm.getHeight());
            bm = Bitmap.createScaledBitmap(bm, Math.round(r * bm.getWidth()), Math.round(r * bm.getHeight()), true);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 70, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
