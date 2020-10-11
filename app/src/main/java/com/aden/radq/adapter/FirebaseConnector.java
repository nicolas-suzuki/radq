package com.aden.radq.adapter;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class FirebaseConnector {
    private static final String TAG = "FirebaseHelper";

    private static DatabaseReference databaseReference;
    private static FirebaseAuth firebaseAuth;

    public static DatabaseReference getFirebase() {
        Log.d(TAG, "getFirebase()");
        if(databaseReference == null){
            Log.d(TAG, "databaseReference == null");
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    public static FirebaseAuth getFirebaseAuth(){
        Log.d(TAG, "getFirebaseAuth()");
        if(firebaseAuth == null){
            Log.d(TAG, "firebaseAuth == null");
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }
}
