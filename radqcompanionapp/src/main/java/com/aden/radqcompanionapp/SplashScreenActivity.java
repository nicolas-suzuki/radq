package com.aden.radqcompanionapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radqcompanionapp.utils.SettingsStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        createToken();
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            CharSequence channelName;
            NotificationChannel channel;
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            //Fall detection Channel (I'm okay)
            channelName = getText(R.string.channel_name_fall_detected_okay);
            channel = new NotificationChannel("channel_fall_detected_okay", channelName , importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

            //Fall detection Channel (I'm NOT okay)
            channelName = getText(R.string.channel_name_fall_detected_not_okay);
            channel = new NotificationChannel("channel_fall_detected_not_okay", channelName , importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

            //Fall detection Channel (NO buttons Pressed)
            channelName = getText(R.string.channel_name_fall_detected_no_buttons_pressed);
            channel = new NotificationChannel("channel_fall_detected_no_buttons_pressed", channelName , importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

            //Detection Started Channel
            channelName = getText(R.string.channel_name_detection_started);
            channel = new NotificationChannel("channel_detection_started", channelName , importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

            //Detection Stopped Channel
            channelName = getText(R.string.channel_name_detection_stopped);
            channel = new NotificationChannel("channel_detection_stopped", channelName , importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    private void createToken(){
        SettingsStorage settingsStorage = new SettingsStorage(this);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                String token = Objects.requireNonNull(task.getResult()).getToken();
                settingsStorage.setPhoneKey(token);
            }
        });
    }
}
