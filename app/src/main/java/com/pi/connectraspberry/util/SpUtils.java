package com.pi.connectraspberry.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pi.connectraspberry.MyApplication;

import java.lang.ref.PhantomReference;
import java.util.Locale;

import javax.crypto.interfaces.PBEKey;

public class SpUtils {


    private static final String TAG = "SpUtils";
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

    public static void setLanguagePreference(String languageCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language_code", languageCode);
        editor.apply();
    }


    public static String getLanguagePreference() {

        Locale defaultLocale = Locale.getDefault();
        String languageCode = defaultLocale.getLanguage();
        Log.d(TAG, "系统默认语言代码: " + languageCode);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        return preferences.getString("language_code", languageCode);
    }

}
