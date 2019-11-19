package com.liu55.shan.joyfood;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by shan on 2018-03-03.
 */

public class PostAdapter extends ArrayAdapter<Post> {

    private Activity context;
    private int resource;

    // Glide.with(context) must be Activity, so declare Activity context, change argument Context context to Activity
    public PostAdapter(@NonNull Activity context, int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource; // list view id
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = getItem(position); // Get the current item of postListView
        //View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        View view; // 提升 ListView 的运行效率
        ViewHolder viewHolder; // 优化获取findViewById()控件实例，先自建以下class ViewHolder
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(com.liu55.shan.joyfood.R.id.image);
            viewHolder.textViewUser = (TextView) view.findViewById(com.liu55.shan.joyfood.R.id.postItemUser);
            viewHolder.textViewName = (TextView) view.findViewById(com.liu55.shan.joyfood.R.id.postItemName);
            viewHolder.textViewText = (TextView) view.findViewById(com.liu55.shan.joyfood.R.id.postItemText);
            viewHolder.textViewTime = (TextView) view.findViewById(com.liu55.shan.joyfood.R.id.postItemTime);
            view.setTag(viewHolder); //Save viewholder in view
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); //Get viewHolder again.
        } // convertView 用于将之前加载的数据进行缓存

        //ImageView imageView = (ImageView) view.findViewById(R.id.image); //viewholder insteads
        //TextView textView = (TextView) view.findViewById(R.id.postText); //viewholder insteads
        //viewHolder.imageView.setImageResource(R.drawable.pasta); //glide insteads
        Glide.with(context).load(post.getImageUrl()).into(viewHolder.imageView); //glide with url into imgView
        // Glide.with(context) must be Activity, so declare Activity context, change argument Context context to Activity
        viewHolder.textViewUser.setText(post.getUser()); // who posted it
        viewHolder.textViewTime.setText(post.getTime()); //posted time
        viewHolder.textViewName.setText(post.getName());
        viewHolder.textViewText.setText(post.getText());

        return view;
    }

    class ViewHolder {
        ImageView imageView;
        TextView textViewUser;
        TextView textViewTime;
        TextView textViewName;
        TextView textViewText;
    }
}
