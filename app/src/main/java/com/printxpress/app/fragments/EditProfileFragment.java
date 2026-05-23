package com.printxpress.app.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.printxpress.app.databinding.FragmentEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private DatabaseReference mDatabase;
    private String userId;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.imageEditProfile.setImageURI(selectedImageUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getUid();

        loadCurrentInfo();

        binding.imageEditProfile.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.buttonSaveProfile.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile();
            } else {
                saveProfile(null);
            }
        });
    }

    private void loadCurrentInfo() {
        if (userId == null) return;
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                if (snapshot.exists()) {
                    binding.editNameEditText.setText(snapshot.child("name").getValue(String.class));
                    binding.editPhoneEditText.setText(snapshot.child("phone").getValue(String.class));
                    String base64Image = snapshot.child("profileImageBase64").getValue(String.class);
                    if (base64Image != null && !base64Image.isEmpty()) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        binding.imageEditProfile.setImageBitmap(decodedByte);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void uploadImageAndSaveProfile() {
        binding.progressBarEdit.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                if (getContext() == null) return;
                InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Resize for profile (smaller than designs)
                int size = 512;
                if (bitmap.getWidth() > size || bitmap.getHeight() > size) {
                    float ratio = Math.min((float) size / bitmap.getWidth(), (float) size / bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(ratio * bitmap.getWidth()), Math.round(ratio * bitmap.getHeight()), true);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                String base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> saveProfile(base64Image));
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBarEdit.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Encoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void saveProfile(String base64Image) {
        String name = "";
        if (binding.editNameEditText.getText() != null) {
            name = binding.editNameEditText.getText().toString().trim();
        }

        String phone = "";
        if (binding.editPhoneEditText.getText() != null) {
            phone = binding.editPhoneEditText.getText().toString().trim();
        }

        if (name.isEmpty()) {
            binding.editNameLayout.setError("Name required");
            binding.progressBarEdit.setVisibility(View.GONE);
            return;
        }

        binding.progressBarEdit.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        if (base64Image != null) {
            updates.put("profileImageBase64", base64Image);
        }

        mDatabase.child("users").child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    if (binding == null) return;
                    binding.progressBarEdit.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    View fragmentView = getView();
                    if (fragmentView != null) {
                        Navigation.findNavController(fragmentView).popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBarEdit.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
