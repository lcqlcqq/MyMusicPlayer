package com.example.mymusicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.example.mymusicplayer.utils.MusicUtils;

public class PlayActivity extends AppCompatActivity {
    MainActivity context;
    LinearLayout playToolbar;
    ImageView backBtn;
    TextView songName;
    TextView songSinger;
    static TextView maxDuration;
    static TextView curDuration;
    ImageView songIcon;
    static SeekBar seekBar;

    ImageButton prevBtn;
    ImageButton pauseBtn;
    ImageButton nextBtn;

    public static int currentSongPosition = -1;
    private boolean isSameAsCurSong = true;
    private boolean isFirstClick = true;
    public static int stat = 1;
    public static MusicPlayService.MusicControl musicControl;
    private MyServiceConnection myServiceConnection;
    //接收消息
    @SuppressWarnings("all")
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            maxDuration.setText(MusicListAdapter.timeFormat(duration));
            curDuration.setText(MusicListAdapter.timeFormat(currentPosition));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playpage);

        int clickedPosition = getIntent().getExtras().getInt("position");
        Log.e("lcq", "点击了第" + clickedPosition + "项");
        Log.e("lcq", "当前是第" + currentSongPosition + "项");
        isSameAsCurSong = currentSongPosition == clickedPosition;
        Log.d("lcq", "是同一个:" + isSameAsCurSong);
        currentSongPosition = clickedPosition;

        myServiceConnection = new MyServiceConnection();
        bindService(new Intent(getApplicationContext(), MusicPlayService.class), myServiceConnection, BIND_AUTO_CREATE);

        playToolbar = (LinearLayout) findViewById(R.id.play_toolbar);
        seekBar = (SeekBar) findViewById(R.id.play_seekbar);
        prevBtn = (ImageButton) findViewById(R.id.btn_prev_small);
        nextBtn = (ImageButton) findViewById(R.id.btn_next_small);
        pauseBtn = (ImageButton) findViewById(R.id.btn_pause_small);
        pauseBtn.setBackgroundResource(stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small:R.drawable.ic_baseline_play_circle_outline_24);
        songName = (TextView) findViewById(R.id.play_song_name);
        songSinger = (TextView) findViewById(R.id.play_song_singer);
        songIcon = (ImageView) findViewById(R.id.play_icon);
        maxDuration = (TextView) findViewById(R.id.max_duration);
        curDuration = (TextView) findViewById(R.id.cur_duration);
        displaySongInformation(MainActivity.mList.get(currentSongPosition));


        backBtn = (ImageView) findViewById(R.id.play_down_arrow);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.stop, R.anim.botton_out);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat == 1) {
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                    stat = 0;
                    musicControl.pause();
                } else if (stat == 0) {
                    pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    stat = 1;
                    musicControl.playContinue();
                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSongPosition > 0) {
                    --currentSongPosition;
                } else if (currentSongPosition == 0) {
                    currentSongPosition = MainActivity.mList.size() - 1;
                }
                musicControl.play(MainActivity.mList.get(currentSongPosition).getPath());
                displaySongInformation(MainActivity.mList.get(currentSongPosition));
                pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSongPosition < MainActivity.mList.size() - 1) {
                    ++currentSongPosition;
                } else {
                    currentSongPosition = 0;
                }
                musicControl.play(MainActivity.mList.get(currentSongPosition).getPath());
                displaySongInformation(MainActivity.mList.get(currentSongPosition));
                pauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
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
    }

    void displaySongInformation(Song song) {
        songName.setText(song.getSong());
        songSinger.setText(song.getSinger());
        songIcon.setImageBitmap(MusicUtils.getAlbumPicture(this, song.getPath(), 3));
        //maxDuration.setText(song.getDuration());
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicControl = (MusicPlayService.MusicControl) iBinder;
            //如果点的是正在播放的歌，不执行play（否则从头播放了）
            if(!isSameAsCurSong){
                musicControl.play(MainActivity.mList.get(currentSongPosition).getPath());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    @Override
    public void onBackPressed() {
        Intent it = getIntent();
        Bundle bd = new Bundle();
        bd.putInt("position", currentSongPosition);
        it.putExtras(bd);
        setResult(0x1, it);

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.e("lcq", "onDestroyed ");
        MainActivity.cur_pos = currentSongPosition;
        MainActivity.songName.setText(MainActivity.mList.get(currentSongPosition).getSong());
        MainActivity.songSinger.setText(MainActivity.mList.get(currentSongPosition).getSinger());
        MainActivity.songIcon.setImageBitmap(MusicUtils.getAlbumPicture(getApplicationContext(),MainActivity.mList.get(currentSongPosition).getPath(),1));
        super.onDestroy();
    }
}
