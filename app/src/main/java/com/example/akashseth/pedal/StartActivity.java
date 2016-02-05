package com.example.akashseth.pedal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class StartActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ImageButton startCyclingButton;
    boolean gps_enabled = false;
    TextView welcomeText1, welcomeText2;
    TextView profileName, profileMobNO;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    double latitude = 0, longitude = 0;
    LatLng latLng;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.titleActivity)));

        profileName = (TextView) findViewById(R.id.profileName);
        profileMobNO = (TextView) findViewById(R.id.mobileNo);

        startCyclingButton = (ImageButton) findViewById(R.id.startCycling);
        welcomeText1 = (TextView) findViewById(R.id.welcomMessage1);
        welcomeText2 = (TextView) findViewById(R.id.welcomMessage2);
        if(isScreenSizeNormal())
            welcomeText2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(float)15.6);

        setProfileDetail();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        startCyclingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                prefetchLocation();
                if (checkIfGpsIsEnabled()) {
                    Intent cyclingIntent = new Intent(getApplicationContext(), CyclingActivity.class).putExtra("LatLng", latLng);
                    ;
                    startActivity(cyclingIntent);
                }
            }

        });


    }

    @Override
    public void onConnected(Bundle connectionHint) {
        connected = true;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void setProfileDetail() {
        if (!sessionManagement.isLoggedIn()) {
            welcomeText1.setText("Welcome to Pedal");
            welcomeText2.setText("Please click cycling below to start your activity");
        } else {
            String[] profileDetails;
            profileDetails = sessionManagement.getProfileDetail();
            profileName.setText(profileDetails[0]);
            profileMobNO.setText("+" + profileDetails[1]);

            if (profileDetails[0].equals("")) {
                welcomeText1.setText("Hello User!");
                welcomeText2.setText("Please click cycling below to start your activity");
            } else {
                if (profileDetails[0].contains(" ")) {
                    profileDetails[0] = profileDetails[0].substring(0, profileDetails[0].indexOf(" "));
                }
                welcomeText1.setText("Hello " + profileDetails[0] + "!");
                welcomeText2.setText("Please click cycling below to start your activity");
            }

        }
    }

    public boolean checkIfGpsIsEnabled() {
        LocationUtility locationUtility = new LocationUtility();
        gps_enabled = locationUtility.isGpsEnabled(getApplicationContext());
        if (!gps_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(StartActivity.this, R.style.AppCompatAlertDialogStyle);
            dialog.setTitle("Gps is not enabled");
            dialog.setMessage("This activity requires gps service. You must enable it");
            dialog.setPositiveButton("Gps settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        } else {
            //latLng=locationUtility.getLastKnowLocation(getApplicationContext());
        }
        return gps_enabled;
    }

    public void prefetchLocation() {
        Log.d("out", "out");
        if (connected) {
            Log.d("in", "in");

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {

                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                latLng = new LatLng(latitude, longitude);

            }
        }


    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setMessage("Are you sure you want to exit from app?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    private boolean isScreenSizeNormal()
    {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize==Configuration.SCREENLAYOUT_SIZE_NORMAL || screenSize==Configuration.SCREENLAYOUT_SIZE_XLARGE)
            return true;
        return false;
    }


}
