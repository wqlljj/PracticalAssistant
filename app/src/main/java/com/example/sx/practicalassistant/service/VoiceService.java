package com.example.sx.practicalassistant.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.bean.EventBean;
import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.view.activity.VideoActivity;
import com.example.sx.practicalassistant.view.activity.VoiceActivity;
import com.example.sx.practicalassistant.view.appwidget.VoiceWidget;

import java.io.IOException;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Created by wqlljj on 2017/3/3.
 */
public class VoiceService extends NotificationListenerService implements MediaButtonReceiver.OnMediaButtonListening {
    private String TAG ="VoiceService";
    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " );
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        MediaButtonReceiver.register(this);
        Log.e("test", "onCreate: 服务启动" );
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent=new Intent(getApplicationContext(), VoiceActivity.class);
        intent.putExtra("data","i'm just have a try");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 100,
                intent, 0);
        remoteView.setOnClickPendingIntent(R.id.image, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        appWidgetManager.updateAppWidget(
                new ComponentName(getApplicationContext(), VoiceWidget.class), remoteView);
    }

    @Subscribe(threadMode = ThreadMode.BackgroundThread )
    public void event(PlayBean bean){
        Log.e(TAG, "event: "+bean.getType() );
        switch (bean.getType()){
            case PlayBean.AUDIO_TYPE:
                HashMap<String, String> paths = bean.getPaths();
                if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                for (String s : paths.keySet()) {
                    playMusic(s);
                    break;
                }
                break;
            case PlayBean.VIDEO_TYPE:
                HashMap<String, String> ps= bean.getPaths();
                if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                for (String s : ps.keySet()) {
                    Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                    intent.putExtra("VideoPath",s);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
                break;
        }
    }
    boolean isPlaying=false;
    private void playMusic(String path) {
        Log.e(TAG, "playMusic: "+path );
        //播放内存卡中的音频文件
//        mediaPlayer=new MediaPlayer();
        mediaPlayer = MediaPlayer.create(this,  Uri.parse("file://"+path));
        //音频流的类型
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
        isPlaying=true;
        //设置音频的来源
//        try {
//            mediaPlayer.setDataSource(this, Uri.parse("file://"+path));
//            mediaPlayer.prepare();//准备一下
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPlaying=false;
                return false;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying=false;
            }
        });
        //设置准备完的监听
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //开始播放
                mediaPlayer.start();
                isPlaying=true;
                //点击播放改变图标
//                imageButton.setImageResource(android.R.drawable.ic_media_pause);
                //获取音乐的播放最大时间
//                int durtion=mediaPlayer.getDuration();
//                //设置进度条的最大值为音乐的播放最大时间
//                seekBar.setMax(durtion);
//                new Mythred().start();
            }
        });
//        //给进度条设置一个事件
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                //获取拖动的进度
//                int progress=seekBar.getProgress();
//                //到那个位置播放
//                mediaPlayer.seekTo(progress);
//            }
//        });
    }
//    class Mythrod extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            while (seekBar.getProgress()<=seekBar.getMax()){
//                //获取音乐的当前播放位置
//                int currentPosition=mediaPlayer.getCurrentPosition();
//                seekBar.setProgress(currentPosition);
//            }
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy:服务销毁 " );
        EventBus.getDefault().unregister(this);
        MediaButtonReceiver.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public void clickHandle(KeyEvent event) {
        Log.e(TAG, "clickHandle: " );
        if(mediaPlayer==null)return;
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }else if(isPlaying){
            mediaPlayer.start();
        }
    }
}
