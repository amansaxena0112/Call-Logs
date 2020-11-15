package com.example.calllogs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    private static final String TAG = "MyWorker";

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //Toast.makeText(getApplicationContext(), "Job is running", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "doWork: ");
        // Create the output of the work
//        Data outputData = new Data.Builder()
//                .putString("Data", "Restarted")
//                .build();
        if (!Utility.isMyServiceRunning(getApplicationContext(), BackgroundService.class)) {
            //START LOCATION SERVICE
            Log.d(TAG, "Job Running1");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Log.d(TAG, "doWork:first ");
                getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
            } else {
                Log.d(TAG, "doWork:second ");
                getApplicationContext().startForegroundService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        }
        Utility.scheduleJob(getApplicationContext());// reschedule the job
        //displayNotification("Hey", "How r u?");

        return Result.success();
    }

    private void displayNotification(String task, String desc){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("logs", "logs", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Logs")
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(1, builder.build() );
    }
}
