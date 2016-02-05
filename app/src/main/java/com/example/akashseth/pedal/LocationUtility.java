package com.example.akashseth.pedal;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Akash seth on 1/22/2016.
 */
public class LocationUtility extends Service implements LocationListener {
    LocationManager locationManager;
    GoogleMap googleMap;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    double speed, distance, distanceInKm, avgSpeed;
    float[] resultsToGetDistance = new float[3];
    Location startLocation, endLocation;
    double startLatitude, endLatitude;
    double startLongitude, endLongitude;
    Date startTime, endTime;
    long start, end, totalTime;
    String starActivityTime = "", distanceString = "0.00", avgSpeedString = "00.00", caloriesString = "0";
    Thread threadForTimer;
    TextView speedText, distanceText, caloriesText, avgSpeedText;
    double totalCalories = 0, calories, weight;
    long totalCaloriesInLong;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    Marker endMarkOnMap = null;
    CameraPosition cameraPosition;
    boolean hasLocation=false;


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
        if (pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, "com.example.akashseth.pedal") == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, false), 250, 5, this);
        } else {
            Log.d("permission", "required");
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

    public LatLng getLastKnowLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);

        Location location = null;

        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, "com.example.akashseth.pedal") == PackageManager.PERMISSION_GRANTED) {

            location = locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);
        } else {
            Log.d("permission", "required");
        }


        return new LatLng(location.getLatitude(), location.getLongitude());

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

            hasLocation=true;
            if (location != null) {
                if (startLocation == null) {
                    startLocation = new Location(location);
                    startLatitude = startLocation.getLatitude();
                    startLongitude = startLocation.getLongitude();

                    startLatitude = round(startLatitude, 6);
                    startLongitude = round(startLongitude, 6);
                }
                    endLocation = new Location(location);
                    endLatitude = endLocation.getLatitude();
                    endLongitude = endLocation.getLongitude();

                    endLatitude = round(endLatitude, 6);
                    endLongitude = round(endLongitude, 6);

                    location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, resultsToGetDistance);

                    if (totalTime > 5000) {
                        calculateDistance();

                        calculateSpeed(location);

                        calculateCalories();

                        calculateAvgSpeed();
                    }

                    // zoomMapToUserLocation();

                    setPointsInArray(new LatLng(startLatitude, startLongitude));

                    startLatitude = endLatitude;
                    startLongitude = endLongitude;

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
        distanceText.setText(distanceString);
    }

    public void calculateSpeed(Location location) {
        speed = location.getSpeed() * 3.6;
        speed = round(speed, 2);
        speedText.setText(Double.toString(speed));
    }

    public void calculateAvgSpeed() {
        avgSpeed = (distance / (totalTime / 1000)) * 3.6;
        avgSpeed = round(avgSpeed, 2);
        avgSpeedString = Double.toString(avgSpeed);
        avgSpeedText.setText(avgSpeedString);
    }

    public void zoomMapToUserLocation() {
        cameraPosition = new CameraPosition.Builder().target(
                new LatLng(startLatitude, startLongitude)).zoom(16).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    protected void initializeMap(GoogleMap map, LatLng latLng) {
        if (googleMap == null) {
            googleMap = map;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);


           /* if (latLng != null) {

                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        latLng).zoom(16).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }*/

        }

    }

    protected void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }


    protected void setPointsInArray(LatLng point) {

        points.add(point);
        drawPathOnMap(points);

    }

    protected void drawPathOnMap(ArrayList<LatLng> points) {
        for (int i = 0; i < points.size() - 1; i++) {

            Polyline line = googleMap.addPolyline(new PolylineOptions()
                    .add(points.get(i), points.get(i + 1))
                    .width(6)
                    .color(Color.rgb(31, 144, 255)));
        }

        MarkerOptions startMark = new MarkerOptions().position(points.get(0));
        startMark.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_green));
        googleMap.addMarker(startMark);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(points.size() - 1), 16));
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


}
