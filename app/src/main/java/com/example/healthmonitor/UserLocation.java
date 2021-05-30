package com.example.healthmonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserLocation extends AppCompatActivity {

    TextView latitudeTextView, longitTextView;
    double latitude;
    double longitude;
    private String userId, Date;
    String usrid;
    String title="Health Monitor";
    String message="Emergency help required nearby ";
    int HR;
    MediaPlayer emrgncysong;
    ArrayList<String> nearbylist;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    java.util.Date today = Calendar.getInstance().getTime();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        nearbylist =new ArrayList<String>();

emrgncysong=MediaPlayer.create(UserLocation.this,R.raw.emergency_sound);
emrgncysong.start();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("Usr");
            latitude=extras.getDouble("Latitude");
            longitude=extras.getDouble("Longitude");

        }
        Date = df.format(today );

        title="There is a medical emergency in your neighbourhood";
        message=
                "https://www.google.com/maps/search/?api=1&query="+String.valueOf( latitude)+","+String.valueOf( longitude);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        GeoFire geoFire = new GeoFire(ref);
nearbylist.clear();
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(String.valueOf(latitude)), Double.parseDouble(String.valueOf(longitude))), 5.00);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("KEY", "KEY IS: " + key);
                if (!key.equals(userId) && !nearbylist.contains(key)) {
                   nearbylist.add(key);
                    Log.d("KEY FOUND", "Key " + key + " entered the search area at" + location.latitude + "," + location.longitude);


                    ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

        FcmNotificationsSender notificationsSender = new
                FcmNotificationsSender("/topics/all", title, message,getApplicationContext(), UserLocation.this);
        notificationsSender.SendNotifications();

    }

    public void stop_sound(View view) {

emrgncysong.release();
        new AlertDialog.Builder(this)
                .setTitle("Sure to disbale emergency mode")
                .setMessage("Press Ok to disable")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {

                    Intent i = new Intent(this,Primary.class);
                    startActivity(i);
                    finish();

                }).create().show();


    }
}