package com.salman.ambulance.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static in.abhilash.ambulance.ui.MainActivity.ID_KEY;

public class MyGPSService extends Service
{
    private static final String BASE_API_URL = "http://13.126.14.112/ambulance?";
    private static final String TAG = "ambulance_service";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;
    private String ID;
    OkHttpClient client;
    Request request;
    Toast toast;
    public static final String ACTION_PING = MyGPSService.class.getName() + ".PING";
    public static final String ACTION_PONG = MyGPSService.class.getName() + ".PONG";


    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            request = new Request.Builder()
                    .url(BASE_API_URL+"id="+ID+"&lat="+location.getLatitude()+"&lng="+location.getLongitude())
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    toast("ERROR UPLOADING: "+e.getMessage());
                    Log.e(TAG, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.w(TAG, responseBody);
                    Log.i(TAG, response.toString());
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        if(responseJson.getInt("status") == 200){
                            //toast(responseJson.getString("message"));
                        }else{
                            toast("ERROR: "+responseJson.getString("error"));
                        }
                    } catch (JSONException e) {
                        toast("EXCEPTION");
                        e.printStackTrace();
                    }
                }
            });
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            if(status == LocationProvider.AVAILABLE)
                Log.e(TAG, "onStatusChanged: " + provider+", status: AVAILABLE");
            else if(status == LocationProvider.OUT_OF_SERVICE)
                Log.e(TAG, "onStatusChanged: " + provider+", status: OUT_OF_SERVICE");
            else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                Log.e(TAG, "onStatusChanged: " + provider+", status: TEMPORARILY_UNAVAILABLE");
            else
                Log.e(TAG, "onStatusChanged: " + provider+", status: UNKNOWN");
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.e(TAG, "onBind");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        toast("SERVICE_STARTED");
        super.onStartCommand(intent, flags, startId);
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        ID = pref.getString(ID_KEY,"0000");
        return START_STICKY;
    }
    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        ID = pref.getString(ID_KEY,"0000");
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG,"Location Permission not granted, stopping service");
            stopSelf();
            return;
        }
            initializeLocationManager();
        client = new OkHttpClient();
        //remove if we do not need network provided location
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        //gps provided location
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        }  catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        toast("SERVICE_DESTROYED");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch(SecurityException sex){
                    Log.i(TAG, "SECURITY EXCEPTION: fail to remove location listeners, ignore", sex);
                }catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }
    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void toast(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(toast != null)
                    toast.cancel();
                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}