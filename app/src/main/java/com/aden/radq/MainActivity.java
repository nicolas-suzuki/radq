package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);
        new CountDownTimer(3000,100){
            public void onTick(long millisUntilFinished) {

            }
            public  void onFinish(){
                setContentView(R.layout.main_activity);
                ImageButton bttnCamera = (ImageButton) findViewById(R.id.bttnCamera);
                bttnCamera.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        openCameraActivity();
                    }
                });

                ImageButton bttnAlarms = (ImageButton) findViewById(R.id.bttnAlarms);
                bttnAlarms.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        openAlarmsActivity();
                    }
                });

                ImageButton bttnNotifications = (ImageButton) findViewById(R.id.bttnNotifications);
                bttnNotifications.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        openNotificationsActivity();
                    }
                });

                ImageButton bttnSettings = (ImageButton) findViewById(R.id.bttnSettings);
                bttnSettings.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        openSettingsActivity();
                    }
                });





            }
        }.start();



    }

    public void openCameraActivity(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void openAlarmsActivity(){
        Intent intent = new Intent(this, AlarmsActivity.class);
        startActivity(intent);
    }

    public void openNotificationsActivity(){
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
