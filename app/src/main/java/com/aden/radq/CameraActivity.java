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

import com.aden.radq.adapter.UsbService;

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
    private static final String TAG = "CameraActivity";

    //TODO re-analyze the need of the following two variables and their logic
    boolean startYolo = false;
    boolean firstTimeYolo = true;
    //TODO change framesToConfirmFall name/logic
    int framesToConfirmFall = 0;
    boolean isCountDownTimerActive = false;

    //Robot control by height
    int countHeightsDetected = 0;
    List<Integer> listOfHeights = new ArrayList<>();

    String personDetectedText;
    String fallDetectedText;

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
        //Get saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String contactEmail = sharedPreferences.getString("contactEmail","aaaaaaa");

        //Initiate Strings
        personDetectedText = getString(R.string.person_detected_text);
        fallDetectedText = getString(R.string.fall_detected_text);

        Log.d(TAG, "Contact Email: " + contactEmail);
        if(contactEmail.isEmpty()){
            //This is the second counter measure to forbid the start of the cameraActivity without
            //an emergency contact set up. First is at the MainActivity level.
            finish();
        } else {
            setContentView(R.layout.camera_activity);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //USB Handler initialization
            mHandler = new MyHandler(this);

            //Camera initialization
            cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCvCameraViewListener(this);

            //Check which camera will be used frontal or back. By default, frontal camera.
            Log.d(TAG, "Front/Back Camera preference: " + sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false));
            if (sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false)) {
                //Use Back Camera
                Log.d(TAG, "Using Back Camera");
                cameraBridgeViewBase.setCameraIndex(0);
            } else {
                //Use Frontal Camera
                Log.d(TAG, "Using Frontal Camera");
                cameraBridgeViewBase.setCameraIndex(1);
            }

            //OpenCV specific
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
        setFilters();
        startService(usbConnection); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Log.d(TAG, "new frame");
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

            //Detection threshold
            float confThreshold = 0.3f;

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
                        Log.d(TAG, "height: " + height);

                        if((usbService != null) && (height > width)){
                            if(countHeightsDetected<3){
                                Log.d(TAG,"countHeightsDetected<3");
                                listOfHeights.add(height);
                                countHeightsDetected++;
                            } else {
                                Log.d(TAG,"countHeightsDetected>=3");
                                countHeightsDetected = 0;
                                double coefficientOfVariationResult = coefficientOfVariationCalculator(listOfHeights);
                                listOfHeights.clear();
                                Log.d(TAG, "coefficientOfVariationResult: " + coefficientOfVariationResult);
                                String command;
                                if(coefficientOfVariationResult < 15.0){
                                    if(height < 500){
                                        command = "frente";
                                        Log.d(TAG,"Frente");
                                        usbService.write(command.getBytes());
                                    } else if (height > 600){
                                        command = "tras";
                                        Log.d(TAG,"Tras");
                                        usbService.write(command.getBytes());
                                    } else {
                                        command = "parar";
                                        Log.d(TAG,"Parar");
                                        usbService.write(command.getBytes());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Adding boxes around detection
            if (confs.size() >= 1) { //if confs.size() == 0, maybe is because nothing was detected. for that, add an else at the end of this statement
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.3f;
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
                        Imgproc.putText(frame, fallDetectedText + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 5);

                        Log.d(TAG, "Fall detected! Precision: " + intConf + "%");
                        framesToConfirmFall++;

                        //This countdown ensures that the person is really down
//                        if(!isCountDownTimerActive) {
//                            new CountDownTimer(5000, 1000) {
//                                @Override
//                                public void onTick(long millisUntilFinished) {
//                                    isCountDownTimerActive = true;
//                                }
//
//                                @Override
//                                public void onFinish() {
//                                    if (framesToConfirmFall >= 5) {
//                                        //TODO the Screenshot is not used anywhere in the code. Uncomment when it's useful
//                                        //takeScreenshot(frame, intConf);
//                                        initiateAlarm();
//                                    }
//                                    isCountDownTimerActive = false;
//                                    framesToConfirmFall = 0;
//                                }
//                            }.start();
//                        }
                        return frame;
                    } else if (idGuy == 1) {
                        // Pessoa detectada
                        Imgproc.putText(frame, personDetectedText + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
                        Log.d(TAG, "Person detected! Precision: " + intConf + "%");
                        return frame;
                    } else {
                        Log.d(TAG, "idGuy!=0||1");
                        return frame;
                    }
                }
            } else {
                //Log.d(TAG, "what else? empty/non detected frame?");
            }
        } //End if (startYolo)
        //Log.d(TAG, "end frame");
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG,"onCameraViewStarted()");
        if (startYolo) {
            String tinyYoloCfg = getExternalFilesDir(null) + "/yolov3-tiny.cfg";
            String tinyYoloWeights = getExternalFilesDir(null) + "/yolov3-tiny.weights";
            Log.d(TAG, "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
            try{
                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
            } catch (Exception e){
                Log.d(TAG, "Exception: " + e);
                Toast toast = Toast.makeText(this, "Exception: " + e , Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG,"onCameraViewStopped()");
        startYolo = false;
    }

    public void YOLO() {
        if (!startYolo) {
            startYolo = true;
            if (firstTimeYolo) {
                firstTimeYolo = false;
                String tinyYoloCfg = getExternalFilesDir(null) + "/yolov3-tiny.cfg";
                String tinyYoloWeights = getExternalFilesDir(null) + "/yolov3-tiny.weights";

                Log.d(TAG, "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
                try{
                    tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
                } catch (Exception e){
                    Log.d(TAG, "Exception: " + e);
                    Toast toast = Toast.makeText(this, "Exception: " + e , Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        } else {
            startYolo = false;
        }
    }

    private void takeScreenshot(Mat frame, int intConf) {
        cameraBridgeViewBase.disableView();
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
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
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
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            } finally {
                try{
                    if(outputStream != null){
                        outputStream.close();
                        Log.d(TAG,"Saved successfully.");
                    }
                } catch (IOException e){
                    Log.d(TAG,"Error: " + e.getMessage());
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

    private double coefficientOfVariationCalculator(List<Integer> heights){
        double sum = 0D;
        double summation = 0D;
        double standardDeviation;
        double average;
        double coefficientOfVariation;

        for(double height : heights){
            sum += height;
        }
        average = sum / heights.size();

        for(double height : heights){
            double aux = height - average;
            summation += aux * aux;
        }
        standardDeviation = Math.sqrt(summation/(heights.size()-1));

        //Coefficient of Variation
        // <15% homogeneous; 15% - 30%; 30%< heterogeneous
        coefficientOfVariation = (standardDeviation/average) * 100;
        return coefficientOfVariation;
    }
}