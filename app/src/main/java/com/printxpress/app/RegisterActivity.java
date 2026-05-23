package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.databinding.ActivityRegisterBinding;
import com.printxpress.app.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.goToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = binding.nameEditText.getText() != null ? binding.nameEditText.getText().toString().trim() : "";
        String email = binding.emailEditTextReg.getText() != null ? binding.emailEditTextReg.getText().toString().trim() : "";
        String phone = binding.phoneEditText.getText() != null ? binding.phoneEditText.getText().toString().trim() : "";
        String password = binding.passwordEditTextReg.getText() != null ? binding.passwordEditTextReg.getText().toString().trim() : "";
        String confirmPassword = binding.confirmPasswordEditText.getText() != null ? binding.confirmPasswordEditText.getText().toString().trim() : "";

        if (ValidationUtils.isEmpty(name)) {
            binding.nameLayout.setError(getString(R.string.error_required));
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.emailLayoutReg.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            binding.phoneLayout.setError("Invalid phone number");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.passwordLayoutReg.setError(getString(R.string.error_password_length));
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError(getString(R.string.error_password_match));
            return;
        }

        binding.progressBarReg.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToDatabase(userId, name, email, phone);
                    } else {
                        binding.progressBarReg.setVisibility(View.GONE);
                        binding.registerButton.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", userId);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);

        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBarReg.setVisibility(View.GONE);
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarReg.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
