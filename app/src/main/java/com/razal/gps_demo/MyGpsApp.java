package com.razal.gps_demo;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class MyGpsApp extends Application {

    //only one instance of this class can be created
    private static MyGpsApp singleton;

    //list that represents places where we have been
    private List<Location> myLocations;

    public List<Location> getMyLocations() {
        return myLocations;
    }

    public void setMyLocations(List<Location> myLocations) {
        this.myLocations = myLocations;
    }

    public MyGpsApp getSingleton(){
        return singleton;
    }

    public void onCreate(){
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    }
}
