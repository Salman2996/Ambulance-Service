package com.salman.helpme;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView latitude,longitude;
    Button requestButton;
    private Location mlocation;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    LocationManager locationManager;
    final Looper looper = null;
    LocationListener locationListener;
    private static final String TAG = "patient";
    private static final String BASE_API_URL = "http://13.126.14.112/patient?";
    OkHttpClient client;
    Request request;
    public static final String ID_KEY = "patient_id";
    private String ID;
    ProgressDialog progressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("PatientPREF", Context.MODE_PRIVATE);
        if(!pref.getBoolean("login_activity_executed", false)){
            Log.v("login","not executed");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        setContentView(R.layout.activity_main);
        ID = pref.getString(ID_KEY,"1111");
        requestButton = (Button) findViewById(R.id.requestAmbulance);
        latitude = (TextView) findViewById(R.id.latTextView);
        longitude = (TextView) findViewById(R.id.lngTextView);
        client = new OkHttpClient();

        progressdialog = new ProgressDialog(MainActivity.this);
        progressdialog.setCancelable(false);
        progressdialog.setMessage("Please Wait....");


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocation = location;
                Log.d(TAG, location.toString());
                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));

                request = new Request.Builder()
                        .url(BASE_API_URL+"id="+ID+"&lat="+location.getLatitude()+"&lng="+location.getLongitude())
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        dismissProgrressDialog();
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
                                toast("SUCCESSFULLY REQUESTED AMBULANCE");
                                Log.v(TAG,responseJson.getString("response"));
                            }else{
                                toast("AMBULANCE REQUEST FAILED");
                                Log.v(TAG,"ERROR: "+responseJson.getString("error"));
                            }
                            dismissProgrressDialog();
                        } catch (JSONException e) {
                            toast("EXCEPTION");
                            e.printStackTrace();
                            dismissProgrressDialog();
                        }
                    }
                });
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!isNetworkAvailable()){
                        toast("No internet connection!");
                        return;
                    }
                    showProgrressDialog();
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        dismissProgrressDialog();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }else{
                        showProgrressDialog();
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);
                    }
                }catch (Exception e){
                    dismissProgrressDialog();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if(locationManager != null && locationListener != null){
                        try{
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);
                        }catch (SecurityException e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        Toast.makeText(this, "Enable location permissions", Toast.LENGTH_SHORT).show();
                    }
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showProgrressDialog(){
        if(progressdialog != null && !progressdialog.isShowing()){
            progressdialog.show();
        }
    }

    private void dismissProgrressDialog(){
        if(progressdialog != null && progressdialog.isShowing()){
            progressdialog.dismiss();
        }
    }

    private void toast(final String message){
        /*Toast.makeText(this, message, Toast.LENGTH_SHORT).show();*/
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
