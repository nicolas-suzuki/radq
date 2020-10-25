package com.aden.radqcompanionapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStorage {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static final String IDENTIFIER_KEY = "loggedUserID";

    @SuppressLint("CommitPrefEdits")
    public SettingsStorage(Context parameterContext){
        int MODE = 0;
        String ARCHIVE = "radqcompanionapp.preferences";
        sharedPreferences = parameterContext.getSharedPreferences(ARCHIVE, MODE);
        editor = sharedPreferences.edit(); //opens the editor. commit will be called within set(s)
    }

    public void setIdentifierKey(String contactID){
        editor.putString(IDENTIFIER_KEY, contactID);
        editor.commit();
    }

    public String getIdentifierKey(){
        return sharedPreferences.getString(IDENTIFIER_KEY,"");
    }
}
