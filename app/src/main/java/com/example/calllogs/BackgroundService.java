package com.example.calllogs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";
    private SessionManager sessionManager;
    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public int counter=0;
    private static final String CHANNEL_ID = "channel_03";
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1033;


    public BackgroundService(Context applicationContext) {
        super();
        Log.i(TAG, "here I am!");
    }

    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate:service ");
        sessionManager = new SessionManager(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.d(TAG, "Foreground service");
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            startForeground(NOTIFICATION_ID, getNotification(CHANNEL_ID));
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, getNotification(CHANNEL_ID));
        }

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        Log.d(TAG, "startTimer: ");

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, Long.valueOf(sessionManager.getInterval())*60000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("", "in timer ++++  "+ (counter++));
                Utility.publishLocationToServer(BackgroundService.this,
                        "location_service");
                //Utility.displayNotification(BackgroundService.this,"Hey", "How r u?");
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Location service start");
        if (sessionManager.getInterval() != null) {
            startTimer();
        }
        // Android O requires a Notification Channel.
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        // Android O requires a Notification Channel.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.app_name);
//            // Create the channel for the notification
//            NotificationChannel mChannel =
//                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
//
//            // Set the Notification Channel for the Notification Manager.
//            mNotificationManager.createNotificationChannel(mChannel);
//
//            startForeground(NOTIFICATION_ID, getNotification(CHANNEL_ID));
//        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification(String CHANNEL_ID) {


        NotificationCompat.Builder builder;
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);
        builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle(name)
                .setOngoing(true)
                .setContentText("Service running")
                .setContentIntent(pendingIntent);
//        if (tripModel == null) {
//            //CharSequence name = getString(R.string.app_name);
//            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    //.setContentTitle(name)
//                    .setOngoing(true)
//                    .setContentText(getString(R.string.location_service_running))
//                    .setContentIntent(pendingIntent);
//        } else {
//            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
////            //contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
////            contentView.setTextViewText(R.id.tripId, tripModel.getTrip_number());
////            if (tripModel.getStatus().equals(Constants.TRIP_STATUS_IN_PROGRESS)) {
////                contentView.setTextColor(R.id.status, Color.parseColor("#FFB3811C"));
////            } else {
////                contentView.setTextColor(R.id.status, Color.parseColor("#FF3E6B0F"));
////            }
////            contentView.setTextViewText(R.id.status, tripModel.getStatus().replace("-", " "));
////            ScreenModel screenModel = StaticHelper.getScreenModel(tripModel);
////            if(screenModel!=null) {
////                contentView.setTextViewText(R.id.screenStatus, screenModel.getDisplay());
////            }
//            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setCustomBigContentView(contentView)
//                    .setOngoing(true)
//                    .setContentIntent(pendingIntent);
//        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }*/
        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }
}
