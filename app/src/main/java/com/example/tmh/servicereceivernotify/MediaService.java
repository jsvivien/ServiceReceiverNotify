package com.example.tmh.servicereceivernotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnErrorListener {
    private WeakReference<TextView> mTextNameSong,
            mTimeTotal, mTimeProgress;
    private WeakReference<ImageView> mImageSong;
    private WeakReference<ImageButton> mButtonPlay, mButtonPrevious, mButtonNext;
    private WeakReference<SeekBar> mSeekbarTime;
    private WeakReference<ListView> mListViewSong;
    public static MediaPlayer mPlayer;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean isPausedInCall = false;
    private final MediaBinder mBinder = new MediaBinder();
    private ArrayList<Song> mSongs;
    private int position = 0;
    private final static String ACTION_PLAY = "ACTION_PLAY";
    private final static String ACTION_PAUSE = "ACTION_PAUSE";
    private final static String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private final static String ACTION_NEXT = "ACTION_NEXT";
    private final static String ACTION_CLOSE = "ACTION_CLOSE";
    private final static int ID_NOTIFY = 1;
    private final static int DELAY_MILLIS = 100;
    private Notification notification;
    private NotificationManager notificationManager;

    public MediaService() {
    }

    @Override
    public void onCreate() {
//        Log.e(getClass().getName(), "onCreat");
        mSongs = new ArrayList<>();
        mSongs = getmSongs(getApplicationContext());
        if (mSongs.size() > 0) {
            Uri uri = Uri.parse(mSongs.get(position).getmPathSong());
            mPlayer = MediaPlayer.create(getApplicationContext(), uri);
        }

        super.onCreate();
    }

    //Lấy list nhạc từ bộ nhớ thiết bị
    private ArrayList<Song> getmSongs(Context context) {
        ArrayList<Song> mSongs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor songCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null, null);
        int songColumn;
        Song song;
        if (songCursor != null && songCursor.getCount() > 0) {
            for (songCursor.moveToFirst(); !songCursor.isAfterLast(); songCursor.moveToNext()) {
                song = new Song();
                song.setId(songCursor.getInt(songCursor.getColumnIndex("_id")));

                songColumn = songCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
                song.setmNameSong(songCursor.getString(songColumn));

                songColumn = songCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
                song.setmPathSong(songCursor.getString(songColumn));

                mSongs.add(song);
            }
        }
        return mSongs;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(getClass().getName(), "onStartCommand");
        init();
        if (MediaService.mPlayer != null) {
            mTextNameSong.get().setText(mSongs.get(position).getmNameSong());
            if (MediaService.mPlayer.isPlaying()) {
                mButtonPlay.get().setImageResource(R.drawable.ic_pause);
                startAnimation();
            } else {
                mButtonPlay.get().setImageResource(R.drawable.ic_play);
                stopAnimation();
            }
        }
        //Custom ACTION
        handleIntent(intent);
        //Quản lý cuộc gọi
        phoneManager();

        return super.onStartCommand(intent, flags, startId);
    }

    //Hàm quản lý cuộc gọi
    private void phoneManager() {
        //Nếu có cuộc gọi đến, tạm dừng máy nge nhạc, và resume khi ngắt kết nối cuộc gọi.
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                // String stateString = "N/A";
                // Log.v(TAG, "Starting CallStateChange");
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING://có cuộc gọi đến
                        if (mPlayer != null) {
                            if (mPlayer.isPlaying()) {
                                mPlayer.pause();
                                isPausedInCall = true;
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE://kết thúc cuộc gọi
                        //Bắt đầu play nhac.
                        if (mPlayer != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                if (!mPlayer.isPlaying())
                                    mPlayer.start();
                            }
                        }
                        break;
                }
            }
        };
        //Đăng ký lắng nge từ việc quản lý điện thoại
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.e(getClass().getName(), "Service onBind");
        return mBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaNext();
//        Log.e(getClass().getName(), "onCompletion");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play:
                mediaPlay();
                break;
            case R.id.button_previous:
                mediaPrevious();
                break;
            case R.id.button_next:
                mediaNext();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPlayer != null) {
            MediaService.mPlayer.seekTo(mSeekbarTime.get().getProgress());
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mPlayer.reset();
//        Log.e(getClass().getName(), "onError");
        return false;
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MediaBinder extends Binder {
        MediaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaService.this;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        creatSong();
        if (mPlayer != null) {
            updateTime();
        }
    }

    private void updateTime() {
        final Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //định dạng time
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                //Hiển thị tổng thời gian
                mTimeTotal.get().setText(format.format(mPlayer.getDuration()));
                //Gán max cho Seekbar
                mSeekbarTime.get().setMax(mPlayer.getDuration());
                //Hiển thị thời gian hoàn thành
                mTimeProgress.get().setText(format.format(mPlayer.getCurrentPosition()));
                //Thực hiện việc cập nhật progress bar
                mSeekbarTime.get().setProgress(mPlayer.getCurrentPosition());
                //Chạy lại sau 0,1s
                mHandler.postDelayed(this, DELAY_MILLIS);
            }
        }, DELAY_MILLIS);
    }

    //Hàm Custom Action
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case ACTION_PLAY:
                playMediaFromNotify();
                break;
            case ACTION_PAUSE:
                pauseMediaFromNotify();
                break;
            case ACTION_NEXT:
                mediaNext();
                break;
            case ACTION_PREVIOUS:
                mediaPrevious();
                break;
            case ACTION_CLOSE:
                stopForeground(true);
                break;
        }
    }

    public void creatNotify(String action) {
        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
//        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext()
                , 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        //set Intent
        Intent playpauseIntent = new Intent(this, MediaService.class);
        Intent nextIntent = new Intent(this, MediaService.class);
        Intent previousIntent = new Intent(this, MediaService.class);
        Intent closeIntent = new Intent(this, MediaService.class);

        playpauseIntent.setAction(action);
        nextIntent.setAction(ACTION_NEXT);
        previousIntent.setAction(ACTION_PREVIOUS);
        closeIntent.setAction(ACTION_CLOSE);

        PendingIntent playpausePendingIntent = PendingIntent.getService(this, 0,
                playpauseIntent, 0);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        //setOnClickPendingIntent
        RemoteViews layoutNotify = new RemoteViews(getPackageName(), R.layout.notify_play);
        if (action == ACTION_PLAY) {
            layoutNotify = new RemoteViews(getPackageName(), R.layout.notify_play);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_play, playpausePendingIntent);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_previous, previousPendingIntent);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_next, nextPendingIntent);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_close, closePendingIntent);
            layoutNotify.setTextViewText(R.id.text_notify_nameSong, mSongs.get(position).getmNameSong());
        } else if (action == ACTION_PAUSE) {
            layoutNotify = new RemoteViews(getPackageName(), R.layout.notify_pause);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_pause, playpausePendingIntent);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_previous, previousPendingIntent);
            layoutNotify.setOnClickPendingIntent(R.id.button_notify_next, nextPendingIntent);
            layoutNotify.setTextViewText(R.id.text_notify_nameSong, mSongs.get(position).getmNameSong());
        }

        //Khởi tạo Notify
        notification = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_cd_80)
                .setContentIntent(pendingIntent)
                .setContent(layoutNotify)
                .build();

        startForeground(ID_NOTIFY, notification);
        //Đối tượng quản lý Notification
        notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        //Hiển thị Notify
        notificationManager.notify(ID_NOTIFY, notification);
    }

    private void init() {
        mButtonPlay = new WeakReference<ImageButton>(MainActivity.mButtonPlay);
        mButtonPrevious = new WeakReference<ImageButton>(MainActivity.mButtonPrevious);
        mButtonNext = new WeakReference<ImageButton>(MainActivity.mButtonNext);
        mSeekbarTime = new WeakReference<SeekBar>(MainActivity.mSeekbarTime);
        mTextNameSong = new WeakReference<TextView>(MainActivity.mTextNameSong);
        mTimeProgress = new WeakReference<TextView>(MainActivity.mTimeStart);
        mTimeTotal = new WeakReference<TextView>(MainActivity.mTimeEnd);
        mImageSong = new WeakReference<ImageView>(MainActivity.mImageSong);
        //set recyclerview
        mListViewSong = new WeakReference<ListView>(MainActivity.mListViewSong);
        SongAdapter mAdapterSong = new SongAdapter(getApplicationContext(), R.layout.item_song, mSongs);
        mListViewSong.get().setAdapter(mAdapterSong);

        mAdapterSong.notifyDataSetChanged();
        //click item Listview
        mListViewSong.get().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final View mView = view;
//                Song mSong = (Song) adapterView.getItemAtPosition(i);
                playMediabyId(i);
                Handler mHandle = new Handler();
                mHandle.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mView.setBackgroundColor(Color.TRANSPARENT);
                    }
                }, 500);
                mView.setBackgroundColor(getResources().getColor(R.color.color_red));
            }
        });
        //
        if (mPlayer != null) {
            mButtonPlay.get().setOnClickListener(this);
            mButtonPrevious.get().setOnClickListener(this);
            mButtonNext.get().setOnClickListener(this);
            mSeekbarTime.get().setOnSeekBarChangeListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
        }

    }

    private void playMediabyId(int id) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
        }
        position = id;
        creatSong();
        mPlayer.start();
        updateTime();
        creatNotify(ACTION_PAUSE);
        mButtonPlay.get().setImageResource(R.drawable.ic_pause);
        startAnimation();
    }

    private void pauseMediaFromNotify() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mButtonPlay.get().setImageResource(R.drawable.ic_play);
            creatNotify(ACTION_PLAY);
            updateTime();
            stopAnimation();
        }
    }

    private void playMediaFromNotify() {
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            mButtonPlay.get().setImageResource(R.drawable.ic_pause);
            creatNotify(ACTION_PAUSE);
            startAnimation();
            updateTime();
        }
    }

    private void mediaPlay() {
        if (mPlayer != null) {
            mTextNameSong.get().setText(mSongs.get(position).getmNameSong());
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mButtonPlay.get().setImageResource(R.drawable.ic_play);
                creatNotify(ACTION_PLAY);
                stopForeground(true);
                stopAnimation();
            } else {
                mPlayer.start();
                mButtonPlay.get().setImageResource(R.drawable.ic_pause);
                creatNotify(ACTION_PAUSE);
                startAnimation();
            }
            updateTime();
        }

    }

    private void mediaPrevious() {
        if (mPlayer != null) {
            position--;
            if (position < 0) {
                position = mSongs.size() - 1;
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            creatSong();
            mPlayer.start();
            updateTime();
            creatNotify(ACTION_PAUSE);
            mButtonPlay.get().setImageResource(R.drawable.ic_pause);
            startAnimation();
        }
    }

    private void mediaNext() {
        if (mPlayer != null) {
            position++;
            if (position > mSongs.size() - 1) {
                position = 0;
            }
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            creatSong();
            mPlayer.start();
            updateTime();
            creatNotify(ACTION_PAUSE);
            mButtonPlay.get().setImageResource(R.drawable.ic_pause);
            startAnimation();
        }
    }

    private void creatSong() {
        if (mSongs.size() > 0) {
            Uri uri = Uri.parse(mSongs.get(position).getmPathSong());
            mPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mTextNameSong.get().setText(mSongs.get(position).getmNameSong());
        }
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.e(getClass().getName(), "Destroy Service");
    }

    //Animation
    private void startAnimation() {
        mImageSong.get().setImageDrawable(getResources().getDrawable(R.drawable.image_circle_tmh));
        Animation rotateLoading = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        mImageSong.get().clearAnimation();
        mImageSong.get().setAnimation(rotateLoading);
    }

    private void stopAnimation() {
        if (mImageSong.get().getAnimation() != null) {
            mImageSong.get().clearAnimation();
            mImageSong.get().setImageDrawable(getResources().getDrawable(R.drawable.image_circle_tmh));
        }
    }

}
