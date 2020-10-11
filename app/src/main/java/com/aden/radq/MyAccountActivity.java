package com.aden.radq;

import android.content.Intent;
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

    private Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account_activity);

        etAccountEmail = findViewById(R.id.etAccountEmail);
        etAccountPassword = findViewById(R.id.etAccountPassword);
        btAccountLogin = findViewById(R.id.btAccountLogin);
        btCreateAccount = findViewById(R.id.btCreateAccount);

        isUserConnected();

        btAccountLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isUserConnected()){
                    account = new Account();
                    if(etAccountEmail.getText().toString().isEmpty()){
                        Log.d(TAG,"Email Vazio");
                        Snackbar.make(findViewById(R.id.LoginActivity), "Email vazio", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    } else if(etAccountPassword.getText().toString().isEmpty()) {
                        Log.d(TAG,"Senha Vazia");
                        Snackbar.make(findViewById(R.id.LoginActivity), "Senha vazia", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    } else {
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
        FirebaseAuth firebaseAuth = FirebaseConnector.getFirebaseAuth();
        if(firebaseAuth.getCurrentUser() != null){
            etAccountEmail.setText(firebaseAuth.getCurrentUser().getEmail());
            etAccountEmail.setEnabled(false);
            etAccountPassword.setEnabled(false);
            btAccountLogin.setText(getText(R.string.logout_button));
            return true;
        }
        return false;
    }

    private void validateLogout(){
        Log.d(TAG,"ValidateLogout()");
        FirebaseAuth firebaseAuth = FirebaseConnector.getFirebaseAuth();
        firebaseAuth.signOut();
        Snackbar.make(findViewById(R.id.LoginActivity), "Logged out", Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
        etAccountEmail.setEnabled(true);
        etAccountPassword.setEnabled(true);
        btAccountLogin.setText(getText(R.string.login_button));
    }

    private void validateLogin() {
        Log.d(TAG, "ValidateLogin()");
        FirebaseAuth firebaseAuth = FirebaseConnector.getFirebaseAuth();
        firebaseAuth.signInWithEmailAndPassword(
                account.getEmail(),
                account.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Snackbar.make(findViewById(R.id.LoginActivity), "Sucesso", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    etAccountEmail.setEnabled(false);
                    etAccountPassword.setEnabled(false);
                    btAccountLogin.setText(getText(R.string.logout_button));

                    Settings settings = new Settings(MyAccountActivity.this);
                    String accountIdentifier = Base64Custom.encodeBase64(account.getEmail());
                    settings.saveData(accountIdentifier);

                } else {
                    Snackbar.make(findViewById(R.id.LoginActivity), "Erro", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });
    }

    private void createAccount(){
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }
}
