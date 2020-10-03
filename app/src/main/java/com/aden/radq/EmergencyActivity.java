package com.aden.radq;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.concurrent.TimeUnit;

public class EmergencyActivity extends AppCompatActivity {
    CountDownTimer countDownTimer;
    Button imOkay;
    Button imNotOkay;
    ConstraintLayout currentLayout;
    TextView emergencyContactWillBeContactedTxt;
    TextView emergencyTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_activity);

        //Initializing view contents
        imOkay = findViewById(R.id.imOkay);
        imNotOkay = findViewById(R.id.imNotOkay);
        currentLayout = findViewById(R.id.emergencyLayout);
        emergencyContactWillBeContactedTxt = findViewById(R.id.emergencyContactWillBeContactedTxt);
        emergencyTitle = findViewById(R.id.emergencyTitle);

        //TODO change string test name/logic
        final String test = (String) emergencyContactWillBeContactedTxt.getText();
        Log.d("timer", "Timer Started");

        countDownTimer = new CountDownTimer(30000,1000){
            boolean tick = true;
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("timer","tick");

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                String test2 = test + seconds + " seconds";
                emergencyContactWillBeContactedTxt.setText(test2);
                if(tick){
                    currentLayout.setBackgroundColor(Color.RED);
                    tick = false;
                } else {
                    currentLayout.setBackgroundColor(Color.WHITE);
                    tick = true;
                }
            }
            @Override
            public void onFinish() {
                Log.d("timer","timer finished");
                //contact
            }
        }.start();


        imOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("emergency", "I'm Okay button pressed");
                countDownTimer.cancel();
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
        imOkay.setVisibility(View.INVISIBLE);
        imNotOkay.setVisibility(View.INVISIBLE);
        emergencyContactWillBeContactedTxt.setVisibility(View.INVISIBLE);
        emergencyTitle.setText(getResources().getString(R.string.contactContact));
        currentLayout.setBackgroundColor(Color.GREEN);
    }
}
