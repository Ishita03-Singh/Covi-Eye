package com.example.healthmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SpashScreen extends Activity {
    ImageView logo;
    TextView designed, name, app_name;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        logo = findViewById(R.id.logo);
        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startEnterAnimation();
            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(currentUser==null){
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();}
                else{  startActivity(new Intent(getApplicationContext(), Primary.class));
                    finish();
                }
            }
        }, 5500);

    }
    private void startEnterAnimation() {


        logo.setVisibility(View.VISIBLE);

    }

}