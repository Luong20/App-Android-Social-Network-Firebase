package com.example.skyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity<Hander> extends AppCompatActivity {
    Handler handler;
    Runnable runnable;
    ImageView  image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        image = findViewById(R.id.logoIv);
        image.animate().alpha(1000).setDuration(0);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);
    }
}
