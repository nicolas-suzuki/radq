package com.aden.radq;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EmergencyActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;

    //Views
    Button btImNotOkay;
    private ConstraintLayout clEmergency;
    private LinearLayout llButtons;
    TextView tvContactWillBeContacted;
    private TextView tvEmergencyTitle;
    private Space spaceEmergency;

    private String accountId;
    ArrayList<String> myContactsId;

    //Firebase
    private ValueEventListener valueEventListenerMyContacts;
    private DatabaseReference databaseReference;

    //Play Alarm Sound
    private SoundPool soundAlarmPool;

    @Override
    protected final void onStop() {
        super.onStop();
        if(valueEventListenerMyContacts != null){
            databaseReference.removeEventListener(valueEventListenerMyContacts);
        }
        countDownTimer.cancel();
        killAlarmSound();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        killAlarmSound();
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Initializing view contents
        Button btImOkay = findViewById(R.id.btImOkay);
        btImNotOkay = findViewById(R.id.btImNotOkay);
        clEmergency = findViewById(R.id.clEmergency);
        tvContactWillBeContacted = findViewById(R.id.tvContactWillBeContacted);
        tvEmergencyTitle = findViewById(R.id.tvEmergencyTitle);
        llButtons = findViewById(R.id.llButtons);
        spaceEmergency = findViewById(R.id.spaceEmergency);

        //Settings
        Settings settings = new Settings(EmergencyActivity.this);

        accountId = settings.getIdentifierKey();

        myContactsId = new ArrayList<>();

        //Play Alarm Sound
        playAlarmSound();

        countDownTimer = new CountDownTimer(30000,1000){
            boolean tick = true;
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                String aux = getString(R.string.emergency_contact_will_be_contacted) +
                        seconds +
                        " " +
                        getString(R.string.text_seconds);
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
                //Button not pressed, time's over
                sendMessageAndStore("YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg");
                contactContacted();
            }
        }.start();

        btImOkay.setOnClickListener(v -> {
            //OKAY Button pressed
            countDownTimer.cancel();
            sendMessageAndStore("aW1va2F5YnV0dG9ucHJlc3NlZA");
            finish();
        });

        btImNotOkay.setOnClickListener(v -> {
            //NOT okay Button pressed
            countDownTimer.cancel();
            sendMessageAndStore("aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA");
            contactContacted();
        });

        databaseReference = FirebaseConnector.getFirebase().
                child("contacts").
                child(accountId);

        valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myContactsId.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    Contact contact = data.getValue(Contact.class);
                    myContactsId.add(Objects.requireNonNull(contact).getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListenerMyContacts);
    }

    void sendMessageAndStore(String message) {
        killAlarmSound();

        String timeStamp = createTimeStamp();

        Notification notification = new Notification();
        notification.setNotification(message);
        notification.setTimestamp(timeStamp);
        notification.setUserId(accountId);

        databaseReference = FirebaseConnector.getFirebase().child("notifications");
        for (String myContactId : myContactsId) {
            databaseReference.
                    child(myContactId).
                    push().
                    setValue(notification);
        }

        databaseReference = FirebaseConnector.getFirebase().child("accounts");
        databaseReference.
                child(accountId).
                child("notifications").
                push().
                setValue(notification);
    }

    void contactContacted(){
        ((ViewGroup) llButtons.getParent()).removeView(llButtons);
        ((ViewGroup) spaceEmergency.getParent()).removeView(spaceEmergency);
        ((ViewGroup) tvContactWillBeContacted.getParent()).removeView(tvContactWillBeContacted);

        tvEmergencyTitle.setText(getResources().getString(R.string.contacts_contacted));
        clEmergency.setBackgroundColor(Color.GREEN);
    }

    private String createTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        Date currentTime = Calendar.getInstance().getTime();

        return simpleDateFormat.format(currentTime);
    }

    private void playAlarmSound(){
        setVolumeLevel();

        int alarmSound; //add settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundAlarmPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundAlarmPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        }

        alarmSound = soundAlarmPool.load(EmergencyActivity.this, R.raw.alarm_sound, 1);

        soundAlarmPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(alarmSound,1,1,0,-1,1));
    }

    private void setVolumeLevel(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Objects.requireNonNull(audioManager).setStreamVolume(AudioManager.STREAM_ALARM,audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
    }

    private void killAlarmSound(){
        if(soundAlarmPool != null){
            soundAlarmPool.release();
            soundAlarmPool = null;
        }
    }
}
