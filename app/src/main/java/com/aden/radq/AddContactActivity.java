package com.aden.radq;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";

    private Button btAddContact;
    private EditText etContactEmail;

    private String contactIdentifier;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        btAddContact = findViewById(R.id.btAddContact);
        etContactEmail = findViewById(R.id.etAccountEmail);

        btAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etContactEmail.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.clAddContactActivity), "ERRO. Sem Email", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else {
                    String contactEmail = etContactEmail.getText().toString();
                    contactIdentifier = Base64Custom.encodeBase64(contactEmail);

                    databaseReference = FirebaseConnector.getFirebase().child("accounts").child(contactIdentifier);

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){
                                firebaseAuth.getCurrentUser().getEmail();

                                //Get contact data
                                Account contactAccount = snapshot.getValue(Account.class);

                                //Get current logged account
                                Settings settings = new Settings(AddContactActivity.this);
                                String loggedUserID = settings.getIdentifier();

                                Contact contact = new Contact();
                                contact.setContactIdentifier(contactIdentifier);
                                contact.setEmail(contactAccount.getEmail());
                                contact.setName(contactAccount.getName());

                                databaseReference = FirebaseConnector.getFirebase().
                                        child("contacts").
                                        child(loggedUserID).
                                        child(contactIdentifier);

                                databaseReference.setValue(contact);
                            } else {
                                Log.d(TAG, "Nao cadastrado");
                                Snackbar.make(findViewById(R.id.clAddContactActivity), "Não cadastrado.", Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }
}