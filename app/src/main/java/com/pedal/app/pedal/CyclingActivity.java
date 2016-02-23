package com.pedal.app.pedal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.Date;

public class CyclingActivity extends BaseActivity  {

    Date endTime;
    ProgressDialog pDialog;
    long start, end;
    Thread threadForTimer;
    TextView speedText, distanceText, timeText, caloriesText, avgSpeedText;
    TextView profileName,profileMobNO;
    long milliSecAfterInterrupt = 0, millis=0;
    ImageButton stopButton,skipButton;
    LocationUtility locationUtility;
    String distance = "0.00", avgSpeed = "0.00", startTimeString = "", lastActive = "", timeElapsed = "00:00", calories = "0", userDataOfCycling[];
    Thread threadForGps;
    GoogleMap googleMap;
    boolean isClickedWifiSettings=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycling);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.cyclingActivity)));


        speedText = (TextView) findViewById(R.id.speed);
        distanceText = (TextView) findViewById(R.id.distance);
        timeText = (TextView) findViewById(R.id.time);
        caloriesText = (TextView) findViewById(R.id.calories);
        avgSpeedText = (TextView) findViewById(R.id.avgSpeed);
        stopButton = (ImageButton) findViewById(R.id.stopButton);
        skipButton = (ImageButton) findViewById(R.id.cross);

        profileName = (TextView) findViewById(R.id.profileName);
        profileMobNO = (TextView) findViewById(R.id.mobileNo);

        if (sessionManagement.isLoggedIn()) {

            String[] profileDetails;
            profileDetails = sessionManagement.getProfileDetail();
            profileName.setText(profileDetails[0]);
            profileMobNO.setText("+" + profileDetails[1]);
        }

        MapFragment mapFragment=(MapFragment) getFragmentManager().findFragmentById(
                R.id.map);
        googleMap=mapFragment.getMap();

        locationUtility = new LocationUtility();
        locationUtility.setTextView(speedText, distanceText, caloriesText, avgSpeedText);
        locationUtility.initializeMap(googleMap);
        locationUtility.getLastKnowLocation(getApplicationContext());

        threadForGps =new Thread()
        {
            @Override
            public void run() {

                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {

                         locationUtility.requestLocationUpdate(getApplicationContext());
                     }
                 });
            }

        };
        threadForGps.start();

        if(isNetworkAvailable(getApplicationContext()))
            new LocationControl().execute();
        else
            alertIfNetworkNotAvailable();

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(threadForTimer!=null)
                threadForTimer.interrupt();
                alertBeforeStop();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(threadForTimer!=null)
                threadForTimer.interrupt();
                alertBeforeCancelActivity();
            }
        });

    }

    protected void setExercisedDetailsOfUser(String distance, String startTimeString, String lastActive, String avgSpeed, String calories) {
        this.distance = distance;
        this.startTimeString = startTimeString;
        this.lastActive = lastActive;
        this.calories = calories;
        this.avgSpeed = avgSpeed;

        userDataOfCycling = new String[6];
        userDataOfCycling[0] = this.startTimeString;
        userDataOfCycling[1] = this.timeElapsed;
        userDataOfCycling[2] = this.lastActive;
        userDataOfCycling[3] = this.distance;
        userDataOfCycling[4] = this.avgSpeed;
        userDataOfCycling[5] = this.calories;

    }

    public void alertBeforeStop() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.StopCycling));
        dialog.setMessage(getString(R.string.StopCyclingMessage));
        dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                setExercisedDetailsOfUser(locationUtility.distanceString, locationUtility.starActivityTime, lastActive, locationUtility.avgSpeedString, locationUtility.caloriesString);

                Bundle bundleForPoints = new Bundle();
                bundleForPoints.putSerializable("Points", locationUtility.points);
                Intent intentForSummary = new Intent(getApplicationContext(), SummaryOfActivity.class).putExtras(bundleForPoints);
                intentForSummary.putExtra("userDataOfCycling", userDataOfCycling);
                startActivity(intentForSummary);

                leaveResources();
                finish();
            }
        });

        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                milliSecAfterInterrupt=millis;
                locationUtility.setStartTimeOfCycling();
                start = locationUtility.getStartTimeOfCycling();
                timer();

            }
        });
        dialog.show();
    }

    public void alertBeforeCancelActivity() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.abortTrip));
        dialog.setMessage(getString(R.string.abortTripMessage));
        dialog.setPositiveButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                leaveResources();
                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                milliSecAfterInterrupt = millis;
                locationUtility.setStartTimeOfCycling();
                start = locationUtility.getStartTimeOfCycling();
                timer();

            }
        });
        dialog.show();

    }

    //function for calculating time and updating every sec
    public void timer() {
        threadForTimer = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                endTime = new Date();
                                lastActive = DateFormat.format("yyyy-MM-dd kk:mm:ss", endTime).toString();
                                end = endTime.getTime();
                                try {
                                    millis = milliSecAfterInterrupt + (end - start);
                                    locationUtility.setTotalTime(millis);
                                    int seconds = (int) (millis / 1000) % 60;
                                    int minutes = (int) ((millis / (1000 * 60)) % 60);
                                    int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
                                    minutes = hours * 60 + minutes;

                                    String Minutes = String.valueOf(minutes);
                                    String Seconds = String.valueOf(seconds);

                                    if (minutes < 10) {
                                        Minutes = "0" + Minutes;
                                    }
                                    if (seconds < 10) {
                                        Seconds = "0" + Seconds;
                                    }

                                    timeElapsed = Minutes + ":" + Seconds; // updated value every 1 second
                                    timeText.setText(timeElapsed);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        threadForTimer.start();
    }

    @Override
    public void onBackPressed() {

       if(threadForTimer!=null)
           threadForTimer.interrupt();
        alertBeforeCancelActivity();
    }

    private class LocationControl extends AsyncTask<Context, Void, Void>
    {

        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(CyclingActivity.this);
            pDialog.setMessage(getString(R.string.fetchingLocationMessage));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected Void doInBackground(Context... params)
        {

            Date date=new Date();
            while (!locationUtility.hasLocation && ((new Date()).getTime()- date.getTime())<45000 ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(final Void unused)
        {
            pDialog.dismiss();
            if(locationUtility.hasLocation==false)
            {
                alertUnableToFetchLocation();
            }
            else
            {
                locationUtility.registerAccelerometerSensor(getApplicationContext());
                if(millis==0) {
                    locationUtility.setStartTimeOfCycling();
                    start = locationUtility.getStartTimeOfCycling();
                    timer();
                }

            }
        }
    }


    public void alertUnableToFetchLocation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setTitle("Location Error");
        dialog.setMessage("Unable to fetch current location. Make sure you are in open place");
        dialog.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                new LocationControl().execute();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                finish();
            }
        });
        dialog.show();

    }

    public void alertIfNetworkNotAvailable() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setTitle("Network Error");
        dialog.setMessage("To view map properly, make sure you are connected to internet");

        dialog.setPositiveButton("Wifi settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isClickedWifiSettings=true;
                Intent wifiSettings=new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(wifiSettings);
            }
        });

        dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                new LocationControl().execute();
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isClickedWifiSettings)
            new LocationControl().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveResources();
    }

    protected void leaveResources()
    {
        threadForGps.interrupt();
        locationUtility.stopLocationUpdates();
        locationUtility.deRegisterSensor();
    }
}
