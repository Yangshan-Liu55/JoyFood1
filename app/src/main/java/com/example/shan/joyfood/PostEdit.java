package com.example.shan.joyfood;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostEdit extends BaseActivity {

    private final int PICK_IMAGE_REQUEST = 171;
    private Uri filePath;
    private ImageView image_show;
    private String post_imageUrl, post_ID;
    private EditText editText_name, editText_text, editText_ingredient, editText_direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        final String post_user, post_name, post_time, post_text, post_ingredient, post_direction;

        //get data from PostShow intent
        post_user = getIntent().getExtras().get("user").toString();
        post_name = getIntent().getExtras().get("name").toString();
        post_time = getIntent().getExtras().get("time").toString();
        post_imageUrl = getIntent().getExtras().get("imageUrl").toString();
        post_text = getIntent().getExtras().get("text").toString();
        post_ingredient = getIntent().getExtras().get("ingredient").toString();
        post_direction = getIntent().getExtras().get("direction").toString();
        post_ID = getIntent().getExtras().get("posID").toString();

        //initial view
        editText_name = (EditText)findViewById(R.id.edit_name);
        TextView textView_user = (TextView)findViewById(R.id.textView_post_user);
        TextView textView_time = (TextView)findViewById(R.id.textView_post_time);
        image_show = (ImageView)findViewById(R.id.image_post_show);
        ImageButton btn_chooseImg = (ImageButton)findViewById(R.id.imgBtn_chooseImg);
        editText_text = (EditText)findViewById(R.id.edit_text);
        editText_ingredient = (EditText)findViewById(R.id.edit_ingredient);
        editText_direction = (EditText)findViewById(R.id.edit_direction);
        ImageButton btn_delete = (ImageButton)findViewById(R.id.imgBtn_Delete);
        Button btn_sendPost = (Button)findViewById(R.id.btn_sendPost);

        //set data
        editText_name.setText(post_name);
        textView_user.setText("By: " + post_user);
        textView_time.setText(post_time);
        Glide.with(PostEdit.this).load(post_imageUrl).into(image_show);
        editText_text.setText(post_text);
        editText_ingredient.setText(post_ingredient);
        editText_direction.setText(post_direction);

        //Choose a picture
        btn_chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //method to show file chooser
                chooseImg();
            }
        });

        //Delete or modify the post data in Firebase database and storage
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference().getRoot();
        final DatabaseReference contentPostIdRef =rootReference.child("postContent").child(post_ID);
        final DatabaseReference commentPostIdRef = rootReference.child("postComment").child(post_ID);
        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post_imageUrl);

        //Delete the post
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostEdit.this);
                builder.setTitle("Delete the Post Forever");
                builder.setMessage("Are you sure to delete?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(PostEdit.this,"Delete Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        storageRef.delete(); //delete the file in Firebase

                        contentPostIdRef.removeValue(); //delete the post data in database
                        commentPostIdRef.removeValue(); //delete the comments data in database

                        Toast.makeText(PostEdit.this,"Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostEdit.this, MainActivity.class));
                        finish();
                    }
                });
                builder.create().show();
            }
        });

        //Modify the post
        btn_sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if entered name
                String postName = editText_name.getText().toString();
                if (!postName.trim().equals("")) {
                    //upload image first for getting the imageUrl from firebase storage
                    uploadImg();

                } else {
                    Toast.makeText(PostEdit.this, "Enter your recipe name/title", Toast.LENGTH_SHORT).show();
                    editText_name.setError("Enter your recipe name/title"); return;}
            }
        });

    }

    //method to show file chose
    private void chooseImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                image_show.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //end chooseImg() method

    //this method will upload the file to Firebase
     private void uploadImg() {
         //final String name = postName;
         //final String ID = postID;
         //if there is a file to upload
         if (filePath != null) {
             final ProgressDialog progressDialog = new ProgressDialog(PostEdit.this);//used inner method
             progressDialog.setTitle("Uploading image");
             progressDialog.show();

             StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
             ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                     //if upload successfully, hide the progress dialog
                     progressDialog.dismiss(); //inner method, progressDialog must be declared final

                     //delete the past image url in firebase storage
                     FirebaseStorage.getInstance().getReferenceFromUrl(post_imageUrl).delete();

                     //get the image url for saving into firebase database
                     String imageUrl = taskSnapshot.getDownloadUrl().toString();
                     modifyPost(imageUrl);//inner method, name must be declared final
                 }
             }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     //if the upload is not successfull, hiding the progress dialog
                     progressDialog.dismiss();
                     //and displaying error message
                     Toast.makeText(PostEdit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                 }
             }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                     //calculating progress percentage
                     double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                     //displaying percentage in progress dialog
                     progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                 }
             });
         } else {
             modifyPost(post_imageUrl);
         }
     }

     private void modifyPost(String imageUrl_arg){
         DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference().getRoot();
         DatabaseReference contentPostIdRef =rootReference.child("postContent").child(post_ID);

         //contentPostIdRef.child("name").setValue(editText_name.getText().toString()); // or set map value:
         Map<String,Object> map = new HashMap<String, Object>();
         //map.put("user", userName);
         map.put("name", editText_name.getText().toString());
         //map.put("time", time);
         map.put("imageUrl", imageUrl_arg); //imageUrl is gotten from uploadImg() method
         map.put("text", editText_text.getText().toString());
         map.put("ingredient", editText_ingredient.getText().toString());
         map.put("direction", editText_direction.getText().toString());
         //map.put("postID", temp_key);//post ID
         contentPostIdRef.updateChildren(map); //Update the data to postID under postContent

         Toast.makeText(this,"Data updated", Toast.LENGTH_SHORT).show();
         startActivity(new Intent(PostEdit.this, MainActivity.class));
         finish();

     }
}
