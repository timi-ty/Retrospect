package com.inc.tracks.retrospect;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.NO_GRAVITY;
import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivitySettings extends AppCompatActivity {
    private AdView adView;
    private Progress savedSettings;
    private ImageView backArrow, resetProgress, creditsView;
    private RelativeLayout tactileLayout, tutorialLayout, popLayout, settingsLayout, alertLayout;
    private PopupWindow creditsPopUp, alertPopUp;
    private View popView;
    private Switch toggleTactile, toggleTutorial;
    private SeekBar sfxSlider, soundTrackSlider;
    private SoundPool settingsSounds;
    private MediaPlayer backgroundMusic;
    private AudioAttributes audioAttributes;
    private Vibrator tactileFeedback;
    private int screenHeight, screenWidth, soundEffect;
    private boolean tactile_on, tutorial_on;
    private float sfx_volume, soundtrack_volume;

    private void getScreenSize(int adHeight) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;

        Log.d("Old screen height", "" + screenHeight);

        if(creditsView != null) {
            int bottomMargin = screenHeight - creditsView.getBottom();
            if ((adHeight*1.4) > bottomMargin)
                screenHeight = (int)Math.rint(screenHeight - (adHeight*1.4) + bottomMargin);
            Log.d("bottom margin", "" + bottomMargin);
        }

        Log.d("New Screen Height", "" + screenHeight);
    }

    private void declareViews(boolean initializing){

        ImageView settingsIcon = findViewById(R.id.big_setting_ic);
        ImageView settingsTitle = findViewById(R.id.settings_title);
        RelativeLayout sfxView = findViewById(R.id.sfx);
        RelativeLayout soundtrackView = findViewById(R.id.soundtrack);
        if(initializing) {
            settingsLayout = findViewById(R.id.settings_layout);
            backArrow = findViewById(R.id.settings_back_arrow);
            tactileLayout = findViewById(R.id.tactile_feedback_layout);
            tutorialLayout = findViewById(R.id.tutorial_layout);
            creditsView = findViewById(R.id.credits);
            resetProgress = findViewById(R.id.reset_progress);
            toggleTactile = findViewById(R.id.tactile_toggle);
            toggleTutorial = findViewById(R.id.tutorial_toggle);
            sfxSlider = findViewById(R.id.sfx_slider);
            soundTrackSlider = findViewById(R.id.soundtrack_slider);

            sfxSlider.setMax(100);
            soundTrackSlider.setMax(100);
            toggleTactile.setChecked(savedSettings.getTactileState() == 1);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/4274568302");
            }
        }


        int back_arrow_diameter = (int) Math.rint(screenHeight*0.06094);
        int back_arrow_margin = (int) Math.rint(screenHeight*0.015);
        int settings_icon_height = (int) Math.rint(screenHeight * 0.0495);
        int settings_icon_width = (int) Math.rint(settings_icon_height*0.9618);
        int settings_icon_margin_top = (int) Math.rint(screenHeight*0.05306);
        int settings_title_height = (int) Math.rint(screenHeight*0.01718);
        int settings_title_width = (int) Math.rint(settings_title_height*8.2803);
        int settings_title_margin_top = (int) Math.rint(screenHeight*0.05);
        int settings_button_height = (int) Math.rint((screenHeight*0.41)/7.0);
        int settings_button_width = (int) Math.rint(settings_button_height*6.936);
        int toggle_button_height = (int) Math.rint(settings_button_height/5.0);
        int toggle_button_margin_right = (int) Math.rint(screenHeight*0.022656);
        int slider_width = (int) Math.rint(settings_button_width/2.0);


        RelativeLayout.LayoutParams backArrowParameters = new RelativeLayout.LayoutParams(back_arrow_diameter, back_arrow_diameter);
        RelativeLayout.LayoutParams settingsIconParameters = new RelativeLayout.LayoutParams(settings_icon_width, settings_icon_height);
        RelativeLayout.LayoutParams settingsTitleParameters = new RelativeLayout.LayoutParams(settings_title_width, settings_title_height);
        RelativeLayout.LayoutParams tactileLayoutParams = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams tutorialLayoutParams = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams creditsParams = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams soundTrackParameters = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams sfxParameters = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams resetProgressParameters = new RelativeLayout.LayoutParams(settings_button_width, settings_button_height);
        RelativeLayout.LayoutParams tactileSwitchParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, toggle_button_height);
        RelativeLayout.LayoutParams tutorialSwitchParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, toggle_button_height);
        RelativeLayout.LayoutParams sliderParameters = new RelativeLayout.LayoutParams(slider_width, ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        backArrowParameters.setMargins(back_arrow_margin, back_arrow_margin, 0, 0);
        settingsIconParameters.setMargins(0, settings_icon_margin_top, 0, 0);
        settingsTitleParameters.setMargins(0, settings_title_margin_top, 0, 0);
        tactileLayoutParams.setMargins(0, settings_button_height, 0, 0);
        soundTrackParameters.setMargins(0, settings_button_height, 0, 0);
        sfxParameters.setMargins(0, settings_button_height, 0, 0);
        creditsParams.setMargins(0, settings_button_height, 0, 0);
        tutorialLayoutParams.setMargins(0, settings_button_height, 0, 0);
        resetProgressParameters.setMargins(0, settings_button_height, 0, 0);
        tactileSwitchParams.setMargins(0, 0, toggle_button_margin_right, 0);
        tutorialSwitchParams.setMargins(0, 0, toggle_button_margin_right, 0);
        sliderParameters.setMargins(0, 0, toggle_button_margin_right, 0);

        settingsIconParameters.addRule(RelativeLayout.BELOW, R.id.settings_back_arrow);
        settingsIconParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        settingsTitleParameters.addRule(RelativeLayout.BELOW, R.id.big_setting_ic);
        settingsTitleParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tutorialLayoutParams.addRule(RelativeLayout.BELOW, R.id.settings_title);
        tutorialLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tactileLayoutParams.addRule(RelativeLayout.BELOW, R.id.tutorial_layout);
        tactileLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        soundTrackParameters.addRule(RelativeLayout.BELOW, R.id.tactile_feedback_layout);
        soundTrackParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        sfxParameters.addRule(RelativeLayout.BELOW, R.id.soundtrack);
        sfxParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        resetProgressParameters.addRule(RelativeLayout.BELOW, R.id.sfx);
        resetProgressParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        creditsParams.addRule(RelativeLayout.BELOW, R.id.reset_progress);
        creditsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tactileSwitchParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tactileSwitchParams.addRule(RelativeLayout.CENTER_VERTICAL);
        tutorialSwitchParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tutorialSwitchParams.addRule(RelativeLayout.CENTER_VERTICAL);
        sliderParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        sliderParameters.addRule(RelativeLayout.CENTER_VERTICAL);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        backArrow.setLayoutParams(backArrowParameters);
        settingsIcon.setLayoutParams(settingsIconParameters);
        settingsTitle.setLayoutParams(settingsTitleParameters);
        sfxView.setLayoutParams(sfxParameters);
        soundtrackView.setLayoutParams(soundTrackParameters);
        tutorialLayout.setLayoutParams(tutorialLayoutParams);
        tactileLayout.setLayoutParams(tactileLayoutParams);
        resetProgress.setLayoutParams(resetProgressParameters);
        creditsView.setLayoutParams(creditsParams);
        toggleTactile.setLayoutParams(tactileSwitchParams);
        toggleTutorial.setLayoutParams(tutorialSwitchParams);
        sfxSlider.setLayoutParams(sliderParameters);
        soundTrackSlider.setLayoutParams(sliderParameters);

        if(initializing) {
            if(settingsLayout.getChildAt(settingsLayout.getChildCount() - 1).getId() != R.id.ad_view)
                settingsLayout.addView(adView, adParams);

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
                                declareViews(false);
                            }
                        }, 200);
                    }
                });
            }

            sfxSlider.getThumb().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            sfxSlider.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            soundTrackSlider.getThumb().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            soundTrackSlider.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            toggleTactile.getThumbDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            toggleTutorial.getThumbDrawable().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        }

        prepare_alert_pop_up(initializing);
        prepare_credits_pop_up(initializing);
    }

    private void declareGameSounds(){
        sfx_volume = savedSettings.getSfxVolume();
        soundtrack_volume = savedSettings.getSoundTrackVolume();
        tactile_on = savedSettings.getTactileState() == 1;
        if (settingsSounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                settingsSounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else settingsSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            soundEffect = settingsSounds.load(this, R.raw.menu_click, 1);
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
        if(tactileFeedback == null){
            tactileFeedback = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        }
    }

    private void playClickEffect(boolean vibrate){
        if(settingsSounds != null) {
            settingsSounds.play(soundEffect, sfx_volume, sfx_volume, 1, 0, 1.0f);
        }
        if(vibrate){
            if(SDK_VERSION >= 26) {
                tactileFeedback.vibrate(VibrationEffect.createOneShot(50, 255));
            } else tactileFeedback.vibrate(50);
        }
    }

    private void updateVolume(){
        if(backgroundMusic != null){
            backgroundMusic.setVolume(soundtrack_volume, soundtrack_volume);
        }
    }

    private void checkTutorialState(){
        tutorial_on = savedSettings.getTutorialState() == 1;
        toggleTutorial.setChecked(tutorial_on);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonActions(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playClickEffect(false);
                ActivitySettings.this.finish();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });

        resetProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playClickEffect(tactile_on);
                showResetProgressAlert();
            }
        });

        creditsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playClickEffect(false);
                showCreditsPopUp();
            }
        });

        tactileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTactile.toggle();
            }
        });

        tutorialLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTutorial.toggle();
            }
        });

        toggleTactile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tactile_on = b;
                playClickEffect(b);
            }
        });

        toggleTutorial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tutorial_on = b;
                playClickEffect(false);
            }
        });

        sfxSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sfx_volume = progress / 100.0f;
                updateVolume();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateVolume();
            }
        });

        soundTrackSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                soundtrack_volume = progress / 100.0f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateVolume();
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

        tactileLayout.setOnTouchListener(indicateTouch);
        tutorialLayout.setOnTouchListener(indicateTouch);
        creditsView.setOnTouchListener(indicateTouch);
        resetProgress.setOnTouchListener(indicateTouch);
    }

    private void prepare_credits_pop_up(boolean initializing){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        if(initializing) {
            if (popLayout == null)
                popLayout = new RelativeLayout(this);
            popView = new View(this);
            if (inflater != null)
                popView = inflater.inflate(R.layout.credits_pop_up, popLayout);
        }
        RelativeLayout popUpTextContainer = popView.findViewById(R.id.credits_window);
        TextView Title1 = popView.findViewById(R.id.programmer);
        TextView Name1 = popView.findViewById(R.id.programmer_name);
        TextView Title2 = popView.findViewById(R.id.artist);
        TextView Name2 = popView.findViewById(R.id.artist_name);
        TextView Title3 = popView.findViewById(R.id.sound);
        TextView Name3 = popView.findViewById(R.id.sound_guy);


        // create the popup window
        int popUpWidth = (int) Math.rint(screenHeight * 0.5);
        int popUpNameSize = (int) Math.rint(screenHeight*0.03);
        int popUpTitleSize = (int) Math.rint(popUpNameSize * 0.7);


        RelativeLayout.LayoutParams textContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popTitle1Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popName1Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popTitle2Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popName2Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popTitle3Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams popName3Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        popTitle2Params.setMargins(0, popUpNameSize/2, 0, 0);
        popTitle3Params.setMargins(0, popUpNameSize/2, 0, 0);
        textContainerParams.setMargins(popUpNameSize, popUpTitleSize, popUpNameSize, popUpTitleSize);

        textContainerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        popTitle1Params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        popTitle1Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        popName1Params.addRule(RelativeLayout.BELOW, R.id.programmer);
        popName1Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        popTitle2Params.addRule(RelativeLayout.BELOW, R.id.programmer_name);
        popTitle2Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        popName2Params.addRule(RelativeLayout.BELOW, R.id.artist);
        popName2Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        popTitle3Params.addRule(RelativeLayout.BELOW, R.id.artist_name);
        popTitle3Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        popName3Params.addRule(RelativeLayout.BELOW, R.id.sound);
        popName3Params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        popUpTextContainer.setLayoutParams(textContainerParams);
        Title1.setLayoutParams(popTitle1Params);
        Name1.setLayoutParams(popName1Params);
        Title2.setLayoutParams(popTitle2Params);
        Name2.setLayoutParams(popName2Params);
        Title3.setLayoutParams(popTitle3Params);
        Name3.setLayoutParams(popName3Params);

        Title1.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpTitleSize);
        Title1.setTextColor(getResources().getColor(R.color.app_text_color));
        Title1.setGravity(CENTER);
        Name1.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpNameSize);
        Name1.setTextColor(getResources().getColor(R.color.app_text_color));
        Name1.setGravity(CENTER);

        Title2.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpTitleSize);
        Title2.setTextColor(getResources().getColor(R.color.app_text_color));
        Title2.setGravity(CENTER);
        Name2.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpNameSize);
        Name2.setTextColor(getResources().getColor(R.color.app_text_color));
        Name2.setGravity(CENTER);

        Title3.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpTitleSize);
        Title3.setTextColor(getResources().getColor(R.color.app_text_color));
        Title3.setGravity(CENTER);
        Name3.setTextSize(TypedValue.COMPLEX_UNIT_PX, popUpNameSize);
        Name3.setTextColor(getResources().getColor(R.color.app_text_color));
        Name3.setGravity(CENTER);

        if(initializing) {
            if(creditsPopUp == null) {
                creditsPopUp = new PopupWindow(popView);
                creditsPopUp.setWidth(popUpWidth);
                creditsPopUp.setHeight(WRAP_CONTENT);
                creditsPopUp.setFocusable(true);
            }

            popView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.performClick();
                    dismissPopUp();
                    return true;
                }
            });

            creditsPopUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    settingsLayout.setAlpha(1.0f);
                }
            });


            creditsPopUp.setAnimationStyle(R.style.center_pop_up_anim);
        }

    }

    private void dismissPopUp(){
        if(creditsPopUp.isShowing()){
            creditsPopUp.dismiss();
        }
    }

    private Drawable getCreditsBackground(){
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
        backgroundDrawable.setSize(0, 0);

        return backgroundDrawable;
    }

    private void prepare_alert_pop_up(boolean initializing){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        if(alertLayout == null)
            alertLayout = new RelativeLayout(this);
        View popView = new View(this);
        if (inflater != null)
            popView = inflater.inflate(R.layout.reset_progress_pop_up, alertLayout);
        RelativeLayout popUpTextContainer = popView.findViewById(R.id.alert_window);
        TextView alertMessage = popView.findViewById(R.id.alert_message);
        TextView alertTitle = popView.findViewById(R.id.alert_title);
        RelativeLayout alertAction = popView.findViewById(R.id.alert_action);
        TextView actionResetProgress = popView.findViewById(R.id.action1);
        actionResetProgress.setText(R.string.reset_progress);
        TextView actionCancel = popView.findViewById(R.id.action_dismiss);
        actionCancel.setText(R.string.cancel);



        // create the popup window
        int popUpWidth = (int) Math.rint(screenHeight * 0.5);
        int popUpHeight = (int) Math.rint(popUpWidth*0.5267);
        int alertTitleSize = (int) Math.rint(screenHeight*0.03);
        int alertMessageSize = (int) Math.rint(alertTitleSize * 0.7);
        int alertActionSize = (int) Math.rint(alertTitleSize * 0.9);
        int alertActionPadding = (int) Math.rint(alertActionSize * 0.3);

        RelativeLayout.LayoutParams textContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams alertMessageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams alertTitleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams alertActionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams actionResetParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams actionCancelParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        alertActionParams.setMargins(0, alertTitleSize/2, 0, 0);
        textContainerParams.setMargins(alertTitleSize, alertMessageSize, alertTitleSize, alertMessageSize);
        actionCancelParams.setMargins(0, 0, alertMessageSize*2, 0);
        actionResetParams.setMargins(alertMessageSize*2, 0, 0, 0);

        textContainerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        alertTitleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        alertTitleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        alertMessageParams.addRule(RelativeLayout.BELOW, R.id.alert_title);
        alertMessageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        alertActionParams.addRule(RelativeLayout.BELOW, R.id.alert_message);
        alertActionParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        actionResetParams.addRule(RelativeLayout.RIGHT_OF, R.id.action_dismiss);

        popUpTextContainer.setLayoutParams(textContainerParams);
        alertMessage.setLayoutParams(alertMessageParams);
        alertTitle.setLayoutParams(alertTitleParams);
        alertAction.setLayoutParams(alertActionParams);
        actionResetProgress.setLayoutParams(actionResetParams);

        alertMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, alertMessageSize);
        alertMessage.setTextColor(getResources().getColor(R.color.app_text_color));
        alertMessage.setGravity(CENTER);
        alertTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, alertTitleSize);
        alertTitle.setTextColor(getResources().getColor(R.color.app_text_color));
        alertTitle.setGravity(CENTER);

        actionCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, alertActionSize);
        actionCancel.setTextColor(getResources().getColor(R.color.app_text_color));
        actionCancel.setBackground(getActionBackground(R.id.action_dismiss, actionCancel));
        actionCancel.setPadding(alertActionPadding*2, alertActionPadding,
                alertActionPadding*2, alertActionPadding);
        actionCancel.setGravity(CENTER);
        actionResetProgress.setTextSize(TypedValue.COMPLEX_UNIT_PX, alertActionSize);
        actionResetProgress.setTextColor(getResources().getColor(R.color.app_text_color));
        actionResetProgress.setBackground(getActionBackground(R.id.action1, actionResetProgress));
        actionResetProgress.setPadding(alertActionPadding*2, alertActionPadding,
                alertActionPadding*2, alertActionPadding);
        actionResetProgress.setGravity(CENTER);

        if(initializing) {
            if(alertPopUp == null) {
                alertPopUp = new PopupWindow(popView);
                alertPopUp.setWidth(popUpWidth);
                alertPopUp.setHeight(WRAP_CONTENT);
                alertPopUp.setFocusable(true);
            }

            alertPopUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    settingsLayout.setAlpha(1.0f);
                }
            });

            actionResetProgress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savedSettings.resetProgress();
                    checkTutorialState();
                    alertPopUp.dismiss();
                }
            });

            actionCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertPopUp.dismiss();
                }
            });

            alertPopUp.setAnimationStyle(R.style.center_pop_up_anim);
        }

    }

    private GradientDrawable getActionBackground(int action, View view){
        int alertTitleSize = (int) Math.rint(screenHeight*0.03);
        int alertActionSize = (int) Math.rint(alertTitleSize * 0.9);
        float[] corner_array = new float[8];
        corner_array[0] = alertActionSize*0.5f;
        corner_array[1] = alertActionSize*0.5f;
        corner_array[2] = alertActionSize*0.5f;
        corner_array[3] = alertActionSize*0.5f;
        corner_array[4] = alertActionSize*0.5f;
        corner_array[5] = alertActionSize*0.5f;
        corner_array[6] = alertActionSize*0.5f;
        corner_array[7] = alertActionSize*0.5f;
        GradientDrawable backgroundDrawable = new GradientDrawable();
        if(action == R.id.action_dismiss) backgroundDrawable.setColor(getResources().getColor(R.color.silver));
        else backgroundDrawable.setColor(getResources().getColor(R.color.green));
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadii(corner_array);
        backgroundDrawable.setSize(view.getWidth(), view.getHeight());

        return backgroundDrawable;
    }

    private void showResetProgressAlert(){

        RelativeLayout mRL = (RelativeLayout) alertLayout.getChildAt(0);
        TextView mTVTitle = (TextView) mRL.getChildAt(0);
        TextView mTVMessage = (TextView) mRL.getChildAt(1);
        RelativeLayout mRLAction = (RelativeLayout) mRL.getChildAt(2);
//        TextView mTVReset = (TextView) mRLAction.getChildAt(0);
//        TextView mTVCancel = (TextView) mRLAction.getChildAt(1);


        String title, message;

        title = "RESET PROGRESS?";
        message = "All progress not uploaded to cloud will be lost. " +
                "This cannot be reversed. To clear your progress from cloud, delete Retrospect's " +
                "backup from your Google account.";

        mTVTitle.setText(title);
        mTVMessage.setText(message);

        if(ActivitySettings.this.hasWindowFocus()) {
            int popUpWidth = (int) Math.rint(screenHeight * 0.5);
            int yLocation = (int) Math.rint(screenHeight * 0.45);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            if (ActivitySettings.this.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                if (!alertPopUp.isShowing()) {
                    alertLayout.setBackground(getAlertBackground());
                    alertPopUp.showAtLocation(settingsLayout, NO_GRAVITY, xLocation, yLocation);
                    settingsLayout.setAlpha(0.7f);
                }
            }
        }
    }

    private Drawable getAlertBackground(){
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
        backgroundDrawable.setSize(0, 0);

        return backgroundDrawable;
    }

    private void showCreditsPopUp(){
        if(ActivitySettings.this.hasWindowFocus()) {
            int popUpWidth = (int) Math.rint(screenHeight * 0.5);
            int yLocation = (int) Math.rint(screenHeight * 0.45);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            if (ActivitySettings.this.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                if (!creditsPopUp.isShowing()) {
                    popLayout.setBackground(getCreditsBackground());
                    creditsPopUp.showAtLocation(settingsLayout, NO_GRAVITY, xLocation, yLocation);
                    settingsLayout.setAlpha(0.7f);
                }
            }
        }
    }

    private void resume_needed_processes(){
        if(savedSettings == null) savedSettings = new Progress(this);
        declareGameSounds();
        checkTutorialState();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
        sfxSlider.setProgress((int) Math.rint(sfx_volume*100.0));
        soundTrackSlider.setProgress((int) Math.rint(soundtrack_volume*100.0));
        toggleTactile.setChecked(tactile_on);
        toggleTutorial.setChecked(tutorial_on);
    }

    private void release_heavy_processes(){
        savedSettings.finish();
        savedSettings = null;
        release_game_sound_pool_late();
        if(backgroundMusic != null) {
            PLAYBACK_PROGRESS = backgroundMusic.getCurrentPosition();
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    private void release_game_sound_pool_late(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (settingsSounds != null) {
                    settingsSounds.release();
                    settingsSounds = null;
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.settings);
        savedSettings = new Progress(this);
        getScreenSize(0);
        declareViews(true);
        setButtonActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        resume_needed_processes();
    }

    @Override
    protected void onPause(){
        savedSettings.saveSettings(tutorial_on ? 1 : 0, tactile_on ? 1 : 0, sfx_volume, soundtrack_volume);
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
