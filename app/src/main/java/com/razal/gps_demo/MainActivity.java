package com.razal.gps_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.view.View;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;


/*
*   Latitude - geografksa sirina
*   Longtitude - geografksa duyina
*   Altitude - visina (domet)
*
* */

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 10;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    //references to the ui elements
    TextView tv_lat,tv_lon,tv_altitude, tv_accuracy, tv_speed, tv_sensor,tv_address, tv_updates, tv_wayPointCounts;
    Switch sw_locationupdates, sw_gps;
    Button btn_newWaypoint,btn_showWaypointList,btn_showMap;

    //current Location
    Location currentLocation;

    //list of saved locations
    List<Location> savedLocations;

    //Config classed for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //Google's API for location services. Majority of the app functions will be using this class
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGuiVariables();
        setConfigurationForLocation();

        manageClickListenersForSwitches();

        updateGPS();

    }

    private void manageClickListenersForSwitches() {

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_gps.isChecked()){
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS");
                }
                else{
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using towers and wifi");
                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_locationupdates.isChecked())
                    startLocationUpdates();
                else
                    stopLocationUpdates();
            }
        });

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the gps location

                //add location the the global list
                MyGpsApp myGpsApp = (MyGpsApp)getApplicationContext();
                savedLocations = myGpsApp.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });

        btn_showWaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to another screen
                Intent intent = new Intent(MainActivity.this, ShowSavedLocations.class);
                startActivity(intent);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to another screen
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void stopLocationUpdates() {

        tv_updates.setText("Location is NOT beinng tracked");
        tv_lat.setText("Off");
        tv_lon.setText("Off");
        tv_speed.setText("Off");
        tv_address.setText("Off");
        tv_accuracy.setText("Off");
        tv_altitude.setText("Off");
        tv_sensor.setText("Off");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack, null);
        updateGPS();
    }

    private void setConfigurationForLocation(){

        //set all properties on locationRequest
        locationRequest = new LocationRequest();

        //how often does the default location check occur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL); //30 seconds
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL); //5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //event that is triggered whenever the update interval is met
        locationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save and update the location
                Location location = locationResult.getLastLocation();
                updateGuiVariables(location);
            }
        };
    }

    private void setGuiVariables(){

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWaypoint = findViewById(R.id.btn_newWaypoint);
        btn_showWaypointList = findViewById(R.id.btn_showWayPointList);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
        btn_showMap = findViewById(R.id.btn_showMap);
    }

    private void updateGuiVariables(Location location) {
        //update all textView objects with new location

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        //have to check if my phone can measure altitude and speed
        if(location.hasAltitude())
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        else
            tv_altitude.setText("Altitude not available");

        if(location.hasSpeed())
            tv_speed.setText(String.valueOf(location.getSpeed()));
        else
            tv_speed.setText("Speed not available");

        //for updating addresses
        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            //return last visited address
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
            tv_address.setText("Unable to get streed address");
        }

        MyGpsApp myGpsApp = (MyGpsApp)getApplicationContext();
        savedLocations = myGpsApp.getMyLocations();

        //show the number of way points saved
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));

    }

    private void updateGPS(){
        //get permissions from user to track GPS
        //get the current location from the user
        //update GUI

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // user provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission.Put the values of the location in our UI components and save current location

                    updateGuiVariables(location);
                    currentLocation = location;

                }
            });

        }

        else{
            //permission not granted, need to request them
            //if user has android version over 23 we can request for permissions

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    //trigger method after granting the permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this,"This app requires permission to be granted in order to work",Toast.LENGTH_SHORT).show();
                    finish();
                }
            break;
        }

    }
}