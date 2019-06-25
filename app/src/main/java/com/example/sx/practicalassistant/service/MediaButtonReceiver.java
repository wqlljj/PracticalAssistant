package com.example.sx.practicalassistant.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.view.activity.VoiceActivity;
import com.example.sx.practicalassistant.view.fragment.HomeFragment;

import java.util.HashMap;

/**
 * Created by SX on 2017/3/6.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    //    private static MediaButtonReceiver mediaButtonReceiver;
    private static Context context;
    private static HashMap<String, OnMediaButtonListening> listenings;
    private static AudioManager mAudioManager;
    private static ComponentName mComponentName;
    private static String TAG = "MediaButtonReceiver";
    private static MediaSession session;

    public MediaButtonReceiver() {
    }

    public static void register(OnMediaButtonListening listening) {
        Log.e(TAG, "register: "+listening.getClass().getSimpleName() );
        if (mAudioManager == null) {
            MediaButtonReceiver.context = BaseApplication.getContext();
            listenings = new HashMap<>();
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mComponentName = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
            int result = mAudioManager.requestAudioFocus(
                    new MyOnAudioFocusChangeListener(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {
                //到这一步，焦点已经请求成功了
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    //注册媒体按键 API 21+（Android 5.0）
                    setMediaButtonEvent();
                } else {
                    //注册媒体按键 API 21 以下， 通常的做法
                    mAudioManager.registerMediaButtonEventReceiver(mComponentName);
                }
            }


        }
        addListening(listening);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    //注意申明 API 21
    private static void setMediaButtonEvent() {
        Log.e(TAG, "setMediaButtonEvent: " );
        session = new MediaSession(context, "随便写一串 tag 就行");
        session.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e(TAG, "onPlay: " );
                //这里处理播放器逻辑 播放
                // updatePlaybackState(true);
                // 播放暂停更新控制中心播放状态
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e(TAG, "onPause: " );
                //这里处理播放器逻辑 暂停
                // updatePlaybackState(false);
                // 播放暂停更新控制中心播放状态
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.e(TAG, "onSkipToNext: " );
                //CMD NEXT 这里处理播放器逻辑 下一曲
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e(TAG, "onSkipToPrevious: " );
                //这里处理播放器逻辑 上一曲
            }
        });
        session.setActive(true);
    }


    private static void addListening(OnMediaButtonListening listening) {
        listenings.put(listening.getClass().getSimpleName(), listening);
    }

    public static void unregister(OnMediaButtonListening listening) {
        listenings.remove(listening.getClass().getSimpleName());
        if (listenings.size() == 0) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                session.release();
            }else{
                mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
            }
//            context.unregisterReceiver(mediaButtonReceiver);
            context = null;
//            mediaButtonReceiver=null;
            listenings = null;
        }
    }

    long lastReceive = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TEST", "onReceive: 收到广播");
        boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()); //判断是不是耳机按键事件
        if (isActionMediaButton && System.currentTimeMillis() - lastReceive > 1000) {
            lastReceive = System.currentTimeMillis();
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); //判断有没有耳机按键事件
            int keyCode = event.getKeyCode();
            for (String s : listenings.keySet()) {
                listenings.get(s).clickHandle(event);
            }
            Log.e(TAG, "onReceive: " + keyCode + "  " + listenings.size());
        }
        abortBroadcast();

    }

    public interface OnMediaButtonListening {
        void clickHandle(KeyEvent event);
    }

    static class MyOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN" );
                    // 重新获得焦点, 可做恢复播放，恢复后台音量的操作
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS" );
                    // 永久丢失焦点除非重新主动获取，这种情况是
                    // 被其他播放器抢去了焦点， 为避免与其他播放器混音，
                    // 可将音乐暂停
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT" );
                    // 暂时丢失焦点，这种情况是被其他应用申请了
                    // 短暂的焦点，可压低后台音量
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.e(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK" );
                    // 短暂丢失焦点，这种情况是被其他应用申请了
                    // 短暂的焦点希望其他声音能压低音量（或者关闭声音）
                    // 凸显这个声音（比如短信提示音），
                    break;
            }
        }
    }


}
