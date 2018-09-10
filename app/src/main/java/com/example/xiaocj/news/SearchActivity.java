package com.example.xiaocj.news;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends Activity {
    private SearchView searchView = null;
    private MyHandler myHandler = new MyHandler();

    private List<Item> datalist = new ArrayList<>();
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initSearchView();
        OperateDataBase.getInstance().setSearchHandler(myHandler);
        TCPClient.setSearchHandler(myHandler);

        searchAdapter = new SearchAdapter(datalist, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchAdapter);


        OperateDataBase.getInstance().getViewed();
    }

    private void initSearchView(){
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                if (text.length() == 0){
                    return false;
                }
                OperateDataBase.getInstance().search(text);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) return false;
                OperateDataBase.getInstance().search(newText);
                return true;
            }
        });
    }

    public class MyHandler extends Handler{
        private JsonFormat jsonFormat = new JsonFormat();

        public MyHandler(){
        }

        public void handleMessage(Message message){
            switch (message.what){
                case 1:
                    datalist.clear();
                    datalist.addAll((List)message.obj);
                    searchAdapter.notifyDataSetChanged();
                    Log.d("receive", "search receive");
                    break;
                case 2:
                    String s = (String)message.obj;
                    datalist.clear();
                    datalist.addAll(jsonFormat.readMessageOneType(s.substring(1, s.length())));
                    searchAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}



class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> implements View.OnClickListener {
    List<Item> list;
    Context context = null;

    public SearchAdapter(List<Item> list, Context context){
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
