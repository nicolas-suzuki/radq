package com.aden.radq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Base64Custom;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Account;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    //Views
    private EditText etCreateAccountName;
    private EditText etCreateAccountPassword;
    private EditText etCreateAccountEmail;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private Settings settings;
    private Account account;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);

        //Initialize views
        etCreateAccountName = findViewById(R.id.etCreateAccountName);
        etCreateAccountEmail = findViewById(R.id.etCreateAccountEmail);
        etCreateAccountPassword = findViewById(R.id.etCreateAccountPassword);
        Button btSaveCreateAccount = findViewById(R.id.btSaveCreateAccount);

        //Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        databaseReference = FirebaseConnector.getFirebase();

        //Get application settings
        settings = new Settings(CreateAccountActivity.this);

        btSaveCreateAccount.setOnClickListener(v -> {
            if(etCreateAccountName.getText().toString().isEmpty()){
                showSnackbar(getString(R.string.error_empty_name_field));
            } else if (etCreateAccountEmail.getText().toString().isEmpty()){
                showSnackbar(getString(R.string.snackbar_error_empty_email_field));
            } else if (etCreateAccountPassword.getText().toString().isEmpty()){
                showSnackbar(getString(R.string.snackbar_error_empty_password_field));
            } else {
                createAccount();
            }
        });
    }

    private void createAccount(){
        firebaseAuth.createUserWithEmailAndPassword(
                etCreateAccountEmail.getText().toString(),
                etCreateAccountPassword.getText().toString()
        ).addOnCompleteListener(CreateAccountActivity.this, task -> {
            if (task.isSuccessful()) {
                account = new Account();
                account.setName(etCreateAccountName.getText().toString());
                account.setEmail(etCreateAccountEmail.getText().toString());
                account.setPassword(etCreateAccountPassword.getText().toString());
                String accountIdentifier = Base64Custom.encodeBase64(account.getEmail());
                account.setId(accountIdentifier);

                //Add account to Firebase Database
                databaseReference.child("accounts").child(account.getId()).setValue(account);

                //Add current user key to Settings
                settings.setIdentifierKey(accountIdentifier);

                showSnackbar(getString(R.string.account_created));
            } else {
                String exceptionError;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    exceptionError = getString(R.string.invalid_password);
                    alertDialogBox(exceptionError);
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    exceptionError = getString(R.string.dialog_message_invalid_email);
                    alertDialogBox(exceptionError);
                } catch (FirebaseAuthUserCollisionException e) {
                    exceptionError = getString(R.string.email_already_in_use);
                    alertDialogBox(exceptionError);
                } catch (Exception e) {
                    exceptionError = getString(R.string.unknown_error_contact_register);
                    alertDialogBox(exceptionError);
                }
            }
        });
    }

    private void alertDialogBox(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(CreateAccountActivity.this);
        dialog.setTitle(getString(R.string.dialog_title_alert));
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clCreateContact), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}
