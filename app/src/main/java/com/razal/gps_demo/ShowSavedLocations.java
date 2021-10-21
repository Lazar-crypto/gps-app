package com.razal.gps_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class ShowSavedLocations extends AppCompatActivity {

    ListView lv_savedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations);

        lv_savedLocations = findViewById(R.id.lv_wayPoints);

        MyGpsApp myGpsApp = (MyGpsApp)getApplicationContext();
        List<Location> savedLocations = myGpsApp.getMyLocations();

        //connect savedLocations to the listView
        lv_savedLocations.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1, savedLocations));
    }
}