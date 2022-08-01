package com.example.mymusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.adapter.MusicListAdapter;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtils;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private CardView btn_search;

    public static ArrayList<Song> mList = new ArrayList<>();

    private MusicListAdapter mAdapter;

    private RecyclerView recyclerView;

    private LinearLayout block_bottom;

    private ImageButton play;

    private ImageButton next;

    public static TextView songName;
    public static TextView songSinger;
    public static ImageView songIcon;

    public static int cur_pos = 0;

    private MyServiceConnection myServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_homepage);

        myServiceConnection = new MyServiceConnection();
        bindService(new Intent(getApplicationContext(),MusicPlayService.class),myServiceConnection,BIND_AUTO_CREATE);


        recyclerView = findViewById(R.id.rv_music);
        btn_search = findViewById(R.id.btn_search);
        block_bottom = findViewById(R.id.bottom_music);

        play = findViewById(R.id.btn_pause_small);
        next = findViewById(R.id.btn_next_small);
        songName = findViewById(R.id.music_name);
        songSinger = findViewById(R.id.music_singer);
        songIcon = findViewById(R.id.img_sm);

        btn_search.setOnClickListener(view -> {
            permissionsRequest();
            if (mList.size() > 0) btn_search.setVisibility(View.INVISIBLE);
        });
        //mediaPlayer = new MediaPlayer();


        //底部播放状态栏
        block_bottom.setOnClickListener(view -> {
            //mediaPlayer.reset();
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.mList.get(0).getPath()));
            //mediaPlayer.start();
            //获取当前播放的歌曲索引
            Bundle bundle = new Bundle();
            bundle.putInt("position", cur_pos);
            Intent it = new Intent(MainActivity.this, PlayActivity.class);
            it.putExtras(bundle);
            startActivity(it);
            overridePendingTransition(R.anim.botton_in, R.anim.stop);
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PlayActivity.stat == 1){
                    play.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                    PlayActivity.stat = 0;
                    PlayActivity.musicControl.pause();
                }else if(PlayActivity.stat == 0){
                    play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                    PlayActivity.stat = 1;
                    PlayActivity.musicControl.playContinue();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PlayActivity.currentSongPosition < MainActivity.mList.size()-1){
                    ++PlayActivity.currentSongPosition;
                }else {
                    PlayActivity.currentSongPosition = 0;
                }
                cur_pos = PlayActivity.currentSongPosition;
                PlayActivity.musicControl.play(MainActivity.mList.get(cur_pos).getPath());
                displayCurrentSongInfo();
                play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void permissionsRequest() {
        PermissionX.init(this).permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "即将申请的权限是程序必须依赖的权限", "我已明白");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
                    }
                })
                .setDialogTintColor(R.color.white, R.color.app_color)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            //获取本地音乐列表
                            getMusicList();
                        } else {
                            show("您拒绝了如下权限：" + deniedList);
                        }
                    }
                });
    }

    private void getMusicList() {
        mList.clear();
        mList = MusicUtils.readMusicSongs(this);
        if (mList.size() > 0) {
            showLocalMusicData();
        } else {
            show("没有发现歌曲");
        }
    }

    private void showLocalMusicData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicListAdapter(MainActivity.this);
        recyclerView.setAdapter(mAdapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x1 && resultCode == 0x1)
        {
            Log.d("lcq", "destroy result");
            Bundle bd = data.getExtras();
            displayCurrentSongInfo();
        }
    }

    private void displayCurrentSongInfo(){
        cur_pos = PlayActivity.currentSongPosition;
        songName.setText(mList.get(cur_pos).getSong());
        songSinger.setText(mList.get(cur_pos).getSinger());
        songIcon.setImageBitmap(MusicUtils.getAlbumPicture(this,mList.get(cur_pos).getPath(),1));
    }
    static class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
    public void show(CharSequence c) {
        Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        play.setBackgroundResource(PlayActivity.stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small:R.drawable.ic_baseline_play_circle_outline_24);
        Log.d("lcq", "onResume");
    }
}

