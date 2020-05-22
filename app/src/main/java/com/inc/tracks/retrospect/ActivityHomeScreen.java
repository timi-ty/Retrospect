package com.inc.tracks.retrospect;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.NonNull;
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
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
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
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PlayGamesAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.inc.tracks.retrospect.util.IabBroadcastReceiver;
import com.inc.tracks.retrospect.util.IabBroadcastReceiver.IabBroadcastListener;
import com.inc.tracks.retrospect.util.IabHelper;
import com.inc.tracks.retrospect.util.IabHelper.IabAsyncInProgressException;
import com.inc.tracks.retrospect.util.IabHelper.OnIabSetupFinishedListener;
import com.inc.tracks.retrospect.util.IabResult;
import com.inc.tracks.retrospect.util.Inventory;
import com.inc.tracks.retrospect.util.Purchase;
import com.inc.tracks.retrospect.util.SkuDetails;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import static android.view.Gravity.NO_GRAVITY;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityHomeScreen extends AppCompatActivity  implements IabBroadcastListener {

    public static boolean NO_ADS = false;
    public static final int BUTTON_CLICKED = 0;
    public static final int RETROSPECT_BOUNCE = 1;
    public static final int RC_SIGN_IN = 9001;
    public static final int RC_UNUSED = 9007;
    public static final int OTHER_ERROR = 907;
    public static final int SIGN_IN_ALERT = 900;
    public static final int ADS_DISABLED_ALERT = 909;
    public static final int COINS_BOUGHT_1K = 911;
    public static final int COINS_BOUGHT_5K = 913;
    public static final int COINS_BOUGHT_15K = 915;
    public static int  PLAYBACK_PROGRESS = 0;
    private final String buyText = "Buy More!";
    private final ArrayList<String> moreItemSkus = new ArrayList<>();
    KeyHelper keyHelper = new KeyHelper();
    private AdView adView;
    Intent activityIntent;
    GoogleSignInClient mGoogleSignInClient;
    PlayersClient playerOneClient;
    private AchievementsClient achievementsClient;
    private LeaderboardsClient leaderboardsClient;
    FirebaseAuth firebaseAuth;
    Progress mProgress;
    ProgressBar loadingBar;
    ImageView startView, difficultyIcon, zenIcon, leaderBoardIcon, achievementsIcon, oTitle, upArrow, downArrow, noAds, settingsIcon,
            coinSign, difficultyText, zenText, playGamesBadge, playerAvatar,  achievementsBadge, leaderBoardsBadge;
    TextView coinsNumber, signInPrompt, playerName;
    PopupWindow storePopUp, alertPopUp;
    View popView, popView1;
    AnimatedVectorDrawableCompat backVectorAnimation;
    Activity mActivity = this;
    HandlerThread homeAnimationsThread;
    ValueAnimator rotateO, pulseMenuItem, slideInCoins, slideOutCoins, flyViewsAnimation,
            popViewsAnimation, moveArrowsAnim, slideOutPlayerLayout,
            fadeArrowsAnim, signInAnim;
    RelativeLayout homeLayout, viewContainer, retrospectTitle, coinsLayout, signInLayout,
            playerOneLayout, alertLayout, popLayout, badgesLayout;
    SoundPool homeSounds;
    MediaPlayer backgroundMusic;
    AudioAttributes audioAttributes;
    Vibrator tactileFeedback;
    VibrationEffect feedBackEffect;
    String price1K = "-", price5K = "-", price15K = "-";
    int coins, screenHeight, screenWidth;
    int[] soundEffect;
    float acceleration, velocity, degrees, startVelocity, sfx_volume, soundtrack_volume;
    double angle;
    boolean slideCoins, animateArrows, startViewPressed, tactile_on, attemptSignIn, mHelperSetup;

    //non-consumable purchase
    static final String SKU_NO_ADS = "no_ads";
    //consumable purchases
    static final String SKU_1000_COINS = "1000_coins";
    static final String SKU_5000_COINS = "5000_coins";
    static final String SKU_15000_COINS = "15000_coins";

    static final int RC_REQUEST = 10001;

    private static final String IAB_TAG = "In-app Billing";
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    FloatEvaluator rotationPhysics = new FloatEvaluator() {
        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            acceleration = (float) (10.0f * Math.sin(angle));
            if(acceleration > 10){
                return (float)endValue;
            }
            velocity = velocity + (acceleration * 0.01f);
            if(velocity > 3){
                return (float)endValue;
            }
            angle = angle + (velocity * 0.01f * 5.0f);
            if(angle > 6.28){
                return (float)endValue;
            }
            degrees = (float) Math.toDegrees(angle);
            return degrees;
        }
    };
    FloatEvaluator floatEvaluator = new FloatEvaluator() {
        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            return super.evaluate(fraction, startValue, endValue);
        }
    };

    private void getScreenSize(int adHeight) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        int realScreenHeight = displayMetrics.heightPixels;
        int computedScreenHeight =  (int) Math.rint(screenWidth * 1280.0/720.0);

        if(computedScreenHeight < realScreenHeight) screenHeight = computedScreenHeight;
        else screenHeight = realScreenHeight;
        keyHelper.takeFour(getResources().getString(R.string.k4));
        Log.d("Old screen height", "" + screenHeight);

        if(viewContainer != null) {
            int bottomMargin = screenHeight - viewContainer.getBottom();
            if ((adHeight*1.4) > bottomMargin)
                screenHeight = (int)Math.rint(screenHeight - (adHeight*1.4) + bottomMargin);
            Log.d("view container margin", "" + bottomMargin);
        }

        Log.d("New Screen Height", "" + screenHeight);
    }
    private void viewDeclarations(boolean initializing){

        ImageView retrTitle = findViewById(R.id.retr_title);
        ImageView spectTitle = findViewById(R.id.spect_title);
        if(initializing) {
            homeLayout = findViewById(R.id.home_screen);
            startView = findViewById(R.id.start_text);
            upArrow = findViewById(R.id.up_arrow);
            downArrow = findViewById(R.id.down_arrow);
            difficultyIcon = findViewById(R.id.difficulty_ic);
            zenIcon = findViewById(R.id.zen_ic);
            leaderBoardIcon = findViewById(R.id.leaderboard_ic);
            achievementsIcon = findViewById(R.id.achievements_ic);
            oTitle = findViewById(R.id.lone_o);
            noAds = findViewById(R.id.no_ads);
            signInLayout = findViewById(R.id.sign_in_layout);
            playGamesBadge = findViewById(R.id.play_controller);
            signInPrompt = findViewById(R.id.sign_in_prompt);
            settingsIcon = findViewById(R.id.settings_ic);
            coinsLayout = findViewById(R.id.coin_layout);
            coinSign = findViewById(R.id.coin_sign);
            coinsNumber = findViewById(R.id.coins_number);
            zenText = findViewById(R.id.zen_text);
            difficultyText = findViewById(R.id.difficulty_text);
            viewContainer = findViewById(R.id.view_container);
            retrospectTitle = findViewById(R.id.retrospect_title);
            playerOneLayout = findViewById(R.id.player_one_layout);
            playerName = findViewById(R.id.player_name);
            playerAvatar = findViewById(R.id.player_avatar);
            badgesLayout = findViewById(R.id.badges_layout);
            achievementsBadge = findViewById(R.id.achievements_badge);
            leaderBoardsBadge = findViewById(R.id.leader_board_badge);
            loadingBar = findViewById(R.id.loading_purchases_bar);
            if (retrospectTitle != null)
                retrospectTitle.setVisibility(View.INVISIBLE);
            difficultyIcon.setVisibility(View.INVISIBLE);
            zenIcon.setVisibility(View.INVISIBLE);
            leaderBoardIcon.setVisibility(View.INVISIBLE);
            achievementsIcon.setVisibility(View.INVISIBLE);
            startView.setVisibility(View.INVISIBLE);
            upArrow.setVisibility(View.INVISIBLE);
            downArrow.setVisibility(View.INVISIBLE);
            difficultyText.setVisibility(View.INVISIBLE);
            zenText.setVisibility(View.INVISIBLE);
            settingsIcon.setVisibility(View.INVISIBLE);
            coinsLayout.setVisibility(View.INVISIBLE);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/4773261755");
            }


            oTitle.setRotation(0.1f - 30f);
        }


        int view_container_side = (int) Math.rint(screenHeight *0.48);
        int view_container_margin_top = (int) Math.rint(screenHeight * 0.07);
        int icon_diameter = (int) Math.rint(screenHeight *0.11132);
        int icon_margin_nudge = (int) Math.rint(screenHeight *0.0104);
        int retrospect_margin_top = (int) Math.rint(screenHeight *0.14);
        int start_height = (int) Math.rint(screenHeight *0.12);
        int start_width = (int) Math.rint(start_height*0.89);
        int arrow_side = (int) Math.rint(screenHeight *0.0323);
        int settings_diameter = (int) Math.rint(screenHeight *0.06094);
        int settings_margin_general = (int) Math.rint(screenHeight *0.015);
        int no_ads_side = (int) Math.rint(screenHeight *0.11094);
        int coins_layout_height = (int) Math.rint(screenHeight *0.06094);
        int coins_layout_margin_top = (int) Math.rint((retrospect_margin_top - coins_layout_height)/2.0);
        int coins_button_diameter = (int) Math.rint(coins_layout_height*0.8);
        int coins_button_margin_left = (int) Math.rint(coins_layout_height*0.1);
        int coins_number_width = (int) Math.rint(screenHeight *0.06094);
        int coins_number_height = (int) Math.rint(coins_number_width*0.4426);
        int coins_number_margin_left = (int) Math.rint(coins_number_height*0.4);
        int coins_number_margin_right = (int) Math.rint(coins_number_height*1.2);
        int retr_height = (int) Math.rint(screenHeight *0.0677);
        int retr_width = (int) Math.rint(retr_height*3.0156);
        int lone_o_height = (int) Math.rint(retr_height);
        int lone_o_width = (int) Math.rint(lone_o_height*0.914);
        int lone_o_margin_left = (int) Math.rint(retr_width * 0.006);
        int lone_o_margin_top = (int) Math.rint(lone_o_height * 0.1);
        int spect_height = (int) Math.rint(screenHeight * 0.1067);
        int spect_width = (int) Math.rint(spect_height*2.11);
        int retrospect_height = (int) Math.rint(spect_height);
        int mode_text_height = (int) Math.rint(screenHeight *0.011);
        int difficulty_text_width = (int) Math.rint(mode_text_height * 6.8);
        int difficulty_text_margin_top = (int) Math.rint(icon_diameter*0.08);
        int zen_text_width = (int) Math.rint(mode_text_height * 2.3);
        int zen_text_margin_bottom = (int) Math.rint(icon_diameter*0.08);
        int spect_title_padding_top = (int) Math.rint(screenHeight *0.007);
        int sign_in_layout_width = (int) Math.rint(screenHeight*0.16);
        int play_games_badge_side = (int) Math.rint(coins_layout_height*0.8);
        int player_layout_height = (int) Math.rint(screenHeight * 0.07);
        int player_layout_width = (int) Math.rint(player_layout_height*3.0);
        int player_layout_margin_top = (int) Math.rint(screenHeight * 0.015);
        int player_content_margin = (int) Math.rint(player_layout_height*0.1);
        int player_avatar_diameter = (int) Math.rint(player_layout_height * 0.8);
        int player_avatar_margin_right = (int) Math.rint(player_layout_height * 0.1);
        int badge_diameter = (int) Math.rint(player_layout_height * 0.8);
        int badge_layout_width = (int) Math.rint(badge_diameter * 3);
        int badge_margin_left = (int) Math.rint((player_layout_width - badge_layout_width)/3.0f);

        RelativeLayout.LayoutParams viewContainerParameters = new RelativeLayout.LayoutParams(view_container_side, view_container_side);
        RelativeLayout.LayoutParams retrospectParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, retrospect_height);
        RelativeLayout.LayoutParams retrParameters = new RelativeLayout.LayoutParams(retr_width, retr_height);
        RelativeLayout.LayoutParams oTitleParameters = new RelativeLayout.LayoutParams(lone_o_width, lone_o_height);
        RelativeLayout.LayoutParams spectParameters = new RelativeLayout.LayoutParams(spect_width, spect_height);
        RelativeLayout.LayoutParams noAdsParameters = new RelativeLayout.LayoutParams(no_ads_side, no_ads_side);
        RelativeLayout.LayoutParams signInLayoutParams = new RelativeLayout.LayoutParams(sign_in_layout_width, coins_layout_height);
        RelativeLayout.LayoutParams signInPromptParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams playGameBadgeParams = new RelativeLayout.LayoutParams(play_games_badge_side, play_games_badge_side);
        RelativeLayout.LayoutParams settingsParameters = new RelativeLayout.LayoutParams(settings_diameter, settings_diameter);
        RelativeLayout.LayoutParams coinsLayoutParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, coins_layout_height);
        RelativeLayout.LayoutParams coinSignParameters = new RelativeLayout.LayoutParams(coins_button_diameter, coins_button_diameter);
        RelativeLayout.LayoutParams coinsNumberParameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams difficultyIconParameters = new RelativeLayout.LayoutParams(icon_diameter, icon_diameter);
        RelativeLayout.LayoutParams zenIconParameters = new RelativeLayout.LayoutParams(icon_diameter, icon_diameter);
        RelativeLayout.LayoutParams achievementsIconParameters = new RelativeLayout.LayoutParams(icon_diameter, icon_diameter);
        RelativeLayout.LayoutParams leaderBoardIconParameters = new RelativeLayout.LayoutParams(icon_diameter, icon_diameter);
        RelativeLayout.LayoutParams difficultyTextParameters = new RelativeLayout.LayoutParams(difficulty_text_width, mode_text_height);
        RelativeLayout.LayoutParams zenTextParameters = new RelativeLayout.LayoutParams(zen_text_width, mode_text_height);
        keyHelper.takeThree(getResources().getString(R.string.k3));
        RelativeLayout.LayoutParams startParameters = new RelativeLayout.LayoutParams(start_width, start_height);
        RelativeLayout.LayoutParams upArrowParameters = new RelativeLayout.LayoutParams(arrow_side, arrow_side);
        RelativeLayout.LayoutParams downArrowParameters = new RelativeLayout.LayoutParams(arrow_side, arrow_side);
        RelativeLayout.LayoutParams playerLayoutParams = new RelativeLayout.LayoutParams(player_layout_width, player_layout_height);
        RelativeLayout.LayoutParams playerAvatarParams = new RelativeLayout.LayoutParams(player_avatar_diameter, player_avatar_diameter);
        RelativeLayout.LayoutParams playerNameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams badgeParams = new RelativeLayout.LayoutParams(badge_layout_width, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams achBadgeParams = new RelativeLayout.LayoutParams(badge_diameter, badge_diameter);
        RelativeLayout.LayoutParams lbBadgeParams = new RelativeLayout.LayoutParams(badge_diameter, badge_diameter);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        viewContainerParameters.setMargins(0, view_container_margin_top, 0, 0);
        retrospectParameters.setMargins(0, retrospect_margin_top, 0, 0);
        oTitleParameters.setMargins(lone_o_margin_left, lone_o_margin_top, 0, 0);
        settingsParameters.setMargins(0, settings_margin_general, settings_margin_general, 0);
        coinsLayoutParameters.setMargins(0, coins_layout_margin_top, 0, 0);
        coinSignParameters.setMargins(coins_button_margin_left, 0, 0, 0);
        playGameBadgeParams.setMargins(0, 0, coins_number_margin_left, 0);
        signInLayoutParams.setMargins(0, coins_number_margin_left*10, 0, 0);
        signInPromptParams.setMargins(coins_number_margin_left*2, 0, coins_number_margin_left, 0);
        coinsNumberParameters.setMargins(coins_number_margin_left, 0, coins_number_margin_right, 0);
        difficultyIconParameters.setMargins(0, icon_margin_nudge, 0, 0);
        zenIconParameters.setMargins(0, 0, 0, icon_margin_nudge);
        achievementsIconParameters.setMargins(icon_margin_nudge, 0, 0, 0);
        leaderBoardIconParameters.setMargins(0, 0, icon_margin_nudge, 0);
        difficultyTextParameters.setMargins(0, difficulty_text_margin_top, 0, 0);
        zenTextParameters.setMargins(0, 0, 0, zen_text_margin_bottom);
        playerLayoutParams.setMargins(0, player_layout_margin_top, 0, 0);
        playerAvatarParams.setMargins(0, 0, player_avatar_margin_right, 0);
        playerNameParams.setMargins(player_content_margin*2, 0, 0, 0);
        badgeParams.setMargins(badge_margin_left, player_content_margin*2, 0, 0);
        achBadgeParams.setMargins(0, 0, 0, 0);
        lbBadgeParams.setMargins(0, 0, player_content_margin*2, 0);

        viewContainerParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewContainerParameters.addRule(RelativeLayout.BELOW, R.id.retrospect_title);
        retrospectParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        retrospectParameters.addRule(RelativeLayout.BELOW, R.id.settings_ic);
        oTitleParameters.addRule(RelativeLayout.RIGHT_OF, R.id.retr_title);
        spectParameters.addRule(RelativeLayout.RIGHT_OF, R.id.lone_o);
        settingsParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        noAdsParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        noAdsParameters.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        signInLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        signInLayoutParams.addRule(RelativeLayout.BELOW, R.id.retrospect_title);
        signInPromptParams.addRule(RelativeLayout.CENTER_VERTICAL);
        signInPromptParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        playGameBadgeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        playGameBadgeParams.addRule(RelativeLayout.RIGHT_OF, R.id.sign_in_prompt);
        coinsLayoutParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        coinsLayoutParameters.addRule(RelativeLayout.BELOW, R.id.settings_ic);
        coinSignParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        coinSignParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        coinsNumberParameters.addRule(RelativeLayout.RIGHT_OF, R.id.coin_sign);
        coinsNumberParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        difficultyIconParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        difficultyIconParameters.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        zenIconParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        zenIconParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        achievementsIconParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        achievementsIconParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leaderBoardIconParameters.addRule(RelativeLayout.CENTER_VERTICAL);
        leaderBoardIconParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        startParameters.addRule(RelativeLayout.CENTER_IN_PARENT);
        difficultyTextParameters.addRule(RelativeLayout.BELOW, R.id.difficulty_ic);
        zenTextParameters.addRule(RelativeLayout.ABOVE, R.id.zen_ic);
        difficultyTextParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        zenTextParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        upArrowParameters.addRule(RelativeLayout.ALIGN_TOP, R.id.start_text);
        downArrowParameters.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.start_text);
        upArrowParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        downArrowParameters.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        playerLayoutParams.addRule(RelativeLayout.BELOW, R.id.retrospect_title);
        playerAvatarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        playerNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        playerAvatarParams.addRule(RelativeLayout.CENTER_VERTICAL);
        playerNameParams.addRule(RelativeLayout.CENTER_VERTICAL);
        badgeParams.addRule(RelativeLayout.BELOW, R.id.player_one_layout);
        achBadgeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        achBadgeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        lbBadgeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lbBadgeParams.addRule(RelativeLayout.CENTER_VERTICAL);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        viewContainer.setLayoutParams(viewContainerParameters);
        retrospectTitle.setLayoutParams(retrospectParameters);
        if (retrTitle != null) {
            retrTitle.setLayoutParams(retrParameters);
        }
        oTitle.setLayoutParams(oTitleParameters);
        if (spectTitle != null) {
            spectTitle.setLayoutParams(spectParameters);
        }
        noAds.setLayoutParams(noAdsParameters);
        signInLayout.setLayoutParams(signInLayoutParams);
        playGamesBadge.setLayoutParams(playGameBadgeParams);
        signInPrompt.setLayoutParams(signInPromptParams);
        settingsIcon.setLayoutParams(settingsParameters);
        coinsLayout.setLayoutParams(coinsLayoutParameters);
        coinSign.setLayoutParams(coinSignParameters);
        coinsNumber.setLayoutParams(coinsNumberParameters);
        difficultyIcon.setLayoutParams(difficultyIconParameters);
        zenIcon.setLayoutParams(zenIconParameters);
        leaderBoardIcon.setLayoutParams(leaderBoardIconParameters);
        achievementsIcon.setLayoutParams(achievementsIconParameters);
        difficultyText.setLayoutParams(difficultyTextParameters);
        zenText.setLayoutParams(zenTextParameters);
        startView.setLayoutParams(startParameters);
        upArrow.setLayoutParams(upArrowParameters);
        downArrow.setLayoutParams(downArrowParameters);
        playerOneLayout.setLayoutParams(playerLayoutParams);
        playerName.setLayoutParams(playerNameParams);
        playerAvatar.setLayoutParams(playerAvatarParams);
        badgesLayout.setLayoutParams(badgeParams);
        achievementsBadge.setLayoutParams(achBadgeParams);
        leaderBoardsBadge.setLayoutParams(lbBadgeParams);

        if(adView.getVisibility() == View.VISIBLE) {
            if (homeLayout.getChildAt(homeLayout.getChildCount() - 1).getId() != R.id.ad_view)
                homeLayout.addView(adView, adParams);
        }


        coinsNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, coins_number_height*0.8f);
        signInPrompt.setTextSize(TypedValue.COMPLEX_UNIT_PX, coins_number_height*0.8f);
        playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, coins_number_height*0.8f);
        playerName.setMaxEms(5);
        coinsNumber.setMaxEms(20);
        playerName.setMaxLines(2);
        coinsNumber.setMaxLines(1);


        float[] corner_array = new float[8];
        corner_array[0] = coinsLayoutParameters.height/2.0f;
        corner_array[1] = coinsLayoutParameters.height/2.0f;
        corner_array[2] = 0;
        corner_array[3] = 0;
        corner_array[4] = 0;
        corner_array[5] = 0;
        corner_array[6] = coinsLayoutParameters.height/2.0f;
        corner_array[7] = coinsLayoutParameters.height/2.0f;
        GradientDrawable coin_layout_back = new GradientDrawable();
        coin_layout_back.setColor(getResources().getColor(R.color.green));
        coin_layout_back.setShape(GradientDrawable.RECTANGLE);
        coin_layout_back.setCornerRadii(corner_array);
        coin_layout_back.setSize(coinsLayoutParameters.width, coinsLayoutParameters.height);

        coinsLayout.setBackground(coin_layout_back);

        float[] corner_array1 = new float[8];
        corner_array1[0] = signInLayoutParams.height/2.0f;
        corner_array1[1] = signInLayoutParams.height/2.0f;
        corner_array1[2] = 0;
        corner_array1[3] = 0;
        corner_array1[4] = 0;
        corner_array1[5] = 0;
        corner_array1[6] = signInLayoutParams.height/2.0f;
        corner_array1[7] = signInLayoutParams.height/2.0f;
        GradientDrawable sign_in_prompt_back = new GradientDrawable();
        sign_in_prompt_back.setColor(getResources().getColor(R.color.green));
        sign_in_prompt_back.setShape(GradientDrawable.RECTANGLE);
        sign_in_prompt_back.setCornerRadii(corner_array1);
        sign_in_prompt_back.setSize(signInLayoutParams.width, signInLayoutParams.height);

        signInLayout.setBackground(sign_in_prompt_back);

        float[] corner_array2 = new float[8];
        corner_array2[0] = 0;
        corner_array2[1] = 0;
        corner_array2[2] = player_layout_height/2.0f;
        corner_array2[3] = player_layout_height/2.0f;
        corner_array2[4] = player_layout_height/2.0f;
        corner_array2[5] = player_layout_height/2.0f;
        corner_array2[6] = 0;
        corner_array2[7] = 0;
        GradientDrawable player_layout_back = new GradientDrawable();
        player_layout_back.setColor(getResources().getColor(R.color.green));
        player_layout_back.setShape(GradientDrawable.RECTANGLE);
        player_layout_back.setCornerRadii(corner_array2);
        player_layout_back.setSize(signInLayoutParams.width, signInLayoutParams.height);

        playerOneLayout.setBackground(player_layout_back);
        achievementsBadge.setBackground(getBadgeBackground());
        leaderBoardsBadge.setBackground(getBadgeBackground());

        if (spectTitle != null)
            spectTitle.setPadding(0, spect_title_padding_top, 0, 0);
        achievementsBadge.setPadding(player_content_margin, player_content_margin, player_content_margin, player_content_margin);
        leaderBoardsBadge.setPadding(player_content_margin, player_content_margin, player_content_margin, player_content_margin);


        if(initializing) {
            if(!NO_ADS) {
                AdRequest adRequest = new AdRequest.Builder().build();
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

        prepareCoinStore(initializing);
        prepare_alert_pop_up(initializing);
    }
    private void initialize_home_screen(){
        Log.d("Initialize Home Screen", "Start Home");
        if (popViewsAnimation == null)
            preparePopViewsIn();
        if (rotateO == null)
            prepareORotation();
        if (pulseMenuItem == null)
            preparePulseMenuItems();
        if (slideInCoins == null)
            prepareSlideInCoins();
        if (slideOutCoins == null)
            prepareSlideOutCoins();
        if (signInAnim == null)
            prepareSignInAnim();
        if (fadeArrowsAnim == null || moveArrowsAnim == null)
            prepareStartArrowAnim();
        if(flyViewsAnimation == null) {
            prepareFlyViewsIn();
            flyViewsAnimation.start();
        }
        else popViewsAnimation.start();

        mProgress = new Progress(this);

        int a = mProgress.getSignInPreference();

        attemptSignIn = a == 1;

        if(a == -1){
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!isSignedIn())
                        showAlert(SIGN_IN_ALERT);
                }
            }, 1500);

        }

        Log.d("Initialize Home Screen", "Done Home");
    }
    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        soundtrack_volume = mProgress.getSoundTrackVolume();
        tactile_on = mProgress.getTactileState() == 1;
        if (homeSounds == null) {
            if(SDK_VERSION >= 21) {
                if(audioAttributes == null) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                }
                homeSounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(2)
                        .build();
            } else homeSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            soundEffect = new int[2];
            soundEffect[0] = homeSounds.load(this, R.raw.menu_click, 1);
            soundEffect[1] = homeSounds.load(this, R.raw.retrospect_drop, 1);
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
    private void playSoundEffect(int playSound){
        long[] timings = new long[6];
        timings[0] = 0;
        timings[2] = 400;
        timings[4] = 200;
        if(homeSounds != null) {
            if (playSound == BUTTON_CLICKED) {
                homeSounds.play(soundEffect[0], sfx_volume, sfx_volume, 1, 0, 1.0f);
            } else if (playSound == RETROSPECT_BOUNCE) {
                timings[1] = 20;
                timings[3] = 10;
                timings[5] = 10;
                homeSounds.play(soundEffect[1], sfx_volume, sfx_volume, 1, 0, 1.0f);

                if(SDK_VERSION >= 26 && tactileFeedback != null && tactile_on){
                    feedBackEffect = VibrationEffect.createWaveform(timings, -1);
                    tactileFeedback.vibrate(feedBackEffect);
                }
                else if (tactileFeedback != null && tactile_on){
                    tactileFeedback.vibrate(timings, -1);
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners(){
        difficultyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundEffect(BUTTON_CLICKED);
                activityIntent = new Intent(ActivityHomeScreen.this, ActivityDifficultyMenu.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });
        zenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundEffect(BUTTON_CLICKED);
                activityIntent = new Intent(ActivityHomeScreen.this, ActivityMainGame.class);
                activityIntent.putExtra("dif", -1);
                activityIntent.putExtra("level", -1);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });
        achievementsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundEffect(BUTTON_CLICKED);
                activityIntent = new Intent(ActivityHomeScreen.this, ActivityAchievements.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });
        achievementsBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(achievementsClient != null) {
                    achievementsClient.getAchievementsIntent()
                            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, RC_UNUSED);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    handleActivityFailure(OTHER_ERROR, e);
                                }
                            });
                } else {
                    achievementsClient = getAchievementsClient(ActivityHomeScreen.this);
                    if(achievementsClient == null) handleActivityFailure(OTHER_ERROR, new Exception());
                    else achievementsBadge.performClick();
                }
            }
        });
        leaderBoardsBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(leaderboardsClient != null) {
                    leaderboardsClient.getAllLeaderboardsIntent()
                            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, RC_UNUSED);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    handleActivityFailure(OTHER_ERROR, e);
                                }
                            });
                } else {
                    leaderboardsClient = getLeaderBoardsClient(ActivityHomeScreen.this);
                    if(leaderboardsClient == null) handleActivityFailure(OTHER_ERROR, new Exception());
                    else leaderBoardsBadge.performClick();
                }
            }
        });
        leaderBoardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSoundEffect(BUTTON_CLICKED);
                if(isSignedIn()) {
                    activityIntent = new Intent(ActivityHomeScreen.this, ActivityLeaderBoard.class);
                    startActivity(activityIntent);
                    overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
                } else handleActivityFailure(SIGN_IN_ALERT, new Exception());
            }
        });
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect(BUTTON_CLICKED);
                activityIntent = new Intent(ActivityHomeScreen.this, ActivitySettings.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.activity_show, R.anim.activity_hide);
            }
        });
        signInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSignedIn()) {
                    playSoundEffect(BUTTON_CLICKED);
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                } else{
                    mGoogleSignInClient.signOut();
                    FirebaseAuth.getInstance().signOut();
                    onDisconnected();
                }
            }
        });
        coinsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSoundEffect(BUTTON_CLICKED);
                showCoinStore();
            }
        });
        noAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseAction(SKU_NO_ADS);
            }
        });
        startView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startViewPressed = (event.getAction() == MotionEvent.ACTION_DOWN && startView.getTranslationY() == 0);
                return false;
            }
        });
        viewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP && startViewPressed) {
                    applyStartPhysics();
                    Log.println(Log.INFO, "Start Motion", "applying physics");
                    startViewPressed = false;
                    return true;
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_MOVE && startViewPressed){
                    startVelocity = getInstantVelocity(event);
                    Log.println(Log.INFO, "Start Motion", "Initial Velocity: " + startVelocity + " pixels/s");
                    float yOff = event.getY() - (viewContainer.getHeight()/2.0f);
                    float depth = 1 - (Math.abs(yOff)/(viewContainer.getHeight()/3.0f));
                    startView.setTranslationY(yOff);
                    startView.setAlpha(depth);
                    startView.setScaleX(depth);
                    startView.setScaleY(depth);
                    return false;
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_CANCEL){
                    startView.setTranslationY(0);
                    startView.setAlpha(1.0f);
                    startView.setScaleX(1);
                    startView.setScaleY(1);
                    return true;
                }
                return false;
            }
        });
        keyHelper.takeOne(getResources().getString(R.string.k1));
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
        difficultyIcon.setOnTouchListener(indicateTouch);
        zenIcon.setOnTouchListener(indicateTouch);
        achievementsIcon.setOnTouchListener(indicateTouch);
        leaderBoardIcon.setOnTouchListener(indicateTouch);
        coinsLayout.setOnTouchListener(indicateTouch);
        settingsIcon.setOnTouchListener(indicateTouch);
//        signInLayout.setOnTouchListener(indicateTouch);
        achievementsBadge.setOnTouchListener(indicateTouch);
        leaderBoardsBadge.setOnTouchListener(indicateTouch);
        noAds.setOnTouchListener(indicateTouch);
    }
    private void prepareORotation(){
        angle = 0.1f;
        rotateO = ValueAnimator.ofObject(rotationPhysics, angle, 360f);
        rotateO.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator rotateAnimator) {

                float rotationAngle = (float) rotateAnimator.getAnimatedValue();
                update_o_on_ui_thread(oTitle, rotationAngle);
            }
        });
        rotateO.setRepeatCount(ValueAnimator.INFINITE);
        rotateO.setRepeatMode(ValueAnimator.REVERSE);
        rotateO.setStartDelay(2100);
    }
    private void update_o_on_ui_thread(final View view, final float rotation){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setRotation(rotation - 30);
            }
        });
    }
    private void preparePulseMenuItems(){
        final float max = 1.05f;
        final float min = 1.0f;
        pulseMenuItem = ValueAnimator.ofObject(floatEvaluator, min, max);
        pulseMenuItem.setDuration(615);
        pulseMenuItem.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float scale = (float) scaleAnimator.getAnimatedValue();
                float floating = 1.0f + ((scale - min) / 4);

                update_view_on_ui_thread(difficultyIcon, 0, scale);
                update_view_on_ui_thread(zenIcon, 0, scale);
                update_view_on_ui_thread(leaderBoardIcon, 0, floating);
                update_view_on_ui_thread(achievementsIcon, 0, floating);
            }
        });
        pulseMenuItem.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){}
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){}
        });
        pulseMenuItem.setRepeatMode(ValueAnimator.REVERSE);
        pulseMenuItem.setRepeatCount(ValueAnimator.INFINITE);
    }
    private void prepareSlideInCoins(){
        slideInCoins = ValueAnimator.ofObject(floatEvaluator);
        slideInCoins.setInterpolator(new OvershootInterpolator());
        slideInCoins.setDuration(500);
        slideInCoins.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator slideAnimator) {
                float slide = (float) slideAnimator.getAnimatedValue();
                if(slideCoins)
                    coinsLayout.setTranslationX(slide);
            }
        });
        slideInCoins.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if(slideCoins) {
                    slideOutCoins.setObjectValues((float) coinsLayout.getWidth() * 0.12f, (float) coinsLayout.getWidth());
                    slideOutCoins.start();
                }
            }
            public void onAnimationCancel(Animator animator){
                coinsLayout.setTranslationX((float) coinsLayout.getWidth() * 0.6f);
            }
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                coinsLayout.setVisibility(View.VISIBLE);

                slideInCoins.setObjectValues((float)coinsLayout.getWidth(), (float)coinsLayout.getWidth()*0.12f);

                coinsLayout.setAlpha(1f);
            }
        });
        slideInCoins.setStartDelay(200);
    }
    private void prepareSlideOutCoins(){
        slideOutCoins = ValueAnimator.ofObject(floatEvaluator);
        slideOutCoins.setInterpolator(new AnticipateOvershootInterpolator());
        slideOutCoins.setDuration(500);
        slideOutCoins.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float slide = (float) scaleAnimator.getAnimatedValue();
                if(slideCoins)
                    coinsLayout.setTranslationX(slide);
            }
        });
        slideOutCoins.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if(slideCoins) {
                    coinsLayout.setAlpha(0f);
                    if (coinsNumber.getText().equals(buyText))
                        coinsNumber.setText(String.format(Locale.ENGLISH, "%d", coins));
                    else
                        coinsNumber.setText(buyText);
                    slideInCoins.setObjectValues((float) coinsLayout.getWidth(), (float) coinsLayout.getWidth() * 0.12f);
                    slideInCoins.start();
                }
            }
            public void onAnimationCancel(Animator animator){
                coinsLayout.setTranslationX((float) coinsLayout.getWidth() * 0.6f);
            }
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){}
        });
        slideOutCoins.setStartDelay(5000);
    }
    private void prepareStartArrowAnim(){
        final float max = 1.0f;
        final float min = 0f;
        moveArrowsAnim = ValueAnimator.ofObject(floatEvaluator, min, max);
        moveArrowsAnim.setDuration(615);
        moveArrowsAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float pulsing = (float) scaleAnimator.getAnimatedValue();
                float arrow_move = upArrow.getHeight() * pulsing;

                upArrow.setAlpha(pulsing);
                downArrow.setAlpha(pulsing);
                upArrow.setTranslationY(-arrow_move);
                downArrow.setTranslationY(arrow_move);
            }
        });
        moveArrowsAnim.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if(animateArrows)
                    fadeArrowsAnim.start();
            }
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upArrow.setVisibility(View.VISIBLE);
                        downArrow.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        fadeArrowsAnim = ValueAnimator.ofObject(floatEvaluator, 1.0f, 0f);
        fadeArrowsAnim.setDuration(615);
        fadeArrowsAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator scaleAnimator) {
                float fade = (float) scaleAnimator.getAnimatedValue();
//                Log.println(Log.INFO, "fadeArrows", "Animation Running");
                upArrow.setAlpha(fade);
                downArrow.setAlpha(fade);
            }
        });
        fadeArrowsAnim.addListener(new ValueAnimator.AnimatorListener(){
            public void onAnimationEnd(Animator animator){
                if(animateArrows)
                    moveArrowsAnim.start();
            }
            public void onAnimationCancel(Animator animator){}
            public void onAnimationRepeat(Animator animator){}
            public void onAnimationStart(Animator animator){ }
        });
    }
    private void prepareFlyViewsIn(){
       flyViewsAnimation = ValueAnimator.ofObject(floatEvaluator, -1.0f, 0f);
       flyViewsAnimation.setDuration(750);
       flyViewsAnimation.setInterpolator(new BounceInterpolator());
       flyViewsAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
           @Override
           public void onAnimationUpdate(ValueAnimator valueAnimator) {
               float bounce = 200 * (float) valueAnimator.getAnimatedValue();
               float appear = 1 + (float) valueAnimator.getAnimatedValue();
               update_view_on_ui_thread(retrospectTitle, bounce, appear);
           }
       });
       flyViewsAnimation.addListener(new Animator.AnimatorListener() {
           @Override
           public void onAnimationStart(Animator animator) {
               retrospectTitle.setVisibility(View.VISIBLE);
               playSoundEffect(RETROSPECT_BOUNCE);
           }

           @Override
           public void onAnimationEnd(Animator animator) {
               popViewsAnimation.start();
           }

           @Override
           public void onAnimationCancel(Animator animator) {

           }

           @Override
           public void onAnimationRepeat(Animator animator) {

           }
       });
       flyViewsAnimation.setStartDelay(1000);
    }
    private void preparePopViewsIn(){
        popViewsAnimation = ValueAnimator.ofObject(floatEvaluator, 0f, 1f);
        popViewsAnimation.setDuration(300);
        popViewsAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float appear = (float) valueAnimator.getAnimatedValue();
                update_view_on_ui_thread(difficultyIcon, -1, appear);
                update_view_on_ui_thread(zenIcon, -1, appear);
                update_view_on_ui_thread(leaderBoardIcon, -1, appear);
                update_view_on_ui_thread(achievementsIcon, -1, appear);
                update_view_on_ui_thread(upArrow, -1, appear);
                update_view_on_ui_thread(downArrow, -1, appear);
                update_view_on_ui_thread(startView, -1, appear);
                update_view_on_ui_thread(settingsIcon, -1, appear);
            }
        });
        popViewsAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                difficultyIcon.setVisibility(View.VISIBLE);
                zenIcon.setVisibility(View.VISIBLE);
                leaderBoardIcon.setVisibility(View.VISIBLE);
                achievementsIcon.setVisibility(View.VISIBLE);
                startView.setVisibility(View.VISIBLE);
                difficultyText.setVisibility(View.VISIBLE);
                zenText.setVisibility(View.VISIBLE);
                settingsIcon.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                pulseMenuItem.start();
                moveArrowsAnim.start();
                fadeArrowsAnim.start();
                slideInCoins.setObjectValues((float)coinsLayout.getWidth(), (float)coinsLayout.getWidth()*0.12f);
                slideInCoins.start();
                Log.println(Log.INFO, "popView", "onEnd Called");
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        popViewsAnimation.setStartDelay(100);
    }
    private void prepareSignInAnim(){
        signInAnim = ValueAnimator.ofObject(floatEvaluator, 1f, 0f);
        signInAnim.setDuration(200);
        signInAnim.setInterpolator(new AnticipateOvershootInterpolator());
        signInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float reAppear = (float) valueAnimator.getAnimatedValue();
                signInLayout.setScaleX(reAppear);
            }
        });
        signInLayout.setPivotX(signInLayout.getLayoutParams().width);
        signInAnim.setRepeatMode(ValueAnimator.REVERSE);
        signInAnim.setRepeatCount(1);

        slideOutPlayerLayout = ValueAnimator.ofObject(floatEvaluator, -1.0f, -0.2f);
        slideOutPlayerLayout.setDuration(1000);
        slideOutPlayerLayout.setInterpolator(new OvershootInterpolator());
        slideOutPlayerLayout.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float slide = (float) animation.getAnimatedValue();
                playerOneLayout.setTranslationY(slide*playerOneLayout.getHeight());
                badgesLayout.setTranslationY(slide*badgesLayout.getHeight());
            }
        });
    }
    private void prepareCoinStore(boolean initializing){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);

        if(initializing) {
            if (popLayout == null)
                popLayout = new RelativeLayout(this);
            popView1 = new View(this);
            if (inflater != null)
                popView1 = inflater.inflate(R.layout.purchase_pop_up, popLayout);
            popLayout.setBackgroundResource(R.drawable.shop_bg);
        }
        RelativeLayout storeLayout = popView1.findViewById(R.id.purchase_pop_up);
        RelativeLayout purchase1Layout = popView1.findViewById(R.id.purchase_1_layout);
        RelativeLayout purchase2Layout = popView1.findViewById(R.id.purchase_2_layout);
        RelativeLayout purchase3Layout = popView1.findViewById(R.id.purchase_3_layout);
        ImageView coins1Image = popView1.findViewById(R.id.coins_1000);
        ImageView coins2Image = popView1.findViewById(R.id.coins_5000);
        ImageView coins3Image = popView1.findViewById(R.id.coins_15000);
        TextView price1Text = popView1.findViewById(R.id.price1);
        TextView price2Text = popView1.findViewById(R.id.price2);
        TextView price3Text = popView1.findViewById(R.id.price3);

        // create the popup window
        int popUpWidth = (int) Math.rint(screenHeight * 0.425568);
        int popUpHeight = (int) Math.rint(popUpWidth * 0.504);
        int purchaseLayoutHeight = (int) Math.rint(popUpHeight*0.8);
        int coins_icon_height = (int) Math.rint(popUpHeight*0.6);
        int coins_icon_width = (int) Math.rint(coins_icon_height*0.92899);
        int price_text_size = (int) Math.rint(popUpWidth * 0.04f);

        RelativeLayout.LayoutParams storeLayoutParams = new RelativeLayout.LayoutParams(popUpWidth, popUpHeight);
        RelativeLayout.LayoutParams coinsImageParams = new RelativeLayout.LayoutParams(coins_icon_width, coins_icon_height);
        RelativeLayout.LayoutParams purchase1Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, purchaseLayoutHeight);
        RelativeLayout.LayoutParams purchase2Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, purchaseLayoutHeight);
        RelativeLayout.LayoutParams purchase3Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, purchaseLayoutHeight);
        RelativeLayout.LayoutParams p1Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams p2Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams p3Params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        coinsImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        coinsImageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        purchase1Params.addRule(RelativeLayout.LEFT_OF, R.id.purchase_2_layout);
        purchase1Params.addRule(RelativeLayout.CENTER_VERTICAL);
        purchase2Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        purchase2Params.addRule(RelativeLayout.CENTER_VERTICAL);
        purchase3Params.addRule(RelativeLayout.RIGHT_OF, R.id.purchase_2_layout);
        purchase3Params.addRule(RelativeLayout.CENTER_VERTICAL);
        p1Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        p1Params.addRule(RelativeLayout.BELOW, R.id.coins_1000);
        p2Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        p2Params.addRule(RelativeLayout.BELOW, R.id.coins_5000);
        p3Params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        p3Params.addRule(RelativeLayout.BELOW, R.id.coins_15000);


        storeLayout.setLayoutParams(storeLayoutParams);
        coins1Image.setLayoutParams(coinsImageParams);
        coins2Image.setLayoutParams(coinsImageParams);
        coins3Image.setLayoutParams(coinsImageParams);
        if (purchase1Layout != null)
            purchase1Layout.setLayoutParams(purchase1Params);
        if (purchase2Layout != null)
            purchase2Layout.setLayoutParams(purchase2Params);
        if (purchase3Layout != null)
            purchase3Layout.setLayoutParams(purchase3Params);
        if (price1Text != null) {
            price1Text.setText(price1K);
            price1Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, price_text_size);
            price1Text.setTextColor(getResources().getColor(R.color.app_text_color));
            price1Text.setLayoutParams(p1Params);
        }
        if (price2Text != null) {
            price2Text.setText(price5K);
            price2Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, price_text_size);
            price2Text.setTextColor(getResources().getColor(R.color.app_text_color));
            price2Text.setLayoutParams(p2Params);
        }
        if (price3Text != null) {
            price3Text.setText(price15K);
            price3Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, price_text_size);
            price3Text.setTextColor(getResources().getColor(R.color.app_text_color));
            price3Text.setLayoutParams(p3Params);
        }


        if(initializing) {
            if(storePopUp == null)
                storePopUp = new PopupWindow(popView1, popUpWidth, popUpHeight, true);

            popView1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.performClick();
                    return true;
                }
            });

            storePopUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    slideCoins = true;
                    slideInCoins.start();
                }
            });

            if (purchase1Layout != null) {
                purchase1Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        purchaseAction(SKU_1000_COINS);
                        playSoundEffect(BUTTON_CLICKED);
                    }
                });
            }

            if (purchase2Layout != null) {
                purchase2Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        purchaseAction(SKU_5000_COINS);
                        playSoundEffect(BUTTON_CLICKED);
                    }
                });
            }

            if (purchase3Layout != null) {
                purchase3Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        purchaseAction(SKU_15000_COINS);
                        playSoundEffect(BUTTON_CLICKED);
                    }
                });
            }

            storePopUp.setAnimationStyle(R.style.purchase_pop_up_anim);
        }
        Log.d("Initialize Home Screen", "Done3");
    }
    private void showCoinStore() {
        RelativeLayout mRL = (RelativeLayout) popLayout.getChildAt(0);
        RelativeLayout item1 = (RelativeLayout) mRL.getChildAt(0);
        TextView item1Price = (TextView) item1.getChildAt(1);
        RelativeLayout item2 = (RelativeLayout) mRL.getChildAt(1);
        TextView item2Price = (TextView) item2.getChildAt(1);
        RelativeLayout item3 = (RelativeLayout) mRL.getChildAt(2);
        TextView item3Price = (TextView) item3.getChildAt(1);
        item1Price.setText(price1K);
        item2Price.setText(price5K);
        item3Price.setText(price15K);
        if(mActivity.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
            if (!storePopUp.isShowing()) {
                slideCoins = false;
                slideInCoins.cancel();
                slideOutCoins.cancel();
                int xLocation = (int) Math.rint((screenWidth - storePopUp.getWidth())/2.0);
                storePopUp.showAtLocation(coinSign, NO_GRAVITY, xLocation, 0);
            }
        }
    }
    private void updateSignInInfo(String prompt, String player_name, Uri player_avatar){
        signInAnim.start();
        signInPrompt.setText(prompt);
        playGamesBadge.setImageResource(R.drawable.ic_play_games_badge_white);
        playerName.setText(player_name);

        ImageManager avatarManager = ImageManager.create(this);
        ImageManager.OnImageLoadedListener imageLoadedListener = new ImageManager.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(Uri uri, Drawable drawable, boolean b) {
                if(b) {
                    BitmapDrawable bd;
                    bd = (BitmapDrawable) drawable;
                    playerAvatar.setImageBitmap(getCroppedAvatarBitmap(bd.getBitmap()));
                }
                else playerAvatar.setImageDrawable(drawable);
            }
        };
        avatarManager.loadImage(imageLoadedListener, player_avatar, R.drawable.big_o);

        slideOutPlayerLayout.start();
    }
    private void updateSignInInfo(){
        if(!isSignedIn()){
            String prompt = getResources().getString(R.string.sign_in);
            String displayName = "Hey, \nStranger";
            signInAnim.start();
            signInPrompt.setText(prompt);
            playGamesBadge.setImageResource(R.drawable.ic_play_games_badge_white);
            playerName.setText(displayName);
            playerAvatar.setImageResource(R.drawable.big_o);
            slideOutPlayerLayout.start();
        }
    }
    private float getInstantVelocity(MotionEvent vEvent){
        final int hSize = vEvent.getHistorySize();
        Log.println(Log.INFO, "Start Motion", "History Size: " + hSize);
        if(hSize < 2) return (vEvent.getY() > 0) ? 200 : -200;
        else{
            return ((vEvent.getHistoricalY(hSize-1) -
                    vEvent.getHistoricalY(hSize-2)) > 0) ? 300 : -300;
        }
    }
    private Bitmap getCroppedAvatarBitmap(Bitmap normalBitmap){
        Bitmap croppedBitmap = Bitmap.createBitmap(normalBitmap.getWidth(),
                normalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, normalBitmap.getWidth(),
                normalBitmap.getHeight());
//        final RectF rectF = new RectF(rect);
//        final float roundPx = (int) Math.rint(screenHeight * 0.0175);


        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(normalBitmap.getWidth() / 2.0f, normalBitmap.getHeight() / 2.0f,
                normalBitmap.getWidth() / 2.0f, paint);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(normalBitmap, rect, rect, paint);

        return croppedBitmap;
    }
    private void applyStartPhysics(){
        final Handler mHandler = new Handler(getMainLooper());
        final float k = 50f;
        final long interval = 30;
        new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 100; i++){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            float dt = interval/1000.0f;
                            float e = startView.getTranslationY();
                            float elasticForce = k * e;
                            float acceleration = elasticForce/1.5f;
                            float displacement =  (startVelocity != 0) ? startVelocity * dt : -e;
                            float alpha = (startVelocity != 0) ? 1 - (Math.abs(e)/(viewContainer.getHeight()/3.0f)) : 1.0f;
                            startView.setTranslationY(e + displacement);
                            startView.setAlpha(alpha);
                            startView.setScaleX(alpha);
                            startView.setScaleY(alpha);
                            /*This either refreshes the differential motion or brings it to a halt at the floor*/
                            startVelocity = ((Math.abs(e) < (viewContainer.getHeight()/20.0)) && (acceleration * velocity) < 0) ?
                                    0 : startVelocity - (acceleration * dt);
                        }
                    });
                    try{ Thread.sleep(40); }
                    catch (InterruptedException e){e.printStackTrace();}
                    if (startView.getTranslationY() < -(viewContainer.getHeight()/10)) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                startView.setTranslationY(0);
                                startView.setAlpha(1.0f);
                                startView.setScaleX(1);
                                startView.setScaleY(1);
                                difficultyIcon.callOnClick();

                            }
                        });
                        break;
                    }
                    else if (startView.getTranslationY() > (viewContainer.getHeight()/10)) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                startView.setTranslationY(0);
                                startView.setAlpha(1.0f);
                                startView.setScaleX(1);
                                startView.setScaleY(1);
                                zenIcon.callOnClick();
                            }
                        });
                        break;
                    }
                    if(startView.getTranslationY() == 0) break;
                }
            }
        }.start();
    }
    private void update_view_on_ui_thread(final View view, final float attr1, final float attr2){
        if(attr1 != -1)
            view.setTranslationY(attr1);
        if(attr2 != -1) {
            view.setScaleY(attr2);
            view.setScaleX(attr2);
        }
    }
    private void updateCoinsDisplay(){
        coins = mProgress.getCollectibleAccount(Progress.COINS);
        coinsNumber.setText(String.format(Locale.ENGLISH, "%d", coins));
    }
    private void getSinInClient(){
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        .requestServerAuthCode(getResources().getString(R.string.default_web_client_id))
                        .build());

        firebaseAuth = FirebaseAuth.getInstance();
        keyHelper.takeTwo(getResources().getString(R.string.k2));
    }
    private void firebaseAuthWithPlayGames(final GoogleSignInAccount acct) {

        Log.d("FirebasePlusGames", "firebaseAuthWithPlayGames:" + acct.getId());

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = PlayGamesAuthProvider.getCredential(acct.getServerAuthCode());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FirebasePlusGames", "signInWithCredential:success");
                            onConnected(acct);
                            loadUnclaimedPayloads();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FirebasePlusGames", "signInWithCredential:failure", task.getException());
                            onDisconnected();
                        }

                        // ...
                    }
                });
    }
    private void signInSilently() {
        Log.println(Log.INFO, "Sign In", "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.println(Log.INFO, "Sign In", "signInSilently(): success");
                            firebaseAuthWithPlayGames(task.getResult());
                        } else {
                            Log.println(Log.INFO, "Sign In", "signInSilently(): failure " + task.getException());
                            onDisconnected();
                        }
                    }
                });
    }
    Runnable cleanFirestore;
    private void cleanFirestore(){
        if(cleanFirestore != null){
            cleanFirestore.run();
        }
    }
    private void cleanFirestore(String payload){
        if(userId() == null || payload.equals("")) return;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        final DocumentReference docRef = firestore.collection("players")
                .document(userId()).collection("payloads")
                .document(payload);

        try{
            docRef.delete();
        }
        catch (IllegalArgumentException iae){
            iae.printStackTrace();
        }

        unclaimedPayloads.remove(payload);
    }
    private void purchaseAction(final String SKU){
        Log.d(IAB_TAG, "Purchase button clicked.");

        // launch the honey purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        setWaitScreen(true);
        Log.d(IAB_TAG, "Launching purchase flow for honey.");

        /*Below is the process for generating the purchase payload*/
        SecureRandom ran = new SecureRandom();
        char[] keyLib = ("1234567890-=!@#$%^&*()_+qwertyuiop[]QWERTYUIOP{}|asdfghjkl;'ASDFGH" +
                "JKL:zxcvbnm,.ZXCVBNM<>?`~").toCharArray();
        if(isSignedIn()) {
            String s1 = userId();
            String s2 = (String.format(Locale.ENGLISH, "%d", System.currentTimeMillis()));
            String s3 = s1 + s2 + SKU;
            for (int i = 0; i < 128; i++) {
                s3 = s3.concat((String.format(Locale.ENGLISH, "%s",
                        keyLib[ran.nextInt(keyLib.length)])));
            }
            char[] temp = s3.toCharArray();

            ArrayList<Integer> reorder = new ArrayList<>();
            String payload = "";

            for (int i = 0; i < s3.length(); i++) {
                reorder.add(i);
            }

            Collections.shuffle(reorder);

            for (int index : reorder) {
                payload = payload.concat((String.format(Locale.ENGLISH, "%s", temp[index])));
            }

            payload = "p" + payload;


            /*payload is now fully generated and purchase will be launched upon verified upload*/

            final String fPayload = payload;
            OnSuccessListener<Void> payloadUploadSuccess = new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(mHelperSetup) {
                        try {
                            mHelper.launchPurchaseFlow(ActivityHomeScreen.this, SKU, RC_REQUEST,
                                    mPurchaseFinishedListener, fPayload);
                        } catch (IabAsyncInProgressException e) {
                            complain("Error launching purchase flow. Another async operation in progress.");
                            new Handler(getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    handleActivityFailure(OTHER_ERROR, new Exception());
                                }
                            }, 100);
                            setWaitScreen(false);
                        }
                    }
                    else {
                        complain("Can't launch purchase flow because IabHelper is not setup");
                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handleActivityFailure(OTHER_ERROR, new Exception());
                            }
                        }, 100);
                        setWaitScreen(false);
                        cleanFirestore();
                    }
                    if(storePopUp.isShowing())
                        storePopUp.dismiss();
                }
            };

            OnFailureListener payloadUploadFail = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complain("Error launching purchase flow. Could not dump payload.");
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleActivityFailure(OTHER_ERROR, new Exception());
                        }
                    }, 100);
                    setWaitScreen(false);
                }
            };

            HashMap<String, String> payloadMap = new HashMap<>();
            Timestamp timestamp = Timestamp.now();
            Date date = timestamp.toDate();
            payloadMap.put("userId", s1);
            payloadMap.put("SKU", SKU);
            payloadMap.put("payload", payload);
            payloadMap.put("timeOfPurchase", date.toString());

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            final DocumentReference docRef = firestore.collection("players")
                    .document(s1).collection("payloads")
                    .document(payload);
            docRef.set(payloadMap)
                    .addOnSuccessListener(payloadUploadSuccess)
                    .addOnFailureListener(payloadUploadFail);

            cleanFirestore = new Runnable() {
                @Override
                public void run() {
                    docRef.delete();
                }
            };
        }
        else{
            complain("Error launching purchase flow. Could not dump payload.");
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleActivityFailure(OTHER_ERROR, new Exception());
                }
            }, 100);
            setWaitScreen(false);
        }
    }
    private void provisionPurchase(int units){
        if(mProgress == null) mProgress = new Progress(this);
        mProgress.updateCollectibleBank(Progress.COINS, units);
        coins = mProgress.getCollectibleAccount(Progress.COINS);
        coinsNumber.setText(String.format(Locale.ENGLISH, "%d", coins));
    }
    private AchievementsClient getAchievementsClient(Context context){
        GoogleSignInAccount achievementsAccount =
                GoogleSignIn.getLastSignedInAccount(context);
        AchievementsClient achievementsClient1 = null;
        if(achievementsAccount != null)
            achievementsClient1 = Games.getAchievementsClient(context, achievementsAccount);
        return achievementsClient1;
    }
    private LeaderboardsClient getLeaderBoardsClient(Context context){
        GoogleSignInAccount leaderBoardsAccount =
                GoogleSignIn.getLastSignedInAccount(context);
        LeaderboardsClient leaderBoardsClient1 = null;
        if(leaderBoardsAccount != null)
            leaderBoardsClient1 = Games.getLeaderboardsClient(context, leaderBoardsAccount);
        return leaderBoardsClient1;
    }
    private void prepare_alert_pop_up(boolean initializing){
        // inflate the layout of the popup window

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        if(initializing) {
            if (alertLayout == null)
                alertLayout = new RelativeLayout(this);
            if(popView == null)
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
                    homeLayout.setAlpha(1.0f);
                    mProgress.saveSignInPreference(attemptSignIn);
                }
            });

            alertLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertPopUp.dismiss();
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
        Log.d("Initialize Home Screen", "Done4");
    }
    private void showAlert(int alertType){

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
            case ADS_DISABLED_ALERT:
                backgroundResource = R.drawable.ad_sale_alert;
                break;
            case COINS_BOUGHT_1K:
                backgroundResource = R.drawable.sale_1_alert;
                break;
            case COINS_BOUGHT_5K:
                backgroundResource = R.drawable.sale_2_alert;
                break;
            case COINS_BOUGHT_15K:
                backgroundResource = R.drawable.sale_3_alert;
                break;
            default:
                return;
        }

        if(ActivityHomeScreen.this.hasWindowFocus()) {
            int popUpWidth = (int) Math.rint(screenHeight * 0.5);
            int yLocation = (int) Math.rint(screenHeight * 0.45);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            if (ActivityHomeScreen.this.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                if (!alertPopUp.isShowing()) {
                    alertLayout.setBackground(getResources().getDrawable(backgroundResource));
                    alertPopUp.showAtLocation(homeLayout, NO_GRAVITY, xLocation, yLocation);
                    homeLayout.setAlpha(0.7f);
                }
            }
        }
    }
    private Drawable getBadgeBackground(){
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(getResources().getColor(R.color.white));
        backgroundDrawable.setShape(GradientDrawable.OVAL);
        backgroundDrawable.setSize(0, 0);

        return backgroundDrawable;
    }
    private void handleActivityFailure(int alertType, Exception e){
        showAlert(alertType);
        e.printStackTrace();
    }
    private String userId(){
        if(isSignedIn())
            return firebaseAuth.getCurrentUser().getUid();
        else return null;
    }
    private boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    private void createIabHelperAndRegisterReceiver(){
        Log.d(IAB_TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, keyHelper.getOne() + keyHelper.getTwo()
                + keyHelper.getThree() + keyHelper.getFour());

        moreItemSkus.add(SKU_1000_COINS);
        moreItemSkus.add(SKU_5000_COINS);
        moreItemSkus.add(SKU_15000_COINS);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(IAB_TAG, "Starting setup.");
        mHelperSetup = false;
        setWaitScreen(true);
        mHelper.startSetup(new OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                setWaitScreen(false);
                Log.d(IAB_TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(ActivityHomeScreen.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                mHelperSetup = true;
                Log.d(IAB_TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(true, moreItemSkus, null,
                            mGotInventoryListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }
    private void complain(String message) {
        Log.e(IAB_TAG, "**** Retrospect Error: " + message);
        showAlert(-1);
    }
    private void setWaitScreen(boolean waitScreen){
        loadingBar.setVisibility(waitScreen ? View.VISIBLE : View.INVISIBLE);
        homeLayout.setAlpha(waitScreen ? 0.7f : 1.0f);
    }
    private void disableAds(boolean disable){
        noAds.setVisibility(disable ? View.INVISIBLE : View.VISIBLE);
        adView.setVisibility(disable ? View.INVISIBLE : View.VISIBLE);
        if(disable) {
            getScreenSize(0);
            viewDeclarations(false);
        }
    }
    private void release_heavy_processes(){
        if(homeAnimationsThread != null) {
            new Handler(homeAnimationsThread.getLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(rotateO != null)
                        rotateO.cancel();
                    if(backVectorAnimation != null)
                        backVectorAnimation.stop();
                }
            });
        }
        slideCoins = false;
        animateArrows = false;
        if(flyViewsAnimation != null)
            flyViewsAnimation.cancel();
        if(popViewsAnimation != null)
            popViewsAnimation.cancel();
        if(pulseMenuItem != null)
            pulseMenuItem.cancel();
        if(slideInCoins != null)
            slideInCoins.cancel();
        if(storePopUp != null)
            storePopUp.dismiss();
        if(backgroundMusic != null) {
            PLAYBACK_PROGRESS = backgroundMusic.getCurrentPosition();
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
        mProgress.finish();
        mProgress = null;
        release_other_resources_late();
    }
    private void release_other_resources_late(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                if (homeSounds != null && !mActivity.hasWindowFocus()) {
                    homeSounds.release();
                    homeSounds = null;
                }
                if (homeAnimationsThread != null && !mActivity.hasWindowFocus()) {
                    homeAnimationsThread.quit();
                    homeAnimationsThread = null;
                }
            }
        }).start();
    }
    private void resume_needed_processes(){
        initialize_home_screen();
        if(homeAnimationsThread == null){
            homeAnimationsThread = new HandlerThread("pendulumPhysics");
            if(!homeAnimationsThread.isAlive()) {
                homeAnimationsThread.start();
            }
        }
        new Handler(homeAnimationsThread.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(rotateO != null && !rotateO.isRunning()) {
                    rotateO.setStartDelay(100);
                    rotateO.start();
                }
                if(backVectorAnimation == null){
                    ImageView backVectorView = findViewById(R.id.back_vector);
                    RelativeLayout.LayoutParams backVectorViewParameters =
                            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    if (backVectorView != null) {
                        backVectorView.setLayoutParams(backVectorViewParameters);
                        backVectorAnimation = AnimatedVectorDrawableCompat.create(
                                ActivityHomeScreen.this, R.drawable.animate_snow_flake_vector);
                        backVectorView.setImageDrawable(backVectorAnimation);
                        backVectorView.setRotation(9.7f);
                    }
                }
                if(backVectorAnimation != null && !backVectorAnimation.isRunning())
                    backVectorAnimation.start();
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        declareGameSounds();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
        slideCoins = true;
        animateArrows = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.home_screen);
        getScreenSize(0);
        viewDeclarations(true);
        getSinInClient();
        setClickListeners();
        createIabHelperAndRegisterReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        resume_needed_processes();
        if(attemptSignIn) signInSilently();
        else if(!isSignedIn()) onDisconnected();
        else updateSignInInfo();
        mProgress.saveSignInPreference(attemptSignIn);
        updateCoinsDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
        release_heavy_processes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, intent)) {
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(intent);

                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithPlayGames(account);
                } catch (ApiException apiException) {
                    onDisconnected();
                    handleActivityFailure(OTHER_ERROR, apiException);
                }
            }
            super.onActivityResult(requestCode, resultCode, intent);
        }
        else {
            Log.d(IAB_TAG, "onActivityResult handled by IABUtil.");
        }
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {

        attemptSignIn = true;
        playerOneClient = Games.getPlayersClient(this, googleSignInAccount);

        playerOneClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        String prompt;
                        Uri avatarUri;
                        if (task.isSuccessful() && task.getResult() != null) {
                            displayName = "Hey, \n" + task.getResult().getDisplayName();
//                            prompt = getResources().getString(R.string.sign_out);
                            prompt = "Sign out";
                            avatarUri = task.getResult().getIconImageUri();
                        } else {
                            prompt = getResources().getString(R.string.sign_in);
                            avatarUri = null;
//                            displayName = "Hey, " + getResources().getString(R.string.stranger);
                            displayName = "Hey, \nStranger";
                        }
                        updateSignInInfo(prompt, displayName, avatarUri);
                    }
                });

        achievementsClient = getAchievementsClient(this);
        leaderboardsClient = getLeaderBoardsClient(this);
    }

    private void onDisconnected() {
        String prompt = getResources().getString(R.string.sign_in);
        String displayName = "Hey, \nStranger";
        updateSignInInfo(prompt, displayName, null);
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(IAB_TAG, "Query inventory finished.");


            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(IAB_TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase noAdsPurchase = inventory.getPurchase(SKU_NO_ADS);
            NO_ADS = (noAdsPurchase != null && verifyDeveloperPayload(noAdsPurchase));
            disableAds(NO_ADS);
            Log.d(IAB_TAG, "User is " + (NO_ADS ? "PREMIUM" : "NOT PREMIUM"));

//            Purchase noAds = inventory.getPurchase(SKU_NO_ADS);
//            if (noAds != null && verifyDeveloperPayload(noAds)) {
//                Log.d(IAB_TAG, "We have honey. Consuming it.");
//                try {
//                    mHelper.consumeAsync(inventory.getPurchase(SKU_NO_ADS), mConsumeFinishedListener);
//                    NO_ADS = false;
//                    disableAds(false);
//                } catch (IabAsyncInProgressException e) {
//                    complain("Error consuming honey. Another async operation in progress.");
//                }
//                return;
//            }

            // Check honey combs cart -- if we own honey combs, we should fill up the jar
            SkuDetails sku1K = inventory.getSkuDetails(SKU_1000_COINS);
            if(sku1K != null)
                price1K = sku1K.getPrice();
            Purchase h1000Purchase = inventory.getPurchase(SKU_1000_COINS);
            if (h1000Purchase != null && verifyDeveloperPayload(h1000Purchase)) {
                Log.d(IAB_TAG, "We have honey. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_1000_COINS), mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming honey. Another async operation in progress.");
                }
                return;
            }

            SkuDetails sku5K = inventory.getSkuDetails(SKU_5000_COINS);
            if(sku5K != null)
                price5K = sku5K.getPrice();
            Purchase h5000Purchase = inventory.getPurchase(SKU_5000_COINS);
            if (h5000Purchase != null && verifyDeveloperPayload(h5000Purchase)) {
                Log.d(IAB_TAG, "We have honey. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_5000_COINS), mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming honey. Another async operation in progress.");
                }
                return;
            }

            SkuDetails sku15K = inventory.getSkuDetails(SKU_15000_COINS);
            if(sku15K != null)
                price15K = sku15K.getPrice();
            Purchase h15000Purchase = inventory.getPurchase(SKU_15000_COINS);
            if (h15000Purchase != null && verifyDeveloperPayload(h15000Purchase)) {
                Log.d(IAB_TAG, "We have honey. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_15000_COINS), mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming honey. Another async operation in progress.");
                }
                return;
            }

            setWaitScreen(false);
            Log.d(IAB_TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(IAB_TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            setWaitScreen(false);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic
                Log.d(IAB_TAG, "Consumption successful. Provisioning.");
                int units = 0;
                switch (purchase.getSku()){
                    case SKU_1000_COINS:
                        units = 1000;
                        break;
                    case SKU_5000_COINS:
                        units = 5000;
                        break;
                    case SKU_15000_COINS:
                        units = 15000;
                        break;
                }
                provisionPurchase(units);
            }
            else {
                complain("Error while consuming: " + result);
            }

            Log.d(IAB_TAG, "End consumption flow.");
        }
    };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(IAB_TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            setWaitScreen(false);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                cleanFirestore();
                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }
//
            Log.d(IAB_TAG, "Purchase successful.");
//
            if (!purchase.getSku().equals(SKU_NO_ADS)) {
                switch (purchase.getSku()){
                    case SKU_1000_COINS:
                        showAlert(COINS_BOUGHT_1K);
                        Log.d(IAB_TAG, "Purchase is 1000 coins. Starting coin consumption.");
                        break;
                    case SKU_5000_COINS:
                        showAlert(COINS_BOUGHT_5K);
                        Log.d(IAB_TAG, "Purchase is 5000 coins. Starting coin consumption.");
                        break;
                    case SKU_15000_COINS:
                        showAlert(COINS_BOUGHT_15K);
                        Log.d(IAB_TAG, "Purchase is 15000 coins. Starting coin consumption.");
                        break;
                }
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming coins. Another async operation in progress.");
                    setWaitScreen(false);
                }
            }
            else if (purchase.getSku().equals(SKU_NO_ADS)) {
                // bought the premium upgrade!
                Log.d(IAB_TAG, "Purchase is premium upgrade. Congratulating user.");
                NO_ADS = true;
                disableAds(true);
                showAlert(ADS_DISABLED_ALERT);
            }
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(IAB_TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(true, moreItemSkus, null,
                    mGotInventoryListener);
        } catch (IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    boolean verifyDeveloperPayload(Purchase p) {
        if(p == null) return false;
        if(p.getDeveloperPayload() == null) return false;

        String payload = p.getDeveloperPayload();

        boolean result = unclaimedPayloads.contains(payload);

        cleanFirestore(payload);

        return result;
    }
    ArrayList<String> unclaimedPayloads = new ArrayList<>();
    private void loadUnclaimedPayloads(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if(isSignedIn()) {
            String s1 = userId();

            CollectionReference colRef = firestore.collection("players")
                    .document(s1).collection("payloads");

            colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> payloads =  queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot payload:
                         payloads) {
                        if(!unclaimedPayloads.contains(payload.getString("payload"))) {
                            unclaimedPayloads.add(payload.getString("payload"));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        adView.destroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(IAB_TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }

        super.onDestroy();
    }
}



// TODO: 1/15/2019 run all animations on a parallel thread
// TODO: 1/15/2019 clean up 