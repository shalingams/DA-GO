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
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sccodesoft.dagorider.Helper.NotificationHelper;
import com.sccodesoft.dagorider.R;
import com.sccodesoft.dagorider.RateActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification().getTitle().equals("Request Canceled!"))
        {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(remoteMessage.getNotification().getTitle().equals("Driver Arrived!"))
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                showArrivedNotificationAPI26(remoteMessage.getNotification().getBody());
            else
                showArrivedNotification(remoteMessage.getNotification().getBody());
        }
        else if(remoteMessage.getNotification().getTitle().equals("Drop Off"))
        {
            openRatingActivity(remoteMessage.getNotification().getBody());
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

    private void openRatingActivity(String body) {
        Intent intent = new Intent(this,RateActivity.class);
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