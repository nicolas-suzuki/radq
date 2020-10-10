package com.aden.radq;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.helper.Base64CustomHelper;
import com.aden.radq.helper.FirebaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AddContactActivity extends AppCompatActivity {
    private Button buttonSaveContact;
    private EditText editTextContactEmail;
    private String contactIdentifier;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseHelper.getFirebaseAuth();

        buttonSaveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextContactEmail.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.clSettings), "ERRO. Sem Email", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    String contactEmail = editTextContactEmail.getText().toString();
                    contactIdentifier = Base64CustomHelper.encodeBase64(contactEmail);

                    databaseReference = FirebaseHelper.getFirebase().child("accounts").child(contactIdentifier);

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.getValue() != null){

                                firebaseAuth.getCurrentUser().getEmail();

                                //databaseReference = FirebaseHelper.getFirebase().child("accounts").child();
                            } else {
                                Snackbar.make(findViewById(R.id.clSettings), "Não cadastrado.", Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));
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
