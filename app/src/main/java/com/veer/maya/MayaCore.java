package com.veer.maya;

import android.content.*;
import android.net.Uri;
import android.os.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class MayaCore {
    private static final String PREFS="maya_ai";
    private static final String KEY="openai_key";
    private static final String PENDING="pending_actions";
    private static final ExecutorService EX=Executors.newSingleThreadExecutor();
    private MayaCore(){}

    public static String getKey(Context c){return c.getSharedPreferences(PREFS,0).getString(KEY,"");}
    public static void saveKey(Context c,String k){c.getSharedPreferences(PREFS,0).edit().putString(KEY,k.trim()).apply();}

    public static void process(Context c,String user){
        Context a=c.getApplicationContext();
        if(user==null||user.trim().isEmpty())return;
        MayaMemory.add(a,"user",user);
        if(getKey(a).isEmpty()){reply(a,"पहले MAYA में OpenAI API key save करें।");return;}
        EX.execute(() -> callAI(a,user.trim()));
    }

    private static void callAI(Context c,String user){
        try{
            JSONObject q=new JSONObject();
            q.put("model","gpt-5.6-luna");
            q.put("instructions", systemPrompt());
            q.put("input","Conversation memory:\n"+MayaMemory.get(c)+"\n\nUser:\n"+user);
            HttpURLConnection h=(HttpURLConnection)new URL("https://api.openai.com/v1/responses").openConnection();
            h.setRequestMethod("POST"); h.setConnectTimeout(20000); h.setReadTimeout(60000);
            h.setRequestProperty("Authorization","Bearer "+getKey(c)); h.setRequestProperty("Content-Type","application/json"); h.setDoOutput(true);
            try(OutputStream os=h.getOutputStream()){os.write(q.toString().getBytes(StandardCharsets.UTF_8));}
            int code=h.getResponseCode();
            InputStream is=code>=400?h.getErrorStream():h.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
            StringBuilder sb=new StringBuilder(); String line; while((line=br.readLine())!=null)sb.append(line);
            if(code>=400){ reply(c,"OpenAI error "+code+". API key/model या quota check करें।"); return; }
            String text=extractText(new JSONObject(sb.toString()));
            JSONObject out;
            try{out=new JSONObject(text);}catch(Exception e){out=new JSONObject();out.put("say",text);out.put("memory",new JSONArray());out.put("actions",new JSONArray());}
            String say=out.optString("say","ठीक है।");
            JSONArray mem=out.optJSONArray("memory"); if(mem!=null)for(int i=0;i<mem.length();i++)MayaMemory.add(c,"memory",mem.optString(i));
            JSONArray actions=out.optJSONArray("actions"); if(actions==null)actions=new JSONArray();
            JSONArray high=new JSONArray(); JSONArray safe=new JSONArray();
            for(int i=0;i<actions.length();i++){
                JSONObject ac=actions.getJSONObject(i); String t=ac.optString("type");
                if(t.equals("dial")||t.equals("message")||t.equals("send_message")||t.equals("purchase")||t.equals("delete"))high.put(ac); else safe.put(ac);
            }
            run(c,safe);
            if(high.length()>0){savePending(c,high);reply(c,say+"\nयह action करने से पहले आपकी confirmation चाहिए। MAYA में Confirm दबाएँ या बोलें: MAYA confirm.");}
            else reply(c,say);
        }catch(Exception e){reply(c,"MAYA में connection/problem आया। Internet, API key और model access check करें।");}
    }

    private static String systemPrompt(){
        return "You are MAYA, a practical Android personal assistant. Answer in concise Hindi/Hinglish unless user uses another language. Return ONLY valid JSON with keys say (string), memory (array of short strings), actions (array). Allowed action objects: {type:'open_app',name}; {type:'tap',text}; {type:'type',text}; {type:'scroll',direction:'up'|'down'}; {type:'back'}; {type:'home'}; {type:'recent'}; {type:'web_search',query}; {type:'dial',number}; {type:'message',number,text}. Use multiple actions in sequence when needed. Never claim an action succeeded unless the local controller can execute it. dial/message and destructive/purchase actions require confirmation. Do not invent contact numbers. For general questions use say and no actions. Keep say short and useful.";
    }

    private static String extractText(JSONObject r){
        try{JSONArray out=r.optJSONArray("output");if(out!=null)for(int i=0;i<out.length();i++){JSONArray c=out.getJSONObject(i).optJSONArray("content");if(c!=null)for(int j=0;j<c.length();j++){JSONObject x=c.getJSONObject(j);if("output_text".equals(x.optString("type")))return x.optString("text");}}}catch(Exception ignored){}
        return "{\"say\":\"Response नहीं मिला\",\"memory\":[],\"actions\":[]}";
    }

    private static void run(Context c,JSONArray a){Handler h=new Handler(Looper.getMainLooper());for(int i=0;i<a.length();i++){final JSONObject x=a.optJSONObject(i);final long d=i*700L;h.postDelayed(()->{if(x!=null)one(c,x);},d);}}
    private static void one(Context c,JSONObject a){try{String t=a.optString("type");MayaAccessibilityService s=MayaAccessibilityService.getInstance();
        if("open_app".equals(t)&&s!=null)s.executeCommand("open "+a.optString("name"));
        else if("tap".equals(t)&&s!=null)s.executeCommand("tap "+a.optString("text"));
        else if("type".equals(t)&&s!=null)s.executeCommand("type "+a.optString("text"));
        else if("scroll".equals(t)&&s!=null)s.executeCommand("scroll "+a.optString("direction"));
        else if(("back".equals(t)||"home".equals(t)||"recent".equals(t))&&s!=null)s.executeCommand(t);
        else if("web_search".equals(t)){Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(a.optString("query"))));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);c.startActivity(i);}
    }catch(Exception ignored){}}

    private static void savePending(Context c,JSONArray a){c.getSharedPreferences(PREFS,0).edit().putString(PENDING,a.toString()).apply();}
    public static void confirmPending(Context c){String raw=c.getSharedPreferences(PREFS,0).getString(PENDING,"");if(raw.isEmpty()){reply(c,"कोई pending action नहीं है।");return;}c.getSharedPreferences(PREFS,0).edit().remove(PENDING).apply();try{run(c,new JSONArray(raw));reply(c,"Confirmed. Action शुरू कर रही हूँ।");}catch(Exception e){reply(c,"Pending action पढ़ नहीं पाई।");}}
    public static void cancelPending(Context c){c.getSharedPreferences(PREFS,0).edit().remove(PENDING).apply();reply(c,"ठीक है, action cancel कर दिया।");}
    public static boolean hasPending(Context c){return !c.getSharedPreferences(PREFS,0).getString(PENDING,"").isEmpty();}

    private static void reply(Context c,String s){MayaMemory.add(c,"assistant",s);Intent i=new Intent("com.veer.maya.REPLY");i.setPackage(c.getPackageName());i.putExtra("text",s);c.sendBroadcast(i);MayaVoiceService.say(s);}
}
