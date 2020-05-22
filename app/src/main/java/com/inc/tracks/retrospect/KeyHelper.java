package com.inc.tracks.retrospect;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

class KeyHelper {

    private String one, two, three, four;

    void takeOne(String s){
        final String oneKey = "6#Gt/.A0ItBh:'";
        char[] dst = new char[s.length()];
        ArrayList<Character> std = new ArrayList<>();
        s.getChars(0, s.length(), dst, 0);
        for(int i = 0; i < dst.length; i++){
            std.add(dst[i]);
            std.add(oneKey.charAt(i % oneKey.length()));
        }
        one = std.toString();
    }

    String getOne(){
        StringBuilder s = new StringBuilder();
        for(int i = 1; i < one.length(); i+=3){
            if(i % 2 != 0) s.append(one.charAt(i));
        }
        return s.toString();
    }

    void takeTwo(String s){
        final String twoKey = "R5J)jUy6DF@1";
        char[] dst = new char[s.length()];
        ArrayList<Character> std = new ArrayList<>();
        s.getChars(0, s.length(), dst, 0);
        for(int i = 0; i < dst.length; i++){
            std.add(dst[i]);
            std.add(twoKey.charAt(i % twoKey.length()));
        }
        two = std.toString();
    }

    String getTwo(){
        StringBuilder s = new StringBuilder();
        for(int i = 1; i < two.length(); i+=3){
            if(i % 2 != 0) s.append(two.charAt(i));
        }
        return s.toString();
    }

    void takeThree(String s){
        final String threeKey = ">?lkjHJ*%$WDyhv";
        char[] dst = new char[s.length()];
        ArrayList<Character> std = new ArrayList<>();
        s.getChars(0, s.length(), dst, 0);
        for(int i = 0; i < dst.length; i++){
            std.add(dst[i]);
            std.add(threeKey.charAt(i % threeKey.length()));
        }
        three = std.toString();
    }

    String getThree(){
        StringBuilder s = new StringBuilder();
        for(int i = 1; i < three.length(); i+=3){
            if(i % 2 != 0) s.append(three.charAt(i));
        }
        return s.toString();
    }

    void takeFour(String s){
        final String fourKey = "P(*65R^&LfyjV";
        char[] dst = new char[s.length()];
        ArrayList<Character> std = new ArrayList<>();
        s.getChars(0, s.length(), dst, 0);
        for(int i = 0; i < dst.length; i++){
            std.add(dst[i]);
            std.add(fourKey.charAt(i % fourKey.length()));
        }
        four = std.toString();
    }

    String getFour(){
        StringBuilder s = new StringBuilder();
        for(int i = 1; i < four.length(); i+=3){
            if(i % 2 != 0) s.append(four.charAt(i));
        }
        return s.toString();
    }
}
