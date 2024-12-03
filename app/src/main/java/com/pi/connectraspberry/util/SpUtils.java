package com.pi.connectraspberry.util;

import android.content.Context;

import com.pi.connectraspberry.MyApplication;

public class SpUtils {


    public static final String PLAY_INTERVAL = "PLAY_INTERVAL";
    public static final String HUE = "HUE";
    public static final String SAT = "SAT";
    public static final String BRIGHT = "BRIGHT";
    public static final String CONTRAST = "CONTRAST";


    public static void putConfig(String key, int value) {
        MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }


    public static int getConfig(String key, int defaultValue) {
        return MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

}
