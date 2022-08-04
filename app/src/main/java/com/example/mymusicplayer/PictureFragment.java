package com.example.mymusicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mymusicplayer.bean.SelectPicPopupWindow;
import com.example.mymusicplayer.utils.PictureUtil;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.OutputStream;


public class PictureFragment extends Fragment {
    View root;
    private final static String URL = "http://api.iw233.cn/api.php?sort=random&type=json";
    public static String glide_url = "";
    public static String conn_url = "";
    private Button btn;
    private Button btn_webView;
    public static ImageView imgPic;
    private Bitmap bitmap;
    private long exitTime = 0;
    public SelectPicPopupWindow selectPicPopupWindow;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    imgPic.setImageBitmap(bitmap);
                    imgPic.setVisibility(View.VISIBLE);
                    Toast.makeText(root.getContext(), "图片加载完毕", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(root.getContext(), "图片加载异常", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }

        }
    };

    public PictureFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_picture, container, false);
        btn = (Button) root.findViewById(R.id.btn);
        imgPic = (ImageView) root.findViewById(R.id.imgPic);
        btn_webView = (Button) root.findViewById(R.id.btn_webView);

        imgPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("lcq", "长按。");
                //View popUpView = getLayoutInflater().inflate(R.layout.menu_main,null);
                ////创建悬浮窗
                //PopupWindow popupWindow = new PopupWindow(
                //        popUpView
                //        , ViewGroup.LayoutParams.WRAP_CONTENT
                //        , ViewGroup.LayoutParams.WRAP_CONTENT
                //        ,true
                //);
                selectPicPopupWindow = new SelectPicPopupWindow((Activity) root.getContext(), itemsOnClick);
                selectPicPopupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

                //popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);

                return true;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(PictureUtil.getJson(URL));
                            conn_url = jsonObject.getString("pic");
                            byte[] image = PictureUtil.getImage(PictureUtil.getFormatPath(conn_url));
                            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                            handler.sendEmptyMessage(1);
                        } catch (Exception e) {
                            handler.sendEmptyMessage(2);
                        }
                    }
                }).start();
            }
        });
        btn_webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(PictureUtil.getJson(URL));
                            glide_url = jsonObject.getString("pic");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("lcq", "url: " + PictureUtil.getFormatPath(glide_url));

                Glide.with(root.getContext()).load(PictureUtil.getFormatPath(glide_url).replaceAll("\\\\", "")).into(imgPic);

                imgPic.setVisibility(View.VISIBLE);
                //handler.sendEmptyMessage(1);
            }
        });
        return root;
    }

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            selectPicPopupWindow.dismiss();
            switch (v.getId()) {
                case R.id.save:
                    System.out.println("save");
                    if (imgPic.getDrawable() instanceof BitmapDrawable) {
                        Toast.makeText(root.getContext(), "成功保存至相册", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) root.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        } else
                            saveImage(bitmap);
                    }
                    break;
                case R.id.cancel:
                    break;
                default:
                    break;
            }
        }

    };

    private void saveImage(Bitmap toBitmap) {
        //开始一个新的进程执行保存图片的操作
        Uri insertUri = root.getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        //使用use可以自动关闭流
        try {
            OutputStream outputStream = root.getContext().getContentResolver().openOutputStream(insertUri, "rw");
            if (toBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                Log.e("保存成功", "success");
            } else {
                Log.e("保存失败", "fail");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}