package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        workOnAdditionalFiles();

        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    //This method guarantees that the files in the /res/raw folder an extracted to the ExternalStorage
    //ready for the app to consume
    //TODO improve code
    private void workOnAdditionalFiles(){
        boolean isCfgHere = false;
        boolean isWeightsHere = false;
        Log.d(TAG, "workOnAdditionalFiles()");

        String path = Objects.requireNonNull(getExternalFilesDir(null)).toString() + "/";

        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : files){
            if (file.getName().equals("yolov3-tiny.cfg")){
                Log.d(TAG, "Configuration file here!");
                isCfgHere = true;
            } else if (file.getName().equals("yolov3-tiny.weights")){
                Log.d(TAG, "Weights file here!");
                isWeightsHere = true;
            }
        }

        if (!isCfgHere){
            File cfgFile = new File(path + "yolov3-tiny.cfg");
            try{
                Log.d(TAG, "isCfgHere try()");
                InputStream inputStream = this.getResources().openRawResource(R.raw.yolov3_tiny_cfg);
                FileOutputStream fileOutputStream = new FileOutputStream(cfgFile);
                byte[] buf = new byte[1024];
                int len;
                while((len=inputStream.read(buf))>0){
                    fileOutputStream.write(buf,0,len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.d(TAG, "isCfgHere catch(): " + e);
                e.printStackTrace();
            }
        }

        if (!isWeightsHere){
            File weightsFile = new File(path + "yolov3-tiny.weights");
            try{
                Log.d(TAG, "isWeightsHere try()");
                InputStream inputStream = this.getResources().openRawResource(R.raw.yolov3_tiny_weights);
                FileOutputStream fileOutputStream = new FileOutputStream(weightsFile);
                byte[] buf = new byte[1024];
                int len;
                while((len=inputStream.read(buf))>0){
                    fileOutputStream.write(buf,0,len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.d(TAG, "isWeightsHere catch(): " + e);
                e.printStackTrace();
            }
        }
    }
}
