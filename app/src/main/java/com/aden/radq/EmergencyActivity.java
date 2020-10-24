package com.aden.radq;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aden.radq.utils.AlarmSound;
import com.aden.radq.utils.NotificationSender;
import com.aden.radq.utils.SettingsStorage;

import java.util.concurrent.TimeUnit;

public class EmergencyActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;

    //Views
    private Button btImNotOkay;
    private ConstraintLayout clEmergency;
    private LinearLayout llButtons;
    private TextView tvContactWillBeContacted;
    private TextView tvEmergencyTitle;
    private Space spaceEmergency;

    //Play Alarm Sound
    private AlarmSound alarmSound;

    private NotificationSender notificationSender;

    @Override
    protected final void onStop() {
        super.onStop();
        countDownTimer.cancel();
        alarmSound.stop();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        alarmSound.stop();
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
        SettingsStorage settingsStorage = new SettingsStorage(EmergencyActivity.this);
        String accountId = settingsStorage.getIdentifierKey();

        //Initialize object NotificationSender
        notificationSender = new NotificationSender(accountId);

        //Play Alarm Sound
        alarmSound = new AlarmSound(this);
        alarmSound.play();

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
                notificationSender.send("YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg");
                contactContacted();
            }
        }.start();

        btImOkay.setOnClickListener(v -> {
            //OKAY Button pressed
            countDownTimer.cancel();
            notificationSender.send("aW1va2F5YnV0dG9ucHJlc3NlZA");
            finish();
        });

        btImNotOkay.setOnClickListener(v -> {
            //NOT okay Button pressed
            countDownTimer.cancel();
            notificationSender.send("aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA");
            contactContacted();
        });
    }

    private void contactContacted(){
        ((ViewGroup) llButtons.getParent()).removeView(llButtons);
        ((ViewGroup) spaceEmergency.getParent()).removeView(spaceEmergency);
        ((ViewGroup) tvContactWillBeContacted.getParent()).removeView(tvContactWillBeContacted);

        tvEmergencyTitle.setText(getResources().getString(R.string.contacts_contacted));
        clEmergency.setBackgroundColor(Color.GREEN);
    }
}
