package com.aden.radqcompanionapp;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radqcompanionapp.utils.FirebaseConnector;
import com.aden.radqcompanionapp.adapter.NotificationAdapter;
import com.aden.radqcompanionapp.utils.SettingsStorage;
import com.aden.radqcompanionapp.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends AppCompatActivity {

    ArrayList<Notification> notifications;
    NotificationAdapter notificationAdapter;

    //Firebase
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        //Initialize views
        ListView lvNotifications = findViewById(R.id.lvNotifications);

        //Load settings
        SettingsStorage settingsStorage = new SettingsStorage(NotificationsActivity.this);
        String accountId = settingsStorage.getIdentifierKey();

        databaseReference = FirebaseConnector.getFirebase().
                child("notifications").
                child(accountId);

        notifications = new ArrayList<>(10);

        notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notifications);

        lvNotifications.setAdapter(notificationAdapter);

        ValueEventListener valueEventListenerNotifications = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification notification = data.getValue(Notification.class);
                    notifications.add(notification);
                }
                Collections.reverse(notifications);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListenerNotifications);
    }
}
