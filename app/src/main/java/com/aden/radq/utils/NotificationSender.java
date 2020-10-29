package com.aden.radq.utils;

import androidx.annotation.NonNull;

import com.aden.radq.model.Contact;
import com.aden.radq.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationSender {
    //Sends and stores notifications to contacts and current accountId
    final String accountId;

    public NotificationSender(String accountId) {
        this.accountId = accountId;
    }

    public void send(String message){
        DatabaseReference databaseReference = FirebaseConnector.getFirebase()
                .child("contacts")
                .child(accountId);
        String timeStamp = createTimeStamp();

        Notification notification = new Notification();
        notification.setNotification(message);
        notification.setTimestamp(timeStamp);
        notification.setUserId(accountId);

        ValueEventListener valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference myNotifications = FirebaseConnector.getFirebase().child("accounts");
                DatabaseReference globalNotifications = FirebaseConnector.getFirebase().child("notifications");

                for (DataSnapshot data : snapshot.getChildren()) {
                    Contact contact = data.getValue(Contact.class);
                    assert contact != null;
                    globalNotifications.
                            child(contact.getId()).
                            push().
                            setValue(notification);
                }
                myNotifications.
                        child(accountId).
                        child("notifications").
                        push().
                        setValue(notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListenerMyContacts);
    }

    private static String createTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        Date currentTime = Calendar.getInstance().getTime();

        return simpleDateFormat.format(currentTime);
    }
}
