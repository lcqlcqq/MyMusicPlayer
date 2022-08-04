package com.example.mymusicplayer.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.PlayActivity;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.bean.Song;
import com.example.mymusicplayer.utils.MusicUtil;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> implements Filterable {

    MainActivity context;

    public MusicListAdapter(MainActivity c) {
        context = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        ViewHolder holder = new ViewHolder(view);


        holder.view.setOnClickListener(view1 -> {
            int pos = holder.getAdapterPosition();
            Song _song = MainActivity.getmList().get(pos);
            //Toast.makeText(view1.getContext(), "点击了第" + pos + "项, 路径: " + _song.getPath(), Toast.LENGTH_SHORT).show();
            Bundle bundle = new Bundle();
            bundle.putInt("position", pos);
            Intent it = new Intent(context, PlayActivity.class);
            it.putExtras(bundle);
            context.startActivity(it);
            context.overridePendingTransition(R.anim.bottom_in, R.anim.stop);
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = MainActivity.getmList().get(position);
        holder.itemPosition.setText(String.valueOf(position + 1));
        holder.itemIcon.setImageBitmap(MusicUtil.getAlbumPicture(context, song.getPath(), 1));
        holder.songName.setText(song.getSong());
        holder.songSinger.setText(song.getSinger());

        holder.duration.setText(timeFormat(song.getDuration()));
    }

    @Override
    public int getItemCount() {
        return MainActivity.getmList().size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            //执行过滤操作
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                //MainActivity.getmList().clear();
                String charString = charSequence.toString();
                ArrayList<Song> lst = new ArrayList<>();
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    Log.d("lcq", "没有过滤内容，使用全部歌曲，数量是"+MainActivity.getAllList().size());
                    //MainActivity.setmList(MainActivity.getAllList());
                    lst = MainActivity.getAllList();
                } else {
                    Log.d("lcq", "过滤：" + charString);
                    for (Song song : MainActivity.getAllList()) {
                        //这里根据需求，添加匹配规则
                        if (song.getSong().contains(charString) || song.getSinger().contains(charString)) {
                            lst.add(song);
                        }
                    }
                    Log.d("lcq", "过滤完成，临时表的歌曲数是" + MainActivity.getmList().size());
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = lst;
                return filterResults;
            }

            //把过滤后的值返回出来
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                MainActivity.setmList((ArrayList<Song>) filterResults.values);
                notifyDataSetChanged();
            }
        };
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

    public static String timeFormat(int t) {
        if (t < 60000) {
            return "00:" + getString((t % 60000) / 1000);
        } else if (t < 3600000) {
            return getString((t % 3600000) / 60000) + ":" + getString((t % 60000) / 1000);
        } else {
            return getString(t / 3600000) + ":" + getString((t % 3600000) / 60000) + ":" + getString((t % 60000) / 1000);
        }
    }

    private static String getString(int t) {
        String res = "";
        if (t > 0) {
            if (t < 10) {
                res = "0" + t;
            } else {
                res = t + "";
            }
        } else {
            res = "00";
        }
        return res;
    }
}
