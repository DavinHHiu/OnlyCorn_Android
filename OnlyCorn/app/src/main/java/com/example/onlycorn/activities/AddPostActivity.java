package com.example.onlycorn.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.R;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.ImageUtils;
import com.example.onlycorn.utils.Pop;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddPostActivity extends AppCompatActivity {
    private EditText captionEt;
    private ImageView postImage, avatarIv;
    private TextView usernameTv, nameTv;
    private Button uploadButton;
    private PlayerView playerView;

    private Uri imageUri;
    private String type;
    private ProgressDialog pd;
    private boolean editPost;
    private String postId;
    private Post post;
    private User user;

    ActivityResultLauncher<Intent> imagePickLauncher;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        initViews();

        FirebaseUser userFb = FirebaseUtils.getUserAuth();
        FirebaseUtils.getDocumentRef(User.COLLECTION, userFb.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            usernameTv.setText(user.getUsername());
                            nameTv.setText(user.getName());
                            try {
                                Glide.with(getApplicationContext()).load(Uri.parse(user.getImage()))
                                        .apply(RequestOptions.circleCropTransform()).into(avatarIv);
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    }
                });

        editPost = getIntent().getStringExtra("key") != null && getIntent().getStringExtra("key").equals("editPost");
        if (editPost) {
            uploadButton.setText("Update");
            postId = getIntent().getStringExtra("postId");
            loadPostData(postId);
        } else {
            uploadButton.setText("Upload");
        }

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            type = "image";
                            postImage.setVisibility(View.VISIBLE);
                            playerView.setVisibility(View.GONE);

                            postImage.setBackground(null);
                            imageUri = data.getData();
                            Picasso.get().load(imageUri).into(postImage);
                        }
                    }
                }
        );
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    imageUri = uri;
                    if (uri != null) {
                        String mimeType = getContentResolver().getType(imageUri);
                        if (mimeType != null && mimeType.startsWith("image/")) {
                            type = "image";
                            postImage.setVisibility(View.VISIBLE);
                            playerView.setVisibility(View.GONE);

                            postImage.setBackground(null);
                            BitmapFactory.Options options = ImageUtils.getImageSize(getApplicationContext(), uri);
                            postImage.setMinimumHeight(options.outHeight);

                            Glide.with(getApplicationContext()).load(uri).into(postImage);
                        } else if (mimeType != null && mimeType.startsWith("video/")) {
                            type = "video";
                            playerView.setVisibility(View.VISIBLE);
                            postImage.setVisibility(View.GONE);
                            SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
                            player.setMediaItem(MediaItem.fromUri(uri));
                            player.prepare();
                            player.play();

                            playerView.setPlayer(player);
                        }
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        pd = new ProgressDialog(this);

    }

    private void initViews() {
        captionEt = findViewById(R.id.captionEt);
        postImage = findViewById(R.id.postImage);
        playerView = findViewById(R.id.playerView);
        uploadButton = findViewById(R.id.uploadButton);
        usernameTv = findViewById(R.id.usernameTv);
        nameTv = findViewById(R.id.nameTv);
        avatarIv = findViewById(R.id.avatarIv);

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = captionEt.getText().toString().trim();

                if (TextUtils.isEmpty(caption)) {
                    Pop.pop(AddPostActivity.this, "Enter caption...");
                    return;
                }

                if (editPost) {
                    beginUpdate(caption, postId);
                } else {
                    uploadData(caption);
                }
            }
        });
    }

    private void loadUser(String userId, ImageView userPhoto, TextView userName, boolean saveGlobal) {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, userId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    User userDB = documentSnapshot.toObject(User.class);

                    if (userDB != null) {
                        if (userName != null) {
                            userName.setText(userDB.getUsername());
                        }
                        try {
                            Glide.with(getApplicationContext()).load(Uri.parse(userDB.getImage()))
                                    .apply(RequestOptions.circleCropTransform()).into(userPhoto);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    if (saveGlobal) {
                        user = userDB;
                    }
                }
            }
        });
    }

    private void beginUpdate(String caption, String postId) {
        pd.setTitle("Updating Post...");
        pd.show();

        if (!post.getImage().equals("noImage")) {
            updateWasWithImage();
        } else if (postImage.getDrawable() != null) {
            updateWithNowImage();
        } else {
            updateWithoutImage();
        }
    }

    private void updateWithoutImage() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Post postDb = new Post(postId, captionEt.getText().toString(), "noMedia",
                "noImage", timestamp, post.getLikes(), post.getComments(),
                user.getUserId(), user.getUsername(), user.getImage());
        DocumentReference ref = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
        ref.set(postDb)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Pop.pop(AddPostActivity.this, "Updated...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(AddPostActivity.this, e.getMessage());
                    }
                });
    }

    private void updateWithNowImage() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timestamp;

        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseUtils.getStorageRef(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            Post postDb = new Post(postId, captionEt.getText().toString(), "image",
                                    downloadUri, timestamp, post.getLikes(), post.getComments(),
                                    user.getUserId(), user.getUsername(), user.getImage());
                            DocumentReference ref = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
                            ref.set(postDb)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Pop.pop(AddPostActivity.this, "Updated...");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Pop.pop(AddPostActivity.this, e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(AddPostActivity.this, e.getMessage());
                    }
                });

    }

    private void updateWasWithImage() {
        StorageReference imageRef = FirebaseUtils.getStorageRefFromUrl(post.getImage());
        imageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timestamp;

                        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseUtils.getStorageRef(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {
                                            Post postDb = new Post(postId, captionEt.getText().toString(), "image",
                                                    downloadUri, timestamp, post.getLikes(), post.getComments(),
                                                    user.getUserId(), user.getUsername(), user.getImage());
                                            DocumentReference ref = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
                                            ref.set(postDb)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            pd.dismiss();
                                                            Pop.pop(AddPostActivity.this, "Updated...");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Pop.pop(AddPostActivity.this, e.getMessage());
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Pop.pop(AddPostActivity.this, e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(AddPostActivity.this, e.getMessage());
                    }
                });
    }

    private void uploadData(String caption) {
        pd.setTitle("Publishing post...");
        pd.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timestamp;

        if ("image".equals(type)) {
            Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                              Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                              while (!uriTask.isSuccessful()) ;
                              String downloadUri = postImage.getDrawable() != null ? uriTask.getResult().toString() : "noImage";

                              if (uriTask.isSuccessful()) {
                                  Post postDb = new Post(timestamp, captionEt.getText().toString(), "image",
                                          downloadUri, timestamp, "0", "0", user.getUserId(),
                                          user.getUsername(), user.getImage());
                                  FirebaseUtils.getDocumentRef(Post.COLLECTION, timestamp).set(postDb)
                                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void unused) {
                                                  pd.dismiss();
                                                  Pop.pop(AddPostActivity.this, "Posted");
                                                  startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                                                  sendPostNotification(postDb);
                                                  finish();
                                              }
                                          })
                                          .addOnFailureListener(new OnFailureListener() {
                                              @Override
                                              public void onFailure(@NonNull Exception e) {
                                                  pd.dismiss();
                                                  Pop.pop(AddPostActivity.this, e.getMessage());
                                              }
                                          });
                              }
                          }
                      }
                    ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Pop.pop(AddPostActivity.this, e.getMessage());
                        }
                    });
        } else if ("video".equals(type)) {
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                              Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                              while (!uriTask.isSuccessful()) ;
                              String downloadUri = imageUri != null ? uriTask.getResult().toString() : "noImage";

                              if (uriTask.isSuccessful()) {
                                  Post postDb = new Post(timestamp, captionEt.getText().toString(), "video",
                                          downloadUri, timestamp, "0", "0", user.getUserId(),
                                          user.getUsername(), user.getImage());
                                  FirebaseUtils.getDocumentRef(Post.COLLECTION, timestamp).set(postDb)
                                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void unused) {
                                                  pd.dismiss();
                                                  Pop.pop(AddPostActivity.this, "Posted");
                                                  startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                                                  sendPostNotification(postDb);
                                                  finish();
                                              }
                                          })
                                          .addOnFailureListener(new OnFailureListener() {
                                              @Override
                                              public void onFailure(@NonNull Exception e) {
                                                  pd.dismiss();
                                                  Pop.pop(AddPostActivity.this, e.getMessage());
                                              }
                                          });
                              }
                          }
                      }
                    ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Pop.pop(AddPostActivity.this, e.getMessage());
                        }
                    });
        } else {
            Post postDb = new Post(timestamp, captionEt.getText().toString(), "noMedia",
                    "noImage", timestamp, "0", "0",
                    user.getUserId(), user.getUsername(), user.getImage());
            FirebaseUtils.getDocumentRef(Post.COLLECTION, timestamp).set(postDb)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Pop.pop(AddPostActivity.this, "Posted");
                            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                            sendPostNotification(postDb);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Pop.pop(AddPostActivity.this, e.getMessage());
                        }
                    });
        }
    }

    private void sendPostNotification(Post post) {
        FirebaseUtils.getDocumentRef(User.FOLLOWER_COLLECTION, user.getUserId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        for (String followerId : data.keySet()) {
                            FirebaseUtils.getDocumentRef(User.COLLECTION, followerId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    String fcmToken = task.getResult().getString("fcmToken");
                                    if (fcmToken != null) {
                                        try{
                                            JSONObject jsonObject = new JSONObject();

                                            JSONObject notificationObj = new JSONObject();
                                            notificationObj.put("title", "Only Corn");
                                            notificationObj.put("body", String.format("%s vừa đăng 1 bài viết mới.", post.getUsername()));

                                            JSONObject dataObj = new JSONObject();
                                            dataObj.put("postId", postId);

                                            jsonObject.put("notification", notificationObj);
                                            jsonObject.put("data", dataObj);
                                            jsonObject.put("to", fcmToken);

                                            FirebaseUtils.callApi(jsonObject);
                                        } catch (Exception ex) {

                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void loadPostData(String postId) {
        CollectionReference ref = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        Query query = ref.orderBy("postId").whereEqualTo("postId", postId);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    post = qds.toObject(Post.class);

                    captionEt.setText(post.getCaption());

                    if (!post.getImage().equals("noImage")) {
                        try {
                            Picasso.get().load(post.getImage()).into(postImage);
                        } catch (Exception ex) {
                            Picasso.get().load(post.getImage()).into(postImage);
                        }
                    }
                }
            }
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickFromCamera();
                }
                if (which == 1) {
                    pickFromGallery();
                }
            }
        });

        builder.create().show();
    }

    private void pickFromGallery() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void pickFromCamera() {
        ImagePicker.with(this).cameraOnly().crop(9f, 16f).maxResultSize(1080, 1920)
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        imagePickLauncher.launch(intent);
                        return null;
                    }
                });
    }
}