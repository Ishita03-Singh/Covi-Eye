package com.example.healthmonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;


public class Primary extends AppCompatActivity {
    FusedLocationProviderClient mFusedLocationClient;
    private FirebaseAuth mAuth;

    private String userId;
    private double latitude;
    private double longitude;
    TextView userName;
    private int p;
    int PERMISSION_ID = 44;
    private GoogleApiClient googleApiClient;
    DatabaseReference database;
    private GoogleSignInOptions gso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary);

        userName=findViewById(R.id.names);
        gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this::onConnectionFailed)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
       Button HeartRate = this.findViewById(R.id.HR);
       Button Ox2 = this.findViewById(R.id.O2);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("Usr");

        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        getLastLocation();


        HeartRate.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
            }, PERMISSION_ID);
            p = 1;
            Intent i = new Intent(v.getContext(), HeartRateMonitor.class);
            i.putExtra("Usr", userId);
            i.putExtra("name", userName.getText());
            i.putExtra("Page", p);
            startActivity(i);
            finish();
        });





        Ox2.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
            }, PERMISSION_ID);
            p = 1;
            Intent i = new Intent(v.getContext(), O2Process.class);
            i.putExtra("Usr", userId);
            i.putExtra("name", userName.getText());
            i.putExtra("Page", p);
            startActivity(i);
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

                   latitude=location.getLatitude();
                     longitude=location.getLongitude();

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
            latitude=mLastLocation.getLatitude();
            longitude=mLastLocation.getLongitude();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(userId);



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
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr= Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if(opr.isDone()){
            GoogleSignInResult result=opr.get();
            handleSignInResult(result);
        }else{
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account=result.getSignInAccount();
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            if (currentFirebaseUser != null) {

               userId= currentFirebaseUser.getUid();
                userName.setText(account.getDisplayName());

            }



        }else{
            Toast.makeText(getApplicationContext(),"Trouble in Signing in",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Sure to Exit?")
                .setMessage("Press Ok to Exit the application")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {

                    Primary.super.onBackPressed();
                    finish();
                    System.exit(0);
                }).create().show();
    }

    public void gotomaps(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Sure to enable emergency mode")
                .setMessage("Press Ok to enable")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                    Intent i = new Intent(this,UserLocation.class);
                    i.putExtra("Usr", userId);
                    i.putExtra("Latitude",latitude);
                    i.putExtra("Longitude",longitude);
                    startActivity(i);
                    finish();
                }).create().show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation(userId, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });





    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void logout(View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()){
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(Primary.this,SignIn.class);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Session not close", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}