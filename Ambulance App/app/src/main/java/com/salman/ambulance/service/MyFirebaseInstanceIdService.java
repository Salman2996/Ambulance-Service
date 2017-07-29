package com.salman.ambulance.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.abhilash.ambulance.UpdateRegToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static in.abhilash.ambulance.ui.MainActivity.ID_KEY;


public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    public static final String TAG = "firebase";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        if(refreshedToken != null){
            SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
            String ID = pref.getString(ID_KEY,"0000");
            UpdateRegToken.update(ID,refreshedToken, getApplicationContext());
        }
    }
}

