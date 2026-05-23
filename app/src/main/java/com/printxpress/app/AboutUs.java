package com.printxpress.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUs extends AppCompatActivity {

    private static final String PHONE_NUMBER = "+94412245476";
    private static final String EMAIL_ADDRESS = "printxpress@gmail.com";
    private static final String WEBSITE_URL  = "https://www.printx.lk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About Us");
        }

        // Wire up views
        LinearLayout layoutPhone   = findViewById(R.id.layoutPhone);
        LinearLayout layoutEmail   = findViewById(R.id.layoutEmail);
        LinearLayout layoutWebsite = findViewById(R.id.layoutWebsite);
        TextView tvPhone           = findViewById(R.id.tvPhone);
        TextView tvEmail           = findViewById(R.id.tvEmail);
        TextView tvWebsite         = findViewById(R.id.tvWebsite);
        View btnBack               = findViewById(R.id.btnBack);

        // Sync UI with constants
        tvPhone.setText(PHONE_NUMBER);
        tvEmail.setText(EMAIL_ADDRESS);
        tvWebsite.setText(WEBSITE_URL.replace("https://", "").replace("http://", ""));

        // Custom Back Button
        btnBack.setOnClickListener(v -> finish());

        // Phone — opens dialer
        layoutPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        });

        // Email — opens mail app
        layoutEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + EMAIL_ADDRESS));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry - PrintX");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        // Website — opens browser
        layoutWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(WEBSITE_URL));
            startActivity(intent);
        });
    }

    // Handles the back arrow in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}