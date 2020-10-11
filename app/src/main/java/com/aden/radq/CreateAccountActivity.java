package com.aden.radq;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Base64Custom;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "ContactActivity";

    public static final String CONTACT_NAME = "contactName";
    private EditText etContactName;

    private EditText etContactPassword;

    public static final String ACCOUNT_IDENTIFIER = "accountID";
    private EditText etContactEmail;

    private Account account;

    private Button btSaveContact;

    private Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);

        etContactName = findViewById(R.id.etContactName);
        etContactEmail = findViewById(R.id.etAccountEmail);
        etContactPassword = findViewById(R.id.etContactPassword);
        btSaveContact = findViewById(R.id.btAddContact);

        //Firebase
        FirebaseConnector.getFirebase();

        mySnackbar = Snackbar.make(findViewById(R.id.clCreateContact), R.string.contact_created, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        btSaveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                account = new Account();
                if(etContactName.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.clCreateContact), "Nome vazio", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else if (etContactEmail.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.clCreateContact), "Email vazio", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else if (etContactPassword.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.clCreateContact), "Senha Vazia", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else {
                    account.setName(etContactName.getText().toString());
                    account.setEmail(etContactEmail.getText().toString());
                    account.setPassword(etContactPassword.getText().toString());
                    setContact();
//              saveData();
                }
            }
        });

//        loadData();
//        updateData();
    }

//    private void saveData(){
//        Log.d(TAG, "saveData()");
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
//        Log.d(TAG, "loadData()");
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
        FirebaseAuth firebaseAuth = FirebaseConnector.getFirebaseAuth();
        firebaseAuth.createUserWithEmailAndPassword(
                account.getEmail(),
                account.getPassword()
        ).addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mySnackbar.show();

                    String accountIdentifier = Base64Custom.encodeBase64(account.getEmail());
                    account.setId(accountIdentifier);
                    account.saveContact();

                    Settings settings = new Settings(CreateAccountActivity.this);
                    settings.saveData(accountIdentifier);

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
