package com.pi.connectraspberry.util;

import android.content.Context;

import com.pi.connectraspberry.MyApplication;

public class SpUtils {


    public static final String PLAY_INTERVAL = "PLAY_INTERVAL";
    public static final String HUE = "HUE";
    public static final String SAT = "SAT";
    public static final String BRIGHT = "BRIGHT";
    public static final String CONTRAST = "CONTRAST";
    public static final String SWITCH = "SWITCH";

    public static void putConfig(String key, int value) {
        MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }


    public static int getConfig(String key, int defaultValue) {
        return MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).getInt(key, defaultValue);
    }


    public static void putBoolean(String key, boolean value) {
        MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return MyApplication.getInstance().getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

}
