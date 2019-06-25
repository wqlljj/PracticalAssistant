package com.example.sx.practicalassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by wqlljj on 2017/3/11.
 */

public class WordsDB  {
    private static WordsDB wordsDB;
    private final SQLiteDatabase wdb;

    public WordsDB(Context context) {
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
        wdb = dbOpenHelper.getWritableDatabase();
    }
    public static WordsDB getIntance(Context context){
        if(wordsDB==null) {
            wordsDB = new WordsDB(context);
        }
        return wordsDB;
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
            if(!tabbleIsExist("word"))
                wdb.execSQL("create table word (id integer primary key autoincrement,name varchar(50),words varchar(100) UNIQUE)");
            wdb.insert("word", null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void update(ContentValues values,String where,String[] args){
        try {
            wdb.update("word", values, where, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void delete(String where,String[] args){
        try {
            wdb.delete("word", where, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Cursor find(String[] columns, String where, String[] args){
        Cursor query=null;
        try {
            query = wdb.query("word", columns, where, args, null, null, "name");
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
