package com.example.xiaocj.news;

import android.content.Context;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder>  implements View.OnClickListener{

    List<Item> list;//存放数据
    Context context;

    private OnItemClickListener mOnItemClickListener = null;

    public ItemAdapter(List<Item> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.listview, parent, false));
        return holder;
    }

    //在这里可以获得每个子项里面的控件的实例，比如这里的TextView,子项本身的实例是itemView，
    // 在这里对获取对象进行操作
    //holder.itemView是子项视图的实例，holder.textView是子项内控件的实例
    //position是点击位置
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        //设置textView显示内容为list里的对应项
        holder.textView.setText(list.get(position).getTitle());

        if (list.get(position).getImgUrl() == null)
            holder.imageView.setImageResource(list.get(position).getImgId());
        else Glide.with(holder.imageView).load(list.get(position).getImgUrl()).into(holder.imageView);
        // holder.imageView.setImageResource(list.get(position).getImgId());

        holder.itemView.setTag(position);
        if (list.get(position).isViewed)
            holder.textView.setTextColor(Color.rgb(128, 128, 128));
        else
            holder.textView.setTextColor(Color.rgb(0, 0, 0));

        //子项的点击事件监听
        holder.itemView.setOnClickListener(this);
    }

    //define interface, to deal with click event
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    @Override
    public void onClick(View view){
        if (mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(view, (int)view.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    //要显示的子项数量
    @Override
    public int getItemCount() {
        return list.size();
    }

    //这里定义的是子项的类，不要在这里直接对获取对象进行操作
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.title);
            imageView = itemView.findViewById(R.id.image);
        }
    }


    //在指定位置插入，原位置的向后移动一格
    public boolean addItem(int position, Item item) {
        if (position < list.size() && position >= 0) {
            list.add(position, item);
            notifyItemInserted(position);
            return true;
        }
        return false;
    }

    //去除指定位置的子项
    public boolean removeItem(int position) {
        if (position < list.size() && position >= 0) {
            list.remove(position);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    //清空显示数据
    public void clearAll() {
        list.clear();
        notifyDataSetChanged();
    }
}

/*
public class ItemAdapter extends ArrayAdapter<Item>{
    private int layoutId;

    public ItemAdapter(Context context, int layoutId, List<Item> list){
        super(context, layoutId, list);
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder viewHolder;
        Item item = getItem(position);
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        } else {
            view = convertView;
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView textView = (TextView) view.findViewById(R.id.title);
        imageView.setImageResource(item.getImgId());
        textView.setText(item.getTitle());
        return view;
    }

}
*/