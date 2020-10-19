package com.aden.radqcompanionapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.aden.radqcompanionapp.adapter.NotificationAdapter;
import com.aden.radqcompanionapp.helper.Settings;
import com.aden.radqcompanionapp.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    private ArrayList<Notification> notifications;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        //Load settings
        Settings settings = new Settings(NotificationsActivity.this);
        String accountId = settings.getIdentifierKey();
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());

        DatabaseReference databaseReference = FirebaseConnector.getFirebase().
                child("notifications").
                child(accountId);

        notifications = new ArrayList<>();

        ListView lvNotifications = findViewById(R.id.lvNotifications);

        notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notifications);

        lvNotifications.setAdapter(notificationAdapter);

        ValueEventListener valueEventListenerNotifications = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "valueEventListenerNotifications. onDataChange");
                notifications.clear();
                int id = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification notification = data.getValue(Notification.class);
                    notifications.add(notification);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationsActivity.this, "1")
                            .setSmallIcon(R.drawable.notification)
                            .setContentTitle(getText(R.string.radq_notification))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    CharSequence charSequence;
                    switch (Objects.requireNonNull(notification).getNotification()) {
                        case "aW1va2F5YnV0dG9ucHJlc3NlZA":
                            charSequence = getText(R.string.aW1va2F5YnV0dG9ucHJlc3NlZA);
                            builder.setContentText(charSequence);
                            break;
                        case "aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA":
                            charSequence = getText(R.string.aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA);
                            builder.setContentText(charSequence);
                            break;
                        case "YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg":
                            charSequence = getText(R.string.YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg);
                            builder.setContentText(charSequence);
                            break;
                    }

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(NotificationsActivity.this);
                    notificationManagerCompat.notify(id, builder.build());
                    id++;
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "valueEventListenerNotifications. onCancelled");
            }
        };
        databaseReference.addValueEventListener(valueEventListenerNotifications);
    }
}
