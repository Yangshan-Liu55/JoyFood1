package com.example.shan.joyfood;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.firebase.client.Firebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shan on 2018-03-05.
 */

public class HomeFragment extends Fragment {

    // Data for ListView
    private List<Post> postList = new ArrayList<Post>();
    private List<String> postIdList = new ArrayList<String>();
    private DatabaseReference myReference;
    private PostAdapter adapter;
    private String post_user, post_name, post_imageUrl, post_text, post_time, post_ingredient, post_direction, post_ID;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home, container, false);
        ListView listView = (ListView)view.findViewById(R.id.listView);

        //FirebaseListAdapter
        Firebase.setAndroidContext(getActivity()); //Get context by getActivity(), not this
        //setContentView(R.layout.home); //Set a view for activity

        //set data to listview
        adapter = new PostAdapter(getActivity(), R.layout.post_item, postList); //Get context by getActivity(), not this
        listView.setAdapter(adapter);

       //initialize postList
        initPostList();

        //Set onClick event for each item in list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Post post = postList.get(position); //Get the current position of the postList<Post>,

                Intent intent = new Intent(getActivity(), PostShow.class); //fragment to activity
                intent.putExtra("user", post.getUser());
                intent.putExtra("name", post.getName());
                intent.putExtra("time", post.getTime());
                intent.putExtra("imageUrl", post.getImageUrl()); //imageID/imageUrl in firebase database
                intent.putExtra("text", post.getText());
                intent.putExtra("ingredient", post.getIngredient());
                intent.putExtra("direction", post.getDirection());
                intent.putExtra("posID", post.getPostID());

                startActivity(intent);
            }
        });

        return view;
    }

    private void initPostList() {
        //Show the posts in the list view(first save all posts to postList), then adapter
        postIdList.clear();
        postList.clear();
        adapter.notifyDataSetChanged();//同时更新ListView，否则ListView上存在历史数据

        myReference = FirebaseDatabase.getInstance().getReference().child("postContent");
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_post(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_post(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                romove_post(dataSnapshot);

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void append_post(DataSnapshot dataSnapshot) {
        //adapter.notifyDataSetChanged();
        Post temp_post;

        Iterator iterator = dataSnapshot.getChildren().iterator(); //dataSnapshot: randomkey post. iterator:dataSnapshot's children is text, name, ...
        while (iterator.hasNext()) {
            post_direction = (String) ((DataSnapshot)iterator.next()).getValue(); //direction
            post_imageUrl = (String) ((DataSnapshot)iterator.next()).getValue(); //imageUrl is String
            post_ingredient = (String) ((DataSnapshot)iterator.next()).getValue();//ingredient
            post_name = (String) ((DataSnapshot)iterator.next()).getValue();//name
            post_ID  = (String) ((DataSnapshot)iterator.next()).getValue();//postID
            post_text = (String) ((DataSnapshot)iterator.next()).getValue();//text
            post_time = (String) ((DataSnapshot)iterator.next()).getValue(); //time when posted
            post_user = (String) ((DataSnapshot)iterator.next()).getValue();//user

            if(!postIdList.contains(post_ID)){

                postIdList.add(post_ID);

                temp_post = new Post(post_user, post_name, post_time, post_imageUrl, post_text, post_ingredient, post_direction, post_ID);
                postList.add(temp_post);

            }

        }                   //Go back to addChildEventListener,(next dataSnapshot - randomkey post)

        adapter.notifyDataSetChanged();
    }

    private void romove_post(DataSnapshot dataSnapshot) {
        String temp_postID = dataSnapshot.getKey();
        if (postIdList.contains(temp_postID)) {
            int romove_position = postIdList.indexOf(temp_postID);

            postIdList.remove(romove_position);
            postList.remove(romove_position);

            adapter.notifyDataSetChanged();
        }
    }
}
