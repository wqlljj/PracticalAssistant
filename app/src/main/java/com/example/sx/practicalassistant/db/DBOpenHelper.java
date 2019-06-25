package com.example.sx.practicalassistant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wqlljj on 2017/3/5.
 */

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(Context context) {
        super(context, "practicalassistant", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table play (id integer primary key autoincrement,name varchar(50),type varchar(8),path varchar(100) UNIQUE)");
        sqLiteDatabase.execSQL("create table word (id integer primary key autoincrement,name varchar(50),words varchar(100) UNIQUE)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//        sqLiteDatabase.execSQL("ALTER TABLE play RENAME TO playOld;");//先将表重命名
//        sqLiteDatabase.execSQL("DROP TABLE playOld");//删除旧表
//        sqLiteDatabase.execSQL("create table play (id integer primary key autoincrement,name varchar(50),type varchar(8),path varchar(100) UNIQUE)");//重新创建表
//        sqLiteDatabase.execSQL("INSERT INTO play (ID,Name,type,path) SELECT ID, Name,type,path FROM playOld;");//将旧表的内容插入到新表中
//        sqLiteDatabase.execSQL("create table word (id integer primary key autoincrement,name varchar(50),words varchar(100) UNIQUE)");

    }
}
