package com.inc.tracks.retrospect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityLevels extends AppCompatActivity {
    private AdView adView;
    private Progress mProgress;
    private GridView gridView;
    private AnimatedVectorDrawableCompat backVectorAnimation;
    private Cursor scoresCursor;
    private SoundPool levelSounds;
    private MediaPlayer backgroundMusic;
    private AudioAttributes audioAttributes;
    private int soundEffect;
    private int screenHeight, screenWidth, difficulty;
    private float sfx_volume;

    private void release_levels_sound_pool_late(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (levelSounds != null) {
                    levelSounds.release();
                    levelSounds = null;
                }
            }
        }).start();
    }
    private void getScreenSize(int adHeight) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;

        screenHeight = screenHeight - (int)Math.rint(adHeight*1.4);
    }
    public void getDifficulty(){
        ImageView backgroundImage = findViewById(R.id.levels_back);
        Intent intentMode1 = getIntent();
        Bundle modeBundle;
        modeBundle = intentMode1.getExtras();
        if(modeBundle != null) {
            difficulty = modeBundle.getInt("dif", 1);
        }
        else {
            difficulty = 1;
        }
        switch(difficulty){
            case ActivityDifficultyMenu.BEGINNER:
                backgroundImage.setImageResource(R.drawable.bg_01);
//                themeColor = R.color.beginner_theme;
                break;
            case ActivityDifficultyMenu.INTERMEDIATE:
                backgroundImage.setImageResource(R.drawable.bg_02);
//                themeColor = R.color.intermediate_theme;
                break;
            case ActivityDifficultyMenu.EXPERT:
                backgroundImage.setImageResource(R.drawable.bg_03);
//                themeColor = R.color.expert_theme;
                break;
            case ActivityDifficultyMenu.EIDETIC:
                backgroundImage.setImageResource(R.drawable.bg_04);
//                themeColor = R.color.eidetic_theme;
                break;
            default: backgroundImage.setImageResource(R.drawable.bg_00);
        }
    }
    public int getEachLevelsMedal(int position){
        scoresCursor.moveToPosition(position);
        int high_score = 0;
        int level = position + 1;
        switch (difficulty) {
            case ActivityDifficultyMenu.BEGINNER:
                high_score = scoresCursor.getInt(scoresCursor.getColumnIndexOrThrow(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN));
                break;
            case ActivityDifficultyMenu.INTERMEDIATE:
                high_score = scoresCursor.getInt(scoresCursor.getColumnIndexOrThrow(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN));
                break;
            case ActivityDifficultyMenu.EXPERT:
                high_score = scoresCursor.getInt(scoresCursor.getColumnIndexOrThrow(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN));
                break;
            case ActivityDifficultyMenu.EIDETIC:
                high_score = scoresCursor.getInt(scoresCursor.getColumnIndexOrThrow(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN));
                break;
        }

        return Achievements.getMedalObtained(ActivityMainGame.getGameMode(level), difficulty, level, high_score);
    }
    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        float soundtrack_volume = mProgress.getSoundTrackVolume();
        if (levelSounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                levelSounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else levelSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            soundEffect = levelSounds.load(this, R.raw.menu_click, 1);
        }
        if(backgroundMusic == null){
            backgroundMusic = new MediaPlayer();
            backgroundMusic = MediaPlayer.create(this, R.raw.menu_music);
            backgroundMusic.setLooping(true);
            if(SDK_VERSION >= 21 && audioAttributes != null){
                backgroundMusic.setAudioAttributes(audioAttributes);
            }
            backgroundMusic.setVolume(soundtrack_volume, soundtrack_volume);
        }
    }
    private void playSoundEffect(){
        if(levelSounds != null)
            levelSounds.play(soundEffect, sfx_volume, sfx_volume, 1, 0, 1.0f);
    }
    @SuppressLint("ClickableViewAccessibility")
    public class LayoutAdapter extends BaseAdapter {
        private Context mContext;

        private LayoutAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return levels.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout levelLayout;
            ImageView medalImage;
            TextView levelText;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                int totalHorizontalSpacing = (int) Math.rint(0.1111 * screenWidth);
                int elementSide = (int) Math.rint((screenWidth - totalHorizontalSpacing)/4.0);
                int medalDiameter = (int) Math.rint(elementSide*0.7);
                int levelTextSize = (int) Math.rint(screenHeight *0.0182);
                int levelTextMargin = (int) Math.rint(levelTextSize*0.3);

                levelLayout = new RelativeLayout(mContext);
                medalImage = new ImageView(mContext);
                levelText = new TextView(mContext);

                ViewGroup.LayoutParams levelLayoutParameters = new ViewGroup.LayoutParams(elementSide, elementSide);
                RelativeLayout.LayoutParams medalImageParameters = new RelativeLayout.LayoutParams(medalDiameter, medalDiameter);
                RelativeLayout.LayoutParams levelTextParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                levelTextParameters.setMargins(0, 0, levelTextMargin, levelTextMargin);

                medalImageParameters.addRule(RelativeLayout.CENTER_IN_PARENT);
                levelTextParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                levelTextParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                levelText.setGravity(Gravity.CENTER);
                levelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, levelTextSize);
                levelText.setTextColor(getResources().getColor(R.color.black));
                levelText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

                levelLayout.addView(medalImage, medalImageParameters);
                levelLayout.addView(levelText, levelTextParameters);

                levelLayout.setBackgroundResource(R.drawable.level_background);
                levelLayout.setLayoutParams(levelLayoutParameters);
            } else {
                levelLayout = (RelativeLayout) convertView;
                medalImage = (ImageView) levelLayout.getChildAt(0);
                levelText = (TextView) levelLayout.getChildAt(1);
            }
            int levelsMedal = getEachLevelsMedal(position);
            medalImage.setImageResource(levelsMedal);
            levelText.setText(levels[position]);
            levelText.setBackground(getLevelTextBackground(position + 1));
            return levelLayout;
        }

        // Just so you know there are 52 levels
        private String[] levels = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
                "41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
                "51", "52"
        };
    }
    private GradientDrawable getLevelTextBackground(int level){
        int backgroundHeight = (int) Math.rint(screenHeight *0.022);
        int backgroundWidth = (int) Math.rint(backgroundHeight * 2);
        float[] corner_array = new float[8];
        corner_array[0] = backgroundHeight*0.5f;
        corner_array[1] = backgroundHeight*0.5f;
        corner_array[2] = backgroundHeight*0.5f;
        corner_array[3] = backgroundHeight*0.5f;
        corner_array[4] = backgroundHeight*0.5f;
        corner_array[5] = backgroundHeight*0.5f;
        corner_array[6] = backgroundHeight*0.5f;
        corner_array[7] = backgroundHeight*0.5f;
        GradientDrawable backgroundDrawable = new GradientDrawable();
        if(((level % 4) - 3) == 0) backgroundDrawable.setColor(getResources().getColor(R.color.silver));
        else if(((level % 3) - 2 == 0) && !(((level % 4) - 3) == 0)) backgroundDrawable.setColor(getResources().getColor(R.color.green));
        else backgroundDrawable.setColor(getResources().getColor(R.color.white));
        if (level == 11 || level == 23 || level == 35 || level == 47) backgroundDrawable.setColor(getResources().getColor(R.color.platinum));
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadii(corner_array);
        backgroundDrawable.setSize(backgroundWidth, backgroundHeight);

        return backgroundDrawable;
    }

    private void gridViewManifest(boolean initializing){
        if(initializing) {
            gridView = findViewById(R.id.levels_grid_view);
            gridView.setVerticalSpacing((int) Math.rint(0.02778 * screenWidth));

            final Intent mainGameIntent = new Intent(ActivityLevels.this, ActivityMainGame.class);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    if (getEachLevelsMedal(position) != R.drawable.locked_level) {// to ensure only unlocked levels and the first level are playable
                        v.setClickable(false);
                        playSoundEffect();
                        mainGameIntent.putExtra("dif", difficulty);
                        mainGameIntent.putExtra("level", position + 1);
                        startActivity(mainGameIntent);
                        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                    } else {
                        Toast lockedLevelMessage = Toast.makeText(ActivityLevels.this, "Finish Previous Level To Unlock", Toast.LENGTH_LONG);
                        lockedLevelMessage.show();
                    }
                }
            });

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/6636119711");
            }
        }

        RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        gridView.setLayoutParams(gridParams);

        if(initializing) {
            RelativeLayout levelsLayout = findViewById(R.id.levels_layout);
            if(levelsLayout.getChildAt(levelsLayout.getChildCount() - 1).getId() != R.id.ad_view)
                levelsLayout.addView(adView, -1, adParams);

            ImageView backVectorView = findViewById(R.id.back_vector);
            RelativeLayout.LayoutParams backVectorViewParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if (backVectorView != null) {
                backVectorView.setLayoutParams(backVectorViewParameters);
                backVectorAnimation = AnimatedVectorDrawableCompat.create(this, R.drawable.animate_snow_flake_vector);
                backVectorView.setImageDrawable(backVectorAnimation);
                backVectorView.setRotation(9.7f);
            }

            if(!NO_ADS) {
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                adView.loadAd(adRequest);

                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getScreenSize(adView.getHeight());
                                Log.d("advert height", "" + adView.getHeight());
                                gridViewManifest(false);
                            }
                        }, 200);
                    }
                });
            }
        }


        gridView.setAdapter(new LayoutAdapter(this));
    }
    private void release_heavy_processes(){
        if (backVectorAnimation != null)
            backVectorAnimation.stop();
        if(backgroundMusic != null) {
            PLAYBACK_PROGRESS = backgroundMusic.getCurrentPosition();
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
        mProgress.finish();
        mProgress = null;
        release_levels_sound_pool_late();
    }
    private void resume_needed_processes(){
        mProgress = new Progress(this);
        scoresCursor = mProgress.getLevelScores(difficulty);
        if(backVectorAnimation != null)
            backVectorAnimation.start();
        declareGameSounds();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.levels);
        getScreenSize(0);
        getDifficulty();
        gridViewManifest(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        getScreenSize(adView.getHeight());
        gridViewManifest(false);
        resume_needed_processes();
    }

    @Override
    protected void onPause() {
        release_heavy_processes();
        adView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
    }
}
