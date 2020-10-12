package com.aden.radq;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.adapter.NotificationAdapter;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    private ArrayList<Notification> notifications;
    private ArrayAdapter arrayAdapter;

    private ListView lvNotifications;

    private DatabaseReference databaseReference;

    private String accountId;

    private ValueEventListener valueEventListenerNotifications;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.notifications_activity);

            Settings settings = new Settings(NotificationsActivity.this);
            accountId = settings.getIdentifier();

            databaseReference = FirebaseConnector.getFirebase().child("accounts").child(accountId).child("notifications");

            notifications = new ArrayList<>();

            lvNotifications = findViewById(R.id.lvNotifications);

            arrayAdapter = new NotificationAdapter(NotificationsActivity.this, notifications);

            lvNotifications.setAdapter(arrayAdapter);

            valueEventListenerNotifications = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "valueEventListenerNotifications. onDataChange");
                    notifications.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Notification notification = data.getValue(Notification.class);
                        notifications.add(notification);
                    }
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "valueEventListenerNotifications. onCancelled");
                }
            };
            Log.d(TAG, "notifications: " + notifications.isEmpty());

            databaseReference.addValueEventListener(valueEventListenerNotifications);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            databaseReference.removeEventListener(valueEventListenerNotifications);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
