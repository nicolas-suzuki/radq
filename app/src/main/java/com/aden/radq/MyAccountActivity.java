package com.aden.radq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Base64Custom;
import com.aden.radq.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class MyAccountActivity extends AppCompatActivity {
    private static final String TAG = "MyAccountActivity";

    private EditText etAccountEmail;
    private EditText etAccountPassword;
    private Button btAccountLogin;
    private FirebaseAuth firebaseAuth;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account_activity);

        etAccountEmail = findViewById(R.id.etCreateAccountEmail);
        etAccountPassword = findViewById(R.id.etAccountPassword);
        btAccountLogin = findViewById(R.id.btAccountLogin);
        Button btCreateAccount = findViewById(R.id.btCreateAccount);

        //Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        if (firebaseAuth.getCurrentUser() != null){
            setViewsAsConnected();
        }

        //Load application settings
        settings = new Settings(MyAccountActivity.this);
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

        btCreateAccount.setOnClickListener(v -> openCreateAccountActivity());
    }

    private boolean isUserConnected() {
        Log.d(TAG,"isUserConnected()");
        return firebaseAuth.getCurrentUser() != null;
    }

    private void setViewsAsConnected(){
        Log.d(TAG,"setViewsAsConnected()");
        etAccountEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        etAccountEmail.setEnabled(false);
        etAccountPassword.setEnabled(false);

        btAccountLogin.setText(getText(R.string.logout_button));
    }

    private void validateLogout(){
        Log.d(TAG,"validateLogout()");
        firebaseAuth.signOut();
        showSnackbar(getString(R.string.logged_out));

        settings.setIdentifierKey("");
        etAccountEmail.setEnabled(true);
        etAccountPassword.setEnabled(true);
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

    private void openCreateAccountActivity(){
        Log.d(TAG,"openCreateAccountActivity()");
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
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
        Snackbar.make(findViewById(R.id.clMyAccountActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}
