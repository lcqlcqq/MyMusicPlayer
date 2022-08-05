package com.example.mymusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayService extends Service {

    public static MediaPlayer mediaPlayer;
    private Timer timer;
    public MusicPlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

            }
        });
    }

    public void addTimer() {
        if (timer == null) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer == null) {
                        return;
                    }
                    Message message = PlayActivity.handler.obtainMessage(1);
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration",mediaPlayer.getDuration());
                    bundle.putInt("currentPosition",mediaPlayer.getCurrentPosition());
                    message.setData(bundle);
                    PlayActivity.handler.sendMessage(message);
                }
            };
            timer.schedule(task,5,500);
        }
    }

    class MusicControl extends Binder {

        public void play (String path) {
            try {
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
                mediaPlayer.start();
                addTimer();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        public void pause() {
            mediaPlayer.pause();
        }

        public void playContinue() {
            mediaPlayer.start();
        }

        public void seekTo(int progress) {
            mediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
        if (mediaPlayer == null)
            return;
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}