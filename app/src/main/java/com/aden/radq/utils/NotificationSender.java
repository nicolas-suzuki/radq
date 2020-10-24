package com.aden.radq.utils;

import androidx.annotation.NonNull;

import com.aden.radq.model.Contact;
import com.aden.radq.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class NotificationSender {
    //Sends and stores notifications to contacts and current accountId

    private String accountId;

    public NotificationSender(String accountId) {
        this.setAccountId(accountId);
    }

    private void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void send(String message){
        DatabaseReference databaseReference;
        String timeStamp = createTimeStamp();

        Notification notification = new Notification();
        notification.setNotification(message);
        notification.setTimestamp(timeStamp);
        notification.setUserId(accountId);

        databaseReference = FirebaseConnector.getFirebase().child("notifications");
        for (String myContactId : getContactsIds()) {
            databaseReference.
                    child(myContactId).
                    push().
                    setValue(notification);
        }

        databaseReference = FirebaseConnector.getFirebase().child("accounts");
        databaseReference.
                child(accountId).
                child("notifications").
                push().
                setValue(notification);
    }

    private static String createTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        Date currentTime = Calendar.getInstance().getTime();

        return simpleDateFormat.format(currentTime);
    }

    private ArrayList<String> getContactsIds(){
        DatabaseReference databaseReference = FirebaseConnector.getFirebase().
                child("contacts").
                child(accountId);

        ArrayList<String> myContactsId = new ArrayList<>();
        ValueEventListener valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Contact contact = data.getValue(Contact.class);
                    myContactsId.add(Objects.requireNonNull(contact).getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListenerMyContacts);
        return myContactsId;
    }
}
