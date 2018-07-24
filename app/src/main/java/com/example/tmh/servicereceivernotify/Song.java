package com.example.tmh.servicereceivernotify;

public class Song {
    private String mNameSong;
    private int mSong;
    private int id;

    public Song(int id, String mNameSong, int mSong) {
        this.id = id;
        this.mNameSong = mNameSong;
        this.mSong = mSong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getmNameSong() {
        return mNameSong;
    }

    public void setmNameSong(String mNameSong) {
        this.mNameSong = mNameSong;
    }

    public int getmSong() {
        return mSong;
    }

    public void setmSong(int mSong) {
        this.mSong = mSong;
    }
}
