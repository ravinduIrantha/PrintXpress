package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.printxpress.app.databinding.ActivityMainBinding;
import com.printxpress.app.utils.DatabaseSeeder;
import com.printxpress.app.utils.NotificationHelper;
import com.printxpress.app.utils.OfferWorker;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Custom navigation logic to ensure each tab always opens its start destination
        binding.navView.setOnItemSelectedListener(item -> {
            if (navController.getCurrentDestination() == null || item.getItemId() != navController.getCurrentDestination().getId()) {
                navController.navigate(item.getItemId(), null, new androidx.navigation.NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(false) // This is the key: don't restore previous tab state
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), false, false)
                        .build());
            }
            return true;
        });

        // Add an OnItemReselectedListener to handle clicks on the already active tab
        binding.navView.setOnItemReselectedListener(item -> {
            // Optional: Pop everything up to the root of the current tab if re-clicked
            navController.popBackStack(item.getItemId(), false);
        });

        // Still need this to update the selected icon when navigating via other means (like back button)
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            android.view.MenuItem item = binding.navView.getMenu().findItem(id);
            if (item != null) {
                item.setChecked(true);
            }
        });

        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        binding.tvPrintXLogo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutUs.class);
            startActivity(intent);
        });

//         DatabaseSeeder.seedDatabase();
        scheduleSpecialOfferNotification();

    }

    private void scheduleSpecialOfferNotification() {
        PeriodicWorkRequest offerRequest = new PeriodicWorkRequest.Builder(OfferWorker.class, 24, TimeUnit.HOURS)
                .addTag("special_offer")
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "special_offer_work",
                ExistingPeriodicWorkPolicy.KEEP,
                offerRequest
        );
    }



    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
