package com.inc.tracks.retrospect;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Locale;

import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static android.util.Log.INFO;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.NO_GRAVITY;
import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;


public class ActivityMainGame extends AppCompatActivity implements RewardedVideoAdListener {
    public static final int ZEN = 0;
    public static final int RETROSPECT = 1;
    public static final int DON_TAP = 2;
    public static final int LEVEL_STARTED = 3;
    public static final int ROUND_COMPLETED = 4;
    public static final int WRONG_SELECTION = 5;
    public static final int TIME_UP = 6;
    public static final int CARD_CLICKED = 7;
    public static final int GET_COIN = 9090;
    public static final int BAD_EGG_IGNORED = 71;
    public static final int TIME_BONUS = 8;
    public static final int JUST_CONTINUE = 9;
    public static final int RESET_TIME = 10;
    public static final int RESET_ROUND = 11;
    public static final int RESET_LEVEL = 12;
    public static final int COMBO_PLUS = 13;
    public static final int COMBO_LOST = 131;
    public static final int IN_BLITZ = -113;
    public static final int RESET_BLITZ = -13;
    public static final int AWARD_BLITZ = -132;
    public static final int FIREWORKS = -131;
    public static final int LOST_WINDOW_FOCUS = 14;
    public static final int FAILED_GAME = 15;
    public static final int WAITING_FOR_RESULT = -15;
    public static final int CLOCK_BREAK = 16;
    public static final int MULTIPLIER_PLUS = -16;
    public static final int AUTO_SOLVE = 17;
    public static final int SLOWED_DOWN = 18;
    public static final int RETRYING = 19;
    public static final int BUTTON_CLICKED = 20;
    public static final int CARD_FLIPPED = 21;
    public static final int TIME_LOW = 22;
    public static final int LEVEL_COMPLETED = 23;
    public static final int TUTORIAL_RESET = 24;
    public static final int SHOW_WELCOME = 25;
    public static final int RETROSPECT_HINT = 26;
    public static final int RETROSPECT_CLICK_HINT = 27;
    public static final int DON_TAP_HINT = 28;
    public static final int DON_TAP_CLICK_HINT = 29;
    public static final int POWER_UP_HINT = 30;
    public static final int SOLVE_HINT = 31;
    public static final int CLEAR_HINT = -31;
    public static final int BREAK_HINT = 32;
    public static final int SLOW_HINT = 33;
    public static final int RETRY_HINT = 34;
    public static final int ROUNDS_HINT = 35;
    public static final int TIME_BAR_HINT = 36;
    public static final int WELL_DONE = 37;
    public static final int TUTORIAL_POP_UP = 38;
    public static final int TUTORIAL_COMPLETE = 39;
    public static final int SHOW_STAGE_START_ROUND = 40;
    public static final int START_RETRO_ZEN = 401;
    public static final int SHOW_STAGE_CONTINUE = 41;
    public static final int HIDE_PLAY_BUTTON_START_ROUND = 42;
    public static final int HIDE_PLAY_BUTTON_SHOW_BAD_EGG = 43;
    public static final int HIDE_PLAY_BUTTON_CONTINUE = 44;
    public static final int SHOW_BAD_EGG = 45;
    public static final int NO_SPECIAL_CASE = 46;
    public static final int BREAK_COST = 200;
    public static final int CLEAR_COST = 150;
    public static final int SOLVE_COST = 50;
    public static final int SLOW_COST = 100;
    public static final int BASE_CONTINUE_COST = 200;
    public static final int COMBO_BAR_MAX = 2000;
    public static int PLAYBACK_PROGRESS = 0;
    private MainGameCoClass mainGameCoClass;
    private Progress mProgress;
    private final Object pauseLock = new Object();
    private final Object restartLock = new Object();
    private final Object timerLock = new Object();
    private final Object tutorialPauseLock = new Object();
    private final Object countDownLock = new Object();
    private final Activity mActivity = this;
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd interstitialAd;
    private RelativeLayout mainGameLayout, backDrop, powerUpsContainer, timeBarContainer, popLayout,
            badEggInfo, coinsLayout, blitzContainer, buyContinueButton;
    private HandlerThread gameThread, timerThread, timeBarThread, taskThread, tutorialThread;
    private ImageView[] cardView;
    private ImageView playButton, retryIcon, cancelIcon, solveIcon, breakClearIcon, slowIcon,
            badCard, movingCoins, clockIcon, explode1, explode2, explode3, tutorialGif, honeyJar,
            commentImage, watchAdToContinueButton;
    private PopupWindow popupWindow;
    private TextView scoreText, roundsText, multiplierText, commendationsText, countDownText,
            coinsAvailable, breakClearUnits, solveUnits, bonusCoinsText,
            slowUnits, continueCoins;
    private View viewToFade;
    private ProgressBar timeBar, blitzBar1, blitzBar2, blitzBar3, loadingAdBar;
    private SoundPool gameSounds;
    private MediaPlayer backgroundMusic;
    private AudioAttributes audioAttributes;
    private Vibrator tactileFeedback;
    private Handler gameThreadHandler, timerThreadHandler, timeBarThreadHandler, taskThreadHandler, uiHandler;
    private ValueAnimator mainGameInflater, wrongSelectionAction, cardFlipAnimator,
            commendationAnimator, cardShrinkAnimator, blitzMadness,
            fadeAnimator, scoreUpdateAnimator, movingCoinsAnim;
    private AnimatedVectorDrawableCompat explodeAnim1, explodeAnim2, explodeAnim3;
    private int dismissTutorialAction, cardBackResource, emptySlotResource, clockResource, difficulty, level,
            gameMode, zenActiveGameMode, score, clkPos, themeColor,
            cardSlots, roundsPlayed, loopIndex, extraCountdowns, timeAllowed, timeBarProgress,
            screenHeight, screenWidth, badEgg, failurePosition, cardToFlip, cardToShrink, fadeCase,
            continueCost, coinWallet, bonusCoins, breakPouch, clearPouch, solvePouch, slowPouch,
            coinPocket, commendationCount, multiplier, bestCombo, scoreUpdateReason, timeLowSound;
    private int[] activeIndex, soundEffect;
    private long levelStartTime, levelStopTime, roundStartTime, timeOfBreak;
    private double[] timeOfSet;
    private float sfx_volume;
    private volatile boolean tutorialUpdated, timeBarActive, timerNeeded, timeBarNeeded,
            gameRunning, restarting, levelCompleted,
            slowedDown, clockBroken;
    private boolean[] slotFilled;
    private volatile boolean[] cardIsPulsing, cardInitialized;
    private boolean inTutorial[], gameActive, timerActive, tactile_on, gameCreated,
            backButtonDormant, clockWiseCoinMove, isFreshLevel, inBlitzMode, hasFreeContinue;

    private final Integer[] cardResourceId = {
            R.drawable.ace_heart, R.drawable.two_heart, R.drawable.three_heart, R.drawable.four_heart,
            R.drawable.five_heart, R.drawable.six_heart, R.drawable.seven_heart, R.drawable.eight_heart,
            R.drawable.nine_heart, R.drawable.ten_heart, R.drawable.jack_heart, R.drawable.queen_heart,
            R.drawable.king_heart,
            R.drawable.ace_spade, R.drawable.two_spade, R.drawable.three_spade, R.drawable.four_spade,
            R.drawable.five_spade, R.drawable.six_spade, R.drawable.seven_spade, R.drawable.eight_spade,
            R.drawable.nine_spade, R.drawable.ten_spade, R.drawable.jack_spade, R.drawable.queen_spade,
            R.drawable.king_spade,
            R.drawable.ace_diamond, R.drawable.two_diamond, R.drawable.three_diamond, R.drawable.four_diamond,
            R.drawable.five_diamond, R.drawable.six_diamond, R.drawable.seven_diamond, R.drawable.eight_diamond,
            R.drawable.nine_diamond, R.drawable.ten_diamond, R.drawable.jack_diamond, R.drawable.queen_diamond,
            R.drawable.king_diamond,
            R.drawable.ace_club, R.drawable.two_club, R.drawable.three_club, R.drawable.four_club,
            R.drawable.five_club, R.drawable.six_club, R.drawable.seven_club, R.drawable.eight_club,
            R.drawable.nine_club, R.drawable.ten_club, R.drawable.jack_club, R.drawable.queen_club,
            R.drawable.king_club,
            R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g, R.drawable.h,
            R.drawable.i, R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o, R.drawable.p, R.drawable.r,
            R.drawable.s, R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
            R.drawable.z,

            R.drawable.avocado, R.drawable.banana, R.drawable.basketball, R.drawable.bomb, R.drawable.cheese,
            R.drawable.diamond, R.drawable.die, R.drawable.dna, R.drawable.eight_ball, R.drawable.football,
            R.drawable.game_controller, R.drawable.hotdog, R.drawable.hourglass, R.drawable.jigsaw,
            R.drawable.key, R.drawable.knife, R.drawable.light_bulb, R.drawable.maple_leaf, R.drawable.money_bag,
            R.drawable.pawn, R.drawable.pizza, R.drawable.skateboard, R.drawable.spider_web, R.drawable.sunflower,
            R.drawable.syringe, R.drawable.tennis_ball, R.drawable.test_tube, R.drawable.top_hat, R.drawable.umbrella,
            R.drawable.watermelon
    };

    private class RetrospectRoundRunnable implements Runnable {
        private void initializeRound(){
            Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 1);
            try {
                Thread.sleep(mainGameCoClass.flip_duration*2);// to make up for time during which card is flipping
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(gameMode == ZEN){
                for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                    if(!cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(cardBackResource);
                                cardInitialized[loopIndex] = true;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                        while (!gameRunning) {
                            if (restarting) {
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (loopIndex = 8; loopIndex >= cardSlots; loopIndex--) {
                    if (cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(emptySlotResource);
                                cardInitialized[loopIndex] = false;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                        while (!gameRunning) {
                            if (restarting) {
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }}
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else if (roundsPlayed == 0 || inTutorial[0]) {
                for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                    if (!cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(cardBackResource);
                                cardInitialized[loopIndex] = true;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Loop index is = " + loopIndex);
                        Log.println(INFO, "trackGameThread", "Card Slots is = " + cardSlots);
                        while (!gameRunning) {
                            if (restarting) {
                                Log.println(INFO, "trackGameThread", "returning...");
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 2);
                Log.println(INFO, "trackGameThread", "Sleep Time Is = " + mainGameCoClass.time);
                Thread.sleep((long) Math.rint((mainGameCoClass.flip_duration) * 1.5), 0);
            } catch (InterruptedException e) {
                if(restarting){
                    return;
                }
                e.printStackTrace();
            }
            make_view_active(slowIcon);

        }
        private void scrollCardsToMemorize(){
            for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (loopIndex < (mainGameCoClass.ord_index.length)) { // all checks of this nature are preventive measures against array out of bounds exception
                            cardView[0].setImageResource(cardResourceId[mainGameCoClass.ord_index[loopIndex]]);
                            flipCard(0);
                        }
                    }
                });
                Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                while (!gameRunning) {
                    if(restarting){
                        return;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainGameCoClass.scrollControl();
            }
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cardView[0].setImageResource(cardBackResource);
                    flipCard(0);
                }
            });
            Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 4);
        }
        private void setCardsOnStage(){
            for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((loopIndex < mainGameCoClass.shuffled_slots.length) && (loopIndex < mainGameCoClass.ran_index.length)) {
                            cardView[mainGameCoClass.shuffled_slots[loopIndex]].setImageResource(cardResourceId[mainGameCoClass.ran_index[loopIndex]]);
                            flipCard(mainGameCoClass.shuffled_slots[loopIndex]);
                        }
                    }
                });
                if ((loopIndex < mainGameCoClass.shuffled_slots.length) && (mainGameCoClass.shuffled_slots[loopIndex] < slotFilled.length)) {
                    slotFilled[mainGameCoClass.shuffled_slots[loopIndex]] = true;
                    if(!inTutorial[1]) make_view_active(cardView[mainGameCoClass.shuffled_slots[loopIndex]]);//power up tutorial will not be solved normally
                }
                try {
                    Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(loopIndex == cardSlots - 1){//escapes the delay between the last card flip and the starting of the timer
                    return;
                }
                mainGameCoClass.setControl();
                Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 5);
                while (!gameRunning) {
                    make_view_dormant(cardView[mainGameCoClass.shuffled_slots[loopIndex]]);// if game is paused in the middle of setting cards, this ensures that card stays inactive
                    if(restarting){
                        return;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        private void startTimer(){
            timerNeeded = true;
            timeBarNeeded = true;
            restartTimer();
            roundStartTime = System.currentTimeMillis();
            make_view_active(breakClearIcon);
            make_view_active(solveIcon);
        }
        @Override
        public void run() {
            gameActive = true;
            timerNeeded = false;
            timeBarNeeded = false;
            retrospectLoop:
            for (int i = 0; i < 1; i++) {
                while (!gameRunning) { //ensures that a new round can't start while the game is paused or broken.
                    if(restarting){
                       break retrospectLoop;
                    }
                    synchronized (pauseLock) { // anywhere you see a block like this is a point where that thread can be paused
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                initializeRound();
                if(restarting){
                    break;
                }
                if(inTutorial[0] || inTutorial[1]){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                       public void run() {
                            if(inTutorial[0]) showTutorialPopUp(RETROSPECT_HINT);
                            else if(inTutorial[1]) showTutorialPopUp(SLOW_HINT);
                        }
                    });
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                scrollCardsToMemorize();
                if(restarting){
                    break;
                }
                while (!gameRunning) {
                    if(restarting){
                        break retrospectLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(inTutorial[0]){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTutorialPopUp(RETROSPECT_CLICK_HINT);
                        }
                    });
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mainGameCoClass.scrollControl();
                if(restarting){
                    break;
                }
                setCardsOnStage();
                if(inTutorial[1]){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTutorialPopUp(BREAK_HINT);
                        }
                    });
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(restarting){
                    break;
                }
                while (!gameRunning) {
                    if(restarting){
                        break retrospectLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startTimer();
            }
            gameActive = false;
            if(restarting) { //lets restart operation know that this thread is terminated and ready to be restarted
                synchronized (restartLock) {
                    restartLock.notifyAll();
                }
            }
        }
    }

    private RetrospectRoundRunnable retrospectRoundRunnable;

    private class DonTapRoundRunnable implements Runnable {
        private void initializeRound(){
            Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 1);
            try {
                Thread.sleep(mainGameCoClass.flip_duration*2);// to make up for time during which card is flipping
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(gameMode == ZEN){
                for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                    if(!cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(cardBackResource);
                                cardInitialized[loopIndex] = true;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                        while (!gameRunning) {
                            if (restarting) {
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (loopIndex = 8; loopIndex >= cardSlots; loopIndex--) {
                    if (cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(emptySlotResource);
                                cardInitialized[loopIndex] = false;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                        while (!gameRunning) {
                            if (restarting) {
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else if (roundsPlayed == 0 || inTutorial[0]) {
                for (loopIndex = 0; loopIndex < cardSlots; loopIndex++) {
                    if (!cardInitialized[loopIndex]) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cardView[loopIndex].setImageResource(cardBackResource);
                                cardInitialized[loopIndex] = true;
                                flipCard(loopIndex);
                            }
                        });
                        Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 3);
                        while (!gameRunning) {
                            if (restarting) {
                                return;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(mainGameCoClass.flip_duration);// to make up for time during which card is flipping
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                Log.println(INFO, "trackGameThread", "Game Thread Is Here = " + gameThread + 2);
                Log.println(INFO, "trackGameThread", "Sleep Time Is = " + mainGameCoClass.time);
                Thread.sleep((long) Math.rint((mainGameCoClass.flip_duration) * 1.5), 0);
            } catch (InterruptedException e) {
                if(restarting){
                    return;
                }
                e.printStackTrace();
            }
        }
        private void donTapScrollActions(final int index){
            Log.println(INFO, "trackDoNotTap", "Displaying New Card " + index);

            if(activeIndex[mainGameCoClass.shuffled_slots[index]] == badEgg){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateScore(BAD_EGG_IGNORED, mainGameCoClass.shuffled_slots[index]);
                    }
                });

            }

            if(index == 1 && !slotFilled[mainGameCoClass.shuffled_slots[index]]){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardView[mainGameCoClass.shuffled_slots[index]].setImageResource(cardResourceId[badEgg]);
                        flipCard(mainGameCoClass.shuffled_slots[index]);
                        make_view_active(cardView[mainGameCoClass.shuffled_slots[index]]);
                    }
                });
                slotFilled[mainGameCoClass.shuffled_slots[index]] = true;
                timeOfSet[mainGameCoClass.shuffled_slots[index]] = System.currentTimeMillis() + mainGameCoClass.flip_duration;
                activeIndex[mainGameCoClass.shuffled_slots[index]] = badEgg;
                cardIsPulsing[mainGameCoClass.shuffled_slots[index]] = false;
                mainGameCoClass.scrollControl();
            }
            else if (!slotFilled[mainGameCoClass.shuffled_slots[index]] || activeIndex[mainGameCoClass.shuffled_slots[index]] == badEgg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardView[mainGameCoClass.shuffled_slots[index]].setImageResource(cardResourceId[mainGameCoClass.ran_index[mainGameCoClass.shuffled_slots[index]]]);
                        flipCard(mainGameCoClass.shuffled_slots[index]);
                        make_view_active(cardView[mainGameCoClass.shuffled_slots[index]]);
                    }
                });
                slotFilled[mainGameCoClass.shuffled_slots[index]] = true;
                timeOfSet[mainGameCoClass.shuffled_slots[index]] = System.currentTimeMillis() + mainGameCoClass.flip_duration;
                activeIndex[mainGameCoClass.shuffled_slots[index]] = mainGameCoClass.ran_index[mainGameCoClass.shuffled_slots[index]];
                cardIsPulsing[mainGameCoClass.shuffled_slots[index]] = false;
                mainGameCoClass.scrollControl();
            }
        }
        private void clearBadEggs(final int index){
            if (slotFilled[mainGameCoClass.shuffled_slots[index]] && activeIndex[mainGameCoClass.shuffled_slots[index]] == badEgg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardView[mainGameCoClass.shuffled_slots[index]].setImageResource(cardBackResource);
                        flipCard(mainGameCoClass.shuffled_slots[index]);
                        slotFilled[mainGameCoClass.shuffled_slots[index]] = false;
                    }
                });
                mainGameCoClass.scrollControl();
            }
        }
        public void run() {
            gameActive = true;
            timerNeeded = true;
            boolean check = true;
            donTapLoop:
            for (int j = 0; j < 1; j++) { // ghost loop to enable the implementation of the break keyword
                initializeRound();
                if(inTutorial[0]){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTutorialPopUp(DON_TAP_CLICK_HINT);
                        }
                    });
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(500, 0); // gives the player time to get ready
                } catch (InterruptedException e) {
                    if(restarting){
                        break;
                    }
                    e.printStackTrace();
                }
                for (int i = 0; i < 5; i++) {
                    Log.println(INFO, "DonTapLoop", "index i = " + i);
                    mainGameCoClass.indexGen(); //generates card resources needed for each cycle
                    while (!gameRunning) { //ensures that a new round can't start while the game is paused or broken.
                        if (restarting) {
                            break donTapLoop;
                        }
                        synchronized (pauseLock) {
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    for(int k = 0; k < cardSlots; k++) { //main game loop
                        Log.println(INFO, "trackDoNotTap", "Setting New Card " + k);
                        donTapScrollActions(k);
                        while (!gameRunning) { //game can be paused here
                            make_view_dormant(cardView[mainGameCoClass.shuffled_slots[k]]);// if card was being set when game was paused, this catches that card amd makes it dormant
                            if (restarting) {
                                break donTapLoop;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                for(int k = 0; k < cardSlots; k++) { //removes all lingering bad eggs from stage
                    clearBadEggs(k);
                    while (!gameRunning) {// game can be paused here
                        if (restarting) {
                            break donTapLoop;
                        }
                        synchronized (pauseLock) {
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                while (check) { //ensures that all slots are empty before declaring that round is complete.
                    check = false;
                    for (int i = 0; i < cardSlots; i++) {
                        check = check || slotFilled[i];
                    }
                    while (!gameRunning) {
                        if (restarting) {
                            break donTapLoop;
                        }
                        synchronized (pauseLock) {
                            try {
                                pauseLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                while (!gameRunning) {
                    if (restarting) {
                        break donTapLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                timerNeeded = false;
                if (!restarting) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(inTutorial[0]) showTutorialPopUp(WELL_DONE);
                            else if(gameMode == ZEN) {
                                fadeViewForAction(commentImage, 0, 0.8f, 500, START_RETRO_ZEN);
                                shootFireWorks();
                            }
                            else gameUmpire(ROUND_COMPLETED);
                        }
                    });
                }
                if (restarting) {
                    synchronized (restartLock) {
                        restartLock.notifyAll();
                    }
                }
            }
            gameActive = false;
        }
    }

    private DonTapRoundRunnable donTapRoundRunnable;

    private class RetrospectTimerRunnable implements Runnable {
        private void slowDownTimer(){
            // positioning this logic here ensures that the game is slowed down regardless of the point in the algorithm slow down was called.
            double a = timeBar.getProgress(), b = timeBar.getMax(), c = mainGameCoClass.time_allowed;
            Log.println(INFO, "Slowing", "Time Ratio Is = " + (a/b));
            timeAllowed = (int) Math.rint((a/b) * c * 5.0); //scales up the time remaining to effect slowing down
            timeBarProgress = timeAllowed;//////makes the timeBar respond to the slowing down/////////
            timeBar.setMax((mainGameCoClass.time_allowed * 5)); /////////////////////////////////////////
            timeBar.setProgress(timeBarProgress);//initializes the new progress of the time bar
        }
        @Override
        public void run() {
            timerActive = true;
            while (!gameRunning) {
                if(restarting || !timerActive || !timerNeeded){
                    break;
                }
                synchronized (pauseLock) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (zenActiveGameMode == RETROSPECT || gameMode == RETROSPECT) {
                boolean sleep_interrupted;
                Log.println(INFO, "trackTimer", "Game is Running = " + gameRunning);
                sleep_interrupted = false;
                if (slowedDown) {
                    slowDownTimer();
                }
                synchronized (timerLock) {
                    try {
                        if(timerNeeded && timerActive) {
                            timerLock.wait(timeAllowed);// basic time allowed for level
                            while (extraCountdowns > 0 && timerNeeded && timerActive){
                                timerLock.wait(mainGameCoClass.extra_time);// bonus time given for card click
                                extraCountdowns--;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        sleep_interrupted = true; //ensures that gameUmpire("time_up") doesn't get called if sleep is interrupted.
                    }
                }
                Log.println(INFO, "trackTimer", "Time Allowed = " + timeAllowed);
                Log.println(INFO, "trackTimer", "Sleep Interrupted = " + sleep_interrupted);
                if (gameRunning && !sleep_interrupted && extraCountdowns == 0 && timerNeeded  && timerActive &&
                        gameMode == RETROSPECT) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameUmpire(TIME_UP);
                        }
                    });
                    timeBar.setProgress(0);//since timeBar is only a concurrent visual representation,
                    // a time mismatch due to processor speed is hidden by this statement
                }
            }
            if(restarting) {
                synchronized (restartLock) {
                    restartLock.notifyAll();
                }
            }
            timerActive = false;
        }
    }

    private RetrospectTimerRunnable retrospectTimerRunnable;

    private class DonTapTimerRunnable implements Runnable {
        @Override
        public void run() {
            timerActive = true;
            if (zenActiveGameMode == DON_TAP || gameMode == DON_TAP) {
                timerLoop1:
                while ((gameMode == DON_TAP || zenActiveGameMode == DON_TAP) && timerNeeded) {
                    for (int i = 0; i < cardSlots; i++) {
                        //once the current time exceeds the sum of the timeOfSet and the dwell_time, then player's time is up.
                        if (gameRunning && timerNeeded && slotFilled[i] && System.currentTimeMillis() >=
                                (timeOfSet[i] + mainGameCoClass.dwell_time) && activeIndex[i] != badEgg) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameUmpire(TIME_UP);
                                }
                            });
                        }
                        //starts hurry-up pulse once half of allowed time elapses
                        cardIsPulsing[i] = (System.currentTimeMillis() >= (timeOfSet[i] + (mainGameCoClass.dwell_time/2)));

                        while (!gameRunning) {
                            if(restarting || !timerNeeded){
                                break timerLoop1;
                            }
                            synchronized (pauseLock) {
                                try {
                                    pauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        synchronized (timerLock) {
                            try {
                                if(timerNeeded)
                                    timerLock.wait(50); //this gives the UI thread time to refresh before the possibility of calling it again
                                else break timerLoop1;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            timerActive = false;
        }
    }

    private DonTapTimerRunnable donTapTimerRunnable;

    private class TimeBarRunnable implements Runnable {

        @Override
        public void run() {
            timeBarActive = true;
            timeBarProgress = timeAllowed;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeBar.setVisibility(View.VISIBLE);
                }
            });
            boolean chill = false;
            boolean timeLowAlarm = false;
            timeBarLoop:
            while (timeBarActive) {
                while (!gameRunning) {
                    if(restarting || !timeBarNeeded){
                        break timeBarLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (timerLock) {
                    try {
                        timerLock.wait(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!timeBarNeeded) break;
                if(timeBarProgress > 0) {
                    timeBarProgress -= 50;
                }                              // prevents time bar progress from being negative
                else{
                    timeBarProgress = 0;
                    chill = true;
                }
                if(timeBar.getProgress() <= timeBar.getMax()/2){
                    if(!timeLowAlarm){
                        for(int i = 0; i < cardSlots; i++){
                            cardIsPulsing[i] = true;
                        }
                        timeLowSound = playSoundEffect(TIME_LOW);
                        timeLowAlarm = true;
                    }
                }
                else{
                    for(int i = 0; i < cardSlots; i++){
                        cardIsPulsing[i] = false;
                    }
                    gameSounds.pause(timeLowSound);
                    timeLowAlarm = false;
                }
                if(timeBarProgress <= timeBar.getMax()){
                    timeBar.setProgress(timeBarProgress);
                }
                else{
                    timeBar.setProgress(timeBar.getMax());
                }
                Log.println(INFO, "Time Bar", "Time Bar Progress = " + timeBarProgress);
                while (!gameRunning || chill) {
                    chill = false;
                    gameSounds.pause(timeLowSound);
                    timeLowAlarm = false;
                    if(restarting || !timeBarNeeded){
                        break timeBarLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(restarting) {
                synchronized (restartLock) {
                    restartLock.notifyAll();
                }
            }
            gameSounds.pause(timeLowSound);
            timeBarActive = false;
        }
    }//** significant change to pauseLock wait. Negated the timeBarNeeded boolean

    private TimeBarRunnable timeBarRunnable;

    private class ComboBarRunnable implements Runnable {

        @Override
        public void run() {
            timeBarActive = true;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeBar.setVisibility(View.VISIBLE);
                }
            });
            boolean chill = false;
            boolean primaryControl;
            timeBarLoop:
            while (timeBarActive) {
                while (!gameRunning) {
                    if(restarting || !timeBarNeeded){
                        break timeBarLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (timerLock) {
                    try {
                        timerLock.wait(50);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                primaryControl = timeBarProgress <= timeBar.getMax();
                if(timeBarProgress > 0) {
                    int reduction;
                    reduction = primaryControl ? 30 : 10;
                    reduction = slowedDown ? reduction / 5 : reduction;
                    if(clockBroken) reduction = 0;
                    timeBarProgress -= reduction;
                }                              // prevents time bar progress from being negative
                else{
                    chill = true;
                    comboUmpire(COMBO_LOST);
                }
                if(timeBarProgress <= timeBar.getMax()/2){
                    for(int i = 0; i < cardSlots; i++){
                        cardIsPulsing[i] = true;
                    }
                }
                else{
                    for(int i = 0; i < cardSlots; i++){
                        cardIsPulsing[i] = false;
                    }
                }
                if(timeBarProgress >= timeBar.getMax()*2){
                    timeBar.setProgress(timeBar.getMax());
                    timeBar.setSecondaryProgress(timeBar.getMax());
                    comboUmpire(MULTIPLIER_PLUS);
                    break;
                }
                else if (!inBlitzMode){
                    if(primaryControl) {
                        timeBar.setProgress(timeBarProgress);
                        timeBar.setSecondaryProgress(0);
                    }
                    else {
                        timeBar.setProgress(timeBar.getMax());
                        timeBar.setSecondaryProgress(timeBarProgress - timeBar.getMax());
                    }
                }
                else {
                    if (timeBarProgress < timeBar.getMax())
                        timeBarProgress = timeBar.getMax();
                    timeBar.setProgress(timeBar.getMax());
                    timeBar.setSecondaryProgress(timeBarProgress - timeBar.getMax());
                    break;
                }
                if(!timeBarNeeded) break;
                Log.println(INFO, "Time Bar", "Time Bar Progress = " + timeBarProgress);
                while (!gameRunning || chill) {
                    chill = false;
                    if(restarting || !timeBarNeeded){
                        break timeBarLoop;
                    }
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(restarting) {
                synchronized (restartLock) {
                    restartLock.notifyAll();
                }
            }
            timeBarActive = false;
        }
    }

    private ComboBarRunnable comboBarRunnable;

    private class CardPulseControl implements Runnable {
        ValueAnimator cardPulseAnimator;

        private void setCardAlpha(final View v, final float a){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.setAlpha(a);
                }
            });
        }
        private void prepareHurryUpPulse(int duration){
            cardPulseAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 1.0f, 0.6f);
            cardPulseAnimator.setDuration(duration);
            cardPulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator pulseAnimator) {
                    float alpha = (float) pulseAnimator.getAnimatedValue();
                    for (int i = 0; i < cardSlots; i++) {
                        if (slotFilled[i] && cardIsPulsing[i])// ensures the cards don't get animated if the slot is empty
                            setCardAlpha(cardView[i], alpha);
                        else if(cardView[i].getAlpha() != 1.0f && !fadeAnimator.isRunning())
                            setCardAlpha(cardView[i], 1.0f);
                    }
                }
            });
            cardPulseAnimator.addListener(new ValueAnimator.AnimatorListener(){
                public void onAnimationEnd(Animator animator){
                    for (int i = 0; i < cardSlots; i++) {
                        if(gameRunning || clockBroken)
                            setCardAlpha(cardView[i], 1.0f);
                        else if(!gameRunning && !clockBroken)
                            setCardAlpha(cardView[i], 0.3f);
                    }
                }
                public void onAnimationCancel(Animator animator){
                    for (int i = 0; i < cardSlots; i++) {
                        if(gameRunning || clockBroken)
                            setCardAlpha(cardView[i], 1.0f);
                        else if(!gameRunning && !clockBroken)
                            setCardAlpha(cardView[i], 0.3f);
                    }
                }
                public void onAnimationRepeat(Animator animator){}
                public void onAnimationStart(Animator animator){}
            });
            cardPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            cardPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }

        private CardPulseControl() {
            prepareHurryUpPulse(mainGameCoClass.flash_interval);
        }

        @Override
        public void run() {
            if (gameRunning && !cardPulseAnimator.isRunning()) {
                cardPulseAnimator.start();
            }
            if(!gameRunning || levelCompleted) {
                for (int i = 0; i < cardSlots; i++) { // kills all card pulses once game stops running
                    if (cardPulseAnimator != null) {
                        if (cardPulseAnimator.isRunning()) {
                            if(gameSounds != null)
                            cardPulseAnimator.end();
//                            Log.println(INFO, "trackFlashingImage", "Canceling Card Pulse...");
                        }
                    }
                }
            }
            if (cardPulseAnimator != null)
                cardPulseAnimator.setDuration(mainGameCoClass.flash_interval);
        }
    }

    private CardPulseControl cardPulseControlRunnable;

    private class TutorialActionRunnable implements Runnable {
        @Override
        public void run() {
            while(inTutorial[0]  || inTutorial[1]) {
                Log.println(INFO, "trackTutorial", "Loop restarting...");
                tutorialUpdated = false;
                while (!tutorialUpdated) {
                    synchronized (tutorialPauseLock) {
                        try {
                            tutorialPauseLock.wait(120000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                while(!mActivity.hasWindowFocus()){
                    Log.d("Tutorial", "Waiting For Focus...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (dismissTutorialAction) {
                            case SHOW_WELCOME:
                                fadeViewForAction(popLayout, 1f, 1f, 200, HIDE_PLAY_BUTTON_START_ROUND);
                                break;
                            case WRONG_SELECTION:
                                reset_game(RESET_LEVEL);
                                break;
                            case TIME_UP:
                                reset_game(RESET_LEVEL);
                                break;
                            case DON_TAP_HINT:
                                prepareRound();
                                break;
                            case WELL_DONE:
                                if(gameMode == RETROSPECT){
                                    gameMode = DON_TAP;
                                    showTutorialPopUp(DON_TAP_HINT);
                                }
                                else if(gameMode == DON_TAP)
                                    showTutorialPopUp(POWER_UP_HINT);
                                break;
                            case POWER_UP_HINT:
//                                showTutorialPopUp(BREAK_HINT);
                                inTutorial[0] = false;
                                inTutorial[1] = true;
                                gameMode = RETROSPECT;
                                reset_game(RESET_LEVEL);
                                break;
                            case BREAK_HINT:
//                                showTutorialPopUp(SOLVE_HINT);
                                Log.d("Break Hint", "Hint launched");
                                breakClearIcon.performClick();
                                resumeGame(false, JUST_CONTINUE);
                                showTutorialPopUp(SOLVE_HINT);
                                break;
                            case CLEAR_HINT:
//                                showTutorialPopUp(SOLVE_HINT);
                                breakClearIcon.performClick();
                                resumeGame(false, JUST_CONTINUE);
                                showTutorialPopUp(TIME_BAR_HINT);
                                break;
                            case SOLVE_HINT:
//                                showTutorialPopUp(SLOW_HINT);
                                solveIcon.performClick();
                                boolean check = false;
                                for (int i = 0; i < cardSlots; i++) {
                                    check = check || slotFilled[i];
                                }
                                if(check) showTutorialPopUp(SOLVE_HINT);
                                else showTutorialPopUp(CLEAR_HINT);
                                break;
                            case SLOW_HINT:
//                                showTutorialPopUp(TIME_BAR_HINT);
                                slowIcon.performClick();
                                resumeGame(false, JUST_CONTINUE);
                                break;
                            case TIME_BAR_HINT:
                                showTutorialPopUp(ROUNDS_HINT);
                                break;
                            case ROUNDS_HINT:
                                showTutorialPopUp(RETRY_HINT);
                                break;
                            case RETRY_HINT:
                                showTutorialPopUp(TUTORIAL_COMPLETE);
                                break;
                            case TUTORIAL_COMPLETE:
                                disableTutorials();
                                setGameParameters();
                                declareViews();
                                make_view_active(retryIcon);
                                reset_game(RESET_LEVEL);
                                break;
                            default:
                                Log.println(INFO, "trackTutorial", "Game resumed...");
                                resumeGame(false, JUST_CONTINUE);
                                break;
                        }
                    }
                });
            }
        }
    }

    private TutorialActionRunnable tutorialActionsRunnable;

    public static int getGameMode(int level){
        if ((level % 4) + 1 == 4) {
            return DON_TAP;
        } else {
            return RETROSPECT;
        }
    }

    private void getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;
    }

    public void prepareMessageHandlers(){
        uiHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                int action = msg.what;
                switch (action){
                    case SHOW_BAD_EGG:
                    case HIDE_PLAY_BUTTON_START_ROUND:
                        fadeViewForAction(null, 0f, 1f, 500, SHOW_STAGE_START_ROUND);
                        break;
                    case HIDE_PLAY_BUTTON_SHOW_BAD_EGG:
                        fadeViewForAction(badEggInfo, 0f, 1f, 800, SHOW_BAD_EGG);
                        break;
                    case HIDE_PLAY_BUTTON_CONTINUE:
                        fadeViewForAction(null, 0f, 1f, 500, SHOW_STAGE_CONTINUE);
                        break;
                    case START_RETRO_ZEN:
                        fadeViewForAction(null, 0, 1, 200, ROUND_COMPLETED);
                        break;
                    case ROUND_COMPLETED:
                        gameUmpire(ROUND_COMPLETED);
                        break;
                    case SHOW_STAGE_START_ROUND:
                        if(gameMode == ZEN){
                            starts_round(zenActiveGameMode);
                        }else {
                            starts_round(gameMode);
                        }
                        break;
                    case SHOW_STAGE_CONTINUE:
                        resumeGame(false, JUST_CONTINUE);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void setGameParameters() {
        ImageView backgroundImage = findViewById(R.id.game_back);
        mProgress = new Progress(this);
        Intent intentMode;
        Bundle modeBundle;
        intentMode = getIntent();
        modeBundle = intentMode.getExtras();

        inTutorial = new boolean[2];
        inTutorial[0] = mProgress.getTutorialState() == 1;
        inTutorial[1] = false;
        Log.println(INFO, "Tutorial", "inTutorial: " + inTutorial[0]);


        if (modeBundle != null) {
            difficulty = modeBundle.getInt("dif", 1);
            level = modeBundle.getInt("level", 1);

            if(difficulty != -1 && level != -1) {
                gameMode = getGameMode(level);
                zenActiveGameMode = -2;
            }
            else {
                gameMode = ZEN;
                zenActiveGameMode = -2;
                difficulty = 1;
                level = 1;
            }
        }
        else{
            difficulty = 1;
            level = 1;
            gameMode = RETROSPECT;
        }


        if(inTutorial[0]){
            difficulty = 1;
            level = 1;
            gameMode = RETROSPECT;
        }

        if (backgroundImage != null) {
            if(gameMode == ZEN){
                backgroundImage.setImageResource(R.drawable.bg_05);
                cardBackResource = R.drawable.card_back_zen;
                emptySlotResource = R.drawable.empty_slot_zen;
                clockResource = R.drawable.clock_zen;
                themeColor = R.color.zen_theme;
            }
            else {
                switch (difficulty) {
                    case 1:
                        backgroundImage.setImageResource(R.drawable.bg_01);
                        cardBackResource = R.drawable.card_back_beginner;
                        emptySlotResource = R.drawable.empty_slot_beginner;
                        clockResource = R.drawable.clock_beginner;
                        themeColor = R.color.beginner_theme;
                        break;
                    case 2:
                        backgroundImage.setImageResource(R.drawable.bg_02);
                        cardBackResource = R.drawable.card_back_intermediate;
                        emptySlotResource = R.drawable.empty_slot_intermediate;
                        clockResource = R.drawable.clock_intermediate;
                        themeColor = R.color.intermediate_theme;
                        break;
                    case 3:
                        backgroundImage.setImageResource(R.drawable.bg_03);
                        cardBackResource = R.drawable.card_back_expert;
                        emptySlotResource = R.drawable.empty_slot_expert;
                        clockResource = R.drawable.clock_expert;
                        themeColor = R.color.expert_theme;
                        break;
                    case 4:
                        backgroundImage.setImageResource(R.drawable.bg_04);
                        cardBackResource = R.drawable.card_back_eidetic;
                        emptySlotResource = R.drawable.empty_slot_eidetic;
                        clockResource = R.drawable.clock_eidetic;
                        themeColor = R.color.eidetic_theme;
                        break;
                    default:
                        backgroundImage.setImageResource(R.drawable.bg_00);
                }
            }
        }

        mainGameCoClass = new MainGameCoClass(difficulty, level, gameMode, inTutorial[0]);
        gameRunning = false;
        extraCountdowns = 0;
        levelCompleted = false;
        roundsPlayed = 0;
        multiplier = 1;
        bestCombo = 0;
        timeAllowed = mainGameCoClass.time_allowed;
        cardSlots = mainGameCoClass.card_slots;
        cardInitialized = new boolean[9];
        activeIndex = new int[9];
        slotFilled = new boolean[9];
        cardIsPulsing = new boolean[9];
        timeOfSet = new double[9];
        clockBroken = false;
        slowedDown = false;
        restarting = false;
        gameActive = false;
        timeBarActive = false;
        timerActive = false;
        timerNeeded = false;
        timeBarNeeded = false;
        continueCost = BASE_CONTINUE_COST;

        coinWallet = mProgress.getCollectibleAccount(Progress.COINS);
        breakPouch = mProgress.getCollectibleAccount(Progress.BREAK);
        solvePouch = mProgress.getCollectibleAccount(Progress.SOLVE);
        clearPouch = mProgress.getCollectibleAccount(Progress.CLEAR);
        slowPouch = mProgress.getCollectibleAccount(Progress.SLOW);

        mProgress.extractHighScoreData(gameMode, difficulty, level);

        isFreshLevel = Achievements.getStarSummary(gameMode, difficulty, level, mProgress.highScore, Progress.NOT_ZEN)[0] < Progress.ONE_STAR;
    }

    private void declareViews() {
        cardView = new ImageView[9];
        cardView[0] = findViewById(R.id.card_view00);
        cardView[1] = findViewById(R.id.card_view01);
        cardView[2] = findViewById(R.id.card_view02);
        cardView[3] = findViewById(R.id.card_view03);
        cardView[4] = findViewById(R.id.card_view04);
        cardView[5] = findViewById(R.id.card_view05);
        cardView[6] = findViewById(R.id.card_view06);
        cardView[7] = findViewById(R.id.card_view07);
        cardView[8] = findViewById(R.id.card_view08);
        playButton = findViewById(R.id.play_button);
        badEggInfo = findViewById(R.id.don_tap_info_layout);
        badCard = findViewById(R.id.bad_egg_view);
        ImageView donTap = findViewById(R.id.don_tap_text);
        cancelIcon = findViewById(R.id.cancel_ic);
        retryIcon = findViewById(R.id.retry_ic);
        coinsLayout = findViewById(R.id.coins_layout);
        RelativeLayout bonusLayout = findViewById(R.id.bonus_layout);
        honeyJar = findViewById(R.id.honey_jar);
        bonusCoinsText = findViewById(R.id.bonus_coins);
        ImageView coinsSign = findViewById(R.id.coins_sign);
        coinsAvailable = findViewById(R.id.coins_available);
        breakClearUnits = findViewById(R.id.free_break_clear);
        solveUnits = findViewById(R.id.free_solve);
        slowUnits = findViewById(R.id.free_slow);
        solveIcon = findViewById(R.id.solve_ic);
        breakClearIcon = findViewById(R.id.break_clear_ic);
        slowIcon = findViewById(R.id.slow_ic);
        RelativeLayout solveLayout = findViewById(R.id.solve_container);
        RelativeLayout breakClearLayout = findViewById(R.id.break_clear_container);
        RelativeLayout slowLayout = findViewById(R.id.slow_container);
        clockIcon = findViewById(R.id.white_clock);
        clockIcon.setImageResource(clockResource);
        scoreText = findViewById(R.id.score_text);
        roundsText = findViewById(R.id.rounds_text);
        multiplierText = findViewById(R.id.multiplier_text);
        multiplierText.setRotation(7.5f);
        movingCoins = findViewById(R.id.moving_coins);
        commendationsText = findViewById(R.id.commendation_text);
        commentImage = findViewById(R.id.comment);
        countDownText = findViewById(R.id.count_down_text);
        explode1 = findViewById(R.id.explode1);
        explode2 = findViewById(R.id.explode2);
        explode3 = findViewById(R.id.explode3);
        timeBar = findViewById(R.id.determinateBar);
        loadingAdBar = findViewById(R.id.loading_ad_bar);
        loadingAdBar.setVisibility(View.INVISIBLE);
        if (timeBar != null) {
            Drawable progressDrawable = getResources().getDrawable(R.drawable.time_bar_default);

            switch (difficulty){
                case ActivityDifficultyMenu.BEGINNER:
                    progressDrawable = getResources().getDrawable(R.drawable.time_bar_beginner);
                    break;
                case ActivityDifficultyMenu.INTERMEDIATE:
                    progressDrawable = getResources().getDrawable(R.drawable.time_bar_intermediate);
                    break;
                case ActivityDifficultyMenu.EXPERT:
                    progressDrawable = getResources().getDrawable(R.drawable.time_bar_expert);
                    break;
                case ActivityDifficultyMenu.EIDETIC:
                    progressDrawable = getResources().getDrawable(R.drawable.time_bar_eidetic);
                    break;

            }

            switch (gameMode){
                case RETROSPECT:
                    timeBar.setMax(mainGameCoClass.time_allowed);
                    timeBar.setProgress(timeBar.getMax());
                    break;
                case DON_TAP:
                    timeBar.setVisibility(View.INVISIBLE);
                    break;
                case ZEN:
                    timeBar.setMax(COMBO_BAR_MAX);
                    timeBar.setProgress(0);
                    progressDrawable = getResources().getDrawable(R.drawable.combo_bar_zen);
                    clockIcon.setVisibility(View.INVISIBLE);
                    break;
            }
            timeBar.setProgressDrawable(progressDrawable);
        }
        blitzContainer = findViewById(R.id.blitz_container);
        blitzBar1 = findViewById(R.id.blitz_bar_1);
        blitzBar1.setMax(100);
        blitzBar2 = findViewById(R.id.blitz_bar_2);
        blitzBar2.setMax(100);
        blitzBar3 = findViewById(R.id.blitz_bar_3);
        blitzBar3.setMax(100);
        tutorialGif = findViewById(R.id.tutorial_gif);

        if(SDK_VERSION >= 26) {
            Drawable blitzBar1Drawable = getDrawable(R.drawable.blitz_bar_1);
            Drawable blitzBar2Drawable = getDrawable(R.drawable.blitz_bar_2);
            Drawable blitzBar3Drawable = getDrawable(R.drawable.blitz_bar_3);

            blitzBar1.setProgressDrawable(blitzBar1Drawable);
            blitzBar2.setProgressDrawable(blitzBar2Drawable);
            blitzBar3.setProgressDrawable(blitzBar3Drawable);
        }
        else {
            blitzBar1.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            blitzBar2.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            blitzBar3.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
        updateBlitz(RESET_BLITZ);

        mainGameLayout = findViewById(R.id.main_game);
        timeBarContainer = findViewById(R.id.time_bar_container);
        backDrop = findViewById(R.id.backdrop);
        RelativeLayout cardsContainer = findViewById(R.id.cards_container);
        powerUpsContainer = findViewById(R.id.power_up_container);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-1923630121694917/8159996515");


        int backdrop_side = (int) Math.rint(screenHeight * 0.4898);
        int backdrop_margin_top = (int) Math.rint(screenHeight * 0.03);
        int cards_container_side = (int) Math.rint(backdrop_side * 0.7831);
        int card_total_margin = (int) Math.rint(screenHeight * 0.02);
        int card_diameter = (int) Math.rint((cards_container_side - card_total_margin) / 3.0);
        int score_height = (int) Math.rint(screenHeight * 0.1125);
        int score_margin_top = (int) Math.rint(screenHeight * 0.025);
        int button_diameter = (int) Math.rint(screenHeight * 0.06094);
        int button_margin = (int) Math.rint(screenHeight * 0.015);
        int coins_available_width = (int) Math.rint(screenHeight*0.06094);
        int coins_available_height = (int) Math.rint(coins_available_width*0.4426);
        int bounus_coins_height = (int) Math.rint(coins_available_width*0.5);
        int power_ups_container_height = (int) Math.rint(screenHeight * 0.06094);
        int power_ups_container_width = (int) Math.rint(power_ups_container_height * 7.846);
        int power_ups_container_margin_top = (int) Math.rint(screenHeight * 0.05);
        int power_up_icon_height = (int) Math.rint(power_ups_container_height);
        int solve_icon_width = (int) Math.rint(power_up_icon_height*1.4265);
        int time_bar_container_height = (int) Math.rint(screenHeight * 0.05);
        int time_bar_container_width = (int) Math.rint(time_bar_container_height * 7.2);
        int time_bar_container_margin_top = (int) Math.rint(time_bar_container_height*1.2);
        int time_bar_height = (int) Math.rint(time_bar_container_height/4.0);
        int time_bar_width = gameMode != ZEN ? (int) Math.rint(time_bar_container_width * 0.4)
                : (int) Math.rint(time_bar_container_width * 0.6);
        int time_bar_margin_right = (int) Math.rint(time_bar_container_width*0.05);
        int blitz_bar_height = (int) Math.rint(screenHeight * 0.008);
        int blitz_bar_width = (int) Math.rint(blitz_bar_height * 4.364);
        int blitz_bar_margin_top = (int) Math.rint(screenHeight * 0.05);
        int blitz_bar_margin_side = (int) Math.rint(screenHeight*0.005);
        int rounds_text_width = (int) Math.rint(time_bar_container_height);
        int white_clock_diameter = (int) Math.rint(time_bar_container_height * 0.5);
        int white_clock_margin_right = (int) Math.rint(white_clock_diameter * 0.25);
        int moving_coins_height = (int) Math.rint(screenHeight * 0.03);
        int moving_coins_width = (int) Math.rint(moving_coins_height * 1.1622);
        int explode_side = (int) Math.rint(screenHeight * 0.25);
        int explode_margin_side = (int) Math.rint(screenHeight * 0.05625);
        int explode_margin_top = (int) Math.rint(screenHeight * 0.15);
        int tutorial_gif_height = (int) Math.rint(screenHeight * 0.25);
        int tutorial_gif_width = (int) Math.rint(tutorial_gif_height * 1.2308);


        RelativeLayout.LayoutParams backdropParameters = new RelativeLayout.LayoutParams(backdrop_side, backdrop_side);
        RelativeLayout.LayoutParams cardsContainerParameters = new RelativeLayout.LayoutParams(cards_container_side, cards_container_side);
        RelativeLayout.LayoutParams scoreTextParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams roundsTextParameters = new RelativeLayout.LayoutParams(rounds_text_width, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams multiplierParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams timeBarContainerParameters = new RelativeLayout.LayoutParams(time_bar_container_width, time_bar_container_height);
        RelativeLayout.LayoutParams cancelParameters = new RelativeLayout.LayoutParams(button_diameter, button_diameter);
        RelativeLayout.LayoutParams retryParameters = new RelativeLayout.LayoutParams(button_diameter, button_diameter);
        RelativeLayout.LayoutParams coinsLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, button_diameter+button_margin);
        RelativeLayout.LayoutParams coinsSignParams = new RelativeLayout.LayoutParams(coins_available_height, coins_available_height);
        RelativeLayout.LayoutParams coinsAvailableParams= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams bonusLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams honeyJarParams = new RelativeLayout.LayoutParams(button_diameter, button_diameter);
        RelativeLayout.LayoutParams bonusCoinsParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, bounus_coins_height);
        RelativeLayout.LayoutParams powerUpsContainerParameters = new RelativeLayout.LayoutParams(power_ups_container_width, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams breakParams = new RelativeLayout.LayoutParams(power_ups_container_height, power_ups_container_height);
        RelativeLayout.LayoutParams solveParams = new RelativeLayout.LayoutParams(solve_icon_width, power_up_icon_height);
        RelativeLayout.LayoutParams slowParams = new RelativeLayout.LayoutParams(power_ups_container_height, power_ups_container_height);
        RelativeLayout.LayoutParams breakLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams solveLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams slowLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams freeBreakParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams freeSolveParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams freeSlowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams whiteClockParameters = new RelativeLayout.LayoutParams(white_clock_diameter, white_clock_diameter);
        RelativeLayout.LayoutParams timeBarParameters = new RelativeLayout.LayoutParams(time_bar_width, time_bar_height);
        RelativeLayout.LayoutParams blitzContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, blitz_bar_height);
        RelativeLayout.LayoutParams blitzBar1Params = new RelativeLayout.LayoutParams(blitz_bar_width, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams blitzBar2Params = new RelativeLayout.LayoutParams(blitz_bar_width, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams blitzBar3Params = new RelativeLayout.LayoutParams(blitz_bar_width, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams movingCoinsParams = new RelativeLayout.LayoutParams(moving_coins_width, moving_coins_height);
        RelativeLayout.LayoutParams commendationsTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams commentParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams explode1Params = new RelativeLayout.LayoutParams(explode_side, explode_side);
        RelativeLayout.LayoutParams explode2Params = new RelativeLayout.LayoutParams(explode_side, explode_side);
        RelativeLayout.LayoutParams explode3Params = new RelativeLayout.LayoutParams(explode_side, explode_side);
        RelativeLayout.LayoutParams cardView0Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView1Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView2Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView3Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView4Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView5Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView6Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView7Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams cardView8Parameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams badCardParameters = new RelativeLayout.LayoutParams(card_diameter, card_diameter);
        RelativeLayout.LayoutParams tutorialGifParams = new RelativeLayout.LayoutParams(tutorial_gif_width, tutorial_gif_height);


        backdropParameters.setMargins(0, backdrop_margin_top, 0, 0);
        scoreTextParameters.setMargins(0, score_margin_top, 0, 0);
        timeBarContainerParameters.setMargins(0, time_bar_container_margin_top, 0, 0);
        cancelParameters.setMargins(0, button_margin, button_margin, 0);
        retryParameters.setMargins(button_margin, button_margin, 0, 0);
        coinsSignParams.setMargins(button_margin/2, 0, button_margin/2, 0);
        coinsAvailableParams.setMargins(0, 0, button_margin/2, 0);
        bonusLayoutParams.setMargins(0, button_margin*3, button_margin, 0);
        bonusCoinsParams.setMargins(0, button_margin/2, 0, 0);
        powerUpsContainerParameters.setMargins(0, power_ups_container_margin_top, 0, 0);
        breakLayoutParams.setMargins(button_margin*2, 0, 0, 0);
        slowLayoutParams.setMargins(0, 0, button_margin*2, 0);
        freeBreakParams.setMargins(0, (int) Math.rint(screenHeight * 0.01), 0, 0);
        freeSolveParams.setMargins(0, (int) Math.rint(screenHeight * 0.01), 0, 0);
        freeSlowParams.setMargins(0, (int) Math.rint(screenHeight * 0.01), 0, 0);
        whiteClockParameters.setMargins(0, 0, white_clock_margin_right, 0);
        timeBarParameters.setMargins(0, 0, time_bar_margin_right, 0);
        movingCoinsParams.setMargins(0, (int) Math.rint(screenHeight * 0.18), 0, 0);
        multiplierParams.setMargins((int) Math.rint(screenHeight * 0.01),
                (int) Math.rint(time_bar_container_margin_top*0.95), 0, 0);
        blitzContainerParams.setMargins(0, blitz_bar_margin_top, blitz_bar_margin_side*3, 0);
        blitzBar1Params.setMargins(0, 0, blitz_bar_margin_side, 0);
        blitzBar2Params.setMargins(0, 0, blitz_bar_margin_side, 0);
        explode1Params.setMargins(explode_margin_side, explode_margin_top, 0, 0);
        explode2Params.setMargins(0, explode_margin_top, explode_margin_side, 0);
        explode3Params.setMargins(0, explode_margin_top, 0, 0);


        cancelParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        coinsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        coinsSignParams.addRule(RelativeLayout.CENTER_VERTICAL);
        coinsAvailableParams.addRule(RelativeLayout.CENTER_VERTICAL);
        coinsAvailableParams.addRule(RelativeLayout.RIGHT_OF, R.id.coins_sign);
        bonusLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        bonusLayoutParams.addRule(RelativeLayout.BELOW, R.id.cancel_ic);
        honeyJarParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bonusCoinsParams.addRule(RelativeLayout.BELOW, R.id.honey_jar);
        bonusCoinsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scoreTextParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scoreTextParameters.addRule(RelativeLayout.BELOW, R.id.retry_ic);
        roundsTextParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        multiplierParams.addRule(RelativeLayout.BELOW, R.id.score_text);
        multiplierParams.addRule(RelativeLayout.RIGHT_OF, R.id.time_bar_container);
        timeBarParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        timeBarParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        blitzContainerParams.addRule(RelativeLayout.BELOW, R.id.score_text);
        blitzContainerParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.time_bar_container);
        blitzBar3Params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        blitzBar2Params.addRule(RelativeLayout.LEFT_OF, R.id.blitz_bar_3);
        blitzBar1Params.addRule(RelativeLayout.LEFT_OF, R.id.blitz_bar_2);
        if(gameMode == RETROSPECT)
            whiteClockParameters.addRule(RelativeLayout.LEFT_OF, R.id.determinateBar);
        else if(gameMode == DON_TAP)
            whiteClockParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        whiteClockParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        timeBarContainerParameters.addRule(RelativeLayout.BELOW, R.id.score_text);
        timeBarContainerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        backdropParameters.addRule(RelativeLayout.BELOW, R.id.time_bar_container);
        backdropParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cardsContainerParameters.addRule(RelativeLayout.CENTER_IN_PARENT);
        movingCoinsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        commendationsTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        commentParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        explode1Params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        explode2Params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        explode3Params.addRule(RelativeLayout.BELOW, R.id.explode1);
        explode3Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cardView0Parameters.addRule(RelativeLayout.CENTER_IN_PARENT);
        cardView1Parameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cardView1Parameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cardView2Parameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cardView2Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cardView3Parameters.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        cardView3Parameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cardView4Parameters.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        cardView4Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cardView5Parameters.addRule(RelativeLayout.CENTER_VERTICAL);
        cardView5Parameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cardView6Parameters.addRule(RelativeLayout.CENTER_VERTICAL);
        cardView6Parameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cardView7Parameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cardView7Parameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        cardView8Parameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        cardView8Parameters.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        badCardParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        badCardParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        powerUpsContainerParameters.addRule(RelativeLayout.BELOW, R.id.backdrop);
        powerUpsContainerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        solveLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        slowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        freeBreakParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        freeBreakParams.addRule(RelativeLayout.BELOW, R.id.break_clear_ic);
        freeSolveParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        freeSolveParams.addRule(RelativeLayout.BELOW, R.id.solve_ic);
        freeSlowParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        freeSlowParams.addRule(RelativeLayout.BELOW, R.id.slow_ic);
        tutorialGifParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        if (backDrop != null)
            backDrop.setLayoutParams(backdropParameters);
        if (cardsContainer != null)
            cardsContainer.setLayoutParams(cardsContainerParameters);
        if (timeBarContainer != null)
            timeBarContainer.setLayoutParams(timeBarContainerParameters);
        if (powerUpsContainer != null)
            powerUpsContainer.setLayoutParams(powerUpsContainerParameters);
        clockIcon.setLayoutParams(whiteClockParameters);
        scoreText.setLayoutParams(scoreTextParameters);
        roundsText.setLayoutParams(roundsTextParameters);
        multiplierText.setLayoutParams(multiplierParams);
        movingCoins.setLayoutParams(movingCoinsParams);
        commendationsText.setLayoutParams(commendationsTextParams);
        commentImage.setLayoutParams(commentParams);
        cancelIcon.setLayoutParams(cancelParameters);
        retryIcon.setLayoutParams(retryParameters);
        coinsLayout.setLayoutParams(coinsLayoutParams);
        coinsSign.setLayoutParams(coinsSignParams);
        coinsAvailable.setLayoutParams(coinsAvailableParams);
        bonusLayout.setLayoutParams(bonusLayoutParams);
        honeyJar.setLayoutParams(honeyJarParams);
        bonusCoinsText.setLayoutParams(bonusCoinsParams);
        breakClearIcon.setLayoutParams(breakParams);
        solveIcon.setLayoutParams(solveParams);
        slowIcon.setLayoutParams(slowParams);
        breakClearLayout.setLayoutParams(breakLayoutParams);
        solveLayout.setLayoutParams(solveLayoutParams);
        slowLayout.setLayoutParams(slowLayoutParams);
        breakClearUnits.setLayoutParams(freeBreakParams);
        solveUnits.setLayoutParams(freeSolveParams);
        slowUnits.setLayoutParams(freeSlowParams);
        timeBar.setLayoutParams(timeBarParameters);
        blitzContainer.setLayoutParams(blitzContainerParams);
        blitzBar1.setLayoutParams(blitzBar1Params);
        blitzBar2.setLayoutParams(blitzBar2Params);
        blitzBar3.setLayoutParams(blitzBar3Params);
        cardView[0].setLayoutParams(cardView0Parameters);
        cardView[1].setLayoutParams(cardView1Parameters);
        cardView[2].setLayoutParams(cardView2Parameters);
        cardView[3].setLayoutParams(cardView3Parameters);
        cardView[4].setLayoutParams(cardView4Parameters);
        cardView[5].setLayoutParams(cardView5Parameters);
        cardView[6].setLayoutParams(cardView6Parameters);
        cardView[7].setLayoutParams(cardView7Parameters);
        cardView[8].setLayoutParams(cardView8Parameters);
        badCard.setLayoutParams(badCardParameters);
        tutorialGif.setLayoutParams(tutorialGifParams);

        coinsAvailable.setTextSize(TypedValue.COMPLEX_UNIT_PX, coins_available_height*0.8f);
        bonusCoinsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, coins_available_height*0.8f);
        scoreText.setTextSize(TypedValue.COMPLEX_UNIT_PX, score_height*0.6f);
        roundsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, time_bar_container_height*0.6f);
        roundsText.setTextColor(getResources().getColor(themeColor));
        multiplierText.setTextSize(TypedValue.COMPLEX_UNIT_PX, score_height*0.4f);
        commendationsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenHeight*0.032f);
        bonusCoinsText.setGravity(CENTER);
        scoreText.setGravity(CENTER);
        roundsText.setGravity(CENTER);
        commendationsText.setGravity(CENTER);
        multiplierText.setGravity(CENTER);
        breakClearUnits.setTextSize(TypedValue.COMPLEX_UNIT_PX, power_up_icon_height*0.4f);
        solveUnits.setTextSize(TypedValue.COMPLEX_UNIT_PX, power_up_icon_height*0.4f);
        slowUnits.setTextSize(TypedValue.COMPLEX_UNIT_PX, power_up_icon_height*0.4f);

        if(inTutorial[0]) roundsText.setText(String.format(Locale.ENGLISH, "%d", 1));
        bonusCoinsText.setText(String.format(Locale.ENGLISH, "%d", 0));


        float[] corner_array = new float[8];
        corner_array[0] = 0;
        corner_array[1] = 0;
        corner_array[2] = 0;
        corner_array[3] = 0;
        corner_array[4] = coinsLayoutParams.height/5.0f;
        corner_array[5] = coinsLayoutParams.height/5.0f;
        corner_array[6] = coinsLayoutParams.height/5.0f;
        corner_array[7] = coinsLayoutParams.height/5.0f;
        GradientDrawable coin_layout_back = new GradientDrawable();
        coin_layout_back.setColor(getResources().getColor(R.color.green));
        coin_layout_back.setShape(GradientDrawable.RECTANGLE);
        coin_layout_back.setCornerRadii(corner_array);
        coin_layout_back.setSize(coinsLayoutParams.width, coinsLayoutParams.height);


        float[] corner_array1 = new float[8];
        corner_array1[0] = bonusCoinsParams.height/2.0f;
        corner_array1[1] = bonusCoinsParams.height/2.0f;
        corner_array1[2] = bonusCoinsParams.height/2.0f;
        corner_array1[3] = bonusCoinsParams.height/2.0f;
        corner_array1[4] = bonusCoinsParams.height/2.0f;
        corner_array1[5] = bonusCoinsParams.height/2.0f;
        corner_array1[6] = bonusCoinsParams.height/2.0f;
        corner_array1[7] = bonusCoinsParams.height/2.0f;

        GradientDrawable bonusBack = new GradientDrawable();
        bonusBack.setShape(GradientDrawable.RECTANGLE);
        bonusBack.setColor(getResources().getColor(R.color.green));
        bonusBack.setCornerRadii(corner_array1);
        bonusBack.setSize(bonusCoinsParams.width, bonusCoinsParams.height);

        coinsLayout.setBackground(coin_layout_back);
        bonusCoinsText.setBackground(bonusBack);

        breakClearUnits.setBackground(getBackDrawable());
        solveUnits.setBackground(getBackDrawable());
        slowUnits.setBackground(getBackDrawable());

        int padding = (int) Math.rint(power_up_icon_height*0.1333f);

        breakClearUnits.setPadding(padding, 0, padding, padding/3);
        solveUnits.setPadding(padding, 0, padding, padding/3);
        slowUnits.setPadding(padding, 0, padding, padding/3);
        bonusCoinsText.setPadding(padding, 0, padding, padding/3);

        int bottom_balance = (int) Math.rint(screenHeight*0.002);
        roundsText.setPadding(0, 0, 0, bottom_balance);

        if(inTutorial[0])
            prepare_tutorial_pop_up();
        else prepare_fail_pop_up();

        if (donTap != null) {
            donTap.setMaxWidth(cards_container_side);
            donTap.setMaxHeight(cards_container_side / 2);
        }
        playButton.setMaxWidth(cards_container_side/3);
        playButton.setMaxHeight(cards_container_side/3);


        if(!gameCreated) {
            retryIcon.setVisibility(View.INVISIBLE);
            cancelIcon.setVisibility(View.INVISIBLE);
            coinsLayout.setVisibility(View.INVISIBLE);
            scoreText.setVisibility(View.INVISIBLE);
            timeBarContainer.setVisibility(View.INVISIBLE);
            backDrop.setVisibility(View.INVISIBLE);
            powerUpsContainer.setVisibility(View.INVISIBLE);
            multiplierText.setVisibility(View.INVISIBLE);
            blitzBar1.setVisibility(View.INVISIBLE);
            blitzBar2.setVisibility(View.INVISIBLE);
            blitzBar3.setVisibility(View.INVISIBLE);
            explode1.setVisibility(View.INVISIBLE);
            explode2.setVisibility(View.INVISIBLE);
            explode3.setVisibility(View.INVISIBLE);
            tutorialGif.setVisibility(View.INVISIBLE);
            bonusCoinsText.setVisibility(View.INVISIBLE);
            honeyJar.setVisibility(View.INVISIBLE);
        }


        prepareGameInflation();
        prepareWrongSelectionAction();
        prepareCardFlip(mainGameCoClass.flip_duration);
        prepareCommendationAnimation();
        prepareCardShrink();
        prepareFadeView();
        prepareScoreAnimator();
        prepareMovingCoinsAnim();
        prepareBlitzMadness();


        explode1.setLayoutParams(explode1Params);
        explodeAnim1 = AnimatedVectorDrawableCompat.create(this, R.drawable.animated_explode_vector);
        explode1.setImageDrawable(explodeAnim1);

        explode2.setLayoutParams(explode2Params);
        explodeAnim2 = AnimatedVectorDrawableCompat.create(this, R.drawable.animated_explode_vector);
        explode2.setImageDrawable(explodeAnim2);

        explode3.setLayoutParams(explode3Params);
        explodeAnim3 = AnimatedVectorDrawableCompat.create(this, R.drawable.animated_explode_vector);
        explode3.setImageDrawable(explodeAnim3);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("D25A5BA8C95EC0184784738ACBD8AC2F")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        interstitialAd.loadAd(adRequest);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                int timeUsed = (int) Math.rint(levelStopTime - levelStartTime);
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice("D25A5BA8C95EC0184784738ACBD8AC2F")
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();
                interstitialAd.loadAd(adRequest);
                showFinalScore(timeUsed);
            }
            @Override
            public void onAdOpened() {
                release_heavy_processes();
            }
        });
    }

    private GradientDrawable getBackDrawable(){
        int power_ups_container_height = (int) Math.rint(screenHeight * 0.06094);
        int power_up_icon_height = (int) Math.rint(power_ups_container_height);

        float[] corner_array = new float[8];
        corner_array[0] = power_up_icon_height*0.2f;
        corner_array[1] = power_up_icon_height*0.2f;
        corner_array[2] = power_up_icon_height*0.2f;
        corner_array[3] = power_up_icon_height*0.2f;
        corner_array[4] = power_up_icon_height*0.2f;
        corner_array[5] = power_up_icon_height*0.2f;
        corner_array[6] = power_up_icon_height*0.2f;
        corner_array[7] = power_up_icon_height*0.2f;

        GradientDrawable gDrawable = new GradientDrawable();
        gDrawable.setShape(GradientDrawable.RECTANGLE);
        gDrawable.setCornerRadii(corner_array);

        return gDrawable;
    }

    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        float soundtrack_volume = mProgress.getSoundTrackVolume();
        tactile_on = mProgress.getTactileState() == 1;
        if(gameSounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                gameSounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else gameSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            soundEffect = new int[16];
            soundEffect[0] = gameSounds.load(this, R.raw.menu_click, 1);
            soundEffect[1] = gameSounds.load(this, R.raw.game_click, 1);
            soundEffect[2] = gameSounds.load(this, R.raw.clock_break, 1);
            soundEffect[3] = gameSounds.load(this, R.raw.time_low, 1);
            soundEffect[4] = gameSounds.load(this, R.raw.card_flipped, 1);
            soundEffect[5] = gameSounds.load(this, R.raw.slow_down, 1);
            soundEffect[6] = gameSounds.load(this, R.raw.round_completed, 1);
            soundEffect[7] = gameSounds.load(this, R.raw.time_reset, 1);
            soundEffect[8] = gameSounds.load(this, R.raw.solve, 1);
            soundEffect[9] = gameSounds.load(this, R.raw.wrong_selection, 1);
            soundEffect[10] = gameSounds.load(this, R.raw.firework_sparks, 1);
            soundEffect[11] = gameSounds.load(this, R.raw.pop_stage, 1);
            soundEffect[12] = gameSounds.load(this, R.raw.get_coin, 1);
            soundEffect[13] = gameSounds.load(this, R.raw.in_blitz_pound, 1);
        }
        if(backgroundMusic == null){
            backgroundMusic = new MediaPlayer();
            backgroundMusic = MediaPlayer.create(this, R.raw.game_music);
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
        long[] timings = new long[4];
        timings[0] = 0;
        timings[2] = 50;
        int sound = 0;
        int repeat = -1;
        float rate = slowedDown ? 0.5f : 1.0f;
        if(gameSounds != null) {
            switch (playSound) {
                case LEVEL_STARTED:
                    timings[1] = 150;
                    timings[3] = 0;
                    sound = gameSounds.play(soundEffect[11], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case CARD_CLICKED:
                    Log.println(INFO, "SoundEffects", "Played Click Effect");
                    sound = gameSounds.play(soundEffect[1], sfx_volume, sfx_volume, 1, 0, rate);
                    break;
                case ROUND_COMPLETED:
                    timings[1] = 50;
                    timings[3] = 30;
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gameSounds.play(soundEffect[6], sfx_volume, sfx_volume, 1, 0, 1.0f);
                        }
                    }, 200);
                    break;
                case WRONG_SELECTION:
                    timings[1] = 50;
                    timings[3] = 0;
                    sound = gameSounds.play(soundEffect[9], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case BUTTON_CLICKED:
                    sound = gameSounds.play(soundEffect[0], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case CLOCK_BREAK:
                    timings[1] = 30;
                    timings[3] = 0;
                    sound = gameSounds.play(soundEffect[2], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case AUTO_SOLVE:
                    timings[1] = 10;
                    timings[3] = 0;
                    sound = gameSounds.play(soundEffect[8], sfx_volume, sfx_volume, 1, 0, rate);
                    break;
                case SLOWED_DOWN:
                    timings[1] = 100;
                    timings[3] = 0;
                    sound = gameSounds.play(soundEffect[5], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case CARD_FLIPPED:
                    sound = gameSounds.play(soundEffect[4], sfx_volume, sfx_volume, 1, 0, rate);
                    break;
                case TIME_LOW:
                    sound = gameSounds.play(soundEffect[3], sfx_volume, sfx_volume, 1, 0, rate);
                    break;
                case LEVEL_COMPLETED:
                    timings[1] = 150;
                    timings[3] = 200;
//                    sound = gameSounds.play(soundEffect[6], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case IN_BLITZ:
                    timings[1] = 999;
                    timings[2] = 0;
                    timings[3] = 999;
                    repeat = 1;
                    sound = gameSounds.play(soundEffect[13], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case FIREWORKS:
                    timings[1] = 200;
                    timings[3] = 100;
                    repeat = 1;
                    sound = gameSounds.play(soundEffect[10], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case RESET_TIME:
                    sound = gameSounds.play(soundEffect[7], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
                case GET_COIN:
                    sound = gameSounds.play(soundEffect[12], sfx_volume, sfx_volume, 1, 0, 1.0f);
                    break;
            }
            if(SDK_VERSION >= 26 && tactileFeedback != null && timings[1] != 0
                    && tactile_on && (!inBlitzMode || playSound == IN_BLITZ)){
                VibrationEffect feedBackEffect = VibrationEffect.createWaveform(timings, repeat);
                tactileFeedback.vibrate(feedBackEffect);
            }
            else if (tactileFeedback != null && timings[1] != 0 && tactile_on
                    && (!inBlitzMode || playSound == IN_BLITZ)){
                tactileFeedback.vibrate(timings, repeat);
            }
        }
        return sound;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setGameUiClickables() {
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(INFO, "trackGameClickables", "Cancel Clicked!");
                killGame();
                ActivityMainGame.this.finish();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });

        retryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setClickable(false);
                playSoundEffect(BUTTON_CLICKED);
                playButton.setClickable(false);// to avoid bug when player restarts and then attempts to play before restart is executed
                reset_game(RESET_LEVEL);
                view.setClickable(true);
            }
        });

        breakClearIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(INFO, "trackPowerUpUsage", "Game is Breaking...");
                if (gameMode == DON_TAP || zenActiveGameMode == DON_TAP) {
                    if(useCollectible(Progress.CLEAR, 1)) {
                        for (int i = 0; i < cardSlots; i++) {
                            if(slotFilled[i] || inTutorial[1]) {
                                cardView[i].setClickable(false);
                                slotFilled[i] = false;
                                updateScore(CARD_CLICKED, i);
                                cardView[i].setImageResource(emptySlotResource);
                                timeOfSet[i] = Math.pow(99, 99); // ensures that the timeOfSet is nullified for empty slots
                            }
                        }
                    }
                    else {
                        pause_game(WAITING_FOR_RESULT);
                        boolean result = purchaseCoins(CLEAR_COST);
                        Log.println(INFO, "trackPowerUp", "consumeCollectible result: " + result);
                        if(result){
                            view.callOnClick();
                        }
                        resumeGame(false, JUST_CONTINUE);
                    }
                } else if(gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT){
                    if ((gameRunning || inTutorial[1]) && !clockBroken) {
                        if(useCollectible(Progress.BREAK, 1)) {
                            clockBroken = true;
                            playSoundEffect(CLOCK_BREAK);
                            timeOfBreak = System.currentTimeMillis();
                            view.setScaleX(0.8f);//to show that icon is pressed
                            view.setScaleY(0.8f);
                            breakClearIcon.setAlpha(0.7f);
                            make_view_dormant(slowIcon);
                            pause_game(CLOCK_BREAK);//completes the clock break
                        }
                        else {
                            pause_game(WAITING_FOR_RESULT);
                            boolean result = purchaseCoins(BREAK_COST);
                            Log.println(INFO, "trackPowerUp", "consumeCollectible result: " + result);
                            if(result) {
                                view.callOnClick();
                            }
                            resumeGame(false, JUST_CONTINUE);
                        }
                    }
//                    else if (clockBroken){
//                        clockBroken = false;
//                        view.setScaleX(1.0f);//to show that icon is released
//                        view.setScaleY(1.0f);
//                        reset_game(true, RESET_ROUND);
//                    }
                }
            }
        });
        slowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!slowedDown) {
                    if (useCollectible(Progress.SLOW, 1)) {
                        slowedDown = true; // boolean to make sure other threads know that the game is being slowed down
                        playSoundEffect(SLOWED_DOWN);
                        Log.println(INFO, "trackPowerUpUsage", "Game is Slowing Down...");
                        view.setScaleX(0.8f);//to show that icon is pressed
                        view.setScaleY(0.8f);
                        view.setAlpha(0.7f);
                        mainGameCoClass.slowDown(); // increases the card scrolling time to make game feel slower
                        if (taskThreadHandler != null && taskThread != null) {
                            if (taskThread.isAlive())
                                taskThreadHandler.post(cardPulseControlRunnable);
                        }
                        if (timerActive && timerNeeded) {
                            Log.println(INFO, "trackSlowAction", "Restarting Timer..");
                            restartTimer();
                        }
                    }
                    else {
                        pause_game(WAITING_FOR_RESULT);
                        boolean result = purchaseCoins(SLOW_COST);
                        Log.println(INFO, "trackPowerUp", "consumeCollectible result: " + result);
                        if(result) {
                            view.callOnClick();
                        }
                        resumeGame(false, JUST_CONTINUE);
                    }
                }
//                else if (gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT) {
//                    slowedDown = false;
//                    Log.println(INFO, "trackPowerUpUsage", "Game Speeding Up...");
//                    view.setScaleX(1.0f);//to show that icon is released
//                    view.setScaleY(1.0f);
//                    view.setAlpha(1.0f);
//                    reset_game(true, RESET_ROUND);
//                }
            }
        });
        solveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(useCollectible(Progress.SOLVE, 1)) {
                    Log.println(INFO, "trackPowerUp", "Solved!");
                    playSoundEffect(AUTO_SOLVE);
                    if (zenActiveGameMode == RETROSPECT || gameMode == RETROSPECT) {
                        for (int i = 0; i < cardSlots; i++) {
                            if (i < cardSlots && clkPos < cardSlots) {//extra check to avoid out of bounds exception due to main ui loop
                                if (cardResourceId[mainGameCoClass.ran_index[i]].equals(cardResourceId[mainGameCoClass.ord_index[clkPos]])) {
                                    cardView[mainGameCoClass.shuffled_slots[i]].setClickable(false);
                                    slotFilled[mainGameCoClass.shuffled_slots[i]] = false;
                                    shrinkCard(mainGameCoClass.shuffled_slots[i]);
                                    give_extra_time();
                                    updateScore(CARD_CLICKED, i);
                                }
                            }
                        }
                        clkPos++;
                        if ((clkPos == cardSlots) && !inTutorial[1]) {
                            gameUmpire(ROUND_COMPLETED);
                        }
                    }
                    else if (zenActiveGameMode == DON_TAP || gameMode == DON_TAP) {
                        int oldest_card_slot = 0;
                        for (int i = 0; i < cardSlots; i++) {
                            if (i < cardSlots) {//extra check to avoid out of bounds exception due to main ui loop
                                Log.d("slot Filled", i + ": " + slotFilled[i]);
                                Log.d("is New Oldest", i + ": " + (timeOfSet[i] < timeOfSet[oldest_card_slot]));
                                Log.d("is Valid Slot", i + ": " + (timeOfSet[i] != Math.pow(99, 99)));
                                Log.d("is Good Slot", i + ": " + (activeIndex[i] != badEgg));
                                Log.d("time Of Set Is", i + ": " + timeOfSet[oldest_card_slot]);
                                if (slotFilled[i] && timeOfSet[i] < timeOfSet[oldest_card_slot] && timeOfSet[i] != Math.pow(99, 99) && activeIndex[i] != badEgg) {
                                    oldest_card_slot = i;
                                }
                            }
                        }
                        boolean check = false; //true if at least one slot is not empty
                        for (int i = 0; i < cardSlots; i++) {
                            if (i < cardSlots) {//extra check to avoid out of bounds exception due to main ui loop
                                check = check || (slotFilled[i] && activeIndex[i] != badEgg);
                            }
                        }
                        if (check) {
                            cardView[oldest_card_slot].setClickable(false);
                            shrinkCard(oldest_card_slot);
                            slotFilled[oldest_card_slot] = false;
                            updateScore(CARD_CLICKED, oldest_card_slot);
                            timeOfSet[oldest_card_slot] = Math.pow(99, 99); // ensures that the former oldest slot releases its title to be taken up by another slot, by giving it an infinitely far away future time
                        }
                    }
                }
                else {
                    pause_game(WAITING_FOR_RESULT);
                    boolean result = purchaseCoins(SOLVE_COST);
                    Log.println(INFO, "trackPowerUp", "consumeCollectible result: " + result);
                    if(result) {
                        view.callOnClick();
                    }
                    resumeGame(false, JUST_CONTINUE);
                }
            }
        });
        make_view_dormant(breakClearIcon);
        make_view_dormant(solveIcon); // when zen recalls this method to effect the change in round, this disables the power ups until the appropriate time
        make_view_dormant(slowIcon);
        if(inTutorial[0])
            make_view_dormant(retryIcon);

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

        retryIcon.setOnTouchListener(indicateTouch);
        cancelIcon.setOnTouchListener(indicateTouch);
        solveIcon.setOnTouchListener(indicateTouch);
        coinsLayout.setOnTouchListener(indicateTouch);
    }

    private void setRetrospectCardClickables() {
        cardView[mainGameCoClass.shuffled_slots[0]].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrospectCardClickActions(0, view);
            }
        });
        cardView[mainGameCoClass.shuffled_slots[1]].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrospectCardClickActions(1, view);
            }
        });
        cardView[mainGameCoClass.shuffled_slots[2]].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrospectCardClickActions(2, view);
            }
        });
        if (cardSlots > 3) {
            cardView[mainGameCoClass.shuffled_slots[3]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(3, view);
                }
            });
            cardView[mainGameCoClass.shuffled_slots[4]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(4, view);
                }
            });
        }
        if (cardSlots > 5) {
            cardView[mainGameCoClass.shuffled_slots[5]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(5, view);
                }
            });
            cardView[mainGameCoClass.shuffled_slots[6]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(6, view);
                }
            });
        }
        if (cardSlots > 7) {
            cardView[mainGameCoClass.shuffled_slots[7]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(7, view);
                }
            });
            cardView[mainGameCoClass.shuffled_slots[8]].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retrospectCardClickActions(8, view);
                }
            });
        }

        for (int j = 0; j < 9; j++) {
            cardView[j].setClickable(false);
        }
    }

    private void setDonTapCardClickables() {
        cardView[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donTapCardClickActions(0, view);
            }
        });
        cardView[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donTapCardClickActions(1, view);
            }
        });
        cardView[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donTapCardClickActions(2, view);
            }
        });
        if(cardSlots > 3) {
            cardView[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(3, view);
                }
            });
            cardView[4].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(4, view);
                }
            });
        }
        if(cardSlots > 5) {
            cardView[5].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(5, view);
                }
            });
            cardView[6].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(6, view);
                }
            });
        }
        if(cardSlots > 7) {
            cardView[7].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(7, view);
                }
            });
            cardView[8].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    donTapCardClickActions(8, view);
                }
            });
        }
        for (int j = 0; j < 9; j++) {
            cardView[j].setClickable(false);
        }
    }

    private void retrospectCardClickActions(int index, final View card){
        if(!gameRunning) return;
        if (cardResourceId[mainGameCoClass.ran_index[index]].equals(cardResourceId[mainGameCoClass.ord_index[clkPos]])) {
            card.setClickable(false);
            playSoundEffect(CARD_CLICKED);
            slotFilled[mainGameCoClass.shuffled_slots[index]] = false;
            give_extra_time();
            clkPos++;
            updateScore(CARD_CLICKED, mainGameCoClass.shuffled_slots[index]);
            if(gameMode == ZEN){
                comboUmpire(COMBO_PLUS);
            }
            shrinkCard(mainGameCoClass.shuffled_slots[index]);
        } else {
            for(int i = 0; i < cardSlots; i++){
                cardIsPulsing[i] = false;
            }
            playSoundEffect(WRONG_SELECTION);
            failurePosition = index;//lets the wrongSelectionAction know which card to pulse
            wrongSelectionAction.start();
        }
        if (clkPos == cardSlots) {
            gameUmpire(ROUND_COMPLETED);
        }
    }

    private void donTapCardClickActions(int index, final View card){
        if(!gameRunning) return;
        if (!(cardResourceId[activeIndex[index]].equals(cardResourceId[badEgg]))) {
            cardView[index].setClickable(false);
            playSoundEffect(CARD_CLICKED);
            slotFilled[index] = false;
            updateScore(CARD_CLICKED, index);
            shrinkCard(index);
            timeOfSet[index] = Math.pow(99, 99); // ensures that the timeOfSet is nullified for empty slots
            if(gameMode == ZEN) {
                coinPocket++;
                if(coinPocket >= 3){
                    emptyCoinPocket();
                }
            }
        } else {
            for(int i = 0; i < cardSlots; i++) {
                cardIsPulsing[i] = false;
            }
            playSoundEffect(WRONG_SELECTION);
            failurePosition = index;
            wrongSelectionAction.start();
        }
        card.setAlpha(1.0f);
    }

    private void gameUmpire(int callGrounds){

        if (callGrounds == LEVEL_STARTED) {
            for (int i = 0; i < 9; i++) { // ensures clear board for a new level to start
                cardView[i].setVisibility(View.INVISIBLE);
            }
            dismissPopUp();
            inflateGameLayout();
            gameCreated = true;
        }

        if (callGrounds == TUTORIAL_RESET) {
            for(int i = 0; i < 9; i++) {
                if (cardInitialized[i])
                    cardView[i].setImageResource(cardBackResource);
                else cardView[i].setImageResource(emptySlotResource);
                cardView[i].setVisibility(View.VISIBLE);
            }
            reset_game(RESET_ROUND); // resets game to normal operation(disables all power ups) once a round is completed
            Log.println(INFO, "trackGameUmpire", "reset_game Returned");
            roundsPlayed = -7; //simulates 0 progress for tutorial purposes
            if(gameMode == DON_TAP){
                showTutorialPopUp(DON_TAP_HINT);
            }
            else {
                prepareRound();
            }
        }

        if (callGrounds == ROUND_COMPLETED) {
            roundsPlayed++;
            playSoundEffect(ROUND_COMPLETED);
            updateScore(TIME_BONUS, -13);
            Log.println(INFO, "trackGameUmpire", "updateScore Returned");
            reset_game(RESET_ROUND); // resets game to normal operation(disables all power ups) once a round is completed
            Log.println(INFO, "trackGameUmpire", "reset_game Returned");
            if(inTutorial[0]){
                showTutorialPopUp(WELL_DONE);
            }
            else if (roundsPlayed == mainGameCoClass.rounds_due) {
                levelCompleted = true;
            }
            else if (!levelCompleted) {
                if(gameMode == ZEN && inBlitzMode && zenActiveGameMode == DON_TAP){
                    updateBlitz(AWARD_BLITZ);
                }
                prepareRound();
            }
        }

        if (callGrounds == WRONG_SELECTION) {
            if(inTutorial[0]){
                showTutorialPopUp(WRONG_SELECTION);
            }
            else {
                Log.println(INFO, "GameUmpire", "Wrong Selection!");
                pause_game(FAILED_GAME);
            }
        }

        if (callGrounds == TIME_UP) {
            if(inTutorial[0]){
                showTutorialPopUp(TIME_UP);
            }
            else {
                Log.println(INFO, "GameUmpire", "Time Up!");
                pause_game(FAILED_GAME);
            }
        }

        if (levelCompleted) {
            levelStopTime = System.currentTimeMillis();
            make_view_dormant(retryIcon);
            make_view_dormant(cancelIcon);
            pause_game(LEVEL_COMPLETED);
            startEndGameProcess(gameMode);
        }
    }

    private void comboUmpire(final int action){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int r = 200 - roundsPlayed;
                if(r < 5) r = 5;
                switch (action){
                    case COMBO_PLUS:
                        timeBarProgress += r;
                        synchronized (pauseLock) {
                            pauseLock.notifyAll();
                        }
                        break;
                    case MULTIPLIER_PLUS:
                        multiplier++;
                        if(multiplier > 99) multiplier = 99;
                        if(timeBarProgress >= timeBar.getMax()*2)
                            timeBarProgress = timeBar.getMax();
                        updateScore(MULTIPLIER_PLUS, multiplier);
                        if(multiplier > bestCombo) bestCombo = multiplier;
                        timeBarThreadHandler.post(comboBarRunnable);
                        break;
                    case COMBO_LOST:
                        multiplier = 1;
                        timeBarProgress = 0;
                        updateScore(COMBO_LOST, multiplier);
                        break;
                }
            }
        });

    }

    private void updateBlitz(int successWeight){
        int blitzIncrement;
        if(successWeight >= Achievements.RETROSPECT_PLATINUM_BASE_SCORE) {
            blitzIncrement = 100;
        }
        else if(successWeight >= Achievements.RETROSPECT_GOLD_BASE_SCORE) {
            blitzIncrement = 50;
        }
        else if(successWeight >= Achievements.RETROSPECT_SILVER_BASE_SCORE) {
            blitzIncrement = 40;
        }
        else if(successWeight >= Achievements.RETROSPECT_BRONZE_BASE_SCORE) {
            blitzIncrement = 30;
        }
        else if (successWeight == RESET_BLITZ){
            blitzBar1.setProgress(0);
            blitzBar2.setProgress(0);
            blitzBar3.setProgress(0);
            stopBlitzMadness();
            inBlitzMode = false;
            return;
        }
        else if (successWeight == AWARD_BLITZ){
            blitzBar1.setProgress(0);
            blitzBar2.setProgress(0);
            blitzBar3.setProgress(0);
            multiplier += 2;
            comboUmpire(MULTIPLIER_PLUS);
            stopBlitzMadness();
            emptyCoinPocket();
            inBlitzMode = false;
            return;
        }
        else if(successWeight < 0){
            return;
        }
        else {
            blitzIncrement = 20;
        }

        int blitz1StepsLeft = blitzBar1.getMax() - blitzBar1.getProgress();
        int blitz2StepsLeft = blitzBar2.getMax() - blitzBar2.getProgress();
        int blitz3StepsLeft = blitzBar3.getMax() - blitzBar3.getProgress();

        if(blitzIncrement > blitz1StepsLeft) {
            blitzBar1.setProgress(100);
            blitzIncrement -= blitz1StepsLeft;
            if(blitzIncrement > blitz2StepsLeft){
                blitzBar2.setProgress(100);
                blitzIncrement -= blitz1StepsLeft;
                if(blitzIncrement > blitz3StepsLeft){
                    blitzBar3.setProgress(100);
                } else blitzBar3.setProgress(blitzBar3.getProgress() + blitzIncrement);
            } else blitzBar2.setProgress(blitzBar2.getProgress() + blitzIncrement);
        } else blitzBar1.setProgress(blitzBar1.getProgress() + blitzIncrement);

        if(blitzBar3.getProgress() >= blitzBar3.getMax()){
            inBlitzMode = true;
            startBlitzMadness();
        }
    }

    private void prepareBlitzMadness(){
        blitzMadness = ValueAnimator.ofObject(new FloatEvaluator(), -1f, 1f);
        blitzMadness.setDuration(50);
        blitzMadness.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float madness = (float) animation.getAnimatedValue();

                scoreText.setRotation(madness);
                backDrop.setRotation(madness);
                timeBarContainer.setRotation(madness);
                retryIcon.setRotation(madness);
                cancelIcon.setRotation(madness);
                breakClearIcon.setRotation(madness);
                solveIcon.setRotation(madness);
                slowIcon.setRotation(madness);
                breakClearUnits.setRotation(madness);
                solveUnits.setRotation(madness);
                slowUnits.setRotation(madness);
                coinsLayout.setRotation(madness);
                multiplierText.setRotation(7.5f + madness);
                blitzBar1.setRotation(madness);
                blitzBar2.setRotation(madness);
                blitzBar3.setRotation(madness);
            }
        });

        blitzMadness.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                scoreText.setRotation(0);
                backDrop.setRotation(0);
                timeBarContainer.setRotation(0);
                retryIcon.setRotation(0);
                cancelIcon.setRotation(0);
                breakClearIcon.setRotation(0);
                solveIcon.setRotation(0);
                slowIcon.setRotation(0);
                breakClearUnits.setRotation(0);
                solveUnits.setRotation(0);
                slowUnits.setRotation(0);
                coinsLayout.setRotation(0);
                multiplierText.setRotation(7.5f);
                blitzBar1.setRotation(0);
                blitzBar2.setRotation(0);
                blitzBar3.setRotation(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                scoreText.setRotation(0);
                backDrop.setRotation(0);
                timeBarContainer.setRotation(0);
                retryIcon.setRotation(0);
                cancelIcon.setRotation(0);
                breakClearIcon.setRotation(0);
                solveIcon.setRotation(0);
                slowIcon.setRotation(0);
                breakClearUnits.setRotation(0);
                solveUnits.setRotation(0);
                slowUnits.setRotation(0);
                coinsLayout.setRotation(0);
                multiplierText.setRotation(0);
                blitzBar1.setRotation(0);
                blitzBar2.setRotation(0);
                blitzBar3.setRotation(0);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        blitzMadness.setRepeatMode(ValueAnimator.REVERSE);
        blitzMadness.setRepeatCount(ValueAnimator.INFINITE);
    }

    private void startBlitzMadness(){
        if(blitzMadness != null) {
            if (blitzMadness.isRunning())
                blitzMadness.end();
            blitzMadness.start();
        }

        playSoundEffect(IN_BLITZ);
    }

    private void stopBlitzMadness(){
        if(blitzMadness != null) {
            if (blitzMadness.isRunning())
                blitzMadness.end();
        }

        if(tactileFeedback != null)
            tactileFeedback.cancel();
    }

    private void shootFireWorks(){
        for(int i = 0; i < 4; i++) {
            final int j = i;
            i = i == 3 ? 5 : i;
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (j == 2) {
                        explode1.setVisibility(View.VISIBLE);
                        explodeAnim1.start();
                    }
                    if (j == 0) {
                        playSoundEffect(FIREWORKS);
                        explode2.setVisibility(View.VISIBLE);
                        explodeAnim2.start();
                    }
                    if (j == 1) {
                        explode3.setVisibility(View.VISIBLE);
                        explodeAnim3.start();
                    }
                    if (j == 3)
                        tactileFeedback.cancel();
                }
            }, 300 * i);
        }
    }

    private void prepare_tutorial_pop_up(){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popLayout = new RelativeLayout(this);
        View popView = new View(this);
        if (inflater != null)
            popView = inflater.inflate(R.layout.tutorial_center_pop_up, popLayout);

        // create the popup window
        int popUpWidth = (int) Math.rint(screenHeight * 0.5);

        popupWindow = new PopupWindow(popView);
        popupWindow.setWidth(popUpWidth);
        popupWindow.setHeight(WRAP_CONTENT);
        popupWindow.setFocusable(true);

        popView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                dismissPopUp();
                return true;
            }
        });


        popupWindow.setAnimationStyle(R.style.center_pop_up_anim);

        if (tutorialActionsRunnable == null)
            tutorialActionsRunnable = new TutorialActionRunnable();
        if (taskThread == null) {
            tutorialThread = new HandlerThread("tutorialThread");
        }
        if(!tutorialThread.isAlive()) {
            tutorialThread.start();
            Handler tutorialHandler = new Handler(tutorialThread.getLooper());
            tutorialHandler.post(tutorialActionsRunnable);
        }

    }

    private void showTutorialPopUp(final int popReason){
        if(mActivity.hasWindowFocus()) {
            if(!(popReason == SOLVE_HINT || popReason == CLEAR_HINT)) freezeGame();
            pause_game(TUTORIAL_POP_UP);
            int popUpWidth = (int) Math.rint(screenWidth * 0.8);
            int popUpHeight = (int) Math.rint(popUpWidth * 0.6874);
            int yLocation = (int) Math.rint(screenHeight * 0.37);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            int xOff, yOff;
            if (!popupWindow.isShowing()) {
                popupWindow.showAtLocation(backDrop, NO_GRAVITY, xLocation, yLocation);
            }
            switch (popReason) {
                case SHOW_WELCOME:
                    popLayout.setBackgroundResource(R.drawable.welcome_image);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case RETROSPECT_HINT:
                    tutorialGif.setImageResource(R.drawable.retrospect_tutorial);
                    tutorialGif.setVisibility(View.VISIBLE);
                    scoreText.setVisibility(View.INVISIBLE);
                    coinsLayout.setVisibility(View.INVISIBLE);
                    popLayout.setBackgroundResource(R.drawable.hint_one_image);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case RETROSPECT_CLICK_HINT:
                    popLayout.setBackgroundResource(R.drawable.hint_two_image);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case DON_TAP_HINT:
                    tutorialGif.setImageResource(R.drawable.dont_tap_tutorial);
                    tutorialGif.setVisibility(View.VISIBLE);
                    scoreText.setVisibility(View.INVISIBLE);
                    coinsLayout.setVisibility(View.INVISIBLE);
                    popLayout.setBackgroundResource(R.drawable.dont_tap_one_image);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case DON_TAP_CLICK_HINT:
                    popLayout.setBackgroundResource(R.drawable.hint_dont_tap);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case WELL_DONE:
                    tutorialGif.setVisibility(View.INVISIBLE);
                    scoreText.setVisibility(View.VISIBLE);
                    coinsLayout.setVisibility(View.VISIBLE);
                    popLayout.setBackgroundResource(R.drawable.well_done);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case TIME_UP:
                    popLayout.setBackgroundResource(R.drawable.too_slow);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case WRONG_SELECTION:
                    popLayout.setBackgroundResource(R.drawable.thats_wrong);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
                case POWER_UP_HINT:
                    updateCollectiblesDisplay();
                    popUpWidth = (int) Math.rint(screenHeight * 0.4);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.45);
                    xOff = (powerUpsContainer.getWidth() - popUpWidth) / 2;
                    yOff = popUpHeight + powerUpsContainer.getHeight();
                    popLayout.setBackgroundResource(R.drawable.power_ups_tut);
                    breakPouch = 1;
                    clearPouch = 1;
                    solvePouch = 3;
                    slowPouch = 1;
                    popupWindow.update(powerUpsContainer, xOff, -yOff, popUpWidth, popUpHeight);
                    break;
                case BREAK_HINT:
                    popLayout.setBackgroundResource(R.drawable.break_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = breakClearIcon.getWidth() / 5;
                    yOff = (int) Math.rint(solveIcon.getWidth() / 8.0) +
                                popUpHeight + breakClearIcon.getHeight();
                    popupWindow.update(breakClearIcon, -xOff, -yOff, popUpWidth, popUpHeight);
                    make_view_active(breakClearIcon);
                    break;
                case CLEAR_HINT:
                    popLayout.setBackgroundResource(R.drawable.clear_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = breakClearIcon.getWidth() / 5;
                    yOff = (int) Math.rint(solveIcon.getWidth() / 8.0) +
                                popUpHeight + breakClearIcon.getHeight();
                    popupWindow.update(breakClearIcon, -xOff, -yOff, popUpWidth, popUpHeight);
                    gameMode = DON_TAP; // fake gameMode to simply show clear broom icon
                    updateCollectiblesDisplay();
                    make_view_active(breakClearIcon);
                    break;
                case SOLVE_HINT:
                    popLayout.setBackgroundResource(R.drawable.solve_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = solveIcon.getWidth() / 5;
                    yOff = (int) Math.rint(solveIcon.getWidth() / 8.0) +
                            popUpHeight + solveIcon.getHeight();
                    popupWindow.update(solveIcon, -xOff, -yOff, popUpWidth, popUpHeight);
                    make_view_active(solveIcon);
                    break;
                case SLOW_HINT:
                    timeBar.setVisibility(View.VISIBLE);
                    clockIcon.setVisibility(View.VISIBLE);
                    popLayout.setBackgroundResource(R.drawable.slow_down_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = (int) Math.rint(solveIcon.getWidth() / 1.25);
                    yOff = (int) Math.rint(solveIcon.getWidth() / 8.0) +
                            popUpHeight + slowIcon.getHeight();
                    popupWindow.update(solveIcon, xOff, -yOff, popUpWidth, popUpHeight);
                    make_view_active(slowIcon);
                    break;
                case TIME_BAR_HINT:
                    timeBar.setVisibility(View.VISIBLE);
                    clockIcon.setVisibility(View.VISIBLE);
                    resetTimeBarProgress();
                    popLayout.setBackgroundResource(R.drawable.time_bar_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = timeBar.getWidth()/2;
                    yOff = (int) Math.rint(scoreText.getHeight()*0.8);
                    popupWindow.update(scoreText, -xOff, -yOff, popUpWidth, popUpHeight);
                    break;
                case ROUNDS_HINT:
                    popLayout.setBackgroundResource(R.drawable.rounds_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = roundsText.getWidth() / 2;
                    yOff = (int) Math.rint((popupWindow.getHeight() + roundsText.getHeight())*1.02);
                    popupWindow.update(roundsText, -xOff, -yOff, popUpWidth, popUpHeight);
                    break;
                case RETRY_HINT:
                    popLayout.setBackgroundResource(R.drawable.quick_retry_tut);
                    popUpWidth = (int) Math.rint(screenHeight * 0.2);
                    popUpHeight = (int) Math.rint(popUpWidth * 0.68843);
                    xOff = retryIcon.getWidth() / 6;
                    yOff = (int) Math.rint(scoreText.getHeight()*0.25);
                    popupWindow.update(retryIcon, -xOff, yOff, popUpWidth, popUpHeight);
                    break;
                case TUTORIAL_COMPLETE:
                    popLayout.setBackgroundResource(R.drawable.tutorial_complete_1);
                    popupWindow.update(popUpWidth, popUpHeight);
                    break;
            }
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    dismissTutorialAction = popReason;
                    tutorialUpdated = true;
                    synchronized (tutorialPauseLock) {
                        tutorialPauseLock.notifyAll();
                    }
                }
            });
        }
    }

    private void disableTutorials(){
        mProgress.disableTutorials();
    }

    private void prepareGameInflation(){
        mainGameInflater = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 1f);
        mainGameInflater.setDuration(500);
        mainGameInflater.setInterpolator(new OvershootInterpolator());
        mainGameInflater.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float appear = (float) valueAnimator.getAnimatedValue();
                retryIcon.setScaleX(appear);
                retryIcon.setScaleY(appear);
                cancelIcon.setScaleX(appear);
                cancelIcon.setScaleY(appear);
                coinsLayout.setPivotY(0f);
                coinsLayout.setScaleY(appear);
                scoreText.setAlpha(appear);
                backDrop.setScaleX(appear);
                backDrop.setScaleY(appear);
                powerUpsContainer.setScaleX(appear);
                timeBarContainer.setScaleX(appear);
                blitzContainer.setScaleX(appear);
                multiplierText.setScaleX(appear);
                multiplierText.setScaleY(appear);
                honeyJar.setScaleX(appear);
                honeyJar.setScaleY(appear);
                bonusCoinsText.setScaleX(appear);
                bonusCoinsText.setScaleY(appear);
                if(appear >= 1) {
                    timeBarContainer.setTranslationY(timeBarContainer.getHeight() * (1 - appear));
                    blitzContainer.setTranslationY(timeBarContainer.getHeight() * (1 - appear));
                    multiplierText.setTranslationY(timeBarContainer.getHeight() * (1 - appear));
                    multiplierText.setTranslationX(multiplierText.getWidth() * (appear - 1));
                }
            }
        });
        mainGameInflater.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                retryIcon.setVisibility(View.VISIBLE);
                cancelIcon.setVisibility(View.VISIBLE);
                coinsLayout.setVisibility(View.VISIBLE);
                scoreText.setVisibility(View.VISIBLE);
                timeBarContainer.setVisibility(View.VISIBLE);
                backDrop.setVisibility(View.VISIBLE);
                powerUpsContainer.setVisibility(View.VISIBLE);
                honeyJar.setVisibility(View.VISIBLE);
                bonusCoinsText.setVisibility(View.VISIBLE);
                if(gameMode != ZEN){
                    blitzBar1.setVisibility(View.INVISIBLE);
                    blitzBar2.setVisibility(View.INVISIBLE);
                    blitzBar3.setVisibility(View.INVISIBLE);
                    multiplierText.setVisibility(View.GONE);
                }
                else {
                    blitzBar1.setVisibility(View.VISIBLE);
                    blitzBar2.setVisibility(View.VISIBLE);
                    blitzBar3.setVisibility(View.VISIBLE);
                    multiplierText.setVisibility(View.VISIBLE);
                    clockIcon.setVisibility(View.GONE);
                    ((RelativeLayout.LayoutParams)timeBar.getLayoutParams()).width =
                            (int) Math.rint(timeBarContainer.getWidth()*0.6);
                }
                playSoundEffect(LEVEL_STARTED);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(inTutorial[0])
                    showTutorialPopUp(SHOW_WELCOME);
                prepareRound();
                Log.println(INFO, "trackGameInflater", "Returned prepareRound and gameRunning = " + gameRunning);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void inflateGameLayout(){
        if(gameCreated){
            mainGameInflater.setStartDelay(0);
        } else mainGameInflater.setStartDelay(1000);
        if(mainGameInflater.isRunning())
            mainGameInflater.cancel();
        mainGameInflater.start();
    }

    private void prepareRound() {

        make_view_dormant(breakClearIcon);
        make_view_dormant(solveIcon);
        make_view_dormant(slowIcon);
        Log.println(INFO, "trackPrepareRound", "making power ups dormant");
        Log.println(INFO, "trackPrepareRound", "Can Be Solved = " + solveIcon.isClickable());
        updateScore(RESET_ROUND, -13);
        if(!(inTutorial[0] || inTutorial[1]))
            dismissPopUp();
        Log.println(INFO, "trackPrepareRound", "Game Running = " + gameRunning);


        if (gameMode == DON_TAP) {
            timeBar.setProgress(0); //this ensures that the flashing thread is functional for the Don'tap mode also by validating one of the checks by default
            timeBar.setVisibility(View.INVISIBLE); // time bar not needed for don't tap
            if(inTutorial[0]) clockIcon.setVisibility(View.INVISIBLE);
            for (int i = 0; i < cardSlots; i++) {
                timeOfSet[i] = Math.pow(99, 99); // ensures that the former oldest slot releases its title to be taken up by another slot, by giving it an infinitely far away future time
            }
            if(roundsPlayed > 0 || inTutorial[0]) {//play button is never used to start during tutorial
                showBadEggThenLaunchRound();
            }
            else{
                fadeViewForAction(playButton, 0f, 1f, 200, NO_SPECIAL_CASE);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setClickable(false);
                        showBadEggThenLaunchRound();
                    }
                });
            }
        }


        else if (gameMode == RETROSPECT) {

            if (roundsPlayed > 0 || (roundsPlayed == -7 && (inTutorial[0] || inTutorial[1]))) {//play button is never used during tutorial
                starts_round(gameMode);
            }
            else{
                fadeViewForAction(playButton, 0f, 1f, 200, NO_SPECIAL_CASE);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setClickable(false);
                        fadeViewForAction(playButton, 1f, 0f, 200, HIDE_PLAY_BUTTON_START_ROUND);
                    }
                });
            }
        }


        else if (gameMode == ZEN) {

            if (inBlitzMode) zenActiveGameMode = DON_TAP;
            else zenActiveGameMode = RETROSPECT;
            zen_round_update();

            if (zenActiveGameMode == DON_TAP) {
                timeBar.setProgress(0); //this ensures that the flashing thread is functional for the Don'tap mode also by validating one of the checks by default
                timeBar.setVisibility(View.INVISIBLE); // time bar not needed for don't tap
                for (int i = 0; i < cardSlots; i++) {
                    timeOfSet[i] = Math.pow(99, 99); // ensures that the former oldest slot releases its title to be taken up by another slot, by giving it an infinitely far away future time
                }
                showBadEggThenLaunchRound();
            }
            else if (zenActiveGameMode == RETROSPECT) {
                if(roundsPlayed > 0) {
                    timeBar.setVisibility(View.VISIBLE); // time bar needed for retrospect
                    starts_round(zenActiveGameMode);
                }
                else {
                    fadeViewForAction(playButton, 0f, 1f, 200, NO_SPECIAL_CASE);
                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.setClickable(false);
                            fadeViewForAction(playButton, 1f, 0f, 200, HIDE_PLAY_BUTTON_START_ROUND);
                        }
                    });
                }
            }
        }
        if(inTutorial[0]) playButton.setVisibility(View.INVISIBLE);//play button isn't used for the tutorials
        else updateCollectiblesDisplay();//collectibles aren't used for tutorials
    }

    private void setGameRunners(int gameModeIdentifier)  {
        if (gameModeIdentifier == RETROSPECT) {
            if (retrospectRoundRunnable == null)
                retrospectRoundRunnable = new RetrospectRoundRunnable();
            if (retrospectTimerRunnable == null )
                retrospectTimerRunnable = new RetrospectTimerRunnable();
            if (timeBarRunnable == null)
                timeBarRunnable = new TimeBarRunnable();
            if (timeBarThread == null)
                timeBarThread = new HandlerThread("timeBarThread");
        }
        if (gameModeIdentifier == DON_TAP) {
            if (donTapRoundRunnable == null)
                donTapRoundRunnable = new DonTapRoundRunnable();
            if (donTapTimerRunnable == null)
                donTapTimerRunnable = new DonTapTimerRunnable();
        }
        if(comboBarRunnable == null && gameMode == ZEN){
            comboBarRunnable = new ComboBarRunnable();
        }
        if(timeBarThread != null) {
            if (!timeBarThread.isAlive() && gameMode != DON_TAP) {
                timeBarThread.start();
                timeBarThreadHandler = new Handler(timeBarThread.getLooper());
            }
        }
        if (gameThread == null)
            gameThread = new HandlerThread("gameThread");
        if(!gameThread.isAlive()) {
            gameThread.start();
            gameThreadHandler = new Handler(gameThread.getLooper());
        }
        if (timerThread == null)
            timerThread = new HandlerThread("timerThread");
        if(!timerThread.isAlive()) {
            timerThread.start();
            timerThreadHandler = new Handler(timerThread.getLooper());
        }
        if (cardPulseControlRunnable == null)
            cardPulseControlRunnable = new CardPulseControl();
        if (taskThread == null) {
            taskThread = new HandlerThread("taskThread");
        }
        if(!taskThread.isAlive()) {
            taskThread.start();
            taskThreadHandler = new Handler(taskThread.getLooper());
            taskThreadHandler.post(cardPulseControlRunnable);
        }
    }

    private void starts_round(int gameModeToStart){

        if (!gameRunning) {// in case a new round is started while the !gameRunning, then ensure the game starts running
            resumeGame(true, JUST_CONTINUE);
            Log.println(INFO, "trackStartsRound", "Game Running = " + gameRunning);
        }
        playButton.setVisibility(View.INVISIBLE);//No playButton once round is started
        for (int i = 0; i < 9; i++) { // ensures visible cards for a new round to start
            if(i >= cardSlots)
                cardView[i].setImageResource(emptySlotResource);
            cardView[i].setVisibility(View.VISIBLE);
        }
        setGameRunners(gameModeToStart);


        if (gameModeToStart == DON_TAP) {
            setDonTapCardClickables();
            if (!gameActive && gameThread.isAlive()) {
                gameThreadHandler.post(donTapRoundRunnable);
            }
            restartTimer();
            make_view_active(breakClearIcon);
            make_view_active(solveIcon);
            make_view_active(slowIcon);
        }


        if(gameModeToStart == RETROSPECT){
            mainGameCoClass.indexGen();
            setRetrospectCardClickables();
            clkPos = 0;
            Log.println(INFO, "trackStartsRound", "Game Thread Is Dead = " + !gameThread.isAlive());
            if(gameThread.isAlive())
                gameThreadHandler.post(retrospectRoundRunnable);
            Log.println(INFO, "trackStartsRound", "Game Thread Started = " + gameThread);
            Log.println(INFO, "trackStartsRound", "Can Be Solved = " + solveIcon.isClickable());
        }
        if(roundsPlayed == 0) {
            levelStartTime = System.currentTimeMillis();
            Log.println(INFO, "trackStartsRound", "Game Started At = " + levelStartTime);
        }
    }

    private void showBadEggThenLaunchRound(){
        for (int i = 0; i < 9; i++) { // ensures clear board for a new level to start
            cardView[i].setVisibility(View.INVISIBLE);
        }
        mainGameCoClass.indexGen(); //this generates the index used for choosing the bad egg(S).
        badEgg = mainGameCoClass.card_index.get(7);
        badCard.setImageResource(cardResourceId[badEgg]);
        if(roundsPlayed > 0) {
            fadeViewForAction(badEggInfo, 0f, 1f, 800, SHOW_BAD_EGG);
        }
        else fadeViewForAction(playButton, 1f, 0f, 200, HIDE_PLAY_BUTTON_SHOW_BAD_EGG);
    }

    private void discardTimer(){
        boolean aBoolean = timerNeeded;// temporarily store timerNeeded
        boolean aBoolean1 = timeBarNeeded;
        while(timerActive || timeBarActive) {
            timerNeeded = false; //notify all timerThreads to discard their timing events
            timeBarNeeded = false;
            synchronized (timerLock) {
                timerLock.notifyAll();
            }
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
        timerNeeded = aBoolean;//restore the application's knowledge of whether or not the timer is still needed
        timeBarNeeded = aBoolean1;
    }

    private void restartTimer(){
        discardTimer();
        Log.println(INFO, "trackTimer", "Timer Restarting...");
        if (timerThreadHandler != null && timerThread != null && timerThread.isAlive()) {//resuming timer
            if(gameMode == RETROSPECT) {
                timerThreadHandler.post(retrospectTimerRunnable);
                if (!timeBarActive && timeBarThreadHandler != null && timeBarThread.isAlive()) {
                    timeBarThreadHandler.post(timeBarRunnable);
                    Log.println(INFO, "timeBarHandler", "Posting Time Bar, timeBarActive:" + !timeBarActive);
                }
            }
            else if(gameMode == DON_TAP || zenActiveGameMode == DON_TAP)
                timerThreadHandler.post(donTapTimerRunnable);
        }
        if(gameMode == ZEN) {
            if(timeBarThread != null){
                if (!timeBarActive && timeBarThreadHandler != null && timeBarThread.isAlive()) {
                    timeBarThreadHandler.post(comboBarRunnable);
                    Log.println(INFO, "timeBarHandler", "Posting Combo Bar, comboBarActive:" + !timeBarActive);
                }
            }
        }
    }

    private void resetTimeBarProgress(){
        boolean aBoolean1 = timeBarNeeded;
        while(timeBarActive) {
            timeBarNeeded = false;
            synchronized (timerLock) {
                timerLock.notifyAll();
            }
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
        timeBarNeeded = aBoolean1;
        final int p = gameMode == ZEN ? 0 : timeBar.getMax();
        if(timeBarThreadHandler != null){
            timeBarThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    timeBar.setProgress(p);
                    timeBar.setSecondaryProgress(0);
                }
            });
        }

    }

    private void pause_game(int pauseCause){
        gameRunning = false;
        if (taskThreadHandler != null && taskThread != null) {
            if (taskThread.isAlive())
                taskThreadHandler.post(cardPulseControlRunnable);
        }
        stopBlitzMadness();
        switch (pauseCause) {
            case LOST_WINDOW_FOCUS:  // this ensures that player cannot interact with game while it is paused.
                onLostFocusFreezeAndShowPlayIcon();
                breakClearIcon.setClickable(false);
                slowIcon.setClickable(false);
                solveIcon.setClickable(false);
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "Lost Window Focus");
                Log.println(INFO, "trackPauseGame", "Done Freezing");
                break;
            case FAILED_GAME:  // this ensures that player cannot interact with game while it is paused.
                onFailFreezeAndShowPopupWindow();
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "FAILED GAME");
                Log.println(INFO, "trackPauseGame", "Done Freezing");
                break;
            case CLOCK_BREAK:
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "Clock Broken");
                break;
            case WAITING_FOR_RESULT:
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "Waiting for result...");
                break;
            case RETRYING:
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "Retrying...");
                break;
            case LEVEL_COMPLETED:
                if (gameThread != null)
                    gameThread.quit();
                if (timerThread != null)
                    timerThread.quit();
                if (taskThread != null) {
                    taskThread.quit();
                    taskThread = null;
                }
                if (timeBarThread != null)
                    timeBarThread.quit();
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "Level Completed!");
                break;
            case TUTORIAL_POP_UP:
                Log.println(INFO, "trackPauseGame", "Game Running = " + gameRunning);
                Log.println(INFO, "trackPauseGame", "New Tutorial Message...");
                break;
        }

        discardTimer();
    }

    private void resumeGame(boolean should_call_reset, int resumeMode) {
        playButton.setVisibility(View.INVISIBLE);//no play button when game is resumed
        for (int j = 0; j < cardSlots; j++) {// for any kind of resumption, cards should always be fully visible
            cardView[j].setAlpha(1.0f);
            Log.println(INFO, "resumeGame", "Slot " + j + " is filled = " + slotFilled[j]);
            if(slotFilled[j])
                cardView[j].setClickable(true);
            Log.println(INFO, "resumeGame", "Card " + j + " is clickable = " + cardView[j].isClickable());
        }
        Log.println(INFO, "trackResumeGame", "Game Continuing...");
//        if(!gameRunning) {
            if(resumeMode >= JUST_CONTINUE){
                if(gameMode == ZEN && inBlitzMode) startBlitzMadness();
                if (gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT) {
                    timeAllowed = timeBar.getProgress(); // continues clock from where it stopped
                }
                if ((gameMode == DON_TAP || zenActiveGameMode == DON_TAP)) {
                    for (int i = 0; i < cardSlots; i++) { // allows a small buffer time after resumption of DonTap
                        timeOfSet[i] = System.currentTimeMillis() + (i * mainGameCoClass.time);
                    }
                }
                //the three conditions below use the alpha of the view to know whether to make it responsive on resume or not
                if(solveIcon.getAlpha() == 1.0f){
                    solveIcon.setClickable(true);
                    Log.println(INFO, "trackResumeGame", "Enabling Solve Icon...");
                }
                if(slowIcon.getAlpha() == 1.0f || slowedDown){
                    slowIcon.setClickable(true);
                    Log.println(INFO, "trackResumeGame", "Enabling Slow Icon...");
                }
                if(breakClearIcon.getAlpha() == 1.0f || clockBroken){
                    breakClearIcon.setClickable(true);
                    Log.println(INFO, "trackResumeGame", "Enabling Break Icon...");
                }
            }
            if(resumeMode >= RESET_TIME){
                if(should_call_reset) {
                    reset_game(resumeMode);
                }
            }
            if (resumeMode == RESET_ROUND) {
                make_view_active(breakClearIcon);
                make_view_active(slowIcon);
                make_view_active(solveIcon);
            }
            Log.println(INFO, "resumeGame", "clockBroken = " + clockBroken);
            if(!clockBroken){ // ensures that the timer is not resumed when the clock is meant to be broken
                synchronized (pauseLock) {
                    gameRunning = true;
                    pauseLock.notifyAll();
                    Log.println(INFO, "resumeGame", "Notifying Pause Lock...");
                    if(timerNeeded) {
                        restartTimer();
                        Log.println(INFO, "trackResumeGame", "Timer restarting because timerNeeded = " + timerNeeded);
                    }
                    if (taskThreadHandler != null && taskThread != null) {
                        if (taskThread.isAlive())
                            taskThreadHandler.post(cardPulseControlRunnable);
                    }
                }
            }
//        }
        Log.println(INFO, "resumeGame", "Finished resume...");
    }

    private void executeDelayedResume() {
        countDownText.setText("");
        new Thread(){
            private void setNumber(final int number){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setText(String.format(Locale.ENGLISH, "%d", number));
                    }
                });
            }
            private void disableSelf(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setVisibility(View.INVISIBLE);
                    }
                });
            }
            private void resumeNow(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resumeGame(true, RESET_ROUND);
                    }
                });
            }
            private void positionCount(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setVisibility(View.VISIBLE);
                        int textSize = (int) Math.rint(screenHeight * 0.15);
                        countDownText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        RelativeLayout.LayoutParams countDownTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        countDownTextParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        countDownText.setLayoutParams(countDownTextParams);
                    }
                });
            }
            @Override
            public void run(){
                synchronized (countDownLock) {
                    try {
                        countDownLock.wait(200);//wait for pop up animation to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                make_view_active(retryIcon);
                for(int i = 3; i >= 1; i--){
                    positionCount();
                    setNumber(i);
                    synchronized (countDownLock) {
                        try {
                            countDownLock.wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(gameRunning || !mActivity.hasWindowFocus()) break;
                    if(i == 1){
                        resumeNow();
                    }
                }
                disableSelf();
            }
        }.start();
    }

    private void reset_game(int resetMode) {
        for (int j = 0; j < cardSlots; j++) {
            cardView[j].setAlpha(1.0f);
        }
        breakClearIcon.setScaleX(1.0f);//to show that icon is released
        breakClearIcon.setScaleY(1.0f);
        slowIcon.setScaleX(1.0f);//to show that icon is released
        slowIcon.setScaleY(1.0f);
        if (resetMode >= RESET_TIME){
            discardTimer();
            playSoundEffect(RESET_TIME);
            if (gameMode == RETROSPECT) {
                timeAllowed = mainGameCoClass.time_allowed;
                timeBarProgress = timeAllowed;
                timeBar.setMax(timeAllowed);
                resetTimeBarProgress();
            }
            else if ((gameMode == DON_TAP || zenActiveGameMode == DON_TAP)) {
                for (int i = 0; i < cardSlots; i++) {
                    timeOfSet[i] = System.currentTimeMillis() + (i * mainGameCoClass.time);
                }
            }
        }
        if (resetMode >= RESET_ROUND){
            extraCountdowns = 0;
            levelCompleted = false;
            mainGameCoClass.resetGameSpeed(level, gameMode, inTutorial[0]);
            if (taskThreadHandler != null && taskThread != null) {
                if (taskThread.isAlive())
                    taskThreadHandler.post(cardPulseControlRunnable);
            }
            slowedDown = false;
            clockBroken = false;
        }
        for(int i = 0; i < 9; i++) {
            cardIsPulsing[i] = false;
            Log.println(INFO, "trackResetGame", "Cancelling Card Pulse... " + i + " = " + !cardIsPulsing[i]);
        }
        if (resetMode >= RESET_LEVEL){
            restarting = true;
            pause_game(RETRYING);
            timerActive = false;
            timerNeeded = false;
            timeBarNeeded = false;
            updateScore(RESET_LEVEL, -13);
            comboUmpire(COMBO_LOST);
            updateBlitz(RESET_BLITZ);
            resetTimeBarProgress();
            dismissPopUp();
            resetCoins();
            if(!inTutorial[0])
                updateCollectiblesDisplay();
            if ((gameMode == DON_TAP || zenActiveGameMode == DON_TAP)) {
                for (int i = 0; i < cardSlots; i++) {
                    timeOfSet[i] = Math.pow(99, 99); // ensures that the former oldest slot releases its title to be taken up by another slot, by giving it an infinitely far away future time
                    slotFilled[i] = false;
                }
            }
            boolean game_dead = false;
            while (!game_dead){
                game_dead = true;
                synchronized (restartLock){
                    try {
                        restartLock.wait(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (pauseLock){
                    pauseLock.notifyAll(); // notifies all threads waiting on pauseLock that a restart instruction is pending
                }
                if(gameThread != null){
                    game_dead = !gameActive;
                    Log.println(INFO, "trackResetLevel", "gameThread Is Dead = " + !gameActive);
                }
                if(timerThread != null){
                    game_dead = game_dead && !timerActive;
                    Log.println(INFO, "trackResetLevel", "timerThread Is Dead = " + !timerActive);
                }
                if(timeBarThread != null){
                    game_dead = game_dead && !timeBarActive;
                    Log.println(INFO, "trackResetLevel", "timeBarThread Is Dead = " + !timeBarActive);
                }
                Log.println(INFO, "trackResetLevel", "Game Is Dead = " + game_dead);
            }
            restarting = false;
            if(inTutorial[0] || inTutorial[1])
                gameUmpire(TUTORIAL_RESET);
            else{
                updateContinueCost(RESET_LEVEL);
                gameUmpire(LEVEL_STARTED);
            }
        }
    }

    private void zen_round_update() {
        mainGameCoClass.updateForZen(roundsPlayed, zenActiveGameMode);
        cardSlots = mainGameCoClass.card_slots;
        Log.println(INFO, "ZenUpdate", "Card Slots = " + cardSlots);
        Log.println(INFO, "ZenUpdate", "Slot Filled Size = " + slotFilled.length);
    }

    private void updateScore(int scoreGrounds, int extra_info) {
        if ((gameMode == DON_TAP) && extra_info != -13) {
            int c = 0;
            if(scoreGrounds == CARD_CLICKED) {
                double a = timeOfSet[extra_info], b = System.currentTimeMillis();
                c = (int) Math.rint(((mainGameCoClass.dwell_time - b + a) / mainGameCoClass.dwell_time) * Achievements.DON_TAP_MAX_BASE_SCORE);
                if (c > 10) {
                    c = 10;
                }
                if (c <= 0) {
                    c = 1;
                }
                if (timeOfSet[extra_info] < Math.pow(99, 99)) {
                    score += c;
                }
            }
            else if(scoreGrounds == BAD_EGG_IGNORED){
                c = 8;
                score += c;
            }
            commendPlayer(DON_TAP, c);
        }
        else if (gameMode == RETROSPECT) {
            if (scoreGrounds == CARD_CLICKED) {
                score++;
            }
            if (scoreGrounds == TIME_BONUS) {
                double a = timeBar.getProgress(), b = timeBar.getMax();
                Log.println(INFO, "Scoring", "Time Progress Is = " + a);
                int time_bonus = (int) Math.rint((a / b) * Achievements.RETROSPECT_MAX_BASE_SCORE);
                score += time_bonus;
                commendPlayer(RETROSPECT, time_bonus);
            }
        }
        else if (gameMode == ZEN) {
            if(zenActiveGameMode == RETROSPECT) {
                if (scoreGrounds == CARD_CLICKED) {
                    score++;
                }
                if (scoreGrounds == TIME_BONUS) {
                    int bonus = cardSlots * (multiplier - 1);
                    score += bonus;
                    /*The algorithm below simulates a time bonus for an un-timed zen mode
                    * this is done to obtain a valid commendation*/
                    double a = roundStartTime,
                            b = slowedDown ? mainGameCoClass.time_allowed * 5 : mainGameCoClass.time_allowed,
                            c = clockBroken ? timeOfBreak : System.currentTimeMillis();
                    double timeUsed = c - a;
                    Log.println(INFO, "Scoring", "Time Used Is = " + timeUsed);
                    Log.println(INFO, "Scoring", "Time allowed Is = " + b);
                    int time_bonus = (int) Math.rint(((b - timeUsed) / b) * Achievements.RETROSPECT_MAX_BASE_SCORE);
                    if(time_bonus <= 0)
                        time_bonus = 1;
                    Log.println(INFO, "Scoring", "Time bonus Is = " + time_bonus);
                    commendPlayer(RETROSPECT, time_bonus);
                }
            }
        }
        if(scoreGrounds == RESET_LEVEL){
            score = 0;
            roundsPlayed = 0;
        }
        if(scoreGrounds == TIME_BONUS || scoreGrounds == MULTIPLIER_PLUS ||
                scoreGrounds == COMBO_LOST) {
            scoreUpdateReason = scoreGrounds;
            scoreUpdateAnimator.start();
        }
        else {
            scoreText.setText(String.format(Locale.ENGLISH, "%d", score));
            if (gameMode == ZEN) {
                int roundsDisplayed = roundsPlayed % 100;
                roundsText.setText(String.format(Locale.ENGLISH, "%d", roundsDisplayed));
            } else if(!(inTutorial[0] || inTutorial[1])){
                int rounds_left = mainGameCoClass.rounds_due - roundsPlayed;
                roundsText.setText(String.format(Locale.ENGLISH, "%d", rounds_left));
            }
        }
    }

    private void prepareScoreAnimator() {

        scoreUpdateAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 1.0f, 1.5f);
        scoreUpdateAnimator.setDuration(100);
        scoreUpdateAnimator.setInterpolator(new AccelerateInterpolator());
        scoreUpdateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = (float) valueAnimator.getAnimatedValue();
                float scale2 = 1 + (((scale - 1.0f)/0.25f) * 0.1f);
                float rotation = 7.5f * (1.5f - scale);
                switch (scoreUpdateReason){
                    case TIME_BONUS:
                        scoreText.setScaleX(scale);
                        scoreText.setScaleY(scale);
                        roundsText.setScaleX(scale2);
                        roundsText.setScaleY(scale2);
                        break;
                    case MULTIPLIER_PLUS:
                        multiplierText.setScaleX(scale);
                        multiplierText.setScaleY(scale);
                        multiplierText.setRotation(rotation);
                        break;
                    case COMBO_LOST:
                        multiplierText.setScaleX(2.0f - scale);
                        multiplierText.setScaleY(2.0f - scale);
                        multiplierText.setRotation(0 - rotation);
                        break;
                }

            }
        });
        scoreUpdateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                multiplierText.setScaleX(1);
                multiplierText.setScaleY(1);
                multiplierText.setRotation(7.5f);
                scoreText.setScaleX(1);
                scoreText.setScaleY(1);
                roundsText.setScaleX(1);
                roundsText.setScaleY(1);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                scoreText.setText(String.format(Locale.ENGLISH, "%d", score));
                if (gameMode == ZEN) {
                    int roundsDisplayed = roundsPlayed % 100;
                    roundsText.setText(String.format(Locale.ENGLISH, "%d", roundsDisplayed));
                } else if(!(inTutorial[0]||inTutorial[1])){
                    int rounds_left = mainGameCoClass.rounds_due - roundsPlayed;
                    roundsText.setText(String.format(Locale.ENGLISH, "%d", rounds_left));
                }
                if(scoreUpdateReason == MULTIPLIER_PLUS || scoreUpdateReason == COMBO_LOST){
                    String m = "X" + multiplier;
                    multiplierText.setText(m);
                }
            }
        });
        scoreUpdateAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scoreUpdateAnimator.setRepeatCount(1);
    }

    private void updateCollectiblesDisplay(){
        Log.println(INFO, "Collectibles", "Display Updated 1");
        coinsAvailable.setText(String.format(Locale.ENGLISH, "%d", coinWallet));
        if(gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT)
            breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", breakPouch));
        else if (gameMode == DON_TAP || zenActiveGameMode == DON_TAP)
            breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", clearPouch));
        solveUnits.setText(String.format(Locale.ENGLISH, "%d", solvePouch));
        slowUnits.setText(String.format(Locale.ENGLISH, "%d", slowPouch));

        Log.println(INFO, "Collectibles", "Display Updated 2");
        if(gameMode == DON_TAP || zenActiveGameMode == DON_TAP) {
            if(clearPouch >= 1) {
                breakClearIcon.setImageResource(R.drawable.ic_clear_free);
                breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", clearPouch));
                ((GradientDrawable)breakClearUnits.getBackground()).setColor(getResources().
                        getColor(R.color.red));
            }
            else {
                breakClearIcon.setImageResource(R.drawable.ic_clear_paid);
                breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", CLEAR_COST));
                ((GradientDrawable)breakClearUnits.getBackground()).setColor(getResources().
                        getColor(R.color.yellow));
            }
        }
        else if(gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT) {
            if(breakPouch >= 1){
                breakClearIcon.setImageResource(R.drawable.ic_break_free);
                breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", breakPouch));
                ((GradientDrawable)breakClearUnits.getBackground()).setColor(getResources().
                        getColor(R.color.red));
            }
            else {
                breakClearIcon.setImageResource(R.drawable.ic_break_paid);
                breakClearUnits.setText(String.format(Locale.ENGLISH, "%d", BREAK_COST));
                ((GradientDrawable)breakClearUnits.getBackground()).setColor(getResources().
                        getColor(R.color.yellow));
            }
        }
        if(solvePouch >= 1) {
            solveIcon.setImageResource(R.drawable.ic_solve_free);
            solveUnits.setText(String.format(Locale.ENGLISH, "%d", solvePouch));
            ((GradientDrawable)solveUnits.getBackground()).setColor(getResources().
                    getColor(R.color.red));
        } else {
            solveIcon.setImageResource(R.drawable.ic_solve_paid);
            solveUnits.setText(String.format(Locale.ENGLISH, "%d", SOLVE_COST));
            ((GradientDrawable)solveUnits.getBackground()).setColor(getResources().
                    getColor(R.color.yellow));
        }
        if(slowPouch >= 1) {
            slowIcon.setImageResource(R.drawable.ic_slow_down_free);
            slowUnits.setText(String.format(Locale.ENGLISH, "%d", slowPouch));
            ((GradientDrawable)slowUnits.getBackground()).setColor(getResources().
                    getColor(R.color.red));
        }
        else {
            slowIcon.setImageResource(R.drawable.ic_slow_down_paid);
            slowUnits.setText(String.format(Locale.ENGLISH, "%d", SLOW_COST));
            ((GradientDrawable)slowUnits.getBackground()).setColor(getResources().
                    getColor(R.color.yellow));
        }
        Log.println(INFO, "Collectibles", "Display Updated");
    }

    private boolean useCollectible(final String selector, final int units) {

        if(hasFreeContinue) {
            hasFreeContinue = false;
            return true;
        }

        int consume_collectible =  0 - units;
        String secondarySelector = selector;//to track if we are deducting coins instead of free collectibles
        if(inTutorial[1]) consume_collectible = 0;

        switch (selector){
            case Progress.COINS:
                if(coinWallet >= units){
                    coinWallet += consume_collectible;
                } else return false;
                break;
            case Progress.BREAK:
                if(breakPouch >= units){
                    breakPouch += consume_collectible;
                } else if(coinWallet >= BREAK_COST){
                    consume_collectible = 0 - BREAK_COST;
                    coinWallet += consume_collectible;
                    secondarySelector = Progress.COINS;
                } else return false;
                break;
            case Progress.CLEAR:
                if(clearPouch >= units){
                    clearPouch += consume_collectible;
                } else if(coinWallet >= CLEAR_COST){
                    consume_collectible = 0 - CLEAR_COST;
                    coinWallet += consume_collectible;
                    secondarySelector = Progress.COINS;
                } else return false;
                break;
            case Progress.SOLVE:
                if(solvePouch >= units){
                    solvePouch += consume_collectible;
                } else if(coinWallet >= SOLVE_COST){
                    consume_collectible = 0 - SOLVE_COST;
                    coinWallet += consume_collectible;
                    secondarySelector = Progress.COINS;
                } else return false;
                break;
            case Progress.SLOW:
                if(slowPouch >= units){
                    slowPouch += consume_collectible;
                } else if(coinWallet >= SLOW_COST){
                    consume_collectible = 0 - SLOW_COST;
                    coinWallet += consume_collectible;
                    secondarySelector = Progress.COINS;
                } else return false;
                break;
        }
        final int tempDelta = consume_collectible;
        final String tempSelector = secondarySelector;
        taskThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgress == null){
                    mProgress = new Progress(ActivityMainGame.this);
                }
                mProgress.updateCollectibleBank(tempSelector, tempDelta);
                if(!selector.equals(Progress.COINS) && !inTutorial[1])//coins stat is not to be updated upon consumption
                    mProgress.updateCollectibleStats(selector, units);
                Log.d("Task1", "Posted");
            }
        });
        Log.println(INFO, "Collectibles", "Display Updated 0");
        updateCollectiblesDisplay();
        return true;
    }

    private void gainCoins() {
        bonusCoins += coinPocket;
        bonusCoinsText.setText(String.format(Locale.ENGLISH, "%d", bonusCoins));
        coinPocket = 0;

        playSoundEffect(GET_COIN);
    }

    private void resetCoins() {
        if(movingCoinsAnim.isRunning())
            movingCoinsAnim.cancel();
        bonusCoins = 0;
        bonusCoinsText.setText(String.format(Locale.ENGLISH, "%d", bonusCoins));
    }

    private boolean purchaseCoins(int intentCost) {
        //purchaseIntent
        return (mProgress.getCollectibleAccount(Progress.COINS) >= intentCost);
    }

    private void give_extra_time() {
        extraCountdowns++;
        timeBarProgress = timeBarProgress + mainGameCoClass.extra_time;
    }

    private void prepare_fail_pop_up(){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popLayout = new RelativeLayout(this);
        View popView = new View(this);
        if (inflater != null)
            popView = inflater.inflate(R.layout.on_fail_popup_window, popLayout);
        watchAdToContinueButton = popView.findViewById(R.id.watch_add);
        buyContinueButton = popView.findViewById(R.id.buy_continue);
        continueCoins = popView.findViewById(R.id.coins_needed);
        ImageView coinsIcon = popView.findViewById(R.id.coins_icon);
        ImageView cancelButton = popView.findViewById(R.id.cancel_pop_up);

        // create the popup window
        Log.println(INFO, "popUp", "backdrop width = " + backDrop.getWidth());
        int popUpWidth = (int) Math.rint(screenHeight * 0.5);
        int popUpHeight = (int) Math.rint(popUpWidth*0.4711);
        int buttonHeight = (int) Math.rint(popUpHeight/3.5);
        int buttonWidth = (int) Math.rint(buttonHeight * 2.208);
        int buttonMargin = (int) Math.rint(popUpWidth * 0.1);
        int buttonMargin2 = (int) Math.rint(popUpWidth * 0.16);
        int buttonMarginTop = (int) Math.rint(popUpHeight-buttonMargin-buttonHeight);
        int coins_icon_height = (int) Math.rint(buttonHeight*0.6);
        int coins_icon_width = (int) Math.rint(coins_icon_height*1.1675);
        int coins_icon_margin_left = (int) Math.rint(coins_icon_width*0.1);
        int cancelDiameter = (int) Math.rint(popUpWidth * 0.1);

        RelativeLayout.LayoutParams watchAdButtonLayout = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
        RelativeLayout.LayoutParams buyContinueButtonParams = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
        RelativeLayout.LayoutParams coinsNeededParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams coinsIconParams = new RelativeLayout.LayoutParams(coins_icon_width, coins_icon_height);
        RelativeLayout.LayoutParams cancelButtonLayout = new RelativeLayout.LayoutParams(cancelDiameter, cancelDiameter);

        watchAdButtonLayout.setMargins(0, buttonMarginTop, buttonMargin2, buttonMargin);
        buyContinueButtonParams.setMargins(buttonMargin2, buttonMarginTop, 0, buttonMargin);
        coinsIconParams.setMargins(coins_icon_margin_left, 0, 0, 0);

        watchAdButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buyContinueButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        coinsIconParams.addRule(RelativeLayout.RIGHT_OF, R.id.coins_needed);
        cancelButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        cancelButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        coinsNeededParams.addRule(RelativeLayout.CENTER_VERTICAL);

        watchAdToContinueButton.setLayoutParams(watchAdButtonLayout);
        buyContinueButton.setLayoutParams(buyContinueButtonParams);
        continueCoins.setLayoutParams(coinsNeededParams);
        coinsIcon.setLayoutParams(coinsIconParams);
        cancelButton.setLayoutParams(cancelButtonLayout);

        continueCoins.setText(String.format(Locale.ENGLISH, "%d", continueCost));
        continueCoins.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight*0.4f);
        continueCoins.setGravity(CENTER);

        popupWindow = new PopupWindow(popView, popUpWidth, popUpHeight, false);

        popView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return true;
            }
        });

        watchAdToContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showRewardedAd()){
                    dismissPopUp();
                }
                else {
                    dismissPopUp();
                    loadRewardedVideoAd();
                    loadingAdBar.setVisibility(View.VISIBLE);
                }
                playSoundEffect(BUTTON_CLICKED);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                killGame();
                int time_used = (int) (levelStopTime - levelStartTime);
                showFinalScore(time_used);
                playSoundEffect(BUTTON_CLICKED);
            }
        });
        buyContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useCollectible(Progress.COINS, continueCost) || hasFreeContinue){
                    updateContinueCost(RESET_ROUND);
                    dismissPopUp();
                    executeDelayedResume();
                } else if (purchaseCoins(continueCost)){
                    v.callOnClick();
                }
                playSoundEffect(BUTTON_CLICKED);
            }
        });

        popupWindow.setAnimationStyle(R.style.center_pop_up_anim);

    }

    private void updateContinueCost(int reason) {
        switch (reason){
            case RESET_ROUND:
                continueCost = continueCost * 2;
                make_view_dormant(watchAdToContinueButton);
                break;
            case RESET_LEVEL:
                continueCost = BASE_CONTINUE_COST;
                break;
        }
        continueCoins.setText(String.format(Locale.ENGLISH, "%d", continueCost));
    }

    private void onFailFreezeAndShowPopupWindow() {
        freezeGame();
        levelStopTime = System.currentTimeMillis();
        make_view_dormant(retryIcon); //to avoid the retry clashing with failure
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mActivity.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                    if (!popupWindow.isShowing()) {
                        int yLocation = (int) Math.rint(screenHeight * 0.425);
                        int xLocation = (int) Math.rint((screenWidth - popupWindow.getWidth())/2.0);
                        Log.println(INFO, "popUp", "Setting animation style: " + R.style.center_pop_up_anim);
                        popupWindow.showAtLocation(backDrop, NO_GRAVITY, xLocation, yLocation);
                    }
                }
                countDownToEnd();
            }
        }, 500);

    }

    private void countDownToEnd() {
        countDownText.setText("");
        countDownText.setVisibility(View.VISIBLE);
        new Thread(){
            private void setNumber(final int number){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setText(String.format(Locale.ENGLISH, "%d", number));
                    }
                });
            }
            private void disableSelf(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setVisibility(View.INVISIBLE);
                    }
                });
            }
            private void positionCount(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownText.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenHeight*0.064f);
                        RelativeLayout.LayoutParams countDownTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        countDownTextParams.setMargins(0, (int) Math.rint(screenHeight * 0.015), 0, 0);
                        countDownTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        countDownTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        countDownText.setLayoutParams(countDownTextParams);
                    }
                });
            }
            private void endGame(){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        killGame();
                        int time_used = (int) (levelStopTime - levelStartTime);
                        showFinalScore(time_used);
                    }
                });
            }
            @Override
            public void run(){
                synchronized (countDownLock) {
                    try {
                        countDownLock.wait(200);//wait for pop up animation to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                make_view_active(retryIcon);
                for(int i = 5; i >= 1; i--){
                    positionCount();
                    setNumber(i);
                    synchronized (countDownLock) {
                        try {
                            countDownLock.wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(gameRunning || !mActivity.hasWindowFocus() || !popupWindow.isShowing()) break;
                    if(i == 1){
                        endGame();
                    }
                }
                disableSelf();
            }
        }.start();
    }

    private void dismissPopUp() {
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        synchronized (countDownLock) {
            countDownLock.notifyAll();
        }
    }

    private void onLostFocusFreezeAndShowPlayIcon() {
        freezeGame();
        if (playButton != null)
            fadeViewForAction(playButton, 0f, 1f, 200, NO_SPECIAL_CASE);
        Log.println(INFO, "trackFreezeGame", "Game Being Frozen...");
        for (int j = 0; j < 9; j++) {
            cardView[j].setVisibility(View.INVISIBLE);
        }
        Log.println(INFO, "trackFreezeGame", "game_state = " + gameRunning);
        Log.println(INFO, "trackFreeze Game", "Play Button Activated");
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setClickable(false);
                fadeViewForAction(playButton, 1f, 0f, 200, HIDE_PLAY_BUTTON_CONTINUE);
            }
        });
    }//**

    private void freezeGame() {
        for (int j = 0; j < cardSlots; j++) {
            make_view_dormant(cardView[j]);
        }
        make_view_dormant(breakClearIcon);
        make_view_dormant(solveIcon);
        make_view_dormant(slowIcon);
    }

    private void make_view_dormant(View view) {
        view.setClickable(false);
        view.setAlpha(0.7f);
    }

    private void make_view_active(final View view) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setClickable(true);
                view.setAlpha(1f);
            }
        });
    }

    private void prepareFadeView() {
        fadeAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 1f, 0f);
        fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator fadeAnimator) {
                float animatedValue = (float) fadeAnimator.getAnimatedValue();
                if(fadeCase == SHOW_STAGE_START_ROUND || fadeCase == SHOW_STAGE_CONTINUE  || fadeCase == ROUND_COMPLETED){
                    for(int i = 0; i < 9; i++) {
                        cardView[i].setAlpha(animatedValue);
                    }
                } else {

                    if(fadeCase == START_RETRO_ZEN) {
                        viewToFade.setScaleX(animatedValue);
                        viewToFade.setScaleY(animatedValue);
                    }
                    else viewToFade.setAlpha(animatedValue);
                    if(viewToFade.equals(playButton) || viewToFade.equals(popLayout)){
                        float alpha2;
                        if(viewToFade.equals(playButton))
                            alpha2 = 0.7f + ((1-animatedValue)*0.3f);
                        else alpha2 = 1.0f;
                        mainGameLayout.setAlpha(alpha2);
                    }
                }
            }
        });
        fadeAnimator.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if (viewToFade != null) {
                    if(viewToFade.getAlpha() <= 0.1f)
                        viewToFade.setVisibility(View.INVISIBLE);
                }
                Message actionMessage = uiHandler.obtainMessage(fadeCase);
                actionMessage.sendToTarget();
            }
            public void onAnimationCancel(Animator animator){viewToFade.setAlpha(1.0f);}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                if(fadeCase == SHOW_STAGE_START_ROUND || fadeCase == SHOW_STAGE_CONTINUE || fadeCase == ROUND_COMPLETED){
                    for(int i = 0; i < 9; i++) {
                        if(fadeCase == SHOW_STAGE_START_ROUND) {
                            if (cardInitialized[i])
                                cardView[i].setImageResource(cardBackResource);
                            else cardView[i].setImageResource(emptySlotResource);
                        }
                        cardView[i].setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);
    }

    private void prepareWrongSelectionAction(){
        wrongSelectionAction = ValueAnimator.ofObject(new FloatEvaluator(), 1.0f, 0.3f);
        wrongSelectionAction.setDuration(200);
        wrongSelectionAction.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator fadeAnimator) {
                float alpha = (float) fadeAnimator.getAnimatedValue();
                if(gameMode == RETROSPECT || zenActiveGameMode == RETROSPECT)
                    cardView[mainGameCoClass.shuffled_slots[failurePosition]].setAlpha(alpha);
                else if(gameMode == DON_TAP || zenActiveGameMode == DON_TAP)
                    cardView[failurePosition].setAlpha(alpha);
            }
        });
        wrongSelectionAction.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){}
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                gameUmpire(WRONG_SELECTION);
            }
        });
        wrongSelectionAction.setRepeatMode(ValueAnimator.REVERSE);
        wrongSelectionAction.setRepeatCount(2);
    }

    private void prepareCardFlip(final int flip_duration){
        cardFlipAnimator = ValueAnimator.ofObject(new FloatEvaluator(), -90, 0);
        cardFlipAnimator.setDuration(flip_duration);
        cardFlipAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float rotation_y = (float)valueAnimator.getAnimatedValue();
                cardView[cardToFlip].setRotationY(rotation_y);
            }
        });
        cardFlipAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                playSoundEffect(CARD_FLIPPED);
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
    }

    private void prepareCardShrink(){
        cardShrinkAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 1.0f, 0f);
        cardShrinkAnimator.setDuration(100);
        cardShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float scale = (float)scaleAnimator.getAnimatedValue();
                cardView[cardToShrink].setScaleX(scale);
                cardView[cardToShrink].setScaleY(scale);
            }
        });
        cardShrinkAnimator.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if (!slotFilled[cardToShrink])
                    cardView[cardToShrink].setImageResource(cardBackResource);
                cardView[cardToShrink].setScaleX(1.0f);
                cardView[cardToShrink].setScaleY(1.0f);
            }
            public void onAnimationCancel(Animator animator){
                if (!slotFilled[cardToShrink])
                    cardView[cardToShrink].setImageResource(cardBackResource);
                cardView[cardToShrink].setScaleX(1.0f);
                cardView[cardToShrink].setScaleY(1.0f);
            }
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){}
        });
    }

    private void prepareCommendationAnimation(){
        commendationAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 1.0f);

        commendationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float scale = (float)scaleAnimator.getAnimatedValue();
                commendationsText.setScaleX(scale);
                commendationsText.setScaleY(scale);
                commendationsText.setAlpha(scale);
            }
        });
        commendationAnimator.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){}
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){commendationAnimator.setInterpolator(new AccelerateInterpolator());}
            public void onAnimationStart(Animator animator){
                commendationsText.setVisibility(View.VISIBLE);
            }
        });
        commendationAnimator.setRepeatMode(ValueAnimator.REVERSE);
        commendationAnimator.setRepeatCount(1);
    }

    private double[] getCoinTrig(){
        final float a = ((View)honeyJar.getParent()).getLeft() + ((View)honeyJar.getParent()).getWidth()/2.0f;
        final float b = movingCoins.getLeft() + movingCoins.getWidth()/2.0f;
        final float c = ((View)honeyJar.getParent()).getTop() + honeyJar.getHeight()/2.0f;
        final float d = backDrop.getTop();
        final float opp = d - c;
        final float adj = a - b;

        final double hyp = Math.hypot(adj, opp);

        double[] vector = {hyp/2.0, Math.atan2(opp, adj)};
        vector[0] = hyp/2.0;

        Log.d("Info", "opp: " + opp);

        return vector;
    }

    double[] trig;
    private void prepareMovingCoinsAnim() {

        movingCoinsAnim = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 3.14159f);
        movingCoinsAnim.setDuration(700);
        movingCoinsAnim.setInterpolator(new AccelerateInterpolator());
        movingCoinsAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float angle = (float) valueAnimator.getAnimatedValue();
                if(trig == null){
                    trig = getCoinTrig();
                }
                float yLocation = (float) (trig[0] * Math.cos(angle));
                float xLocation = (float) ((trig[0]/2.0) * Math.sin(angle)) * (clockWiseCoinMove ? 1 : -1);

                double[] location = getRotatedCoordinates(xLocation, yLocation, trig);

                movingCoins.setTranslationY((float) location[1]);

                movingCoins.setTranslationX((float) location[0]);
            }
        });
        movingCoinsAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                trig = getCoinTrig();
                movingCoins.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                movingCoins.setVisibility(View.INVISIBLE);
                gainCoins();
                if(!inTutorial[0])
                    updateCollectiblesDisplay();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private double[] getRotatedCoordinates(float x, float y, final double trig[]){
        double coordinates[] = new double[2];
        final float pi = 3.1415926f;
        final double projectionAngle = (pi/2.0f) - trig[1];

        final double thetaNot = Math.atan2(y, x);
        final double magnitude = Math.hypot(x, y);
        final double thetaOne = thetaNot + projectionAngle;

        final double x0 = (magnitude * Math.cos(thetaOne));
        final double y0  = (magnitude * Math.sin(thetaOne));

        final double hyp = 2 * trig[0] * Math.sin(projectionAngle/2.0);
        final double phi = (pi/2.0) - (pi - projectionAngle)/2.0;

//        coordinates[0] = x0;
//        coordinates[1] = y0;

        coordinates[0] = x0 + (hyp * Math.cos(phi));
        coordinates[1] = y0 + (hyp * Math.sin(phi));

        return coordinates;
    }

    private void fadeViewForAction(View view, float startAlpha, float endAlpha, int duration, int action){
        if(fadeAnimator.isRunning()) {
            fadeAnimator.end();
            Log.println(INFO, "trackFade", "Running animation canceled = "  + viewToFade);
        }
        if(action == SHOW_BAD_EGG )
            fadeAnimator.setRepeatCount(1);
        else if(action == START_RETRO_ZEN) {
            for (int i = 0; i < 9; i++) { // ensures clear board for a new new mode to start
                cardView[i].setVisibility(View.INVISIBLE);
            }
            fadeAnimator.setRepeatCount(1);
            fadeAnimator.setInterpolator(new OvershootInterpolator());
            commentImage.setImageResource(R.drawable.big_o);
//            commentImage.setTextColor(getResources().getColor(R.color.white));
//            commentImage.setText(R.string.app_name);
        }
        else fadeAnimator.setRepeatCount(0);
        if(view != null)
            view.setVisibility(View.VISIBLE);
        viewToFade = view;
        Log.println(INFO, "trackFade", "New animation = " + viewToFade);
        fadeCase = action;
        fadeAnimator.setDuration(duration);
        fadeAnimator.setObjectValues(startAlpha, endAlpha);
        fadeAnimator.start();
    }

    private void flipCard(int card){
        if(cardFlipAnimator.isRunning()) {
            cardFlipAnimator.end();
        }
        cardFlipAnimator.setDuration(mainGameCoClass.flip_duration);
        cardToFlip = card;
        cardFlipAnimator.start();
    }

    private void shrinkCard(int card){
        if(cardShrinkAnimator.isRunning()) {
            cardShrinkAnimator.end();
        }
        cardShrinkAnimator.setDuration(mainGameCoClass.shrink_duration);
        cardToShrink = card;
        cardShrinkAnimator.start();
    }

    private void commendPlayer(int activeGameMode, int successWeight){
        if(commendationAnimator.isRunning())
            commendationAnimator.end();
        String commendation = "";
        if (activeGameMode == DON_TAP){
            commendationAnimator.setDuration(300);
            if(successWeight >= Achievements.DON_TAP_PLATINUM_BASE_SCORE) {
                commendation = "Ridiculously Rapid!";
                commendationsText.setTextColor(getResources().getColor(R.color.platinum));
                commendationCount+=2;
            }
            else if(successWeight >= Achievements.DON_TAP_GOLD_BASE_SCORE) {
                commendation = "Extremely Fast!";
                commendationsText.setTextColor(getResources().getColor(R.color.gold));
                commendationCount++;
            }
            else if(successWeight >= Achievements.DON_TAP_SILVER_BASE_SCORE) {
                commendation = "Swift Fingers!";
                commendationsText.setTextColor(getResources().getColor(R.color.silver));
            }
            else if(successWeight >= Achievements.DON_TAP_BRONZE_BASE_SCORE) {
                commendation = "Quick!";
                commendationsText.setTextColor(getResources().getColor(R.color.bronze));
            }
            else {
                commendation = "Click Faster!";
                commendationsText.setTextColor(getResources().getColor(R.color.red));
            }
        }
        else if (activeGameMode == RETROSPECT){
            commendationAnimator.setDuration(500);
            if(successWeight >= Achievements.RETROSPECT_PLATINUM_BASE_SCORE) {
                commendationCount = 2 * cardSlots;
                commendation = "Insane!";
                commendationsText.setTextColor(getResources().getColor(R.color.platinum));
            }
            else if(successWeight >= Achievements.RETROSPECT_GOLD_BASE_SCORE) {
                commendationCount = cardSlots;
                commendation = "Blazing!";
                commendationsText.setTextColor(getResources().getColor(R.color.gold));
            }
            else if(successWeight >= Achievements.RETROSPECT_SILVER_BASE_SCORE) {
                commendation = "Speedy!";
                commendationsText.setTextColor(getResources().getColor(R.color.silver));
            }
            else if(successWeight >= Achievements.RETROSPECT_BRONZE_BASE_SCORE) {
                commendation = "Okay!";
                commendationsText.setTextColor(getResources().getColor(R.color.bronze));
            }
            else {
                commendation = "Go Faster!";
                commendationsText.setTextColor(getResources().getColor(R.color.red));
            }
            if(gameMode == ZEN) {
                updateBlitz(successWeight);
            }
        }
        String commend = commendation + "  +" + successWeight;
        commendationsText.setText(commend);

        if(commendationCount >= 2 * cardSlots){
            if(isFreshLevel) coinPocket+=10;
            else coinPocket++;
            commendationCount = 0;
            emptyCoinPocket();
        }

        commendationAnimator.setInterpolator(new OvershootInterpolator());
        commendationAnimator.start();
    }

    private void emptyCoinPocket(){
        clockWiseCoinMove = System.currentTimeMillis() % 2 == 0;
        if (movingCoinsAnim.isRunning())
            movingCoinsAnim.end();
        movingCoinsAnim.start();
    }

    private void startEndGameProcess(final int gameMode){
        final ValueAnimator closingAnimator2 = ValueAnimator.ofObject(new FloatEvaluator(), 0f, 0.8f);
        closingAnimator2.setDuration(2000);
        closingAnimator2.setInterpolator(new OvershootInterpolator());
        int commendationResource = 0;
        long decider = System.currentTimeMillis();
        if (gameMode == DON_TAP){
            if(decider % 7 == 0)
                commendationResource = R.drawable.royal_flush_image;
            else if(decider % 5 == 0)
                commendationResource = R.drawable.clever_image;
            else if(decider % 3 == 0)
                commendationResource = R.drawable.royal_flush_image;
            else if(decider % 2 == 0)
                commendationResource = R.drawable.clever_image;
            else commendationResource = R.drawable.royal_flush_image;
        }
        else if (gameMode == RETROSPECT){
            if(decider % 7 == 0)
                commendationResource = R.drawable.brilliant_image;
            else if(decider % 5 == 0)
                commendationResource = R.drawable.royal_flush_image;
            else if(decider % 3 == 0)
                commendationResource = R.drawable.clever_image;
            else if(decider % 2 == 0)
                commendationResource = R.drawable.brilliant_image;
            else
                commendationResource = R.drawable.clever_image;
        }

        commentImage.setImageResource(commendationResource);

        closingAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float scale = (float)scaleAnimator.getAnimatedValue();
                if (commentImage != null) {
                    commentImage.setScaleX(scale);
                    commentImage.setScaleY(scale);
                }
            }
        });
        closingAnimator2.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                int timeElapsedForLevel = (int) Math.rint(levelStopTime - levelStartTime);
                killGame();
                showAdThenEnd(timeElapsedForLevel);
            }
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                if (commentImage != null) {
                    commentImage.setVisibility(View.VISIBLE);
//                    commentImage.setTextColor(getResources().getColor(R.color.gold));
                    playSoundEffect(LEVEL_COMPLETED);
                }
            }
        });
        closingAnimator2.setRepeatMode(ValueAnimator.REVERSE);
        closingAnimator2.setRepeatCount(1);

        final ValueAnimator closingAnimator1 = ValueAnimator.ofObject(new FloatEvaluator(), 1.0f, 0f);
        closingAnimator1.setDuration(400);
        closingAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float disappear = (float) valueAnimator.getAnimatedValue();
                for(int i = 0; i < 9; i++){
                    cardView[i].setAlpha(disappear);
                }
            }
        });
        closingAnimator1.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                retryIcon.setClickable(false);
                cancelIcon.setClickable(false);
                freezeGame();
                backButtonDormant = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                closingAnimator2.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        closingAnimator1.start();
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                shootFireWorks();
            }
        }, 500);
    }

    private void showAdThenEnd(final int timeUsed){
        if(!NO_ADS) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (interstitialAd.isLoaded() && System.currentTimeMillis() % 2 == 0) {
                        interstitialAd.show();
                    } else {
                        Log.d("Ad", "Interstitial ad skipped ;p ... because ad not loaded? " + !interstitialAd.isLoaded());
                        showFinalScore(timeUsed);
                    }
                }
            });
        } else showFinalScore(timeUsed);
    }

    private void showFinalScore(int time_used){
        Intent scorePageIntent = new Intent(ActivityMainGame.this, ActivityScorePage.class);
        scorePageIntent.putExtra("game mode", gameMode);
        scorePageIntent.putExtra("dif", difficulty);
        scorePageIntent.putExtra("level", level);
        Log.println(INFO, "PuttingScore", "Score = " + score);
        scorePageIntent.putExtra("score", score);
        scorePageIntent.putExtra("rounds", roundsPlayed);
        scorePageIntent.putExtra("best combo", bestCombo);
        scorePageIntent.putExtra("time used", time_used);
        scorePageIntent.putExtra("bonus coins", bonusCoins);
        startActivity(scorePageIntent);
        ActivityMainGame.this.finish();
    }

    private void initializeRewardedVideoAd(){
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
    }

    private void loadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mRewardedVideoAd.loadAd("ca-app-pub-1923630121694917/8273523068", adRequest);
    }

    private boolean showRewardedAd(){
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
            return true;
        }
        return false;
    }

    private void killGame(){
        pause_game(LOST_WINDOW_FOCUS);
        release_heavy_processes();
        dismissPopUp();
        freezeGame();
        retryIcon.setClickable(false);
        cancelIcon.setClickable(false);
        tactileFeedback.cancel();
        if(taskThread != null) {
            taskThread.quit();
            taskThread = null;
        }
        if(gameThread != null){
            gameThread.quit();
            gameThread = null;
        }
        if(timerThread != null){
            timerThread.quit();
            timerThread = null;
        }
        if(timeBarThread != null){
            timeBarThread.quit();
            timeBarThread = null;
        }
        if(tutorialThread != null){
            tutorialThread.quit();
            tutorialThread = null;
        }
    }

    private void resume_needed_processes(){
        if(mProgress == null) mProgress = new Progress(this);
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
                if (gameSounds != null) {
                    gameSounds.release();
                    gameSounds = null;
                }
            }
        }).start();
    }

    private void release_heavy_processes(){
        if(mProgress != null)
            mProgress.finish();
        mProgress = null;
        release_game_sound_pool_late();
        if(backgroundMusic != null) {
            PLAYBACK_PROGRESS = backgroundMusic.getCurrentPosition();
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.println(INFO, "onCreate", "Called onCreate");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_game);
        getScreenSize();
        prepareMessageHandlers();
        if(!gameCreated)
            setGameParameters();
        declareViews();
        resume_needed_processes();
        setGameUiClickables();
        initializeRewardedVideoAd();
        loadRewardedVideoAd();
        if(!gameCreated) gameUmpire(LEVEL_STARTED);
        else pause_game(LOST_WINDOW_FOCUS);
    }


    @Override
    protected void onResume() {
        mRewardedVideoAd.resume(this);
        resume_needed_processes();
        Log.println(INFO, "trackOnStart", "Gained Window Focus");
        Log.println(INFO, "trackOnStart", "Game Running = " + gameRunning);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        release_heavy_processes();
        if((gameRunning || clockBroken) && gameThread != null) {
            Log.println(INFO, "trackOnPause", "Game is Running = " + gameRunning);
            Log.println(INFO, "trackOnPause", "Clock is Broken = " + clockBroken);
            Log.println(INFO, "trackOnPause", "Game Paused");
            pause_game(LOST_WINDOW_FOCUS);
            Log.println(INFO, "trackOnPause", "Game is Running = " + gameRunning);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(!backButtonDormant) {
            if ((gameRunning || clockBroken) && gameThread != null) {
                Log.println(INFO, "trackOnPause", "Game is Running = " + gameRunning);
                Log.println(INFO, "trackOnPause", "Clock is Broken = " + clockBroken);
                Log.println(INFO, "trackOnPause", "Game Paused");
                pause_game(LOST_WINDOW_FOCUS);
                Log.println(INFO, "trackOnPause", "Game is Running = " + gameRunning);
            } else {
                killGame();
                super.onBackPressed();
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
            if(loadingAdBar.getVisibility() == View.VISIBLE){
                Toast.makeText(this, "Sorry, ad could not be loaded.", Toast.LENGTH_SHORT)
                        .show();
                killGame();
                int time_used = (int) (levelStopTime - levelStartTime);
                showFinalScore(time_used);
                loadingAdBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onRewarded(RewardItem reward) {
        hasFreeContinue = true;
//        Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
//                reward.getAmount(), Toast.LENGTH_SHORT).show();
        // Reward the user.
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(this, "onRewardedVideoAdLeftApplication",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        if(!gameRunning && hasFreeContinue) buyContinueButton.performClick();
        else {
            killGame();
            int time_used = (int) (levelStopTime - levelStartTime);
            showFinalScore(time_used);
        }
//        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        if(loadingAdBar.getVisibility() == View.VISIBLE){
            Toast.makeText(this, "Sorry, ad could not be loaded.", Toast.LENGTH_SHORT)
                    .show();
            killGame();
            int time_used = (int) (levelStopTime - levelStartTime);
            showFinalScore(time_used);
            loadingAdBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if(loadingAdBar.getVisibility() == View.VISIBLE){
            loadingAdBar.setVisibility(View.INVISIBLE);
            showRewardedAd();
        }
//        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
//        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
//        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }
}

// TODO: 12/20/2018 enable google search indexing
// TODO: 1/11/2019 Fix ZenMode
// TODO: 1/21/2019 use AudioAttributes class to fully define SoundPool properties
// TODO: 1/22/2019 prevent excessive object creation by reusing existing objects, especially for animations
// TODO: 1/28/2019 reset the resolution of all vectors to the correct values
// TODO: 1/30/2019 do code inspection for the whole project
// TODO: 2/1/2019 fix the appearance of platinum stars on mum's tab
