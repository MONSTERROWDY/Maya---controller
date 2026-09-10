package com.veer.maya;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import java.util.*;

public class MayaVoiceService extends Service {
    static MayaVoiceService me;
    SpeechRecognizer sr;
    TextToSpeech tts;
    Handler h = new Handler(Looper.getMainLooper());
    boolean listening;
    boolean liveMode;

    public static void start(Context c) {
        Intent i = new Intent(c, MayaVoiceService.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i); else c.startService(i);
    }
    public static void stop(Context c) { c.stopService(new Intent(c, MayaVoiceService.class)); }
    public static void setLiveMode(Context c, boolean on) {
        c.getSharedPreferences("maya_voice",0).edit().putBoolean("live",on).apply();
        if(me!=null) me.liveMode=on;
    }
    public static boolean isLiveMode(Context c) { return c.getSharedPreferences("maya_voice",0).getBoolean("live",false); }
    public static void say(String s) { if(me!=null && me.tts!=null && s!=null) me.tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "maya"); }

    @Override public void onCreate() {
        super.onCreate(); me=this; liveMode=isLiveMode(this);
        NotificationChannel ch = new NotificationChannel("maya_voice","MAYA Voice",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this,"maya_voice")
                .setContentTitle("MAYA is active")
                .setContentText(liveMode ? "Live conversation mode" : "Wake word mode")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now).setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(7,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE); else startForeground(7,n);
        tts = new TextToSpeech(this, status -> { try { tts.setLanguage(Locale.getDefault()); } catch(Exception ignored) {} });
        if (checkSelfPermission("android.permission.RECORD_AUDIO") == PackageManager.PERMISSION_GRANTED) listen();
    }

    private void listen() {
        if (listening) return;
        if (checkSelfPermission("android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) return;
        listening=true;
        try { if(sr!=null) sr.destroy(); } catch(Exception ignored) {}
        sr=SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new RecognitionListener() {
            public void onResults(Bundle b) {
                listening=false;
                ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(a!=null&&!a.isEmpty()) handle(a.get(0));
                h.postDelayed(() -> listen(), liveMode ? 250 : 700);
            }
            public void onError(int e){ listening=false; h.postDelayed(() -> listen(), liveMode ? 350 : 1000); }
            public void onReadyForSpeech(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float r){}
            public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onPartialResults(Bundle b){} public void onEvent(int a,Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().toLanguageTag());
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
        try { sr.startListening(i); } catch(Exception e){ listening=false; h.postDelayed(() -> listen(),1200); }
    }

    private void handle(String heard) {
        if (heard == null) return;
        String x=heard.trim();
        if(x.isEmpty()) return;
        String l=x.toLowerCase(Locale.US);
        boolean wake=l.contains("maya") || x.contains("माया");
        if (!liveMode && !wake) return;
        if(wake) x=x.replaceAll("(?i)\\bmaya\\b"," ").replace("माया"," ").trim();
        if(x.isEmpty()){say("हाँ, बोलिए");return;}
        MayaCore.process(this,x);
    }

    @Override public int onStartCommand(Intent i,int f,int id){
        liveMode=isLiveMode(this);
        return START_STICKY;
    }
    @Override public void onDestroy(){ listening=false; try{if(sr!=null)sr.destroy();}catch(Exception ignored){} if(tts!=null)tts.shutdown(); me=null; super.onDestroy(); }
    @Override public IBinder onBind(Intent i){return null;}
}
