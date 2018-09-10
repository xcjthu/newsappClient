package com.example.xiaocj.news;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Channels {
    static public String[] titleString = {"国内新闻", "国际新闻", "经济新闻", "体育新闻", "台湾新闻", "教育新闻", "游戏新闻"};
    static public List<String> titles = new ArrayList<>();
    static public List<List<Item>> itemList = new ArrayList<>();
    static public List<Item> focusItemList = new ArrayList<>();
    static public Map<String, Integer> mapStringToInt = new HashMap<>();
    static public int focusPosition = 0;
    static public Item focus = null;

    static List<String> visibleTitle = new ArrayList<>();
    private Channels(){
        for (String s : titleString) {
            titles.add(s);
            itemList.add(new ArrayList<Item>());
            mapStringToInt.put(s, mapStringToInt.size());
            visibleTitle.add(s);
        }
    }

    static Channels channels = null;

    static public Channels getInstance(){
        if (channels == null)
            channels = new Channels();
        return channels;
    }



    static public void addData(String kind, List<Item> data){
        // itemList.get(mapStringToInt.get(kind)).addAll(0, data);
        addData(mapStringToInt.get(kind), data);
    }
    static public void addData(int type, List<Item> data){
        itemList.get(type).addAll(data);
        if (type == focusPosition)
            focusItemList.addAll(data);
        // OperateDataBase.getInstance().insertBatchData(data, titles.get(type));
    }

    static public void changeFocus(int position){
        int realPos = mapStringToInt.get(visibleTitle.get(position));
        if (realPos == focusPosition)
            return;
        changeFocus(visibleTitle.get(position));

    }
    static public void changeFocus(String kind){
        int position = mapStringToInt.get(kind);
        focusItemList.clear();
        focusItemList.addAll(itemList.get(position));
        focusPosition = position;
    }

    static public void changeVisiable(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            visibleTitle.clear();
            for (String title : titles) {
                if (jsonObject.getBoolean(title)){
                    visibleTitle.add(title);
                }
            }
            focusItemList.clear();
            focusItemList.addAll(itemList.get(mapStringToInt.get(visibleTitle.get(0))));
            focusPosition = mapStringToInt.get(visibleTitle.get(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
