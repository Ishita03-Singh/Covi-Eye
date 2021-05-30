package com.example.healthmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.healthmonitor.Math.Fft;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class O2Process extends AppCompatActivity {


    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera;
    private static PowerManager.WakeLock wakeLock = null;

    private Toast mainToast;


    public String user;

    private ProgressBar ProgO2;
    public int ProgP = 0;
    public int inc = 0;

    private static long startTime = 0;
    private double SamplingFreq;

    private static final double RedBlueRatio = 0;
    double Stdr = 0;
    double Stdb = 0;
    double sumred = 0;
    double sumblue = 0;
    public int o2;
    public String userName;

    public ArrayList<Double> RedColorAvgList = new ArrayList<Double>();
    public ArrayList<Double> BlueColorAvgList = new ArrayList<Double>();
    public int counter = 0;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o2_process);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("Usr");
            userName=extras.getString("name");

        }

        preview = findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        ProgO2 = findViewById(R.id.O2PB);
        ProgO2.setProgress(0);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        camera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
        camera.setDisplayOrientation(90);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private final android.hardware.Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            //if data or size == null ****
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;
            double RedAvg;
            double BlueAvg;

            RedAvg = ImageProcessing.decodetoRedBlueGreenAvg(data.clone(), height, width, 1);
            sumred = sumred + RedAvg;
            BlueAvg = ImageProcessing.decodetoRedBlueGreenAvg(data.clone(), height, width, 2);
            sumblue = sumblue + BlueAvg;

            RedColorAvgList.add(RedAvg);
            BlueColorAvgList.add(BlueAvg);

            ++counter;


            if (RedAvg < 200) {
                inc = 0;
                ProgP = inc;
                ProgO2.setProgress(ProgP);
                processing.set(false);
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 30) {

                startTime = System.currentTimeMillis();
                SamplingFreq = (counter / totalTimeInSecs);
                Double[] Red = RedColorAvgList.toArray(new Double[RedColorAvgList.size()]);
                Double[] Blue = BlueColorAvgList.toArray(new Double[BlueColorAvgList.size()]);
                double HRFreq = Fft.FFT(Red, counter, SamplingFreq);
                double bpm = (int) ceil(HRFreq * 60);

                double meanr = sumred / counter;
                double meanb = sumblue / counter;

                for (int i = 0; i < counter - 1; i++) {

                    Double bufferb = Blue[i];

                    Stdb = Stdb + ((bufferb - meanb) * (bufferb - meanb));

                    Double bufferr = Red[i];

                    Stdr = Stdr + ((bufferr - meanr) * (bufferr - meanr));

                }

                double varr = sqrt(Stdr / (counter - 1));
                double varb = sqrt(Stdb / (counter - 1));

                double R = (varr / meanr) / (varb / meanb);

                double spo2 = 100 - 5 * (R);
                o2 = (int) (spo2);

                if ((o2 < 80 || o2 > 99) || (bpm < 45 || bpm > 200)) {
                    inc = 0;
                    ProgP = inc;
                    ProgO2.setProgress(ProgP);
                    startTime = System.currentTimeMillis();
                    counter = 0;
                    processing.set(false);
                    return;
                }

            }

            if (o2 != 0) {
                Intent i = new Intent(O2Process.this, O2Result.class);
                i.putExtra("O2R", o2);
                i.putExtra("Usr", user);
                i.putExtra("name",userName);
                startActivity(i);
                finish();
            }

            if (RedAvg != 0) {
                ProgP = inc++ / 34;
                ProgO2.setProgress(ProgP);
            }

            processing.set(false);

        }
    };

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {


        @SuppressLint("LongLogTag")
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(O2Process.this, Primary.class);
        i.putExtra("Usr", user);
        startActivity(i);
        finish();
    }



}