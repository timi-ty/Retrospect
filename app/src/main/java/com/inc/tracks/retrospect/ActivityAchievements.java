package com.inc.tracks.retrospect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityAchievements extends AppCompatActivity {

    public static final int BUTTON_CLICKED = 0;
    int screenWidth, screenHeight, soundEffect;
    float sfx_volume, soundtrack_volume;
    private AdView adView;
    Progress mProgress;
    Activity mActivity = this;
    ListView achievementsListView;
    ImageView backArrow, achievementsIcon, achievementsTitle;
    AnimatedVectorDrawableCompat backVectorAnimation;
    SoundPool soundPool;
    MediaPlayer backgroundMusic;
    AudioAttributes audioAttributes;
    String[][] achievementDetails;
    int[][] achievementProgress;

    public class LayoutAdapter extends BaseAdapter {
        private Context mContext;

        private LayoutAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return achievements.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout achievementElementContainer;
            RelativeLayout achievementElement;
            RelativeLayout achievementTextContainer;
            ImageView achievementImage;
            TextView achievementName;
            TextView achievementDescription;
            TextView achievementStatus;


            if (convertView == null) {

                int achievements_list_fake_padding = (int) Math.rint(screenHeight * 0.03646);
                int achievements_list_height = (int) Math.rint((screenHeight*0.6484375) + achievements_list_fake_padding);
                int achievementElementContainerHeight = (int) Math.rint(achievements_list_height/4.0);
                int achievementElementHeight = (int) Math.rint(screenHeight*0.134375);
                int achievementElementWidth = (int) Math.rint(achievements_list_height*0.76948);
                int achievementImageSide = (int) Math.rint(screenHeight*0.108333);
                int achievementImageMarginLeft = (int) Math.rint((achievementElementHeight - achievementImageSide)/2.0);
                int achievementNameSize = (int) Math.rint(screenHeight*0.022);
                int achievementDescriptionSize = (int) Math.rint(screenHeight*0.0168);
                int achievementTextMarginLeft = (int) Math.rint(achievementNameSize*1.2);
                int achievementDescriptionMarginTop = (int) Math.rint(achievementDescriptionSize*0.6);
                int achievementStatusSize = (int) Math.rint(screenHeight*0.0182);
                int achievementStatusMargin = (int) Math.rint(achievementStatusSize);

                // if it's not recycled, initialize some attributes


                achievementElementContainer = new RelativeLayout(mContext);
                achievementElement = new RelativeLayout(mContext);
                achievementImage = new ImageView(mContext);
                achievementName = new TextView(mContext);
                achievementDescription = new TextView(mContext);
                achievementTextContainer = new RelativeLayout(mContext);
                achievementStatus = new TextView(mContext);

                ListView.LayoutParams achieveElementContainerParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, achievementElementContainerHeight);
                RelativeLayout.LayoutParams achieveElementParams = new RelativeLayout.LayoutParams(achievementElementWidth, achievementElementHeight);
                RelativeLayout.LayoutParams achievementImageParams = new RelativeLayout.LayoutParams(achievementImageSide, achievementImageSide);
                RelativeLayout.LayoutParams achievementNameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams achievementTextContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams achievementDescriptionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams achievementStatusParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                achievementImageParams.setMargins(achievementImageMarginLeft, 0, 0, 0);
                achievementNameParams.setMargins(0, 0, 0, 0);
                achievementDescriptionParams.setMargins(0, achievementDescriptionMarginTop, 0, 0);
                achievementTextContainerParams.setMargins(achievementTextMarginLeft, 0, 0, 0);
                achievementStatusParams.setMargins(0, 0, achievementStatusMargin, 0);

                achievementImage.setId(R.id.achievement_image);
                achievementName.setId(R.id.achievement_name);
                achievementDescription.setId(R.id.achievement_description);
                achievementTextContainer.setId(R.id.achievement_text_container);
                achievementStatus.setId(R.id.achievement_status);

                achieveElementParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                achievementImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
                achievementDescriptionParams.addRule(RelativeLayout.BELOW, achievementName.getId());
                achievementTextContainerParams.addRule(RelativeLayout.RIGHT_OF, achievementImage.getId());
                achievementTextContainerParams.addRule(RelativeLayout.CENTER_VERTICAL);
                achievementStatusParams.addRule(RelativeLayout.ALIGN_BOTTOM, achievementTextContainer.getId());
                achievementStatusParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


                achievementElement.setBackground(getAchievementElementBackground());
                achievementName.setTextSize(TypedValue.COMPLEX_UNIT_PX, achievementNameSize);
                achievementDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, achievementDescriptionSize);
                achievementStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, achievementStatusSize);
                achievementName.setTextColor(getResources().getColor(R.color.app_text_color));
                achievementDescription.setTextColor(getResources().getColor(R.color.app_text_color));
                achievementStatus.setTextColor(getResources().getColor(R.color.app_text_color));
                achievementName.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                achievementDescription.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                achievementStatus.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

                achievementTextContainer.addView(achievementName, achievementNameParams);
                achievementTextContainer.addView(achievementDescription, achievementDescriptionParams);

                achievementElement.addView(achievementImage, achievementImageParams);
                achievementElement.addView(achievementTextContainer, achievementTextContainerParams);
                achievementElement.addView(achievementStatus, achievementStatusParams);

                achievementElementContainer.addView(achievementElement, achieveElementParams);

                achievementElementContainer.setLayoutParams(achieveElementContainerParams);

            } else {
                achievementElementContainer = (RelativeLayout) convertView;
                achievementElement = (RelativeLayout) achievementElementContainer.getChildAt(0);
                achievementImage = (ImageView) achievementElement.getChildAt(0);
                achievementTextContainer = (RelativeLayout) achievementElement.getChildAt(1);
                achievementStatus = (TextView) achievementElement.getChildAt(2);
                achievementName = (TextView) achievementTextContainer.getChildAt(0);
                achievementDescription = (TextView) achievementTextContainer.getChildAt(1);
            }

            String name = achievementDetails[position][0];
            String description = achievementDetails[position][1];
            int progress = achievementProgress[position][0];
            int goal = achievementProgress[position][1];
            int state = achievementProgress[position][2];
            String ratio = progress + "/" + goal;

            achievementImage.setImageResource(Achievements.getAchievementImage(name, state));
            achievementName.setText(name);
            achievementDescription.setText(description);
            achievementStatus.setText(ratio);

            return achievementElementContainer;
        }

        // Just so you know there are 30 achievements
        private String[] achievements = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24"
        };
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

    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        soundtrack_volume = mProgress.getSoundTrackVolume();
        if (soundPool == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                soundPool = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            soundEffect = soundPool.load(this, R.raw.menu_click, 1);
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

    private void playSoundEffect(int playSound){
        if(soundPool != null) {
            if (playSound == BUTTON_CLICKED) {
                soundPool.play(soundEffect, sfx_volume, sfx_volume, 1, 0, 1.0f);
            }
        }
    }

    private GradientDrawable getAchievementElementBackground(){
        int listViewHeight = (int) Math.rint(screenHeight*0.6484375);
        int listViewWidth = (int) Math.rint(listViewHeight*0.76948);
        int backgroundHeight = (int) Math.rint(screenHeight*0.134375);
        float[] corner_array = new float[8];
        corner_array[0] = screenHeight*0.0390325f;
        corner_array[1] = screenHeight*0.0390325f;
        corner_array[2] = screenHeight*0.0390325f;
        corner_array[3] = screenHeight*0.0390325f;
        corner_array[4] = screenHeight*0.0390325f;
        corner_array[5] = screenHeight*0.0390325f;
        corner_array[6] = screenHeight*0.0390325f;
        corner_array[7] = screenHeight*0.0390325f;
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(getResources().getColor(R.color.white));
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadii(corner_array);
        backgroundDrawable.setSize(listViewWidth, backgroundHeight);

        return backgroundDrawable;
    }

    private GradientDrawable getAchievementListBackground(){
        int backgroundHeight = achievementsListView.getHeight();
        int backgroundWidth = achievementsListView.getWidth();
        float[] corner_array = new float[8];
        corner_array[0] = screenHeight*0.015613f;
        corner_array[1] = screenHeight*0.015613f;
        corner_array[2] = screenHeight*0.015613f;
        corner_array[3] = screenHeight*0.015613f;
        corner_array[4] = screenHeight*0.015613f;
        corner_array[5] = screenHeight*0.015613f;
        corner_array[6] = screenHeight*0.015613f;
        corner_array[7] = screenHeight*0.015613f;
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(getResources().getColor(R.color.white));
        backgroundDrawable.setAlpha(51);
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadii(corner_array);
        backgroundDrawable.setSize(backgroundWidth, backgroundHeight);

        return backgroundDrawable;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createLayout(boolean initializing){

        if(initializing) {
            backArrow = findViewById(R.id.back_arrow);
            achievementsIcon = findViewById(R.id.big_achievements_ic);
            achievementsTitle = findViewById(R.id.achievements_title);
            achievementsListView = findViewById(R.id.achievements_list_view);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/6435851302");
            }
        }

        int achievements_list_margin_top = (int) Math.rint(screenHeight*0.0479);
        int achievements_list_fake_padding = (int) Math.rint(screenHeight * 0.03646);
        int achievements_list_height = (int) Math.rint((screenHeight*0.6484375) + achievements_list_fake_padding);
        int achievements_list_width = (int) Math.rint((achievements_list_height*0.76948) + achievements_list_fake_padding);
        int back_arrow_diameter = (int) Math.rint(screenHeight*0.06094);
        int back_arrow_margin = (int) Math.rint(screenHeight*0.015);
        int achievements_icon_height = (int) Math.rint(screenHeight * 0.0796875);
        int achievements_icon_width = (int) Math.rint(achievements_icon_height*0.63725);
        int achievements_icon_margin_top = (int) Math.rint(screenHeight*0.04);
        int achievements_title_height = (int) Math.rint(screenHeight*0.01875);
        int achievements_title_width = (int) Math.rint(achievements_title_height*14.541667);
        int achievements_title_margin_top = (int) Math.rint(screenHeight*0.05);

        RelativeLayout.LayoutParams backArrowParams = new RelativeLayout.LayoutParams(back_arrow_diameter, back_arrow_diameter);
        RelativeLayout.LayoutParams achievementsIconParams = new RelativeLayout.LayoutParams(achievements_icon_width, achievements_icon_height);
        RelativeLayout.LayoutParams achievementsTitleParams = new RelativeLayout.LayoutParams(achievements_title_width, achievements_title_height);
        RelativeLayout.LayoutParams achievementsListParams = new RelativeLayout.LayoutParams(achievements_list_width, achievements_list_height);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        backArrowParams.setMargins(back_arrow_margin, back_arrow_margin, 0, 0);
        achievementsIconParams.setMargins(0, achievements_icon_margin_top, 0, 0);
        achievementsTitleParams.setMargins(0, achievements_title_margin_top, 0, 0);
        achievementsListParams.setMargins(0, achievements_list_margin_top, 0, 0);

        achievementsIconParams.addRule(RelativeLayout.BELOW, R.id.back_arrow);
        achievementsIconParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        achievementsTitleParams.addRule(RelativeLayout.BELOW, R.id.big_achievements_ic);
        achievementsTitleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        achievementsListParams.addRule(RelativeLayout.BELOW, R.id.achievements_title);
        achievementsListParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        backArrow.setLayoutParams(backArrowParams);
        achievementsIcon.setLayoutParams(achievementsIconParams);
        achievementsTitle.setLayoutParams(achievementsTitleParams);
        achievementsListView.setLayoutParams(achievementsListParams);


        achievementsListView.setAdapter(new LayoutAdapter(this));
        achievementsListView.setBackground(getAchievementListBackground());


        if(initializing) {
            RelativeLayout achievementsLayout = findViewById(R.id.achievements_layout);
            if(achievementsLayout.getChildAt(achievementsLayout.getChildCount() - 1).getId() != R.id.ad_view)
                achievementsLayout.addView(adView, -1, adParams);

            ImageView backVectorView = findViewById(R.id.back_vector);
            RelativeLayout.LayoutParams backVectorViewParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if (backVectorView != null) {
                backVectorView.setLayoutParams(backVectorViewParameters);
                backVectorAnimation = AnimatedVectorDrawableCompat.create(this, R.drawable.animate_snow_flake_vector);
                backVectorView.setImageDrawable(backVectorAnimation);
                backVectorView.setRotation(9.7f);
            }

            View.OnTouchListener indicateTouch = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(0.6f);
                        return !v.hasOnClickListeners();
                    } else if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE
                            || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                        v.setAlpha(1.0f);
                        return true;
                    } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        v.setAlpha(1.0f);
                        return !v.hasOnClickListeners();
                    }
                    return false;
                }
            };

            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSoundEffect(BUTTON_CLICKED);
                    ActivityAchievements.this.finish();
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                }
            });
            backArrow.setOnTouchListener(indicateTouch);

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
                                createLayout(false);
                            }
                        }, 200);
                    }
                });
            }
        }
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
        release_sound_pool_late();
    }

    private void release_sound_pool_late(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (soundPool != null  && !mActivity.hasWindowFocus()) {
                    soundPool.release();
                    soundPool = null;
                }
            }
        }).start();
    }

    private void resume_needed_processes(){
        mProgress = new Progress(this);
        if (backVectorAnimation != null)
            backVectorAnimation.start();
        declareGameSounds();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.achievements);
        achievementDetails = Achievements.getAchievementsListings();
        achievementProgress = Achievements.getAchievementsProgress(this);
        getScreenSize(0);
        createLayout(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
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
