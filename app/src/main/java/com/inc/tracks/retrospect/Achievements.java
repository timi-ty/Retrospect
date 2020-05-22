package com.inc.tracks.retrospect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import java.util.ArrayList;

import static com.inc.tracks.retrospect.ActivitySplashScreen.ACHIEVEMENTS_COUNT;
import static com.inc.tracks.retrospect.ActivitySplashScreen.LEVEL_COUNT;
import static com.inc.tracks.retrospect.ActivitySplashScreen.TOTAL_LEVEL_COUNT;

class Achievements {

    static final int DON_TAP_BRONZE_BASE_SCORE = 2;
    static final int DON_TAP_SILVER_BASE_SCORE = 4;
    static final int DON_TAP_GOLD_BASE_SCORE = 6;
    static final int DON_TAP_PLATINUM_BASE_SCORE = 8;
    static final int DON_TAP_MAX_BASE_SCORE = 10;

    static final int RETROSPECT_BRONZE_BASE_SCORE = 5;
    static final int RETROSPECT_SILVER_BASE_SCORE = 10;
    static final int RETROSPECT_GOLD_BASE_SCORE = 15;
    static final int RETROSPECT_PLATINUM_BASE_SCORE = 18;
    static final int RETROSPECT_MAX_BASE_SCORE = 20;

    private static final String achievement_name0 = "TWINKLE, TWINKLE";
    private static final String achievement_name1 = "TO TAP OR NOT TO TAP";
    private static final String achievement_name2 = "REACH FOR THE STARS";
    private static final String achievement_name3 = "GALILEO'S APPRENTICE";
    private static final String achievement_name4 = "PEANUTS AND ELEPHANTS";
    private static final String achievement_name5 = "SPILL THE SHALLOWS";
    private static final String achievement_name6 = "WADDLE THE MIDDLE";
    private static final String achievement_name7 = "DUNK THE BOTTOM";
    private static final String achievement_name8 = "FOR SPARTA!";
    private static final String achievement_name9 = "BLEED AND AUTOCRACY";
    private static final String achievement_name10 = "GREY ANTI-MATTER";
    private static final String achievement_name11 = "STICKS AND STONES";
    private static final String achievement_name12 = "MINE SWEEPER";
    private static final String achievement_name13 = "QUESTION MARKS AND PRAYERS";
    private static final String achievement_name14 = "POOLS OF BUTTER";
    private static final String achievement_name15 = "CLANG WITH NO FANGS";
    private static final String achievement_name16 = "THE RICH MAN AND THE PAUPER";
    private static final String achievement_name17 = "ATLAS BALLS";
    private static final String achievement_name18 = "MAGMA OVER FLAMES";
    private static final String achievement_name19 = "EARTH METALS ARE RARE";
    private static final String achievement_name20 = "MIDAS TRIED HIS BEST";
    private static final String achievement_name21 = "ENRICH OLYMPUS";
    private static final String achievement_name22 = "NAPOLEONS CHOIR";
    private static final String achievement_name23 = "JUICE THE GOOSE";

    private static final String achievement_description0 = "Get your first star";
    private static final String achievement_description1 = "Complete the first Don\'t Tap level";
    private static final String achievement_description2 = "Get 100 stars";
    private static final String achievement_description3 = "Get 500 Stars";
    private static final String achievement_description4 = "Complete the first Eidetic Level";
    private static final String achievement_description5 = "Unlock the Intermediate difficulty";
    private static final String achievement_description6 = "Unlock the Expert difficulty";
    private static final String achievement_description7 = "Unlock the Eidetic difficulty";
    private static final String achievement_description8 = "Get a gold medal in Beginner\'s level 52";
    private static final String achievement_description9 = "Get gold medals in all level 52\'s";
    private static final String achievement_description10 = "Complete all levels";
    private static final String achievement_description11 = "Use the Break power-up 50 times";
    private static final String achievement_description12 = "Use the Clear power-up 50 times";
    private static final String achievement_description13 = "Use the Solve power-up 150 times";
    private static final String achievement_description14 = "Use the Slow power-up 50 times";
    private static final String achievement_description15 = "Accumulate 1500 coins";
    private static final String achievement_description16 = "Accumulate 5000 coins";
    private static final String achievement_description17 = "Reach a X15 combo in Zen";
    private static final String achievement_description18 = "Reach a X30 combo in Zen";
    private static final String achievement_description19 = "Get your first Platinum Medal";
    private static final String achievement_description20 = "Get 100 Gold medals or better";
    private static final String achievement_description21 = "Get 100 Platinum medals";
    private static final String achievement_description22 = "Go Platinum in 25 Eidetic levels";
    private static final String achievement_description23 = "Go Platinum in all levels";

    private AchievementsClient achievementsClient;

    static int[] getStarSummary(int gameMode, int difficulty, int level, int score, int zenRoundsCompleted) {// index 0 for number of stars, 1 for points needed to get one more star

        MainGameCoClass mainGameCoClass = new MainGameCoClass(difficulty, level, gameMode, false);
        int rounds_playable = mainGameCoClass.rounds_due;
        int card_slots = mainGameCoClass.card_slots;
        int min_points_per_card;
        int[] star_summary = new int[6];
        int base_score;
        int one_star_score;
        int two_star_score;
        int three_star_score;
        int four_star_score;

        if (gameMode == ActivityMainGame.DON_TAP) {
            //The total score depends on your average score per card click, the following algorithm is based on this fact
            final int card_scroll_loops = 5;
            one_star_score = card_slots * DON_TAP_BRONZE_BASE_SCORE * rounds_playable * card_scroll_loops;
            two_star_score = card_slots * DON_TAP_SILVER_BASE_SCORE * rounds_playable * card_scroll_loops;
            three_star_score = card_slots * DON_TAP_GOLD_BASE_SCORE * rounds_playable * card_scroll_loops;
            four_star_score = card_slots * DON_TAP_PLATINUM_BASE_SCORE * rounds_playable * card_scroll_loops;
        } else if (gameMode == ActivityMainGame.RETROSPECT) {
            //The maximum time bonus obtainable per round is 500 points, the following algorithm is based on this fact
            min_points_per_card = 1;
            base_score = card_slots * min_points_per_card * rounds_playable;
            one_star_score = base_score + (RETROSPECT_BRONZE_BASE_SCORE * rounds_playable);
            two_star_score = base_score + (RETROSPECT_SILVER_BASE_SCORE * rounds_playable);
            three_star_score = base_score + (RETROSPECT_GOLD_BASE_SCORE * rounds_playable);
            four_star_score = base_score + (RETROSPECT_PLATINUM_BASE_SCORE * rounds_playable);
        } else {
            final int ZEN_ROUNDS_FOR_ONE_STAR = 5;
            final int ZEN_ROUNDS_FOR_TWO_STARS = 20;
            final int ZEN_ROUNDS_FOR_THREE_STARS = 35;
            final int ZEN_ROUNDS_FOR_FOUR_STARS = 60;

            //The number of stars obtained for zen depends on the number of zenRoundsCompleted, *do not get confused by the "score" suffixes
            one_star_score = ZEN_ROUNDS_FOR_ONE_STAR;
            two_star_score = ZEN_ROUNDS_FOR_TWO_STARS;
            three_star_score = ZEN_ROUNDS_FOR_THREE_STARS;
            four_star_score = ZEN_ROUNDS_FOR_FOUR_STARS;

            score = zenRoundsCompleted;
        }

        if (score >= four_star_score) {
            star_summary[0] = Progress.FOUR_STARS;
            star_summary[5] = 0;
        } else if (score >= three_star_score) {
            star_summary[0] = Progress.THREE_STARS;
            star_summary[5] = four_star_score;
        } else if (score >= two_star_score) {
            star_summary[0] = Progress.TWO_STARS;
            star_summary[5] = three_star_score;
        } else if (score >= one_star_score) {
            star_summary[0] = Progress.ONE_STAR;
            star_summary[5] = two_star_score;
        } else {
            star_summary[0] = Progress.NO_STARS;
            star_summary[5] = one_star_score;
        }

        star_summary[4] = four_star_score;
        star_summary[3] = three_star_score;
        star_summary[2] = two_star_score;
        star_summary[1] = one_star_score;

        return star_summary;
    }

    static int getMedalObtained(int gameMode, int difficulty, int level, int score) {
        MainGameCoClass mainGameCoClass = new MainGameCoClass(difficulty, level, gameMode, false);
        int rounds_played = mainGameCoClass.rounds_due;
        int card_slots = mainGameCoClass.card_slots;
        int min_points_per_card;
        int medal_Rid;
        int min_score;
        int bronze_medal_score;
        int silver_medal_score;
        int gold_medal_score;
        int platinum_medal_score;

        if (gameMode == ActivityMainGame.DON_TAP) {
            //The total score depends on your average score per card click, the following algorithm is based on this fact
            final int card_scroll_loops = 5;
            bronze_medal_score = card_slots * DON_TAP_BRONZE_BASE_SCORE * rounds_played * card_scroll_loops;
            silver_medal_score = card_slots * DON_TAP_SILVER_BASE_SCORE * rounds_played * card_scroll_loops;
            gold_medal_score = card_slots * DON_TAP_GOLD_BASE_SCORE * rounds_played * card_scroll_loops;
            platinum_medal_score = card_slots * DON_TAP_PLATINUM_BASE_SCORE * rounds_played * card_scroll_loops;
        } else {
            //The maximum time bonus obtainable per round is 500 points, the following algorithm is based on this fact
            min_points_per_card = 1;
            min_score = card_slots * min_points_per_card * rounds_played;
            bronze_medal_score = min_score + (RETROSPECT_BRONZE_BASE_SCORE * rounds_played);
            silver_medal_score = min_score + (RETROSPECT_SILVER_BASE_SCORE * rounds_played);
            gold_medal_score = min_score + (RETROSPECT_GOLD_BASE_SCORE * rounds_played);
            platinum_medal_score = min_score + (RETROSPECT_PLATINUM_BASE_SCORE * rounds_played);
        }

        if (score >= platinum_medal_score) {
            medal_Rid = R.drawable.platinum_medal;
        } else if (score >= gold_medal_score) {
            medal_Rid = R.drawable.gold_medal;
        } else if (score >= silver_medal_score) {
            medal_Rid = R.drawable.silver_medal;
        } else if (score >= bronze_medal_score) {
            medal_Rid = R.drawable.bronze_medal;
        } else if (score == Progress.LOCKED_LEVEL_SCORE) {
            medal_Rid = R.drawable.locked_level;
        } else medal_Rid = R.drawable.unlocked_level_ic;

        return medal_Rid;
    }

    static String[][] getAchievementsListings() {
        String[][] list = new String[24][3];

        list[0][0] = achievement_name0;
        list[0][1] = achievement_description0;
        list[0][2] = "1";

        list[1][0] = achievement_name1;
        list[1][1] = achievement_description1;
        list[1][2] = "1";

        list[2][0] = achievement_name2;
        list[2][1] = achievement_description2;
        list[2][2] = "100";

        list[3][0] = achievement_name3;
        list[3][1] = achievement_description3;
        list[3][2] = "500";

        list[4][0] = achievement_name4;
        list[4][1] = achievement_description4;
        list[4][2] = "1";

        list[5][0] = achievement_name5;
        list[5][1] = achievement_description5;
        list[5][2] = "1";

        list[6][0] = achievement_name6;
        list[6][1] = achievement_description6;
        list[6][2] = "1";

        list[7][0] = achievement_name7;
        list[7][1] = achievement_description7;
        list[7][2] = "1";

        list[8][0] = achievement_name8;
        list[8][1] = achievement_description8;
        list[8][2] = "1";

        list[9][0] = achievement_name9;
        list[9][1] = achievement_description9;
        list[9][2] = "4";

        list[10][0] = achievement_name10;
        list[10][1] = achievement_description10;
        list[10][2] = "208";

        list[11][0] = achievement_name11;
        list[11][1] = achievement_description11;
        list[11][2] = "50";

        list[12][0] = achievement_name12;
        list[12][1] = achievement_description12;
        list[12][2] = "50";

        list[13][0] = achievement_name13;
        list[13][1] = achievement_description13;
        list[13][2] = "150";

        list[14][0] = achievement_name14;
        list[14][1] = achievement_description14;
        list[14][2] = "50";

        list[15][0] = achievement_name15;
        list[15][1] = achievement_description15;
        list[15][2] = "1500";

        list[16][0] = achievement_name16;
        list[16][1] = achievement_description16;
        list[16][2] = "5000";

        list[17][0] = achievement_name17;
        list[17][1] = achievement_description17;
        list[17][2] = "60";

        list[18][0] = achievement_name18;
        list[18][1] = achievement_description18;
        list[18][2] = "150";

        list[19][0] = achievement_name19;
        list[19][1] = achievement_description19;
        list[19][2] = "1";

        list[20][0] = achievement_name20;
        list[20][1] = achievement_description20;
        list[20][2] = "100";

        list[21][0] = achievement_name21;
        list[21][1] = achievement_description21;
        list[21][2] = "100";

        list[22][0] = achievement_name22;
        list[22][1] = achievement_description22;
        list[22][2] = "25";

        list[23][0] = achievement_name23;
        list[23][1] = achievement_description23;
        list[23][2] = "208";

        return list;
    }

    static int getAchievementImage(String achievementName, int state) {
        boolean isGotten = state == 1;
        switch (achievementName) {
            case achievement_name0:
                if (isGotten)
                    return R.drawable.exaltation;
                else return R.drawable.exaltation_blur;
            case achievement_name1:
                if (isGotten)
                    return R.drawable.medal_bronze;
                else return R.drawable.medal_blur;
            case achievement_name2:
                if (isGotten)
                    return R.drawable.telescope_silver;
                else return R.drawable.telescope_blur;
            case achievement_name3:
                if (isGotten)
                    return R.drawable.telescope_gold;
                else return R.drawable.telescope_blur;
            case achievement_name4:
                if (isGotten)
                    return R.drawable.elephant;
                else return R.drawable.elephant_blur;
            case achievement_name5:
                if (isGotten)
                    return R.drawable.karate_intermediate;
                else return R.drawable.karate_blur;
            case achievement_name6:
                if (isGotten)
                    return R.drawable.karate_expert;
                else return R.drawable.karate_blur;
            case achievement_name7:
                if (isGotten)
                    return R.drawable.karate_eidetic;
                else return R.drawable.karate_blur;
            case achievement_name8:
                if (isGotten)
                    return R.drawable.sword;
                else return R.drawable.sword_blur;
            case achievement_name9:
                if (isGotten)
                    return R.drawable.grimm;
                else return R.drawable.sword_blur;
            case achievement_name10:
                if (isGotten)
                    return R.drawable.brain_pink;
                else return R.drawable.brain_blur;
            case achievement_name11:
                if (isGotten)
                    return R.drawable.arm;
                else return R.drawable.arm_blur;
            case achievement_name12:
                if (isGotten)
                    return R.drawable.medal_bronze;
                else return R.drawable.medal_blur;
            case achievement_name13:
                if (isGotten)
                    return R.drawable.torch;
                else return R.drawable.torch_blur;
            case achievement_name14:
                if (isGotten)
                    return R.drawable.clock;
                else return R.drawable.clock_blur;
            case achievement_name15:
                if (isGotten)
                    return R.drawable.medal_gold;
                else return R.drawable.medal_blur;
            case achievement_name16:
                if (isGotten)
                    return R.drawable.honey;
                else return R.drawable.honey_blur;
            case achievement_name17:
                if (isGotten)
                    return R.drawable.helmet_silver;
                else return R.drawable.helmet_blur;
            case achievement_name18:
                if (isGotten)
                    return R.drawable.helmet_gold;
                else return R.drawable.helmet_blur;
            case achievement_name19:
                if (isGotten)
                    return R.drawable.medal_silver;
                else return R.drawable.medal_blur;
            case achievement_name20:
                if (isGotten)
                    return R.drawable.trophy_gold;
                else return R.drawable.trophy_blur;
            case achievement_name21:
                if (isGotten)
                    return R.drawable.trophy_platinum;
                else return R.drawable.trophy_blur;
            case achievement_name22:
                if (isGotten)
                    return R.drawable.brain_gold;
                else return R.drawable.brain_blur;
            case achievement_name23:
                if (isGotten)
                    return R.drawable.crown;
                else return R.drawable.crown_blur;
        }
        return 0;
    }

    static int[][] getAchievementsProgress(Context context) {
        Cursor achievementsCursor = getAchievementsCursor(context);
        int[][] ints = new int[24][4];
        for (int i = 0; i < ACHIEVEMENTS_COUNT; i++) {
            achievementsCursor.moveToNext();
            ints[i][0] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN));
            ints[i][1] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_GOAL_COLUMN));
            ints[i][2] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_STATE_COLUMN));
            ints[i][3] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_GOOGLE_STATE_COLUMN));
        }

        return ints;
    }

    private static Cursor getAchievementsCursor(Context context) {
        FeedProgressDbHelper progressDbHelper = new FeedProgressDbHelper(context);
        SQLiteDatabase readableProgressDb = progressDbHelper.getReadableDatabase();
        String[] projection1 = {
                BaseColumns._ID,
                ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN,
                ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_GOAL_COLUMN,
                ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_STATE_COLUMN,
                ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_GOOGLE_STATE_COLUMN
        };
        return readableProgressDb.query(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE, projection1, null, null, null, null, null);
    }

    private Progress aProgress;
    private ArrayList<String> achievementsObtained;
    private boolean[] isGotten;
    private boolean[] isPushed;
    private boolean[] skipCheck;
    private int[] progress;
    String[] checkForAchievements(boolean isSignedIn, Context context) {
        aProgress = new Progress(context);
        achievementsObtained = new ArrayList<>();
        isGotten = new boolean[24];
        isPushed = new boolean[24];
        skipCheck = new boolean[24];
        progress = new int[24];

        String[] names = new String[24];
        int[][] params = getAchievementsProgress(context);
        String[][] details = getAchievementsListings();

        achievementsClient = getAchievementsClient(context);

        for(int i = 0; i < ACHIEVEMENTS_COUNT; i++){
            names[i] = details[i][0];
            isGotten[i] = params[i][2] == 1;
            isPushed[i] = params[i][3] == 1;
            progress[i] = params[i][0];
            skipCheck[i] = isGotten[i] && isPushed[i];
        }

        checkFirstThird(isSignedIn, context);

        checkSecondThird(isSignedIn, context);

        checkLastThird(isSignedIn, context);

        updateAchievements(names, isGotten, isPushed, progress, context);

        aProgress.finish();

        return achievementsObtained.toArray(new String[0]);
    }
    private void checkFirstThird(boolean isSignedIn, Context context){
        if (aProgress.sumAllStars() >= 1 && !skipCheck[0]) {
            if(!isGotten[0]) achievementsObtained.add(achievement_name0);
            isGotten[0] = true;
            isPushed[0] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_twinkle_twinkle), -1);
            progress[0] = 1;
        }
        else if(!skipCheck[0]) progress[0] = 0;

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(3),
                ActivityDifficultyMenu.BEGINNER, 3);
        int beginnerLevel3Score = aProgress.highScore;
        if (getStarSummary(ActivityMainGame.getGameMode(3), ActivityDifficultyMenu.BEGINNER, 3,
                beginnerLevel3Score, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR && !skipCheck[1]) {
            if(!isGotten[1]) achievementsObtained.add(achievement_name1);
            isGotten[1] = true;
            isPushed[1] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_to_tap_or_not_to_tap), -1);
            progress[1] = 1;
        }
        else if(!skipCheck[1]) progress[1] = 0;

        if (aProgress.sumAllStars() >= 100 && !skipCheck[2]) {
            if(!isGotten[2]) achievementsObtained.add(achievement_name2);
            isGotten[2] = true;
            isPushed[2] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_reach_for_the_stars), 100);
            progress[2] = 100;
        }
        else if(!skipCheck[2]) {
            progress[2] = aProgress.sumAllStars();
            isPushed[2] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_reach_for_the_stars), progress[2]);
        }

        if (aProgress.sumAllStars() >= 500 && !skipCheck[3]) {
            if(!isGotten[3]) achievementsObtained.add(achievement_name3);
            isGotten[3] = true;
            isPushed[3] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_galileos_apprentice), 500);
            progress[3] = 500;
        }
        else if(!skipCheck[3]) {
            progress[3] = aProgress.sumAllStars();
            isPushed[3] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_galileos_apprentice), progress[3]);
        }

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(1),
                ActivityDifficultyMenu.EIDETIC, 1);
        int eideticLevel1Score = aProgress.highScore;
        if (getStarSummary(ActivityMainGame.getGameMode(1), ActivityDifficultyMenu.EIDETIC, 1,
                eideticLevel1Score, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR && !skipCheck[4]) {
            if(!isGotten[4]) achievementsObtained.add(achievement_name4);
            isGotten[4] = true;
            isPushed[4] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_peanuts_and_elephants), -1);
            progress[4] = 1;
        }
        else if(!skipCheck[4]) progress[4] = 0;

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(1),
                ActivityDifficultyMenu.INTERMEDIATE, 1);
        int intermediateLevel1Score = aProgress.highScore;
        if (intermediateLevel1Score != Progress.LOCKED_LEVEL_SCORE && !skipCheck[5]) {
            if(!isGotten[5]) achievementsObtained.add(achievement_name5);
            isGotten[5] = true;
            isPushed[5] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_spill_the_shallows), -1);
            progress[5] = 1;
        }
        else if(!skipCheck[5]) progress[5] = 0;

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(1),
                ActivityDifficultyMenu.EXPERT, 1);
        int expertLevel1Score = aProgress.highScore;
        if (expertLevel1Score != Progress.LOCKED_LEVEL_SCORE && !skipCheck[6]) {
            if(!isGotten[6]) achievementsObtained.add(achievement_name6);
            isGotten[6] = true;
            isPushed[6] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_waddle_the_middle), -1);
            progress[6] = 1;
        }
        else if(!skipCheck[6]) progress[6] = 0;

        if (eideticLevel1Score != Progress.LOCKED_LEVEL_SCORE && !skipCheck[7]) {
            if(!isGotten[7]) achievementsObtained.add(achievement_name7);
            isGotten[7] = true;
            isPushed[7] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_dunk_the_bottom), -1);
            progress[7] = 1;
        }
        else if(!skipCheck[7]) progress[7] = 0;

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(52),
                ActivityDifficultyMenu.BEGINNER, 52);
        int beginnerLevel52Score = aProgress.highScore;
        if (getStarSummary(ActivityMainGame.getGameMode(52), ActivityDifficultyMenu.BEGINNER, 52,
                beginnerLevel52Score, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS && !skipCheck[8]) {
            if(!isGotten[8]) achievementsObtained.add(achievement_name8);
            isGotten[8] = true;
            isPushed[8] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_for_sparta), -1);
            progress[8] = 1;
        }
        else if(!skipCheck[8]) progress[8] = 0;

        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(52),
                ActivityDifficultyMenu.INTERMEDIATE, 52);
        int intermediateLevel52Score = aProgress.highScore;
        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(52),
                ActivityDifficultyMenu.EXPERT, 52);
        int expertLevel52Score = aProgress.highScore;
        aProgress.extractHighScoreData(ActivityMainGame.getGameMode(52),
                ActivityDifficultyMenu.EIDETIC, 52);
        int eideticLevel52Score = aProgress.highScore;
        int count = 0;
        count = getStarSummary(ActivityMainGame.getGameMode(52), ActivityDifficultyMenu.BEGINNER, 52,
                beginnerLevel52Score, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS ? count + 1 : count;
        count = getStarSummary(ActivityMainGame.getGameMode(52), ActivityDifficultyMenu.INTERMEDIATE, 52,
                intermediateLevel52Score, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS ? count + 1 : count;
        count = getStarSummary(ActivityMainGame.getGameMode(52), ActivityDifficultyMenu.EXPERT, 52,
                expertLevel52Score, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS ? count + 1 : count;
        count = getStarSummary(ActivityMainGame.getGameMode(52), ActivityDifficultyMenu.EIDETIC, 52,
                eideticLevel52Score, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS ? count + 1 : count;
        if (count >= 4 && !skipCheck[9]) {
            if(!isGotten[9]) achievementsObtained.add(achievement_name9);
            isGotten[9] = true;
            isPushed[9] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_bleed_and_autocracy), 4);
            progress[9] = 4;
        }
        else if(!skipCheck[9]){
            progress[9] = count;
            isPushed[9] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_bleed_and_autocracy), progress[9]);
        }

        int levelsCompleted = 0;
        for(int i = 1; i <= LEVEL_COUNT; i++){
            int gameMode = ActivityMainGame.getGameMode(i);
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.BEGINNER, i);
            int beginnerLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.INTERMEDIATE, i);
            int intermediateLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.EXPERT, i);
            int expertLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.EIDETIC, i);
            int eideticLevelScore = aProgress.highScore;


            boolean beginnerComplete = getStarSummary(gameMode, ActivityDifficultyMenu.BEGINNER, i,
                    beginnerLevelScore, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR;
            boolean intermediateComplete = getStarSummary(gameMode, ActivityDifficultyMenu.INTERMEDIATE, i,
                    intermediateLevelScore, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR;
            boolean expertComplete = getStarSummary(gameMode, ActivityDifficultyMenu.EXPERT, i,
                    expertLevelScore, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR;
            boolean eideticComplete = getStarSummary(gameMode, ActivityDifficultyMenu.EIDETIC, i,
                    eideticLevelScore, Progress.NOT_ZEN)[0] >= Progress.ONE_STAR;

            levelsCompleted = beginnerComplete ? levelsCompleted + 1 : levelsCompleted;
            levelsCompleted = intermediateComplete ? levelsCompleted + 1 : levelsCompleted;
            levelsCompleted = expertComplete ? levelsCompleted + 1 : levelsCompleted;
            levelsCompleted = eideticComplete ? levelsCompleted + 1 : levelsCompleted;
        }
        if (levelsCompleted >= TOTAL_LEVEL_COUNT && !skipCheck[10]) {
            if(!isGotten[10]) achievementsObtained.add(achievement_name10);
            isGotten[10] = true;
            isPushed[10] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_grey_antimatter), TOTAL_LEVEL_COUNT);
            progress[10] = TOTAL_LEVEL_COUNT;
        }
        else if(!skipCheck[10]){
            progress[10] = levelsCompleted;
            isPushed[10] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_grey_antimatter), progress[10]);
        }

        if(aProgress.getCollectibleStats(Progress.BREAK) >= 50 && !skipCheck[11]){
            if(!isGotten[11]) achievementsObtained.add(achievement_name11);
            isGotten[11] = true;
            isPushed[11] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_sticks_and_stones), 50);
            progress[11] = 50;
        }
        else if(!skipCheck[11]){
            progress[11] = aProgress.getCollectibleStats(Progress.BREAK);
            isPushed[11] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_sticks_and_stones), progress[11]);
        }

        if(aProgress.getCollectibleStats(Progress.CLEAR) >= 50 && !skipCheck[12]){
            if(!isGotten[12]) achievementsObtained.add(achievement_name12);
            isGotten[12] = true;
            isPushed[12] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_mine_sweeper), 50);
            progress[12] = 50;
        }
        else if(!skipCheck[12]){
            progress[12] = aProgress.getCollectibleStats(Progress.CLEAR);
            isPushed[12] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_mine_sweeper), progress[12]);
        }
    }
    private void checkSecondThird(boolean isSignedIn, Context context){
        if(aProgress.getCollectibleStats(Progress.SOLVE) >= 150 && !skipCheck[13]){
            if(!isGotten[13]) achievementsObtained.add(achievement_name13);
            isGotten[13] = true;
            isPushed[13] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_questionmarks_and_prayers), 50);
            progress[13] = 150;
        }
        else if(!skipCheck[13]){
            progress[13] = aProgress.getCollectibleStats(Progress.SOLVE);
            isPushed[13] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_questionmarks_and_prayers), progress[13]);
        }

        if(aProgress.getCollectibleStats(Progress.SLOW) >= 50 && !skipCheck[14]){
            if(!isGotten[14]) achievementsObtained.add(achievement_name14);
            isGotten[14] = true;
            isPushed[14] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_pools_of_butter), 50);
            progress[14] = 50;
        }
        else if(!skipCheck[14]){
            progress[14] = aProgress.getCollectibleStats(Progress.SLOW);
            isPushed[14] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_pools_of_butter), progress[14]);
        }

        if(aProgress.getCollectibleStats(Progress.COINS) >= 1500 && !skipCheck[15]){
            if(!isGotten[15]) achievementsObtained.add(achievement_name15);
            isGotten[15] = true;
            isPushed[15] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_clang_with_no_fangs), 1500);
            progress[15] = 1500;
        }
        else if(!skipCheck[15]){
            progress[15] = aProgress.getCollectibleStats(Progress.COINS);
            isPushed[15] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_clang_with_no_fangs), progress[15]);
        }

        if(aProgress.getCollectibleStats(Progress.COINS) >= 5000 && !skipCheck[16]){
            if(!isGotten[16]) achievementsObtained.add(achievement_name16);
            isGotten[16] = true;
            isPushed[16] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_the_rich_man_and_the_pauper), 5000);
            progress[16] = 5000;
        }
        else if(!skipCheck[16]){
            progress[16] = aProgress.getCollectibleStats(Progress.COINS);
            isPushed[16] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_the_rich_man_and_the_pauper), progress[16]);
        }

        aProgress.extractHighScoreData(ActivityMainGame.ZEN, 1, 1);
        if(aProgress.pBestCombo >= 60 && !skipCheck[17]){
            if(!isGotten[17]) achievementsObtained.add(achievement_name17);
            isGotten[17] = true;
            isPushed[17] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_atlas_balls), -1);
            progress[17] = 60;
        }
        else if(!skipCheck[17]) progress[17] = aProgress.pBestCombo;

        if(aProgress.pBestCombo >= 150 && !skipCheck[18]){
            if(!isGotten[18]) achievementsObtained.add(achievement_name18);
            isGotten[18] = true;
            isPushed[18] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_magma_over_flames), -1);
            progress[18] = 150;
        }
        else if(!skipCheck[18]) progress[18] = aProgress.pBestCombo;

    }
    private void checkLastThird(boolean isSignedIn, Context context){
        int eideticPlatinumCount = 0;
        int platinumCount = 0;
        int goldCount = 0;
        for(int i = 1; i <= LEVEL_COUNT; i++){
            int gameMode = ActivityMainGame.getGameMode(i);
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.BEGINNER, i);
            int beginnerLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.INTERMEDIATE, i);
            int intermediateLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.EXPERT, i);
            int expertLevelScore = aProgress.highScore;
            aProgress.extractHighScoreData(gameMode,
                    ActivityDifficultyMenu.EIDETIC, i);
            int eideticLevelScore = aProgress.highScore;


            boolean beginnerGold = getStarSummary(gameMode, ActivityDifficultyMenu.BEGINNER, i,
                    beginnerLevelScore, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS;
            boolean intermediateGold = getStarSummary(gameMode, ActivityDifficultyMenu.INTERMEDIATE, i,
                    intermediateLevelScore, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS;
            boolean expertGold = getStarSummary(gameMode, ActivityDifficultyMenu.EXPERT, i,
                    expertLevelScore, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS;
            boolean eideticGold = getStarSummary(gameMode, ActivityDifficultyMenu.EIDETIC, i,
                    eideticLevelScore, Progress.NOT_ZEN)[0] >= Progress.THREE_STARS;

            boolean beginnerPlatinum = getStarSummary(gameMode, ActivityDifficultyMenu.BEGINNER, i,
                    beginnerLevelScore, Progress.NOT_ZEN)[0] >= Progress.FOUR_STARS;
            boolean intermediatePlatinum = getStarSummary(gameMode, ActivityDifficultyMenu.INTERMEDIATE, i,
                    intermediateLevelScore, Progress.NOT_ZEN)[0] >= Progress.FOUR_STARS;
            boolean expertPlatinum = getStarSummary(gameMode, ActivityDifficultyMenu.EXPERT, i,
                    expertLevelScore, Progress.NOT_ZEN)[0] >= Progress.FOUR_STARS;
            boolean eideticPlatinum = getStarSummary(gameMode, ActivityDifficultyMenu.EIDETIC, i,
                    eideticLevelScore, Progress.NOT_ZEN)[0] >= Progress.FOUR_STARS;

            goldCount = beginnerGold ? goldCount + 1 : goldCount;
            goldCount = intermediateGold ? goldCount + 1 : goldCount;
            goldCount = expertGold ? goldCount + 1 : goldCount;
            goldCount = eideticGold ? goldCount + 1 : goldCount ;

            platinumCount = beginnerPlatinum ? platinumCount + 1 : platinumCount;
            platinumCount = intermediatePlatinum ? platinumCount + 1 : platinumCount;
            platinumCount = expertPlatinum ? platinumCount + 1 : platinumCount;
            platinumCount = eideticPlatinum ? platinumCount + 1 : platinumCount;

            eideticPlatinumCount = eideticPlatinum ? eideticPlatinumCount + 1 : eideticPlatinumCount;
        }
        if(platinumCount >= 1 && !skipCheck[19]){
            if(!isGotten[19]) achievementsObtained.add(achievement_name19);
            isGotten[19] = true;
            isPushed[19] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_earth_metals_are_rare), -1);
            progress[19] = 1;
        }
        else if(!skipCheck[19]) progress[19] = 0;

        if(goldCount >= 100 && !skipCheck[20]){
            if(!isGotten[20]) achievementsObtained.add(achievement_name20);
            isGotten[20] = true;
            isPushed[20] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_midas_tried_his_best), 100);
            progress[20] = 100;
        }
        else if(!skipCheck[20]){
            progress[20] = goldCount;
            isPushed[20] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_midas_tried_his_best), progress[20]);
        }

        if(platinumCount >= 100 && !skipCheck[21]){
            if(!isGotten[21]) achievementsObtained.add(achievement_name21);
            isGotten[21] = true;
            isPushed[21] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_enrich_olympus), 100);
            progress[21] = 100;
        }
        else if(!skipCheck[21]){
            progress[21] = platinumCount;
            isPushed[21] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_enrich_olympus), progress[21]);
        }

        if(eideticPlatinumCount >= 25 && !skipCheck[22]){
            if(!isGotten[22]) achievementsObtained.add(achievement_name22);
            isGotten[22] = true;
            isPushed[22] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_napoleons_choir), 25);
            progress[22] = 25;
        }
        else if(!skipCheck[22]){
            progress[22] = eideticPlatinumCount;
            isPushed[22] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_napoleons_choir), progress[22]);
        }

        if(platinumCount >= TOTAL_LEVEL_COUNT && !skipCheck[23]){
            if(!isGotten[23]) achievementsObtained.add(achievement_name23);
            isGotten[23] = true;
            isPushed[23] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_juice_the_goose), TOTAL_LEVEL_COUNT);
            progress[23] = TOTAL_LEVEL_COUNT;
        }
        else if(!skipCheck[23]){
            progress[23] = platinumCount;
            isPushed[23] = pushAchievementToPlayGames(isSignedIn,
                    context.getString(R.string.achievement_juice_the_goose), progress[23]);
        }
    }

    private boolean pushAchievementToPlayGames(boolean isSignedIn, final String achievementKey, int steps){
        if(isSignedIn && achievementsClient != null) {
            if (steps < 0)
                achievementsClient.unlock(achievementKey);
            else achievementsClient.setSteps(achievementKey, steps);
            return true;
        }
        return false;
    }

    private AchievementsClient getAchievementsClient(Context context){
       GoogleSignInAccount achievementsAccount =
               GoogleSignIn.getLastSignedInAccount(context);
        AchievementsClient achievementsClient = null;
       if(achievementsAccount != null)
           achievementsClient = Games.getAchievementsClient(context, achievementsAccount);
       return achievementsClient;
    }

    private static void updateAchievements(String[] names, boolean[] isGotten, boolean[] isPushed, int[] progress, Context context){
        FeedProgressDbHelper progressDbHelper = new FeedProgressDbHelper(context);
        SQLiteDatabase writableProgressDb = progressDbHelper.getReadableDatabase();
        ContentValues progressValues = new ContentValues();

        for(int i = 0; i < ACHIEVEMENTS_COUNT; i++) {
            String selection1 = ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_NAME_COLUMN + " = ?";
            String[] selectionArgs1 = {names[i]};
            int state = isGotten[i] ? 1 : 0;
            int pushed = isPushed[i] ? 1 : 0;

            progressValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_STATE_COLUMN, state);
            progressValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_GOOGLE_STATE_COLUMN, pushed);
            progressValues.put(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN, progress[i]);
            writableProgressDb.update(ProgressDbContract.FeedAchievementsEntry.ACHIEVEMENTS_TABLE, progressValues, selection1, selectionArgs1);
        }
    }
}