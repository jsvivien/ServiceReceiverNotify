package com.example.tmh.servicereceivernotify;

public class Song {
    private int id;
    private String mNameSong;
    private String mPathSong;

    public Song() {
    }

    public Song(String mNameSong, String mPathSong) {
        this.mNameSong = mNameSong;
        this.mPathSong = mPathSong;
    }

    public Song(int id, String mNameSong, String mPathSong) {
        this.id = id;
        this.mNameSong = mNameSong;
        this.mPathSong = mPathSong;
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

    public String getmPathSong() {
        return mPathSong;
    }

    public void setmPathSong(String mPathSong) {
        this.mPathSong = mPathSong;
    }
}
