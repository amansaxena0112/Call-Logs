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
        registerActivityLifecycleCallbacks(callback);

        super.onCreate();
        mInstance = this;

        MultiDex.install(this);

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

