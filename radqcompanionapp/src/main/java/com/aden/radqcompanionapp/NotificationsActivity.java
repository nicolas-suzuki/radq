package com.aden.radqcompanionapp;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.aden.radqcompanionapp.adapter.NotificationAdapter;
import com.aden.radqcompanionapp.helper.Settings;
import com.aden.radqcompanionapp.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private ArrayList<Notification> notifications;
    private NotificationAdapter notificationAdapter;

    //Firebase
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        //Initialize views
        ListView lvNotifications = findViewById(R.id.lvNotifications);

        //Load settings
        Settings settings = new Settings(NotificationsActivity.this);
        String accountId = settings.getIdentifierKey();

        databaseReference = FirebaseConnector.getFirebase().
                child("notifications").
                child(accountId);

        notifications = new ArrayList<>();

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
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListenerNotifications);
    }
}
