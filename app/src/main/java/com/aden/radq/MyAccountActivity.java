package com.aden.radq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public class MyAccountActivity extends AppCompatActivity {
    private static final String TAG = "ContactLoginActivity";

    private EditText etAccountEmail;
    private EditText etAccountPassword;
    private Button btAccountLogin;
    private Button btCreateAccount;
    private FirebaseAuth firebaseAuth;
    private Settings settings;

    private Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account_activity);

        etAccountEmail = findViewById(R.id.etAccountEmail);
        etAccountPassword = findViewById(R.id.etAccountPassword);
        btAccountLogin = findViewById(R.id.btAccountLogin);
        btCreateAccount = findViewById(R.id.btCreateAccount);

        //Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        if (firebaseAuth.getCurrentUser() != null){
            setViewsAsConnected();
        }

        settings = new Settings(MyAccountActivity.this);

        btAccountLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
                if(!isUserConnected()){
                    if(etAccountEmail.getText().toString().isEmpty()){
                        Log.d(TAG,getString(R.string.error_empty_email_field));
                        showSnackbar(getString(R.string.error_empty_email_field));
                    } else if(etAccountPassword.getText().toString().isEmpty()) {
                        Log.d(TAG,getString(R.string.error_empty_password_field));
                        showSnackbar(getString(R.string.error_empty_password_field));
                    } else {
                        Log.d(TAG,"new Account()");
                        account = new Account();
                        account.setEmail(etAccountEmail.getText().toString());
                        account.setPassword(etAccountPassword.getText().toString());

                        validateLogin();
                    }
                } else {
                    validateLogout();
                }
            }
        });

        btCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private boolean isUserConnected() {
        Log.d(TAG,"isUserConnected()");
        if(firebaseAuth.getCurrentUser() != null){
            return true;
        } else {
            return false;
        }
    }

    private void setViewsAsConnected(){
        etAccountEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        etAccountEmail.setEnabled(false);
        etAccountPassword.setEnabled(false);
        etAccountPassword.setActivated(false);

        btAccountLogin.setText(getText(R.string.logout_button));
    }


    private void validateLogout(){
        Log.d(TAG,"ValidateLogout()");
        firebaseAuth.signOut();
        showSnackbar(getString(R.string.logged_out));

        settings.setIdentifierKey("");
        etAccountEmail.setEnabled(true);
        etAccountPassword.setEnabled(true);
        btAccountLogin.setText(getText(R.string.login_button));
    }

    private void validateLogin() {
        Log.d(TAG, "ValidateLogin()");
        firebaseAuth.signInWithEmailAndPassword(
                account.getEmail(),
                account.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setViewsAsConnected();

                    String accountIdentifier = Base64Custom.encodeBase64(account.getEmail());
                    settings.setIdentifierKey(accountIdentifier);

                    showSnackbar(getString(R.string.logged_in));
                } else {
                    showSnackbar(getString(R.string.unknown_error_logging_in));
                }
            }
        });
    }

    private void createAccount(){
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clMyAccountActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}
