package com.example.calllogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class used to call Rest API.
 */
public class VolleyService {

    private String TAG = "VolleyService";
    VolleyResult mResultCallback = null;
    Context mContext;
    private int timeOut = 15000; //15 sec
    private int maxReTry = 0;
    private ProgressDialog loadingDialog = null;
    //private AnimationImage anims;
    public static RequestQueue queue;
    private SessionManager sessionManager;
    String c_url = "";

    public VolleyService(VolleyResult resultCallback, Context context) {
        mResultCallback = resultCallback;
        mContext = context;
        sessionManager = new SessionManager(mContext);
    }

    public void APICall(String requestType, String urlEndPoint, int requestMethod, Request.Priority priority,
                        int maxReTry, final HashMap<String, String> hParams, final HashMap<String, String> params,
                        final Bundle extraBundle){
        APICall(requestType, urlEndPoint, requestMethod, priority, maxReTry, hParams, params, extraBundle, "V1",null);
    }

    public void APICall(String requestType, String urlEndPoint, int requestMethod, Request.Priority priority,
                        int maxReTry, final HashMap<String, String> hParams, final HashMap<String, String> params,
                        final Bundle extraBundle, String body){
        APICall(requestType, urlEndPoint, requestMethod, priority, maxReTry, hParams, params, extraBundle, "V1",body);
    }
    //Volley Post Method Rest API Calling Function
    public void APICall(final String requestType, final String urlEndPoint, int requestMethod, final Request.Priority priority,
                        int maxReTry, final HashMap<String, String> hParams, final HashMap<String, String> params,
                        final Bundle extraBundle, String apiVersion, final String body) {

        Log.d(TAG, "APICall urlEndPoint:" + body);
        Log.d(TAG, "APICall urlEndPoint:" + urlEndPoint + " requestMethod:" + requestMethod);
        Log.d(TAG, "APICall hParams:" + hParams.toString() + " params:" + params.toString() + " extraBundle:" + extraBundle.toString());
        try {
            if (extraBundle.getBoolean("popup", true)) {
                showProgressPopup(mContext, "Please wait...");
            }
            String url = Utility.getUrl(mContext, urlEndPoint);;

            Log.d(TAG, "url: " + url);
            c_url = url;
            StringRequest strReq = new StringRequest(requestMethod,
                    url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (extraBundle.getBoolean("popup", true)) {
                        Log.d(TAG, "inside dismiss: " + extraBundle.getBoolean("popup", true));
                        dismissLoadingPopup();
                    }

                    Log.d(TAG, "urlEndPoint: " + urlEndPoint + "response: " + response);
                    Log.d(TAG, "mResultCallback: " + mResultCallback);

                    if (mResultCallback != null) {
                        mResultCallback.notifySuccess(requestType, Constants.SERVICE_VOLLEY_200, response, extraBundle);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dismissLoadingPopup();
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    callErrorMethod(requestType, error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Log.d(TAG, "Params: " + params.toString());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Log.d(TAG, "headerParams: " + hParams.toString());
                    return hParams;
                }

                @Override
                public Priority getPriority() {
                    return priority;
                }

                @Override
                public String getBodyContentType() {
//                    if (body != null) {
//                        return "application/json; charset=utf-8";
//                    }else {
//
//                    }
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    if (body != null) {
                        try {
                            Log.e(TAG, "getBody(): request: " + body);
                            if (body != null) {
                                Log.d(TAG, "getBody: " + body.getBytes(getParamsEncoding()));
                                Log.d(TAG, "getBody: " + body.getBytes("utf-8"));
                                return body.getBytes("utf-8");
                                //return body.getBytes(getParamsEncoding());
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, "getBody(): request has no json");
                            e.printStackTrace();
                        }
                        return new byte[0];
                    }else {
                        return super.getBody();
                    }
                }
            };

            strReq.setRetryPolicy(new DefaultRetryPolicy(
                    timeOut, maxReTry,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            strReq.setShouldCache(false);

            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(strReq, url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void callErrorMethod(String requestType, VolleyError error) {
        String json = null;
        NetworkResponse response = error.networkResponse;
        if (response != null) {
            if (response.data != null) {
                json = new String(response.data);
            }
            handleErrors(response.statusCode, json);
            if (mResultCallback != null) {
                mResultCallback.notifyError(requestType, response.statusCode, json);
            }
        } else {
            handleErrors(Constants.SERVICE_VOLLEY_000, Constants.SERVICE_VOLLEY_CANT_REACH);
            if (mResultCallback != null) {
                mResultCallback.notifyError(requestType, Constants.SERVICE_VOLLEY_000, Constants.SERVICE_VOLLEY_CANT_REACH);
            }
        }
    }

    //Volley Get Method Rest Api Calling Function
    public void getDataVolley(final String requestType, String url, final Bundle b) {
        try {
            if (b == null || b.getBoolean("popup", true)) {
                showProgressPopup(mContext,"Please wait...");
            }
            queue = Volley.newRequestQueue(mContext);
            url = Utility.getUrl(mContext, url);
//            if (b == null || !b.getBoolean(Constants.KEY_GOOGLE_CALL, false)) {
//                url = StaticHelper.getUrl(mContext, url);
//            }
            Log.d(TAG, url);
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Upcoming trips result1");
                    dismissLoadingPopup();
                    Log.d(TAG, "response: " + response);
                    if (mResultCallback != null) {
                        mResultCallback.notifySuccess(requestType, Constants.SERVICE_VOLLEY_200, response, b);

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Upcoming trips result2");
                    dismissLoadingPopup();
                    String json = null;
                    NetworkResponse response = error.networkResponse;
                    if (response != null) {
                        if (response.data != null) {
                            json = new String(response.data);
                        }
                        handleErrors(response.statusCode, json);
                        if (mResultCallback != null) {
                            mResultCallback.notifyError(requestType, response.statusCode, json);
                        }

                    } else {
                        handleErrors(Constants.SERVICE_VOLLEY_000, Constants.SERVICE_VOLLEY_CANT_REACH);
                        if (mResultCallback != null) {
                            mResultCallback.notifyError(requestType, Constants.SERVICE_VOLLEY_000, Constants.SERVICE_VOLLEY_CANT_REACH);
                        }
                    }
                }
            });

            strReq.setRetryPolicy(new DefaultRetryPolicy(
                    timeOut, maxReTry,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            strReq.setShouldCache(false);
            queue.add(strReq);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleErrors(int statusCode, String result) {
        switch (statusCode) {
            case Constants.SERVICE_VOLLEY_000:
                Utility.displayToast(mContext, mContext.getString(R.string.unable_to_reach_our_server_chekc_network_connection), Toast.LENGTH_SHORT);
                break;
            case Constants.SERVICE_VOLLEY_403:
                //StaticHelper.stopLocationAlarm(mContext);
                Utility.displayToast(mContext, mContext.getString(R.string.invalid_session_token_try_login_again), Toast.LENGTH_SHORT);
//                if(sessionManager!=null) {
//                    Crashlytics.log(TAG + " "+"Url : "+ c_url +"  "+ Constants.KEY_LOGIN_STATUS + " -> " + sessionManager.isLoggedIn());
//                }else {
//                    Crashlytics.log(TAG+ " "+"Url : "+ c_url );
//                }
//                StaticHelper.backgroundSessionLogout(mContext);
                break;
            default:
                if (result != null && !result.equalsIgnoreCase("")) {
                    Utility.displayToast(mContext, result, Toast.LENGTH_SHORT);
                } else {
                    Utility.displayToast(mContext, statusCode + " " + mContext.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    private void showProgressPopup(Context context, String showText) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = new ProgressDialog(context);
        loadingDialog.setMessage(showText);
        loadingDialog.setCancelable(false);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.show();
    }

    public void dismissLoadingPopup() { //accessing from login activity also bez we need firebase authentication
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
