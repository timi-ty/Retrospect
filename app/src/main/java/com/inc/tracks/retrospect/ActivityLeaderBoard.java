package com.inc.tracks.retrospect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT;
import static android.view.Gravity.NO_GRAVITY;
import static com.inc.tracks.retrospect.ActivityHomeScreen.NO_ADS;
import static com.inc.tracks.retrospect.ActivityHomeScreen.PLAYBACK_PROGRESS;
import static com.inc.tracks.retrospect.ActivitySplashScreen.SDK_VERSION;

public class ActivityLeaderBoard extends AppCompatActivity {

    public static final int BUTTON_CLICKED = 0;
    private final Object loadLock = new Object();
    private int screenWidth, screenHeight, soundEffect;
    private float sfx_volume;
    private AdView adView;
    private PopupWindow alertPopUp;
    private RelativeLayout alertLayout;
    private View popView;
    private HandlerThread loaderThread;
    private Activity mActivity = this;
    private Progress mProgress;
    private ListView leaderBoardListView;
    private RelativeLayout leaderBoardLayout;
    private ProgressBar loadingBar;
    private AnimatedVectorDrawableCompat backVectorAnimation;
    private Cursor highScoreCursor;
    private LeaderBoardEntry[] leaderBoardEntries;
    private SoundPool soundPool;
    private MediaPlayer backgroundMusic;
    private AudioAttributes audioAttributes;
    private LeaderboardsClient leaderboardsClient;
    private ImageManager avatarManager = ImageManager.create(ActivityLeaderBoard.this);

    private boolean topScoresLoaded;
    private boolean centeredScoresLoaded;
    private class Loader implements Runnable {
        @Override
        public void run() {
            boolean loaded = topScoresLoaded && centeredScoresLoaded;
            int i = 0;
            while (!loaded){
                loadLeaderBoardScores();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaderBoardLayout.setAlpha(0.7f);
                    }
                });
                synchronized (loadLock) {
                    try {
                        loadLock.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!mActivity.hasWindowFocus()) return;
                loaded = topScoresLoaded && centeredScoresLoaded;
                i++; if(i > 3){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleFailure(new Exception());
                        }
                    });
                    break;
                }
            }
            final boolean fLoaded = loaded;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingBar.setVisibility(View.INVISIBLE);
                    if(fLoaded) {
                        leaderBoardLayout.setAlpha(1.0f);
                        leaderBoardListView.setAdapter(new ActivityLeaderBoard.LayoutAdapter(
                                ActivityLeaderBoard.this));
                        leaderBoardListView.setBackground(getLeaderBoardListBackground());
                    }
                }
            });
        }
    }

    private Loader loader;

    private class LayoutAdapter extends BaseAdapter {
        private Context mContext;

        private LayoutAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return leaders.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout elementContainer;
            RelativeLayout scoreElement;
            RelativeLayout infoContainer;
            ImageView avatarView;
            ImageView badgeView;
            TextView playerPosition;
            TextView playerName;
            TextView highScore;


            if (convertView == null) {

                int leaderBoardListFakePadding = (int) Math.rint(screenHeight * 0.03646);
                int leaderBoardListHeight = (int) Math.rint((screenHeight*0.6484375) + leaderBoardListFakePadding);
                int elementContainerHeight = (int) Math.rint(leaderBoardListHeight/5.0);
                int scoreElementHeight = (int) Math.rint(elementContainerHeight*0.8);
                int scoreElementWidth = (int) Math.rint(leaderBoardListHeight*0.76948);
                int playerPositionSize = (int) Math.rint(screenHeight*0.022);
                int playerNameSize = (int) Math.rint(screenHeight*0.022);
                int highScoreSize = (int) Math.rint(screenHeight*0.022);
                int playerPositionMarginLeft = (int) Math.rint(scoreElementWidth*0.05);
                int infoContainerMarginLeft = (int) Math.rint(scoreElementWidth*0.1);
                int avatarSide = (int) Math.rint(scoreElementHeight*0.8);
                int avatarMargin = (int) Math.rint(scoreElementHeight*0.1);
                int badgeSide = (int) Math.rint(scoreElementHeight*0.6);
                int badgeMargin = (int) Math.rint(scoreElementHeight*0.2);

                // if it's not recycled, initialize some attribute

                elementContainer = new RelativeLayout(mContext);
                scoreElement = new RelativeLayout(mContext);
                infoContainer = new RelativeLayout(mContext);
                playerPosition = new TextView(mContext);
                playerName = new TextView(mContext);
                highScore = new TextView(mContext);
                avatarView = new ImageView(mContext);
                badgeView = new ImageView(mContext);


                ListView.LayoutParams elementContainerParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, elementContainerHeight);
                RelativeLayout.LayoutParams scoreElementParams = new RelativeLayout.LayoutParams(scoreElementWidth, scoreElementHeight);
                RelativeLayout.LayoutParams infoContainerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams playerPositionParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams playerNameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams highScoreParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams avatarParams = new RelativeLayout.LayoutParams(avatarSide, avatarSide);
                RelativeLayout.LayoutParams badgeParams = new RelativeLayout.LayoutParams(badgeSide, badgeSide);


                playerPositionParams.setMargins(playerPositionMarginLeft, 0, playerPositionMarginLeft, 0);
                infoContainerParams.setMargins(infoContainerMarginLeft, 0, 0, 0);
//                avatarParams.setMargins(avatarMargin, 0, 0, 0);
                badgeParams.setMargins(0, 0, badgeMargin, 0);

                infoContainer.setId(R.id.info_container);
                playerPosition.setId(R.id.player_position);
                playerName.setId(R.id.player_name);
                highScore.setId(R.id.high_score);
                avatarView.setId(R.id.avatar);
                badgeView.setId(R.id.badge_medal);

                scoreElementParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                playerPositionParams.addRule(RelativeLayout.CENTER_VERTICAL);
                playerPositionParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                infoContainerParams.addRule(RelativeLayout.CENTER_VERTICAL);
                infoContainerParams.addRule(RelativeLayout.RIGHT_OF, avatarView.getId());
                playerNameParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                playerNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                highScoreParams.addRule(RelativeLayout.BELOW, playerName.getId());
                avatarParams.addRule(RelativeLayout.RIGHT_OF, playerPosition.getId());
                avatarParams.addRule(RelativeLayout.CENTER_VERTICAL);
                badgeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                badgeParams.addRule(RelativeLayout.CENTER_VERTICAL);

                scoreElement.setBackground(getScoreElementBackground());
                playerPosition.setTextSize(TypedValue.COMPLEX_UNIT_PX, playerPositionSize);
                playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, playerNameSize);
                highScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, highScoreSize);
                playerPosition.setTextColor(getResources().getColor(R.color.app_text_color));
                playerName.setTextColor(getResources().getColor(R.color.app_text_color));
                highScore.setTextColor(getResources().getColor(R.color.green));
                playerPosition.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                playerName.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                highScore.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                playerPosition.setMaxLines(1);
                playerName.setMaxEms(20);
                highScore.setMaxEms(20);


                infoContainer.addView(playerName, playerNameParams);
                infoContainer.addView(highScore, highScoreParams);

                scoreElement.addView(playerPosition, playerPositionParams);
                scoreElement.addView(infoContainer, infoContainerParams);
                scoreElement.addView(avatarView, avatarParams);
                scoreElement.addView(badgeView, badgeParams);

                elementContainer.addView(scoreElement, scoreElementParams);

                elementContainer.setLayoutParams(elementContainerParams);

            } else {
                elementContainer = (RelativeLayout) convertView;
                scoreElement = (RelativeLayout) elementContainer.getChildAt(0);
                playerPosition = (TextView) scoreElement.getChildAt(0);
                infoContainer = (RelativeLayout) scoreElement.getChildAt(1);
                avatarView = (ImageView) scoreElement.getChildAt(2);
                badgeView = (ImageView) scoreElement.getChildAt(3);
                playerName = (TextView) infoContainer.getChildAt(0);
                highScore = (TextView) infoContainer.getChildAt(1);
            }

            LeaderBoardEntry leaderBoardEntry = getLeaderBoardEntry(position);

            playerPosition.setText(leaderBoardEntry.rank);
            playerName.setText(leaderBoardEntry.playerName);
            highScore.setText(leaderBoardEntry.displayScore);
            badgeView.setImageResource(leaderBoardEntry.badgeResource);

            final ImageView fAvatarView = avatarView;
            ImageManager.OnImageLoadedListener imageLoadedListener = new ImageManager.OnImageLoadedListener() {
                @Override
                public void onImageLoaded(Uri uri, Drawable drawable, boolean b) {
                    if (b) {
                        BitmapDrawable bd;
                        bd = (BitmapDrawable) drawable;
                        fAvatarView.setImageBitmap(getCroppedAvatarBitmap(bd.getBitmap()));
                        fAvatarView.setVisibility(View.VISIBLE);
                    }
                    else {
                        fAvatarView.setImageDrawable(drawable);
                        fAvatarView.setVisibility(View.INVISIBLE);
                    }
                }
            };
            avatarManager.loadImage(imageLoadedListener, leaderBoardEntry.avatar, R.drawable.big_o);

            return elementContainer;
        }

        private String[] leaders = {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
                "41"
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

    LeaderboardsClient.LeaderboardScores leaderBoardScores;
    LeaderboardScoreBuffer leaderBoardScoreBuffer;
    ArrayList<LeaderboardScore> scoreList;
    private void loadLeaderBoardScores() {
        highScoreCursor = mProgress.getHighScoreCursor();
        scoreList = new ArrayList<>();
        if(leaderboardsClient != null) {
            if(!topScoresLoaded) {
                leaderboardsClient.loadTopScores(getResources().getString
                                (R.string.leaderboard_hall_of_champions),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC,
                        20, false).addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardsClient.LeaderboardScores>>() {
                    @Override
                    public void onSuccess(AnnotatedData<LeaderboardsClient.LeaderboardScores> leaderBoardScoresAnnotatedData) {
                        leaderBoardScores = leaderBoardScoresAnnotatedData.get();
                        if (leaderBoardScores != null) {
                            leaderBoardScoreBuffer = leaderBoardScores.getScores();
                            for (int i = 0; i < leaderBoardScoreBuffer.getCount(); i++) {
                                scoreList.add(leaderBoardScoreBuffer.get(i));
                            }
                            for(int i = scoreList.size(); i <= 20; i++){
                                scoreList.add(null);
                            }
                            topScoresLoaded = true;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(leaderBoardScores != null){
                        leaderBoardScores.release();
                        leaderBoardScores = null;
                        }
                        if(leaderBoardScoreBuffer != null) {
                            leaderBoardScoreBuffer.release();
                            leaderBoardScoreBuffer = null;
                        }
                        e.printStackTrace();
                    }
                });
            }
            if(!centeredScoresLoaded) {
                leaderboardsClient.loadPlayerCenteredScores(getResources().getString
                                (R.string.leaderboard_hall_of_champions),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC,
                        20, false).addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardsClient.LeaderboardScores>>() {
                    @Override
                    public void onSuccess(AnnotatedData<LeaderboardsClient.LeaderboardScores> leaderBoardScoresAnnotatedData) {
                        leaderBoardScores = leaderBoardScoresAnnotatedData.get();
                        if (leaderBoardScores != null) {
                            leaderBoardScoreBuffer = leaderBoardScores.getScores();
                            for (int i = 0; i < leaderBoardScoreBuffer.getCount(); i++) {
                                scoreList.add(leaderBoardScoreBuffer.get(i));
                            }
                            leaderBoardEntries = cacheScoresToLocal();
                            leaderBoardScores.release();
                            leaderBoardScoreBuffer.release();
                            leaderBoardScores = null;
                            leaderBoardScoreBuffer = null;
                            synchronized (loadLock) {
                                centeredScoresLoaded = true;
                                loadLock.notifyAll();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(leaderBoardScores != null){
                            leaderBoardScores.release();
                            leaderBoardScores = null;
                        }
                        if(leaderBoardScoreBuffer != null) {
                            leaderBoardScoreBuffer.release();
                            leaderBoardScoreBuffer = null;
                        }
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private LeaderboardsClient getLeaderBoardsClient(Context context){
        GoogleSignInAccount leaderBoardsAccount =
                GoogleSignIn.getLastSignedInAccount(context);
        LeaderboardsClient leaderBoardsClient1 = null;
        if(leaderBoardsAccount != null)
            leaderBoardsClient1 = Games.getLeaderboardsClient(context, leaderBoardsAccount);
        return leaderBoardsClient1;
    }

    private LeaderBoardEntry getLeaderBoardEntry(int position){
        boolean scoreAvailable = leaderBoardEntries != null;
        if(scoreAvailable) scoreAvailable = leaderBoardEntries.length > position;
        if(scoreAvailable) scoreAvailable = leaderBoardEntries[position] != null;

        if(position == 20){
            LeaderBoardEntry leaderBoardEntry = new LeaderBoardEntry();
            leaderBoardEntry.displayScore = "";
            leaderBoardEntry.playerName =   "";
            leaderBoardEntry.rank = "............................................................" +
                    "...........................................................................";
            leaderBoardEntry.avatar = null;
            leaderBoardEntry.badgeResource = 0;
            return leaderBoardEntry;
        }
        if(scoreAvailable){
            switch (position){
                case 0:
                    leaderBoardEntries[position].badgeResource =
                            R.drawable.ic_leaderboard_gold_medal;
                    break;
                case 1:
                    leaderBoardEntries[position].badgeResource =
                            R.drawable.ic_leaderboard_silver_medal;
                    break;
                case 2:
                    leaderBoardEntries[position].badgeResource =
                            R.drawable.ic_leaderboard_bronze_medal;
                    break;
            }
            return leaderBoardEntries[position];
        }
        else {
            LeaderBoardEntry leaderBoardEntry = new LeaderBoardEntry();
            highScoreCursor.moveToFirst();
            leaderBoardEntry.displayScore = "- - - -";
            leaderBoardEntry.playerName = "- - - - - -";
            leaderBoardEntry.rank = "-";
            leaderBoardEntry.avatar = null;
            leaderBoardEntry.badgeResource = 0;
            return leaderBoardEntry;
        }
    }

    private LeaderBoardEntry[] cacheScoresToLocal(){
        boolean scoreObtainable = scoreList != null;
        if(scoreObtainable){
            scoreObtainable = scoreList.size() > 0;
        }
        if(scoreObtainable){
            Log.d("null check", "scoreList: " + scoreList.size());
            LeaderBoardEntry[] leaderBoardData = new LeaderBoardEntry[scoreList.size()];
            for(int j = 0; j < scoreList.size(); j++){
                if(scoreList.get(j) != null) {
                    leaderBoardData[j] = new LeaderBoardEntry();
                    leaderBoardData[j].playerName = scoreList.get(j).
                            getScoreHolderDisplayName();
                    leaderBoardData[j].rank = scoreList.get(j).getDisplayRank();
                    leaderBoardData[j].displayScore = scoreList.get(j).getDisplayScore();
                    leaderBoardData[j].avatar = scoreList.get(j).getScoreHolderIconImageUri();
                }
            }
            return  leaderBoardData;
        }
        scoreList = null;
        return null;
    }

    private GradientDrawable getScoreElementBackground(){
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

    private GradientDrawable getLeaderBoardListBackground(){
        int backgroundHeight = leaderBoardListView.getHeight();
        int backgroundWidth = leaderBoardListView.getWidth();
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

    private void declareGameSounds(){
        sfx_volume = mProgress.getSfxVolume();
        float soundtrack_volume = mProgress.getSoundTrackVolume();
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

    @SuppressLint("ClickableViewAccessibility")
    private void createLayout(boolean initializing){

        ImageView backArrow = findViewById(R.id.back_arrow);
        ImageView leaderBoardIcon = findViewById(R.id.big_leader_board_ic);
        ImageView leaderBoardTitle = findViewById(R.id.leader_board_title);
        if(initializing) {
            leaderBoardListView = findViewById(R.id.leader_board_list);
            loadingBar = findViewById(R.id.loading_bar);
            leaderBoardLayout = findViewById(R.id.leader_board_layout);

            if(adView == null) {
                adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setId(R.id.ad_view);
                adView.setAdUnitId("ca-app-pub-1923630121694917/4356482874");
            }
        }


        int leaderBoardListMarginTop = (int) Math.rint(screenHeight*0.05885);
        int leaderBoardListFakePadding = (int) Math.rint(screenHeight * 0.03646);
        int leaderBoardListHeight = (int) Math.rint((screenHeight*0.6484375) + leaderBoardListFakePadding);
        int leaderBoardListWidth = (int) Math.rint((leaderBoardListHeight*0.76948) + leaderBoardListFakePadding);
        int back_arrow_diameter = (int) Math.rint(screenHeight*0.06094);
        int back_arrow_margin = (int) Math.rint(screenHeight*0.015);
        int leaderBoardIconHeight = (int) Math.rint(screenHeight * 0.04867);
        int leaderBoardIconWidth = (int) Math.rint(leaderBoardIconHeight*1.1694);
        int leaderBoardIconTop = (int) Math.rint(screenHeight*0.05306);
        int leaderBoardTitleHeight = (int) Math.rint(screenHeight*0.018594);
        int leaderBoardTitleWidth = (int) Math.rint(leaderBoardTitleHeight*12.306264);
        int leaderBoardTitleMarginTop = (int) Math.rint(screenHeight*0.05);

        RelativeLayout.LayoutParams backArrowParams = new RelativeLayout.LayoutParams(back_arrow_diameter, back_arrow_diameter);
        RelativeLayout.LayoutParams leaderBoardIconParams = new RelativeLayout.LayoutParams(leaderBoardIconWidth, leaderBoardIconHeight);
        RelativeLayout.LayoutParams leaderBoardTitleParams = new RelativeLayout.LayoutParams(leaderBoardTitleWidth, leaderBoardTitleHeight);
        RelativeLayout.LayoutParams leaderBoardListParams = new RelativeLayout.LayoutParams(leaderBoardListWidth, leaderBoardListHeight);

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        backArrowParams.setMargins(back_arrow_margin, back_arrow_margin, 0, 0);
        leaderBoardIconParams.setMargins(0, leaderBoardIconTop, 0, 0);
        leaderBoardTitleParams.setMargins(0, leaderBoardTitleMarginTop, 0, 0);
        leaderBoardListParams.setMargins(0, leaderBoardListMarginTop, 0, 0);

        leaderBoardIconParams.addRule(RelativeLayout.BELOW, R.id.back_arrow);
        leaderBoardIconParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        leaderBoardTitleParams.addRule(RelativeLayout.BELOW, R.id.big_leader_board_ic);
        leaderBoardTitleParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        leaderBoardListParams.addRule(RelativeLayout.BELOW, R.id.leader_board_title);
        leaderBoardListParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        backArrow.setLayoutParams(backArrowParams);
        leaderBoardIcon.setLayoutParams(leaderBoardIconParams);
        leaderBoardTitle.setLayoutParams(leaderBoardTitleParams);
        leaderBoardListView.setLayoutParams(leaderBoardListParams);


        if(initializing) {
            if(leaderBoardLayout.getChildAt(leaderBoardLayout.getChildCount() - 1).getId() != R.id.ad_view)
                leaderBoardLayout.addView(adView, adParams);

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
                    ActivityLeaderBoard.this.finish();
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

        if(!initializing && topScoresLoaded && centeredScoresLoaded) {
            leaderBoardLayout.setAlpha(1.0f);
            leaderBoardListView.setAdapter(new ActivityLeaderBoard.LayoutAdapter(
                    ActivityLeaderBoard.this));
            leaderBoardListView.setBackground(getLeaderBoardListBackground());
        }

        prepareAlertPopUp(initializing);
    }

    private Bitmap getCroppedAvatarBitmap(Bitmap normalBitmap){
        Bitmap croppedBitmap = Bitmap.createBitmap(normalBitmap.getWidth(),
                normalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, normalBitmap.getWidth(),
                normalBitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = (int) Math.rint(screenHeight * 0.0175);


        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(normalBitmap, rect, rect, paint);

        return croppedBitmap;
    }

    private void prepareAlertPopUp(boolean initializing){
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
        ImageView actionRetry = popView.findViewById(R.id.action1);
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
        RelativeLayout.LayoutParams actionRetryParams = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);
        RelativeLayout.LayoutParams actionDismissParams = new RelativeLayout.LayoutParams(buttonWidth, buttonHeight);

        alertActionParams.setMargins(0, margin1*4, 0, 0);
        textContainerParams.setMargins(margin1, margin2, margin1, margin2);
        actionDismissParams.setMargins(0, 0, margin2, 0);
        actionRetryParams.setMargins(margin2, 0, 0, 0);

        textContainerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        alertActionParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        actionRetryParams.addRule(RelativeLayout.RIGHT_OF, R.id.action_dismiss);

        popUpTextContainer.setLayoutParams(textContainerParams);
        alertAction.setLayoutParams(alertActionParams);
        actionRetry.setLayoutParams(actionRetryParams);
        actionDismiss.setLayoutParams(actionDismissParams);

        if(initializing) {
            if(alertPopUp == null) {
                alertPopUp = new PopupWindow(popView);
                alertPopUp.setWidth(popUpWidth);
                alertPopUp.setHeight(popUpHeight);
                alertPopUp.setFocusable(true);
            }


            actionRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler(loaderThread.getLooper()).post(loader);
                    playSoundEffect(BUTTON_CLICKED);
                }
            });

            actionDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertPopUp.dismiss();
                    ActivityLeaderBoard.this.finish();
                    playSoundEffect(BUTTON_CLICKED);
                }
            });

            alertPopUp.setAnimationStyle(R.style.center_pop_up_anim);
        }

    }


    private void showAlert(){
        if(ActivityLeaderBoard.this.hasWindowFocus()) {
            int popUpWidth = (int) Math.rint(screenHeight * 0.5);
            int yLocation = (int) Math.rint(screenHeight * 0.45);
            int xLocation = (int) Math.rint((screenWidth - popUpWidth) / 2.0);
            if (ActivityLeaderBoard.this.hasWindowFocus()) { //make sure popup is not inflated when activity is not on screen as this will throw an exception
                if (!alertPopUp.isShowing()) {
                    alertLayout.setBackground(getResources().getDrawable(R.drawable.whoops_alert));
                    alertPopUp.showAtLocation(leaderBoardLayout, NO_GRAVITY, xLocation, yLocation);
                    leaderBoardLayout.setAlpha(0.7f);
                }
            }
        }
    }

    private void handleFailure(Exception e){
        showAlert();
        e.printStackTrace();
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
        declareGameSounds();
        if (backVectorAnimation != null)
            backVectorAnimation.start();
        if(backgroundMusic != null){
            backgroundMusic.start();
            backgroundMusic.seekTo(PLAYBACK_PROGRESS);
        }
        if(loader == null) loader = new Loader();
        if(loaderThread == null) loaderThread = new HandlerThread("loader");
        if(!loaderThread.isAlive())
            loaderThread.start();
        new Handler(loaderThread.getLooper()).post(loader);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.leader_board);
        getScreenSize(0);
        leaderboardsClient = getLeaderBoardsClient(this);
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