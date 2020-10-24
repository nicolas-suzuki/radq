package com.aden.radqcompanionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.aden.radqcompanionapp.helper.Settings;
import com.aden.radqcompanionapp.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        //Load settings
        Settings settings = new Settings(MainActivity.this);
        String accountId = settings.getIdentifierKey();

        if(settings.getIdentifierKey() != null) {
            if ((settings.getIdentifierKey().isEmpty()) && (firebaseAuth.getCurrentUser() != null)) {
                firebaseAuth.signOut();
            }
            if ((firebaseAuth.getCurrentUser() == null) && (!settings.getIdentifierKey().isEmpty())) {
                settings.setIdentifierKey("");
            }
        }

        databaseReference = FirebaseConnector.getFirebase().
                child("notifications").
                child(accountId);

        notificationPopup();

        setContentView(R.layout.main_activity);

        ImageButton ibNotifications = findViewById(R.id.ibNotifications);
        ibNotifications.setOnClickListener(v -> openNotificationsActivity());

        ImageButton ibSettings = findViewById(R.id.ibLogin);
        ibSettings.setOnClickListener(v -> openLoginActivity());
    }

    private void openNotificationsActivity(){
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    private void openLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //Dialog box to warn the user about not defining a contact in the application settings
    private void alertDialogBox(String title, String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    //Notifications PopUp
    private void notificationPopup(){
        Query lastquery = databaseReference.orderByKey().limitToLast(1);
        ValueEventListener valueEventListenerNotificationPop = lastquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Test", "t");
                Notification notification = null;
                for (DataSnapshot data : snapshot.getChildren()) {
                    notification = data.getValue(Notification.class);
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "1")
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(getText(R.string.radq_notification))
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

                CharSequence charSequence;
                switch (notification.getNotification()) {
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

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                notificationManagerCompat.notify(100, builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.addValueEventListener(valueEventListenerNotificationPop);
    }
}
