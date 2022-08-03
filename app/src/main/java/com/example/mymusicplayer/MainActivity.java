package com.example.mymusicplayer;

import static com.example.mymusicplayer.PlayActivity.notificationUtil;
import static com.example.mymusicplayer.PlayActivity.receiver;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.example.mymusicplayer.utils.MusicUtil;
import com.example.mymusicplayer.utils.NotificationUtil;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static ArrayList<Song> mList = new ArrayList<>();

    public static ArrayList<Song> getmList(){
        return mList;
    }
    private static void setmList(ArrayList<Song> lst){
        mList = lst;
    }
    private static ImageButton play;

    public static void setPlayBtnImg(int drawableId){
        play.setBackgroundResource(drawableId);
    }
    private MusicListAdapter mAdapter;

    private RecyclerView recyclerView;

    private CardView btn_search;

    private LinearLayout block_bottom;

    private ImageButton next;

    private static TextView songName;

    public static void setSongName(int pos){
        songName.setText(mList.get(pos).getSong());
    }

    private static TextView songSinger;

    public static void setSongSinger(int pos){
        songSinger.setText(mList.get(pos).getSinger());
    }
    private static ImageView songIcon;

    public static void setSongIcon(int pos, Context context){
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(context, MainActivity.getmList().get(pos).getPath(), 1));
    }

    private static int cur_pos = 0;

    public static int getCurPos(){
        return cur_pos;
    }
    public static void setCurPos(int p){
        cur_pos = p;
    }

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
            if (mList.size() > 0) btn_search.setVisibility(View.GONE);
        });

        //底部播放状态栏
        block_bottom.setOnClickListener(view -> {
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
                if(MainActivity.mList.size() > 0 && PlayActivity.musicControl != null) {
                    if (PlayActivity.stat == 1) {
                        play.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                        PlayActivity.stat = 0;
                        PlayActivity.musicControl.pause();
                    } else if (PlayActivity.stat == 0) {
                        play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                        PlayActivity.stat = 1;
                        PlayActivity.musicControl.playContinue();
                    }
                }
                notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,PlayActivity.stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small:R.drawable.ic_baseline_play_circle_outline_24);
                notificationUtil.notifyUpdateUI();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.mList.size() > 0 && PlayActivity.musicControl != null) {
                    if (PlayActivity.currentSongPosition < MainActivity.mList.size() - 1) {
                        ++PlayActivity.currentSongPosition;
                    } else {
                        PlayActivity.currentSongPosition = 0;
                    }
                    cur_pos = PlayActivity.currentSongPosition;
                    PlayActivity.musicControl.play(MainActivity.mList.get(cur_pos).getPath());
                    updateSongInfoButton(mList.get(cur_pos));
                    updateSongInfoNoNavi(mList.get(cur_pos));
                    play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24_small);
                }
            }
        });
        notificationUtil = new NotificationUtil(getApplicationContext());
        notificationUtil.showMusicDemoNotification();
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi,PlayActivity.stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24_small:R.drawable.ic_baseline_play_circle_outline_24);
        notificationUtil.notifyUpdateUI();
    }

    private void updateSongInfoNoNavi(Song song){
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_name_navi,song.getSong());
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_singer_navi,song.getSinger());
        notificationUtil.getRemoteViews().setImageViewBitmap(R.id.img_navi, MusicUtil.getAlbumPicture(getApplicationContext(),song.getPath(),2));
        notificationUtil.notifyUpdateUI();
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
        setmList(MusicUtil.readMusicSongs(this));
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

    private void updateSongInfoButton(Song song){
        cur_pos = PlayActivity.currentSongPosition;
        songName.setText(song.getSong());
        songSinger.setText(song.getSinger());
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(this,song.getPath(),1));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        notificationUtil.cancelMusicDemoNotification();
    }
}

