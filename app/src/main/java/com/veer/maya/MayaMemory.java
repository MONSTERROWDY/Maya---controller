package com.veer.maya;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public final class MayaMemory {

    private static final String PREFS = "maya_memory";
    private static final String HISTORY = "history";
    private static final int MAX = 60;

    private MayaMemory(){}

    public static synchronized void add(
            Context context,
            String role,
            String text
    ){
        try{

            SharedPreferences p =
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

            JSONArray old =
                    new JSONArray(p.getString(HISTORY, "[]"));

            JSONArray out = new JSONArray();

            int start =
                    Math.max(0, old.length() - MAX + 1);

            for(int i=start;i<old.length();i++){
                out.put(old.get(i));
            }

            JSONObject item = new JSONObject();

            item.put("role", role);
            item.put("text", text == null ? "" : text);

            out.put(item);

            p.edit()
                    .putString(HISTORY, out.toString())
                    .apply();

        }catch(Exception ignored){}
    }

    public static String get(Context context){

        return context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(HISTORY, "[]");
    }

    public static void clear(Context context){

        context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(HISTORY)
                .apply();
    }
}
