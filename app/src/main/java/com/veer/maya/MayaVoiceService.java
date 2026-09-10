package com.veer.maya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class MayaVoiceService extends Service {

    public static MayaVoiceService me;

    private SpeechRecognizer sr;
    private TextToSpeech tts;

    private final Handler h =
            new Handler(Looper.getMainLooper());

    private boolean listening;
    private boolean liveMode;

    public static void start(Context c) {

        Intent i =
                new Intent(
                        c,
                        MayaVoiceService.class
                );

        if(Build.VERSION.SDK_INT >= 26)
            c.startForegroundService(i);
        else
            c.startService(i);
    }

    public static void stop(Context c) {
        c.stopService(
                new Intent(
                        c,
                        MayaVoiceService.class
                )
        );
    }

    public static void setLiveMode(
            Context c,
            boolean on
    ) {

        c.getSharedPreferences(
                "maya_voice",
                0
        ).edit()
                .putBoolean("live",on)
                .apply();

        if(me != null)
            me.liveMode=on;
    }

    public static boolean isLiveMode(Context c) {

        return c.getSharedPreferences(
                "maya_voice",
                0
        ).getBoolean(
                "live",
                false
        );
    }

    public static void say(String s) {

        if(me != null &&
                me.tts != null &&
                s != null &&
                !s.isEmpty()) {

            try {
                me.tts.speak(
                        s,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "maya"
                );
            } catch(Exception ignored) {}
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        me=this;
        liveMode=isLiveMode(this);

        if(Build.VERSION.SDK_INT >= 26) {

            NotificationChannel ch =
                    new NotificationChannel(
                            "maya_voice",
                            "MAYA Voice",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager nm =
                    getSystemService(
                            NotificationManager.class
                    );

            if(nm != null)
                nm.createNotificationChannel(ch);
        }

        Notification n;

        if(Build.VERSION.SDK_INT >= 26) {

            n =
                    new Notification.Builder(
                            this,
                            "maya_voice"
                    )
                    .setContentTitle(
                            "MAYA is active"
                    )
                    .setContentText(
                            "MAYA voice assistant"
                    )
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build();

        } else {

            n =
                    new Notification.Builder(this)
                    .setContentTitle(
                            "MAYA is active"
                    )
                    .setContentText(
                            "MAYA voice assistant"
                    )
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build();
        }

        try {

            if(Build.VERSION.SDK_INT >= 29) {

                startForeground(
                        7,
                        n,
                        ServiceInfo
                                .FOREGROUND_SERVICE_TYPE_MICROPHONE
                );

            } else {

                startForeground(
                        7,
                        n
                );
            }

        } catch(Exception e) {

            startForeground(
                    7,
                    n
            );
        }

        tts =
                new TextToSpeech(
                        this,
                        status -> {

                            try {
                                if(status ==
                                        TextToSpeech.SUCCESS) {

                                    int r =
                                            tts.setLanguage(
                                                    new Locale(
                                                            "hi",
                                                            "IN"
                                                    )
                                            );

                                    if(r ==
                                            TextToSpeech.LANG_MISSING_DATA ||
                                            r ==
                                            TextToSpeech.LANG_NOT_SUPPORTED) {

                                        tts.setLanguage(
                                                Locale.getDefault()
                                        );
                                    }
                                }
                            } catch(Exception ignored) {}
                        }
                );

        if(checkSelfPermission(
                "android.permission.RECORD_AUDIO"
        ) == PackageManager.PERMISSION_GRANTED) {

            listen();
        }
    }

    private void listen() {

        if(listening)
            return;

        if(checkSelfPermission(
                "android.permission.RECORD_AUDIO"
        ) != PackageManager.PERMISSION_GRANTED)
            return;

        listening=true;

        try {

            if(sr != null)
                sr.destroy();

        } catch(Exception ignored) {}

        sr =
                SpeechRecognizer
                        .createSpeechRecognizer(this);

        sr.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onResults(
                            android.os.Bundle b
                    ) {

                        listening=false;

                        ArrayList<String> a =
                                b.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if(a != null &&
                                !a.isEmpty()) {

                            handle(
                                    a.get(0)
                            );
                        }

                        h.postDelayed(
                                () -> listen(),
                                liveMode ? 300 : 900
                        );
                    }

                    @Override
                    public void onError(int e) {

                        listening=false;

                        h.postDelayed(
                                () -> listen(),
                                liveMode ? 400 : 1100
                        );
                    }

                    public void onReadyForSpeech(
                            android.os.Bundle b
                    ) {}

                    public void onBeginningOfSpeech() {}

                    public void onRmsChanged(float r) {}

                    public void onBufferReceived(
                            byte[] b
                    ) {}

                    public void onEndOfSpeech() {}

                    public void onPartialResults(
                            android.os.Bundle b
                    ) {}

                    public void onEvent(
                            int a,
                            android.os.Bundle b
                    ) {}
                }
        );

        Intent i =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );

        i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "hi-IN"
        );

        i.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        try {
            sr.startListening(i);
        } catch(Exception e) {

            listening=false;

            h.postDelayed(
                    () -> listen(),
                    1200
            );
        }
    }

    private void handle(String heard) {

        if(heard == null)
            return;

        String x=heard.trim();

        if(x.isEmpty())
            return;

        String lower =
                x.toLowerCase(Locale.US);

        boolean wake =
                lower.contains("maya") ||
                x.contains("माया") ||
                x.contains("माइया") ||
                x.contains("मैया");

        if(!liveMode && !wake)
            return;

        if(wake) {

            x=x.replaceAll(
                    "(?i)\\bmaya\\b",
                    " "
            );

            x=x.replace(
                    "माया",
                    " "
            );

            x=x.replace(
                    "माइया",
                    " "
            );

            x=x.replace(
                    "मैया",
                    " "
            );

            x=x.trim();
        }

        if(x.isEmpty()) {

            say(
                    "हाँ boss, बोलिए।"
            );

            return;
        }

        String low =
                x.toLowerCase(Locale.US);

        if(
                low.equals("confirm") ||
                low.equals("yes") ||
                low.equals("हाँ") ||
                low.equals("हा") ||
                low.contains("कर दो") ||
                low.contains("कर दीजिए")
        ) {

            MayaCore.confirmPending(this);
            return;
        }

        if(
                low.equals("cancel") ||
                low.equals("no") ||
                low.equals("नहीं") ||
                low.contains("मत करो")
        ) {

            MayaCore.cancelPending(this);
            return;
        }

        MayaCore.process(
                this,
                x
        );
    }

    @Override
    public int onStartCommand(
            Intent i,
            int flags,
            int id
    ) {

        liveMode=isLiveMode(this);

        if(!listening)
            listen();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        listening=false;

        try {
            if(sr != null)
                sr.destroy();
        } catch(Exception ignored) {}

        try {
            if(tts != null)
                tts.shutdown();
        } catch(Exception ignored) {}

        me=null;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }
}
