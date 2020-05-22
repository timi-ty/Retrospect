package com.inc.tracks.retrospect;

import android.provider.BaseColumns;

final class ProgressDbContract {
    private ProgressDbContract(){}

    /* Inner classes that defines the tables' contents */
    static class FeedBeginnerProgressEntry implements BaseColumns{
        static final String BEGINNER_TABLE = "beginner_progress";
        static final String LEVEL_COLUMN = "level";
        static final String HIGH_SCORE_COLUMN = "high_score";
        static final String TIME_USED_COLUMN = "time_used";
        static final String STARS_OBTAINED_COLUMN = "stars_obtained";
    }

    static class FeedIntermediateProgressEntry implements BaseColumns{
        static final String INTERMEDIATE_TABLE = "intermediate_progress";
        static final String LEVEL_COLUMN = "level";
        static final String HIGH_SCORE_COLUMN = "high_score";
        static final String TIME_USED_COLUMN = "time_used";
        static final String STARS_OBTAINED_COLUMN = "stars_obtained";
    }

    static class FeedExpertProgressEntry implements BaseColumns{
        static final String EXPERT_TABLE = "expert_progress";
        static final String LEVEL_COLUMN = "level";
        static final String HIGH_SCORE_COLUMN = "high_score";
        static final String TIME_USED_COLUMN = "time_used";
        static final String STARS_OBTAINED_COLUMN = "stars_obtained";
    }

    static class FeedEideticProgressEntry implements BaseColumns{
        static final String EIDETIC_TABLE = "eidetic_progress";
        static final String LEVEL_COLUMN = "level";
        static final String HIGH_SCORE_COLUMN = "high_score";
        static final String TIME_USED_COLUMN = "time_used";
        static final String STARS_OBTAINED_COLUMN = "stars_obtained";
    }

    static class FeedZenProgressEntry implements BaseColumns{
        static final String ZEN_TABLE = "zen_progress";
        static final String HIGH_SCORE_COLUMN = "high_score";
        static final String ROUNDS_COMPLETED_COLUMN = "rounds_completed";
        static final String TIME_USED_COLUMN = "time_used";
        static final String BEST_COMBO_COLUMN = "best_combo";
    }

    static class FeedPersistentProgressEntry implements BaseColumns{
        static final String PERSISTENT_PROGRESS_TABLE = "persistent_progress";
        static final String LEVEL_COLUMN = "level";
        static final String BEGINNER_STARS_OBTAINED = "beginner_stars";
        static final String INTERMEDIATE_STARS_OBTAINED = "intermediate_stars";
        static final String EXPERT_STARS_OBTAINED = "expert_stars";
        static final String EIDETIC_STARS_OBTAINED = "eidetic_stars";
    }

     static class FeedCollectiblesEntry implements BaseColumns{
        static final String COLLECTIBLES_TABLE = "collectibles_table";
        static final String COIN_BANK = "coins";
        static final String FREE_BREAK = "free_break";
        static final String FREE_CLEAR = "free_clear";
        static final String FREE_SOLVE = "free_solve";
        static final String FREE_SLOW = "free_slow";
        static final String COINS_EARNED = "coins_earned";
        static final String BREAK_USAGE = "break_usage";
        static final String CLEAR_USAGE = "clear_usage";
        static final String SOLVE_USAGE = "solve_usage";
        static final String SLOW_USAGE = "slow_usage";
    }

    static class FeedStatsEntry implements BaseColumns{
        static final String STATS_TABLE = "stats_table";
        static final String TUTORIAL_STATE = "play_tutorial";
        static final String TACTILE_FEEDBACK = "tactile_feedback";
        static final String SFX_VOLUME = "sfx_volume";
        static final String SOUNDTRACK_VOLUME = "soundtrack_volume";
        static final String SIGN_IN_PREFERENCE = "sign_in_preference";
    }

    static class FeedAchievementsEntry implements BaseColumns{
        static final String ACHIEVEMENTS_TABLE = "achievements";
        static final String ACHIEVEMENT_NAME_COLUMN = "achievement_name";
        static final String ACHIEVEMENT_DESCRIPTION_COLUMN = "achievement_description";
        static final String ACHIEVEMENT_PARAMETER_PROGRESS_COLUMN = "achievement_parameter_progress";
        static final String ACHIEVEMENT_PARAMETER_GOAL_COLUMN = "achievement_parameter_goal";
        static final String ACHIEVEMENT_STATE_COLUMN = "achievement_state";
        static final String ACHIEVEMENT_GOOGLE_STATE_COLUMN = "achievement_google_state";
    }
}
