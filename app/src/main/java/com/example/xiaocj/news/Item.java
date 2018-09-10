package com.example.xiaocj.news;

import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item {

    private String title;
    private String url;
    private int imgId;
    private int newid;
    private String pubDate;
    private String author;
    private String description;
    private String imgUrl = null;

    private Pattern pattern = Pattern.compile("<img.*?src=\"(http.*?)\".*?>");

    public boolean isViewed = false;

    public Item(String title, int id){
        this.title = title;
        this.imgId = id;
    }

    public Item(String title, String url, int newid, String imgUrl){
        this.title = title;
        this.url = url;
        this.newid = newid;
        this.imgUrl = imgUrl;
        imgId = R.mipmap.ic_launcher;
    }

    public Item(String jsonString){
        try {
            JSONObject object = new JSONObject(jsonString);
            title =  object.getString("title");
            url =  object.getString("url");
            newid =  object.getInt("id");
            author = object.getString("author");
            description = object.getString("description");
            pubDate = object.getString("pubDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public int getNewid(){
        return newid;
    }

    public String getTitle(){
        return title;
    }

    public int getImgId(){
        return imgId;
    }

    public String getDescription(){
        return description;
    }

    public String getImgUrl(){
        if (imgUrl == null){
            if (description != null) {
                Matcher m = pattern.matcher(description);
                if (m.find())
                    imgUrl = m.group(1);
            }
        }
        return imgUrl;
    }

    public void setImgUrl(String u){
        imgUrl = u;
    }

    public String getPubDate(){
        return pubDate;
    }

    public String getAuthor(){
        return author;
    }

    public void addAttrs(String attrName, String attrValue){
        switch (attrName){
            case "title":
                title = attrValue;
                break;
            case "link":
                url = attrValue;
                break;
            case "author":
                author = attrValue;
                break;
            case "pubDate":
                pubDate = attrValue;
                break;
            case "description":
                description = attrValue;
                break;
        }
    }

    public String getUrl(){
        return url;
    }

    public String toFullString(){

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("url", url);
            jsonObject.put("id", newid);
            jsonObject.put("description", description);
            jsonObject.put("author", author);
            jsonObject.put("pubDate", pubDate);
            jsonObject.put("imgUrl", getImgUrl());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public String html(Item item){
        return  "<head><meta charset=\"UTF-8\"></head> <div> <p>" + item.getAuthor() + "</p><div>" + item.getPubDate() + "</div> </div> " + item.getDescription();
    }

    public String getShareContent(){
        return "title:" + title
                + "\nauthor:" + author
                + "\npubDate:" + pubDate
                + "\nurl:" + url;
    }
}
