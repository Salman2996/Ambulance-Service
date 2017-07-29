package com.salman.ambulance.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import in.abhilash.ambulance.UpdateRegToken;
import in.abhilash.ambulance.service.MyGPSService;
import in.abhilash.ambulance.R;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1 ;
    Button startButton, stopButton;
    private String ID;
    private boolean service_running = false;
    public static final String TAG = "firebase";
    public static final String ID_KEY = "ambulance_id";
    private Toast toast;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateToken:
                String token = FirebaseInstanceId.getInstance().getToken();
                if(token!= null){
                    Log.v(TAG,"Token: "+token);
                    SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                    String ID = pref.getString(ID_KEY,"0000");
                    UpdateRegToken.update(ID,token, MainActivity.this);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if(!pref.getBoolean("login_activity_executed", false)){
            Log.v("login","not executed");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            if(extras != null) loginIntent.putExtras(extras);
            startActivity(loginIntent);
            finish();
        }
        setContentView(R.layout.activity_main);
        ID = pref.getString(ID_KEY,"0000");

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        final Intent serviceIntent = new Intent(MainActivity.this, MyGPSService.class);

        service_running = isMyServiceRunning(MyGPSService.class);

        if(service_running){
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        }else{
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    toast("No internet connection. Try again");
                    return;
                }
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }else{
                    startService(serviceIntent);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                stopService(serviceIntent);
            }
        });


        if(extras != null){
            String patient_id = intent.getStringExtra("patient_id");
            final String patient_lat = intent.getStringExtra("patient_lat");
            final String patient_lng = intent.getStringExtra("patient_lng");
            final String hospital_lat = intent.getStringExtra("hospital_lat");
            final String hospital_lng = intent.getStringExtra("hospital_lng");
            final String ambulance_lat = intent.getStringExtra("ambulance_lat");
            final String ambulance_lng = intent.getStringExtra("ambulance_lng");
            if(patient_id != null && !patient_id.isEmpty()
                    && patient_lat != null && !patient_lat.isEmpty()
                        && patient_lng != null && !patient_lng.isEmpty()){
                //Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4192?q=" + Uri.encode("1st & Pike, Seattle"));

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Incoming Ambulance Request")
                        .setMessage("Accept and start Navigation?")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //String url = "https://www.google.com/maps/dir/12.964612,77.573787/12.965465,77.574110/12.964769,77.576802";

                                StringBuilder builder = new StringBuilder();
                                builder.append("https://www.google.com/maps/dir/");
                                builder.append(ambulance_lat+','+ambulance_lng+'/');
                                builder.append(hospital_lat+','+hospital_lng+'/');
                                builder.append(patient_lat+','+patient_lng);

                                Uri gmmIntentUri = Uri.parse(builder.toString());
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null);
                alertDialog.show();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    final Intent serviceIntent = new Intent(MainActivity.this, MyGPSService.class);
                    startService(serviceIntent);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
