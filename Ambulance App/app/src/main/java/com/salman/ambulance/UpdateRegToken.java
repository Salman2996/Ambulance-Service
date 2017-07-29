package com.salman.ambulance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.widget.Toast;


public class UpdateRegToken {
    private static final String BASE_API_URL = "http://13.126.14.112/ambulance/registertoken";
    private static final String TAG = "firebase";

    public static void update(String ID, String refreshedToken, final Context context) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", ID);
        params.put("token", refreshedToken);

        JSONObject parameters = new JSONObject(params);

        Log.v(TAG,parameters.toString());
        RequestBody body = RequestBody.create(mediaType, parameters.toString());
        //RequestBody body = RequestBody.create(mediaType, "{\n\t\"id\":\""+ID+"\",\n\t\"token\":\""+refreshedToken+"\"\n}");

        Request request = new Request.Builder()
                .url(BASE_API_URL)
                .post(body)
                .addHeader("content-type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.w(TAG, response.body().string());
                Log.i(TAG, response.toString());
                Handler handler = new Handler(Looper.getMainLooper());
                if(response.code() == 200){
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context, "FCM token updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(context, "FCM update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}