package com.inc.tracks.retrospect;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class MainGameCoClass{

    int[] ord_index, ran_index, shuffled_slots;
    int time, dwell_time, time_allowed, extra_time, rounds_due, card_slots, flip_duration, flash_interval, shrink_duration;
    ArrayList<Integer> card_index;
    private int lower_boundary, upper_boundary, setTime;
    private double zenSpeed;
    private Random random;

    MainGameCoClass(int difficulty, int level, int gameMode, boolean inTutorial) {

        random = new Random();
        card_slots = (2 * difficulty) + 1;
        ord_index = new int[card_slots];
        ran_index = new int[card_slots];
        shuffled_slots = new int[card_slots];

        if (level <= 4) {
            lower_boundary = 52;
            upper_boundary = 74;
        }
        if (level >= 5 && level <= 8) {
            lower_boundary = 39;
            upper_boundary = 52;
        }
        if (level >= 9 && level <= 12) {
            lower_boundary = 26;
            upper_boundary = 39;
        }
        if (level >= 13 && level <= 16) {
            lower_boundary = 13;
            upper_boundary = 26;
        }
        if (level >= 17 && level <= 20) {
            lower_boundary = 0;
            upper_boundary = 13;
        }
        if (level >= 21 && level <= 24) {
            lower_boundary = 26;
            upper_boundary = 52;
        }
        if (level >= 25 && level <= 28) {
            lower_boundary = 13;
            upper_boundary = 39;
        }
        if (level >= 29 && level <= 32) {
            lower_boundary = 0;
            upper_boundary = 26;
        }
        if (level >= 33 && level <= 36) {
            lower_boundary = 39;
            upper_boundary = 74;
        }
        if (level >= 37 && level <= 40) {
            lower_boundary = 26;
            upper_boundary = 74;
        }
        if (level >= 41 && level <= 44) {
            lower_boundary = 13;
            upper_boundary = 52;
        }
        if (level >= 45 && level <= 48) {
            lower_boundary = 0;
            upper_boundary = 39;
        }
        if (level >= 49 && level <= 51) {
            lower_boundary = 13;
            upper_boundary = 74;
        }
        if (level == 52) {
            lower_boundary = 0;
            upper_boundary = 74;
        }
        if(((level % 3) - 2 == 0) && !(((level % 4) - 3) == 0)){
            lower_boundary = 74;
            upper_boundary = 104;
        }
        if(level == 11 || level == 23 || level == 35 || level == 47){
            lower_boundary = 74;
            upper_boundary = 104;
        }

        setClassicGameSpeed(level, gameMode, inTutorial);
    }

    void updateForZen(int rounds_played, int gameMode){
        int difficulty, level;

        if(rounds_played<5){
            difficulty = 1;
            level = 5 + random.nextInt(10);
        }
        else if(rounds_played<10){
            difficulty = 1;
            level = 10 + random.nextInt(15);
        }
        else if(rounds_played<15){
            difficulty = 2;
            level = 1 + random.nextInt(5);
            if(rounds_played == 10)
                level = 2;
        }
        else if(rounds_played<20){
            difficulty = 1;
            level = 15 + random.nextInt(20);
        }
        else if(rounds_played<25){
            difficulty = 1;
            level = 20 + random.nextInt(25);
        }
        else if(rounds_played<30){
            difficulty = 2;
            level = 5 + random.nextInt(10);
            if(rounds_played == 25)
                level = 4;
        }
        else if(rounds_played<35){
            difficulty = 1;
            level = 25 + random.nextInt(30);
        }
        else if(rounds_played<40){
            difficulty = 1;
            level = 30 + random.nextInt(35);
        }
        else if(rounds_played<45){
            difficulty = 2;
            level = 10 + random.nextInt(15);
            if(rounds_played == 40)
                level = 6;
        }
        else if(rounds_played<50){
            difficulty = 1;
            level = 35 + random.nextInt(40);
        }
        else if(rounds_played<55){
            difficulty = 1;
            level = 40 + random.nextInt(45);
        }
        else if(rounds_played<60){
            difficulty = 2;
            level = 15 + random.nextInt(20);
            if(rounds_played == 55)
                level = 8;
        }
        else if(rounds_played<65){
            difficulty = 1;
            level = 45 + random.nextInt(40);
        }
        else if(rounds_played<70){
            difficulty = 1;
            level = 50 + random.nextInt(55);
        }
        else if(rounds_played<75){
            difficulty = 2;
            level = 20 + random.nextInt(25);
            if(rounds_played == 70)
                level = 10;
        }
        else if(rounds_played<80){
            difficulty = 2;
            level = 25 + random.nextInt(30);
        }
        else if(rounds_played<85){
            difficulty = 3;
            level = 1 + random.nextInt(5);
        }
        else if(rounds_played<90){
            difficulty = 2;
            level = 30 + random.nextInt(35);
            if(rounds_played == 85)
                level = 12;
        }
        else if(rounds_played<95){
            difficulty = 2;
            level = 35 + random.nextInt(40);
        }
        else if(rounds_played<100){
            difficulty = 3;
            level = 5 + random.nextInt(10);
        }
        else if(rounds_played<105){
            difficulty = 2;
            level = 40 + random.nextInt(45);
            if(rounds_played == 100)
                level = 14;
        }
        else if(rounds_played<110){
            difficulty = 2;
            level = 45 + random.nextInt(50);
        }
        else if(rounds_played<115){
            difficulty = 3;
            level = 10 + random.nextInt(15);
        }
        else if(rounds_played<120){
            difficulty = 2;
            level = 50 + random.nextInt(55);
            if(rounds_played == 115)
                level = 16;
        }
        else if(rounds_played<125){
            difficulty = 1;
            level = 55 + random.nextInt(60);
        }
        else if(rounds_played<130){
            difficulty = 3;
            level = 15 + random.nextInt(20);
        }
        else if(rounds_played<135){
            difficulty = 3;
            level = 20 + random.nextInt(25);
            if(rounds_played == 130)
                level = 2;
        }
        else if(rounds_played<140){
            difficulty = 4;
            level = 1 + random.nextInt(5);
        }
        else if(rounds_played<145){
            difficulty = 3;
            level = 25 + random.nextInt(30);
        }
        else if(rounds_played<150){
            difficulty = 3;
            level = 30 + random.nextInt(35);
            if(rounds_played == 145)
                level = 4;
        }
        else if(rounds_played<155){
            difficulty = 4;
            level = 5 + random.nextInt(10);
        }
        else if(rounds_played<160){
            difficulty = 3;
            level = 35 + random.nextInt(40);
        }
        else if(rounds_played<165){
            difficulty = 3;
            level = 40 + random.nextInt(45);
            if(rounds_played == 160)
                level = 6;
        }
        else if(rounds_played<170){
            difficulty = 4;
            level = 10 + random.nextInt(15);
        }
        else if(rounds_played<175){
            difficulty = 3;
            level = 45 + random.nextInt(50);
        }
        else if(rounds_played<180){
            difficulty = 3;
            level = 50 + random.nextInt(55);
            if(rounds_played == 175)
                level = 8;
        }
        else if(rounds_played<185){
            difficulty = 4;
            level = 15 + random.nextInt(20);
        }
        else if(rounds_played<190){
            difficulty = 4;
            level = 20 + random.nextInt(25);
        }
        else if(rounds_played<195){
            difficulty = 2;
            level = 55 + random.nextInt(60);
            if(rounds_played == 190)
                level = 10;
        }
        else if(rounds_played<200){
            difficulty = 4;
            level = 25 + random.nextInt(30);
        }
        else if(rounds_played<205){
            difficulty = 4;
            level = 30 + random.nextInt(35);
        }
        else if(rounds_played<210){
            difficulty = 3;
            level = 55 + random.nextInt(60);
            if(rounds_played == 205)
                level = 10;
        }
        else if(rounds_played<215){
            difficulty = 4;
            level = 35 + random.nextInt(40);
        }
        else{
            difficulty = 4;
            level = 40 + random.nextInt(45);
            if(((rounds_played % 15) - 10) == 0)
                level = 12;
        }

        if(gameMode == ActivityMainGame.DON_TAP){
            if(rounds_played <= 10)
                level = 2;
            else if(rounds_played <= 25)
                level = 4;
            else if(rounds_played <= 40)
                level = 6;
            else if(rounds_played <= 55)
                level = 8;
            else if(rounds_played <= 70)
                level = 10;
            else if(rounds_played <= 85)
                level = 12;
            else if(rounds_played <= 100)
                level = 14;
            else if(rounds_played <= 115)
                level = 16;
            else if(rounds_played <= 130)
                level = 2;
            else if(rounds_played <= 145)
                level = 4;
            else if(rounds_played <= 160)
                level = 6;
            else if(rounds_played <= 190)
                level = 10;
            else if(rounds_played <= 205)
                level = 10;
            else
                level = 12;
        }

        card_slots = (2 * difficulty) + 1;
        ord_index = new int[card_slots];
        ran_index = new int[card_slots];
        shuffled_slots = new int[card_slots];
        rounds_due = -11;
        setZenGameSpeed(level);

        if (level <= 4) {
            lower_boundary = 52;
            upper_boundary = 74;
        }
        else if (level <= 8) {
            lower_boundary = 39;
            upper_boundary = 52;
        }
        else if (level <= 12) {
            lower_boundary = 26;
            upper_boundary = 39;
        }
        else if (level <= 16) {
            lower_boundary = 13;
            upper_boundary = 26;
        }
        else if (level <= 20) {
            lower_boundary = 0;
            upper_boundary = 13;
        }
        else if (level <= 24) {
            lower_boundary = 26;
            upper_boundary = 52;
        }
        else if (level <= 28) {
            lower_boundary = 13;
            upper_boundary = 39;
        }
        else if (level <= 32) {
            lower_boundary = 0;
            upper_boundary = 26;
        }
        else if (level <= 36) {
            lower_boundary = 39;
            upper_boundary = 74;
        }
        else if (level <= 40) {
            lower_boundary = 26;
            upper_boundary = 74;
        }
        else if (level <= 44) {
            lower_boundary = 13;
            upper_boundary = 52;
        }
        else if (level <= 48) {
            lower_boundary = 0;
            upper_boundary = 39;
        }
        else if (level <= 51) {
            lower_boundary = 13;
            upper_boundary = 74;
        }
        else if (level == 52) {
            lower_boundary = 0;
            upper_boundary = 74;
        }

        if((gameMode != ActivityMainGame.RETROSPECT || (difficulty == 1 && rounds_played >= 15) ||
                (difficulty == 2 && rounds_played >= 55) || (difficulty == 3 && rounds_played >= 105)
                || (difficulty == 4 && rounds_played >= 165)) &&
                ((level % 3) - 2 == 0) && !(((level % 4) - 3) == 0)){
            lower_boundary = 74;
            upper_boundary = 104;
        }

        if((gameMode != ActivityMainGame.RETROSPECT || (difficulty == 1 && rounds_played >= 10) ||
                (difficulty == 2 && rounds_played >= 55) || (difficulty == 3 && rounds_played >= 105)
                || (difficulty == 4 && rounds_played >= 165)) &&
                (level == 11 || level == 23 || level == 35 || level == 47)){
            lower_boundary = 74;
            upper_boundary = 104;
        }

        if(time >= 450){
            flip_duration = 200;
            shrink_duration = 180;
            flash_interval = 300;
        }
        else if(time >= 350){
            flip_duration = 160;
            shrink_duration = 150;
            flash_interval = 250;
        }
        else if(time >= 200){
            flip_duration = 120;
            shrink_duration = 120;
            flash_interval = 200;
        }
        else if(time >= 150){
            flip_duration = 80;
            shrink_duration = 90;
            flash_interval = 150;
        }
    }

    void indexGen(){
        card_index = new ArrayList<>();
        ArrayList<Integer> shuffled_list = new ArrayList<>();
        ArrayList<Integer> active_card_index = new ArrayList<>();
        for(int j = lower_boundary; j<upper_boundary; j++) {
            card_index.add(j);
        }
        Collections.shuffle(card_index);
        for(int j = 0; j<card_slots; j++) {
            ran_index[j] = card_index.get(j);
        }
        for(int j = 0; j<card_slots; j++) {
            active_card_index.add(ran_index[j]);
        }
        Collections.shuffle(active_card_index);
        for(int j = 0; j<card_slots; j++) {
            ord_index[j] = active_card_index.get(j);
        }
        for(int j = 0; j<card_slots; j++) {
            shuffled_list.add(j);
        }
        Collections.shuffle(shuffled_list);
        for(int j = 0; j<card_slots; j++) {
            shuffled_slots[j] = shuffled_list.get(j);
        }
    }

    void scrollControl() {
        try {
            Thread.sleep(time, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setControl() {
        try {
            Thread.sleep(setTime, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void slowDown(){
        time = time * 3;
        dwell_time = dwell_time * 3;
        flip_duration = flip_duration * 3;
        flash_interval = flash_interval * 2;
        shrink_duration = shrink_duration * 3;
        setTime = setTime * 3;
    }

    void resetGameSpeed(int level, int gameMode, boolean inTutorial){
        if(gameMode == ActivityMainGame.ZEN){
            setZenGameSpeed();
        }
        else setClassicGameSpeed(level, gameMode, inTutorial);
    }

    private void switchLevelForSpeed(int level){
        switch(level){
            case 1:
                time = 500;
                setTime = 500;
                time_allowed = 500 * card_slots;
                rounds_due = 1;
                break;
            case 2:
                time = 460;
                setTime = 570;
                time_allowed = 450 * card_slots;
                rounds_due = 3;
                break;
            case 3:
                time = 440;
                setTime = 550;
                time_allowed = 425 * card_slots;
                rounds_due = 2;
                break;
            case 4:
                time = 560;
                setTime = 470;
                time_allowed = 385 * card_slots;
                rounds_due = 5;
                break;
            case 5:
                time = 520;
                setTime = 540;
                time_allowed = 330 * card_slots;
                rounds_due = 3;
                break;
            case 6:
                time = 550;
                setTime = 520;
                time_allowed = 320 * card_slots;
                rounds_due = 2;
                break;
            case 7:
                time = 530;
                setTime = 450;
                time_allowed = 330 * card_slots;
                rounds_due = 3;
                break;
            case 8:
                time = 500;
                setTime = 390;
                time_allowed = 300 * card_slots;
                rounds_due = 4;
                break;

            case 9:
                time = 430;
                setTime = 320;
                time_allowed = 330 * card_slots;
                rounds_due = 3;
                break;
            case 10:
                time = 430;
                setTime = 320;
                time_allowed = 290 * card_slots;
                rounds_due = 7;
                break;
            case 11:
                time = 390;
                setTime = 390;
                time_allowed = 320 * card_slots;
                rounds_due = 5;
                break;
            case 12:
                time = 450;
                setTime = 350;
                time_allowed = 300 * card_slots;
                rounds_due = 9;
                break;
            case 13:
                time = 430;
                setTime = 385;
                time_allowed = 315 * card_slots;
                rounds_due = 5;
                break;
            case 14:
                time = 550;
                setTime = 305;
                time_allowed = 280 * card_slots;
                rounds_due = 6;
                break;
            case 15:
                time = 515;
                setTime = 270;
                time_allowed = 295 * card_slots;
                rounds_due = 8;
                break;
            case 16:
                time = 500;
                setTime = 250;
                time_allowed = 285 * card_slots;
                rounds_due = 4;
                break;
            case 17:
                time = 440;
                setTime = 145;
                time_allowed = 330 * card_slots;
                rounds_due = 5;
                break;
            case 18:
                time = 350;
                setTime = 205;
                time_allowed = 300 * card_slots;
                rounds_due = 7;
                break;
            case 19:
                time = 330;
                setTime = 240;
                time_allowed = 285 * card_slots;
                rounds_due = 11;
                break;
            case 20:
                time = 330;
                setTime = 240;
                time_allowed = 285 * card_slots;
                rounds_due = 7;
                break;
            case 21:
                time = 295;
                setTime = 205;
                time_allowed = 300 * card_slots;
                rounds_due = 9;
                break;
            case 22:
                time = 255;
                setTime = 165;
                time_allowed = 280 * card_slots;
                rounds_due = 12;
                break;
            case 23:
                time = 215;
                setTime = 235;
                time_allowed = 250 * card_slots;
                rounds_due = 9;
                break;
            case 24:
                time = 335;
                setTime = 155;
                time_allowed = 210 * card_slots;
                rounds_due = 5;
                break;
            case 25:
                time = 195;
                setTime = 100;
                time_allowed = 270 * card_slots;
                rounds_due = 3;
                break;
            case 26:
                time = 225;
                setTime = 80;
                time_allowed = 260 * card_slots;
                rounds_due = 6;
                break;
            case 27:
                time = 290;
                setTime = 190;
                time_allowed = 290 * card_slots;
                rounds_due = 7;
                break;
            case 28:
                time = 380;
                setTime = 130;
                time_allowed = 230 * card_slots;
                rounds_due = 8;
                break;
            case 29:
                time = 340;
                setTime = 200;
                time_allowed = 200 * card_slots;
                rounds_due = 5;
                break;
            case 30:
                time = 270;
                setTime = 130;
                time_allowed = 180  * card_slots;
                rounds_due = 3;
                break;
            case 31:
                time = 250;
                setTime = 165;
                time_allowed = 165 * card_slots;
                rounds_due = 5;
                break;
            case 32:
                time = 310;
                setTime = 125;
                time_allowed = 145 * card_slots;
                rounds_due = 3;
                break;
            case 33:
                time = 135;
                setTime = 200;
                time_allowed = 220 * card_slots;
                rounds_due = 7;
                break;
            case 34:
                time = 255;
                setTime = 120;
                time_allowed = 180 * card_slots;
                rounds_due = 5;
                break;
            case 35:
                time = 215;
                setTime = 190;
                time_allowed = 150 * card_slots;
                rounds_due = 3;
                break;
            case 36:
                time = 245;
                setTime = 170;
                time_allowed = 140 * card_slots;
                rounds_due = 5;
                break;
            case 37:
                time = 165;
                setTime = 205;
                time_allowed = 125 * card_slots;
                rounds_due = 3;
                break;
            case 38:
                time = 255;
                setTime = 145;
                time_allowed = 100 * card_slots;
                rounds_due = 4;
                break;
            case 39:
                time = 150;
                setTime = 90;
                time_allowed = 160 *  card_slots;
                rounds_due = 5;
                break;
            case 40:
                time = 170;
                setTime = 125;
                time_allowed = 145 * card_slots;
                rounds_due = 2;
                break;
            case 41:
                time = 150;
                setTime = 195;
                time_allowed = 115 * card_slots;
                rounds_due = 5;
                break;
            case 42:
                time = 210;
                setTime = 155;
                time_allowed = 100 * card_slots;
                rounds_due = 3;
                break;
            case 43:
                time = 190;
                setTime = 180;
                time_allowed = 115 * card_slots;
                rounds_due = 6;
                break;
            case 44:
                time = 250;
                setTime = 100;
                time_allowed = 100 * card_slots;
                rounds_due = 5;
                break;
            case 45:
                time = 145;
                setTime = 80;
                time_allowed = 145 * card_slots;
                rounds_due = 4;
                break;
            case 46:
                time = 175;
                setTime = 60;
                time_allowed = 135 * card_slots;
                rounds_due = 8;
                break;
            case 47:
                time = 135;
                setTime = 130;
                time_allowed = 105 * card_slots;
                rounds_due = 5;
                break;
            case 48:
                time = 145;
                setTime = 110;
                time_allowed = 110 * card_slots;
                rounds_due = 4;
                break;
            case 49:
                time = 110;
                setTime = 75;
                time_allowed = 125 * card_slots;
                rounds_due = 7;
                break;
            case 50:
                time = 130;
                setTime = 90;
                time_allowed = 115 * card_slots;
                rounds_due = 5;
                break;
            case 51:
                time = 150;
                setTime = 70;
                time_allowed = 120 * card_slots;
                rounds_due = 9;
                break;
            case 52:
                time = 110;
                setTime = 50;
                time_allowed = 100 * card_slots;
                rounds_due = 16;
                break;
        }
    }
    private void setClassicGameSpeed(int level, int gameMode, boolean inTutorial){

        switchLevelForSpeed(level);

        if(gameMode == ActivityMainGame.DON_TAP){
            final double oldRange = 350.0;
            final double newRange = 200.0;
            final double min = 150.0;
            final double newMin = 300.0;

            time = (int) Math.rint(newMin + (((time - min)/oldRange) * newRange));
        }

        if(upper_boundary > 74 && gameMode == ActivityMainGame.RETROSPECT){
            time = (int) Math.rint(time * 1.5);
            setTime = (int) Math.rint(setTime * 1.5);
            time_allowed = (int) Math.rint(time_allowed * 1.5);
        }

        extra_time = time_allowed / 10;
        dwell_time = (int) Math.rint(2.5 * time);

        if(inTutorial){
            time = time * 2;
            setTime = (int) Math.rint(setTime * 1.5);
            time_allowed = time_allowed * 5;
            dwell_time = (int) Math.rint(3.5 * time);
        }


        if(time >= 450){
            flip_duration = 200;
            shrink_duration = 180;
            flash_interval = 600;
        }
        else if(time >= 350){
            flip_duration = 160;
            shrink_duration = 150;
            flash_interval = 500;
        }
        else if(time >= 200){
            flip_duration = 120;
            shrink_duration = 120;
            flash_interval = 200;
        }
        else if(time >= 150){
            flip_duration = 80;
            shrink_duration = 90;
            flash_interval = 300;
        }
        if(gameMode == ActivityMainGame.ZEN){
            rounds_due = -11;
        }
    }

    private void setZenGameSpeed(int level){
        zenSpeed = 1 + (level * 0.1);
        time = (int) Math.rint(150 * (zenSpeed + 4.0) / zenSpeed);
        setTime = (int) Math.rint(time*(0.25 + (0.5 * random.nextDouble())));

        time_allowed = (int) Math.rint(card_slots * time);
        extra_time = time_allowed / 10;
        dwell_time = (int) Math.rint(2.5 * time);
    }
    private void setZenGameSpeed(){
        time = (int) Math.rint(150 * (zenSpeed + 4.0) / zenSpeed);
        setTime = (int) Math.rint(time*(0.25 + (0.5 * random.nextDouble())));

        time_allowed = (int) Math.rint(card_slots * time);
        extra_time = time_allowed / 10;
        dwell_time = (int) Math.rint(2.5 * time);
    }

}