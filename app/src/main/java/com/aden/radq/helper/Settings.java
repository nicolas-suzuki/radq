package com.aden.radq.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    //Settings values
    private static final String IDENTIFIER_KEY = "loggedUserID";
    private static final String SWITCH_CAMERA_FRONT_BACK = "switchCameraFrontBack";
    private static final String ROBOT_INSTRUCTIONS = "switchRobotInstructions";

    //ALL SETTERS MUST HAVE A "editor.commit()" AT THE END OF THE METHOD
    @SuppressLint("CommitPrefEdits")
    public Settings(Context parameterContext){
        int MODE = 0;
        String ARCHIVE = "radq.preferences";
        sharedPreferences = parameterContext.getSharedPreferences(ARCHIVE, MODE);
        editor = sharedPreferences.edit(); //opens the editor. commit will be called within set(s)
    }

    public final void setIdentifierKey(String contactID){
        editor.putString(IDENTIFIER_KEY, contactID);
        editor.commit();
    }

    public final String getIdentifierKey(){
        return sharedPreferences.getString(IDENTIFIER_KEY,"");
    }

    public final void setSwitchCameraFrontBack(Boolean switchCameraFrontBack){
        editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, switchCameraFrontBack);
        editor.commit();
    }

    public final boolean getSwitchCameraFrontBack(){
        return sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK,false);
    }

    public final void setRobotInstructions(Boolean switchRobotInstructions){
        editor.putBoolean(ROBOT_INSTRUCTIONS, switchRobotInstructions);
        editor.commit();
    }

    public final boolean getRobotInstructions(){
        return sharedPreferences.getBoolean(ROBOT_INSTRUCTIONS,false);
    }
}
