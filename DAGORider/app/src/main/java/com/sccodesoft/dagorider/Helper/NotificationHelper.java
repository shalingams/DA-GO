package com.sccodesoft.dagorider.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.sccodesoft.dagorider.R;

public class NotificationHelper extends ContextWrapper {

    private static final String SC_CHANNEL_ID = "com.sccodesoft.dagorider.SCCODESOFT";
    private static final String SC_CHANNEL_NAME = "SCCODESOFT DAGO";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChanels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChanels() {
        NotificationChannel sccodesoftChannels = new NotificationChannel(SC_CHANNEL_ID,
                SC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        sccodesoftChannels.enableLights(true);
        sccodesoftChannels.enableVibration(true);
        sccodesoftChannels.setLightColor(Color.GRAY);
        sccodesoftChannels.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(sccodesoftChannels);
    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getDagoNotification(String title, String content, PendingIntent contentIntent, Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),SC_CHANNEL_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_directions_car_black_24dp);
    }
}
