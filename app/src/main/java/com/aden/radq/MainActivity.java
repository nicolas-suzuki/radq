package com.aden.radq;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    boolean startYolo = false;
    boolean firstTimeYolo = true;
    int framesParaConfirmarQueda = 0;
    Net tinyYolo;

    public void YOLO(View Button){
        if (!startYolo){
            startYolo = true;
            if (firstTimeYolo){
                firstTimeYolo = false;
                String tinyYoloCfg = getExternalFilesDir(null) + "/dnns/yolov3-tiny.cfg" ;
                String tinyYoloWeights = getExternalFilesDir(null) + "/dnns/yolov3-tiny.weights";

                Log.i("tinyLocation1" , "Tiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
                tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
            }
        }
        else{
            startYolo = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                if (status == BaseLoaderCallback.SUCCESS) {
                    cameraBridgeViewBase.setCameraIndex(1);
                    cameraBridgeViewBase.enableView();
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
                        if (framesParaConfirmarQueda > 10) {
                            framesParaConfirmarQueda = 0;
                            Log.i("deteccao", "Queda Confirmada");
                        }
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
        if (startYolo){
            String tinyYoloCfg = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny.cfg" ;
            String tinyYoloWeights = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny.weights";
            Log.i("tinyLocation2" , "Tiny Weights: " + tinyYoloWeights + "\nTiny CFG: " + tinyYoloCfg);
            tinyYolo = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
        }
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem, yo!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}