package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.utils.SettingsStorage;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        workOnAdditionalFiles();
        createToken();

        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    //This method guarantees that the files in the /res/raw folder an extracted to the ExternalStorage
    //ready for the app to consume
    private void workOnAdditionalFiles() {
        boolean isCfgHere = false;
        boolean isWeightsHere = false;

        String path = Objects.requireNonNull(getExternalFilesDir(null)) + "/";

        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : Objects.requireNonNull(files)){
            if (file.getName().equals("yolov3-tiny.cfg")){
                isCfgHere = true;
            } else if (file.getName().equals("yolov3-tiny.weights")){
                isWeightsHere = true;
            }
        }

        if (!isCfgHere){
            File cfgFile = new File(path + "yolov3-tiny.cfg");
            Log.d("SplashScreen", "weightsFile path: " + cfgFile.getPath());
            InputStream inputStream = getResources().openRawResource(R.raw.yolov3_tiny_cfg);
            try (FileOutputStream fileOutputStream = new FileOutputStream(cfgFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e("SplashScreen", "Error loading cfg: " + e);
                showSnackbar("Error loading cfg");
            }
        }

        if (!isWeightsHere){
            File weightsFile = new File(path + "yolov3-tiny.weights");
            Log.d("SplashScreen", "weightsFile path: " + weightsFile.getPath());
            InputStream inputStream = getResources().openRawResource(R.raw.yolov3_tiny_weights);
            try (FileOutputStream fileOutputStream = new FileOutputStream(weightsFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e("SplashScreen", "Error loading weight: " + e);
                showSnackbar("Error loading weight");
            }
        }
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clSettingsActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }

    private void createToken(){
        SettingsStorage settingsStorage = new SettingsStorage(this);

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("SplashScreen", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                settingsStorage.setPhoneKey(token);
                Log.d("SplashScreen", "FCM Token: " + token);
            });

    }
}
