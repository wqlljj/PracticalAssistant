package com.example.sx.practicalassistant.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wqlljj on 2016/11/30.
 */

public class WordsDBUtils {
    private static SQLiteDatabase sqLiteDatabase;
    public static String getTranscription(Context context,String word) {
        AssetManager assets = context.getResources().getAssets();
        String packageResourcePath = context.getCacheDir().getAbsolutePath();
        File file = new File(packageResourcePath, "chinese.db");
        if (!file.exists()){
            Toast.makeText(context,"文件不存在",Toast.LENGTH_LONG).show();
            try {
                InputStream open = assets.open("chinese.db");
                FileOutputStream out=new FileOutputStream(file);
                byte[] b=new byte[2048];
                int len=-1;
                while((len=open.read(b))!=-1){
                    out.write(b,0,len);
                }
                out.close();
                open.close();
                Toast.makeText(context,"完成",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("TEST", "onCreate: "+e.getMessage());
            }
        }
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file,null);
        Cursor chinese = sqLiteDatabase.query("chinese", new String[]{"json"}, "word=?", new String[]{word}, null, null, null);
        if(chinese.moveToFirst()){
            String data = chinese.getString(chinese.getColumnIndex("json"));
            Log.e("TEST", "query: data="+data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                chinese.close();
                return jsonObject.getString("transcription");
            } catch (JSONException e) {
                e.printStackTrace();
                chinese.close();
                return "";
            }
        }
        chinese.close();
        return null;
    }
}
