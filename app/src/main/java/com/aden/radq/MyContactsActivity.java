package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Contact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyContactsActivity extends AppCompatActivity {
    private static final String TAG = "MyContactsActivity";

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> myContacts;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerMyContacts;

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerMyContacts);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerMyContacts);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_contacts_activity);

        Button btAddContact = (Button) findViewById(R.id.btAddContact);
        ListView lvContacts = (ListView) findViewById(R.id.lvContacts);

        //Load settings
        Settings settings = new Settings(MyContactsActivity.this);
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());
        String ID = settings.getIdentifierKey();

        myContacts = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(
                MyContactsActivity.this,
                android.R.layout.simple_list_item_1,
                myContacts
        );
        lvContacts.setAdapter(arrayAdapter);

        databaseReference = FirebaseConnector.getFirebase().
                child("contacts").
                child(ID);

        valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myContacts.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    Contact contact = data.getValue(Contact.class);
                    myContacts.add(contact.getName());
                }
                Log.d(TAG, "MyContacts.size(): " + myContacts.size());
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
