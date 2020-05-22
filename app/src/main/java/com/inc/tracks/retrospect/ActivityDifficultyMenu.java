package com.inc.tracks.retrospect;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityDifficultyMenu extends AppCompatActivity {

    public static final int BEGINNER = 1;
    public static final int INTERMEDIATE = 2;
    public static final int EXPERT = 3;
    public static final int EIDETIC = 4;
    private AdView adView;
    Progress mProgress;
    AnimatedVectorDrawableCompat backVectorAnimation;
    Activity mActivity = this;
    ValueAnimator pulseDumbbell;
    Intent intent2;
    RelativeLayout beginnerView, intermediateView, expertView, eideticView, starsLayout;
    ImageView difficultySymbol, backArrow, difficultyTitle, iLock, exLock, eiLock;
    TextView bCompletion, iCompletion, exCompletion, eiCompletion, starCount;
    SoundPool difficultySounds;
    AudioAttributes audioAttributes;
    MediaPlayer backgroundMusic;
    private  int difficulty, screenHeight, soundEffect;
    boolean[] difficultyOpen;
    float sfx_volume, soundtrack_volume;
    FloatEvaluator rotationPhysics = new FloatEvaluator() {
        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            return super.evaluate(fraction, startValue, endValue);
        }
    };

    private void release_difficulty_sound_pool_late(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (difficultySounds != null  && !mActivity.hasWindowFocus()) {
                    difficultySounds.release();
                    difficultySounds = null;
                }
                Log.println(Log.INFO, "difficultySoundsState", "difficultySounds is " + difficultySounds);
            }
        }).start();
    }
    private void getScreenSize(int adHeight) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;

        Log.d("Old screen height", "" + screenHeight);

        if(eideticView != null) {
            int bottomMargin = screenHeight - eideticView.getBottom();
            if ((adHeight*1.4) > bottomMargin)
                screenHeight = (int)Math.rint(screenHeight - (adHeight*1.4) + bottomMargin);
            Log.d("view container margin", "" + bottomMargin);
        }

        Log.d("New Screen Height", "" + screenHeight);
    }
    private void animation_manifest(){

        pulseDumbbell = ValueAnimator.ofObject(rotationPhysics, 1.0, 1.1);
        pulseDumbbell.setDuration(500);
        pulseDumbbell.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator rotateAnimator) {
                float scale = (float) rotateAnimator.getAnimatedValue();
//                Log.println(Log.INFO, "pulseDumbbell", "Animation Running");
                difficultySymbol.setScaleX(scale);
                difficultySymbol.setScaleY(scale);
            }
        });
        pulseDumbbell.setRepeatMode(ValueAnimator.REVERSE);
        pulseDumbbell.setRepeatCount(ValueAnimator.INFINITE);
        pulseDumbbell.start();
    }
    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        soundtrack_volume = mProgress.getSoundTrackVolume();
        if(difficultySounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                difficultySounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else difficultySounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            soundEffect = difficultySounds.load(this, R.raw.menu_click, 1);
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
//        if(playSound == ActivityHomeScreen.BUTTON_CLICKED){
            difficultySounds.play(soundEffect, sfx_volume, sfx_volume, 1, 0, 1.0f);
//        }
    }
    private void viewDeclarations(boolean initializing){

        RelativeLayout starCountContainer = findViewById(R.id.star_count_container);
        ImageView demarcation = findViewById(R.id.demarcation);
        TextView maxStars = findViewById(R.id.max_stars);
        if(initializing) {
            beginnerView = findViewById(R.id.beginner);
            intermediateView = findViewById(R.id.intermediate);
            expertView = findViewById(R.id.expert);
            eideticView = findViewById(R.id.eidetic);
            difficultySymbol = findViewById(R.id.difficulty_symbol);
            backArrow = findViewById(R.id.back_arrow);
            difficultyTitle = findViewById(R.id.difficulty_title);
            starsLayout = findViewById(R.id.stars_count_layout);
            starsLayout.setBackgroundResource(R.drawable.ic_star_count);
            starCount = findViewById(R.id.star_count);

            iLock = findViewById(R.id.intermediate_lock);
            exLock = findViewById(R.id.expert_lock);
            eiLock = findViewById(R.id.eidetic_lock);

            bCompletion = findViewById(R.id.beginner_completion);
            iCompletion = findViewById(R.id.intermediate_completion);
            exCompletion = findViewById(R.id.expert_completion);
            eiCompletion = findViewById(R.id.eidetic_completion);

            beginnerView.setVisibility(View.INVISIBLE);
            intermediateView.setVisibility(View.INVISIBLE);
            expertView.setVisibility(View.INVISIBLE);
            eideticView.setVisibility(View.INVISIBLE);
            difficultySymbol.setVisibility(View.INVISIBLE);
            backArrow.setVisibility(View.INVISIBLE);
            difficultyTitle.setVisibility(View.INVISIBLE);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/3519001783");
            }
        }

        int difficulty_symbol_height = (int) Math.rint(screenHeight *0.043);
        int difficulty_symbol_width = (int) Math.rint(difficulty_symbol_height*2.06);
        int difficulty_symbol_margin_top = (int) Math.rint(screenHeight *0.08304);
        int back_arrow_diameter = (int) Math.rint(screenHeight *0.06094);
        int back_arrow_margin = (int) Math.rint(screenHeight *0.015);
        int star_layout_height = (int) Math.rint(screenHeight *0.1);
        int star_layout_width = (int) Math.rint(star_layout_height*1.0484);
        int stars_text_size = (int) Math.rint(star_layout_height*0.18);
        int max_stars_text_size = (int) Math.rint(star_layout_height*0.108);
        int difficulty_button_height = (int) Math.rint((screenHeight *0.41)/7.0);
        int difficulty_button_width = (int) Math.rint(difficulty_button_height*6.936);
        int difficulty_title_height = (int) Math.rint(screenHeight *0.01718);
        int difficulty_title_width = (int) Math.rint(difficulty_title_height*9.687);
        int difficulty_title_margin_top = (int) Math.rint(screenHeight *0.05);
        int lock_side = (int) Math.rint(difficulty_button_height);
        int completion_text_height = (int) Math.rint(difficulty_button_height * 0.3);
        int indicator_margin_side = (int) Math.rint(screenHeight * 0.012);

        RelativeLayout.LayoutParams difficultySymbolParameters = new RelativeLayout.LayoutParams(difficulty_symbol_width, difficulty_symbol_height);
        RelativeLayout.LayoutParams backArrowParameters = new RelativeLayout.LayoutParams(back_arrow_diameter, back_arrow_diameter);
        RelativeLayout.LayoutParams beginnerParameters = new RelativeLayout.LayoutParams(difficulty_button_width, difficulty_button_height);
        RelativeLayout.LayoutParams intermediateParameters = new RelativeLayout.LayoutParams(difficulty_button_width, difficulty_button_height);
        RelativeLayout.LayoutParams expertParameters = new RelativeLayout.LayoutParams(difficulty_button_width, difficulty_button_height);
        RelativeLayout.LayoutParams eideticParameters = new RelativeLayout.LayoutParams(difficulty_button_width, difficulty_button_height);
        RelativeLayout.LayoutParams iLockParams = new RelativeLayout.LayoutParams(lock_side, lock_side);
        RelativeLayout.LayoutParams exLockParams = new RelativeLayout.LayoutParams(lock_side, lock_side);
        RelativeLayout.LayoutParams eiLockParams = new RelativeLayout.LayoutParams(lock_side, lock_side);
        RelativeLayout.LayoutParams bCompletionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams iCompletionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams exCompletionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams eiCompletionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams difficultyTitleParameters = new RelativeLayout.LayoutParams(difficulty_title_width, difficulty_title_height);
        RelativeLayout.LayoutParams backVectorViewParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams starLayoutParams = new RelativeLayout.LayoutParams(star_layout_width, star_layout_height);
        RelativeLayout.LayoutParams starContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams starCountParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams demarcationParams = new RelativeLayout.LayoutParams(star_layout_width/3, star_layout_height/40);
        RelativeLayout.LayoutParams maxStarsParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        beginnerParameters.setMargins(0, difficulty_button_height, 0, 0);
        intermediateParameters.setMargins(0, difficulty_button_height, 0, 0);
        expertParameters.setMargins(0, difficulty_button_height, 0, 0);
        eideticParameters.setMargins(0, difficulty_button_height, 0, 0);
        difficultySymbolParameters.setMargins(0, difficulty_symbol_margin_top, 0, 0);
        backArrowParameters.setMargins(back_arrow_margin, back_arrow_margin, 0, 0);
        difficultyTitleParameters.setMargins(0, difficulty_title_margin_top, 0, 0);
        iLockParams.setMargins(0, 0, indicator_margin_side, 0);
        exLockParams.setMargins(0, 0, indicator_margin_side, 0);
        eiLockParams.setMargins(0, 0, indicator_margin_side, 0);
        bCompletionParams.setMargins(0, 0, indicator_margin_side, 0);
        iCompletionParams.setMargins(0, 0, indicator_margin_side, 0);
        exCompletionParams.setMargins(0, 0, indicator_margin_side, 0);
        eiCompletionParams.setMargins(0, 0, indicator_margin_side, 0);
        starLayoutParams.setMargins(0, back_arrow_margin, back_arrow_margin, 0);
        starContainerParams.setMargins(0, 0, 0, (int)Math.rint(back_arrow_margin * 1.8));
        starCountParams.setMargins(0, back_arrow_margin/3, 0, 0);


        difficultySymbolParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        difficultySymbolParameters.addRule(RelativeLayout.BELOW, R.id.back_arrow);
        difficultyTitleParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        difficultyTitleParameters.addRule(RelativeLayout.BELOW, R.id.difficulty_symbol);
        beginnerParameters.addRule(RelativeLayout.BELOW, R.id.difficulty_title);
        intermediateParameters.addRule(RelativeLayout.BELOW, R.id.beginner);
        expertParameters.addRule(RelativeLayout.BELOW, R.id.intermediate);
        eideticParameters.addRule(RelativeLayout.BELOW, R.id.expert);
        beginnerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        intermediateParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        expertParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        eideticParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        iLockParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        iLockParams.addRule(RelativeLayout.CENTER_VERTICAL);
        exLockParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        exLockParams.addRule(RelativeLayout.CENTER_VERTICAL);
        eiLockParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        eiLockParams.addRule(RelativeLayout.CENTER_VERTICAL);
        bCompletionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        bCompletionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        iCompletionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        iCompletionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        exCompletionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        exCompletionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        eiCompletionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        eiCompletionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        starLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        starContainerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        starContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        starCountParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        demarcationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        demarcationParams.addRule(RelativeLayout.BELOW, R.id.star_count);
        maxStarsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        maxStarsParams.addRule(RelativeLayout.BELOW, R.id.demarcation);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        difficultyTitle.setLayoutParams(difficultyTitleParameters);
        difficultySymbol.setLayoutParams(difficultySymbolParameters);
        backArrow.setLayoutParams(backArrowParameters);
        beginnerView.setLayoutParams(beginnerParameters);
        intermediateView.setLayoutParams(intermediateParameters);
        expertView.setLayoutParams(expertParameters);
        eideticView.setLayoutParams(eideticParameters);
        iLock.setLayoutParams(iLockParams);
        exLock.setLayoutParams(exLockParams);
        eiLock.setLayoutParams(eiLockParams);
        bCompletion.setLayoutParams(bCompletionParams);
        iCompletion.setLayoutParams(iCompletionParams);
        exCompletion.setLayoutParams(exCompletionParams);
        eiCompletion.setLayoutParams(eiCompletionParams);
        starsLayout.setLayoutParams(starLayoutParams);
        starCountContainer.setLayoutParams(starContainerParams);
        starCount.setLayoutParams(starCountParams);
        demarcation.setLayoutParams(demarcationParams);
        maxStars.setLayoutParams(maxStarsParams);

        if(initializing) {
            RelativeLayout difficultyLayout = findViewById(R.id.difficulty_layout);
            if(difficultyLayout.getChildAt(difficultyLayout.getChildCount() - 1).getId() != R.id.ad_view)
                difficultyLayout.addView(adView, adParams);
        }

        bCompletion.setTextSize(TypedValue.COMPLEX_UNIT_PX, completion_text_height);
        iCompletion.setTextSize(TypedValue.COMPLEX_UNIT_PX, completion_text_height);
        exCompletion.setTextSize(TypedValue.COMPLEX_UNIT_PX, completion_text_height);
        eiCompletion.setTextSize(TypedValue.COMPLEX_UNIT_PX, completion_text_height);
        starCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, stars_text_size);
        maxStars.setTextSize(TypedValue.COMPLEX_UNIT_PX, max_stars_text_size);

        maxStars.setText(String.format(Locale.ENGLISH, "%d", 832));


        if(initializing) {
            demarcation.setBackground(drawDemarcation());

            ImageView backVectorView = findViewById(R.id.back_vector);
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
                                viewDeclarations(false);
                            }
                        }, 200);
                    }
                });
            }
        }
    }

    private GradientDrawable drawDemarcation(){
        GradientDrawable demarcation = new GradientDrawable();
        demarcation.setShape(GradientDrawable.RECTANGLE);
        demarcation.setColor(getResources().getColor(R.color.white));
        demarcation.setSize(starsLayout.getLayoutParams().width, starsLayout.getLayoutParams().height);
        return demarcation;
    }
    @SuppressLint("ClickableViewAccessibility")
    private void difficulty_clickables(){
        intent2 = new Intent(ActivityDifficultyMenu.this, ActivityLevels.class);
        beginnerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect();
                difficulty = BEGINNER;
                intent2.putExtra("dif", difficulty);
                startActivity(intent2);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }});
        intermediateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect();
                if(difficultyOpen[0]) {
                    difficulty = INTERMEDIATE;
                    intent2.putExtra("dif", difficulty);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                }
                else {
                    Toast lockedDifficultyMessage =  Toast.makeText(ActivityDifficultyMenu.this, "Get 50 Stars to unlock", Toast.LENGTH_LONG);
                    lockedDifficultyMessage.show();
                }
            }});
        expertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect();
                if(difficultyOpen[1]) {
                    difficulty = EXPERT;
                    intent2.putExtra("dif", difficulty);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                }
                else {
                    Toast lockedDifficultyMessage =  Toast.makeText(ActivityDifficultyMenu.this, "Get 100 Stars to unlock", Toast.LENGTH_LONG);
                    lockedDifficultyMessage.show();
                }
            }});
        eideticView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect();
                if(difficultyOpen[2]) {
                    difficulty = EIDETIC;
                    intent2.putExtra("dif", difficulty);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                }
                else {
                    Toast lockedDifficultyMessage =  Toast.makeText(ActivityDifficultyMenu.this, "Get 200 Stars to unlock", Toast.LENGTH_LONG);
                    lockedDifficultyMessage.show();
                }
            }});
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect();
                ActivityDifficultyMenu.this.finish();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });
        View.OnTouchListener indicateTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.6f);
                    return !v.hasOnClickListeners();
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_OUTSIDE
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
                    v.setAlpha(1.0f);
                    return true;
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_UP){
                    v.setAlpha(1.0f);
                    return !v.hasOnClickListeners();
                }
                return false;
            }
        };
        beginnerView.setOnTouchListener(indicateTouch);
        intermediateView.setOnTouchListener(indicateTouch);
        expertView.setOnTouchListener(indicateTouch);
        eideticView.setOnTouchListener(indicateTouch);
        backArrow.setOnTouchListener(indicateTouch);
    }
    private void release_heavy_processes(){
        if (pulseDumbbell != null )
            pulseDumbbell.end();
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
        release_difficulty_sound_pool_late();
    }
    private void resume_needed_processes(){
        mProgress = new Progress(this);

        difficultyOpen = mProgress.openUnlockedDifficulties();

        controlLocks();
        popViewsIn();
        difficulty_clickables();

        int bComp = mProgress.sumBeginnerStars();
        int iComp = mProgress.sumIntermediateStars();
        int exComp = mProgress.sumExpertStars();
        int eiComp = mProgress.sumEideticStars();
        int starsGotten = mProgress.sumAllStars();

        String bString = bComp + "/" + 208;
        String iString = iComp + "/" + 208;
        String exString = exComp + "/" + 208;
        String eiString = eiComp + "/" + 208;

        bCompletion.setText(bString);
        iCompletion.setText(iString);
        exCompletion.setText(exString);
        eiCompletion.setText(eiString);
        starCount.setText(String.format(Locale.ENGLISH, "%d", starsGotten));

        if (pulseDumbbell != null)
            pulseDumbbell.start();
        if (backVectorAnimation != null)
            backVectorAnimation.start();
        declareGameSounds();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
    }
    private void controlLocks(){
        if(difficultyOpen[0]){
            iLock.setVisibility(View.INVISIBLE);
            iCompletion.setVisibility(View.VISIBLE);
        }
        else {
            iLock.setVisibility(View.VISIBLE);
            iCompletion.setVisibility(View.INVISIBLE);
        }
        if(difficultyOpen[1]){
            exLock.setVisibility(View.INVISIBLE);
            exCompletion.setVisibility(View.VISIBLE);
        }
        else {
            exLock.setVisibility(View.VISIBLE);
            exCompletion.setVisibility(View.INVISIBLE);
        }
        if(difficultyOpen[2]){
            eiLock.setVisibility(View.INVISIBLE);
            eiCompletion.setVisibility(View.VISIBLE);
        }
        else {
            eiLock.setVisibility(View.VISIBLE);
            eiCompletion.setVisibility(View.INVISIBLE);
        }
    }
    FloatEvaluator floatEvaluator = new FloatEvaluator() {
        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            return super.evaluate(fraction, startValue, endValue);
        }
    };
    private void popViewsIn(){
        ValueAnimator popViewsAnimation = ValueAnimator.ofObject(floatEvaluator, 0f, 1f);
        popViewsAnimation.setDuration(750);
        popViewsAnimation.setInterpolator(new OvershootInterpolator());
        popViewsAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float appear = (float) valueAnimator.getAnimatedValue();
                backArrow.setScaleX(appear);
                backArrow.setScaleY(appear);
                difficultySymbol.setScaleX(appear);
                difficultySymbol.setScaleY(appear);
                difficultyTitle.setScaleX(appear);
                difficultyTitle.setScaleY(appear);
                beginnerView.setScaleX(appear + ((1 - (float)Math.pow(appear, 2))*0.5f));
                beginnerView.setAlpha(appear + ((1 - (float)Math.pow(appear, 2))*0.5f));
                intermediateView.setScaleX(appear + ((1 - (float)Math.pow(appear, 3))*0.4f));
                intermediateView.setAlpha(appear + ((1 - (float)Math.pow(appear, 3))*0.4f));
                expertView.setScaleX(appear + ((1 - (float)Math.pow(appear, 3))*0.3f));
                expertView.setAlpha(appear + ((1 - (float)Math.pow(appear, 3))*0.3f));
                eideticView.setScaleX(appear + ((1 - (float)Math.pow(appear, 3))*0.2f));
                eideticView.setAlpha(appear + ((1 - (float)Math.pow(appear, 3))*0.2f));
            }
        });
        popViewsAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                beginnerView.setVisibility(View.VISIBLE);
                intermediateView.setVisibility(View.VISIBLE);
                expertView.setVisibility(View.VISIBLE);
                eideticView.setVisibility(View.VISIBLE);
                difficultySymbol.setVisibility(View.VISIBLE);
                backArrow.setVisibility(View.VISIBLE);
                difficultyTitle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animation_manifest();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        popViewsAnimation.setStartDelay(300);
        popViewsAnimation.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.difficulty_menu);
        getScreenSize(0);
        viewDeclarations(true);
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