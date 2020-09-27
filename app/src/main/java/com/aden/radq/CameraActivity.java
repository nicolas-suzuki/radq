package com.aden.radq;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;
import static com.aden.radq.SettingsActivity.SWITCH_CAMERA_FRONT_BACK;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    boolean startYolo = false;
    boolean firstTimeYolo = true;
    int framesParaConfirmarQueda = 0;
    Net tinyYolo;

    public void YOLO() {
        if (!startYolo) {
            startYolo = true;
            if (firstTimeYolo) {
                firstTimeYolo = false;
                String tinyYoloCfg = getExternalFilesDir(null) + "/dnns/yolov3-tiny.cfg";
                String tinyYoloWeights = getExternalFilesDir(null) + "/dnns/yolov3-tiny.weights";

                Log.i("tinyLocation1", "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
            }
        } else {
            startYolo = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        //downloadNecessaryFiles();
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        //cameraBridgeViewBase.setMaxFrameSize(5000,5000);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Log.i("cameraFrontBack", "Switch: " + sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false));
        if (sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK, false)) {
            //Use Back Camera
            Log.i("cameraFrontBack", "Switch Back");
            cameraBridgeViewBase.setCameraIndex(0);
        } else {
            //Use Frontal Camera
            Log.i("cameraFrontBack", "Switch Frontal");
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
                        Log.d("metrics", "height: " + height);
                    }
                }
            }
            int ArrayLength = confs.size();

            //deteccao
            if (ArrayLength >= 1) {
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
                        // Queda detectada
                        Imgproc.putText(frame, "Queda Detectada" + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 5);
                        Log.i("deteccao", "Queda Detectada! Precisão: " + intConf + "%");
                        framesParaConfirmarQueda++;
//                        if (framesParaConfirmarQueda > 10) {
//                            framesParaConfirmarQueda = 0;
//                            Log.i("deteccao", "Queda Confirmada");
//                            //takeScreenshot(cameraBridgeViewBase);
//
//                            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//                            View popupView = layoutInflater.inflate(R.layout.emergency_activity,null);
//
//                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
//                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                            boolean focusable = true; //set to false in the future and add buttons to the emergency view
//                            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//                            popupWindow.showAtLocation(findViewById(R.id.CameraView),Gravity.CENTER,0,0);
//                        }
                    } else if (idGuy == 1) {
                        // Pessoa detectada
                        Imgproc.putText(frame, "deteccao" + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                        Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
                    } else {
                        Log.w("deteccao", "idGuy!=0||1");
                    }
                }
            }
        }
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (startYolo) {
            String tinyYoloCfg = getExternalFilesDir(null) + "/dnns/yolov3-tiny.cfg";
            String tinyYoloWeights = getExternalFilesDir(null) + "/dnns/yolov3-tiny.weights";
            Log.i("tinyLocation2", "\nTiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
            tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
        }
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There's a problem, yo!", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    private void downloadNecessaryFiles() {
        if (checkDownloadedFiles()) { //check if files already there
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            assert downloadManager != null;

            Uri uri = Uri.parse("https://drive.google.com/uc?export=download&id=1QTWqtQSASSe8AIugP6tb2480Ro7Gt2yN");
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.downloading_necessary_files));
            request.setDescription(getString(R.string.downloading_WEIGHT_File));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.parse("file://" + getExternalFilesDir(null) + "/yolov3-tiny.weights"));
            downloadManager.enqueue(request);

            uri = Uri.parse("https://drive.google.com/uc?export=download&id=1Y0CX4-Z4ZrteVkuzj2B8MU6WT65qIrw0");
            request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.downloading_necessary_files));
            request.setDescription(getString(R.string.downloading_CFG_File));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.parse("file://" + getExternalFilesDir(null) + "/yolov3-tiny.cfg"));
            downloadManager.enqueue(request);
        }
    }

    private boolean checkDownloadedFiles() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).toString() + "/dnns";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        Log.d("Files", "Size: " + files.length);

        for (File file : files) {
            Log.d("Files", "FileName: " + file.getName());
        }
        return true;
    }

    private void takeScreenshot(View view) {
        Date date = new Date();
        CharSequence now = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
        String filename = getExternalFilesDir(null) + "/screenshot/" + now + ".jpg";

//        View root = getWindow().getDecorView();
//        root.setDrawingCacheEnabled(true);
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        File file = new File(filename);
        Objects.requireNonNull(file.getParentFile()).mkdir();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

//            Uri uri = Uri.fromFile(file);
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri,"image/*");
//            //startActivity
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initiateAlarm() {

    }

    private void sendMessageToContact() {

    }

}