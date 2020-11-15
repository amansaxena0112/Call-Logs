package com.example.calllogs;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Date;

public class CallLogHelper {

    private static final String TAG = "CallLogHelper";

    public static Cursor getAllCallLogs(ContentResolver cr, Context context) {
        // reading all data in descending order according to DATE
//        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.set(2014, Calendar.MAY, 25);
//        String fromDate = String.valueOf(calendar.getTimeInMillis());
//        calendar.set(2014, Calendar.MAY, 30);
//        String toDate = String.valueOf(calendar.getTimeInMillis());
//        String[] whereValue = {fromDate,toDate};
//
//        Uri callUri = Uri.parse("content://call_log/calls");
//        Cursor curCallLogs = cr.query(callUri, null, android.provider.CallLog.Calls.DATE+" BETWEEN "+fromDate+" AND "+toDate, whereValue, strOrder);

        SessionManager sessionManager = new SessionManager(context);
        Cursor cur = null;
        if (sessionManager.getDate() > 0) {

            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
            Uri callUri = Uri.parse("content://call_log/calls");

            Calendar calendar = Calendar.getInstance();

            calendar.set(sessionManager.getYear(), sessionManager.getMonth(), sessionManager.getDate(), sessionManager.getHour(), sessionManager.getMin());

            String fromDate = String.valueOf(calendar.getTimeInMillis());

            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            String toDate = String.valueOf(c.getTimeInMillis());
            String[] whereValue = {fromDate, toDate};


            cur = cr.query(callUri, null, android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);

        }else {
            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
            Uri callUri = Uri.parse("content://call_log/calls");

            Calendar calendar = Calendar.getInstance();

            Log.d(TAG, "getAllCallLogs: "+calendar.get(Calendar.HOUR_OF_DAY));
            Log.d(TAG, "getAllCallLogs: "+calendar.get(Calendar.MINUTE));
            Log.d(TAG, "getAllCallLogs: "+calendar.get(Calendar.DATE));
            Log.d(TAG, "getAllCallLogs: "+calendar.get(Calendar.MONTH));
            Log.d(TAG, "getAllCallLogs: "+calendar.get(Calendar.YEAR));
            sessionManager.saveDate(calendar.get(Calendar.DATE));
            sessionManager.saveMonth(calendar.get(Calendar.MONTH));
            sessionManager.saveYear(calendar.get(Calendar.YEAR));
            sessionManager.saveHour(calendar.get(Calendar.HOUR_OF_DAY));
            sessionManager.saveMin(calendar.get(Calendar.MINUTE));
//            calendar.set(2020, Calendar.JANUARY, 30, 18, 30);
//
//            String fromDate = String.valueOf(calendar.getTimeInMillis());
//            calendar.set(2020, Calendar.FEBRUARY, 2, 12, 30);
//            String toDate = String.valueOf(calendar.getTimeInMillis());
//            String[] whereValue = {fromDate, toDate};


            cur = cr.query(callUri, null, null, null, strOrder);
        }
        return cur;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void insertPlaceholderCall(ContentResolver contentResolver,
                                             String name, String number) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, 0);
        values.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, name);
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME, number);
        values.put(CallLog.Calls.PHONE_ACCOUNT_ID, number);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        Log.d("Call Log", "Inserting call log placeholder for " + number);
        contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
    }

    public static Long createDate(int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month, day);
        Log.d(TAG, "createDate:datetime "+calendar.getTimeInMillis());

        return calendar.getTimeInMillis();

    }

}

