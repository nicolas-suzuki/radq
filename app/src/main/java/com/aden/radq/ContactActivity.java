package com.aden.radq;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class ContactActivity extends AppCompatActivity {
    public static final String CONTACT_NAME = "contactName";
    EditText contactName;

    EditText contactPassword;

    public static final String CONTACT_EMAIL = "contactEmail";
    EditText contactEmail;

    private Contact contact;

    private Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_activity);

        contactName = findViewById(R.id.contactNameEditTxt);
        contactEmail = findViewById(R.id.contactEmailEditTxt);
        contactPassword = findViewById(R.id.contactPasswordEdit);
        Button bttnSaveSettings = findViewById(R.id.bttnSaveContact);

        //Firebase
        DatabaseReference databaseReference = FirebaseHelper.getFirebase();

        mySnackbar = Snackbar.make(findViewById(R.id.contactView), R.string.contact_created, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        bttnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact = new Contact();
                contact.setName(contactName.getText().toString());
                contact.setEmail(contactEmail.getText().toString());
                contact.setPassword(contactPassword.getText().toString());
                setContact();
//              saveData();
            }
        });

//        loadData();
//        updateData();
    }

//    private void saveData(){
//        Log.d("settingsData", "saveData()");
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(CONTACT_NAME,contactName.getText().toString());
//        editor.putString(CONTACT_EMAIL,contactEmail.getText().toString());
//        //TODO validations
//        editor.apply();
//
//        contact = new Contact();
//        contact.setName(contactName.getText().toString());
//        contact.setEmail(contactEmail.getText().toString());
//        contact.setPassword(contactPassword.getText().toString());
//        setContact();
//    }
//
//    private void loadData(){
//        Log.d("settingsData", "loadData()");
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
//        getContactEmail = sharedPreferences.getString(CONTACT_EMAIL,"");
//        getContactName = sharedPreferences.getString(CONTACT_NAME,"");
//    }
//
//    private void updateData(){
//        Log.d("settingsData", "updateData()");
//        contactName.setText(getContactName);
//        contactEmail.setText(getContactEmail);
//    }

    private void setContact(){
        //contact's information can be null
        FirebaseAuth firebaseAuth = FirebaseHelper.getFirebaseAuth();
        firebaseAuth.createUserWithEmailAndPassword(
                contact.getEmail(),
                contact.getPassword()
        ).addOnCompleteListener(ContactActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mySnackbar.show();
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    contact.setId(firebaseUser.getUid());
                    contact.saveContact();
                } else {
                    String exceptionError = "";

                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthWeakPasswordException e) {
                        exceptionError = getString(R.string.invalid_password);
                        alertDialogBox(exceptionError);

                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        exceptionError = getString(R.string.invalid_email);
                        alertDialogBox(exceptionError);
                    } catch (FirebaseAuthUserCollisionException e) {
                        exceptionError = getString(R.string.email_already_in_use);
                        alertDialogBox(exceptionError);
                    } catch (Exception e) {
                        exceptionError = getString(R.string.unkown_error_contact_register);
                        alertDialogBox(exceptionError);
                    }
                }
            }
        });
    }

    //Dialog box to warn the user about not defining a contact in the application settings
    private void alertDialogBox(String e){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(e);
        dialog.setTitle(getString(R.string.default_alert_dialog_title));
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}
