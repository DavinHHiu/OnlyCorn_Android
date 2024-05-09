package com.example.onlycorn.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.example.onlycorn.R;
import com.example.onlycorn.utils.Pop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat postNotiSwitch;

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private boolean isPostEnabled;

    private static final String TOPIC_POST_NOTIFICATION = "POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        isPostEnabled = sp.getBoolean(TOPIC_POST_NOTIFICATION, false);

        initViews();
    }

    private void initViews() {
        postNotiSwitch = findViewById(R.id.postNotiSwitch);
        if (isPostEnabled) {
            postNotiSwitch.setChecked(true);
        } else {
            postNotiSwitch.setChecked(false);
        }
        postNotiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sp.edit();
                editor.putBoolean(TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked) {
                    subscribePostNotification();
                } else {
                    unsubscribePostNotification();
                }
            }
        });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String message = "You will receive post notifications";
                        if (!task.isSuccessful()) {
                            message = "Subscription failed";
                        }
                        Pop.pop(SettingsActivity.this, message);
                    }
                });
    }

    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String message = "You will not receive post notifications";
                        if (!task.isSuccessful()) {
                            message = "UnSubscription failed";
                        }
                        Pop.pop(SettingsActivity.this, message);
                    }
                });
    }
}