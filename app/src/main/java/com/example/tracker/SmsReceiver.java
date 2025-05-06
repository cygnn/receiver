package com.example.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.ActivityCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

public class SmsReceiver extends BroadcastReceiver {
    private FusedLocationProviderClient fusedLocationClient;
    private static final String TARGET_PHONE_NUMBER = "+639981553123";
    private static final String KEYWORD = "asa ka";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = sms.getDisplayOriginatingAddress();
                    String message = sms.getMessageBody();

                    Log.d("SMS_RECEIVED", "From: " + sender + ", Message: " + message);

                    if (normalizePhoneNumber(sender).equals(normalizePhoneNumber(TARGET_PHONE_NUMBER)) && message.trim().equalsIgnoreCase(KEYWORD)) {
                        Log.d("SMS_MATCH", "Target message received && keyword success!");
                        sendSms(context, sender); // Will only work if permission is granted already
                    }
                }
            }
        }
    }
    private String normalizePhoneNumber(String number) {
        return number.replace("+63", "0").replaceAll("\\s+", "");
    }
    public void sendSms(Context context, String phoneNum) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Log.d("SENDING", "Tryting to send the sms");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION", "PERMISSION IS GRANTED");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            String message = "Latitude: " + lat + ", Longitude: " + lon;
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(phoneNum, null, message, null, null);
                            Log.d("LOCATION", "Sent location to " + phoneNum);
                        }
                        else{
                            Log.d("LOCATION", "Location is null");
                        }
                    });
        } else {
            Log.d("LOCATION", "Permission not granted, can't send location.");
        }
    }

}
