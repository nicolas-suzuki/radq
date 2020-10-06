package com.aden.radq;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;

public class EmergencyActivity extends AppCompatActivity {
    CountDownTimer countDownTimer;
    Button imOkay;
    Button imNotOkay;
    LinearLayout emergencyLayout;
    LinearLayout layoutButtons;
    TextView emergencyContactWillBeContactedTxt;
    TextView emergencyTitle;

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

        String emergencySubtitleTxt = getString(R.string.emergency_contact_will_be_contacted);
        String secondsText = getString(R.string.secondsTxt);

        Log.d("timer", "Timer Started");
        countDownTimer = new CountDownTimer(30000,1000){
            boolean tick = true;
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("timer","tick");

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
                Log.d("timer","timer finished");
                sendMessageToContact();
            }
        }.start();
        imOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("emergency", "I'm Okay button pressed");
                countDownTimer.cancel();
                finish();
            }
        });

        imNotOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("emergency", "Not Okay button pressed");
                countDownTimer.cancel();
                //TODO
                sendMessageToContact();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    private void sendMessageToContact() {
        //TODO Send Message then:
        contactContacted();
    }

    private void contactContacted(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String contactName = sharedPreferences.getString("contactName","");
        emergencyLayout.removeView(layoutButtons);
        emergencyContactWillBeContactedTxt.setVisibility(View.INVISIBLE);
        String aux = contactName + " " + getResources().getString(R.string.contactContact);
        emergencyTitle.setText(aux);
        emergencyLayout.setBackgroundColor(Color.GREEN);
    }
}
