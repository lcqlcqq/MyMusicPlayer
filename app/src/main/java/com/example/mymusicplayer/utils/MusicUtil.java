package com.example.mymusicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.R;
import com.example.mymusicplayer.bean.Song;

import java.io.File;
import java.util.ArrayList;

public class MusicUtil {

    /**
     * 从sdcard\Music\目录读取
     *
     * @param context
     * @return
     */
    public static ArrayList<Song> readMusicSongs(Context context) {
        //final ArrayList<File> songs = getMusicList(Environment.getExternalStorageDirectory());

        ArrayList<Song> musicList = new ArrayList<>();
        //System.out.println(Environment.getExternalStorageDirectory().getPath());Uri.parse(Environment.getExternalStorageDirectory().getPath())
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_MUSIC,
                null, MediaStore.Audio.Media.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                if (song.size > 1000 * 800) {
                    //切割标题，分出歌曲名和歌手
                    if (song.song.contains("-")) {
                        String[] str = song.song.split("-");
                        song.singer = str[0];
                        song.song = str[1];
                    }
                    musicList.add(song);
                }
            }
            cursor.close();
        }
        return musicList;
    }

    /**
     * 获取专辑图
     * @param context 上下文
     * @param path    歌曲路径
     * @param type    1 Activity中显示  2 通知栏中显示  3 详细页大图
     * @return
     */
    public static Bitmap getAlbumPicture(Context context, String path, int type) {
        Log.e("lcq", "album_path: " + path);
        //歌曲检索
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            //设置数据源
            mmr.setDataSource(path);
        } catch (Exception e) {
            Toast.makeText(context, "读取文件异常", Toast.LENGTH_SHORT).show();
        }
        //获取图片数据
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture = null;
        if (data != null) {
            //获取bitmap对象
            albumPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (type == 3)  // 详细页大图
                return albumPicture;
            // 获取宽高
            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            // 创建操作图片用的Matrix对象
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // 设置缩放比例
            matrix.postScale(sx, sy);
            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);
        } else {
            // 从歌曲文件读取不出来专辑图片时用来代替的默认专辑图片
            if(context == null) return null;
            if (type == 1) {
                //Activity中显示
                albumPicture = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_music);
            } else if (type == 2) {
                //通知栏显示
                albumPicture = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_notification_default);
            }else if (type == 3){
                return BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_notification_default);
            }

            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            // 创建操作图片用的Matrix对象
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // 设置缩放比例
            matrix.postScale(sx, sy);
            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);

        }
        return albumPicture;
    }
}