package com.example.calllogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utility implements VolleyResult {

    private static final String TAG = "Utility";
    private static Toast toast = null;
    private static Gson gson = new Gson();

    public static boolean isNetworkAvailable(Context context) {
        /*ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
        return (networkInfo != null) && networkInfo.isConnected();
*/
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            if (networks != null) {
                for (Network mNetwork : networks) {
                    networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                    if (networkInfo != null && networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                        return true;
                    }
                }
            }
        } else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static void displayToast(Context context, String text, int duration) {
        if (context != null) {
            if (toast != null) {
                toast.cancel();
            }
            LayoutInflater inflater = (LayoutInflater.from(context));
            View toastLayout = inflater.inflate(R.layout.content_custom_toast, null);
            toast = new Toast(context);
            toast.setDuration(duration);
            toast.setView(toastLayout);
            TextView textView = toastLayout.findViewById(R.id.textviewToast);
            textView.setText(text);
            toast.show();

        }
    }

    public static String getUrl(Context context, String endPoint) {
        return context.getString(R.string.base_url) + endPoint;
    }

    public static boolean checkPermissionStatus(Context context, String permissionName) {
        int result = ContextCompat.checkSelfPermission(context, permissionName);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(final Activity context, final String permissionName, final int requestCode) {
        ActivityCompat.requestPermissions(context, new String[]{permissionName}, requestCode);
    }

    public static boolean shouldShowPermissionRationale(final Activity activity, final String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    // schedule the start of the service every 10 - 30 seconds
    @TargetApi(Build.VERSION_CODES.M)
    public static void scheduleJob(Context context) {

        OneTimeWorkRequest checkService = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWork();
        workManager.enqueue(checkService);

    }

    public static void periodicWorkManager(Context context) {

        SessionManager sessionManager = new SessionManager(context);
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(MyWorker.class, Long.valueOf(sessionManager.getInterval()), TimeUnit.MINUTES)
                        .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(saveRequest);
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (service.foreground) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static void refreshService(Context context, Long alarmInterval) {
        stopLocationService(context);
        startLocationService(context, alarmInterval);
    }

    public static void startLocationService(Context context, Long alarmInterval) {
        if (!Utility.isMyServiceRunning(context, BackgroundService.class)) {
            //START LOCATION SERVICE
            try {
                context.startService(new Intent(context, BackgroundService.class));
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, BackgroundService.class));
                }
            }
            scheduleJob(context);
            periodicWorkManager(context);
            //setLocationAlarm(context, alarmInterval);
        }
    }

    public static void stopLocationService(Context context) {
        if (Utility.isMyServiceRunning(context, BackgroundService.class)) {
            context.stopService(new Intent(context, BackgroundService.class));
            //stopLocationAlarm(context);
        }
    }

    public static void publishLocationToServer(Context context,
                                        String eventFrom) {
        SessionManager sessionManager = new SessionManager(context);
        Log.d(TAG, "publishLocationToServer: "+eventFrom);

//                HashMap<String, String> headerParams = new HashMap<>();
//                headerParams.put(Constants.KEY_TOKEN, driverModel.getToken());
//
//                Bundle apiBundle = new Bundle();
//                apiBundle.putBoolean(Constants.KEY_SHOW_POPUP, false);
//                apiBundle.putBoolean(Constants.KEY_LOCATION_CALL, true);
//
//                VolleyService volleyService = new VolleyService(null, context);
//                volleyService.APICall("", Constants.URL_DRIVER_LOCATION, Request.Method.POST, Request.Priority.LOW, Constants.VOLLEY_RETRY_POSTDATA, headerParams, params, apiBundle);

        VolleyService mVolleyService = new VolleyService(null, context);
        List<ContactModel> logs = fetchList(context);
        HashMap<String, String> params = new HashMap<>();
        //params.put(Constants.USER_ID, String.valueOf(driverModel.getId()));

        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("Content-Type", "application/json");

        Bundle apiBundle = new Bundle();
        apiBundle.putBoolean("popup", false);
//        apiBundle.putBoolean(Constants.KEY_PREFIX_URL, false);

        mVolleyService.APICall("add_logs", "/api/Logs/SaveLogs/index.php" , Request.Method.POST, Request.Priority.HIGH, Constants.VOLLEY_RETRY_GETDATA, headerParams, params, apiBundle,gson.toJson(logs).toString());
    }

    public static void displayNotification(Context context, String task, String desc){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("logs", "logs", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Logs")
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(1, builder.build() );
    }

    private static List<ContactModel> fetchList(Context context){
        List<ContactModel> logs = new ArrayList<>();
        List<ContactModel> incoming = new ArrayList<>();
        List<ContactModel> outgoing = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(context);
        Cursor curLog = CallLogHelper.getAllCallLogs(context.getContentResolver(), context);
        while (curLog.moveToNext()) {
            ContactModel contactModel = new ContactModel();
            String callNumber = curLog.getString(curLog
                    .getColumnIndex(CallLog.Calls.NUMBER));
            //conNumbers.add(callNumber);
            contactModel.setPhone(callNumber);

            String callName = curLog
                    .getString(curLog
                            .getColumnIndex(CallLog.Calls.CACHED_NAME));
            Log.d(TAG, "setCallLogs: "+callName);
            if (callName == null || callName.isEmpty()) {
                //conNames.add("Unknown");
                contactModel.setFirstName("Unknown");
            } else
                contactModel.setFirstName(callName);
            //conNames.add(callName);

            String callDate = curLog.getString(curLog
                    .getColumnIndex(CallLog.Calls.DATE));
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm");
            String dateString = formatter.format(new Date(Long
                    .parseLong(callDate)));
            contactModel.setCreateddate(dateString);

            String callType = curLog.getString(curLog
                    .getColumnIndex(CallLog.Calls.TYPE));
            if (callType.equals("1")) {
                //conType.add("Incoming");
                contactModel.setCall_Type("Incoming");
            } else
                contactModel.setCall_Type("Outgoing");
            //conType.add("Outgoing");

            String duration = curLog.getString(curLog
                    .getColumnIndex(CallLog.Calls.DURATION));
            //conTime.add(duration);
            contactModel.setDuration(duration);
            contactModel.setSource(sessionManager.getUserNumber());

            logs.add(contactModel);
            if (contactModel.getCall_Type().equalsIgnoreCase("Incoming")){
                incoming.add(contactModel);
            }else {
                outgoing.add(contactModel);
            }

        }
        if (sessionManager.getCallType() != null){
            if (sessionManager.getCallType().equalsIgnoreCase("3")){
                return logs;
            }else if (sessionManager.getCallType().equalsIgnoreCase("2")){
                return outgoing;
            }else {
                return incoming;
            }
        }else {
            return logs;
        }
    }

    @Override
    public void notifySuccess(String requestType, int statusCode, String response, Bundle b) {

    }

    @Override
    public void notifyError(String requestType, int statusCode, String error) {

    }
}
