package com.example.mymusicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.PlayActivity;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    MainActivity context;

    public MusicListAdapter(MainActivity c, ArrayList<Song> list) {
        context = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        ViewHolder holder = new ViewHolder(view);


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                Song _song = MainActivity.mList.get(pos);

                Toast.makeText(view.getContext(), "点击了第" + pos + "项, 文件路径: " + _song.getPath(), Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putInt("position", pos);
                Intent it = new Intent(context, PlayActivity.class);
                it.putExtras(bundle);
                context.startActivity(it);
                context.overridePendingTransition(R.anim.botton_in, R.anim.stop);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = MainActivity.mList.get(position);
        holder.itemPosition.setText(String.valueOf(position + 1));
        holder.itemIcon.setImageBitmap(MusicUtils.getAlbumPicture(null, song.getPath(), 1));
        holder.songName.setText(song.getSong());
        holder.songSinger.setText(song.getSinger());
        //歌曲的毫秒数
        holder.duration.setText(String.valueOf(song.getDuration()));
    }

    @Override
    public int getItemCount() {
        return MainActivity.mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView itemIcon;
        TextView itemPosition;
        TextView songName;
        TextView songSinger;
        TextView duration;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            view = rootView;
            itemPosition = (TextView) rootView.findViewById(R.id.item_position);
            songName = (TextView) rootView.findViewById(R.id.item_song_name);
            songSinger = (TextView) rootView.findViewById(R.id.item_singer);
            duration = (TextView) rootView.findViewById(R.id.duration_time);
            itemIcon = (ImageView) rootView.findViewById(R.id.item_icon);

        }
    }
}
