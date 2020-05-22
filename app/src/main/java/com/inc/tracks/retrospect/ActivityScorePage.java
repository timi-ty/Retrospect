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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
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
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;
import static android.view.Gravity.NO_GRAVITY;
import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityScorePage extends AppCompatActivity {

    public static final int ROLLING_SCORE = 0;
    public static final int POP_STAR = 1;
    public static final int SPRAY_CONFETTI = 2;
    public static final int GET_ACHIEVEMENT = 3;
    public static final int GAIN_COLLECTIBLE = 4;
    public static final int NEW_HIGH_SCORE = 5;
    public static final int GO_PLATINUM = 6;
    public static final int BUTTON_CLICKED = 7;
    public static final int SIGN_IN_ALERT = 213;
    public static final int OTHER_ERROR = 214;
    public static final int RC_SIGN_IN = 9001;
    final Object pauseLock = new Object();
    private AdView adView;
    GoogleSignInClient mGoogleSignInClient;
    private Progress currentProgress;
    private RelativeLayout notificationsLayout, alertLayout, scoreLayout;
    private PopupWindow alertPopUp;
    private View popView;
    private ImageView cancelIcon, replayIcon, proceedIcon, personalBestTitleView, starOne,
                        starTwo, starThree, medalView, confettiView, notificationImage;
    private TextView scoreNumberTextView, timeUsedTextView, nextStarTextView, personalBestScoreTextView,
                        personalBestTimeTextView, notificationTitle;
    private Activity mActivity = this;
    private HandlerThread scoreAnimThread;
    private ValueAnimator pulseStars;
    private AnimatedVectorDrawableCompat backVectorAnimation, confettiAnimation;
    private SoundPool scoreSounds;
    MediaPlayer backgroundMusic;
    AudioAttributes audioAttributes;
    Vibrator tactileFeedback;
    private int screenHeight, screenWidth, gameMode, difficulty, level, score, roundsCompleted,
            timeUsed, bestCombo, coinsEarned,
            pulse_worthy_stars, personalBestScore, personalBestTime;
    private int[] starSummary, soundEffect;
    private String nextStarString;
    private String[] achievementsQueue, rewardQueue;
    private Integer[] quantityQueue;
    private boolean newPersonalBest, scoreRolling, platinumObtained, freshlyCompleted,
            animationDone, attemptSignIn;
    private float sfx_volume;


    private class RollingScoreRunnable implements Runnable{
        private RollingScoreRunnable(){}
        private void setScoreTextOnUiThread(final int text){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scoreNumberTextView.setText(String.format(Locale.ENGLISH, "%d", text));
                }
            });
        }
        private void setNextTextOnUiThread(){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nextStarTextView.setText(nextStarString);
                    if(platinumObtained) {
                        pulseStars.setInterpolator(new AnticipateOvershootInterpolator());
                        confettiView.setVisibility(View.VISIBLE);
                        confettiAnimation.start();
                        playSoundEffect(SPRAY_CONFETTI);
                    }
                }
            });
        }
        private void popStarOnUiThread(final int i){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    popStarsIn(i);
                }
            });
        }
        @Override
        public void run(){
            try {
                Thread.sleep(1000);// lets the UI get comfortable before animation starts
            } catch (Exception e) {
                e.printStackTrace();
            }
            int rollingScore = 0;
            int stepSize = score/200 != 0 ? score/200 : 1;
            scoreRolling = true;
            int scoreCountSound = playSoundEffect(ROLLING_SCORE);
            rollingScoreLoop:
            for(int i = 1; i < 5; i++) {
                while (rollingScore < score) {
                    rollingScore += stepSize;
                    setScoreTextOnUiThread(rollingScore);
                    if (rollingScore >= starSummary[i] || !scoreRolling) {
                        if(score >= starSummary[i]) {
                            popStarOnUiThread(i);
                            rollingScore = starSummary[i];
                        }
                        scoreRolling = false;
                        while (!scoreRolling) {
                            synchronized (pauseLock) {
                                try {
                                    if(scoreSounds != null)
                                        scoreSounds.pause(scoreCountSound);
                                    pauseLock.wait(600);
                                    if (i >= starSummary[0])
                                        break rollingScoreLoop;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(scoreSounds != null)
                                    scoreSounds.resume(scoreCountSound);
                            }
                        }
                        break;
                    }
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            while (rollingScore < score && scoreRolling) {
                rollingScore += 5;
                setScoreTextOnUiThread(rollingScore);
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.println(Log.INFO, "Score", "Setting score to: " + score);
            setScoreTextOnUiThread(score);
            setNextTextOnUiThread();

            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(starSummary[0] >= Progress.ONE_STAR) animateMedal();
                }
            }, 200);
            if(starSummary[0] <= Progress.NO_STARS) animationDone = true;
            int i = 0;
            while(!animationDone) {
                synchronized (pauseLock){
                    try {
                        pauseLock.wait(10000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                i++; if(i > 1) break;
            }
            finalizeScorePage();

            if(scoreSounds != null)
                scoreSounds.stop(scoreCountSound);
        }
    }

    private class RollingScoreAndRoundsRunnable implements Runnable{
        private RollingScoreAndRoundsRunnable(){}
        private void setScoreTextOnUiThread(final int text){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scoreNumberTextView.setText(String.format(Locale.ENGLISH, "%d", text));
                }
            });
        }
        private void setNextTextOnUiThread(int rounds){
            if(rounds == 1)
                nextStarString = "You completed " + rounds + " round";
            else nextStarString = "You completed " + rounds + " rounds";
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nextStarTextView.setText(nextStarString);
                }
            });
        }
        private void popStarOnUiThread(final int i){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    popStarsIn(i);
                }
            });
        }
        @Override
        public void run(){
            try {
                Thread.sleep(1000);// lets the UI get comfortable before animation starts
            } catch (Exception e) {
                e.printStackTrace();
            }
            int rollingScore = 0; //value that counts upwards to final score
            int rollingRounds = 0; //value that counts upwards to final roundsCompleted
            int averageScorePerRound; //final score is divided into "roundsCompleted" places
            int stepSize = score/200 != 0 ? score/200 : 1;
            try {
                final double a = score, b = roundsCompleted;
                averageScorePerRound = (int) Math.rint(a / b);
            }catch (ArithmeticException infinite){
                infinite.printStackTrace();
                Log.println(Log.VERBOSE, "RollingRunnable", "Couldn't compute averageScorePerRound because rounds completed = " + roundsCompleted);
                averageScorePerRound = 0;
            }
            scoreRolling = true;
            int scoreCountSound = playSoundEffect(ROLLING_SCORE);
            rollingStarLoop:
            for(int i = 1; i < 5; i++) {
                /*starSummary[i] is the number of rounds required to obtain "i" star*/
                while (rollingRounds < starSummary[i] && rollingScore < score) {
                    rollingScore += stepSize;
                    setScoreTextOnUiThread(rollingScore);
                    rollingRounds++; //attempt to increase rollingRounds by 1 depending on the outcome of the statement below
                    if (rollingScore >= (averageScorePerRound*rollingRounds) && roundsCompleted > 0) {
                        setNextTextOnUiThread(rollingRounds);
                        if(rollingRounds >= starSummary[i]) {//award "i" star if rollingRounds is high enough
                            popStarOnUiThread(i);
                            rollingScore = (averageScorePerRound*rollingRounds);
                            scoreRolling = false;
                        }
                    } else rollingRounds--;//failed to increase the rolling rounds, thus, nullify increment and try again
                    while (!scoreRolling) {
                        if(rollingScore >= score) break rollingStarLoop;//stop counting once final score is reached
                        /*the statement below gets called when the screen is tapped to hasten score page animation*/
                        if(!(rollingRounds >= starSummary[i])){
                            if(roundsCompleted >= starSummary[i]){//checks how far the player is allowed to skip
                                rollingRounds = starSummary[i];//skips as far as possible
                                setNextTextOnUiThread(rollingRounds);
                                popStarOnUiThread(i);
                            } else break rollingStarLoop;//called when there are no more pending star animations
                            rollingScore = (averageScorePerRound*rollingRounds);
                        }
                        synchronized (pauseLock) {
                            try {
                                if(scoreSounds != null)
                                    scoreSounds.pause(scoreCountSound);
                                pauseLock.wait(600);//time it takes to play one star animation
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(scoreSounds != null)
                                scoreSounds.resume(scoreCountSound);
                        }
                    }
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            scoreRolling = true;
            //quickly counts towards final score when there are no more pending star animations
            while (rollingScore < score && scoreRolling) {
                rollingScore += 5;
                setScoreTextOnUiThread(rollingScore);
                try {
                    Thread.sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // jump to final score if screen is tapped again
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finalizeScorePage();
                }
            }, 200);
            Log.println(Log.INFO, "Score", "Setting score to: " + score);
            setScoreTextOnUiThread(score);
            setNextTextOnUiThread(roundsCompleted);
            animationDone = true;
            scoreSounds.stop(scoreCountSound);
        }
    }

    private void getScreenSize(int adHeight) {
        int tempScreenHeight = screenHeight;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;

        Log.d("Old screen height", "" + screenHeight);

        if(replayIcon != null) {
            int bottomMargin = screenHeight - replayIcon.getBottom();
            if ((adHeight*1.4) > bottomMargin)
                screenHeight = (int)Math.rint(screenHeight - (adHeight*1.4) + bottomMargin);
            else screenHeight  = tempScreenHeight;
            Log.d("view container margin", "" + bottomMargin);
        }

        Log.d("New Screen Height", "" + screenHeight);
    }

    private void viewDeclarations(boolean initializing) {

        RelativeLayout noteContainer = findViewById(R.id.note_container);
        ImageView whiteClock = findViewById(R.id.white_clock_ic);
        ImageView goldClock = findViewById(R.id.gold_clock_ic);
        ImageView scoreTitleView = findViewById(R.id.score_title);
        if(initializing) {
            scoreLayout = findViewById(R.id.score_page);
            replayIcon = findViewById(R.id.replay_ic);
            proceedIcon = findViewById(R.id.proceed_ic);
            cancelIcon = findViewById(R.id.cancel_ic_score);
            personalBestTitleView = findViewById(R.id.personal_best_text);
            scoreNumberTextView = findViewById(R.id.score_number);
            timeUsedTextView = findViewById(R.id.time_used);
            nextStarTextView = findViewById(R.id.next_star_text);
            personalBestScoreTextView = findViewById(R.id.personal_best_score);
            personalBestTimeTextView = findViewById(R.id.personal_best_time);
            starOne = findViewById(R.id.star1);
            starTwo = findViewById(R.id.star2);
            starThree = findViewById(R.id.star3);
            medalView = findViewById(R.id.medal);
            notificationsLayout = findViewById(R.id.notification);
            notificationImage = findViewById(R.id.notification_image);
            notificationTitle = findViewById(R.id.notification_title);

            notificationsLayout.setVisibility(View.INVISIBLE);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/4356482874");
            }
        }

        int score_title_height = (int) Math.rint(screenHeight * 0.039);
        int score_title_width = (int) Math.rint(score_title_height*4.4);
        int score_title_margin_top = (int) Math.rint(screenHeight *0.05306);
        int score_number_height = (int) Math.rint(screenHeight * 0.09);
        int score_number_margin_top = (int) Math.rint(screenHeight *0.01);
        int time_used_height = (int) Math.rint(screenHeight * 0.032);
        int time_used_margin_top = (int) Math.rint(screenHeight * 0.01);
        int white_clock_diameter = (int) Math.rint(screenHeight * 0.032);
        int white_clock_margin_bottom = (int) Math.rint(white_clock_diameter * 0.11);
        int white_clock_margin_right = (int) Math.rint(white_clock_diameter * 0.3);
        int white_clock_padding = (int) Math.rint(white_clock_diameter * 0.15);
        int stars_height = (int) Math.rint(screenHeight *0.0989);
        int stars_width = (int) Math.rint(stars_height*1.026);
        int stars_margin_top = (int) Math.rint(screenHeight *0.03);
        int stars_margin = (int) Math.rint(stars_width*0.25);
        int next_star_margin_top = (int) Math.rint(screenHeight * 0.015);
        int next_star_height = (int) Math.rint(screenHeight * 0.024);
        int actions_button_diameter = (int) Math.rint(screenHeight *0.1112);
        int actions_button_margin_side = (int) Math.rint(screenWidth *0.05);
        int actions_button_margin_top = (int) Math.rint(screenHeight *0.05);
        int medal_diameter = (int) Math.rint(screenHeight *0.08);
        int medal_margin_top = (int) Math.rint(screenHeight *0.01);
        int personal_best_height = (int) Math.rint(screenHeight *0.03646);
        int personal_best_width = (int) Math.rint(personal_best_height*4.64286);
        int personal_best_margin_top = (int) Math.rint(screenHeight * 0.01);
        int personal_best_score_height = (int) Math.rint(screenHeight *0.036456);
        int personal_best_time_height = (int) Math.rint(screenHeight *0.0219);
        int gold_clock_diameter = (int) Math.rint(screenHeight *0.0219);
        int gold_clock_margin_bottom = (int) Math.rint(gold_clock_diameter*0.11);
        int gold_clock_margin_right = (int) Math.rint(gold_clock_diameter*0.3);
        int gold_clock_padding = (int) Math.rint(gold_clock_diameter*0.15);
        int cancel_diameter = (int) Math.rint(screenHeight *0.06094);
        int cancel_margin = (int) Math.rint(screenHeight *0.015);
        int notification_height = (int) Math.rint(screenHeight *0.0989);
        int notification_image_side = (int) Math.rint(notification_height*0.8);
        int notification_margin = (int) Math.rint(notification_image_side*0.2);
        int notification_text_size = (int) Math.rint(notification_image_side*0.6);


        RelativeLayout.LayoutParams cancelParameters = new RelativeLayout.LayoutParams(cancel_diameter, cancel_diameter);
        RelativeLayout.LayoutParams scoreTitleParameters = new RelativeLayout.LayoutParams(score_title_width, score_title_height);
        RelativeLayout.LayoutParams scoreNumberParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams timeUsedParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams whiteClockParameters = new RelativeLayout.LayoutParams(white_clock_diameter, white_clock_diameter);
        RelativeLayout.LayoutParams starOneParameters = new RelativeLayout.LayoutParams(stars_width, stars_height);
        RelativeLayout.LayoutParams starTwoParameters = new RelativeLayout.LayoutParams(stars_width, stars_height);
        RelativeLayout.LayoutParams starThreeParameters = new RelativeLayout.LayoutParams(stars_width, stars_height);
        RelativeLayout.LayoutParams nextStarParameters = new RelativeLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams medalParams = new RelativeLayout.LayoutParams(medal_diameter, medal_diameter);
        RelativeLayout.LayoutParams personalBestParameters = new RelativeLayout.LayoutParams(personal_best_width, personal_best_height);
        RelativeLayout.LayoutParams personalBestScoreParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams personalBestTimeParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams goldClockParameters = new RelativeLayout.LayoutParams(gold_clock_diameter, gold_clock_diameter);
        RelativeLayout.LayoutParams replayIconParameters = new RelativeLayout.LayoutParams(actions_button_diameter, actions_button_diameter);
        RelativeLayout.LayoutParams proceedIconParameters = new RelativeLayout.LayoutParams(actions_button_diameter, actions_button_diameter);
        RelativeLayout.LayoutParams notificationParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, notification_height);
        RelativeLayout.LayoutParams noteParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams notificationImageParams = new RelativeLayout.LayoutParams(notification_image_side, notification_image_side);
        RelativeLayout.LayoutParams notificationTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        cancelParameters.setMargins(0, cancel_margin, cancel_margin, 0);
        scoreTitleParameters.setMargins(0, score_title_margin_top, 0, 0);
        scoreNumberParameters.setMargins(0, score_number_margin_top, 0, 0);
        timeUsedParameters.setMargins(0, time_used_margin_top, 0, 0);
        whiteClockParameters.setMargins(0, 0, white_clock_margin_right, white_clock_margin_bottom);
        starOneParameters.setMargins(0, stars_margin_top, stars_margin, 0);
        starTwoParameters.setMargins(stars_margin, stars_margin_top, 0, 0);
        starThreeParameters.setMargins(0, stars_margin_top, 0, 0);
        nextStarParameters.setMargins(0, next_star_margin_top, 0, 0);
        medalParams.setMargins(0, medal_margin_top, 0, 0);
        personalBestParameters.setMargins(0, personal_best_margin_top, 0, 0);
        goldClockParameters.setMargins(0, 0, gold_clock_margin_right, gold_clock_margin_bottom);
        replayIconParameters.setMargins(actions_button_margin_side, actions_button_margin_top, 0, 0);
        proceedIconParameters.setMargins(0, actions_button_margin_top, actions_button_margin_side, 0);
        notificationParams.setMargins(0, notification_margin, 0, 0);
        notificationImageParams.setMargins(notification_margin, 0, 0, 0);
        notificationTextParams.setMargins(notification_margin, 0, notification_margin, 0);


        notificationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        noteParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        notificationImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        notificationImageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        notificationTextParams.addRule(RelativeLayout.RIGHT_OF, R.id.notification_image);
        notificationTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
        cancelParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        scoreTitleParameters.addRule(RelativeLayout.BELOW, R.id.cancel_ic_score);
        scoreTitleParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scoreNumberParameters.addRule(RelativeLayout.BELOW, R.id.score_title);
        scoreNumberParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        timeUsedParameters.addRule(RelativeLayout.BELOW, R.id.score_number);
        timeUsedParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        whiteClockParameters.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.time_used);
        whiteClockParameters.addRule(RelativeLayout.LEFT_OF, R.id.time_used);
        starOneParameters.addRule(RelativeLayout.BELOW, R.id.time_used);
        starTwoParameters.addRule(RelativeLayout.BELOW, R.id.time_used);
        starThreeParameters.addRule(RelativeLayout.BELOW, R.id.time_used);
        starOneParameters.addRule(RelativeLayout.LEFT_OF, R.id.star3);
        starTwoParameters.addRule(RelativeLayout.RIGHT_OF, R.id.star3);
        starThreeParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        nextStarParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        nextStarParameters.addRule(RelativeLayout.BELOW, R.id.star3);
        medalParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        medalParams.addRule(RelativeLayout.BELOW, R.id.next_star_text);
        personalBestParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        personalBestParameters.addRule(RelativeLayout.BELOW, R.id.medal);
        personalBestScoreParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        personalBestScoreParameters.addRule(RelativeLayout.BELOW, R.id.personal_best_text);
        personalBestTimeParameters.addRule(RelativeLayout.BELOW, R.id.personal_best_score);
        personalBestTimeParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        goldClockParameters.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.personal_best_time);
        goldClockParameters.addRule(RelativeLayout.LEFT_OF, R.id.personal_best_time);
        replayIconParameters.addRule(RelativeLayout.BELOW, R.id.personal_best_time);
        proceedIconParameters.addRule(RelativeLayout.BELOW, R.id.personal_best_time);
        if(starSummary[0] == Progress.NO_STARS && gameMode != ActivityMainGame.ZEN){
            proceedIcon.setVisibility(View.GONE);
            replayIconParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        else {
            replayIconParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        if(gameMode == ActivityMainGame.ZEN ){
            proceedIcon.setImageResource(R.drawable.score_page_leaderboard_ic);
        }
        proceedIconParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        cancelIcon.setLayoutParams(cancelParameters);
        scoreTitleView.setLayoutParams(scoreTitleParameters);
        scoreNumberTextView.setLayoutParams(scoreNumberParameters);
        timeUsedTextView.setLayoutParams(timeUsedParameters);
        starOne.setLayoutParams(starOneParameters);
        starTwo.setLayoutParams(starTwoParameters);
        starThree.setLayoutParams(starThreeParameters);
        nextStarTextView.setLayoutParams(nextStarParameters);
        medalView.setLayoutParams(medalParams);
        personalBestTitleView.setLayoutParams(personalBestParameters);
        personalBestScoreTextView.setLayoutParams(personalBestScoreParameters);
        personalBestTimeTextView.setLayoutParams(personalBestTimeParameters);
        replayIcon.setLayoutParams(replayIconParameters);
        proceedIcon.setLayoutParams(proceedIconParameters);
        notificationsLayout.setLayoutParams(notificationParams);
        noteContainer.setLayoutParams(noteParams);
        notificationImage.setLayoutParams(notificationImageParams);
        notificationTitle.setLayoutParams(notificationTextParams);
        if (whiteClock != null) {
            whiteClock.setLayoutParams(whiteClockParameters);
            whiteClock.setPadding(white_clock_padding, white_clock_padding, white_clock_padding, white_clock_padding);
        }
        if (goldClock != null) {
            goldClock.setLayoutParams(goldClockParameters);
            goldClock.setPadding(gold_clock_padding, gold_clock_padding, gold_clock_padding, gold_clock_padding);
        }

        if(initializing) {
            if(scoreLayout.getChildAt(scoreLayout.getChildCount() - 1).getId() != R.id.ad_view)
                scoreLayout.addView(adView, adParams);
        }

        scoreNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, score_number_height);
        timeUsedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, time_used_height);
        nextStarTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, next_star_height);
        personalBestScoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, personal_best_score_height);
        personalBestTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, personal_best_time_height);
        notificationTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, notification_text_size*0.45f);


        if(initializing) {
            ImageView backVectorView = findViewById(R.id.back_vector);
            RelativeLayout.LayoutParams backVectorViewParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if (backVectorView != null) {
                backVectorView.setLayoutParams(backVectorViewParameters);
                backVectorAnimation = AnimatedVectorDrawableCompat.create(this, R.drawable.animate_snow_flake_vector);
                backVectorView.setImageDrawable(backVectorAnimation);
                backVectorView.setRotation(9.7f);
            }

            confettiView = findViewById(R.id.confetti_view);
            RelativeLayout.LayoutParams confettiVectorViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if (confettiView != null) {
                confettiView.setLayoutParams(confettiVectorViewParams);
                confettiAnimation = AnimatedVectorDrawableCompat.create(this, R.drawable.confetti_animator);
                confettiView.setImageDrawable(confettiAnimation);
            }

            proceedIcon.setVisibility(View.INVISIBLE);
            replayIcon.setVisibility(View.INVISIBLE);
            cancelIcon.setVisibility(View.INVISIBLE);

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

        float[] corner_array = new float[8];
        corner_array[0] = screenHeight *0.0390325f;
        corner_array[1] = screenHeight *0.0390325f;
        corner_array[2] = screenHeight *0.0390325f;
        corner_array[3] = screenHeight *0.0390325f;
        corner_array[4] = screenHeight *0.0390325f;
        corner_array[5] = screenHeight *0.0390325f;
        corner_array[6] = screenHeight *0.0390325f;
        corner_array[7] = screenHeight *0.0390325f;
        GradientDrawable notification_back = new GradientDrawable();
        notification_back.setColor(getResources().getColor(R.color.white));
        notification_back.setShape(GradientDrawable.RECTANGLE);
        notification_back.setCornerRadii(corner_array);
        notification_back.setSize(notificationParams.width, notificationParams.height);

        notificationsLayout.setBackground(notification_back);

        prepare_alert_pop_up(initializing);
    }

    private void declareGameSounds(){
        sfx_volume = currentProgress.getSfxVolume();
        float soundtrack_volume = currentProgress.getSoundTrackVolume();

        if(scoreSounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                scoreSounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else scoreSounds = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
            soundEffect = new int[9];
            soundEffect[0] = scoreSounds.load(this, R.raw.menu_click, 1);
            soundEffect[1] = scoreSounds.load(this, R.raw.achievement_chime, 1);
            soundEffect[2] = scoreSounds.load(this, R.raw.clock_break, 1);
            soundEffect[3] = scoreSounds.load(this, R.raw.collectible_gained, 1);
            soundEffect[4] = scoreSounds.load(this, R.raw.get_star, 1);
            soundEffect[5] = scoreSounds.load(this, R.raw.go_platinum, 1);
            soundEffect[6] = scoreSounds.load(this, R.raw.new_high_score, 1);
            soundEffect[7] = scoreSounds.load(this, R.raw.score_count, 1);
            soundEffect[8] = scoreSounds.load(this, R.raw.spray_confetti, 1);
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

    private int playSoundEffect(int playSound){
        if(scoreSounds != null) {
            switch (playSound) {
                case GET_ACHIEVEMENT:
                    return scoreSounds.play(soundEffect[1], sfx_volume, sfx_volume, 4, 0, 1.0f);
                case ROLLING_SCORE:
                    return scoreSounds.play(soundEffect[7], sfx_volume/2, sfx_volume/2, 5, -1, 1.0f);
                case POP_STAR:
                    return scoreSounds.play(soundEffect[4], sfx_volume, sfx_volume, 2, 0, 1.0f);
                case SPRAY_CONFETTI:
                    return scoreSounds.play(soundEffect[8], sfx_volume, sfx_volume, 1, -1, 1.0f);
                case GAIN_COLLECTIBLE:
                    return scoreSounds.play(soundEffect[3], sfx_volume, sfx_volume, 1, 0, 1.0f);
                case NEW_HIGH_SCORE:
                    return scoreSounds.play(soundEffect[6], sfx_volume, sfx_volume, 1, 0, 1.0f);
                case GO_PLATINUM:
                    return scoreSounds.play(soundEffect[5], sfx_volume, sfx_volume, 3, 0, 1.0f);
                case BUTTON_CLICKED:
                    return scoreSounds.play(soundEffect[0], sfx_volume, sfx_volume, 1, 0, 1.0f);
            }
        }
        return 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void uiClickables(){
        replayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect(BUTTON_CLICKED);
                Intent mainGameIntent = new Intent(ActivityScorePage.this, ActivityMainGame.class);
                if(gameMode != ActivityMainGame.ZEN) {// leaves the intent empty for zen mode
                    mainGameIntent.putExtra("dif", difficulty);
                    mainGameIntent.putExtra("level", level);
                }
                else {
                    mainGameIntent.putExtra("dif", -1);
                    mainGameIntent.putExtra("level", -1);
                }
                startActivity(mainGameIntent);
                ActivityScorePage.this.finish();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });

        proceedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect(BUTTON_CLICKED);
                if(gameMode != ActivityMainGame.ZEN) {
                    Intent mainGameIntent = new Intent(ActivityScorePage.this, ActivityMainGame.class);
                    if (level < 52) {
                        mainGameIntent.putExtra("dif", difficulty);
                        mainGameIntent.putExtra("level", level + 1);
                    } else if (difficulty < 4) {
                        mainGameIntent.putExtra("dif", difficulty + 1);
                        mainGameIntent.putExtra("level", 1);
                    } else {
                        mainGameIntent = new Intent(ActivityScorePage.this, ActivityHomeScreen.class);
                    }
                    startActivity(mainGameIntent);
                    ActivityScorePage.this.finish();
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                }
                else {
                    Intent mainGameIntent = new Intent(ActivityScorePage.this, ActivityLeaderBoard.class);
                    if(isSignedIn()){
                        startActivity(mainGameIntent);
                        ActivityScorePage.this.finish();
                        overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                    }
                    else{
                        handleActivityFailure(SIGN_IN_ALERT, new Exception());
                    }
                }
            }
        });

        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect(BUTTON_CLICKED);
                ActivityScorePage.this.finish();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });

        RelativeLayout scorePageLayout = findViewById(R.id.score_page);
        if (scorePageLayout != null) {
            scorePageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scoreRolling = false;
                }
            });
        }

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

        replayIcon.setOnTouchListener(indicateTouch);
        proceedIcon.setOnTouchListener(indicateTouch);
        cancelIcon.setOnTouchListener(indicateTouch);
    }

    private void getScoreData(){
        Intent intentMode = getIntent();
        Bundle modeBundle = intentMode.getExtras();
        if(modeBundle != null) {
            score = modeBundle.getInt("score", 0);
            Log.println(Log.INFO, "getScoreData", "Score = " + score);
            timeUsed = modeBundle.getInt("time used", 0);
            gameMode = modeBundle.getInt("game mode", 0);
            difficulty = modeBundle.getInt("dif", 0);
            level = modeBundle.getInt("level", 0);
            roundsCompleted = modeBundle.getInt("rounds", 0);
            bestCombo = modeBundle.getInt("best combo", 0);
            coinsEarned = modeBundle.getInt("bonus coins", 0);
        }
        starSummary = Achievements.getStarSummary(gameMode, difficulty, level, score, roundsCompleted);

        if(starSummary[5] == 0)
            platinumObtained = true;
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void transactWithDb(){
        currentProgress = new Progress(this);

        currentProgress.extractHighScoreData(gameMode, difficulty, level);
        boolean notPreviouslyCompleted = Achievements.getStarSummary(gameMode, currentProgress.pDifficulty,
                currentProgress.pLevel, currentProgress.highScore, currentProgress.pRoundsCompleted)[0] < Progress.ONE_STAR;

        newPersonalBest = currentProgress.updateCompletion(gameMode, difficulty, level, score,
                roundsCompleted, bestCombo, timeUsed, starSummary[0], this);

        currentProgress.extractHighScoreData(gameMode, difficulty, level);
        personalBestScore = currentProgress.highScore;
        personalBestTime = currentProgress.highScoreTime;

        freshlyCompleted = notPreviouslyCompleted && starSummary[0] >= Progress.ONE_STAR;
    }

    private void checkForAchievements(){
        Achievements playerAchievements = new Achievements();
        achievementsQueue = playerAchievements.checkForAchievements(isSignedIn(), this);
    }

    private void displayScoreData(){
        String digitalTimeUsed = getDigitalClockTime(timeUsed);
        timeUsedTextView.setText(digitalTimeUsed);
        if(platinumObtained)
            nextStarString = "Congratulations! You have achieved Platinum!";
        else nextStarString = "Next Star At " + starSummary[5];
        if(newPersonalBest){
            personalBestTitleView.setImageResource(R.drawable.personal_best);
            playSoundEffect(NEW_HIGH_SCORE);
        }
        String digitalPersonalBestTime = getDigitalClockTime(personalBestTime);
        personalBestScoreTextView.setText(String.format(Locale.ENGLISH, "%d", personalBestScore));
        personalBestTimeTextView.setText(digitalPersonalBestTime);
        scoreAnimThread = new HandlerThread("scoreAnimThread");
        scoreAnimThread.start();
        if(gameMode == ActivityMainGame.ZEN)
            new Handler(scoreAnimThread.getLooper()).post(new RollingScoreAndRoundsRunnable());
        else new Handler(scoreAnimThread.getLooper()).post(new RollingScoreRunnable());

        Handler notificationHandler = new Handler(getMainLooper());
        int delay = 1000;
        for(String identifier : achievementsQueue){
            final String s = identifier;
            notificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNotification(s);
                }
            }, delay);
            delay += 3500;
        }
        for(int i = 0; i < rewardQueue.length; i++){
            final String s = rewardQueue[i];
            final int q = quantityQueue[i];
            notificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNotification(s, q);
                }
            }, delay);
            delay += 3500;
        }

        animatePulsingItems();
    }

    public void awardCollectibles(){
        ArrayList<String> descriptor = new ArrayList<>();
        ArrayList<Integer> quantity = new ArrayList<>();

        int bonus_coins = 200;
        int bonus_break = 1;
        int bonus_clear = 1;
        int bonus_solve = 3;
        int bonus_slow = 2;

        if(freshlyCompleted && gameMode != ActivityMainGame.ZEN) {

            switch (difficulty) {
                case ActivityDifficultyMenu.INTERMEDIATE:
                    bonus_coins = bonus_coins * 2;
                    bonus_break = bonus_break * 2;
                    bonus_clear = bonus_clear * 2;
                    bonus_solve = bonus_solve * 2;
                    bonus_slow = bonus_slow * 2;
                    break;
                case ActivityDifficultyMenu.EXPERT:
                    bonus_coins = bonus_coins * 3;
                    bonus_break = bonus_break * 3;
                    bonus_clear = bonus_clear * 3;
                    bonus_solve = bonus_solve * 3;
                    bonus_slow = bonus_slow * 3;
                    break;
                case ActivityDifficultyMenu.EIDETIC:
                    bonus_coins = bonus_coins * 4;
                    bonus_break = bonus_break * 4;
                    bonus_clear = bonus_clear * 4;
                    bonus_solve = bonus_solve * 4;
                    bonus_slow = bonus_slow * 4;
                    break;
            }

            switch (level) {
                case 1:
                    descriptor.add(Progress.COINS);
                    quantity.add(bonus_coins);
                    descriptor.add(Progress.BREAK);
                    quantity.add(bonus_break);
                    descriptor.add(Progress.CLEAR);
                    quantity.add(bonus_clear);
                    descriptor.add(Progress.SOLVE);
                    quantity.add(bonus_solve);
                    descriptor.add(Progress.SLOW);
                    quantity.add(bonus_slow);
                    break;
                case 15:
                    descriptor.add(Progress.COINS);
                    quantity.add(bonus_coins);
                    break;
                case 25:
                    descriptor.add(Progress.COINS);
                    quantity.add(bonus_coins);
                    descriptor.add(Progress.BREAK);
                    quantity.add(bonus_break);
                    descriptor.add(Progress.CLEAR);
                    quantity.add(bonus_clear);
                    descriptor.add(Progress.SOLVE);
                    quantity.add(bonus_solve);
                    descriptor.add(Progress.SLOW);
                    quantity.add(bonus_slow);
                    break;
                case 52:
                    descriptor.add(Progress.COINS);
                    quantity.add(bonus_coins);
                    descriptor.add(Progress.BREAK);
                    quantity.add(bonus_break * 2);
                    descriptor.add(Progress.CLEAR);
                    quantity.add(bonus_clear);
                    descriptor.add(Progress.SOLVE);
                    quantity.add(bonus_solve);
                    descriptor.add(Progress.SLOW);
                    quantity.add(bonus_slow);
                    break;

            }
        }
        else if(gameMode == ActivityMainGame.ZEN){
            if(roundsCompleted >= 100){
                descriptor.add(Progress.COINS);
                quantity.add(bonus_coins);
                descriptor.add(Progress.BREAK);
                quantity.add(bonus_break * 2);
                descriptor.add(Progress.CLEAR);
                quantity.add(bonus_clear);
                descriptor.add(Progress.SOLVE);
                quantity.add(bonus_solve);
                descriptor.add(Progress.SLOW);
                quantity.add(bonus_slow);
            }
        }

        rewardQueue = descriptor.toArray(new String[0]);
        quantityQueue = quantity.toArray(new Integer[0]);

        if(rewardQueue.length > 0 && quantityQueue.length > 0){
            for(int i = 0; i < rewardQueue.length; i++){
                currentProgress.updateCollectibleBank(rewardQueue[i], quantityQueue[i]);
            }
        }

        currentProgress.updateCollectibleBank(Progress.COINS, coinsEarned);
        currentProgress.updateCollectibleStats(Progress.COINS, coinsEarned);
    }
    FloatEvaluator floatEvaluator = new FloatEvaluator() {
        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            return super.evaluate(fraction, startValue, endValue);
        }
    };
    public String getDigitalClockTime(long time){
        long millis, total_secs, secs, total_mins, mins, hours;
        String hours_prefix, mins_prefix, secs_prefix, millis_prefix;
        String digitalTime;

        millis = time % 1000;
        Log.println(Log.INFO, "ConvertingTime", "Millis = " + millis);
        total_secs = (time - millis) / 1000;
        secs = total_secs % 60;
        Log.println(Log.INFO, "ConvertingTime", "Seconds = " + secs);
        total_mins = (total_secs - secs) / 60;
        mins = total_mins % 60;
        Log.println(Log.INFO, "ConvertingTime", "Minutes = " + mins);
        hours = (total_mins - mins) / 60;
        Log.println(Log.INFO, "ConvertingTime", "Hours = " + hours);

        millis_prefix = ".";
        secs_prefix = ":";
        mins_prefix = hours_prefix = "";

        if(millis < 100 && millis >= 10){
            millis_prefix = ".0";
        }
        else if(millis < 10){
            millis_prefix = ".00";
        }

        if(secs < 10){
            secs_prefix = ":0";
        }

        if(mins < 10){
            mins_prefix = "0";
        }

        if(hours < 10 && hours > 0){
            hours_prefix = "0";
        }

        if(hours > 0){
            mins_prefix = ":" + mins_prefix;
            digitalTime = hours_prefix + hours + mins_prefix + mins + secs_prefix + secs + millis_prefix + millis;
        }
        else digitalTime = mins_prefix + mins + secs_prefix + secs + millis_prefix + millis;


        return digitalTime;
    }

    private void popStarsIn(final int star_to_pop_in){
        final ValueAnimator popStarIn = ValueAnimator.ofObject(floatEvaluator, 0f, 1.0f);
        popStarIn.setDuration(500);
        popStarIn.setInterpolator(new OvershootInterpolator());
        popStarIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float scale = (float)scaleAnimator.getAnimatedValue();

                switch (star_to_pop_in) {
                    case 1:
                        starOne.setScaleX(scale);
                        starOne.setScaleY(scale);
                        break;
                    case 2:
                        starTwo.setScaleX(scale);
                        starTwo.setScaleY(scale);
                        break;
                    case 3:
                        starThree.setScaleX(scale);
                        starThree.setScaleY(scale);
                        break;
                    case 4:
                        starOne.setScaleX(scale);
                        starOne.setScaleY(scale);
                        starTwo.setScaleX(scale);
                        starTwo.setScaleY(scale);
                        starThree.setScaleX(scale);
                        starThree.setScaleY(scale);
                        break;
                }
            }
        });
        popStarIn.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                synchronized (pauseLock) {
                    scoreRolling = true;
                    pauseLock.notifyAll();
                }
                pulse_worthy_stars = star_to_pop_in;
            }
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                switch (star_to_pop_in) {
                    case 1:
                        starOne.setImageResource(R.drawable.gold_star);
                        break;
                    case 2:
                        starTwo.setImageResource(R.drawable.gold_star);
                        break;
                    case 3:
                        starThree.setImageResource(R.drawable.gold_star);
                        break;
                    case 4:
                        starOne.setImageResource(R.drawable.platinum_star);
                        starTwo.setImageResource(R.drawable.platinum_star);
                        starThree.setImageResource(R.drawable.platinum_star);
                        playSoundEffect(GO_PLATINUM);
                        popStarIn.setStartDelay(500);
                        break;
                }
                playSoundEffect(POP_STAR);
            }
        });
        popStarIn.start();
    }

    private void animatePulsingItems(){
        pulseStars = ValueAnimator.ofObject(floatEvaluator, 1.0f, 1.1f);
        pulseStars.setDuration(500);
        pulseStars.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float floating = (float)scaleAnimator.getAnimatedValue();

                if(pulse_worthy_stars >= Progress.ONE_STAR) {
                    starOne.setScaleX(floating);
                    starOne.setScaleY(floating);
                    starOne.setAlpha(floating);
                }
                if(pulse_worthy_stars >= Progress.TWO_STARS) {
                    starTwo.setScaleX(floating);
                    starTwo.setScaleY(floating);
                    starTwo.setAlpha(floating);
                }
                if (pulse_worthy_stars >= Progress.THREE_STARS) {
                    starThree.setScaleX(floating);
                    starThree.setScaleY(floating);
                    starThree.setAlpha(floating);
                }
                if(newPersonalBest){
                    personalBestTitleView.setScaleX(floating);
                    personalBestTitleView.setScaleY(floating);
                    personalBestTitleView.setAlpha(floating);
                }
            }
        });
        pulseStars.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){ }
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){}
        });
        pulseStars.setRepeatMode(ValueAnimator.REVERSE);
        pulseStars.setRepeatCount(ValueAnimator.INFINITE);
        pulseStars.start();
    }

    private void playNotification(){
        final ValueAnimator notifyAnimationIn = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 1f);
        final ValueAnimator notifyAnimationOut = ValueAnimator.ofObject(new FloatEvaluator(), 1f, 0f);

        notifyAnimationIn.setDuration(500);
        notifyAnimationIn.setInterpolator(new OvershootInterpolator());
        notifyAnimationIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float bubbleIn = (float) animation.getAnimatedValue();

                notificationsLayout.setScaleX(bubbleIn);
                notificationsLayout.setScaleY(bubbleIn);
            }
        });
        notifyAnimationIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                notificationsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                notifyAnimationOut.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        notifyAnimationOut.setDuration(500);
        notifyAnimationOut.setInterpolator(new AnticipateOvershootInterpolator());
        notifyAnimationOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float bubbleOut = (float) animation.getAnimatedValue();

                notificationsLayout.setScaleX(bubbleOut);
                notificationsLayout.setScaleY(bubbleOut);
            }
        });
        notifyAnimationOut.setStartDelay(1000);

        notifyAnimationIn.start();
    }

    private void updateNotification(String notifier){
        notificationImage.setImageResource(Achievements.getAchievementImage(notifier, 1));
        notificationTitle.setText(notifier);

        playNotification();

        playSoundEffect(GET_ACHIEVEMENT);
    }

    private void updateNotification(String notifier, int quantity){
        int imageResource = 0;
        String prefix = "Congrats! You earned ";
        String suffix = "";
        switch (notifier){
            case Progress.COINS:
                imageResource = R.drawable.continue_honeycomb;
                suffix = quantity == 1 ? " free coins" : " free coins";
                break;
            case Progress.BREAK:
                imageResource = R.drawable.ic_break_free;
                suffix = quantity == 1 ? " free break power-up" : " free break power-ups";
                break;
            case Progress.CLEAR:
                imageResource = R.drawable.ic_clear_free;
                suffix = quantity == 1 ? " free clear power-up" : " free clear power-ups";
                break;
            case Progress.SOLVE:
                imageResource = R.drawable.ic_solve_free;
                suffix = quantity == 1 ? " free solve power-up" : " free solve power-ups";
                break;
            case Progress.SLOW:
                imageResource = R.drawable.ic_slow_down_free;
                suffix = quantity == 1 ? " free slow power-up" : " free slow power-ups";
                break;
        }
        String message = prefix + quantity + suffix;

        notificationImage.setImageResource(imageResource);
        notificationTitle.setText(message);

        playNotification();

        playSoundEffect(GAIN_COLLECTIBLE);
    }

    private void animateMedal(){
        final ValueAnimator medalAnimator = ValueAnimator.ofObject(floatEvaluator, 0, 2);
        final ValueAnimator medalPulse = ValueAnimator.ofObject(floatEvaluator, 2, 2.1);

        medalAnimator.setDuration(2100);
        medalAnimator.setInterpolator(new OvershootInterpolator());
        medalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float morph = (float) animation.getAnimatedValue();
                float rotation = medalAnimator.getDuration() == 2100 ?
                        morph * 900.0f : morph * 720;

                medalView.setScaleY(morph);
                medalView.setScaleX(morph);
                medalView.setRotationY(rotation);
            }
        });
        medalAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                medalView.setImageResource(Achievements.getMedalObtained(gameMode, difficulty,
                        level, score));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(medalAnimator.getDuration() == 2100)
                    medalPulse.start();
                else if(medalAnimator.getDuration() == 2000){
                    synchronized (pauseLock){
                        animationDone = true;
                        pauseLock.notifyAll();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        medalPulse.setDuration(500);
        medalPulse.setInterpolator(new AnticipateOvershootInterpolator());
        medalPulse.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float pulse = (float) animation.getAnimatedValue();

                medalView.setScaleX(pulse);
                medalView.setScaleY(pulse);
            }
        });
        medalPulse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                medalAnimator.setObjectValues(2, 1);
                medalAnimator.setDuration(2000);
                medalAnimator.setInterpolator(new AnticipateOvershootInterpolator());
                medalAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        medalPulse.setRepeatCount(3);
        medalPulse.setRepeatMode(ValueAnimator.REVERSE);

        medalAnimator.start();
    }

    private void finalizeScorePage(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inflateButtons();
            }
        });
    }

    private void inflateButtons(){
        ValueAnimator mainGameInflater = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 1f);
        mainGameInflater.setDuration(500);
        mainGameInflater.setInterpolator(new OvershootInterpolator());
        mainGameInflater.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float appear = (float) valueAnimator.getAnimatedValue();
                replayIcon.setScaleX(appear);
                replayIcon.setScaleY(appear);
                cancelIcon.setScaleX(appear);
                cancelIcon.setScaleY(appear);
                proceedIcon.setScaleX(appear);
                proceedIcon.setScaleY(appear);
            }
        });
        mainGameInflater.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                replayIcon.setVisibility(View.VISIBLE);
                cancelIcon.setVisibility(View.VISIBLE);
                if(starSummary[0] <= Progress.NO_STARS && gameMode != ActivityMainGame.ZEN){
                    proceedIcon.setVisibility(View.GONE);
                }
                else {
                    proceedIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mainGameInflater.start();
    }

    private void prepare_alert_pop_up(boolean initializing){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        if(initializing) {
            if (alertLayout == null)
                alertLayout = new RelativeLayout(this);
            popView = new View(this);
            if (inflater != null)
                popView = inflater.inflate(R.layout.alert_pop_up, alertLayout);
        }
        RelativeLayout popUpTextContainer = popView.findViewById(R.id.alert_window);
        RelativeLayout alertAction = popView.findViewById(R.id.alert_action);
        ImageView actionSignIn = popView.findViewById(R.id.action1);
        ImageView actionDismiss = popView.findViewById(R.id.action_dismiss);



        // create the popup window
        int popUpWidth = (int) Math.rint(screenHeight * 0.5);
        int popUpHeight = (int) Math.rint(popUpWidth * 0.4644);
        int buttonWidth = (int) Math.rint(popUpWidth*0.3);
        int buttonHeight = (int) Math.rint(buttonWidth * 0.3606656);
        int margin1 = (int) Math.rint(screenHeight*0.03);
        int margin2 = (int) Math.rint(margin1 * 0.7);

        RelativeLayout.LayoutParams textContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams alertActionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams actionSignInParams = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
        RelativeLayout.LayoutParams actionDismissParams = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);

        alertActionParams.setMargins(0, margin1*3, 0, 0);
        textContainerParams.setMargins(margin1, margin2, margin1, margin2);
        actionDismissParams.setMargins(0, 0, margin2, 0);
        actionSignInParams.setMargins(margin2, 0, 0, 0);

        textContainerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        alertActionParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        actionSignInParams.addRule(RelativeLayout.RIGHT_OF, R.id.action_dismiss);

        popUpTextContainer.setLayoutParams(textContainerParams);
        alertAction.setLayoutParams(alertActionParams);
        actionSignIn.setLayoutParams(actionSignInParams);
        actionDismiss.setLayoutParams(actionDismissParams);

        if(initializing) {
            if(alertPopUp == null) {
                alertPopUp = new PopupWindow(popView);
                alertPopUp.setWidth(popUpWidth);
                alertPopUp.setHeight(popUpHeight);
                alertPopUp.setFocusable(true);
            }

            alertPopUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    scoreLayout.setAlpha(1.0f);
                    currentProgress.saveSignInPreference(attemptSignIn);
                }
            });

            actionSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                    attemptSignIn = true;
                    alertPopUp.dismiss();
                    playSoundEffect(BUTTON_CLICKED);
                }
            });

            actionDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptSignIn = false;
                    alertPopUp.dismiss();
                    playSoundEffect(BUTTON_CLICKED);
                }
            });

            alertPopUp.setAnimationStyle(R.style.center_pop_up_anim);
        }

    }


    private void showAlert(final int alertType){

        RelativeLayout mRL = (RelativeLayout) alertLayout.getChildAt(0);
        RelativeLayout mRLAction = (RelativeLayout) mRL.getChildAt(0);

        mRLAction.setVisibility(View.GONE);

        int backgroundResource;

        switch (alertType){
            case OTHER_ERROR:
                backgroundResource = R.drawable.offline_alert;
                break;
            case SIGN_IN_ALERT:
                mRLAction.setVisibility(View.VISIBLE);
                backgroundResource = R.drawable.sign_in_alert;
                break;
            default:
                return;
        }

        if(ActivityScorePage.this.hasWindowFocus()) {
            int popUpWidth = (int) Math.rint(screenHeight * 0.5);
            int yLocation = (int) Math.rint(screenHeight * 0.45);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            if (ActivityScorePage.this.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                if (!alertPopUp.isShowing()) {
                    alertLayout.setBackground(getResources().getDrawable(backgroundResource));
                    alertPopUp.showAtLocation(scoreLayout, NO_GRAVITY, xLocation, yLocation);
                    scoreLayout.setAlpha(0.7f);
                }
            }
        }
    }


    private void handleActivityFailure(int alertType, Exception e){
        showAlert(alertType);
        e.printStackTrace();
    }

    private void getSinInClient(){
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
    }

    private void resume_needed_processes(){
        if(currentProgress == null) currentProgress = new Progress(this);
        if (backVectorAnimation != null)
            backVectorAnimation.start();
        declareGameSounds();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
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
                if (scoreSounds != null) {
                    scoreSounds.release();
                    scoreSounds = null;
                }
            }
        }).start();
    }

    private void release_heavy_processes(){
        if(backgroundMusic != null) {
            PLAYBACK_PROGRESS = backgroundMusic.getCurrentPosition();
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
        currentProgress.finish();
        currentProgress = null;
        release_game_sound_pool_late();
        scoreRolling = false;
        if (backVectorAnimation != null)
            backVectorAnimation.stop();
        if(scoreAnimThread != null){
            scoreAnimThread.quit();
            scoreAnimThread = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.score_page);
        getScreenSize(0);
        getSinInClient();
        getScoreData();
        viewDeclarations(true);
        uiClickables();
        transactWithDb();
        awardCollectibles();
        currentProgress.openUnlockedDifficulties();
        checkForAchievements();
        displayScoreData();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                onDisconnected();
                handleActivityFailure(OTHER_ERROR, apiException);
            }
        }
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        attemptSignIn = googleSignInAccount != null;
        if(currentProgress != null){
            currentProgress.updateCompletion(gameMode, difficulty, level, score, roundsCompleted,
                    bestCombo, timeUsed, starSummary[0], this);
        }
        // update again to try pushing the score to the leader boards
        if(isSignedIn()) proceedIcon.performClick();
    }

    private void onDisconnected() {

    }
}

