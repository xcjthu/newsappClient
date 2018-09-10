package com.example.xiaocj.news;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySqliteDataBase extends SQLiteOpenHelper{
    private static final int dbVersion = 1;
    private static final String dbName = "goodData.db";
    public static final String tableName = "news";
    public static final String searchTableName = "search";

    public MySqliteDataBase (Context context){
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){

        Log.d("use", "sqlDatabase");
        String sql = "create table if not exists " + tableName + "(id int not null unique primary key, kind text not null, title text not null, url text not null, viewed int , imgUrl text, description text, pubDate text, author text)";
        sqLiteDatabase.execSQL(sql);
        String sql0 = "create virtual table if not exists " + searchTableName + " using fts3(title, id, imgUrl);";
        sqLiteDatabase.execSQL(sql0);
        Log.d("create database", "virtual table");
        String sql1 = "create index kind on " + tableName + "(kind)";
        sqLiteDatabase.execSQL(sql);
        System.out.println("create database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        String sql = "DROP TABLE IF EXISTS " + tableName;
        String sql0 = "DROP TABLE IF EXISTS " + searchTableName;
        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(sql0);
        onCreate(sqLiteDatabase);
    }

}
