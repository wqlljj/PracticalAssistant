package com.example.sx.practicalassistant.view.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.sx.practicalassistant.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.MemoryHandler;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private VideoView mVideoView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private String TAG = "VideoActivity";
    private String videoPath;
    private String playingVideoPath = "";
    private int position = 0;
    private View contentView;
    private SeekBar seekBar;
    private static final int UPDATE_PROGRESS = 1;
    private boolean seekBarChanging = false;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    if (!seekBarChanging) {
                        int currentPosition = mVideoView.getCurrentPosition();
                        Log.e(TAG, "handleMessage: " + currentPosition);
                        seekBar.setProgress(currentPosition);
                        playingTime.setText(formatTime(currentPosition));
                        time.setText(formatTime(mVideoView.getDuration()));
                        if(seekBar.getMax()!=mVideoView.getDuration()) {
                            seekBar.setMax(mVideoView.getDuration());
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    break;
            }
        }
    };
    private TextView playingTime;
    private TextView time;
    private CheckBox play_pause;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        contentView = findViewById(R.id.fullscreen_content);
        seekBar = ((SeekBar) findViewById(R.id.seekBar));
        seekBar.setOnSeekBarChangeListener(this);
        playingTime = ((TextView) findViewById(R.id.playingTime));
        time = ((TextView) findViewById(R.id.time));
        play_pause = ((CheckBox) findViewById(R.id.play_pause));
        play_pause.setOnCheckedChangeListener(this);
        title = ((TextView) findViewById(R.id.title));
        findViewById(R.id.back).setOnClickListener(this);

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.play_pause).setOnTouchListener(mDelayHideTouchListener);
        videoPath = getIntent().getStringExtra("VideoPath");
    }

    public void playVideo(final String path) {
        if (!new File(path).exists()) {
            Toast.makeText(this, "文件不存在：" + path, Toast.LENGTH_SHORT).show();
            return;
        }
        mVideoView.suspend();
        Log.e(TAG, "playVideo: " + path);
        mVideoView.setVideoURI(Uri.parse(path));
        playingVideoPath = path;
        title.setText(new File(path).getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        if (!TextUtils.isEmpty(videoPath) && !videoPath.equals(playingVideoPath)) {
            playVideo(videoPath);
        } else if (!TextUtils.isEmpty(videoPath)) {
            startVideo();
        }
    }

    private void startVideo() {
        if (mVideoView.isPlaying()) return;
        mVideoView.start();
        Log.e(TAG, "onResume: " + position);
        mVideoView.seekTo(position);
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            pauseVideo();
        }
    }

    private void pauseVideo() {
        position = mVideoView.getCurrentPosition();
        mVideoView.pause();
        mHandler.removeMessages(UPDATE_PROGRESS);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        play_pause.setChecked(true);
        Log.e(TAG, "onPrepared: ");
        if (!mVideoView.isPlaying()) {
            Log.e(TAG, "onPrepared: position = "+position );
            if (position != 0) {
                mVideoView.seekTo(position);
                mVideoView.start();
                mVideoView.pause();
            }
            mVideoView.start();
        } else mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "onInfo: "+what+"  "+extra );
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    if (position != 0) {
                        mp.seekTo(position);
                        mp.start();
                        mp.pause();
                    }
                    mVideoView.start();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion: ");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: " + what + "  " + extra);
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.suspend();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (!mVideoView.isPlaying()) {
                mVideoView.start();
            }
            Log.e(TAG, "onProgressChanged: " + progress + "  " + (progress - mVideoView.getCurrentPosition()) + "  " + mVideoView.getCurrentPosition());
            mVideoView.seekTo(progress);
            playingTime.setText(formatTime(progress));
            Log.e(TAG, "onProgressChanged: " + mVideoView.getCurrentPosition());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "onStartTrackingTouch: ");
        seekBarChanging = true;
        mHandler.removeMessages(UPDATE_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "onStopTrackingTouch: ");
        seekBarChanging = false;
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    public String formatTime(long time) {
        String standardTime;
        long seconds = time / 1000;
//        Log.e(TAG, "formatTime: duration = "+duration );
        if (mVideoView.getDuration() <= 0) {
            standardTime = "00:00";
        } else if (mVideoView.getDuration() < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (mVideoView.getDuration() < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.e(TAG, "onCheckedChanged: " + isChecked);
        switch (buttonView.getId()) {
            case R.id.play_pause:
                if (isChecked) {
                    startVideo();
                } else {
                    pauseVideo();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged: ");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState: ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
