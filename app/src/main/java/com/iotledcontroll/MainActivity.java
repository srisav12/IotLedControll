package com.iotledcontroll;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bton, btoff;
    private int propertyval, blinkval;
    private Boolean oncheck = false, offcheck = false, blinkcheck = false,timercheck=false;
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private Context context;
    Button btnDatePicker, btnTimePicker, bt_timer;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a7y68ji03hfoc.iot.us-west-2.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-west-2:b298d571-db34-4af2-b096-cc78344ef0ce";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_WEST_2;

    CognitoCachingCredentialsProvider credentialsProvider;

    AWSIotDataClient iotDataClient;
    AlertDialog dialog;
    Button blink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        Log.e("date", strDate);
        init();

    }

    private void init() {
        context = this;
        dialog = new SpotsDialog(context);

        bton = (Button) findViewById(R.id.bton);
        btoff = (Button) findViewById(R.id.btoff);
        blink = (Button) findViewById(R.id.blink);
        btnDatePicker = (Button) findViewById(R.id.btn_date);
        btnTimePicker = (Button) findViewById(R.id.btn_time);
        bt_timer = (Button) findViewById(R.id.bt_timer);
        txtDate = (EditText) findViewById(R.id.in_date);
        txtTime = (EditText) findViewById(R.id.in_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        bton.setOnClickListener(this);
        btoff.setOnClickListener(this);
        blink.setOnClickListener(this);
        bt_timer.setOnClickListener(this);
        initializesdk();


    }

    private void initializesdk() {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        iotDataClient = new AWSIotDataClient(credentialsProvider);
        String iotDataEndpoint = CUSTOMER_SPECIFIC_ENDPOINT;
        iotDataClient.setEndpoint(iotDataEndpoint);

        /// Get the initial shadow from aws console
        GetShadowTask getStatusShadowTask = new GetShadowTask("Bulb");
        getStatusShadowTask.execute();

    }

    @Override
    public void onClick(View v) {
        UpdateShadowTask updateShadowTask = new UpdateShadowTask();
        updateShadowTask.setThingName("Bulb");
        Calendar c = Calendar.getInstance();
        String newState;
        JSONObject root = new JSONObject();
        JSONObject state = new JSONObject();
        JSONObject desired = new JSONObject();
        switch (v.getId()) {


            case R.id.bton:
                if (!oncheck) {
                    propertyval = 1;
                    offcheck = false;
                    try {
                        desired.put("property", propertyval);
                        desired.put("datetime","");
                        state.put("desired", desired);
                        root.put("state", state);
                        updateShadowTask.setState(root.toString());
                        updateShadowTask.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /*newState = String.format("{\"state\":{\"desired\":{\"property\":%d}}}", newSetpoint);
                    updateShadowTask.setState(newState);
                    updateShadowTask.execute();*/
                } else {
                    showAlert("Light is already on!");
                }
                break;

            case R.id.btoff:
                if (!offcheck) {
                    propertyval = 0;
                    oncheck = false;
                    try {
                        desired.put("property", propertyval);
                        desired.put("datetime","");
                        state.put("desired", desired);
                        root.put("state", state);
                        updateShadowTask.setState(root.toString());
                        updateShadowTask.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                 /*   newState = String.format("{\"state\":{\"desired\":{\"property\":%d}}}", newSetpoint);
                    updateShadowTask.setState(newState);
                    updateShadowTask.execute();*/
                } else {
                    showAlert("Light is already off!");
                }
                break;

            case R.id.blink:

                if (!blinkcheck) {
                    propertyval = -1;
                    blinkval = 1;
                    blinkcheck = true;


                    /*String newState = String.format("{\"state\":{\"desired\":{\"property\":%d}}}", 2);
                    updateShadowTask.setState(newState);
                    updateShadowTask.execute();*/
                } else {
                    propertyval = -1;
                    blinkval = 0;
                    blinkcheck = false;
                   /* String newState = String.format("{\"state\":{\"desired\":{\"property\":%d}}}", 3);
                    updateShadowTask.setState(newState);
                    updateShadowTask.execute()*/
                    ;
                }
                try {
                    desired.put("property", propertyval);
                    desired.put("blink", blinkval);
                    desired.put("datetime","");
                    state.put("desired", desired);
                    root.put("state", state);
                    updateShadowTask.setState(root.toString());
                    updateShadowTask.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;

            case R.id.btn_date:
                // Get Current Date
                c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                txtDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;

            case R.id.btn_time:
                // Get Current Time
                c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                txtTime.setText(hourOfDay + ":" + minute+":00");
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
                break;

            case R.id.bt_timer:
                if(!blinkcheck)
                {
                    String datetime=txtDate.getText().toString()+" "+txtTime.getText().toString();
                    Log.e("datetime",datetime);
                    try {
                        desired.put("datetime",datetime);
                        if(timercheck==true){
                            desired.put("property", 1);

                        }
                        else{
                            desired.put("property", 0);

                        }
                        state.put("desired", desired);
                        root.put("state", state);
                        updateShadowTask.setState(root.toString());
                        updateShadowTask.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                else
                {
                    Toast.makeText(this,"Turn the blink off before setting the timer",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }


    private class GetShadowTask extends AsyncTask<Void, Void, String> {

        private final String thingName;

        public GetShadowTask(String name) {
            thingName = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();

        }

        @Override
        protected String doInBackground(Void... voids) {
            String resultString = null;
            try {
                GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest()
                        .withThingName(thingName);
                GetThingShadowResult result = iotDataClient.getThingShadow(getThingShadowRequest);
                byte[] bytes = new byte[result.getPayload().remaining()];
                result.getPayload().get(bytes);
                resultString = new String(bytes);


                Log.e("response", resultString);
            } catch (Exception e) {
                Log.e("E", "getShadowTask", e);
            }
            return resultString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.hide();
            setleds(s);
        }
    }

    private void setleds(String s) {

        try {
            int blinkstatus = -1;
            btoff.setBackgroundColor(Color.BLACK);
            bton.setBackgroundColor(Color.BLACK);
            blink.setBackgroundColor(Color.BLACK);
            JSONObject root = new JSONObject(s);
            JSONObject state = root.getJSONObject("state");
            JSONObject desired = state.getJSONObject("desired");
            int ledstatus = desired.getInt("property");
            if (ledstatus == -1) {
                blinkstatus = desired.getInt("blink");
            }
            Log.e("ledstatus", "" + ledstatus);
            if (ledstatus == 0) {
                offcheck = true;
                oncheck=false;
                timercheck=true;
                btoff.setBackgroundColor(Color.RED);


            } else if (ledstatus == 1) {
                offcheck=false;
                oncheck = true;
                timercheck=false;
                bton.setBackgroundColor(Color.GREEN);
            } else if (ledstatus == -1) {
                if (blinkstatus == 0) {
                    blink.setBackgroundColor(Color.RED);
                } else if (blinkstatus == 1) {
                    blink.setBackgroundColor(Color.GREEN);


                }
            }

            // blink.setOnCheckedChangeListener(MainActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class UpdateShadowTask extends AsyncTask<Void, Void, String> {

        private String thingName;
        private String updateState;

        public void setThingName(String name) {
            thingName = name;
        }

        public void setState(String state) {
            updateState = state;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String resultString = null;
            try {
                UpdateThingShadowRequest request = new UpdateThingShadowRequest();
                request.setThingName(thingName);

                ByteBuffer payloadBuffer = ByteBuffer.wrap(updateState.getBytes());
                request.setPayload(payloadBuffer);

                UpdateThingShadowResult result = iotDataClient.updateThingShadow(request);

                byte[] bytes = new byte[result.getPayload().remaining()];
                result.getPayload().get(bytes);
                resultString = new String(bytes);

            } catch (Exception e) {
                Log.e(UpdateShadowTask.class.getCanonicalName(), "updateShadowTask", e);

            }
            return resultString;

        }

        @Override
        protected void onPostExecute(String result) {

            dialog.hide();
            GetShadowTask getStatusShadowTask = new GetShadowTask("Bulb");
            getStatusShadowTask.execute();
            Log.e("updateshadow", result);


        }
    }

    private void showAlert(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
