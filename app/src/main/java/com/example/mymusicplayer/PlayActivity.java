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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mymusicplayer.adapter.MusicListAdapter;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtil;
import com.example.mymusicplayer.utils.NotificationUtil;

public class PlayActivity extends AppCompatActivity {
    //广播接收
    public static NotificationReceiver receiver;
    //通知类
    public static NotificationUtil notificationUtil;

    private IntentFilter intentFilter;

    private LinearLayout playToolbar;
    private ImageView backBtn;
    private TextView songName;
    private TextView songSinger;
    public static TextView maxDuration;
    public static TextView curDuration;
    public static ImageView songIcon;
    public static SeekBar seekBar;

    private ImageButton prevBtn;
    private ImageButton pauseBtn;
    private ImageButton nextBtn;

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
            //歌曲时长、当前进度
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            if((duration - currentPosition) < 500) {
                Log.d("lcq", "当前歌曲播放完了");
            }
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            //显示时长
            maxDuration.setText(MusicListAdapter.timeFormat(duration));
            curDuration.setText(MusicListAdapter.timeFormat(currentPosition));

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playpage);

        //获取列表位置
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
        prevBtn = (ImageButton) findViewById(R.id.play_btn_prev);
        nextBtn = (ImageButton) findViewById(R.id.btn_next_small);
        pauseBtn = (ImageButton) findViewById(R.id.play_btn_pause);
        pauseBtn.setBackgroundResource(stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small : R.drawable.ic_baseline_play_circle_outline_24);

        songName = (TextView) findViewById(R.id.play_song_name);
        songSinger = (TextView) findViewById(R.id.play_song_singer);
        songIcon = (ImageView) findViewById(R.id.play_icon);
        maxDuration = (TextView) findViewById(R.id.max_duration);
        curDuration = (TextView) findViewById(R.id.cur_duration);

        displaySongInformation(MainActivity.getmList().get(currentSongPosition));
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small:R.drawable.ic_baseline_play_circle_outline_24);
        displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));

        backBtn = (ImageView) findViewById(R.id.play_down_arrow);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                stopRotateIcon();
                overridePendingTransition(R.anim.stop, R.anim.botton_out);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat == 1) {//当前是播放状态，执行暂停
                    //同步按钮
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                    MainActivity.setPlayBtnImg(R.drawable.ic_baseline_play_circle_outline_24);
                    notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_play_circle_outline_24);
                    notificationUtil.notifyUpdateUI();
                    stat = 0;
                    musicControl.pause();
                    stopRotateIcon();
                } else if (stat == 0) { //当前是暂停状态，播放
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                    notificationUtil.notifyUpdateUI();
                    stat = 1;
                    musicControl.playContinue();
                    startRotateIcon();
                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //上一首策略（顺序）
                if (currentSongPosition > 0) {
                    --currentSongPosition;
                } else if (currentSongPosition == 0) {
                    currentSongPosition = MainActivity.getmList().size() - 1;
                }
                //切歌直接播放
                stat = 1;
                musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
                //更新当前页和通知的信息
                pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));
                displaySongInformation(MainActivity.getmList().get(currentSongPosition));
                startRotateIcon();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //下一首策略（顺序）
                if (currentSongPosition < MainActivity.getmList().size() - 1) {
                    ++currentSongPosition;
                } else {
                    currentSongPosition = 0;
                }
                stat = 1;
                musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
                pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                displaySongInformation(MainActivity.getmList().get(currentSongPosition));
                displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));
                startRotateIcon();
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
        Log.d("lcq", "ROTATE_ANGLE START = " + rotateAngle);
        objectAnimator = ObjectAnimator.ofFloat(songIcon,"rotation",rotateAngle,rotateAngle+360f);
        objectAnimator.setDuration(ANIMATION_DURATION);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(-1);

        objectAnimator.start();
        if(!MusicPlayService.mediaPlayer.isPlaying()) {
            Log.d("lcq", "现在是暂停状态。动画暂停。");
            objectAnimator.pause();
        }else{
            startRotateIcon();
        }

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

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.setCurPos(currentSongPosition);
        MainActivity.setSongName(currentSongPosition);
        MainActivity.setSongSinger(currentSongPosition);
        MainActivity.setSongIcon(currentSongPosition,getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void displaySongInformation(Song song) {
        songName.setText(song.getSong());
        songSinger.setText(song.getSinger());
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(this, song.getPath(), 3));
    }

    private void displaySongInfoOnNavi(Song song){
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_name_navi,song.getSong());
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_singer_navi,song.getSinger());
        notificationUtil.getRemoteViews().setImageViewBitmap(R.id.img_navi, MusicUtil.getAlbumPicture(getApplicationContext(),song.getPath(),2));
        notificationUtil.notifyUpdateUI();
    }

    public void startRotateIcon(){
        if(songIcon!=null && objectAnimator!=null){
            //Animation animation = AnimationUtils.loadAnimation(this, R.anim.img_rotate);
            //LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
            //animation.setInterpolator(lin);
            //songIcon.startAnimation(animation);
            objectAnimator.resume();
        }
    }
    public void stopRotateIcon(){
        //保存当前转的角度
        if(objectAnimator.isPaused()) return;
        float currentPlayTime = objectAnimator.getCurrentPlayTime();
        rotateAngle += (float) (currentPlayTime /(float) ANIMATION_DURATION) * 360f;
        if(rotateAngle >= 360f) rotateAngle -= 360f;
        Log.d("lcq","ROTATE_ANGLE SAVE = " + rotateAngle + ", cur = " + currentPlayTime);
        objectAnimator.pause();
    }
    class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
            super();
        }
        //接收通知的广播
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "pause_notification":  //正在播放，执行暂停
                    if(stat == 1) {
                        stat = 0;
                        musicControl.pause();
                        //播放页更新
                        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                        //通知更新
                        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, R.drawable.ic_baseline_play_circle_outline_24);
                        notificationUtil.notifyUpdateUI();
                        //主页底部栏更新
                        MainActivity.setPlayBtnImg(R.drawable.ic_baseline_play_circle_outline_24);
                        stopRotateIcon();
                    }else{
                        stat = 1;
                        musicControl.playContinue();

                        pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                        notificationUtil.notifyUpdateUI();
                        MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24_small);
                        startRotateIcon();
                    }
                    break;
                case "prev_notification":
                    if (currentSongPosition > 0) {
                        --currentSongPosition;
                    } else if (currentSongPosition == 0) {
                        currentSongPosition = MainActivity.getmList().size() - 1;
                    }
                    stat = 1;
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
                    notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                    displaySongInformation(MainActivity.getmList().get(currentSongPosition));
                    displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));

                    MainActivity.setCurPos(currentSongPosition);
                    MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    MainActivity.setSongName(currentSongPosition);
                    MainActivity.setSongSinger(currentSongPosition);
                    MainActivity.setSongIcon(currentSongPosition,getApplicationContext());
                    startRotateIcon();
                    break;
                case "next_notification":
                    if (currentSongPosition < MainActivity.getmList().size() - 1) {
                        ++currentSongPosition;
                    } else {
                        currentSongPosition = 0;
                    }
                    stat = 1;
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    musicControl.play(MainActivity.getmList().get(currentSongPosition).getPath());
                    notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,R.drawable.ic_baseline_pause_circle_outline_24_small);
                    displaySongInformation(MainActivity.getmList().get(currentSongPosition));
                    displaySongInfoOnNavi(MainActivity.getmList().get(currentSongPosition));

                    MainActivity.setCurPos(currentSongPosition);
                    MainActivity.setPlayBtnImg(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    MainActivity.setSongName(currentSongPosition);
                    MainActivity.setSongSinger(currentSongPosition);
                    MainActivity.setSongIcon(currentSongPosition,getApplicationContext());
                    startRotateIcon();
                    break;
            }

        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }
    }

}
