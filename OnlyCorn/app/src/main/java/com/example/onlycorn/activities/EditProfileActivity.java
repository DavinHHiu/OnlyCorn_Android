package com.example.onlycorn.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.R;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.ImageUtils;
import com.example.onlycorn.utils.Pop;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView avatarIv;
    private EditText nameEt, userNameEt, emailEt;
    private TextView editAvatar;
    private Button updateButton;

    private FirebaseUser userFb;
    private User user;
    private FirebaseAuth mAuth;

    private String storagePath = "Users_Avatar/user_";
    private Uri imageUri;
    ActivityResultLauncher<Intent> imagePickLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        initViews();

        mAuth = FirebaseAuth.getInstance();
        userFb = mAuth.getCurrentUser();
        loadUserInfo();

        editAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAvatarUpdateMethod();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileUser();
            }
        });

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            Glide.with(getApplicationContext()).load(imageUri)
                                    .apply(RequestOptions.circleCropTransform()).into(avatarIv);
                        }
                    }
                }
        );
    }

    private void loadUserInfo() {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, userFb.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        nameEt.setText(user.getName());
                        userNameEt.setText(user.getUsername());
                        emailEt.setText(user.getEmail());

                        try {
                            imageUri = Uri.parse(user.getImage());
                            Glide.with(getApplicationContext()).load(imageUri)
                                    .apply(RequestOptions.circleCropTransform()).into(avatarIv);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void updateProfileUser() {
        String name = nameEt.getText().toString().trim();
        String username = userNameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Pop.pop(this, "Vui lòng điền đầy đủ thông tin");
        } else {
            User userDB = new User(userFb.getUid(), name, email, username, imageUri.toString(), "online");
            FirebaseUtils.getDocumentRef(User.COLLECTION, userFb.getUid())
                    .set(userDB)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                uploadProfileAvatar();
                                Pop.pop(getApplicationContext(), "Cập nhật thành công");
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }
                    });
        }
    }

    private void uploadProfileAvatar() {
        String filePathAndName = storagePath + userFb.getUid();

        StorageReference storageRef2 = FirebaseUtils.getStorageRef(filePathAndName);
        if (imageUri != null && imageUri.toString().equals(user.getImage())) {
            return;
        }
        storageRef2.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        if (uriTask.isSuccessful()) {
                            String downloadUri = uriTask.getResult().toString();

                            FirebaseUtils.getDocumentRef(User.COLLECTION, userFb.getUid())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            User userDB = task.getResult().toObject(User.class);
                                            if (userDB != null) {
                                                userDB.setImage(downloadUri);
                                                FirebaseUtils.getDocumentRef(User.COLLECTION, userFb.getUid())
                                                        .set(userDB);
                                            }
                                        }
                                    });


                        } else {
                            Pop.pop(getApplicationContext(), "Some error occured");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Pop.pop(getApplicationContext(), e.getMessage());
                    }
                });
    }

    private void pickAvatarUpdateMethod() {
        ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent(new Function1<Intent, Unit>() {
                    @Override
                    public Unit invoke(Intent intent) {
                        imagePickLauncher.launch(intent);
                        return null;
                    }
                });
    }

    private void initViews() {
        nameEt = findViewById(R.id.nameEt);
        userNameEt = findViewById(R.id.usernameEt);
        emailEt = findViewById(R.id.emailEt);
        editAvatar = findViewById(R.id.edit_avatar);
        updateButton = findViewById(R.id.updateButton);
        avatarIv = findViewById(R.id.avatarIv);
    }
}