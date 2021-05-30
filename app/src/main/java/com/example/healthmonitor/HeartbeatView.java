package com.example.healthmonitor;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("MissingPermission")
public class HeartbeatView extends AppCompatActivity {
    FusedLocationProviderClient mFusedLocationClient;
double latitud;
double longitud;
    private String user, userName,Date;
    int HR;
    int PERMISSION_ID = 44;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    java.util.Date today = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat_view);


        Date = df.format(today );
        TextView RHR = this.findViewById(R.id.HRR);
        Button SHR = this.findViewById(R.id.SendHR);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            HR = bundle.getInt("bpm");
            user = bundle.getString("Usr");
            userName=bundle.getString("name");
            Log.d("DEBUG_TAG", "ccccc" + user);
            RHR.setText(String.valueOf(HR));
        }
        if(HR<=60)
        { ImageView result = (ImageView)findViewById(R.id.result);
            Picasso.get()
                    .load(R.drawable.hr3)
                    .into(result);
        }
        else if(HR>=90)
        { ImageView result = (ImageView)findViewById(R.id.result);
            Picasso.get()
                    .load(R.drawable.hr2)
                    .into(result);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();

        SHR.setOnClickListener(v -> {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);

            sendIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(userName) + "'s heart rate is "+RHR.getText() + " and location is" + "\n" +
                    "https://www.google.com/maps/search/?api=1&query="+String.valueOf(latitud)+","+String.valueOf(longitud));
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            finish();
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {

        if (checkPermissions()) {

            if (isLocationEnabled()) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitud=location.getLatitude();
                            longitud=location.getLongitude();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {

            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitud=mLastLocation.getLatitude();
            longitud=mLastLocation.getLongitude();

        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(HeartbeatView.this, Primary.class);
        i.putExtra("Usr", user);
        startActivity(i);
        finish();
    }


}