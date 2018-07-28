package com.example.tmh.servicereceivernotify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<Song> {
    private Context mContext;
    private int mResource;
    private ArrayList<Song> mSongs;

    public SongAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Song> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mSongs = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song,parent,false);
            viewHoder = new ViewHoder();
            viewHoder.mTextNameSong = convertView.findViewById(R.id.text_itemNameSong);
            //lưu lại
            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }
        Song mSong = mSongs.get(position);
        viewHoder.mTextNameSong.setText(mSong.getmNameSong());
        return convertView;
    }
    private class ViewHoder{
        private TextView mTextNameSong;
    }
}
