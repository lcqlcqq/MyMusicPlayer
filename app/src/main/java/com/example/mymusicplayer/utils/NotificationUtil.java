package com.example.mymusicplayer.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.PlayActivity;
import com.example.mymusicplayer.R;

public class NotificationUtil {
    private NotificationManager manager;
    private Context context;
    private Notification notification;
    private static final int MUSIC_DEMO_NOTIFICATION_ID = 1003;
    private RemoteViews remoteViews;

    public RemoteViews getRemoteViews() {
        return remoteViews;
    }

    public NotificationUtil(Context context) {
        this.context = context;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_music);
        remoteViews.setOnClickPendingIntent(R.id.btn_pause_navi,
                PendingIntent.getBroadcast(context, 1, new Intent(
                        PlayActivity.stat == 1 ? "pause_notification":"play_notification"), 0));
        //remoteViews.setOnClickPendingIntent(R.id.btn_pause_notification, PendingIntent.getBroadcast(context, 1, new Intent("pause_notification"), 0));
        //remoteViews.setOnClickPendingIntent(R.id.btn_continue_notification, PendingIntent.getBroadcast(context, 1, new Intent("continue_notification"), 0));
        remoteViews.setOnClickPendingIntent(R.id.btn_prev_navi, PendingIntent.getBroadcast(context, 1, new Intent("previous_notification"), 0));
        remoteViews.setOnClickPendingIntent(R.id.btn_next_navi, PendingIntent.getBroadcast(context, 1, new Intent("next_notification"), 0));
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notifyUpdateUI() {
        manager.notify(MUSIC_DEMO_NOTIFICATION_ID, notification);
    }

    public void cancelMusicDemoNotification() {
        manager.cancel(MUSIC_DEMO_NOTIFICATION_ID);
    }

    @SuppressLint("WrongConstant")
    public void showMusicDemoNotification() {
        String channel_id = "channel_music_demo";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(notificationChannel);
            notification = new NotificationCompat.Builder(context, channel_id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(PendingIntent.getActivity(context, 1, new Intent(), Notification.FLAG_ONGOING_EVENT))
                    .setCustomBigContentView(remoteViews)
                    .setOngoing(true)
                    .setChannelId(notificationChannel.getId())
                    .build();
        }
        manager.notify(MUSIC_DEMO_NOTIFICATION_ID, notification);
    }
}
