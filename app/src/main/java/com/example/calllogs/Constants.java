package com.example.calllogs;

public class Constants {

    public static final int SERVICE_VOLLEY_200 = 200; // success
    public static final int SERVICE_VOLLEY_000 = 000; // unable to receive any message from server(not reachable)
    public static final int SERVICE_VOLLEY_403 = 403; //session expires in server(invalid token)
    public static final int SERVICE_VOLLEY_401 = 401; // Invalid login details
    public static final int SERVICE_VOLLEY_400 = 400; // Invalid trip data. clear local data and go to home screen
    public static final String SERVICE_VOLLEY_CANT_REACH = "error";
    public static final int VOLLEY_RETRY_GETDATA = 5;
}
