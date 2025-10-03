package com.aden.radq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.model.Account;
import com.aden.radq.model.Contact;
import com.aden.radq.utils.Base64CustomConverter;
import com.aden.radq.utils.FirebaseConnector;
import com.aden.radq.utils.SettingsStorage;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddContactActivity extends AppCompatActivity {
    //Views
    private EditText etContactEmail;

    String contactIdentifier;

    //Firebase
    DatabaseReference databaseReference;

    SettingsStorage settingsStorage;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        //Initialize views
        Button btAddContact = findViewById(R.id.btSaveCreateAccount);
        etContactEmail = findViewById(R.id.etCreateAccountEmail);

        //Start settings class to get stored pref data
        settingsStorage = new SettingsStorage(AddContactActivity.this);

        btAddContact.setOnClickListener(v -> {
            if(etContactEmail.getText().toString().isEmpty()){
                showSnackbar(getString(R.string.error_no_email));
            } else {
                contactIdentifier = Base64CustomConverter.encodeBase64(etContactEmail.getText().toString());

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
                            String loggedUserID = settingsStorage.getIdentifierKey();

                            Contact contact = new Contact();
                            contact.setId(contactIdentifier);
                            contact.setEmail(Objects.requireNonNull(contactAccount).getEmail());
                            contact.setName(contactAccount.getName());

                            databaseReference = FirebaseConnector.getFirebase().
                                    child("contacts").
                                    child(loggedUserID).
                                    child(contactIdentifier);

                            databaseReference.setValue(contact).addOnCompleteListener(AddContactActivity.this, task -> {
                                if(task.isSuccessful()){
                                    showSnackbar(getString(R.string.snackbar_contact_added_successfully));
                                } else {
                                    showSnackbar(getString(R.string.snackbar_unknown_error_adding_contact));
                                }
                            });
                        } else {
                            showSnackbar(getString(R.string.snackbar_error_contact_not_found));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clAddContactActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}