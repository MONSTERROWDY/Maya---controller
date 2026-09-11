package com.veer.maya;

import android.content.Context;
import android.content.SharedPreferences;

public class MayaMemory {

    private static final String PREF = "maya_memory";

    public static void save(Context c, String key, String value) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static String get(Context c, String key) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(key, "");
    }

    public static void clear(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
