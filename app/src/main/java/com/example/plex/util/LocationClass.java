package com.example.plex.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationClass {

    private static FusedLocationProviderClient mFusedLocationProviderClient;
    private static Location mLastKnownLocation;

    @SuppressLint("MissingPermission")
    public static LatLng getDeviceLocation(final Context context){

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        if(task.isSuccessful() && task.getResult()!=null){
                            mLastKnownLocation = task.getResult();
                        }
                        else
                            Toast.makeText(context,"Some Error Occured",Toast.LENGTH_LONG).show();
                    }
                });
        return new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
    }


}
