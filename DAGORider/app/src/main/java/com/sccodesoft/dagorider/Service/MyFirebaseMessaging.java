package com.sccodesoft.dagorider.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Helper.NotificationHelper;
import com.sccodesoft.dagorider.Model.Token;
import com.sccodesoft.dagorider.R;
import com.sccodesoft.dagorider.RateActivity;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenToServer(s);
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if(remoteMessage.getData() != null) {
            Map<String,String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");

            if(title.equals("Request Canceled!")) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

                LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                        .sendBroadcast(new Intent(Common.CANCEL_BROADCAST_STRING));

            } else if (title.equals("Driver Arrived!")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    showArrivedNotificationAPI26(message);
                else
                    showArrivedNotification(message);
            } else if (title.equals("Drop Off")) {

                String start_address = data.get("start_address");
                String end_address = data.get("end_address");
                String time = data.get("time");
                String distance = data.get("distance");
                String total = data.get("total");
                Log.i("SHEEEEEd",total);
                String location_start = data.get("location_start");
                String location_end = data.get("location_end");
                openRatingActivity(message,start_address,end_address,time,distance,total,location_start,location_end);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getDagoNotification("Driver Arrived",body,contentIntent,defaultSound);

        notificationHelper.getManager().notify(1,builder.build());
    }

    private void openRatingActivity(String body,String start_address, String end_address, String time, String distance, String fee,String location_start,String location_end) {

        LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                .sendBroadcast(new Intent(Common.DROPOFF_BROADCAST_STRING));

        Intent intent = new Intent(this,RateActivity.class);
        intent.putExtra("start_address",start_address);
        intent.putExtra("end_address",end_address);
        intent.putExtra("time",time);
        intent.putExtra("distance",distance);
        intent.putExtra("total",fee);
        intent.putExtra("location_start",location_start);
        intent.putExtra("location_end",location_end);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_directions_car_black_24dp)
                .setContentTitle("Driver Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }
}
