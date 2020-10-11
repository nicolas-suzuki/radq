package com.aden.radq;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Contact;
import com.aden.radq.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;

public class EmergencyActivity extends AppCompatActivity {
    private static final String TAG = "EmergencyActivity";

    private CountDownTimer countDownTimer;

    private Button imOkay;
    private Button imNotOkay;
    private ConstraintLayout emergencyLayout;
    private LinearLayout layoutButtons;
    private TextView emergencyContactWillBeContactedTxt;
    private TextView emergencyTitle;

    private String accountId;
    private String message;

    private ValueEventListener valueEventListenerMyContacts;
    private DatabaseReference databaseReference;

    private ArrayList<String> myContactsId;

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerMyContacts);
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerMyContacts);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Initializing view contents
        imOkay = findViewById(R.id.imOkay);
        imNotOkay = findViewById(R.id.imNotOkay);
        emergencyLayout = findViewById(R.id.emergencyLayout);
        emergencyContactWillBeContactedTxt = findViewById(R.id.emergencyContactWillBeContactedTxt);
        emergencyTitle = findViewById(R.id.emergencyTitle);
        layoutButtons = findViewById(R.id.layoutButtons);

        Settings settings = new Settings(EmergencyActivity.this);
        accountId = settings.getIdentifier();

        String emergencySubtitleTxt = getString(R.string.emergency_contact_will_be_contacted);
        String secondsText = getString(R.string.secondsTxt);

        Log.d(TAG, "Timer Started");
        countDownTimer = new CountDownTimer(30000,1000){
            boolean tick = true;
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG,"tick");

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                //TODO change string name/logic
                String aux = emergencySubtitleTxt + seconds + secondsText;
                emergencyContactWillBeContactedTxt.setText(aux);
                if(tick){
                    imNotOkay.setBackgroundColor(Color.RED);
                    imNotOkay.setTextColor(Color.WHITE);
                    tick = false;
                } else {
                    imNotOkay.setBackgroundColor(Color.WHITE);
                    imNotOkay.setTextColor(Color.RED);
                    tick = true;
                }
            }
            @Override
            public void onFinish() {
                Log.d(TAG,"timer finished");
                sendMessageToContact();
            }
        }.start();
        imOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "I'm Okay button pressed");
                message = "I'm OKay button pressed";
                countDownTimer.cancel();
                finish();
            }
        });

        imNotOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Not Okay button pressed");
                message = "I'm NOT OKay button pressed";
                countDownTimer.cancel();
                sendMessageToContact();
            }
        });

        myContactsId = new ArrayList<>();
        databaseReference = FirebaseConnector.getFirebase().
                child("contacts").child(accountId);

        valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myContactsId.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    Contact contact = data.getValue(Contact.class);
                    myContactsId.add(contact.getContactIdentifier());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    private void sendMessageToContact() {
        Notification notification = new Notification();
        notification.setUserId(accountId);
        notification.setNotification(message);

        databaseReference = FirebaseConnector.getFirebase().child("notifications");

        for (String myContactId : myContactsId) {
            databaseReference.
                    child(accountId).
                    child(myContactId).
                    push().
                    setValue(notification);
        }
        contactContacted();
    }

    private void contactContacted(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String contactName = sharedPreferences.getString("contactName","");

        ((ViewGroup) layoutButtons.getParent()).removeView(layoutButtons);
        emergencyContactWillBeContactedTxt.setVisibility(View.INVISIBLE);
        String aux = contactName + " " + getResources().getString(R.string.contactContact);
        emergencyTitle.setText(aux);
        emergencyLayout.setBackgroundColor(Color.GREEN);
    }
}
