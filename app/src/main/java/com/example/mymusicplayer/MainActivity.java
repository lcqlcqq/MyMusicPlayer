package com.example.mymusicplayer;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    private Button btn_search;

    public static ArrayList<Song> mList = new ArrayList<>();

    private MusicListAdapter mAdapter;

    private RecyclerView recyclerView;

   /* private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_homepage);
        recyclerView = findViewById(R.id.rv_music);
        btn_search = findViewById(R.id.btn_search);
        LinearLayout block_bottom = findViewById(R.id.bottom_music);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionsRequest();
                if (mList.size() > 0) btn_search.setVisibility(View.INVISIBLE);
            }
        });

        //底部播放状态栏
        block_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前播放的歌曲索引（从service？）
                int pos = 0;
                Bundle bundle = new Bundle();
                bundle.putInt("position", pos);
                Intent it = new Intent(MainActivity.this, PlayActivity.class);
                it.putExtras(bundle);
                startActivity(it);
                overridePendingTransition(R.anim.botton_in, R.anim.stop);
            }
        });
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
        //清除列表数据
        mList.clear();
        mList = MusicUtils.readMusicSongs(this);
        Log.e("lcq", "size: " + mList.size());
        if (mList != null && mList.size() > 0) {
            //显示本地音乐
            showLocalMusicData();
        } else {
            show("没有发现歌曲");
        }
    }

    private void showLocalMusicData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicListAdapter(MainActivity.this, mList);
        recyclerView.setAdapter(mAdapter);
    }

    protected void show(CharSequence c) {
        Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
    }
}

