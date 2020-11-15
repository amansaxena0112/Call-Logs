package com.example.calllogs;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends ListActivity implements VolleyResult {

    private static final String TAG = "MainActivity";
    private static final int JOB_ID = 101;
    private Dialog mBottomSheetDialog;
    private static final int CREDENTIAL_PICKER_REQUEST = 500;
    private static final int READ_PHONE_STATE = 501;
    private static final int READ_CALL_LOG = 502;
    private boolean incomingCalls = false ,outgoingCalls = false, from = false;
    private int interval = 0;
    private Button save;
    private TextView from_date, to_date;
    private SessionManager sessionManager;
    private int year, month, day;
    private ImageView from_cancel, to_cancel, filter;
    private CheckBox incoming_check, outgoing_check;
    private List<ContactModel> contacts;
    private List<ContactModel> logs;
    private List<ContactModel> incoming;
    private List<ContactModel> outgoing;
    Intent mServiceIntent;
    private BackgroundService mService;
    private VolleyService mVolleyService;
    private Gson gson = new Gson();
    private String fromDate= "", toDate = "";
    private Calendar calendar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from_date = findViewById(R.id.from_date);
        to_date = findViewById(R.id.to_date);
        from_cancel = findViewById(R.id.from_cancel);
        to_cancel = findViewById(R.id.to_cancel);
        filter = findViewById(R.id.filter);
        outgoing_check = findViewById(R.id.outgoing_check);
        incoming_check = findViewById(R.id.incoming_check);
        contacts = new ArrayList<>();
        logs = new ArrayList<>();
        incoming = new ArrayList<>();
        outgoing = new ArrayList<>();

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year,month,day);


        mVolleyService = new VolleyService(this, MainActivity.this);

        sessionManager = new SessionManager(MainActivity.this);

        Log.d(TAG, "onCreate: "+sessionManager.getCallType());
        if (Utility.checkPermissionStatus(MainActivity.this, Manifest.permission.READ_CALL_LOG)){
            if (Utility.checkPermissionStatus(MainActivity.this, Manifest.permission.READ_PHONE_STATE)){
                if (sessionManager.getUserNumber() == null) {
                    openInfoBottomSheet();
                }else {
                    getList();
                }
            }else {
                Utility.requestPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
            }
        }else {
            Utility.requestPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG, READ_CALL_LOG);
        }

        from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                from = true;
                setDate(view);
            }
        });

        to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                from = false;
                setDate(view);
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInfoBottomSheet();
            }
        });

        from_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                from_date.setText("");
                fromDate = "";
                getList();
            }
        });

        to_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                to_date.setText("");
                toDate = "";
                getList();
            }
        });

        setFilters();

        incoming_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if (outgoing_check.isChecked()){

                        contacts.clear();
                        contacts.addAll(logs);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, logs));
                    }else {

                        contacts.clear();
                        contacts.addAll(incoming);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, incoming));
                    }
                }else {
                    if (outgoing_check.isChecked()){

                        contacts.clear();
                        contacts.addAll(outgoing);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, outgoing));
                    }else {

                        contacts.clear();
                        contacts.addAll(logs);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, logs));
                    }
                }
            }
        });

        outgoing_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (incoming_check.isChecked()){

                        contacts.clear();
                        contacts.addAll(logs);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, logs));
                    }else {

                        contacts.clear();
                        contacts.addAll(outgoing);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, outgoing));
                    }
                }else {
                    if (incoming_check.isChecked()){

                        contacts.clear();
                        contacts.addAll(incoming);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, incoming));
                    }else {

                        contacts.clear();
                        contacts.addAll(logs);

                        setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                                R.id.tvNameMain, logs));
                    }
                }
            }
        });

        mService = new BackgroundService(MainActivity.this);
        mServiceIntent = new Intent(MainActivity.this, mService.getClass());
        if (!Utility.isMyServiceRunning(MainActivity.this, mService.getClass())) {
            startService(mServiceIntent);
            if (sessionManager.getInterval() != null) {
                Utility.scheduleJob(MainActivity.this);
                Utility.periodicWorkManager(MainActivity.this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getList();
    }

    private void saveList() {
        HashMap<String, String> params = new HashMap<>();
        //params.put(Constants.USER_ID, String.valueOf(driverModel.getId()));

        HashMap<String, String> headerParams = new HashMap<>();
        //headerParams.put(Constants.KEY_TOKEN, driverModel.getToken());

        Bundle apiBundle = new Bundle();
        apiBundle.putBoolean("popup", false);
//        apiBundle.putBoolean(Constants.KEY_PREFIX_URL, false);

        mVolleyService.APICall("add_logs", "/logs/" , Request.Method.POST, Request.Priority.HIGH, Constants.VOLLEY_RETRY_GETDATA, headerParams, params, apiBundle,gson.toJson(logs).toString());
    }

    private void getList() {
        if (sessionManager.getUserNumber() != null) {
            boolean in = false, out = false;
            if (sessionManager.getCallType() != null) {
                if (sessionManager.getCallType().equalsIgnoreCase("3")) {
                    in = true;
                    out = true;
                } else if (sessionManager.getCallType().equalsIgnoreCase("2")) {
                    out = true;
                    in = false;
                } else {
                    in = true;
                    out = false;
                }
            }
            String url = "/api/Logs/GetLogs/?source=" + sessionManager.getUserNumber()
                    + "&incoming=" + in + "&outgoing=" + out + "&from=" + fromDate + "&to=" + toDate;
            HashMap<String, String> params = new HashMap<>();
            //params.put(Constants.USER_ID, String.valueOf(driverModel.getId()));

            HashMap<String, String> headerParams = new HashMap<>();
            //headerParams.put(Constants.KEY_TOKEN, driverModel.getToken());

            Bundle apiBundle = new Bundle();
            apiBundle.putBoolean("popup", true);

            mVolleyService.APICall("Cart", url, Request.Method.GET, Request.Priority.HIGH, Constants.VOLLEY_RETRY_GETDATA, headerParams, params, apiBundle);
        }
    }

    private void setFilters(){
        if (sessionManager.getCallType() != null){
            if (!sessionManager.getCallType().equalsIgnoreCase("3")){
                incoming_check.setVisibility(View.GONE);
                outgoing_check.setVisibility(View.GONE);
            }else {
                incoming_check.setVisibility(View.VISIBLE);
                outgoing_check.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    private void showDate(int year, int month, int day) {
        if (from) {
            fromDate = new StringBuilder().append(day).append("/")
                    .append(month).append("/").append(year).toString();
            from_date.setText(new StringBuilder().append(day).append("/")
                    .append(month).append("/").append(year));
            getList();
        }else {
            toDate = new StringBuilder().append(day).append("/")
                    .append(month).append("/").append(year).toString();
            to_date.setText(new StringBuilder().append(day).append("/")
                    .append(month).append("/").append(year));
            getList();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    fromDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener fromDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    year = arg1;
                    month = arg2+1;
                    day = arg3;
                    showDate(arg1, arg2+1, arg3);
                }
            };

    @Override
    public void notifySuccess(String requestType, int statusCode, String response, Bundle b) {
        Log.d(TAG, "notifySuccess: "+response);
        if (response != null && !response.equalsIgnoreCase("")){
            Type listType = new TypeToken<List<ContactModel>>() {
            }.getType();
            logs.clear();
            logs = gson.fromJson(response, listType);
            if (logs.size() > 0) {
                if (sessionManager.getCallType().equalsIgnoreCase("3")){
                    for (int j = 0; j < logs.size(); j++){
                        if (logs.get(j).getCall_Type().equalsIgnoreCase("Incoming")){
                            incoming.add(logs.get(j));
                        }else {
                            outgoing.add(logs.get(j));
                        }
                    }
                }
                contacts.clear();
                contacts.addAll(logs);
                setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                        R.id.tvNameMain, logs));
            }else {
                if (sessionManager.getCallType().equalsIgnoreCase("3")){
                    Log.d(TAG, "onCreate:1 ");
                    //setList(contacts);
                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);

                    setCallLogs(curLog);

                    contacts.clear();
                    contacts.addAll(logs);

                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                            R.id.tvNameMain, logs));
                }else if (sessionManager.getCallType().equalsIgnoreCase("2")){
                    Log.d(TAG, "onCreate:2 ");
                    //setList(outgoing);
                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);

                    setCallLogs(curLog);

                    contacts.clear();
                    contacts.addAll(outgoing);

                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                            R.id.tvNameMain, outgoing));
                }else {
                    Log.d(TAG, "onCreate:3 ");
                    //setList(incoming);
                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);

                    setCallLogs(curLog);

                    contacts.clear();
                    contacts.addAll(incoming);

                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
                            R.id.tvNameMain, incoming));
                }
            }
        }

    }

    @Override
    public void notifyError(String requestType, int statusCode, String error) {

    }

    private class MyAdapter extends ArrayAdapter<ContactModel> {

        public MyAdapter(Context context, int resource, int textViewResourceId,
                         List<ContactModel> contactModels) {
            super(context, resource, textViewResourceId, contactModels);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = setList(position, parent);
            return row;
        }

        private View setList(int position, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = inf.inflate(R.layout.liststyle, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.tvNameMain);
            TextView tvNumber = (TextView) row.findViewById(R.id.tvNumberMain);
            TextView tvTime = (TextView) row.findViewById(R.id.tvTime);
            TextView tvDate = (TextView) row.findViewById(R.id.tvDate);
            TextView tvType = (TextView) row.findViewById(R.id.tvType);

            tvName.setText(contacts.get(position).getFirstName());
            tvNumber.setText(contacts.get(position).getPhone());
            tvTime.setText("( " + contacts.get(position).getDuration() + "sec )");
            tvDate.setText(contacts.get(position).getCreateddate());
            tvType.setText("( " + contacts.get(position).getCall_Type() + " )");

            return row;
        }
    }

    private void setCallLogs(Cursor curLog) {
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

            contacts.add(contactModel);
            logs.add(contactModel);
            if (contactModel.getCall_Type().equalsIgnoreCase("Incoming")){
                incoming.add(contactModel);
            }else {
                outgoing.add(contactModel);
            }

        }

        //getHintPhoneNumber();
    }

    private void getHintPhoneNumber() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setAccountTypes(ContactsContract.CommonDataKinds.Identity.CONTACT_ID)
                .build();
        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        CredentialsClient client = Credentials.getClient(this, options);
        PendingIntent intent = client.getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(
                    intent.getIntentSender(),
                    CREDENTIAL_PICKER_REQUEST,
                    null, 0, 0, 0,null
            );
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater imf = getMenuInflater();
        imf.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            Intent intent = new Intent(MainActivity.this, InsertCallLog.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        switch (requestCode){
            case CREDENTIAL_PICKER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    Log.d(TAG, "onActivityResult: "+data.toString());
                    Log.d(TAG, "onActivityResult: "+data.getType());
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                            //phoneNumber?.setText(credential?.id?.replace("+91", ""))
                    Log.d(TAG, "onActivityResult: "+credential.getId().replace("+91",""));
                    Log.d(TAG, "onActivityResult: "+credential.getName());
                    Log.d(TAG, "onActivityResult: "+credential.getGivenName());
                }
                break;

        }
    }

    private void openInfoBottomSheet() {
        if (!MainActivity.this.isFinishing() && mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) {
            return;
        }

        final View view = getLayoutInflater().inflate(R.layout.info_bottom_sheet, null);
        final EditText number = view.findViewById(R.id.number);
        final CheckBox incoming_calls = view.findViewById(R.id.incoming_calls);
        final CheckBox outgoing_calls = view.findViewById(R.id.outgoing_calls);
        RadioGroup time_group = view.findViewById(R.id.time_group);
        save = view.findViewById(R.id.save);

        if (sessionManager.getUserNumber() != null){
            number.setText(sessionManager.getUserNumber());
        }

        if (sessionManager.getCallType() != null){
            if (sessionManager.getCallType().equalsIgnoreCase("3")){
                incomingCalls = true;
                outgoingCalls = true;
                incoming_calls.setChecked(true);
                outgoing_calls.setChecked(true);
            }else if(sessionManager.getCallType().equalsIgnoreCase("1")){
                incoming_calls.setChecked(true);
                incomingCalls = true;
            }else {
                outgoingCalls = true;
                outgoing_calls.setChecked(true);
            }
        }

        if (sessionManager.getInterval() != null){
            interval = Integer.valueOf(sessionManager.getInterval());
            RadioButton radioButton = view.findViewWithTag(sessionManager.getInterval());
            if (radioButton != null){
                radioButton.setChecked(true);
            }
        }

        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        outgoing_calls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                outgoingCalls = b;
            }
        });

        incoming_calls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                incomingCalls = b;
            }
        });

        time_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = (RadioButton) view.findViewById(i);
                if (radioButton != null){
                    interval = Integer.valueOf(radioButton.getTag().toString());
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ph_number = number.getText().toString();
                if (ph_number == null || ph_number.isEmpty() || ph_number.length() < 10){
                    number.setError("Please enter 10 digit number");
                    Utility.displayToast(MainActivity.this,"Please enter 10 digit number",Toast.LENGTH_SHORT);
                    return;
                }
                if (!incomingCalls && !outgoingCalls){
                    Utility.displayToast(MainActivity.this,"Please select a call type",Toast.LENGTH_SHORT);
                    return;
                }
                if (interval == 0){
                    Utility.displayToast(MainActivity.this,"Please select a time interval",Toast.LENGTH_SHORT);
                    return;
                }

                sessionManager.saveUserNumber(ph_number);

                if (incomingCalls && outgoingCalls){
                    sessionManager.saveCallType("3");
                }else if (incomingCalls){
                    sessionManager.saveCallType("1");
                }else {
                    sessionManager.saveCallType("2");
                }

                sessionManager.saveInterval(String.valueOf(interval));


//                Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver());
//
//                setCallLogs(curLog);
//
//                setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
//                        R.id.tvNameMain, contacts));
//                if (sessionManager.getCallType().equalsIgnoreCase("3")){
////                    setList(contacts);
//                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);
//
//                    setCallLogs(curLog);
//
//                    contacts.clear();
//                    contacts.addAll(logs);
//
//                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
//                            R.id.tvNameMain, logs));
//                }else if (sessionManager.getCallType().equalsIgnoreCase("2")){
//                    //setList(outgoing);
//                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);
//
//                    setCallLogs(curLog);
//
//                    contacts.clear();
//                    contacts.addAll(outgoing);
//
//                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
//                            R.id.tvNameMain, outgoing));
//                }else {
//                    //setList(incoming);
//                    Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver(), MainActivity.this);
//
//                    setCallLogs(curLog);
//
//                    contacts.clear();
//                    contacts.addAll(incoming);
//
//                    setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
//                            R.id.tvNameMain, incoming));
//                }
                setFilters();
                Utility.refreshService(MainActivity.this, Long.valueOf(sessionManager.getInterval()));
                getList();
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new Dialog(MainActivity.this,
                R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(false);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                Cursor curLog = CallLogHelper.getAllCallLogs(getContentResolver());
//
//                setCallLogs(curLog);
//
//                setListAdapter(new MyAdapter(MainActivity.this, android.R.layout.simple_list_item_1,
//                        R.id.tvNameMain, conNames));
//            }
//        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CALL_LOG:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "User granted permission. ");
                    if (Utility.checkPermissionStatus(MainActivity.this, Manifest.permission.READ_PHONE_STATE)){
                        openInfoBottomSheet();
                    }else {
                        Utility.requestPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
                    }
                } else {
                    Utility.requestPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG, READ_CALL_LOG);
                }
                break;
            case READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "User granted permission. ");
                    if (Utility.checkPermissionStatus(MainActivity.this, Manifest.permission.READ_CALL_LOG)){
                        openInfoBottomSheet();
                    }else {
                        Utility.requestPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG, READ_CALL_LOG);
                    }
                } else {
                    Utility.requestPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
