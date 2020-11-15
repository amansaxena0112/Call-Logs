package com.example.calllogs;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends MultiDexApplication {

    private static final String TAG = "MyApplication";
    private static MyApplication mInstance;
    private RequestQueue mRequestQueue;
    private int resumed;
    private int paused;
    private int started;
    private int stopped;
    private Activity mCurrentActivity = null;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

//    private NetworkChangeReceiver networkChangeReceiver;
//
//    private NetworkChangeReceiver getConnectivityReceiver() {
//        if (networkChangeReceiver == null)
//            networkChangeReceiver = new NetworkChangeReceiver();
//
//        return networkChangeReceiver;
//    }

    private final Application.ActivityLifecycleCallbacks callback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            mCurrentActivity = activity;
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d("application", "onActivityDestroyed");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            mCurrentActivity = activity;
            ++resumed;
            Log.d("application", "onActivityResumed: " + resumed);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            ++paused;
            Log.d("application", "onActivityPaused: " + paused);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d("application", "onActivityStarted: " + started);
            mCurrentActivity = activity;
            ++started;
        }

        @Override
        public void onActivityStopped(final Activity activity) {
            ++stopped;
            Log.d("application", "onActivityStopped: " + stopped + " start: " + started);
        }
    };

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    @Override
    public void onCreate() {
        /**
         * CleverTap in-app-notification
         * */
        //ActivityLifecycleCallback.register(this);
        registerActivityLifecycleCallbacks(callback);

        super.onCreate();
        mInstance = this;

       // UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        MultiDex.install(this);

        //FirebaseApp.initializeApp(this);
        //Fabric.with(this, new Crashlytics());
        //DBHelper.initDB(getApplicationContext());


//        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//        firebaseRemoteConfig.setConfigSettings(configSettings);
//
//        // set in-app defaults
//        Map<String, Object> remoteConfigDefaults = new HashMap();
//        remoteConfigDefaults.put(Constants.KEY_UPDATE_REQUIRED, false);
//        remoteConfigDefaults.put(Constants.KEY_CURRENT_VERSION, StaticHelper.getAppVersionCode(this));
//        //remoteConfigDefaults.put(BlowhornConst.KEY_UPDATE_URL,"https://play.google.com/apps/testing/net.blowhorn.driverapp");
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
//        filter.addCategory(Intent.CATEGORY_DEFAULT);
//        try {
//            registerReceiver(getConnectivityReceiver(), filter);
//        } catch (Exception e) {
//            // already registered
//        }
//        firebaseRemoteConfig.setDefaults(remoteConfigDefaults);
//        firebaseRemoteConfig.fetch(3600) // fetch every 1 hours
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            android.util.Log.d(TAG, "remoteConfig is fetched.");
//                            firebaseRemoteConfig.activateFetched();
//                        }
//                    }
//                });

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public boolean isAppForeground() {
        return resumed > paused;
    }

    public boolean isApplicationVisible() {
        return started > stopped;
    }

}

