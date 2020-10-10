package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.helper.Contact;
import com.aden.radq.helper.FirebaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "ContactLoginActivity";

    private EditText contactEmail;
    private EditText contactPassword;
    private Contact contact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        contactEmail = findViewById(R.id.contactEmailEditTxt);
        contactPassword = findViewById(R.id.contactPasswordEditText);
        Button buttonLogin = findViewById(R.id.contactLoginButton);
        Button buttonCreateAccount = findViewById(R.id.createAccountButton);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact = new Contact();
                if(contactEmail.getText().toString().isEmpty()){
                    Log.d(TAG,"Email Vazio");
                    Snackbar.make(findViewById(R.id.LoginActivity), "Email vazio", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else if(contactPassword.getText().toString().isEmpty()) {
                    Log.d(TAG,"Senha Vazia");
                    Snackbar.make(findViewById(R.id.LoginActivity), "Senha vazia", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                } else {
                    contact.setEmail(contactEmail.getText().toString());
                    contact.setPassword(contactPassword.getText().toString());

                    validateLogin();
                }
            }
        });

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void validateLogin() {
        Log.d(TAG, "ValidateLogin()");
        FirebaseAuth firebaseAuth = FirebaseHelper.getFirebaseAuth();
        firebaseAuth.signInWithEmailAndPassword(
                contact.getEmail(),
                contact.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Snackbar.make(findViewById(R.id.LoginActivity), "Sucesso", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
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
