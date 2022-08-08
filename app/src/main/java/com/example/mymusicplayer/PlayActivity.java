package com.example.mymusicplayer;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mymusicplayer.adapter.MusicListAdapter;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtil;
import com.example.mymusicplayer.utils.NotificationUtil;

import java.util.Objects;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    //广播接收
    public static NotificationReceiver receiver;
    //通知类
    public static NotificationUtil notificationUtil;

    private IntentFilter intentFilter;

    private LinearLayout playToolbar;
    private ImageView backBtn;
    private static TextView songName;
    private static TextView songSinger;
    public static TextView maxDuration;
    public static TextView curDuration;
    public static ImageView songIcon;
    public static SeekBar seekBar;

    private ImageButton prevBtn;
    private ImageButton pauseBtn;
    private ImageButton nextBtn;
    private ImageButton patternBtn;
    private ImageButton listBtn;
    private static int play_pattern; //0列表循环，1单曲循环，2随机
    public static int getPlayPattern(){
        return play_pattern;
    }
    public static int currentSongPosition = -1;
    private boolean isSameAsCurSong = true;
    public static int stat = 1;
    private static final int ANIMATION_DURATION = 360 * 60;
    private static float rotateAngle = 0f;

    public static MusicPlayService.MusicControl musicControl;

    private MyServiceConnection myServiceConnection;

    private ObjectAnimator objectAnimator;

    @SuppressWarnings("all")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case 1:  //进度
                    //歌曲时长、当前进度
                    int duration = data.getInt("duration");
                    int currentPosition = data.getInt("currentPosition");
                    if ( duration == currentPosition) {
                        Log.d("lcq", "播完了。。。");
                        if(play_pattern != 1) {

                            if(PlayActivity.getPlayPattern() == 0) {
                                if (PlayActivity.currentSongPosition < MainActivity.getmList().size() - 1) {
                                    ++PlayActivity.currentSongPosition;
                                } else {
                                    PlayActivity.currentSongPosition = 0;
                                }
                            }else if(PlayActivity.getPlayPattern() == 2){
                                Random random = new Random(System.currentTimeMillis());
                                int i = random.nextInt(MainActivity.getmList().size());
                                PlayActivity.currentSongPosition = i == PlayActivity.currentSongPosition ? (i * (i+1))% MainActivity.getmList().size() : i;
                            }
                            PlayActivity.stat = 1;
                            MainActivity.setCurPos(currentSongPosition);
                            Song song = MainActivity.getmList().get(currentSongPosition);
                            PlayActivity.musicControl.play(song.getPath());
                            if(songName != null && songSinger != null && songIcon != null) {
                                songName.setText(song.getSong());
                                songSinger.setText(song.getSinger());
                                songIcon.setImageBitmap(MusicUtil.getAlbumPicture(null, song.getPath(), 3));  //大图
                            }
                            notificationUtil.getRemoteViews().setTextViewText(R.id.music_name_navi, song.getSong());
                            notificationUtil.getRemoteViews().setTextViewText(R.id.music_singer_navi, song.getSinger());
                            notificationUtil.getRemoteViews().setImageViewBitmap(R.id.img_navi, MusicUtil.getAlbumPicture(null, song.getPath(), 2));
                            notificationUtil.notifyUpdateUI();
                            MainActivity.songName.setText(song.getSong());
                            MainActivity.songSinger.setText(song.getSinger());
                            MainActivity.songIcon.setImageBitmap(MusicUtil.getAlbumPicture(null, song.getPath(), 1));
                        }
                    }
                    seekBar.setMax(duration);
                    seekBar.setProgress(currentPosition);
                    //显示时长
                    maxDuration.setText(MusicListAdapter.timeFormat(duration));
                    curDuration.setText(MusicListAdapter.timeFormat(currentPosition));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playpage);

        //获取播放的歌在列表的位置
        int clickedPosition = getIntent().getExtras().getInt("position");
        isSameAsCurSong = currentSongPosition == clickedPosition;
        currentSongPosition = clickedPosition;

        //服务绑定
        myServiceConnection = new MyServiceConnection();
        bindService(new Intent(getApplicationContext(), MusicPlayService.class), myServiceConnection, BIND_AUTO_CREATE);
        //注册广播接收
        receiver = new NotificationReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("pause_notification");
        intentFilter.addAction("play_notification");
        intentFilter.addAction("prev_notification");
        intentFilter.addAction("next_notification");
        registerReceiver(receiver, intentFilter);

        playToolbar = (LinearLayout) findViewById(R.id.play_toolbar);
        seekBar = (SeekBar) findViewById(R.id.play_seekbar);
        listBtn = findViewById(R.id.play_btn_list);
        patternBtn = findViewById(R.id.play_btn_pattern);
        patternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPatternBtnBackground();
            }
        });
        if(play_pattern == 1){
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24);
        }
        else if(play_pattern == 2){
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_shuffle_24);
        }
        else {
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_repeat_24);
        }
        prevBtn = (ImageButton) findViewById(R.id.play_btn_prev);
        nextBtn = (ImageButton) findViewById(R.id.btn_next_small);
        pauseBtn = (ImageButton) findViewById(R.id.play_btn_pause);
        pauseBtn.setBackgroundResource(stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24 : R.drawable.ic_baseline_play_circle_outline_24);

        songName = (TextView) findViewById(R.id.play_song_name);
        songSinger = (TextView) findViewById(R.id.play_song_singer);
        songIcon = (ImageView) findViewById(R.id.play_icon);
        maxDuration = (TextView) findViewById(R.id.max_duration);
        curDuration = (TextView) findViewById(R.id.cur_duration);

        displaySongInformation(MainActivity.getmList().get(currentSongPosition));
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, stat == 1 ?
                R.drawable.ic_baseline_pause_circle_outline_24_black : R.drawable.ic_baseline_play_circle_outline_24_black);
        displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));

        backBtn = (ImageView) findViewById(R.id.play_down_arrow);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                stopRotateIcon();
                overridePendingTransition(R.anim.stop, R.anim.bottom_out);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat == 1) {//当前是播放状态，执行暂停
                    pause();
                } else if (stat == 0) { //当前是暂停状态，播放
                    playContinue();
                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                musicControl.seekTo(sb.getProgress());
            }
        });


        initRotateAnim();
    }

    private void setPatternBtnBackground() {
        if(play_pattern == 0){
            play_pattern++;
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24);
            Toast.makeText(getApplicationContext(),"单曲循环", Toast.LENGTH_SHORT).show();
            if(!MusicPlayService.mediaPlayer.isLooping()) {
                MusicPlayService.mediaPlayer.setLooping(true);
            }
        }
        else if(play_pattern == 1){
            play_pattern++;
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_shuffle_24);
            Toast.makeText(getApplicationContext(),"随机播放", Toast.LENGTH_SHORT).show();
            MusicPlayService.mediaPlayer.setLooping(false);
        }
        else {
            play_pattern=0;
            patternBtn.setBackgroundResource(R.drawable.ic_baseline_repeat_24);
            Toast.makeText(getApplicationContext(),"顺序播放", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化动画，从上次保存的位置开始
     */
    private void initRotateAnim() {
        objectAnimator = ObjectAnimator.ofFloat(songIcon, "rotation", rotateAngle, rotateAngle + 360f);
        objectAnimator.setDuration(ANIMATION_DURATION);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(-1);
        objectAnimator.start();
        if (!MusicPlayService.mediaPlayer.isPlaying()) {
            objectAnimator.pause();
        } else {
            startRotateIcon();
        }
    }

    /**
     * 暂停播放
     */
    private void pause() {
        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
        //MainActivity.setPlayBtnImg(R.drawable.ic_baseline_play_circle_outline_24);
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, R.drawable.ic_baseline_play_circle_outline_24_black);
        notificationUtil.notifyUpdateUI();
        stat = 0;
        musicControl.pause();
        stopRotateIcon();
    }

    /**
     * 继续播放
     */
    private void playContinue() {
        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
        //MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24_small);
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, R.drawable.ic_baseline_pause_circle_outline_24_black);
        notificationUtil.notifyUpdateUI();
        stat = 1;
        musicControl.playContinue();
        startRotateIcon();
    }

    /**
     * 下一首
     */
    private void playNext() {
        if(play_pattern == 0 || play_pattern == 1){  //顺序播放，单曲循环
            if (currentSongPosition < MainActivity.getmList().size() - 1) {
                ++currentSongPosition;
            } else {
                currentSongPosition = 0;
            }
        }else if(play_pattern == 2){
            //随机
            Random random = new Random(System.currentTimeMillis());
            int i = random.nextInt(MainActivity.getmList().size());
            currentSongPosition = i == currentSongPosition ? (i * 2)% MainActivity.getmList().size() : i;
        }
        musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
        stat = 1;
        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
        displaySongInformation(MainActivity.getmList().get(currentSongPosition));
        startRotateIcon();

        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, R.drawable.ic_baseline_pause_circle_outline_24_black);
        displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));
    }

    /**
     * 上一首
     */
    private void playPrev() {
        if(play_pattern == 0 || play_pattern == 1){  //顺序播放，单曲循环
            if (currentSongPosition > 0) {  //列表循环
                --currentSongPosition;
            } else if (currentSongPosition == 0) {
                currentSongPosition = MainActivity.getmList().size() - 1;
            }
        }else if(play_pattern == 2){
            //随机
            Random random = new Random(System.currentTimeMillis() >> 1);
            int i = random.nextInt(MainActivity.getmList().size());
            currentSongPosition = i == currentSongPosition ? (i * (i+1))% MainActivity.getmList().size() : i;
        }
        stat = 1;
        //更新当前页和通知的信息
        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
        displaySongInformation(MainActivity.getmList().get(currentSongPosition));
        startRotateIcon();
        musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, R.drawable.ic_baseline_pause_circle_outline_24_black);
        displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));
    }


    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicControl = (MusicPlayService.MusicControl) iBinder;
            //如果用户点击了正在播放的歌，不执行play（不重新播）
            if (!isSameAsCurSong) {
                Log.d("lcq", "不是同一首歌，开始重新播放");
                musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
                startRotateIcon();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    /**
     * 显示歌曲详情
     *
     * @param song 当前播放的歌
     */
    private void displaySongInformation(Song song) {
        if(songName == null || songSinger == null || songIcon == null) return;
        songName.setText(song.getSong());
        songSinger.setText(song.getSinger());
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(PlayActivity.this, song.getPath(), 3));  //大图
    }

    /**
     * 在通知显示歌曲
     *
     * @param song 当前播放的歌
     */
    private void displaySongInfoOnNavi(Song song) {
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_name_navi, song.getSong());
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_singer_navi, song.getSinger());
        notificationUtil.getRemoteViews().setImageViewBitmap(R.id.img_navi, MusicUtil.getAlbumPicture(getApplicationContext(), song.getPath(), 2));
        notificationUtil.notifyUpdateUI();
    }

    /**
     * 继续动画转动
     */
    public void startRotateIcon() {
        if (songIcon != null && objectAnimator != null) {
            objectAnimator.resume();
        }
    }

    /**
     * 暂停动画，保存角度
     */
    public void stopRotateIcon() {
        //保存当前转的角度
        if (objectAnimator.isPaused() || songIcon == null || objectAnimator == null) return;
        float currentPlayTime = objectAnimator.getCurrentPlayTime();
        rotateAngle += (float) (currentPlayTime / (float) ANIMATION_DURATION) * 360f;
        if (rotateAngle >= 360f) rotateAngle -= 360f;
        //Log.d("lcq","ROTATE_ANGLE SAVE = " + rotateAngle + ", cur = " + currentPlayTime);
        objectAnimator.pause();
    }

    /**
     * 设置主页底部歌曲信息
     * @param pos 当前歌曲位置
     */
    private void changeButtonSongInfo(int pos) {
        MainActivity.setCurPos(pos);
        MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24);
        MainActivity.setSongName(pos);
        MainActivity.setSongSinger(pos);
        MainActivity.setSongIcon(pos, getApplicationContext());
    }

    class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
            super();
        }

        //接收广播
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case "pause_notification":
                    if (stat == 1) {  //正在播放，执行暂停
                        //主页底部栏按钮
                        MainActivity.setPlayBtnImg(R.drawable.ic_baseline_play_circle_outline_24);
                        pause();
                    } else {
                        MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24);
                        playContinue();
                    }
                    break;
                case "prev_notification":
                    playPrev();
                    changeButtonSongInfo(currentSongPosition);
                    break;
                case "next_notification":
                    playNext();
                    changeButtonSongInfo(currentSongPosition);
                    break;
            }

        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }
    }

    /**
     * 返回Activity了，就更新主页的信息，在当前Activity完全不可见前设置信息
     */
    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.setCurPos(currentSongPosition);
        MainActivity.setSongName(currentSongPosition);
        MainActivity.setSongSinger(currentSongPosition);
        MainActivity.setSongIcon(currentSongPosition, getApplicationContext());
    }

    /**
     * 释放一些静态成员
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        songName = null;
        songSinger = null;
        songIcon = null;
    }
}
