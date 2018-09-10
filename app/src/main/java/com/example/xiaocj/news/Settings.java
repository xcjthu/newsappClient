package com.example.xiaocj.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.MyViewHolder> implements View.OnClickListener {
    boolean[] visable = null;
    Context context = null;

    public SettingAdapter(Context context){
        visable = new boolean[Channels.titles.size()];
        for (int i = 0; i < Channels.titles.size(); ++ i){
            if (Channels.visibleTitle.contains(Channels.titles.get(i)))
                visable[i] = true;
            else
                visable[i] = false;
        }
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.choose, parent, false));
        return holder;
    }

    @Override
    public int getItemCount() {
        return Channels.titles.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.checkBox.setChecked(visable[position]);
        holder.checkBox.setOnClickListener(this);
        holder.checkBox.setTag(position);
        holder.textView.setText(Channels.titles.get(position));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        int position = (int)view.getTag();
        visable[position] = !visable[position];
        this.notifyDataSetChanged();
        Log.d("click", "gg");
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView textView;
        public MyViewHolder(View itemView){
            super(itemView);
            checkBox = itemView.findViewById(R.id.settingCheckBox);
            textView = itemView.findViewById(R.id.channelName);
        }
    }
}

public class Settings extends AppCompatActivity {
    SettingAdapter settingAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingAdapter = new SettingAdapter(this);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.settingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(settingAdapter);

        toolbar = (Toolbar)findViewById(R.id.settingToolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.channels, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.confirm:
                boolean allFalse = true;
                for (int i = 0; i < settingAdapter.visable.length; i++) {
                    if (settingAdapter.visable[i]){
                        allFalse = false;
                        Log.d("comfirm", Channels.titles.get(i));
                    }

                }
                if (allFalse){
                    Toast.makeText(Settings.this, "您需要至少选择一个频道", Toast.LENGTH_SHORT);
                    break;
                }
                Intent intent = this.getIntent();
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < Channels.titles.size(); i++) {
                    try {
                        jsonObject.put(Channels.titles.get(i), settingAdapter.visable[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra("result", jsonObject.toString());
                this.setResult(Activity.RESULT_OK, intent);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}


