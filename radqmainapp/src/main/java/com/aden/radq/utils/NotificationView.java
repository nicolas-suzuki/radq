package com.aden.radq.utils;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class NotificationView extends AppCompatActivity {
    public static void showSnackbar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
