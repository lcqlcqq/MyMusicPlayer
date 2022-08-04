package com.example.mymusicplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mymusicplayer.R;
public class SaveMenuFragment extends Fragment {

    private View root;
    private Button btn_cancel;

    private Button btn_save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.picture_longclick_menu,container,false);
        btn_save = root.findViewById(R.id.save);
        btn_cancel = root.findViewById(R.id.cancel);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
