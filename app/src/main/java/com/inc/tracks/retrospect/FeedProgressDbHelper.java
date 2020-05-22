package com.inc.tracks.retrospect;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedProgressDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ProgressRecord.db";

    public FeedProgressDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_BEGINNER_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE + "(" +
            ProgressDbContract.FeedBeginnerProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedBeginnerProgressEntry.LEVEL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedBeginnerProgressEntry.HIGH_SCORE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedBeginnerProgressEntry.TIME_USED_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedBeginnerProgressEntry.STARS_OBTAINED_COLUMN + " INTEGER)";

    private static final String SQL_CREATE_INTERMEDIATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE + "(" +
            ProgressDbContract.FeedIntermediateProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedIntermediateProgressEntry.LEVEL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedIntermediateProgressEntry.HIGH_SCORE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedIntermediateProgressEntry.TIME_USED_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedIntermediateProgressEntry.STARS_OBTAINED_COLUMN + " INTEGER)";

    private static final String SQL_CREATE_EXPERT_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE + "(" +
            ProgressDbContract.FeedExpertProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedExpertProgressEntry.LEVEL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedExpertProgressEntry.HIGH_SCORE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedExpertProgressEntry.TIME_USED_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedExpertProgressEntry.STARS_OBTAINED_COLUMN + " INTEGER)";

    private static final String SQL_CREATE_EIDETIC_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE + "(" +
            ProgressDbContract.FeedEideticProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedEideticProgressEntry.LEVEL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedEideticProgressEntry.HIGH_SCORE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedEideticProgressEntry.TIME_USED_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedEideticProgressEntry.STARS_OBTAINED_COLUMN + " INTEGER)";

    private static final String SQL_CREATE_ZEN_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE + "(" +
            ProgressDbContract.FeedZenProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedZenProgressEntry.HIGH_SCORE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedZenProgressEntry.ROUNDS_COMPLETED_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedZenProgressEntry.TIME_USED_COLUMN + " INTEGER," +
            ProgressDbContract.FeedZenProgressEntry.BEST_COMBO_COLUMN + " INTEGER" + ")";

    private static final String SQL_CREATE_COLLECTIBLES_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE + "(" +
            ProgressDbContract.FeedCollectiblesEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedCollectiblesEntry.COIN_BANK + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.FREE_BREAK + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.FREE_CLEAR + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.FREE_SOLVE + " INTEGER," +
            ProgressDbContract.FeedCollectiblesEntry.FREE_SLOW + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.COINS_EARNED + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.BREAK_USAGE + " INTEGER, " +
            ProgressDbContract.FeedCollectiblesEntry.CLEAR_USAGE + " INTEGER," +
            ProgressDbContract.FeedCollectiblesEntry.SOLVE_USAGE + " INTEGER," +
            ProgressDbContract.FeedCollectiblesEntry.SLOW_USAGE + " INTEGER" + ")";

    private static final String SQL_CREATE_PERSISTENT_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE + "(" +
            ProgressDbContract.FeedPersistentProgressEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedPersistentProgressEntry.LEVEL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedPersistentProgressEntry.BEGINNER_STARS_OBTAINED + " INTEGER, " +
            ProgressDbContract.FeedPersistentProgressEntry.INTERMEDIATE_STARS_OBTAINED + " INTEGER, " +
            ProgressDbContract.FeedPersistentProgressEntry.EXPERT_STARS_OBTAINED + " INTEGER, " +
            ProgressDbContract.FeedPersistentProgressEntry.EIDETIC_STARS_OBTAINED + " INTEGER" + ")";

    private static final String SQL_CREATE_STATS_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedStatsEntry.STATS_TABLE + "(" +
            ProgressDbContract.FeedStatsEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedStatsEntry.TUTORIAL_STATE + " INTEGER, " +
            ProgressDbContract.FeedStatsEntry.TACTILE_FEEDBACK + " INTEGER, " +
            ProgressDbContract.FeedStatsEntry.SFX_VOLUME + " FLOAT, " +
            ProgressDbContract.FeedStatsEntry.SOUNDTRACK_VOLUME + " FLOAT, " +
            ProgressDbContract.FeedStatsEntry.SIGN_IN_PREFERENCE + " INTEGER" + ")";

    private static final String SQL_CREATE_ACHIEVEMENT_ENTRIES = "CREATE TABLE IF NOT EXISTS " + ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE + "(" +
            ProgressDbContract.FeedAchievementsEntry._ID + " INTEGER PRIMARY KEY, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_NAME_COLUMN + " TEXT, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_DESCRIPTION_COLUMN + " TEXT, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_GOAL_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_STATE_COLUMN + " INTEGER, " +
            ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_GOOGLE_STATE_COLUMN + " INTEGER" + ")";

    private static final String SQL_DELETE_BEGINNER_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedBeginnerProgressEntry.BEGINNER_TABLE;

    private static final String SQL_DELETE_INTERMEDIATE_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedIntermediateProgressEntry.INTERMEDIATE_TABLE;

    private static final String SQL_DELETE_EXPERT_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedExpertProgressEntry.EXPERT_TABLE;

    private static final String SQL_DELETE_EIDETIC_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedEideticProgressEntry.EIDETIC_TABLE;

    private static final String SQL_DELETE_ZEN_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedZenProgressEntry.ZEN_TABLE;

    private static final String SQL_DELETE_STATS_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedStatsEntry.STATS_TABLE;

    private static final String SQL_DELETE_COLLECTIBLES_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedCollectiblesEntry.COLLECTIBLES_TABLE;

    private static final String SQL_DELETE_PERSISTENT_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedPersistentProgressEntry.PERSISTENT_PROGRESS_TABLE;

    private static final String SQL_DELETE_ACHIEVEMENT_ENTRIES = "DROP TABLE IF EXISTS " + ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE;


    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_BEGINNER_ENTRIES);
        db.execSQL(SQL_CREATE_INTERMEDIATE_ENTRIES);
        db.execSQL(SQL_CREATE_EXPERT_ENTRIES);
        db.execSQL(SQL_CREATE_EIDETIC_ENTRIES);
        db.execSQL(SQL_CREATE_ZEN_ENTRIES);
        db.execSQL(SQL_CREATE_COLLECTIBLES_ENTRIES);
//        db.execSQL(SQL_CREATE_PERSISTENT_ENTRIES);
        db.execSQL(SQL_CREATE_STATS_ENTRIES);
        db.execSQL(SQL_CREATE_ACHIEVEMENT_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        deleteDb(db);
        onCreate(db);
    }

    void deleteDb(SQLiteDatabase db){
        db.execSQL(SQL_DELETE_BEGINNER_ENTRIES);
        db.execSQL(SQL_DELETE_INTERMEDIATE_ENTRIES);
        db.execSQL(SQL_DELETE_EXPERT_ENTRIES);
        db.execSQL(SQL_DELETE_EIDETIC_ENTRIES);
        db.execSQL(SQL_DELETE_ZEN_ENTRIES);
        db.execSQL(SQL_DELETE_STATS_ENTRIES);
        db.execSQL(SQL_DELETE_COLLECTIBLES_ENTRIES);
//        db.execSQL(SQL_DELETE_PERSISTENT_ENTRIES);
        db.execSQL(SQL_DELETE_ACHIEVEMENT_ENTRIES);
    }
}
