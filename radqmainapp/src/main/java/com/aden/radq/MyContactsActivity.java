package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.model.Contact;
import com.aden.radq.utils.FirebaseConnector;
import com.aden.radq.utils.SettingsStorage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyContactsActivity extends AppCompatActivity {

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> myContacts;

    //Firebase
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerMyContacts;

    @Override
    protected final void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerMyContacts);
    }

    @Override
    protected final void onStop() {
        super.onStop();
        if(valueEventListenerMyContacts != null) {
            databaseReference.removeEventListener(valueEventListenerMyContacts);
        }
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_contacts_activity);

        //Initialize views
        Button btAddContact = findViewById(R.id.btAddContact);
        ListView lvContacts = findViewById(R.id.lvContacts);

        //Load settings
        SettingsStorage settingsStorage = new SettingsStorage(MyContactsActivity.this);
        String accountId = settingsStorage.getIdentifierKey();

        myContacts = new ArrayList<>(5);
        arrayAdapter = new ArrayAdapter<>(
                MyContactsActivity.this,
                android.R.layout.simple_list_item_1,
                myContacts
        );
        lvContacts.setAdapter(arrayAdapter);

        databaseReference = FirebaseConnector.getFirebase().
                child("contacts").
                child(accountId);

        valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myContacts.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    Contact contact = data.getValue(Contact.class);
                    myContacts.add(Objects.requireNonNull(contact).getName());
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListenerMyContacts);

        btAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MyContactsActivity.this, AddContactActivity.class);
            startActivity(intent);
        });
    }
}
