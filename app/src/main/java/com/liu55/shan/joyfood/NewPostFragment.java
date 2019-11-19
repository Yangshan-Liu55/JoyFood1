package com.liu55.shan.joyfood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shan on 2018-03-05.
 */

public class NewPostFragment extends Fragment {

    private DatabaseReference rootReference, contentRef, commentRef;
    private FirebaseAuth.AuthStateListener authStateListener; //for onStop
    private FirebaseAuth mAuth; //for onStop
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private String userName;
    private boolean imgChosen;
    private EditText editText_name, editText_text, editText_ingredient, editText_direction;
    private Button btn_addPost, btn_go_Login;
    private ImageButton btn_chooseImg;
    private ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(com.liu55.shan.joyfood.R.layout.new_post, container, false);

        //check if log in
        mAuth = FirebaseAuth.getInstance();//一定先实例化，否则onStart时add authStateListener报错无实例引用
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    btn_go_Login.setVisibility(View.VISIBLE);
                    btn_addPost.setVisibility(View.GONE);
                }
                else {
                    btn_go_Login.setVisibility(View.GONE);
                    btn_addPost.setVisibility(View.VISIBLE);
                }
            }
        };

        editText_name = (EditText)view.findViewById(com.liu55.shan.joyfood.R.id.editText_name);
        editText_text = (EditText)view.findViewById(com.liu55.shan.joyfood.R.id.editText_text);
        editText_ingredient = (EditText)view.findViewById(com.liu55.shan.joyfood.R.id.editText_ingredient);
        editText_direction = (EditText)view.findViewById(com.liu55.shan.joyfood.R.id.editText_direction);

        btn_chooseImg = (ImageButton) view.findViewById(com.liu55.shan.joyfood.R.id.btn_chooseImg);
        btn_addPost = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.btn_addPost);
        btn_go_Login = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.btn_go_Login);
        imageView = (ImageView) view.findViewById(com.liu55.shan.joyfood.R.id.image_uploaded);
        imageView.setVisibility(View.GONE);

        rootReference = FirebaseDatabase.getInstance().getReference().getRoot();
        contentRef = rootReference.child("postContent");//with child() can access into the root
        commentRef = rootReference.child("postComment");
        firebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = firebaseStorage.getReference();

        //Choose a picture
        btn_chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //method to show file chooser
                chooseImg();
            }
        });

        //Add the post data to firebase database
        btn_addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if entered name
                String postName = editText_name.getText().toString();
                if (!postName.trim().equals("")) {
                    //upload image first for getting the imageUrl from firebase storage
                    uploadImg(postName);
                } else {
                    Toast.makeText(getActivity(), "Enter your recipe name/title", Toast.LENGTH_SHORT).show();
                    editText_name.setError("Enter your recipe name/title"); return;}
            }
        });

        btn_go_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        return view;
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                imgChosen = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //end chooseImg() method

    //this method will upload the file to Firebase
    private void uploadImg(String postName) {
        final String name = postName;

        //if there is a file to upload
        if (filePath != null && imgChosen) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());//used inner method
            progressDialog.setTitle("Uploading image");
            progressDialog.show();

            StorageReference ref = mStorageRef.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //if upload successfully, hide the progress dialog
                    progressDialog.dismiss(); //inner method, progressDialog must be declared final

                    //get the image url for saving into firebase database
                    String imageUrl = taskSnapshot.getDownloadUrl().toString();
                    addPost(name, imageUrl);//inner method, name must be declared final
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //if the upload is not successfull, hiding the progress dialog
                    progressDialog.dismiss();
                    //and displaying error message
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
            //if no img selected, add a default img for the post
            String defaulImageUrl = "https://firebasestorage.googleapis.com/v0/b/joyfood-database.appspot.com/o/images%2Flogo-cut.png?alt=media&token=d7e16b73-48d5-4983-9247-c8a1849457f5";
            addPost(name, defaulImageUrl);
        }
    }

    private void addPost(String name, String imageUrl_arg) {
        //get user string from currentUser's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser.getEmail().toString();
        userName = userEmail.substring(0, userEmail.indexOf("@"));
        //get current time
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date date=new Date(System.currentTimeMillis());
        String time = format.format(date);

        //save all the data to firebase database
        Map<String, Object> map = new HashMap<String, Object>();
        String temp_key = contentRef.push().getKey(); //Add a child(postID) with random key for postContent
        contentRef.updateChildren(map); //Update the contentID in database(postContent)

        //Add several children to the random key content
        DatabaseReference postRef = contentRef.child(temp_key);
        Map<String,Object> map2 = new HashMap<String, Object>();
        map2.put("user", userName);
        map2.put("name", name);
        map2.put("time", time);
        map2.put("imageUrl", imageUrl_arg); //imageUrl is gotten from uploadImg() method
        map2.put("text", editText_text.getText().toString());
        map2.put("ingredient", editText_ingredient.getText().toString());
        map2.put("direction", editText_direction.getText().toString());
        map2.put("postID", temp_key);//post ID
        postRef.updateChildren(map2); //Update the data to postID under postContent

        //save the postID under postComment too
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put(temp_key, ""); //
        commentRef.updateChildren(map3);

        if (imgChosen){
            Toast.makeText(getActivity(), "Posted ", Toast.LENGTH_SHORT).show();
            imgChosen = false;}
        else { Toast.makeText(getActivity(), "Posted with no image", Toast.LENGTH_SHORT).show();}

        //clear the input text
        editText_name.getText().clear();
        editText_text.getText().clear();
        editText_ingredient.getText().clear();
        editText_direction.getText().clear();

        imageView.setVisibility(View.GONE);
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
