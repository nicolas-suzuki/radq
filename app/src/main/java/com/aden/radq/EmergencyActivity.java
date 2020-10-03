package com.aden.radq;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class EmergencyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_activity);
        final ConstraintLayout currentLayout = findViewById(R.id.emergencyLayout);
        final TextView textView = findViewById(R.id.emergencyContactWillBeContactedTxt);
        final String test = (String) textView.getText();
        Log.d("timer", "Timer Started");

        new CountDownTimer(30000,1000){
            boolean tick = true;
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("timer","tick");

                String test2 = test + millisUntilFinished + "seconds";
                textView.setText(test2);
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

            }
        }.start();
    }
}
