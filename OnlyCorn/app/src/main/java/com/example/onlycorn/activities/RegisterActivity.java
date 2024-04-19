package com.example.onlycorn.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.onlycorn.R;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEt, passwordEt;
    private TextView haveAccount;
    private Button registerBtn;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEt.setError("Invalid email address");
                    emailEt.setFocusable(true);
                } else if (password.length() < 6) {
                    passwordEt.setError("Password length at least 6 characters");
                    passwordEt.setFocusable(true);
                } else {
                    registerUser(email, password);
                }
            }
        });

        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            User userDB = new User(user.getUid(), user.getEmail(), "online");
                            FirebaseUtils.getDocumentRef(User.COLLECTION, user.getUid())
                                    .set(userDB);
                            Pop.pop(RegisterActivity.this, "Registerd...\n"+user.getEmail());
                        }
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Pop.pop(RegisterActivity.this, e.getMessage());
                    }
                });
    }

    private void initViews() {
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        registerBtn = findViewById(R.id.register_btn);
        haveAccount = findViewById(R.id.have_accountTv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Register User...");

        mAuth = FirebaseAuth.getInstance();
    }
}