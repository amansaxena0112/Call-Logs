package com.example.calllogs;

import android.os.Bundle;

/**
 * Volley Callback
 */
public interface VolleyResult {

    /**
     * Notify success.
     *
     * @param requestType the request type
     * @param response    the response
     */
    void notifySuccess(String requestType, int statusCode, String response, Bundle b);

    /**
     * Notify error.
     *
     * @param requestType the request type
     * @param error       the error
     */
    void notifyError(String requestType, int statusCode, String error);
}
