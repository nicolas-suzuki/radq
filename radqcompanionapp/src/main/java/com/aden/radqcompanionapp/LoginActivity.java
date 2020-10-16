package com.aden.radqcompanionapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.aden.radqcompanionapp.helper.Base64Custom;
import com.aden.radqcompanionapp.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "ContactLoginActivity";

    private EditText etAccountEmail;
    private EditText etAccountPassword;
    private Button btAccountLogin;
    private LinearLayout llPassword;
    private LinearLayout llLoginFields;

    private Settings settings;

    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        etAccountEmail = findViewById(R.id.etAccountEmail);
        etAccountPassword = findViewById(R.id.etAccountPassword);
        btAccountLogin = findViewById(R.id.btAccountLogin);
        Button btCreateAccount = findViewById(R.id.btCreateAccount);
        llPassword = findViewById(R.id.llPassword);
        llLoginFields = findViewById(R.id.llLoginFields);

        //Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        if (firebaseAuth.getCurrentUser() != null){
            setViewsAsConnected();
        }

        //Load application settings
        settings = new Settings(LoginActivity.this);
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());

        btAccountLogin.setOnClickListener(v -> {
            closeKeyboard();
            if(!isUserConnected()){
                if(etAccountEmail.getText().toString().isEmpty()){
                    Log.d(TAG,getString(R.string.error_empty_email_field));
                    showSnackbar(getString(R.string.error_empty_email_field));
                } else if(etAccountPassword.getText().toString().isEmpty()) {
                    Log.d(TAG,getString(R.string.error_empty_password_field));
                    showSnackbar(getString(R.string.error_empty_password_field));
                } else {
                    validateLogin();
                }
            } else {
                validateLogout();
            }
        });

        btCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateAccountActivity();
            }
        });
    }

    private boolean isUserConnected() {
        Log.d(TAG,"isUserConnected()");
        return firebaseAuth.getCurrentUser() != null;
    }

    private void validateLogout(){
        Log.d(TAG,"validateLogout()");
        firebaseAuth.signOut();
        showSnackbar(getString(R.string.logged_out));

        settings.setIdentifierKey("");
        etAccountEmail.setEnabled(true);

        ((ViewGroup) llLoginFields).addView(llPassword);
        btAccountLogin.setText(getText(R.string.login_button));
    }

    private void validateLogin() {
        Log.d(TAG, "validateLogin()");
        firebaseAuth.signInWithEmailAndPassword(
                etAccountEmail.getText().toString(),
                etAccountPassword.getText().toString()
        ).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                setViewsAsConnected();

                String accountIdentifier = Base64Custom.encodeBase64(etAccountEmail.getText().toString());
                settings.setIdentifierKey(accountIdentifier);

                showSnackbar(getString(R.string.logged_in));
            } else {
                showSnackbar(getString(R.string.unknown_error_logging_in));
            }
        });
    }

    private void setViewsAsConnected(){
        Log.d(TAG,"setViewsAsConnected()");
        etAccountEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        etAccountEmail.setEnabled(false);

        ((ViewGroup) llPassword.getParent()).removeView(llPassword);

        btAccountLogin.setText(getText(R.string.logout_button));
    }

    private void openCreateAccountActivity(){
        if(isUserConnected()){
            alertDialogBox(getString(R.string.disconnect_before_proceed));
        } else {
            Log.d(TAG,"openCreateAccountActivity()");
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        }
    }

    private void closeKeyboard(){
        Log.d(TAG,"closeKeyboard()");
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void showSnackbar(String message){
        Log.d(TAG,"showSnackbar()");
        Snackbar.make(findViewById(R.id.clLoginActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }

    private void alertDialogBox(String e){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.default_alert_dialog_title));
        dialog.setMessage(e);
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}
