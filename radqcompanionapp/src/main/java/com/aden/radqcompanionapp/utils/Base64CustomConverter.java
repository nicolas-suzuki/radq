package com.aden.radqcompanionapp.utils;

import android.util.Base64;

public class Base64CustomConverter {

    public static String encodeBase64(String textToEncode){
        return Base64.encodeToString(textToEncode.getBytes(),Base64.DEFAULT).
                replaceAll("(\\n|\\r)","");//"(\\n|\\r)",""
    }

//    public static String decodeBase64(String encodedText){
//        return new String (Base64.decode(encodedText,Base64.DEFAULT));
//    }
}
