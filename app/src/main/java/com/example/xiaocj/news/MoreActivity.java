package com.example.xiaocj.news;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MoreActivity extends Activity {

    List<Item> datalist;

    List<Item> collection;
    List<Item> recommend;

    MoreAdapter dataAdapter;

    TabLayout tabLayout;
    MyHandler handler;

    int tabposition = 0;


    private void initTabLayout(){
        tabLayout = (TabLayout)findViewById(R.id.moreTopTable);
        tabLayout.addTab(tabLayout.newTab().setText("收藏"), 0);
        tabLayout.addTab(tabLayout.newTab().setText("推荐阅读"), 1);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabposition = tab.getPosition();
                datalist.clear();
                if (tab.getPosition() == 0)
                    datalist.addAll(collection);
                else
                    datalist.addAll(recommend);
                Log.d("tab", "change to " + tab.getPosition());
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void initList(){
        datalist = new ArrayList<>();
        collection = new ArrayList<>();
        recommend = new ArrayList<>();

        OperateDataBase.getInstance().getViewed();
        OperateDataBase.getInstance().getLiked();
    }

    private void initAdapter(){
        dataAdapter = new MoreAdapter(datalist, MoreActivity.this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.moreList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dataAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        initList();
        handler = new MyHandler();
        initTabLayout();
        OperateDataBase.getInstance().setMoreHander(handler);
        Log.d("create", "moreActivity");
        TCPClient.setMoreHandler(handler);
        initAdapter();
    }

    public class MyHandler extends Handler {
        private JsonFormat jsonFormat = new JsonFormat();

        public MyHandler(){
        }

        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    collection.clear();
                    collection.addAll((List<Item>)message.obj);
                    if (tabposition == 0) {
                        datalist.clear();
                        datalist.addAll(collection);
                        dataAdapter.notifyDataSetChanged();
                    }

                    Log.d("receive", "receive collection");
                    break;
                case 2:
                    String s = (String)message.obj;
                    recommend = jsonFormat.readMessageOneType(s.substring(1, s.length()));
                    if (tabposition == 1){
                        datalist.clear();
                        datalist.addAll(recommend);
                        dataAdapter.notifyDataSetChanged();
                    }
                    Log.d("receive", "receive recommene");
                    break;
            }
        }
    }

}



class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.MyViewHolder> implements View.OnClickListener {
    List<Item> list;
    Context context = null;

    public MoreAdapter(List<Item> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.listview, parent, false));
        return holder;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getTitle());
        if (list.get(position).getImgUrl() == null)
            holder.imageView.setImageResource(list.get(position).getImgId());
        else Glide.with(holder.imageView).load(list.get(position).getImgUrl()).into(holder.imageView);

        holder.itemView.setTag(position);
        if (list.get(position).isViewed)
            holder.textView.setTextColor(Color.rgb(128, 128, 128));
        else
            holder.textView.setTextColor(Color.rgb(0, 0, 0));

        holder.itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        int position = (int)view.getTag();
        Log.d("detail", "id" + list.get(position).getNewid());
        OperateDataBase.getInstance().selectDescription(list.get(position).getNewid());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.title);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}

