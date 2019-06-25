package com.example.sx.practicalassistant.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.db.PlayBeanDB;
import com.example.sx.practicalassistant.utils.AudioUtils;
import com.example.sx.practicalassistant.utils.VoiceUtils;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ContactManager;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static android.content.ContentValues.TAG;

/**
 * Created by SX on 2017/2/28.
 */

public class BaseApplication extends Application implements AudioUtils.ScanListening {

    private SharedPreferences sharedPreferences;

    public static Context getContext() {
        return applicatin;
    }
    public static BaseApplication getBaseApplication() {
        return applicatin;
    }
    private static BaseApplication applicatin;
    @Override
    public void onCreate() {
        super.onCreate();
        applicatin=this;//585230a6   562de879  58b593b0
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58b593b0");
        //监听通讯录
//        VoiceUtils.getIntance(this).uploadcontact();
        getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);
        sharedPreferences = getSharedPreferences("practicalassistant", Context.MODE_PRIVATE);
        boolean b =true;
        try {
            b = sharedPreferences.getLong(PlayBean.AUDIO_TYPE, 0l) == 0;
        }catch (Exception e){
        }
        if(b){
            AudioUtils.scanFile("",PlayBean.AUDIO_TYPE,applicatin,this);
        }
        //bugly
        Context context = getApplicationContext();
// 获取当前包名
        String packageName = context.getPackageName();
// 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
// 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
// 初始化Bugly
        CrashReport.initCrashReport(context, "f7cf5993f8", true, strategy);
    }
//监听联系人数据的监听对象
    private static ContentObserver mObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            VoiceUtils.getIntance(applicatin).uploadcontact(null);
            }
        };

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
    @Override
    public void finsh(final String type, Long time) {
        sharedPreferences.edit().putLong(type,time).apply();
        new Thread(){
            @Override
            public void run() {
                super.run();
                getWord(type,null);
            }
        }.start();

        switch (type){
            case PlayBean.AUDIO_TYPE:
                boolean b =true;
                try {
                    b = sharedPreferences.getLong(PlayBean.VIDEO_TYPE, 0l) == 0;
                }catch (Exception e){
                }
                if(b){
                    AudioUtils.scanFile("",PlayBean.VIDEO_TYPE,applicatin,this);
                }
                break;
            case PlayBean.VIDEO_TYPE:
                break;
        }
    }

    public  void getWord(String type,LexiconListener lexiconListener) {
        Cursor cursor = PlayBeanDB.getIntance(applicatin).find(new String[]{"name", "path"}, "type = ?", new String[]{type});
        if((cursor==null||!cursor.moveToFirst())){
            if(lexiconListener!=null)
            lexiconListener.onLexiconUpdated("",null);
             Toast.makeText(getApplicationContext(),"词表为空",Toast.LENGTH_LONG).show();
            return;
        }
        HashSet<String> word=new HashSet<>();
        do{
            String name = cursor.getString(cursor.getColumnIndex("name"));
            name=name.substring(0,name.lastIndexOf("."));
            String[] split = name.split("(-|、|_| )");
            Log.e("TEST", "getWord: "+Arrays.toString(split) );
            for (String s : split) {
                word.add(s);
            }
        }while(cursor.moveToNext());
        word.remove("");
        HashMap<String, HashSet<String>> words = new HashMap<>();
        words.put(type,word);
        try {
            VoiceUtils.getIntance(applicatin).uploadWords(words,lexiconListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        cursor=null;
    }
}
