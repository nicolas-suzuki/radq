package com.aden.radq.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.aden.radq.R;

import java.util.Objects;

public class AlarmSound {
    private SoundPool soundAlarmPool;
    private final Context context;

    public AlarmSound(Context context) {
        this.context = context;
    }

    public void play(){
        setVolumeLevel();

        int alarmSound; //add settings
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundAlarmPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build();

        alarmSound = soundAlarmPool.load(context, R.raw.alarm_sound, 1);

        soundAlarmPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(alarmSound,1,1,0,-1,1));
    }

    public void stop(){
        if(soundAlarmPool != null){
            soundAlarmPool.release();
            soundAlarmPool = null;
        }
    }

    private void setVolumeLevel(){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Objects.requireNonNull(audioManager).setStreamVolume(AudioManager.STREAM_ALARM,audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
    }
}
