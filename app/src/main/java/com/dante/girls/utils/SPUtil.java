package com.dante.girls.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dante.girls.base.App;

/**
 * Helps with shared preference.
 */
public class SPUtil {

    private static SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.context);
    private static SharedPreferences.Editor editor = sp.edit();

    public static void save(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public static void save(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void save(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public static String get(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static String getString(String key) {
        return sp.getString(key, "");
    }

    public static int getInt(String key) {
        return sp.getInt(key, 0);
    }

    public static boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }
}
