package com.example.xiaocj.news;


import android.app.Notification;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonFormat {
    public List[] readMessageArray(String jsonString){
        /** for main activity
         *  parse the string to itemList
         */
        Log.d("Json", jsonString);
        try {
            JSONArray array = new JSONArray(jsonString);

            List<Item>[] ans = new ArrayList[MainActivity.titleList.length];
            for (int j = 0; j < MainActivity.titleList.length; ++ j) {
                ans[j] = new ArrayList<>();

                JSONArray array1 = array.getJSONArray(j);

                for (int i = 0; i < array1.length(); ++i) {
                    JSONObject object = array1.getJSONObject(i);
                    String imgUrl = null;
                    try {
                        imgUrl = object.getString("imgUrl");
                    } catch (JSONException e){
                        imgUrl = null;
                    }
                    Item tmp = new Item(object.getString("title"), object.getString("url"), object.getInt("id"), imgUrl);
                    Log.d("title: ", object.getString("title"));
                    Log.d("url: ", object.getString("url"));
                    ans[j].add(tmp);
                }
            }
            return ans;
        } catch (JSONException e) {
            Log.d("JSON", "error");
            e.printStackTrace();
            return null;
        }
    }

    public List<Item> readMessageOneType(String jsonString){
        try {
            JSONArray array = new JSONArray(jsonString);
            List<Item> ans = new ArrayList<>();
            for (int i = 0; i < array.length(); ++ i){
                JSONObject object = array.getJSONObject(i);
                String imgUrl = null;
                try{
                    imgUrl = object.getString("imgUrl");
                } catch (JSONException e){
                    imgUrl = null;
                }
                Item tmp = new Item(object.getString("title"), object.getString("url"), object.getInt("id"), imgUrl);
                ans.add(tmp);
            }
            return ans;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }


}
