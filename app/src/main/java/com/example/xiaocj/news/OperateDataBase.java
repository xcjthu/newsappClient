package com.example.xiaocj.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class OperateDataBase {

    Context context;
    MySqliteDataBase mdb;
    SQLiteDatabase db;
    MainActivity.MyHandler handler;
    static OperateDataBase instance = null;


    SearchActivity.MyHandler searchHandler = null;
    public void setSearchHandler(SearchActivity.MyHandler handler){
        searchHandler = handler;
    }


    MoreActivity.MyHandler moreHander = null;
    public void setMoreHander(MoreActivity.MyHandler moreHander){
        this.moreHander = moreHander;
    }


    static public OperateDataBase getInstance(){
        return instance;
    }

    public OperateDataBase(Context context, MainActivity.MyHandler handler){
        this.context = context;
        this.handler = handler;
        mdb = new MySqliteDataBase(context);
        instance = this;
    }

    public void insertBatchData(final List<Item> list, final String kind){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("database", "insert");
                db = mdb.getWritableDatabase();
                Log.d("insert", "getDone");
                db.beginTransaction();
                for (int i = 0; i < list.size(); ++ i){
                    // String sql = "insert into " + MySqliteDataBase.tableName + " (id, title, url, kind) Values (" + list.get(i).getNewid() + ", \"" + list.get(i).getTitle() + "\", \"" + list.get(i).getUrl() + "\", \"" + kind + "\");";
                    // db.execSQL(sql);
                    // Log.d("insert", sql);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", list.get(i).getNewid());
                    contentValues.put("title", list.get(i).getTitle());
                    contentValues.put("imgUrl", list.get(i).getImgUrl());
                    contentValues.put("url", list.get(i).getUrl());
                    contentValues.put("kind", kind);
                    contentValues.put("viewed", 0);
                    try{
                        db.insertOrThrow(MySqliteDataBase.tableName, null, contentValues);
                    }catch (SQLiteConstraintException e){
                        e.getStackTrace();
                    }

                }

                db.setTransactionSuccessful();
                db.endTransaction();

                db = mdb.getWritableDatabase();
                db.beginTransaction();
                for (int i = 0; i < list.size(); ++ i){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", list.get(i).getNewid());
                    contentValues.put("title", list.get(i).getTitle());
                    contentValues.put("imgUrl", list.get(i).getImgUrl());
                    try{
                        db.insertOrThrow(MySqliteDataBase.searchTableName, null, contentValues);
                    }catch (SQLiteConstraintException e){
                        e.getStackTrace();
                    }

                }
                db.setTransactionSuccessful();
                db.endTransaction();

            }
        }).start();

    }

    public void updateDescription(final Item item){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();

                ContentValues contentValues = new ContentValues();
                contentValues.put("description", item.getDescription());
                contentValues.put("author", item.getAuthor());
                contentValues.put("pubDate", item.getPubDate());
                Log.d("update", "description");
                db.update(MySqliteDataBase.tableName, contentValues, "id = ?", new String[]{item.getNewid() + ""});
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }).start();
    }

    public void updateViewed(final int id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();
                ContentValues contentValues = new ContentValues();
                contentValues.put("viewed", 1);
                Log.d("view", "" + id);
                db.update(MySqliteDataBase.tableName, contentValues, "id = ?", new String[]{"" + id});
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }).start();
    }

    public void selectBatchFromDataBase(final String kind){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();

                //String sql = "select * from news whre kind = \"" + kind + "\";" ;

                Cursor cursor;
                //if (kind == "收藏")
                //    cursor = db.query(MySqliteDataBase.tableName, new String[]{"id", "title", "url"}, "desciption is not ?", new String[]{"null"}, null, null, null);
                //else
                cursor = db.query(MySqliteDataBase.tableName, new String[]{"id", "title", "url", "imgUrl", "viewed"}, "kind = ?", new String[]{kind}, null, null, null);

                if (cursor.getCount() > 0){
                    Log.d("select from database", "num " + cursor.getCount());
                    List<Item> ans = new ArrayList<>();
                    while (cursor.moveToNext()){
                        Item tmp = new Item(cursor.getString(1), cursor.getString(2), cursor.getInt(0), cursor.getString(3));
                        tmp.isViewed = (cursor.getInt(4) == 1);
                        ans.add(tmp);
                    }
                    db.setTransactionSuccessful();

                    Message message = new Message();
                    message.what = 3;
                    switch (kind){
                        case "国内新闻":
                            message.arg1 = 0;
                            break;
                        case "国际新闻":
                            message.arg1 = 1;
                            break;
                        case "经济新闻":
                            message.arg1 = 2;
                            break;
                        case "体育新闻":
                            message.arg1 = 3;
                            break;
                        case "台湾新闻":
                            message.arg1 = 4;
                            break;
                        case "教育新闻":
                            message.arg1 = 5;
                            break;
                        case "游戏新闻":
                            message.arg1 = 6;
                            break;
                    }
                    message.obj = ans;
                    Log.d("database", "send message to MainActivity");
                    handler.sendMessage(message);
                }
                db.endTransaction();
            }
        }).start();
    }

    public void selectDescription(final int id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();
                Cursor cursor = db.query(MySqliteDataBase.tableName, new String[]{"author", "pubDate", "url", "title", "description", "imgUrl"}, "id = ?", new String[]{"" + id}, null, null, null);
                Item ans = null;
                if (cursor.getCount() > 0){
                    if (cursor.moveToNext()){
                        if (cursor.getString(4) != null) {
                            ans = new Item(cursor.getString(3), cursor.getString(2), id, cursor.getString(5));
                            ans.addAttrs("author", cursor.getString(0));
                            ans.addAttrs("pubDate", cursor.getString(1));
                            ans.addAttrs("description", cursor.getString(4));
                        }
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (ans != null){
                    Message message = new Message();
                    message.obj = 2 + ans.toFullString() + "$";
                    message.what = 4;
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }
                else{
                    Log.d("OperateDataBase", "select description error");
                    Message message = new Message();

                    message.what = 5;

                    message.arg1 = id;

                    Log.d("set message", "message.what = " + 5 + " message.arg1 = " + id);
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    public void search(final String words){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();
                Log.d("database", "search " + words);
                //Cursor cursor = db.query(MySqliteDataBase.searchTableName, new String[]{"title", "id", "imgUrl"}, "title match ?", new String[]{"*" + words + "*"}, null, null, null);
                // Cursor cursor = db.rawQuery("select title, id, imgUrl from search where title match " + "'*" + words + "*';", null);
                Cursor cursor = db.rawQuery("select title, id, imgUrl from " + MySqliteDataBase.searchTableName + " where title like \"%" + words + "%\";", null);
                if (cursor.getCount() >= 0){
                    Log.d("search", "get Count " + cursor.getCount());
                    List<Item> ans = new ArrayList<>();
                    while (cursor.moveToNext()){
                        Item tmp = new Item(cursor.getString(0), "", cursor.getInt(1), cursor.getString(2));
                        Log.d("title", cursor.getString(0));
                        ans.add(tmp);
                    }
                    //ans.add(new Item("fuck!", "", 5, "https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=3481253183,1931983666&fm=173&app=25&f=JPEG?w=639&h=446&s=672009E30CA20A8EEA98C0B303000091"));

                    Message message = new Message();
                    message.what = 1;
                    message.obj = ans;
                    searchHandler.sendMessage(message);
                }

                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }).start();
    }

    public void getViewed(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();
                Cursor cursor =  db.query(MySqliteDataBase.tableName, new String[]{"title", "id", "kind"}, "viewed = ? limit 20", new String[]{"1"}, null, null, null);
                JSONArray jsonArray = new JSONArray();
                if (cursor.getCount() >= 0){
                    while (cursor.moveToNext()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("title", cursor.getString(0));
                            jsonObject.put("id", cursor.getInt(1));
                            jsonObject.put("kind", cursor.getString(2));

                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    JSONObject ans = new JSONObject();
                    ans.put("type", "recommend");
                    ans.put("id", 0);
                    ans.put("data", jsonArray);
                    TCPClient.getInstance().send(ans.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }).start();
    }

    public void getLiked(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = mdb.getWritableDatabase();
                db.beginTransaction();
                Cursor cursor =  db.query(MySqliteDataBase.tableName, new String[]{"title", "id", "imgUrl", "url"}, "description is not null", new String[]{}, null, null, null);

                List<Item> ans = new ArrayList<>();
                if (cursor.getCount() >= 0){
                    while (cursor.moveToNext()) {
                        Item tmp = new Item(cursor.getString(0), cursor.getString(3), cursor.getInt(1), cursor.getString(2));
                        ans.add(tmp);
                    }
                }
                Message message = new Message();
                message.what = 1;
                message.obj = ans;
                moreHander.sendMessage(message);

                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }).start();
    }
}
