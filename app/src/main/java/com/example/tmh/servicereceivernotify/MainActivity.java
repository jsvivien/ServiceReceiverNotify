package com.example.tmh.servicereceivernotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static ImageButton mButtonPrevious, mButtonPlay, mButtonNext;
    public static SeekBar mSeekbarTime;
    public static TextView mTimeStart, mTimeEnd, mTextNameSong;
    public static ImageView mImageSong;
    private Intent mPlayService;
    private MediaService mService;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.e(getClass().getName(), "onCreat");

        init();

        mButtonPrevious.setOnClickListener(this);
        mButtonPlay.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mSeekbarTime.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlayService = new Intent(MainActivity.this, MediaService.class);
        startService(mPlayService);
        bindService(mPlayService, mConnection, Context.BIND_AUTO_CREATE);
//        Log.e(getClass().getName(), "onStart: Bind to Service");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e(getClass().getName(), "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.e(getClass().getName(), "onPause");
    }

    private void init() {
        mButtonPrevious = (ImageButton) findViewById(R.id.button_previous);
        mButtonPlay = (ImageButton) findViewById(R.id.button_play);
        mButtonNext = (ImageButton) findViewById(R.id.button_next);
        mSeekbarTime = (SeekBar) findViewById(R.id.seekbar_time);
        mTimeStart = (TextView) findViewById(R.id.text_timeStart);
        mTimeEnd = (TextView) findViewById(R.id.text_timeEnd);
        mTextNameSong = (TextView) findViewById(R.id.text_nameSong);
        mImageSong = (ImageView) findViewById(R.id.anim_example);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Khi thoát ứng dụng - dừng Activity thì Unbind from the service
        if (mBound) {
//            Log.e(getClass().getName(), "onStop: UnBind from Service");
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!MediaService.mPlayer.isPlaying()) {
            stopService(mPlayService);
        }
//        Log.e(getClass().getName(), "onDestroy");
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaService.MediaBinder binder = (MediaService.MediaBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onClick(View view) {

    }
}
