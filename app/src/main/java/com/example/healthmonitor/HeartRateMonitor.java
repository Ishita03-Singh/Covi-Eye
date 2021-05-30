package com.example.healthmonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.healthmonitor.Math.Fft;

import java.security.Policy;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.ceil;

public class HeartRateMonitor extends AppCompatActivity {


    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera;
    int PERMISSION_ID = 44;
    private static PowerManager.WakeLock wakeLock = null;

    private Toast mainToast;
    public int Beats = 0;
    public double bufferAvgB = 0;

    public String user;
    public String userName;

    private ProgressBar ProgHeart;
    public int ProgP = 0;
    public int inc = 0;

    private static long startTime = 0;
    private double SamplingFreq;

    public ArrayList<Double> GreenColorAvgList = new ArrayList<Double>();
    public ArrayList<Double> RedColorAvgList = new ArrayList<Double>();
    public int counter = 0;
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_monitor);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("Usr");
            userName=extras.getString("name");

        }

        preview = findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        ProgHeart = findViewById(R.id.HRPB);
        ProgHeart.setProgress(0);

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

            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
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


        private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onPreviewFrame(byte[] data, Camera cam) {

                if (data == null) throw new NullPointerException();
                Camera.Size size = cam.getParameters().getPreviewSize();
                if (size == null) throw new NullPointerException();

                if (!processing.compareAndSet(false, true)) return;

                int width = size.width;
                int height = size.height;

                double GreenAvg;
                double RedAvg;

                GreenAvg = ImageProcessing.decodetoRedBlueGreenAvg(data.clone(), height, width, 3);
                RedAvg = ImageProcessing.decodetoRedBlueGreenAvg(data.clone(), height, width, 1);

                GreenColorAvgList.add(GreenAvg);
                RedColorAvgList.add(RedAvg);

                ++counter;

                if (RedAvg < 200) {
                    inc = 0;
                    ProgP = inc;
                    counter = 0;
                    ProgHeart.setProgress(ProgP);
                    processing.set(false);
                }

                long endTime = System.currentTimeMillis();
                double totalTimeInSecs = (endTime - startTime) / 1000d;
                if (totalTimeInSecs >= 30) {

                    Double[] Green = GreenColorAvgList.toArray(new Double[GreenColorAvgList.size()]);
                    Double[] Red = RedColorAvgList.toArray(new Double[RedColorAvgList.size()]);

                    SamplingFreq = (counter / totalTimeInSecs);

                    double HRFreq = Fft.FFT(Green, counter, SamplingFreq);
                    double bpm = (int) ceil(HRFreq * 60);
                    double HR1Freq = Fft.FFT(Red, counter, SamplingFreq);
                    double bpm1 = (int) ceil(HR1Freq * 60);


                    if ((bpm > 45 || bpm < 200)) {
                        if ((bpm1 > 45 || bpm1 < 200)) {

                            bufferAvgB = (bpm + bpm1) / 2;
                        } else {
                            bufferAvgB = bpm;
                        }
                    } else if ((bpm1 > 45 || bpm1 < 200)) {
                        bufferAvgB = bpm1;
                    }

                    if (bufferAvgB < 45 || bufferAvgB > 200) {
                        inc = 0;
                        ProgP = inc;
                        ProgHeart.setProgress(ProgP);
                        mainToast = Toast.makeText(getApplicationContext(), "Measurement Failed", Toast.LENGTH_SHORT);
                        mainToast.show();
                        startTime = System.currentTimeMillis();
                        counter = 0;
                        processing.set(false);
                        return;
                    }

                    Beats = (int) bufferAvgB;
                }

                if (Beats != 0) {
                    Intent i = new Intent(HeartRateMonitor.this, HeartbeatView.class);
                    i.putExtra("bpm", Beats);
                    i.putExtra("Usr", user);
                    i.putExtra("name",userName);
                    startActivity(i);
                    finish();
                }


                if (RedAvg != 0) {

                    ProgP = inc++ / 34;
                    ProgHeart.setProgress(ProgP);
                }

                //keeps taking frames tell 30 seconds
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
        Intent i = new Intent(HeartRateMonitor.this, Primary.class);
        i.putExtra("Usr", user);
        startActivity(i);
        finish();
    }
}