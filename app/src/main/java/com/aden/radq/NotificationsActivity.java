package com.aden.radq;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.NotificationAdapter;
import com.aden.radq.model.Notification;
import com.aden.radq.utils.FirebaseConnector;
import com.aden.radq.utils.SettingsStorage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    ArrayList<Notification> notifications;
    NotificationAdapter notificationAdapter;

    //Firebase
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerNotifications;

    @Override
    protected final void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerNotifications);
    }

    @Override
    protected final void onStop() {
        super.onStop();
        if(valueEventListenerNotifications != null){
            databaseReference.removeEventListener(valueEventListenerNotifications);
        }
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        //Initialize views
        ListView lvNotifications = findViewById(R.id.lvNotifications);

        //Load settings
        SettingsStorage settingsStorage = new SettingsStorage(NotificationsActivity.this);
        String accountId = settingsStorage.getIdentifierKey();

        databaseReference = FirebaseConnector.getFirebase().
                child("accounts").
                child(accountId).
                child("notifications");

        notifications = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notifications);

        lvNotifications.setAdapter(notificationAdapter);

        valueEventListenerNotifications = new ValueEventListener() {
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
