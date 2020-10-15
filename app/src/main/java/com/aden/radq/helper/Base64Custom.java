package com.aden.radq.helper;

import android.util.Base64;
import android.util.Log;

public class Base64Custom {
    private static final String TAG = "Base64Custom";

    public static String encodeBase64(String textToEncode){
        Log.d(TAG, "encodeBase64");
        return Base64.encodeToString(textToEncode.getBytes(),Base64.DEFAULT).
                replaceAll("([\\n\\r])","");
    }

    public static String decodeBase64(String encodedText){
        Log.d(TAG, "decodeBase64");
        return new String (Base64.decode(encodedText,Base64.DEFAULT));
    }
}
