package com.aden.radq.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {
    private static final String TAG = "Settings";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private final String IDENTIFIER_KEY = "loggedUserID";
    private final String SWITCH_CAMERA_FRONT_BACK = "switchCameraFrontBack";

    @SuppressLint("CommitPrefEdits")
    public Settings(Context parameterContext){
        Log.d(TAG, "Settings accessed.");
        int MODE = 0;
        String ARCHIVE = "radq.preferences";
        sharedPreferences = parameterContext.getSharedPreferences(ARCHIVE, MODE);
        editor = sharedPreferences.edit(); //opens the editor. commit will be called within set(s)
    }

    public void setIdentifierKey(String contactID){
        Log.d(TAG, "Settings saved.");
        editor.putString(IDENTIFIER_KEY, contactID);
        editor.commit();
    }

    public String getIdentifierKey(){
        return sharedPreferences.getString(IDENTIFIER_KEY,"");
    }

    public void setSwitchCameraFrontBack(Boolean switchCameraFrontBack){
        Log.d(TAG, "Settings saved.");
        editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, switchCameraFrontBack);
        editor.commit();
    }

    public boolean getSwitchCameraFrontBack(){
        return sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK,false);
    }
}
