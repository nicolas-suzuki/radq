package com.aden.radq;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EmergencyActivity extends AppCompatActivity {
    private static final String TAG = "EmergencyActivity";

    private CountDownTimer countDownTimer;

    private Button btImOkay;
    private Button btImNotOkay;
    private ConstraintLayout clEmergency;
    private LinearLayout llButtons;
    private TextView tvContactWillBeContacted;
    private TextView tvEmergencyTitle;

    private String accountId;
    private String message;
    private String timeStamp;

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
        btImOkay = findViewById(R.id.btImOkay);
        btImNotOkay = findViewById(R.id.btImNotOkay);
        clEmergency = findViewById(R.id.clEmergency);
        tvContactWillBeContacted = findViewById(R.id.tvContactWillBeContacted);
        tvEmergencyTitle = findViewById(R.id.tvEmergencyTitle);
        llButtons = findViewById(R.id.llButtons);

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
                tvContactWillBeContacted.setText(aux);
                if(tick){
                    btImNotOkay.setBackgroundColor(Color.RED);
                    btImNotOkay.setTextColor(Color.WHITE);
                    tick = false;
                } else {
                    btImNotOkay.setBackgroundColor(Color.WHITE);
                    btImNotOkay.setTextColor(Color.RED);
                    tick = true;
                }
            }
            @Override
            public void onFinish() {
                //TODO static message
                Log.d(TAG,"timer finished");
                message = "Button not pressed. Time's over.";

                sendMessageToContact();
                contactContacted();
            }
        }.start();

        btImOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO static message
                Log.d(TAG, "I'm Okay button pressed");
                message = "I'm OKay button pressed";

                countDownTimer.cancel();
                sendMessageToContact();
                finish();
            }
        });

        btImNotOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO static message
                Log.d(TAG, "Not Okay button pressed");
                message = "I'm NOT OKay button pressed";

                countDownTimer.cancel();
                sendMessageToContact();
                contactContacted();
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
        Log.d(TAG,"sendMessageToContact()");

        createTimeStamp();

        Notification notification = new Notification();
        notification.setUserId(accountId);
        notification.setNotification(message);
        notification.setTimestamp(timeStamp);

        databaseReference = FirebaseConnector.getFirebase().child("notifications");
        for (String myContactId : myContactsId) {
            databaseReference.
                    child(accountId).
                    child(myContactId).
                    push().
                    setValue(notification);
        }
        storeNotification();
    }

    private void contactContacted(){
        Log.d(TAG,"contactContacted()");

        ((ViewGroup) llButtons.getParent()).removeView(llButtons);
        tvContactWillBeContacted.setVisibility(View.INVISIBLE);
        tvEmergencyTitle.setText(getResources().getString(R.string.contacts_contacted));
        clEmergency.setBackgroundColor(Color.GREEN);
    }

    private void storeNotification(){
        Log.d(TAG,"storeNotification()");

        Notification notification = new Notification();
        notification.setUserId(accountId);
        notification.setNotification(message);
        notification.setTimestamp(timeStamp);

        databaseReference = FirebaseConnector.getFirebase().child("accounts");
        databaseReference.child(accountId).child("notifications").push().setValue(notification);
    }

    private void createTimeStamp(){
        Log.d(TAG,"createTimeStamp()");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss", Locale.getDefault());

        Date currentTime = Calendar.getInstance().getTime();

        timeStamp = simpleDateFormat.format(currentTime);
    }
}
