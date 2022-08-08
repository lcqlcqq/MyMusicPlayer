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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusicplayer.adapter.HomePagerAdapter;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtil;
import com.example.mymusicplayer.utils.NotificationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static ArrayList<Song> mList = new ArrayList<>();

    private static ArrayList<Song> allList = new ArrayList<>();

    public static ArrayList<Song> getmList() {
        return mList;
    }

    public static void setmList(ArrayList<Song> lst) {
        mList = lst;
    }

    public static ArrayList<Song> getAllList() {
        return allList;
    }


    private static ImageButton play;

    public static void setPlayBtnImg(int drawableId) {
        play.setBackgroundResource(drawableId);
    }

    private CardView btn_search;

    private LinearLayout block_bottom;

    private ImageButton next;

    private TextView tv_title;

    public static TextView songName;

    public static void setSongName(int pos) {
        songName.setText(mList.get(pos).getSong());
    }

    public static TextView songSinger;

    public static void setSongSinger(int pos) {
        songSinger.setText(mList.get(pos).getSinger());
    }

    public static ImageView songIcon;

    public static void setSongIcon(int pos, Context context) {
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(context, MainActivity.getmList().get(pos).getPath(), 1));
    }

    private static int cur_pos = 0;

    public static int getCurPos() {
        return cur_pos;
    }

    public static void setCurPos(int p) {
        cur_pos = p;
    }

    private MyServiceConnection myServiceConnection;

    long ViewTitleClickCnt = 0, ViewTitleClickStartTime = 0, ViewTitleClickStopTime = 0;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    private LinearLayout searchNavi;
    public static EditText editText;
    private ImageView btn_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_homepage);

        myServiceConnection = new MyServiceConnection();
        bindService(new Intent(getApplicationContext(), MusicPlayService.class), myServiceConnection, BIND_AUTO_CREATE);

        tv_title = findViewById(R.id.tv_title);

        btn_search = findViewById(R.id.btn_search);
        block_bottom = findViewById(R.id.bottom_music);
        //recyclerView = findViewById(R.id.rv_music);
        play = findViewById(R.id.btn_pause_small);
        next = findViewById(R.id.btn_next_small);
        songName = findViewById(R.id.music_name);
        songSinger = findViewById(R.id.music_singer);
        songIcon = findViewById(R.id.img_sm);
        searchNavi = findViewById(R.id.search_navi);
        editText = findViewById(R.id.search_edit);
        btn_clear = findViewById(R.id.btn_clear_search_filter);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.getText().clear();
            }
        });
        //draggingButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
        //    }
        //});

        viewPager = findViewById(R.id.mainViewPager);
        bottomNavigationView = findViewById(R.id.navigationView);
        permissionsRequest();
        initNavigationView();
        btn_search.setVisibility(View.GONE);
        //editText.setVisibility(View.INVISIBLE);

//        btn_search.setOnClickListener(view -> {
//            permissionsRequest();
//            if (mList.size() > 0) btn_search.setVisibility(View.GONE);
//        });
//        btn_search_list = findViewById(R.id.btn_search_list_filter);
//        btn_search_list.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (editText.getVisibility() == View.INVISIBLE)
//                    editText.setVisibility(View.VISIBLE);
//                else
//                    editText.setVisibility(View.INVISIBLE);
//            }
//        });

        //底部播放状态栏
        block_bottom.setOnClickListener(view -> {
            if (mList.size() == 0) {
                show("歌单里没有发现任何歌曲");
                return;
            }
            //获取当前播放的歌曲索引
            Bundle bundle = new Bundle();
            bundle.putInt("position", cur_pos);
            Intent it = new Intent(MainActivity.this, PlayActivity.class);
            it.putExtras(bundle);
            startActivity(it);
            overridePendingTransition(R.anim.bottom_in, R.anim.stop);
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mList.size() > 0 && PlayActivity.musicControl != null) {
                    if (PlayActivity.stat == 1) {
                        play.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
                        PlayActivity.stat = 0;
                        PlayActivity.musicControl.pause();
                    } else if (PlayActivity.stat == 0) {
                        play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        PlayActivity.stat = 1;
                        PlayActivity.musicControl.playContinue();
                    }
                }
                notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, PlayActivity.stat == 1 ?
                        R.drawable.ic_baseline_pause_circle_outline_24_black : R.drawable.ic_baseline_play_circle_outline_24_black);
                notificationUtil.notifyUpdateUI();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mList.size() > 0 && PlayActivity.musicControl != null) {
                    if(PlayActivity.getPlayPattern() == 0||PlayActivity.getPlayPattern() == 1) {
                        if (PlayActivity.currentSongPosition < MainActivity.mList.size() - 1) {
                            ++PlayActivity.currentSongPosition;
                        } else {
                            PlayActivity.currentSongPosition = 0;
                        }
                    }else if(PlayActivity.getPlayPattern() == 2){
                        Random random = new Random(System.currentTimeMillis() >> 1);
                        int i = random.nextInt(MainActivity.getmList().size());
                        PlayActivity.currentSongPosition = i == PlayActivity.currentSongPosition ? (i * (i+1))% MainActivity.getmList().size() : i;
                    }
                    PlayActivity.stat = 1;
                    cur_pos = PlayActivity.currentSongPosition;
                    PlayActivity.musicControl.play(MainActivity.mList.get(cur_pos).getPath());
                    updateSongInfoButton(mList.get(cur_pos));
                    updateSongInfoNoNavi(mList.get(cur_pos));
                    play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
            }
        });
        notificationUtil = new NotificationUtil(getApplicationContext());
        notificationUtil.showMusicDemoNotification();
        notificationUtil.getRemoteViews().setImageViewResource(R.id.btn_pause_navi, PlayActivity.stat == 1 ?
                R.drawable.ic_baseline_pause_circle_outline_24_black : R.drawable.ic_baseline_play_circle_outline_24_black);
        notificationUtil.notifyUpdateUI();

        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewTitleClickCnt++;
                if (ViewTitleClickCnt == 1) {
                    ViewTitleClickStartTime = System.currentTimeMillis();
                }
                if (ViewTitleClickCnt <= 2) {
                    ViewTitleClickStopTime = System.currentTimeMillis();
                    if (ViewTitleClickStopTime - ViewTitleClickStartTime > 1000) {
                        ViewTitleClickStartTime = 0;
                        ViewTitleClickStopTime = 0;
                        ViewTitleClickCnt = 0;
                    } else {
                        ViewTitleClickStartTime = ViewTitleClickStopTime;
                    }
                }
                if (ViewTitleClickCnt >= 3) {
                    ViewTitleClickStopTime = System.currentTimeMillis();
                    if (ViewTitleClickStopTime - ViewTitleClickStartTime > 1000) {
                        ViewTitleClickStartTime = 0;
                        ViewTitleClickStopTime = 0;
                        ViewTitleClickCnt = 0;
                    } else {
                        ViewTitleClickStartTime = 0;
                        ViewTitleClickStopTime = 0;
                        ViewTitleClickCnt = 0;
                        Toast.makeText(getApplicationContext(), "执行", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void initNavigationView() {
        List<Fragment> fragmentArr = new ArrayList<>();
        fragmentArr.add(new LocalMusicFragment());
        fragmentArr.add(new PictureFragment());
        fragmentArr.add(new HomeInfoFragment());

        bottomNavigationView.getMenu().add(0, 0, 1, "首页").setIcon(R.drawable.tab_1);
        bottomNavigationView.getMenu().add(0, 1, 1, "图片").setIcon(R.drawable.tab_3);
        //navigationView.getMenu().add(0, 2, 1, "园地").setIcon(R.drawable.tab_3);
        bottomNavigationView.getMenu().add(0, 3, 1, "我的").setIcon(R.drawable.tab_4);
        //禁止滑动
        viewPager.setUserInputEnabled(false);
        /**
         * 设置ViewPager2的滑动监听事件
         * isUserInputEnabled = true 时
         * */
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //设置导航栏选中位置
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                switch (position){
                    case 0:
                        searchNavi.setVisibility(View.VISIBLE);
                        tv_title.setText("本地音乐");
                        break;
                    case 1:
                        searchNavi.setVisibility(View.GONE);
                        tv_title.setText("图图");
                        break;
                    case 2:
                        searchNavi.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        viewPager.setAdapter(new HomePagerAdapter(this, fragmentArr));
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        //去掉底部长按波纹效果
        bottomNavigationView.setItemBackground(null);
        //去掉导航长按显示toast
        ViewGroup bottomNavigationMenuView = (ViewGroup) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < fragmentArr.size(); i++) {
            bottomNavigationMenuView.getChildAt(i).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
        }
        /**
         * 设置导航栏菜单项Item选中监听
         * */
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                viewPager.setCurrentItem(item.getItemId(), false);
                return true;
            }
        });
    }

    private void updateSongInfoNoNavi(Song song) {
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_name_navi, song.getSong());
        notificationUtil.getRemoteViews().setTextViewText(R.id.music_singer_navi, song.getSinger());
        notificationUtil.getRemoteViews().setImageViewBitmap(R.id.img_navi, MusicUtil.getAlbumPicture(getApplicationContext(), song.getPath(), 2));
        notificationUtil.notifyUpdateUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
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
        allList.clear();
        setAllList(MusicUtil.readMusicSongs(this));
        setmList(getAllList());
        Log.e("lcq", "mList: " + mList.size() + ", allList: " + allList.size());
        if (allList.size() > 0) {

        } else {
            show("没有发现歌曲");
        }
    }

    private void setAllList(ArrayList<Song> readMusicSongs) {
        allList = readMusicSongs;
    }


    private void updateSongInfoButton(Song song) {
        cur_pos = PlayActivity.currentSongPosition;
        songName.setText(song.getSong());
        songSinger.setText(song.getSinger());
        songIcon.setImageBitmap(MusicUtil.getAlbumPicture(this, song.getPath(), 1));
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
        play.setBackgroundResource(PlayActivity.stat == 1 ? R.drawable.ic_baseline_pause_circle_outline_24 : R.drawable.ic_baseline_play_circle_outline_24);
        Log.d("lcq", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        notificationUtil.cancelMusicDemoNotification();

        play = null;
        songName = null;
        songSinger = null;
        songIcon = null;
    }
}

