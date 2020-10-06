package com.aden.radq;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;
import static com.aden.radq.SettingsActivity.SWITCH_CAMERA_FRONT_BACK;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //TODO re-analyze the need of the following two variables and their logic
    boolean startYolo = false;
    boolean firstTimeYolo = true;
    //TODO change framesToConfirmFall name/logic
    int framesToConfirmFall = 0;

    //Camera connection + detection specific variables
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    Net tinyYolo;

    //USB connection + control specific variables
    private UsbService usbService;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        //USB Handler initialization
        mHandler = new MyHandler(this);

        //Camera initialization
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        //Get saved preferences: Front/Back camera
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Log.d("cameraFrontBack", "Front/Back Camera preference: " + sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false));
        if (sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false)) {
            //Use Back Camera
            Log.i("cameraFrontBack", "Using Back Camera");
            cameraBridgeViewBase.setCameraIndex(0);
        } else {
            //Use Frontal Camera
            Log.i("cameraFrontBack", "Using Frontal Camera");
            cameraBridgeViewBase.setCameraIndex(1);
        }
            setContentView(R.layout.camera_activity);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //USB Handler initialization
            mHandler = new MyHandler(this);

//            if (!editText.getText().toString().equals("")) {
//                String data = editText.getText().toString();
//                if (usbService != null) { // if UsbService was correctly binded, Send data
//                    usbService.write(data.getBytes());
//                }
//            }


            //Camera initialization
            cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCvCameraViewListener(this);

            Log.d("cameraFrontBack", "Front/Back Camera preference: " + sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false));
            if (sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false)) {
                //Use Back Camera
                Log.i("cameraFrontBack", "Using Back Camera");
                cameraBridgeViewBase.setCameraIndex(0);
            } else {
                //Use Frontal Camera
                Log.i("cameraFrontBack", "Using Frontal Camera");
                cameraBridgeViewBase.setCameraIndex(1);
            }

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                if (status == BaseLoaderCallback.SUCCESS) {
                    cameraBridgeViewBase.enableView();
                    YOLO();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onCameraState","onResume()");
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
        //TODO
        setFilters();
        startService(usbConnection); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onCameraState","onPause()");
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        //TODO
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onCameraState","onDestroy()");
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if (startYolo) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);
            tinyYolo.setInput(imageBlob);

            java.util.List<Mat> result = new java.util.ArrayList<>(2);
            List<String> outBlobNames = new java.util.ArrayList<>();
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");

            tinyYolo.forward(result, outBlobNames);

            float confThreshold = 0.5f;

            List<Integer> clsIds = new ArrayList<>();
            List<Float> confs = new ArrayList<>();
            List<Rect> rects = new ArrayList<>();

            for (int i = 0; i < result.size(); ++i) {
                Mat level = result.get(i);
                for (int j = 0; j < level.rows(); ++j) {
                    Mat row = level.row(j);
                    Mat scores = row.colRange(5, level.cols());
                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                    float confidence = (float) mm.maxVal;
                    Point classIdPoint = mm.maxLoc;
                    if (confidence > confThreshold) {
                        int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                        int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                        int width = (int) (row.get(0, 2)[0] * frame.cols());
                        int height = (int) (row.get(0, 3)[0] * frame.rows());

                        int left = centerX - width / 2;
                        int top = centerY - height / 2;

                        clsIds.add((int) classIdPoint.x);
                        confs.add(confidence);
                        rects.add(new Rect(left, top, width, height));
                        Log.d("metrics", "height: " + height);
                        //If 500, stop
                    }
                }
            }
            int ArrayLength = confs.size();

            // Adding boxes around detection
            if (ArrayLength >= 1) { //if ArrayLength == 0, maybe is because nothing was detected. for that, add an else at the end of this statement
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.2f;
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
                Rect[] boxesArray = rects.toArray(new Rect[0]);
                MatOfRect boxes = new MatOfRect(boxesArray);
                MatOfInt indices = new MatOfInt();
                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

                // Draw result boxes:
                int[] ind = indices.toArray();
                for (int idx : ind) {
                    Rect box = boxesArray[idx];
                    int idGuy = clsIds.get(idx);
                    float conf = confs.get(idx);
                    int intConf = (int) (conf * 100);

                    if (idGuy == 0) {
                        // Fall detected
                        Imgproc.putText(frame, "Queda Detectada" + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 5);

                        Log.i("detection", "Fall detected! Precision: " + intConf + "%");
                        framesToConfirmFall++;
                        if (framesToConfirmFall > 10) {
                            //This countdown ensures that the person is really down
//                            CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
//                                @Override
//                                public void onTick(long millisUntilFinished) {
//
//                                }
//                                @Override
//                                public void onFinish() {
                                    //TODO the Screenshot is not used anywhere in the code. Uncomment when it's useful
                                    //takeScreenshot(frame, intConf);
                                    initiateAlarm();
                                    framesToConfirmFall = 0;
//                                }
//                            }.start();

                        }
                    } else if (idGuy == 1) {
                        // Pessoa detectada
                        Imgproc.putText(frame, "Pessoa detectada" + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
                        Log.i("detection", "Person detected! Precision: " + intConf + "%");
                    } else {
                        Log.e("detection", "idGuy!=0||1");
                    }
                }
            }
        }
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("onCameraState","onCameraViewStarted()");
        if (startYolo) {
            String tinyYoloCfg = getExternalFilesDir(null) + "/yolov3-tiny.cfg";
            String tinyYoloWeights = getExternalFilesDir(null) + "/yolov3-tiny.weights";
            Log.i("tinyLocation2", "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
            try{
                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
            } catch (Exception e){
                Log.i("tinyLocation1", "Exception: " + e);
                Toast toast = Toast.makeText(this, "Exception: " + e , Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onCameraViewStopped() {
        Log.d("onCameraState","onCameraViewStopped()");
        startYolo = false;
    }

    public void YOLO() {
        if (!startYolo) {
            startYolo = true;
            if (firstTimeYolo) {
                firstTimeYolo = false;
                String tinyYoloCfg = getExternalFilesDir(null) + "/yolov3-tiny.cfg";
                String tinyYoloWeights = getExternalFilesDir(null) + "/yolov3-tiny.weights";

                Log.i("tinyLocation1", "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
                try{
                    tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
                } catch (Exception e){
                    Log.i("tinyLocation1", "Exception: " + e);
                    Toast toast = Toast.makeText(this, "Exception: " + e , Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        } else {
            startYolo = false;
        }
    }

    private void takeScreenshot(Mat frame, int intConf) {
        Date date = new Date();
        CharSequence now = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
        String filename = now + "_conf:" + intConf + ".jpg";

        Bitmap bitmap = null;
        FileOutputStream outputStream = null;

        File sd = new File(getExternalFilesDir(null) + "/fall_detection_images");
        boolean success = true;

        try{
            bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame,bitmap);
        } catch (CvException e){
            Log.d("save_image", Objects.requireNonNull(e.getMessage()));
        }

        if(!sd.exists()){
            success = sd.mkdir();
        }

        if (success){
            File destination = new File(sd,filename);
            try{
                outputStream = new FileOutputStream(destination);
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            } catch(Exception e){
                Log.d("save_image", Objects.requireNonNull(e.getMessage()));
            } finally {
                try{
                    if(outputStream != null){
                        outputStream.close();
                        Log.d("save_image","Saved successfully.");
                    }
                } catch (IOException e){
                    Log.d("save_image","Error: " + e.getMessage());
                }
            }
        }
    }

    private void initiateAlarm() {
        Intent intent = new Intent(this, EmergencyActivity.class);
        startActivity(intent);
    }

    // USB Connection + Control Classes Section //

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            usbService = ((UsbService.UsbBinder) iBinder).getService();
            usbService.setHandler(mHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            usbService = null;
        }
    };

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

    // USB Connection + Control Classes Section //

    private void startService(ServiceConnection serviceConnection) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, UsbService.class);
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, UsbService.class);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // This handler will be passed to UsbService. Data received from serial port is displayed through this handler
    private static class MyHandler extends Handler {
        private final WeakReference<CameraActivity> mActivity;

        public MyHandler(CameraActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }
}