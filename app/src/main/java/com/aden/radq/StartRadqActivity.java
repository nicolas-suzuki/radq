package com.aden.radq;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.services.UsbService;
import com.aden.radq.utils.CoefficientOfVariationCalculator;
import com.aden.radq.utils.NotificationSender;
import com.aden.radq.utils.SettingsStorage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StartRadqActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private boolean isDetectionStarted = false;
    private boolean isFirstTimeDetection = true;

    //Camera connection + detection specific variables
    CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private Net detectionEssential;

    //Fall confirmation
    private boolean firstDetection = true;
    private int framesToConfirmFall = 0;
    private Date startTime;

    private NotificationSender notificationSender;

    /////////////////////////////////// Robot variables section ///////////////////////////////////
    //Robot control by height
    private int countHeightsDetected = 0;
    private final List<Integer> listOfHeights = new ArrayList<>(3);

    //Robot instructions
    private TextView tvRobotInstructions;
    private boolean isRobotInstructionsEnabled = false;

    //USB connection + control specific variables
    UsbService usbService;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_radq_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Camera initialization
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        //Settings
        SettingsStorage settingsStorage = new SettingsStorage(StartRadqActivity.this);
        String accountId = settingsStorage.getIdentifierKey();

        //Robot instructions
        isRobotInstructionsEnabled = settingsStorage.getRobotInstructions();
        if (isRobotInstructionsEnabled) {
            tvRobotInstructions = findViewById(R.id.tvRobotInstructions);
            tvRobotInstructions.setText("");
        }

        //Notification
        notificationSender = new NotificationSender(accountId);
        notificationSender.send("c3RhcnRpbmdmYWxsZGV0ZWN0aW9u");

        //Check which camera will be used frontal or back. By default, frontal camera.
        if (settingsStorage.getSwitchCameraFrontBack()) {
            //Use Back Camera
            cameraBridgeViewBase.setCameraIndex(0);
        } else {
            //Use Frontal Camera
            cameraBridgeViewBase.setCameraIndex(1);
        }

        //OpenCV specific
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                if (status == BaseLoaderCallback.SUCCESS) {
                    cameraBridgeViewBase.enableView();
                    checkIfDetectionStarted();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
    }

    @Override
    protected final void onResume() {
        super.onResume();

        //Detection
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_error_OpenCVLoader), Toast.LENGTH_SHORT).show();
        }
        cameraBridgeViewBase.enableView();
        checkIfDetectionStarted();

        //Usb service
        setFilters();
        startService(usbConnection); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected final void onPause() {
        super.onPause();

        //Detection
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }

        //Usb service
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        //Detection
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        notificationSender.send("b3ZlcnJpZGUgb3IgYmF0dGVyeSBsb3cu");
    }

    @Override
    public final Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if (isDetectionStarted) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);
            detectionEssential.setInput(imageBlob);

            java.util.List<Mat> result = new java.util.ArrayList<>(2);
            List<String> outBlobNames = new java.util.ArrayList<>(2);
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");

            detectionEssential.forward(result, outBlobNames);

            //Detection threshold
            float confThreshold = 0.3f;

            List<Integer> clsIds = new ArrayList<>(2);
            List<Float> confs = new ArrayList<>(2);
            List<Rect> rects = new ArrayList<>(2);

            int size = result.size();
            for (int i = 0; i < size; ++i) {
                Mat level = result.get(i);
                int rows = level.rows();
                for (int j = 0; j < rows; ++j) {
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
                        //TODO this usbService != null does not work as I imagined.
                        // This doesn't check if there's a current device connected. Only if the object was created
                        if ((usbService != null) && (height > width)) {
                            if (countHeightsDetected < 6) { //Change this number to increase/decrease the list of number to be calculated before taking an action
                                listOfHeights.add(height);
                                countHeightsDetected++;
                            } else {
                                countHeightsDetected = 0;
                                double coefficientOfVariationResult = CoefficientOfVariationCalculator.calculate(listOfHeights);
                                listOfHeights.clear();
                                String command;
                                if (coefficientOfVariationResult < 15.0) {
                                    if (height < 500) {
                                        if (isRobotInstructionsEnabled)
                                            runOnUiThread(() -> tvRobotInstructions.setText(getString(R.string.robot_instruction_forward)));
                                        //noinspection SpellCheckingInspection
                                        command = "frente"; //Forward
                                        usbService.write(command.getBytes());
                                    } else if (height > 600) {
                                        if (isRobotInstructionsEnabled)
                                            runOnUiThread(() -> tvRobotInstructions.setText(getString(R.string.robot_instructions_backwards)));
                                        //noinspection SpellCheckingInspection
                                        command = "tras"; //Backwards
                                        usbService.write(command.getBytes());
                                    } else {
                                        if (isRobotInstructionsEnabled)
                                            runOnUiThread(() -> tvRobotInstructions.setText(getString(R.string.robot_instruction_stop)));
                                        //noinspection SpellCheckingInspection
                                        command = "parar"; //Stop
                                        usbService.write(command.getBytes());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Adding boxes around detection
            if (confs.size() >= 1) {
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.3f;
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
                Rect[] boxesArray = rects.toArray(new Rect[0]);
                MatOfRect boxes = new MatOfRect(boxesArray);
                MatOfInt indices = new MatOfInt();
                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

                // Draw result boxes (make sure to return frames to avoid unnecessary calculations)
                int[] ind = indices.toArray();
                for (int idx : ind) {
                    Rect box = boxesArray[idx];
                    int idGuy = clsIds.get(idx);
                    float conf = confs.get(idx);
                    int intConf = (int) (conf * 100);

                    if (idGuy == 0) {
                        // Fall detected
                        if (firstDetection) {
                            //Initialize automatic fall confirmation
                            startTime = Calendar.getInstance().getTime();
                            firstDetection = false;
                        }
                        Imgproc.putText(frame, getString(R.string.fall_detected_text) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 5);

                        framesToConfirmFall++;

                        if (framesToConfirmFall == 5) {
                            Date endTime = Calendar.getInstance().getTime();
                            long differenceInMinutes = endTime.getTime() - startTime.getTime();
                            long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMinutes);
                            if (differenceInSeconds < 10) {
                                //Fall automatically confirmed
                                firstDetection = true;
                                framesToConfirmFall = 0;
                                if (usbService != null) { //Last command that will be send to the robot
                                    //noinspection SpellCheckingInspection
                                    String command = "emergencia"; //Emergency
                                    usbService.write(command.getBytes());
                                }
                                openEmergencyActivity();
                            } else {
                                //False alarm
                                framesToConfirmFall = 0;
                                firstDetection = true;
                                return frame;
                            }
                        } else {
                            return frame;
                        }
                    } else if (idGuy == 1) {
                        // Person detected
                        Imgproc.putText(frame, getString(R.string.person_detected_text) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
                        return frame;
                    }
                }
            }
        }
        return frame;
    }

    @Override
    public final void onCameraViewStarted(int width, int height) {
        if (isDetectionStarted) {
            initializeDetection();
        }
    }

    @Override
    public final void onCameraViewStopped() {
        isDetectionStarted = false;
    }

    public final void checkIfDetectionStarted() {
        if (!isDetectionStarted) {
            isDetectionStarted = true;
            if (isFirstTimeDetection) {
                isFirstTimeDetection = false;
                initializeDetection();
            }
        }
    }

    public final void initializeDetection() {
        String tinyYoloCfg = getExternalFilesDir(null) + "/yolov3-tiny.cfg";
        String tinyYoloWeights = getExternalFilesDir(null) + "/yolov3-tiny.weights";
        try {
            detectionEssential = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getText(R.string.toast_error_initializeDetection), Toast.LENGTH_SHORT).show();
        }
    }

    private void openEmergencyActivity() {
        Intent intent = new Intent(StartRadqActivity.this, EmergencyActivity.class);
        startActivity(intent);
    }

    //////////////////////// USB Connection + Control Classes Section //////////////////////////////

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            usbService = ((UsbService.UsbBinder) iBinder).getService();
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
                    Toast.makeText(context, getText(R.string.toast_action_usb_permission_granted), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, getText(R.string.toast_action_usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, getText(R.string.toast_action_no_usb), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, getText(R.string.toast_action_usb_disconnected), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, getText(R.string.toast_action_usb_device_not_supported), Toast.LENGTH_SHORT).show();
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