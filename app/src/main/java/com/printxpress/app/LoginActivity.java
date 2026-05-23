package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.databinding.ActivityLoginBinding;
import com.printxpress.app.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.goToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = "";
        if (binding.emailEditText.getText() != null) {
            email = binding.emailEditText.getText().toString().trim();
        }

        String password = "";
        if (binding.passwordEditText.getText() != null) {
            password = binding.passwordEditText.getText().toString().trim();
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.emailLayout.setError(getString(R.string.error_invalid_email));
            return;
        } else {
            binding.emailLayout.setError(null);
        }

        if (ValidationUtils.isEmpty(password)) {
            binding.passwordLayout.setError(getString(R.string.error_required));
            return;
        } else {
            binding.passwordLayout.setError(null);
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
