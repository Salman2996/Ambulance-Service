package com.salman.ambulance.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import in.abhilash.ambulance.R;
import in.abhilash.ambulance.ui.MainActivity;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "FCM";
    private Toast toast;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Map<String,String> fcmData = remoteMessage.getData();
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + fcmData);

        if(fcmData != null && fcmData.containsKey("patient_id")
                && fcmData.containsKey("patient_lat") && fcmData.containsKey("patient_lng")
                && fcmData.containsKey("hospital_lat") && fcmData.containsKey("hospital_lng")
                && fcmData.containsKey("ambulance_lat") && fcmData.containsKey("ambulance_lng")){
            Intent i = new Intent();
            i.setClass(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("patient_id",fcmData.get("patient_id"));
            i.putExtra("patient_lat",fcmData.get("patient_lat"));
            i.putExtra("patient_lng",fcmData.get("patient_lng"));
            i.putExtra("hospital_lat",fcmData.get("hospital_lat"));
            i.putExtra("hospital_lng",fcmData.get("hospital_lng"));
            i.putExtra("ambulance_lat",fcmData.get("ambulance_lat"));
            i.putExtra("ambulance_lng",fcmData.get("ambulance_lng"));
            notifyUser(i);
            startActivity(i);
        }else{
            toast("INVALID_NOTIFICATION");
        }
    }

    public void notifyUser(Intent notificationIntent) {
        Context context = getApplicationContext();
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Ambulance Request Received")
                .setContentText("This ambulance is allotted to a calling patient")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();
        final NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(100, notification);
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
