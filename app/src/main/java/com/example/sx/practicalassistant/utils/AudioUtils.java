package com.example.sx.practicalassistant.utils;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.example.sx.practicalassistant.app.BaseApplication;
import com.example.sx.practicalassistant.bean.EventBean;
import com.example.sx.practicalassistant.bean.PlayBean;
import com.example.sx.practicalassistant.db.PlayBeanDB;

import java.io.File;
import java.io.FileFilter;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import de.greenrobot.event.EventBus;

/**
 * Created by wqlljj on 2017/3/5.
 */

public class AudioUtils {
    private static ExecutorService executorService=Executors.newFixedThreadPool(4);
//    private static ExecutorService executorService=Executors.newCachedThreadPool();
    private static Context context;
    private static ScanListening scanListening;
    private static HashSet<Long> work=new HashSet<>();
    private static ArrayList<ContentValues> mp3s=new ArrayList<>();
    public static int index=0;
    public static void scanFile(final String fileName,final String type, Context context,ScanListening scanListening){
        AudioUtils.context = context;
        AudioUtils.scanListening = scanListening;
        final String path= Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(Environment.getExternalStorageDirectory().getParentFile().getParentFile() + "/" + "sdcard1");
        final String sdPath= file.exists()?file.getAbsolutePath():path;
        Log.e(TAG, "scanFile: "+sdPath );
        index++;
        mp3s.clear();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (Object.class) {
                    work.add(Thread.currentThread().getId());
                }
                searchFile(TextUtils.isEmpty(fileName)?".":fileName, path, type,index);
                if(!TextUtils.isEmpty(sdPath)){
                    Log.e(TAG, "scanFile: sdPath="+sdPath );
                    searchFile(TextUtils.isEmpty(fileName)?".":fileName, sdPath, type,index);
                }
                synchronized (Object.class) {
                    work.remove(Thread.currentThread().getId());
                }
                check(type,index-1);
            }
        });
    }
    public  interface ScanListening{
        void finsh(String type,Long time);
    }
    public static void play(PlayBean playBean,Context context){
        Log.e(TAG, "play: "+playBean );
        EventBus.getDefault().post(playBean);
//        if(AudioUtils.context==null){
//            AudioUtils.context=context;
//        }
//        Set<String> paths;
//        switch (playBean.getType()){
//            case PlayBean.AUDIO_TYPE:
//                 paths = playBean.getPaths().keySet();
//                for (String path : paths) {
//                    if(new File(path).exists())
//                    player("file://"+path , playBean.getType());
//                    else {
//                        PlayBeanDB.getIntance(context).delete("path = ?",new String[]{path});
//                        continue;
//                    }
//                    break;
//                }
//                break;
//            case PlayBean.VIDEO_TYPE:
//                paths = playBean.getPaths().keySet();
//                for (String path : paths) {
//                    if(new File(path).exists())
//                    player("file://"+path , playBean.getType());
//                    else {
//                        PlayBeanDB.getIntance(context).delete("path = ?",new String[]{path});
//                        continue;
//                    }
//                    break;
//                }
//                 break;
//        }
    }
    public static Uri getUriForFile(Context context, String path) {
        return FileProvider.getUriForFile(context, "com.example.sx.practicalassistant.fileProvider", new File(path));
    }
    private static void player(String path, String type) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getUriForFile(context,path), type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
    private static void searchFile(final String filename, String path, final String type,final int index) {
        try {
            new File(path).listFiles(new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    if(index!=AudioUtils.index){
                        Log.e(TAG, "accept: 提前结束"+index+"次搜索" );
                        return false;
                    }
                    if (file.isDirectory()) {
                        String[] split = file.getAbsolutePath().split("/");
                        if(split.length>6|| split[split.length-1].startsWith(".")|| split[split.length-1].startsWith("_")){
                            return false;
                        }
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (Object.class) {
                                    work.add(Thread.currentThread().getId());
                                }
                                searchFile(filename, file.getAbsolutePath(), type,index);
                                synchronized (Object.class) {
                                    work.remove(Thread.currentThread().getId());
                                }
                                check(type,index);
                            }
                        });
                    } else {
                        Log.e(TAG, "accept: "+file.getAbsolutePath() );
                        String mimeType = getMimeType(file.getName());
                        if (file.getName().contains(filename) &&(mimeType==null?false:mimeType.matches(type))) {
                            Log.e("TES", "searchFile: " + file.getPath());
                            ContentValues values = new ContentValues();
                            values.put("name", file.getName());
                            values.put("type", type);
                            values.put("path", file.getAbsolutePath());
                            mp3s.add(values);
                            if(!filename.equals(".")){
                                PlayBean playBean = new PlayBean(type);
                                playBean.addPath(file.getAbsolutePath(),filename);
//                                play(playBean,context);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e){
            return;
        }
    }
    private static void check(String type,int index){
        if(work.size()==0&&AudioUtils.index==index){
            Log.e(TAG, "check: 扫描完成" );
            PlayBeanDB intance = PlayBeanDB.getIntance(context);
//            Cursor cur = intance.find(new String[]{"name", "path"}, "type = ?", new String[]{mp3s.get(0).getAsString("type")});
//            if(cur.moveToFirst()){
//                do{
//                    String name = cur.getString(cur.getColumnIndex("name"));
//                    String type1 = cur.getString(cur.getColumnIndex("type"));
//                    String path = cur.getString(cur.getColumnIndex("path"));
//                    Log.e(TAG, "查询: "+name+"  "+ type1+"  "+path);
//                }while (cur.moveToNext());
//            }
            for (ContentValues values : mp3s) {
                Cursor cursor = intance.find(new String[]{"name"}, "path = ?", new String[]{values.getAsString("path")});
                if(!cursor.moveToFirst()){
                    Log.e(TAG, "查询: "+values.get("name")+"  "+ values.get("type")+"  "+values.get("path"));
                    intance.insert(values);
                }else{
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    Log.e(TAG, "重复: "+index+"   "+name );
                }
                cursor.close();
            }
            Log.e(TAG, "check: 扫描完成1" );
            scanListening.finsh(type,System.currentTimeMillis());
        }else{
            //Log.e("TEST", "work.size(): "+work.size() );
        }
    }
    private static String TAG="TEST";
    private static String getMimeType(String fileName) {
//        Log.e(TAG, "getMimeType: 1" );
        FileNameMap fileNameMap= URLConnection.getFileNameMap();
//        Log.e(TAG, "getMimeType: 2" );
        String type=fileNameMap.getContentTypeFor(fileName);
//        Log.e(TAG, "getMimeType: 3" +type);
        if(isAudio(type)){
            type=PlayBean.AUDIO_TYPE;
        }else if(isVideo(type)){
            type=PlayBean.VIDEO_TYPE;
        }else if(isImage(type)){
            type="image/";
        }
//        Log.e(TAG, "getMimeType: 4" );
        return type;
        }
    private static boolean isAudio(String mMimeType) {
        return (mMimeType != null && mMimeType.startsWith("audio/"));
    }
    private static boolean isVideo(String mMimeType) {
        return (mMimeType != null && mMimeType.startsWith("video/"));
        }

    private static boolean isImage(String mMimeType) {
        return ((mMimeType != null && mMimeType.startsWith("image/")));
            }

}
