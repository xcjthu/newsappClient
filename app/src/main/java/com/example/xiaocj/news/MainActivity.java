package com.example.xiaocj.news;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import  android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    //private List<Item> list = new ArrayList<>();
    static public Context context;
    //private List<Item>[] tabItemlist;
    private JsonFormat jsonFormater = new JsonFormat();
    SwipeRefreshLayout swipeRefreshLayout = null;
    private OperateDataBase operateDataBase = null;

    //private Item focus = null;

    //private int tabPosition = 0;

    static public String[] titleList = {"国内新闻", "国际新闻", "经济新闻", "体育新闻", "台湾新闻", "教育新闻", "游戏新闻"};
    private TabLayout tabLayout;
    private ItemAdapter itemAdapter;

    private TCPClient tcpClient;
    //static public MyHandler myHandler;
    private MyHandler myHandler;

    private Toolbar toolbar = null;

    private void initPointer(){
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        myHandler = new MyHandler(handlerThread);

        operateDataBase = new OperateDataBase(this, myHandler);
        context = this;
        tabLayout = (TabLayout)findViewById(R.id.topTable);
        tcpClient = new TCPClient(myHandler);
        Log.d("init", "all pointer done");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Channels.getInstance();

        initPointer();

        new Thread(tcpClient).start();
        // moreData();
        initTopTable();
        initRefreshLayout();


        initList();
        Log.d("init", "list");

        initItemAdapter();
        Log.d("init", "itemAdapter");
        initRecyclerView();
        Log.d("init", "recyclerView");
        initToolbar();
    }

    private void initToolbar(){
        toolbar = (Toolbar)findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listview1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);
    }

    private void initItemAdapter(){
        itemAdapter = new ItemAdapter(Channels.focusItemList, MainActivity.this);
        itemAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                Toast.makeText(MainActivity.this, Channels.focusItemList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
                // list.get(position).isViewed = true;
                // focus = list.get(position);
                Channels.focus = Channels.focusItemList.get(position);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        operateDataBase.selectDescription(Channels.focusItemList.get(position).getNewid());
                    }
                }).start();


                /*
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                intent.putExtra("url", list.get(position).getUrl());
                startActivity(intent);
                */
            }
        });
    }

    private void initList(){

        //tabItemlist = new ArrayList[titleList.length];

        int index = 0;
        for (int j = 0; j < titleList.length; ++ j) {
            //tabItemlist[j] = new ArrayList<>();
            operateDataBase.selectBatchFromDataBase(titleList[j]);
        }
        //list = tabItemlist[0];
        //list.addAll(tabItemlist[0]);
    }

    private void initRefreshLayout(){
        swipeRefreshLayout = findViewById(R.id.freshlayout);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("Refresh", "Refresh to get more data");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moreData();

                    }
                }, 2000);
                // moreData();
            }
        });
    }

    private void initTopTable(){
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (int i = 0; i < Channels.visibleTitle.size(); ++ i){
            tabLayout.addTab(tabLayout.newTab().setText(Channels.visibleTitle.get(i)), i);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Channels.changeFocus(tab.getPosition());
                /*
                list.clear();
                list.addAll(tabItemlist[tab.getPosition()]);
                tabPosition = tab.getPosition();
                */
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.channelManage:
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.search:
                Intent intent1 = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent1);
                break;
        }
        return true;
    }

    private void addDataToManyList(String jsonString){
        List[] newData = jsonFormater.readMessageArray(jsonString.substring(1, jsonString.length()));

        for (int i = 0; i < newData.length; ++ i){
            Channels.addData(i, newData[i]);
            operateDataBase.insertBatchData(newData[i], Channels.titles.get(i));
            /*
            tabItemlist[i].addAll(0, newData[i]);
            operateDataBase.insertBatchData(newData[i], titleList[i]);
            */
        }
        // list.addAll(0, newData[tabPosition]);
    }

    private void addDataToOneList(String jsonString2){
        List newData2 = jsonFormater.readMessageOneType(jsonString2.substring(2, jsonString2.length()));
        int type = jsonString2.charAt(1) - '0';
        Channels.addData(type, newData2);
        operateDataBase.insertBatchData(newData2, Channels.titles.get(type));
        /*
        tabItemlist[type].addAll(0, newData2);
        operateDataBase.insertBatchData(newData2, titleList[type]);
        if (tabPosition == type)
            list.addAll(0, newData2);
        */
    }

    private void addOneTypeFromDataBase(int type, List object){
        Channels.addData(type, object);
        /*
        tabItemlist[type].addAll(object);
        if (type == tabPosition){
            list.addAll(object);
        }*/
    }

    private void lookDetailOfNews(String jsonString3, int like){
        try {
            JSONObject object = new JSONObject(jsonString3.substring(1, jsonString3.length()));
            //String imgUrl = null;
            //try{
            //    imgUrl = object.getString("imgUrl");
            //}catch (JSONException e){
            //    imgUrl = null;
            //}
            //Item tmp = new Item(object.getString("title"), object.getString("url"), object.getInt("id"), imgUrl);
            //tmp.addAttrs("description", object.getString("description"));
            //tmp.addAttrs("author", object.getString("author"));
            //tmp.addAttrs("pubDate", object.getString("pubDate"));

            // operateDataBase.updateDescription(tmp);
            if (Channels.focus != null)
                Channels.focus.isViewed = true;

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            // intent.putExtra("object", tmp.toFullString());
            intent.putExtra("object", object.toString());
            intent.putExtra("like", like);
            startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class MyHandler extends Handler{
        public MyHandler(HandlerThread handlerThread){
            super(handlerThread.getLooper());
        }
        @Override
        public void handleMessage(final Message msg){
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
    */
            System.out.println(System.currentTimeMillis());
            Log.d("handler message", "message.what = " + msg.what);
            switch (msg.what){
                case 1:
                    String jsonString = (String)msg.obj;
                    addDataToManyList(jsonString);
                    break;
                case 2:
                    String jsonString2 = (String)msg.obj;
                    addDataToOneList(jsonString2);
                    break;
                case 3:
                    Log.d("receive message ", "message.what = 3");
                    addOneTypeFromDataBase(msg.arg1, (List) msg.obj);
                    break;
                case 4:
                    String jsonString3 = (String)msg.obj;
                    lookDetailOfNews(jsonString3, msg.arg1);
                    break;
                case 5:
                    final int arg1 = msg.arg1;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("gg", "msg.arg1: " + arg1);
                            sendMessageToServer("description", arg1);
                        }
                    }).start();
                    break;
                case 6:
                    tcpClient = null;
                    //Looper.prepare();
                    //runOnUiThread(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        Toast.makeText(MainActivity.this, "could not connect the internet", Toast.LENGTH_SHORT);
                    //    }
                    //});
                    break;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UI", "UI would be refresh");
                    itemAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }
            });
            Log.d("handle message", "end");
            /*
                }
            }).start();
            */
        }
    }

    private void sendMessageToServer(String kind, int id){
        if (tcpClient == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Toast", "gg");
                    Toast.makeText(MainActivity.this, "could not connect the Internet", Toast.LENGTH_SHORT);
                }
            });

            return;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("type", kind);
            object.put("id", id);
            Log.d("send Message to Server", object.toString());
            tcpClient.send(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void moreData(){
        // sendMessageToServer("moreData");
        int id;
        if (Channels.focusItemList.size() == 0)
            id = -1;
        else
            id = Channels.focusItemList.get(0).getNewid();
        Log.d("data", "getMoreData");
        sendMessageToServer(Channels.titles.get(Channels.focusPosition), id);
        // tcpClient.send("moreData");
        // itemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            String result = data.getStringExtra("result");
            tabLayout.removeAllTabs();
            Channels.changeVisiable(result);
            initTopTable();

        }
    }
}

