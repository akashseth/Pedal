package com.pedal.app.pedal;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Akash seth on 1/22/2016.
 */
public class LocationUtility extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener ,SensorEventListener{
    LocationManager locationManager;
    GoogleMap googleMap;
    boolean isGPSEnabled = false;
    double speed, distance, distanceInKm, avgSpeed;
    float[] resultsToGetDistance = new float[3];
    Location startLocation, endLocation,location;
    double startLatitude, endLatitude;
    double startLongitude, endLongitude;
    Date startTime;
    long start, totalTime;
    String starActivityTime = "", distanceString = "0.00", avgSpeedString = "0.00", caloriesString = "0";
    TextView speedText, distanceText, caloriesText, avgSpeedText;
    double totalCalories = 0, calories, weight;
    long totalCaloriesInLong;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    boolean hasLocation=false;
    int count =0;
    LatLng startPoint,endPoint;

    private SensorManager sensorMan;
    private Sensor accelerometer;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    boolean isMoving=false;

    GoogleApiClient mGoogleApiClient;

    public void setTextView(TextView speedText, TextView distanceText, TextView caloriesText, TextView avgSpeedText) {
        this.speedText = speedText;
        this.distanceText = distanceText;
        this.caloriesText = caloriesText;
        this.avgSpeedText = avgSpeedText;
    }

    public void requestLocationUpdate(Context context) {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(true);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, "com.pedal.app.pedal") == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 250,3, this);

        } else {
           // Log.d("permission", "required");
        }



    }

    public boolean isGpsEnabled(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        return isGPSEnabled;
    }

    public void setStartTimeOfCycling() {
        startTime = new Date();
        starActivityTime = DateFormat.format("yyyy-MM-dd kk:mm:ss", startTime).toString();
        start = startTime.getTime();

    }

    public long getStartTimeOfCycling() {
        return this.start;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location=location;
        hasLocation = true;
        if (count == 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(location.getLatitude(), location.getLongitude())), 16));
        }

        if(totalTime>3000)
        if (isMoving){
            if (location != null) {
                if (startLocation == null) {
                    startLocation = new Location(location);
                    startLatitude = startLocation.getLatitude();
                    startLongitude = startLocation.getLongitude();

                    //startLatitude = round(startLatitude, 6);
                    //startLongitude = round(startLongitude, 6);
                }
                endLocation = new Location(location);
                endLatitude = endLocation.getLatitude();
                endLongitude = endLocation.getLongitude();

                //endLatitude = round(endLatitude, 6);
                //endLongitude = round(endLongitude, 6);

                location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, resultsToGetDistance);

                    calculateDistance();

                    calculateSpeed(location);

                    calculateCalories();

                    calculateAvgSpeed();

                setPointsInArray(new LatLng(startLatitude, startLongitude));

                startLatitude = endLatitude;
                startLongitude = endLongitude;

            }
         }

    }

    public void calculateCalories() {
        if (weight == 0) {
            weight = 70;
        }
        calories = 0.35 * (resultsToGetDistance[0] / 1000) * weight;
        totalCalories += calories;
        totalCaloriesInLong = (long) totalCalories;
        caloriesString = Long.toString(totalCaloriesInLong);
        caloriesText.setText(caloriesString);
    }

    public void calculateDistance() {
        distance += resultsToGetDistance[0];
        distanceInKm = distance / 1000;
        distanceInKm = round(distanceInKm, 2);
        distanceString = Double.toString(distanceInKm);
        if(distanceInKm==0)
            distanceText.setText("0.00");
            else
        distanceText.setText(distanceString);
    }

    public void calculateSpeed(Location location) {
        speed = location.getSpeed() * 3.6;
        speed = round(speed, 2);
        if(speed==0)
        speedText.setText("0.00");
        else
            speedText.setText(Double.toString(speed));
    }

    public void calculateAvgSpeed() {
        avgSpeed = (distance / (totalTime / 1000)) * 3.6;
        avgSpeed = round(avgSpeed, 2);
        avgSpeedString = Double.toString(avgSpeed);
        if(avgSpeed==0)
        avgSpeedText.setText("0.00");
        else
            avgSpeedText.setText(avgSpeedString);
    }

    protected void initializeMap(GoogleMap map) {
        if (googleMap == null) {
            googleMap = map;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
         }

    }

    protected void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    protected void setPointsInArray(LatLng point) {
        points.add(point);

            if (count == 0) {
                startPoint = point;

                MarkerOptions startMark = new MarkerOptions().position(startPoint);
                startMark.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_green));
                googleMap.addMarker(startMark);

                count++;
            }
            endPoint=point;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endPoint, 16));
            drawPathOnMap();
            startPoint = endPoint;
    }

    protected void drawPathOnMap() {

           Polyline line = googleMap.addPolyline(new PolylineOptions()
                   .add(startPoint, endPoint)
                   .width(6)
                   .color(Color.rgb(31, 144, 255)));
    }

    protected void stopLocationUpdates()
    {
        try {
            locationManager.removeUpdates(this);
        }
        catch (SecurityException ex)
        {
            ex.printStackTrace();
        }

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(x * x + y * y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            mAccel=(float)round(mAccel,3);

            if(Math.abs(mAccel) > 1){
                //avgSpeedText.setText(Float.toString(Math.abs(mAccel)));
                isMoving=true;

                if(totalTime>3000)
                if(location!=null)
                calculateSpeed(location);
            }
            else if(Math.abs(mAccel)<0.003 && Math.abs(mAccel)>0){
                isMoving=false;
                speedText.setText("0.00");
               // avgSpeedText.setText(Float.toString(Math.abs(mAccel)));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }

    protected void deRegisterSensor()
    {
        if(sensorMan!=null)
        sensorMan.unregisterListener(this);
    }

    protected void registerAccelerometerSensor(Context context)
    {
        sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }
    public void getLastKnowLocation(Context context)
    {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
       Location  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom((latLng), 16));
        }
        mGoogleApiClient.disconnect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
