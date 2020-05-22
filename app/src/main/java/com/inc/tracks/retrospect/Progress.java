package com.inc.tracks.retrospect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;

import static com.inc.tracks.retrospect.ActivitySplashScreen.ACHIEVEMENTS_COUNT;
import static com.inc.tracks.retrospect.ActivitySplashScreen.LEVEL_COUNT;

public class Progress {

    private FeedProgressDbHelper progressDbHelper;
    private SQLiteDatabase readableProgressDb;
    private SQLiteDatabase writableProgressDb;
    int pDifficulty;
    int pLevel;
    int highScore;
    int highScoreTime;
    int pRoundsCompleted;
    int pBestCombo;
    static final int NO_STARS = 0;
    static final int ONE_STAR = 1;
    static final int TWO_STARS = 2;
    static final int THREE_STARS = 3;
    static final int FOUR_STARS = 4;
    static final int LOCKED_LEVEL_SCORE = -11;
    static final String COINS = "coins_collectible";
    static final String BREAK = "free_break";
    static final String CLEAR = "free_clear";
    static final String SOLVE = "free_solve";
    static final String SLOW = "free_slow";

    private LeaderboardsClient leaderboardsClient;

    static final int NOT_ZEN = -31;


    public Progress(Context context){
        progressDbHelper = new FeedProgressDbHelper(context);
        readableProgressDb = progressDbHelper.getReadableDatabase();
        writableProgressDb = progressDbHelper.getWritableDatabase();
    }

    private LeaderboardsClient getLeaderBoardsClient(Context context){
        GoogleSignInAccount leaderBoardsAccount =
                GoogleSignIn.getLastSignedInAccount(context);
        LeaderboardsClient leaderboardsClient1 = null;
        if(leaderBoardsAccount != null)
            leaderboardsClient1 = Games.getLeaderboardsClient(context, leaderBoardsAccount);
        return leaderboardsClient1;
    }

    boolean updateCompletion(int gameMode, int difficulty, int level, int score,
                                 int roundsCompleted, int bestCombo, int completion_time, int stars,
                                 Context context){
        boolean newHighScore = false;
        if(gameMode == ActivityMainGame.ZEN){
            if(leaderboardsClient == null) leaderboardsClient = getLeaderBoardsClient(context);
            if(score > getExistingHighScoreSummary()[0] || (score == getExistingHighScoreSummary()[0] && completion_time < getExistingHighScoreSummary()[1])){
                updateProgressDb(score, completion_time, roundsCompleted, bestCombo);
                if(leaderboardsClient != null) {
                    leaderboardsClient.submitScore(context.getString(R.string.
                            leaderboard_hall_of_champions), score);
                }
                newHighScore = true;
            } else if (leaderboardsClient != null){
                leaderboardsClient.submitScore(context.getString(R.string.
                        leaderboard_hall_of_champions), getExistingHighScoreSummary()[0]);
            }
            if(bestCombo > getExistingHighScoreSummary()[3]){
                updateBestCombo(bestCombo);
            }
        }
        else {
            if(score > getExistingHighScoreSummary(difficulty, level)[0] || (score == getExistingHighScoreSummary(difficulty, level)[0] && completion_time < getExistingHighScoreSummary(difficulty, level)[1])){
                updateProgressDb(difficulty, level, score, completion_time, stars);
                newHighScore = true;
            }
//            if(stars >= getExistingPersistentStars(difficulty, level)){
//                updatePersistentStars(difficulty, level, stars);
//            }
            if(Achievements.getStarSummary(gameMode, difficulty, level, score, roundsCompleted)[0] >= ONE_STAR) {
                unlockNextLevel(difficulty, level);
            }
        }
        return newHighScore;
    }

    void updateCollectibleBank(String selector, int delta_collectible){

        String selection1 = ProgressDbContract.FeedCollectiblesEntry.COIN_BANK + " != ?";
        String selectionArgs1[] = {"NULL"};

        ContentValues progressValues = new ContentValues();
        switch (selector){
            case Progress.COINS:
                int coins = getCollectibleAccount(Progress.COINS) + delta_collectible;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.COIN_BANK, coins);
                break;
            case Progress.BREAK:
                int breaks = getCollectibleAccount(Progress.BREAK) + delta_collectible;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_BREAK, breaks);
                break;
            case Progress.CLEAR:
                int clear = getCollectibleAccount(Progress.CLEAR) + delta_collectible;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_CLEAR, clear);
                break;
            case Progress.SOLVE:
            int solve = getCollectibleAccount(Progress.SOLVE) + delta_collectible;
            progressValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_SOLVE, solve);
            break;
            case Progress.SLOW:
                int slow = getCollectibleAccount(Progress.SLOW) + delta_collectible;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_SLOW, slow);
                break;
        }
        writableProgressDb.update(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, progressValues, selection1, selectionArgs1);
    }

    void updateCollectibleStats(String selector, int units_used){

        String selection1 = ProgressDbContract.FeedCollectiblesEntry.COIN_BANK + " != ?";
        String selectionArgs1[] = {"NULL"};

        ContentValues progressValues = new ContentValues();
        switch (selector){
            case Progress.COINS:
                int coins = getCollectibleStats(Progress.COINS) + units_used;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.COINS_EARNED, coins);
                break;
            case Progress.BREAK:
                int breaks = getCollectibleStats(Progress.BREAK) + units_used;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.BREAK_USAGE, breaks);
                break;
            case Progress.CLEAR:
                int clear = getCollectibleStats(Progress.CLEAR) + units_used;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.CLEAR_USAGE, clear);
                break;
            case Progress.SOLVE:
                int solve = getCollectibleStats(Progress.SOLVE) + units_used;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.SOLVE_USAGE, solve);
                break;
            case Progress.SLOW:
                int slow = getCollectibleStats(Progress.SLOW) + units_used;
                progressValues.put(ProgressDbContract.FeedCollectiblesEntry.SLOW_USAGE, slow);
                break;
        }
        if(progressValues.size() != 0)
            writableProgressDb.update(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, progressValues, selection1, selectionArgs1);
    }

    void extractHighScoreData(int gameMode, int difficulty, int level){
        int[] scoreSummary;
        if(gameMode == ActivityMainGame.ZEN) {
            scoreSummary = getExistingHighScoreSummary(); //returns the current high score with its completion time
            pRoundsCompleted = scoreSummary[2];
            pBestCombo = scoreSummary[3];
        }
        else scoreSummary = getExistingHighScoreSummary(difficulty, level); //returns the current high score with its completion time
        highScore = scoreSummary[0];
        highScoreTime = scoreSummary[1];
        pDifficulty = difficulty;
        pLevel = level;
    }

    int getCollectibleAccount(String selector){
        int collectible = 0;
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedCollectiblesEntry.COIN_BANK,
                ProgressDbContract.FeedCollectiblesEntry.FREE_BREAK,
                ProgressDbContract.FeedCollectiblesEntry.FREE_CLEAR,
                ProgressDbContract.FeedCollectiblesEntry.FREE_SOLVE,
                ProgressDbContract.FeedCollectiblesEntry.FREE_SLOW
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();
        switch (selector) {
            case COINS:
                collectible = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.COIN_BANK));
                break;
            case BREAK:
                collectible = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.FREE_BREAK));
                break;
            case CLEAR:
                collectible = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.FREE_CLEAR));
                break;
            case SOLVE:
                collectible = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.FREE_SOLVE));
                break;
            case SLOW:
                collectible = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.FREE_SLOW));
                break;
        }

        cursor.close();
        return collectible;
    }

    int getCollectibleStats(String selector){
        int used = 0;
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedCollectiblesEntry.COINS_EARNED,
                ProgressDbContract.FeedCollectiblesEntry.BREAK_USAGE,
                ProgressDbContract.FeedCollectiblesEntry.CLEAR_USAGE,
                ProgressDbContract.FeedCollectiblesEntry.SOLVE_USAGE,
                ProgressDbContract.FeedCollectiblesEntry.SLOW_USAGE
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();
        switch (selector) {
            case COINS:
                used = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.COINS_EARNED));
                break;
            case BREAK:
                used = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.BREAK_USAGE));
                break;
            case CLEAR:
                used = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.CLEAR_USAGE));
                break;
            case SOLVE:
                used = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.SOLVE_USAGE));
                break;
            case SLOW:
                used = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedCollectiblesEntry.SLOW_USAGE));
                break;
        }

        cursor.close();
        return used;
    }

    void disableTutorials(){
        ContentValues progressValues = new ContentValues();
        String selection1 = ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE + " >= ?";
        String selectionArgs1[] = {"1"};
        progressValues.put(ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE, 0);
        writableProgressDb.update(ProgressDbContract.FeedStatsEntry.STATS_TABLE, progressValues, selection1, selectionArgs1);
    }

    int getTutorialState(){
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();

        int stats = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE));
        cursor.close();
        return stats;
    }

    int getTactileState(){
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedStatsEntry.TACTILE_FEEDBACK
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();

        int stats = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedStatsEntry.TACTILE_FEEDBACK));
        cursor.close();
        return stats;
    }
    public float getSfxVolume(){
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedStatsEntry.SFX_VOLUME
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();

        float stats = cursor.getFloat(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedStatsEntry.SFX_VOLUME));
        cursor.close();
        return stats;
    }
    public float getSoundTrackVolume(){
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedStatsEntry.SOUNDTRACK_VOLUME
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();

        float stats = cursor.getFloat(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedStatsEntry.SOUNDTRACK_VOLUME));
        cursor.close();
        return stats;
    }
    int getSignInPreference(){
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedStatsEntry.SIGN_IN_PREFERENCE
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();

        int stats = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedStatsEntry.SIGN_IN_PREFERENCE));
        cursor.close();
        return stats;
    }

    void saveSettings(int tutorial_state, int tactile_state, float sfx_volume, float soundtrack_volume){
        ContentValues progressValues = new ContentValues();

        String selection1 = ProgressDbContract.FeedStatsEntry._ID + " >= ?";
        String selectionArgs1[] = {"0"};
        progressValues.put(ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE, tutorial_state);
        progressValues.put(ProgressDbContract.FeedStatsEntry.TACTILE_FEEDBACK, tactile_state);
        progressValues.put(ProgressDbContract.FeedStatsEntry.SFX_VOLUME, sfx_volume);
        progressValues.put(ProgressDbContract.FeedStatsEntry.SOUNDTRACK_VOLUME, soundtrack_volume);
        writableProgressDb.update(ProgressDbContract.FeedStatsEntry.STATS_TABLE, progressValues, selection1, selectionArgs1);
    }

    void saveSignInPreference(boolean preference){
        ContentValues progressValues = new ContentValues();
        int sign_in_preference = preference ? 1 : 0;

        String selection1 = ProgressDbContract.FeedStatsEntry._ID + " >= ?";
        String selectionArgs1[] = {"0"};

        progressValues.put(ProgressDbContract.FeedStatsEntry.SIGN_IN_PREFERENCE, sign_in_preference);
        writableProgressDb.update(ProgressDbContract.FeedStatsEntry.STATS_TABLE, progressValues, selection1, selectionArgs1);
    }

    private int[] getExistingHighScoreSummary(int difficulty, int level){
        int[] existingHighScoreSummary = new int[2];
        String levelString = "" + level;
        Cursor cursor = null;
        switch (difficulty) {
            case ActivityDifficultyMenu.BEGINNER:
                String[] projection1 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN,
                        ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN,
                        ProgressDbContract.FeedBeginnerProgressEntry.TIME_USED_COLUMN
                };
                String selection1 = ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs1[] = {levelString};
                cursor = readableProgressDb.query(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, projection1, selection1, selectionArgs1, null, null, null);
                cursor.moveToFirst();
                existingHighScoreSummary[0] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN));
                existingHighScoreSummary[1] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedBeginnerProgressEntry.TIME_USED_COLUMN));
                break;
            case ActivityDifficultyMenu.INTERMEDIATE:
                String[] projection2 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN,
                        ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN,
                        ProgressDbContract.FeedIntermediateProgressEntry.TIME_USED_COLUMN
                };
                String selection2 = ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs2[] = {levelString};
                cursor = readableProgressDb.query(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, projection2, selection2, selectionArgs2, null, null, null);
                cursor.moveToFirst();
                existingHighScoreSummary[0] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN));
                existingHighScoreSummary[1] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedIntermediateProgressEntry.TIME_USED_COLUMN));
                break;
            case ActivityDifficultyMenu.EXPERT:
                String[] projection3 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN,
                        ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN,
                        ProgressDbContract.FeedExpertProgressEntry.TIME_USED_COLUMN
                };
                String selection3 = ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs3[] = {levelString};
                cursor = readableProgressDb.query(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, projection3, selection3, selectionArgs3, null, null, null);
                cursor.moveToFirst();
                existingHighScoreSummary[0] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN));
                existingHighScoreSummary[1] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedExpertProgressEntry.TIME_USED_COLUMN));
                break;
            case ActivityDifficultyMenu.EIDETIC:
                String[] projection4 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN,
                        ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN,
                        ProgressDbContract.FeedEideticProgressEntry.TIME_USED_COLUMN
                };
                String selection4 = ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs4[] = {levelString};
                cursor = readableProgressDb.query(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, projection4, selection4, selectionArgs4, null, null, null);
                cursor.moveToFirst();
                existingHighScoreSummary[0] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN));
                existingHighScoreSummary[1] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedEideticProgressEntry.TIME_USED_COLUMN));
                break;
        }
        if(cursor != null) {
            cursor.close();
        }
        return existingHighScoreSummary;
    }// index 0 for score and 1 for completion time
    private int[] getExistingHighScoreSummary(){
        int[] existingHighScore = new int[4];
        Cursor cursor;
        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN,
                ProgressDbContract.FeedZenProgressEntry.TIME_USED_COLUMN,
                ProgressDbContract.FeedZenProgressEntry.ROUNDS_COMPLETED_COLUMN,
                ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN
        };
        cursor = readableProgressDb.query(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, projection, null, null, null, null, null);
        cursor.moveToFirst();
        existingHighScore[0] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN));
        existingHighScore[1] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedZenProgressEntry.TIME_USED_COLUMN));
        existingHighScore[2] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedZenProgressEntry.ROUNDS_COMPLETED_COLUMN));
        existingHighScore[3] = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN));

        cursor.close();
        return existingHighScore;
    }// index 0 for score, 1 for completion time, 2 for rounds completed and 3 for best combo

//    private int getExistingPersistentStars(int difficulty, int level){
//        int existingStars = 0;
//        String[] projection1 = {
//                BaseColumns._ID,
//                ProgressDbContract.FeedPersistentProgressEntry.LEVEL_COLUMN,
//                ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED,
//                ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED,
//                ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED,
//                ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED
//        };
//        String levelString = "" + level;
//        String selection = ProgressDbContract.FeedPersistentProgressEntry.LEVEL_COLUMN + " = ?";
//        String selectionArgs[] = {levelString};
//        Cursor cursor = readableProgressDb.query(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, projection1, selection, selectionArgs, null, null, null);
//        cursor.moveToNext();
//        switch (difficulty) {
//            case ActivityDifficultyMenu.BEGINNER:
//                existingStars = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED));
//                break;
//            case ActivityDifficultyMenu.INTERMEDIATE:
//                existingStars = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED));
//                break;
//            case ActivityDifficultyMenu.EXPERT:
//                existingStars = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED));
//                break;
//            case ActivityDifficultyMenu.EIDETIC:
//                existingStars = cursor.getInt(cursor.getColumnIndexOrThrow(ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED));
//                break;
//        }
//        cursor.close();
//        return existingStars;
//    }


//    public int sumAllPersistentStars(){
//        Cursor cursor;
//        int sum[] = new int[4];
//        String SQLStatement = "SELECT SUM " + "(" +
//                ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED + ") " + "FROM "
//                + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE;
//        cursor = readableProgressDb.rawQuery(SQLStatement, null);
//        cursor.moveToFirst();
//        sum[0] = cursor.getInt(cursor.getColumnIndexOrThrow("SUM"));
//        SQLStatement = "SELECT SUM " + "(" +
//                ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED + ") " + "FROM "
//                + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE;
//        cursor = readableProgressDb.rawQuery(SQLStatement, null);
//        cursor.moveToFirst();
//        sum[1] = cursor.getInt(cursor.getColumnIndexOrThrow("SUM"));
//        SQLStatement = "SELECT SUM " + "(" +
//                ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED + ") " + "FROM "
//                + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE;
//        cursor = readableProgressDb.rawQuery(SQLStatement, null);
//        cursor.moveToFirst();
//        sum[2] = cursor.getInt(cursor.getColumnIndexOrThrow("SUM"));
//        SQLStatement = "SELECT SUM " + "(" +
//                ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED + ") " + "FROM "
//                + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE;
//        cursor = readableProgressDb.rawQuery(SQLStatement, null);
//        cursor.moveToFirst();
//        sum[3] = cursor.getInt(cursor.getColumnIndexOrThrow("SUM"));
//        cursor.close();
//        return (sum[0] + sum[1] + sum[2] + sum[3]);
//    }
    int sumAllStars(){
        Cursor cursor;
        int sum[] = new int[4];
        String sumColumn = "sum";

        String SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedBeginnerProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum[0] = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedIntermediateProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum[1] = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedExpertProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum[2] = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedEideticProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum[3] = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        cursor.close();
        return (sum[0] + sum[1] + sum[2] + sum[3]);
    }

    int sumBeginnerStars(){
        Cursor cursor;
        int sum;
        String sumColumn = "sum";

        String SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedBeginnerProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        cursor.close();
        return sum;
    }

    int sumIntermediateStars(){
        Cursor cursor;
        int sum;
        String sumColumn = "sum";

        String SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedIntermediateProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        cursor.close();

        return sum;
    }

    int sumExpertStars(){
        Cursor cursor;
        int sum;
        String sumColumn = "sum";

        String SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedExpertProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        cursor.close();
        return sum;
    }

    int sumEideticStars(){
        Cursor cursor;
        int sum;
        String sumColumn = "sum";

        String SQLStatement = "SELECT SUM" + "(" +
                ProgressDbContract.FeedEideticProgressEntry.STARS_OBTAINED_COLUMN + ") as " + sumColumn + " FROM "
                + ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE;
        cursor = readableProgressDb.rawQuery(SQLStatement, null);
        cursor.moveToFirst();
        sum = cursor.getInt(cursor.getColumnIndexOrThrow(sumColumn));

        cursor.close();
        return sum;
    }

    boolean[] openUnlockedDifficulties(){
        final int minForIntermediate = 50;
        final int minForExpert = 100;
        final int minForEidetic = 200;

        boolean[] difficultyUnlocked = new boolean[3];

        int stars = sumAllStars();

        if(stars >= minForIntermediate){
            unlockDifficulty(ActivityDifficultyMenu.INTERMEDIATE);
            difficultyUnlocked[0] = true;
        }
        if(stars >= minForExpert){
            unlockDifficulty(ActivityDifficultyMenu.EXPERT);
            difficultyUnlocked[1] = true;
        }
        if(stars >= minForEidetic){
            unlockDifficulty(ActivityDifficultyMenu.EIDETIC);
            difficultyUnlocked[2] = true;
        }

        return difficultyUnlocked;
    }


//    private void updatePersistentStars(int difficulty, int level, int stars){
//        ContentValues progressValues = new ContentValues();
//        String levelString = "" + level;
//        String selection = ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " = ?";
//        String selectionArgs[] = {levelString};
//
//        switch (difficulty) {
//            case ActivityDifficultyMenu.BEGINNER:
//                progressValues.put(ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED, stars);
//                writableProgressDb.update(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, progressValues, selection, selectionArgs);
//                break;
//            case ActivityDifficultyMenu.INTERMEDIATE:
//                progressValues.put(ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED, stars);
//                writableProgressDb.update(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, progressValues, selection, selectionArgs);
//                break;
//            case ActivityDifficultyMenu.EXPERT:
//                progressValues.put(ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED, stars);
//                writableProgressDb.update(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, progressValues, selection, selectionArgs);
//                break;
//            case ActivityDifficultyMenu.EIDETIC:
//                progressValues.put(ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED, stars);
//                writableProgressDb.update(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, progressValues, selection, selectionArgs);
//                break;
//        }
//    }
    Cursor getLevelScores(int difficulty){
        switch (difficulty) {
            case ActivityDifficultyMenu.BEGINNER:
                String[] projection1 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN
                };
                return readableProgressDb.query(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, projection1, null, null, null, null, null);
            case ActivityDifficultyMenu.INTERMEDIATE:
                String[] projection2 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN
                };
                return readableProgressDb.query(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, projection2, null, null, null, null, null);
            case ActivityDifficultyMenu.EXPERT:
                String[] projection3 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN
                };
                return readableProgressDb.query(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, projection3, null, null, null, null, null);
            case ActivityDifficultyMenu.EIDETIC:
                String[] projection4 = {
                        BaseColumns._ID,
                        ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN
                };
                return readableProgressDb.query(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, projection4, null, null, null, null, null);
        }
        return null;
    }

    Cursor getHighScoreCursor(){

        String[] projection = {
                BaseColumns._ID,
                ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN,
        };

        String selection = ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN + " >= ?";
        String selectionArgs[] = {"0"};

        return readableProgressDb.query(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, projection, selection, selectionArgs, null, null, null);
    }

    void resetProgress(){
        progressDbHelper.deleteDb(writableProgressDb);
        initializeDatabase();
    }

    void initializeDatabase(){
        SQLiteDatabase writableProgressDb = progressDbHelper.getWritableDatabase();
        progressDbHelper.onCreate(writableProgressDb);
        Cursor cursor;
        ContentValues mContentValues = new ContentValues();

        String[] projection = {
                BaseColumns._ID,
        };
        cursor = writableProgressDb.query(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < LEVEL_COUNT) {
            for (int i = cursor.getCount(); i < LEVEL_COUNT; i++) {
                mContentValues.put(ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN, i+1);
                if(i == 0){
                    mContentValues.put(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN, 0);
                }else {
                    mContentValues.put(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN, Progress.LOCKED_LEVEL_SCORE);
                }
                writableProgressDb.insert(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, null, mContentValues);
            }
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < LEVEL_COUNT) {
            for (int i = cursor.getCount(); i < LEVEL_COUNT; i++) {
                mContentValues.put(ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN, i+1);
                mContentValues.put(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN, Progress.LOCKED_LEVEL_SCORE);
                writableProgressDb.insert(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, null, mContentValues);
            }
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < LEVEL_COUNT) {
            for (int i = cursor.getCount(); i < LEVEL_COUNT; i++) {
                mContentValues.put(ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN, i+1);
                mContentValues.put(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN, Progress.LOCKED_LEVEL_SCORE);
                writableProgressDb.insert(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, null, mContentValues);
            }
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < LEVEL_COUNT) {
            for (int i = cursor.getCount(); i < LEVEL_COUNT; i++) {
                mContentValues.put(ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN, i+1);
                mContentValues.put(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN, Progress.LOCKED_LEVEL_SCORE);
                writableProgressDb.insert(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, null, mContentValues);
            }
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < 1) {
            mContentValues.put(ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN, 0);
            writableProgressDb.insert(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, null, mContentValues);
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < 1) {
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.COIN_BANK, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_BREAK, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_CLEAR, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_SOLVE, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.FREE_SLOW, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.COINS_EARNED, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.BREAK_USAGE, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.CLEAR_USAGE, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.SOLVE_USAGE, 0);
            mContentValues.put(ProgressDbContract.FeedCollectiblesEntry.SLOW_USAGE, 0);
            writableProgressDb.insert(ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE, null, mContentValues);
        }
        mContentValues.clear();
//        cursor = writableProgressDb.query(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, projection, null, null, null, null, null);
//        if(cursor.getCount() < LEVEL_COUNT) {
//            for (int i = cursor.getCount(); i < LEVEL_COUNT; i++) {
//                mContentValues.put(ProgressDbContract.FeedPersistentProgressEntry.LEVEL_COLUMN, i+1);
//                mContentValues.put(ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED, 0);
//                mContentValues.put(ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED, 0);
//                mContentValues.put(ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED, 0);
//                mContentValues.put(ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED, 0);
//                writableProgressDb.insert(ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE, null, mContentValues);
//            }
//        }
//        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedStatsEntry.STATS_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < 1) {
            mContentValues.put(ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE, 1);
            mContentValues.put(ProgressDbContract.FeedStatsEntry.TACTILE_FEEDBACK, 1);
            mContentValues.put(ProgressDbContract.FeedStatsEntry.SFX_VOLUME, 0.5);
            mContentValues.put(ProgressDbContract.FeedStatsEntry.SOUNDTRACK_VOLUME, 0.5);
            mContentValues.put(ProgressDbContract.FeedStatsEntry.SIGN_IN_PREFERENCE, -1);
            writableProgressDb.insert(ProgressDbContract.FeedStatsEntry.STATS_TABLE, null, mContentValues);
        }
        mContentValues.clear();
        cursor = writableProgressDb.query(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE, projection, null, null, null, null, null);
        if(cursor.getCount() < ACHIEVEMENTS_COUNT) {
            String[][] details = Achievements.getAchievementsListings();
            for (int i = cursor.getCount(); i < ACHIEVEMENTS_COUNT; i++) {
                int goal = Integer.parseInt(details[i][2]);
                mContentValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_NAME_COLUMN, details[i][0]);
                mContentValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_DESCRIPTION_COLUMN, details[i][1]);
                mContentValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN, 0);
                mContentValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_GOAL_COLUMN, goal);
                writableProgressDb.insert(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE, null, mContentValues);
            }
        }
        cursor.close();
    }

    public void finish(){
        readableProgressDb.close();
        writableProgressDb.close();
        progressDbHelper.close();
    }

    private void updateProgressDb(int difficulty, int level, int score, int completion_time, int stars){
        ContentValues progressValues = new ContentValues();

        String levelString = "" + level;
        switch (difficulty) {
            case ActivityDifficultyMenu.BEGINNER:
                String selection1 = ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs1[] = {levelString};
                progressValues.put(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN, score);
                progressValues.put(ProgressDbContract.FeedBeginnerProgressEntry.TIME_USED_COLUMN, completion_time);
                progressValues.put(ProgressDbContract.FeedBeginnerProgressEntry.STARS_OBTAINED_COLUMN, stars);
                writableProgressDb.update(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, progressValues, selection1, selectionArgs1);
                break;
            case ActivityDifficultyMenu.INTERMEDIATE:
                String selection2 = ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs2[] = {levelString};
                progressValues.put(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN, score);
                progressValues.put(ProgressDbContract.FeedIntermediateProgressEntry.TIME_USED_COLUMN, completion_time);
                progressValues.put(ProgressDbContract.FeedIntermediateProgressEntry.STARS_OBTAINED_COLUMN, stars);
                writableProgressDb.update(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, progressValues, selection2, selectionArgs2);
                break;
            case ActivityDifficultyMenu.EXPERT:
                String selection3 = ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs3[] = {levelString};
                progressValues.put(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN, score);
                progressValues.put(ProgressDbContract.FeedExpertProgressEntry.TIME_USED_COLUMN, completion_time);
                progressValues.put(ProgressDbContract.FeedExpertProgressEntry.STARS_OBTAINED_COLUMN, stars);
                writableProgressDb.update(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, progressValues, selection3, selectionArgs3);
                break;
            case ActivityDifficultyMenu.EIDETIC:
                String selection4 = ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN + " = ?";
                String selectionArgs4[] = {levelString};
                progressValues.put(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN, score);
                progressValues.put(ProgressDbContract.FeedEideticProgressEntry.TIME_USED_COLUMN, completion_time);
                progressValues.put(ProgressDbContract.FeedEideticProgressEntry.STARS_OBTAINED_COLUMN, stars);
                writableProgressDb.update(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, progressValues, selection4, selectionArgs4);
                break;
        }
    }
    private void updateProgressDb(int score, int completion_time, int rounds_completed, int bestCombo){
        ContentValues progressValues = new ContentValues();

        String selection1 = ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN + " >= ?";
        String selectionArgs1[] = {"0"};
        progressValues.put(ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN, score);
        progressValues.put(ProgressDbContract.FeedZenProgressEntry.TIME_USED_COLUMN, completion_time);
        progressValues.put(ProgressDbContract.FeedZenProgressEntry.ROUNDS_COMPLETED_COLUMN, rounds_completed);
        progressValues.put(ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN, bestCombo);
        writableProgressDb.update(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, progressValues, selection1, selectionArgs1);
    }
    private void updateBestCombo(int bestCombo){
        ContentValues progressValues = new ContentValues();

        String selection1 = ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN + " >= ?";
        String selectionArgs1[] = {"0"};
        progressValues.put(ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN, bestCombo);
        writableProgressDb.update(ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE, progressValues, selection1, selectionArgs1);
    }

    private void unlockNextLevel(int difficulty, int level){
        int nextLevel = level + 1;
        if(getExistingHighScoreSummary(difficulty, nextLevel)[0] == LOCKED_LEVEL_SCORE) {
            ContentValues progressValues = new ContentValues();
            String nextLevelString = "" + (nextLevel);
            switch (difficulty) {
                case ActivityDifficultyMenu.BEGINNER:
                    String selection1 = ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs1[] = {nextLevelString};
                    progressValues.put(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, progressValues, selection1, selectionArgs1);
                    break;
                case ActivityDifficultyMenu.INTERMEDIATE:
                    String selection2 = ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs2[] = {nextLevelString};
                    progressValues.put(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, progressValues, selection2, selectionArgs2);
                    break;
                case ActivityDifficultyMenu.EXPERT:
                    String selection3 = ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs3[] = {nextLevelString};
                    progressValues.put(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, progressValues, selection3, selectionArgs3);
                    break;
                case ActivityDifficultyMenu.EIDETIC:
                    String selection4 = ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs4[] = {nextLevelString};
                    progressValues.put(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, progressValues, selection4, selectionArgs4);
                    break;
            }
        }
    }

    private void unlockDifficulty(int difficulty){
        if(getExistingHighScoreSummary(difficulty, 1)[0] == LOCKED_LEVEL_SCORE) {
            ContentValues progressValues = new ContentValues();
            String firstLevelString = "" + (1);
            switch (difficulty) {
                case ActivityDifficultyMenu.BEGINNER:
                    String selection1 = ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs1[] = {firstLevelString};
                    progressValues.put(ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE, progressValues, selection1, selectionArgs1);
                    break;
                case ActivityDifficultyMenu.INTERMEDIATE:
                    String selection2 = ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs2[] = {firstLevelString};
                    progressValues.put(ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE, progressValues, selection2, selectionArgs2);
                    break;
                case ActivityDifficultyMenu.EXPERT:
                    String selection3 = ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs3[] = {firstLevelString};
                    progressValues.put(ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE, progressValues, selection3, selectionArgs3);
                    break;
                case ActivityDifficultyMenu.EIDETIC:
                    String selection4 = ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN + " = ?";
                    String selectionArgs4[] = {firstLevelString};
                    progressValues.put(ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN, 0);
                    writableProgressDb.update(ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE, progressValues, selection4, selectionArgs4);
                    break;
            }
        }
    }
}

