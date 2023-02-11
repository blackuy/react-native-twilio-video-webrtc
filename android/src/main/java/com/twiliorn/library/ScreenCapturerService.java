/**
 * Service to orchestrate the Twilio Screen Share connection and the various video
 * views.
 * <p>
 * Authors:
 * Manish Sahu <msahu2595@gmail.com>
 */
package com.twiliorn.library;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

@TargetApi(29)
public class ScreenCapturerService extends Service {
    private static final String CHANNEL_ID = "screen_capture";
    private static final String CHANNEL_NAME = "Screen_Capture";

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder. We know this service always runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ScreenCapturerService getService() {
            // Return this instance of ScreenCapturerService so clients can call public methods
            return ScreenCapturerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void startForeground() {
        NotificationChannel chan =
                new NotificationChannel(
                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        final int notificationId = (int) System.currentTimeMillis();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification =
                notificationBuilder
                        .setOngoing(true)
                        // .setSmallIcon(R.drawable.ic_screen_share_white_24dp)
                        .setContentTitle("ScreenCapturerService is running in the foreground")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
        startForeground(notificationId, notification);
    }

    public void endForeground() {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
