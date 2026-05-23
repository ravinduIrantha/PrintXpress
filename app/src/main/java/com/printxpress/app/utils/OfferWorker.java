package com.printxpress.app.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class OfferWorker extends Worker {

    public OfferWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Trigger the notification
        NotificationHelper.showOrderNotification(
                getApplicationContext(),
                "🔥 Special Offer!",
                "Get 15% off on all Premium Business Cards today only! Tap to shop."
        );
        return Result.success();
    }
}
