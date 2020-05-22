package com.inc.tracks.retrospect;

import com.google.android.gms.ads.MobileAds;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ActivitySplashScreen extends AppCompatActivity {
    public static final int SDK_VERSION = Build.VERSION.SDK_INT;
    public static final int LEVEL_COUNT = 52;
    public static final int TOTAL_LEVEL_COUNT = 208;
    public static final int ACHIEVEMENTS_COUNT = 24;

    Intent intent;
    private void setLogoSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int tracksLogoHeight = (int) Math.rint(screen_height * 0.13585);
        int tracksLogoWidth = (int) Math.rint(tracksLogoHeight * 2.60645);

        ImageView tracksLogo = findViewById(R.id.tracks);

        RelativeLayout.LayoutParams tracksLogoLayout = new RelativeLayout.LayoutParams(tracksLogoWidth, tracksLogoHeight);
        tracksLogoLayout.addRule(RelativeLayout.CENTER_IN_PARENT);

        if (tracksLogo != null)
            tracksLogo.setLayoutParams(tracksLogoLayout);
    }
    public void initializeDatabase(){
       Progress temp =  new Progress(this);
       temp.initializeDatabase();
       temp.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.intro_screen);
        setLogoSize();
        initializeDatabase();
        MobileAds.initialize(this, "ca-app-pub-1923630121694917~7218573640");

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public  void run(){
                intent = new Intent(ActivitySplashScreen.this, ActivityHomeScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        }, 2000);
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public  void run(){
                ActivitySplashScreen.this.finish();
            }
        }, 2050);
    }

    @Override
    public void onBackPressed() {

    }
}









