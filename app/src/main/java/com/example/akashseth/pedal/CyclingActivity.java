package com.example.akashseth.pedal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

public class CyclingActivity extends BaseActivity {

    Date startTime, endTime;
    ProgressDialog pDialog;
    long start, end;
    Thread threadForTimer;
    TextView speedText, distanceText, timeText, caloriesText, avgSpeedText;
    long milliSecAfterInterrupt = 0, millis;
    GoogleMap googleMap;
    ImageButton stopButton,skipButton;
    LocationUtility locationUtility;
    String distance = "00.00", avgSpeed = "00.00", startTimeString = "", lastActive = "", timeElapsed = "00:00", calories = "0", userDataOfCycling[];
    Thread threadForGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycling);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.cyclingActivity)));

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();

        speedText = (TextView) findViewById(R.id.speed);
        distanceText = (TextView) findViewById(R.id.distance);
        timeText = (TextView) findViewById(R.id.time);
        caloriesText = (TextView) findViewById(R.id.calories);
        avgSpeedText = (TextView) findViewById(R.id.avgSpeed);
        stopButton = (ImageButton) findViewById(R.id.stopButton);
        skipButton = (ImageButton) findViewById(R.id.cross);

        locationUtility = new LocationUtility();

        locationUtility.setTextView(speedText, distanceText, caloriesText, avgSpeedText);

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


        LatLng latLng = getIntent().getParcelableExtra("LatLng");
        locationUtility.initializeMap(googleMap,latLng);

        new LocationControl().execute();

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                threadForTimer.interrupt();
                alertBeforeStop();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                finish();
            }
        });

        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                milliSecAfterInterrupt=millis;
                startTime=new Date();
                start = startTime.getTime();

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

                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                milliSecAfterInterrupt = millis;
                startTime = new Date();
                start = startTime.getTime();

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

            while (!locationUtility.hasLocation) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            return null;
        }

        protected void onPostExecute(final Void unused)
        {
            pDialog.dismiss();
            locationUtility.setStartTimeOfCycling();
            start = locationUtility.getStartTimeOfCycling();
            timer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadForGps.interrupt();
        threadForTimer.interrupt();
        locationUtility.stopLocationUpdates();
    }
}
