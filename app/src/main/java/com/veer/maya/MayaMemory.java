package com.veer.maya;
import android.content.*;import org.json.*;
public class MayaMemory{static final String P="maya_memory",H="h";public static void add(Context c,String r,String t){try{SharedPreferences p=c.getSharedPreferences(P,0);JSONArray a=new JSONArray(p.getString(H,"[]")),b=new JSONArray();for(int i=Math.max(0,a.length()-39);i<a.length();i++)b.put(a.get(i));JSONObject o=new JSONObject();o.put("role",r);o.put("text",t);b.put(o);p.edit().putString(H,b.toString()).apply();}catch(Exception e){}}public static String get(Context c){return c.getSharedPreferences(P,0).getString(H,"[]");}}
