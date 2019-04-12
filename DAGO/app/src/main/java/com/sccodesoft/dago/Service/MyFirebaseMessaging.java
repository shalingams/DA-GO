package com.sccodesoft.dago.Service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.sccodesoft.dago.Common.Common;
import com.sccodesoft.dago.CustomerCall;
import com.sccodesoft.dago.Model.Token;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData() != null) {
            //Lat lng from rider app
            Map<String,String> data = remoteMessage.getData();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");
            String destlat = data.get("destlat");
            String destlng = data.get("destlng");
            String customerid = data.get("customerid");
            String isKandy = data.get("isKandy");

           // LatLng customer_location = new Gson().fromJson(message, LatLng.class);

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("destlat",destlat);
            intent.putExtra("destlng",destlng);
            intent.putExtra("customer", customer);
            intent.putExtra("cusid",customerid);
            intent.putExtra("isKandy",isKandy);

            startActivity(intent);
        }
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }
}
