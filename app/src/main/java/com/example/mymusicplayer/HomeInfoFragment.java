package com.example.mymusicplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mymusicplayer.R;

import org.jetbrains.annotations.NotNull;

public class HomeInfoFragment extends Fragment {

    private TextView tv_content;
    private View view;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_info, container, false);
        tv_content = view.findViewById(R.id.test);
        tv_content.setText("HomeInfoFragment...");
        return view;
    }
}