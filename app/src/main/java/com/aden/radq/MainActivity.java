package com.aden.radq;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aden.radq.helper.UsbService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_CODE = 1;
    String contactEmail;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        workOnAdditionalFiles();

        setContentView(R.layout.main_activity);

        ImageButton bttnCamera = findViewById(R.id.bttnCamera);
        bttnCamera.setOnClickListener(v -> openCameraActivity());

        ImageButton bttnAlarms = findViewById(R.id.bttnAlarms);
        bttnAlarms.setOnClickListener(v -> openAlarmsActivity());

        ImageButton bttnNotifications = findViewById(R.id.bttnNotifications);
        bttnNotifications.setOnClickListener(v -> openNotificationsActivity());

        ImageButton bttnSettings = findViewById(R.id.bttnSettings);
        bttnSettings.setOnClickListener(v -> openSettingsActivity());
    }

    public void openCameraActivity(){
        //Get the saved preferences and check if there's a contact registered
        //if not, it won't start the CameraActivity and will show up a message
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        contactEmail = sharedPreferences.getString("contactEmail","aaaaaaaaa");
        Log.d("contactEmail", "Contact Email: " + contactEmail);
        if(contactEmail.isEmpty()) {
            alertDialogBox();
        } else {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
    }

    public void openAlarmsActivity(){
        Intent intent = new Intent(this, AlarmsActivity.class);
        startActivity(intent);
    }

    public void openNotificationsActivity(){
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void checkPermission(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        },PERMISSIONS_CODE);
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
        assert files != null;
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

    //Dialog box to warn the user about not defining a contact in the application settings
    private void alertDialogBox(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.contact_alert_dialog_message));
        dialog.setTitle(getString(R.string.contact_alert_dialog_title));
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    // USB Connection + Control Classes Section //
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
