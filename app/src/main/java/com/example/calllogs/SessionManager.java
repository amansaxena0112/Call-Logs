package com.example.calllogs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final String PREF_NAME = "logsInfo";
    private static final String USER_INFO = "userInfo";
    private static final String CALL_TYPE = "callType";
    private static final String INTERVAL = "interval";
    private static final String HOUR = "hour";
    private static final String MIN = "min";
    private static final String DATE = "date";
    private static final String MONTH = "month";
    private static final String YEAR = "year";


    //private static final String IS_APPLY_ALL_STOP = "IsApplyStop";
    //private static final String CONTACT_NAME = "contactName";
    //private static final String CONTACT_NUM = "contactNum";


    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();
    }

    /**
     * Create number session
     */
    public void saveUserNumber(String number) {
        editor.putString(USER_INFO, number);
        editor.commit();
    }

    public void saveCallType(String call) {
        editor.putString(CALL_TYPE, call);
        editor.commit();
    }

    public void saveInterval(String interval) {
        editor.putString(INTERVAL, interval);
        editor.commit();
    }

    public void saveDate(int date) {
        editor.putInt(DATE, date);
        editor.commit();
    }

    public void saveMonth(int month) {
        editor.putInt(MONTH, month);
        editor.commit();
    }

    public void saveYear(int year) {
        editor.putInt(YEAR, year);
        editor.commit();
    }

    public void saveHour(int hour) {
        editor.putInt(HOUR, hour);
        editor.commit();
    }

    public void saveMin(int min) {
        editor.putInt(MIN, min);
        editor.commit();
    }

    /**
     * Get stored session data
     */
    public String getUserNumber() {
        String str = pref.getString(USER_INFO, null);
        return str;
    }

    public String getCallType() {
        String str = pref.getString(CALL_TYPE, null);
        return str;
    }

    public String getInterval() {
        String str = pref.getString(INTERVAL, null);
        return str;
    }

    public int getDate() {
        int str = pref.getInt(DATE, 0);
        return str;
    }

    public int getMonth() {
        int str = pref.getInt(MONTH, 0);
        return str;
    }

    public int getYear() {
        int str = pref.getInt(YEAR, 0);
        return str;
    }

    public int getHour() {
        int str = pref.getInt(HOUR, 0);
        return str;
    }

    public int getMin() {
        int str = pref.getInt(MIN, 0);
        return str;
    }

}
