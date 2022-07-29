package com.example.mymusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mymusicplayer.utils.MusicUtils;

public class PlayActivity extends AppCompatActivity {
    LinearLayout playToolbar;
    ImageView backBtn;
    TextView songName;
    TextView songSinger;
    ImageView songIcon;
    SeekBar seekBar;

    ImageButton prevBtn;
    ImageButton playBtn;
    ImageButton nextBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_playpage);
        playToolbar = (LinearLayout) findViewById(R.id.play_toolbar);
        backBtn = (ImageView) findViewById(R.id.play_down_arrow);
        songName = (TextView) findViewById(R.id.play_song_name);
        songSinger = (TextView) findViewById(R.id.play_song_singer);
        songIcon = (ImageView) findViewById(R.id.play_icon);
        seekBar = (SeekBar) findViewById(R.id.play_seekbar);
        prevBtn = (ImageButton) findViewById(R.id.btn_prev_small);
        playBtn = (ImageButton) findViewById(R.id.btn_play_small);
        nextBtn = (ImageButton) findViewById(R.id.btn_next_small);

        int pos = getIntent().getExtras().getInt("position");


        songName.setText(MainActivity.mList.get(pos).getSong());
        songSinger.setText(MainActivity.mList.get(pos).getSinger());
        songIcon.setImageBitmap(MusicUtils.getAlbumPicture(null,MainActivity.mList.get(pos).getPath(),3));
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayActivity.super.onBackPressed();
                overridePendingTransition(R.anim.stop,R.anim.botton_out);
            }
        });

    }




}
