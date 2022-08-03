package com.example.mymusicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.adapter.MusicListAdapter;

public class LocalMusicFragment extends Fragment {

    private View root;

    private RecyclerView recyclerView;

    private MusicListAdapter mAdapter;

    public LocalMusicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_local_music, container, false);
        if (recyclerView == null) {
            recyclerView = root.findViewById(R.id.rv_music);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new MusicListAdapter((MainActivity) this.getActivity());
            recyclerView.setAdapter(mAdapter);
        }
        return root;
    }
}