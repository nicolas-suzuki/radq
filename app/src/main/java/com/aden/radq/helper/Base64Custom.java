package com.aden.radq.helper;

import android.util.Base64;

public final class Base64Custom {

    public static String encodeBase64(String textToEncode){
        return Base64.encodeToString(textToEncode.getBytes(),Base64.DEFAULT).
                replaceAll("(\\n|\\r)",""); //"(\\n|\\r)",""
    }
//    public static String decodeBase64(String encodedText){
//        Log.d(TAG, "decodeBase64");
//        return new String (Base64.decode(encodedText,Base64.DEFAULT));
//    }
}
