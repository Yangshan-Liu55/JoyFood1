package com.liu55.shan.joyfood;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PostShow extends BaseActivity {

    private String post_user, post_name, post_time, post_imageUrl, post_text, post_ingredient, post_direction, post_ID;
    private String userName;
    private ImageButton btn_send_msg, btn_modify, btn_delete, imgBtnMsg;
    private ImageView image_show;
    private Bitmap bp;
    private final int SUCCESS = 0;
    private final int FAIL = 1;
//    private final int CLICK = 2;
    private boolean fullScreen;
    private float scaleWidth, scaleHeight;

    private Button btn_post_to_login;
    private EditText input_msg;
    private TextView commentTextView;
    private LinearLayout edit_bar_layout, layoutMsg;
    private DatabaseReference rootReference, contentPostIdRef, commentPostIdRef;
    private FirebaseAuth.AuthStateListener authStateListener; //for onStop method
    private FirebaseAuth mAuth; //for onStop method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.liu55.shan.joyfood.R.layout.activity_post_show);

        //get data from HomeFragment intent
        post_user = getIntent().getExtras().get("user").toString();
        post_name = getIntent().getExtras().get("name").toString();
        post_time = getIntent().getExtras().get("time").toString();
        post_imageUrl = getIntent().getExtras().get("imageUrl").toString();
        post_text = getIntent().getExtras().get("text").toString();
        post_ingredient = getIntent().getExtras().get("ingredient").toString();
        post_direction = getIntent().getExtras().get("direction").toString();
        post_ID = getIntent().getExtras().get("posID").toString();
        //setTitle("JoyFood: " + post_name);

        //check if log in
        mAuth = FirebaseAuth.getInstance();//一定先实例化，否则onStart时add authStateListener报错
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user1 = firebaseAuth.getCurrentUser();
                if (user1 != null) {
         //如果user为null 则无法获取String userEmail，系统bug出现app crash。所以下面这两句只能放在if函数里
                    String userEmail = user1.getEmail().toString();
                    String currentUserName = userEmail.substring(0, userEmail.indexOf("@"));

                    if (post_user.equals(currentUserName)){
                        btn_post_to_login.setVisibility(View.GONE);
                        btn_modify.setVisibility(View.VISIBLE);
                        btn_delete.setVisibility(View.VISIBLE);
                        imgBtnMsg.setVisibility(View.VISIBLE);
                    }
                    else {
                        btn_post_to_login.setVisibility(View.GONE);
                        btn_modify.setVisibility(View.GONE);
                        btn_delete.setVisibility(View.GONE);
                        imgBtnMsg.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    edit_bar_layout.setVisibility(View.VISIBLE);
                    btn_post_to_login.setVisibility(View.VISIBLE);
                    btn_modify.setVisibility(View.GONE);
                    btn_delete.setVisibility(View.GONE);
                    imgBtnMsg.setVisibility(View.GONE);
                    layoutMsg.setVisibility(View.GONE);
                }
            }
        };

        //post detail view
        TextView textView_name = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_name);
        TextView textView_user = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_user);
        TextView textView_time = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_time);
        image_show = (ImageView)findViewById(com.liu55.shan.joyfood.R.id.image_show);
        TextView textView_text = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_text);
        TextView textView_ingredient = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_ingredient);
        TextView textView_direction = (TextView)findViewById(com.liu55.shan.joyfood.R.id.textView_direction);
        btn_modify  = (ImageButton)findViewById(com.liu55.shan.joyfood.R.id.imgBtnModify);
        btn_delete = (ImageButton)findViewById(com.liu55.shan.joyfood.R.id.imgBtnDelete);

        //go to modify the post activity
        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostShow.this, PostEdit.class);
                intent.putExtra("user", post_user);
                intent.putExtra("name", post_name);
                intent.putExtra("time", post_time);
                intent.putExtra("imageUrl", post_imageUrl); //imageID/imageUrl in firebase database
                intent.putExtra("text", post_text);
                intent.putExtra("ingredient", post_ingredient);
                intent.putExtra("direction", post_direction);
                intent.putExtra("posID", post_ID);

                startActivity(intent);
            }
        });

        //delete the post data in database and storage
        rootReference = FirebaseDatabase.getInstance().getReference().getRoot();
        contentPostIdRef =rootReference.child("postContent").child(post_ID);
        commentPostIdRef = rootReference.child("postComment").child(post_ID);
        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post_imageUrl);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostShow.this);
                builder.setTitle("Delete the Post Forever");
                builder.setMessage("Are you sure to delete?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(PostShow.this,"Delete Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //storageRef.delete(); //delete the file in Firebase

                        contentPostIdRef.removeValue(); //delete the post data in database
                        commentPostIdRef.removeValue(); //delete the comments data in database

                        Toast.makeText(PostShow.this,"Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostShow.this, MainActivity.class));
                        finish();
                    }
                });
                builder.create().show();
            }
        });

        //comment view
        btn_post_to_login = (Button)findViewById(com.liu55.shan.joyfood.R.id.btn_post_to_login);
        btn_send_msg = (ImageButton)findViewById(com.liu55.shan.joyfood.R.id.imageBtn_send_comment);
        commentTextView = (TextView)findViewById(com.liu55.shan.joyfood.R.id.commentTextView);
        input_msg = (EditText)findViewById(com.liu55.shan.joyfood.R.id.input_msg) ;
        imgBtnMsg = (ImageButton)findViewById(com.liu55.shan.joyfood.R.id.imgBtnMsg);
        ImageButton imgBtnHide = (ImageButton)findViewById(com.liu55.shan.joyfood.R.id.imgBtnHide);
        layoutMsg = (LinearLayout)findViewById(com.liu55.shan.joyfood.R.id.cmmtLayout) ;
        edit_bar_layout = (LinearLayout)findViewById(com.liu55.shan.joyfood.R.id.edit_bar_layout) ;

        //initialize comment layout gone
        layoutMsg.setVisibility(View.GONE);

        //show post detail
        textView_name.setText(post_name);
        textView_user.setText("By: " + post_user);
        textView_time.setText(post_time);
        textView_text.setText(post_text);
        textView_ingredient.setText(post_ingredient);
        textView_direction.setText(post_direction);

        //image_show.setImageResource(R.drawable.pasta); //glide with url into imgView insteads
        Glide.with(PostShow.this).load(post_imageUrl).into(image_show); //below insteads

        //Go to LOG IN
        btn_post_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostShow.this, LoginActivity.class));
                finish();
            }
        });

        //Show comment view/message linear layout
        imgBtnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMsg.setVisibility(View.VISIBLE);
                edit_bar_layout.setVisibility(View.GONE);
            }
        });

        //Send message and save to database
        mAuth = FirebaseAuth.getInstance();
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get user string from currentUser's email
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userEmail = currentUser.getEmail().toString();
                userName = userEmail.substring(0, userEmail.indexOf("@"));
                //get current time
                //long currentTime=System.currentTimeMillis();
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
                Date date=new Date(System.currentTimeMillis());
                String time = format.format(date);
                //Sage message to database postComment
                if (!input_msg.getText().toString().trim().equals("")) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    String temp_key = commentPostIdRef.push().getKey(); //get a message item

                    DatabaseReference messageRef = commentPostIdRef.child(temp_key);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("user", userName); // key:name, value:user_name
                    map2.put("time", time);
                    map2.put("message", input_msg.getText().toString());
                    messageRef.updateChildren(map2); // save the map2 data into firebase

                    input_msg.getText().clear();
                } else {
                    input_msg.setError("Input message");
                }
            }
        });

        //show message in comment textView
        commentPostIdRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendComment(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendComment(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Hide comment view/message linear layout
        imgBtnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_bar_layout.setVisibility(View.VISIBLE);
                layoutMsg.setVisibility(View.GONE);
            }
        });

    }


    //show message in textView
    private void appendComment(DataSnapshot dataSnapshot) {
        String comment_msg, comment_user, comment_time;
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            comment_msg = (String) ((DataSnapshot)iterator.next()).getValue();
            comment_time = (String) ((DataSnapshot)iterator.next()).getValue();
            comment_user = (String) ((DataSnapshot)iterator.next()).getValue();
            //Show data in TextView
            commentTextView.append(comment_time +"  " +comment_user + ": \n" + comment_msg + "\n\n"); //Show the data to TextView chat_conversation
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

}
