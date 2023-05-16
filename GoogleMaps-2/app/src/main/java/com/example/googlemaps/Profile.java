package com.example.googlemaps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;

import com.example.googlemaps.Adapter.ChatMessageRecyclerAdapter;
import com.example.googlemaps.Fragments.BottomSheetFragmentUsernameAndBioUpdate;
import com.example.googlemaps.Fragments.UserListFragment;
import com.example.googlemaps.model.Message;
import com.example.googlemaps.model.User;
import com.example.googlemaps.model.UserClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.hasnat.sweettoast.SweetToast;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.example.googlemaps.utils.ImageLoader.loadImageChatMessage;

public class Profile extends AppCompatActivity implements View.OnClickListener {
    Context context;

    byte[] dataImageByte;
    TextView tv_currentUserName_profile_fragment;
    CircleImageView iv_profileImage_profile_fragment;
    ImageView btn_profile_image_change;
    ImageView btn_save_edit_user_name;
    TextView tv_profile_fragment_bio;

    String username;
    String imageUrl;
    String userBio;
    String userId;
    User user;
    private String download_url;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    String timeStamp;
    @SuppressWarnings("rawtypes")
    private StorageTask uploadImageTask;
    private StorageReference imageMessageStorageRef;
    private StorageReference fileReference;

    Boolean isUsername;

    private FirebaseFirestore mDb;

    public Profile(Context context) {

        this.context = context;
    }

    public Profile() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:{
                UserListFragment fragment =
                        (UserListFragment) getSupportFragmentManager().findFragmentByTag("User List");
                if(fragment != null){
                    if(fragment.isVisible()){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }

            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void initSupportActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        setTitle("Profile");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_2);

        tv_currentUserName_profile_fragment = findViewById(R.id.tv_profile_fragment_username);

        tv_profile_fragment_bio = findViewById(R.id.tv_profile_fragment_bio);
        tv_profile_fragment_bio.setOnClickListener(this);
        iv_profileImage_profile_fragment = findViewById(R.id.iv_profile_fragment);

        findViewById(R.id.iv_profile_fragment).setOnClickListener(this);

        findViewById(R.id.btn_profile_image_change).setOnClickListener(this);

        findViewById(R.id.btn_save_edit_username).setOnClickListener(this);


        mDb = FirebaseFirestore.getInstance();
        imageMessageStorageRef = FirebaseStorage.getInstance().getReference().child("messages_image");
        fetchCurrentUserdata();
        initSupportActionBar();

    }

    @Override
    public void onStart() {
        super.onStart();

    }



    private void fetchCurrentUserdata() {
        user = ((UserClient) (getApplicationContext())).getUser();

        if (user != null) {
            username = user.getUsername();
            tv_currentUserName_profile_fragment.setText(username);
            imageUrl = user.getAvatar();
            if (user.getBio() != null) {

                userBio = user.getBio();
                tv_profile_fragment_bio.setText(userBio);

            } else {
                tv_profile_fragment_bio.setText("");
            }
            userId = user.getUser_id();
            //iv_profileImage_profile_fragment.setImageResource(R.drawable.placeholder_image_chat);
            if (imageUrl.equals("default")) {
                iv_profileImage_profile_fragment.setImageResource(R.drawable.placeholder_image_chat);

            } else {
                loadImageChatMessage(iv_profileImage_profile_fragment, imageUrl);
            }
        } else {
            Toast.makeText(context, "User not found..", Toast.LENGTH_SHORT).show();
        }

    }


    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }


    private void uploadImage() {

        if (imageUri != null) {
            final StorageReference file_path = imageMessageStorageRef.child(userId + ".jpg");
            UploadTask uploadTask = file_path.putFile(imageUri);
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        SweetToast.error(Profile.this, "Error: " + task.getException().getMessage());
                    }
                    download_url = file_path.getDownloadUrl().toString();
                    return file_path.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        if (task.isSuccessful()) {
                            download_url = task.getResult().toString();
                            user.setAvatar(download_url);
                            loadImageChatMessage(iv_profileImage_profile_fragment, download_url);
                            mDb.collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                View parentLayout = findViewById(android.R.id.content);
                                                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                            }
                                            // progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            //progressDialog.dismiss();
                                        }
                                    });
                            Log.e("tag", "Image sent successfully");
                        } else {
                            SweetToast.warning(Profile.this, "Failed to send image. Try again");
                        }
                    }
                }
            });
        }


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();

        }

    }

    public void reloadData() {
        mDb.collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: successfully set the user client.");
                            user = task.getResult().toObject(User.class);
                            if (user != null) {
                                username = user.getUsername();
                                tv_currentUserName_profile_fragment.setText(username);
                                imageUrl = user.getAvatar();
                                if (user.getBio() != null) {

                                    userBio = user.getBio();
                                    tv_profile_fragment_bio.setText(userBio);

                                } else {
                                    tv_profile_fragment_bio.setText("");
                                }
                                userId = user.getUser_id();
                                if (imageUrl.equals("default")) {
                                    iv_profileImage_profile_fragment.setImageResource(R.drawable.placeholder_image_chat);

                                } else {
                                    loadImageChatMessage(iv_profileImage_profile_fragment, imageUrl);
                                }
                            } else {
                                Toast.makeText(context, "User not found..", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    private void openBottomSheet(Boolean isUsername) {
        BottomSheetFragmentUsernameAndBioUpdate bottomSheetFragmentUsernameAndBioUpdate = new BottomSheetFragmentUsernameAndBioUpdate(context, isUsername);
        assert getSupportFragmentManager() != null;
        bottomSheetFragmentUsernameAndBioUpdate.show(getSupportFragmentManager(), "edit");


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_profile_image_change: {
                openImage();
                break;
            }
            case R.id.iv_profile_fragment: {
                openImage();
                break;
            }
            case R.id.btn_save_edit_username: {
                isUsername = true;
                openBottomSheet(true);
                break;
            }
            case R.id.tv_profile_fragment_bio: {
                isUsername = false;
                openBottomSheet(false);
                break;
            }

        }
    }
}


