package com.sccodesoft.dago.Service;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.sccodesoft.dago.CustomerCall;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Lat lng from rider app
        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        Intent intent = new Intent(getBaseContext(),CustomerCall.class);
        intent.putExtra("lat",customer_location.latitude);
        intent.putExtra("lng",customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());

        startActivity(intent);
    }
}
