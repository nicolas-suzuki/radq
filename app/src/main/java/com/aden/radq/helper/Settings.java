package com.aden.radq.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private Context context;
    private SharedPreferences sharedPreferences;
    private final String ARCHIVE = "radq.preferences";
    private final int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String IDENTIFIER_KEY = "loggedUserID";

    @SuppressLint("CommitPrefEdits")
    public Settings(Context parameterContext){
        context = parameterContext;
        sharedPreferences = context.getSharedPreferences(ARCHIVE,MODE);
        editor = sharedPreferences.edit();
    }

    public void saveData (String contactID){
        editor.putString(IDENTIFIER_KEY, contactID);
        editor.commit();
    }

    public String getIdentifier(){
        return sharedPreferences.getString(IDENTIFIER_KEY,null);
    }
}