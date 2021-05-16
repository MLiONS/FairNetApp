package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the FirebaseAnalytics instance.
        Handler handler=new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent=new Intent(Welcome.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}
