package com.aden.radq;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Base64Custom;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Account;
import com.aden.radq.model.Contact;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";

    private EditText etContactEmail;

    private String contactIdentifier;
    private DatabaseReference databaseReference;

    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        Button btAddContact = findViewById(R.id.btSaveCreateAccount);
        etContactEmail = findViewById(R.id.etCreateAccountEmail);

        //Start settings class to get stored pref data
        settings = new Settings(AddContactActivity.this);

        btAddContact.setOnClickListener(v -> {
            if(etContactEmail.getText().toString().isEmpty()){
                Log.d(TAG, getString(R.string.error_no_email));
                showSnackbar(getString(R.string.error_no_email));
            } else {
                contactIdentifier = Base64Custom.encodeBase64(etContactEmail.getText().toString());

                databaseReference = FirebaseConnector.getFirebase().
                        child("accounts").
                        child(contactIdentifier);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            //Get contact data
                            Account contactAccount = snapshot.getValue(Account.class);

                            //Get current logged account
                            String loggedUserID = settings.getIdentifierKey();
                            Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ loggedUserID);

                            Contact contact = new Contact();
                            contact.setId(contactIdentifier);
                            contact.setEmail(contactAccount.getEmail());
                            contact.setName(contactAccount.getName());

                            databaseReference = FirebaseConnector.getFirebase().
                                    child("contacts").
                                    child(loggedUserID).
                                    child(contactIdentifier);

                            databaseReference.setValue(contact).addOnCompleteListener(AddContactActivity.this, task -> {
                                if(task.isSuccessful()){
                                    Log.d(TAG, getString(R.string.contact_added_successfully));
                                    showSnackbar(getString(R.string.contact_added_successfully));
                                } else {
                                    Log.d(TAG, getString(R.string.unknown_error_adding_contact));
                                    showSnackbar(getString(R.string.unknown_error_adding_contact));
                                }
                            });
                        } else {
                            Log.d(TAG, getString(R.string.error_contact_not_found));
                            showSnackbar(getString(R.string.error_contact_not_found));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clAddContactActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}