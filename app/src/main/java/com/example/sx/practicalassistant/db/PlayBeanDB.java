package com.example.sx.practicalassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by wqlljj on 2017/3/5.
 */

public class PlayBeanDB {
    private static PlayBeanDB playBeanDB;
    private final SQLiteDatabase wdb;

    public PlayBeanDB(Context context) {
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
        wdb = dbOpenHelper.getWritableDatabase();
    }
    public static PlayBeanDB getIntance(Context context){
        if(playBeanDB==null) {
            playBeanDB = new PlayBeanDB(context);
        }
        return playBeanDB;
    }
    public boolean tabbleIsExist(String tableName){
        boolean result = false;
        if(tableName == null){
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"+tableName.trim()+"' ";
            cursor = wdb.rawQuery(sql, null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                if(count>0){
                    result = true;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }
    public  void insert(ContentValues values){
        try {
            if(!tabbleIsExist("play"))
                wdb.execSQL("create table play (id integer primary key autoincrement,name varchar(50),type varchar(8),path varchar(100) UNIQUE)");
            wdb.insert("play", null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void update(ContentValues values,String where,String[] args){
        try {
            wdb.update("play", values, where, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void delete(String where,String[] args){
        try {
            wdb.delete("play", where, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Cursor find(String[] columns,String where,String[] args){
        Cursor query=null;
        try {
             query = wdb.query("play", columns, where, args, null, null, "name");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return query;
    }
    public void destory(){
        wdb.close();
    }
}
