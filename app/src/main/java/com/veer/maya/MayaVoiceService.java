package com.veer.maya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class MayaVoiceService extends Service {

    SpeechRecognizer recognizer;
    TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotification();

        tts = new TextToSpeech(
            this,
            status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(
                        new Locale("hi","IN")
                    );
                }
            }
        );

        startListening();
    }

    void createNotification() {

        String channel = "maya_voice";

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel nc =
                new NotificationChannel(
                    channel,
                    "MAYA Voice",
                    NotificationManager.IMPORTANCE_LOW
                );

            getSystemService(
                NotificationManager.class
            ).createNotificationChannel(nc);
        }

        Notification.Builder b =
            Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, channel)
            : new Notification.Builder(this);

        b.setContentTitle("MAYA")
         .setContentText(
             "Hello Boss voice assistant active"
         )
         .setSmallIcon(
             android.R.drawable.ic_btn_speak_now
         );

        startForeground(
            77,
            b.build()
        );
    }

    void startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition available नहीं है");
            return;
        }

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(
            new RecognitionListener() {

                @Override
                public void onResults(
                    Bundle results
                ) {

                    ArrayList<String> list =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        );

                    if (list == null ||
                        list.isEmpty()) {

                        restart();
                        return;
                    }

                    handle(list.get(0));
                }

                @Override public void onError(int e) {
                    restart();
                }

                @Override public void onReadyForSpeech(Bundle b){}
                @Override public void onBeginningOfSpeech(){}
                @Override public void onRmsChanged(float r){}
                @Override public void onBufferReceived(byte[] b){}
                @Override public void onEndOfSpeech(){}
                @Override public void onPartialResults(Bundle b){}
                @Override public void onEvent(int a, Bundle b){}
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
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            false
        );

        try {
            recognizer.startListening(i);
        } catch (Exception ignored) {
            restart();
        }
    }

    void handle(String heard) {

        if (heard == null) {
            restart();
            return;
        }

        String text =
            heard.trim();

        String lower =
            text.toLowerCase(Locale.ROOT);

        boolean wake =
            lower.contains("hello boss") ||
            lower.contains("hello boss.") ||
            text.contains("हेलो बॉस") ||
            lower.contains("maya") ||
            text.contains("माया");

        if (!wake) {
            restart();
            return;
        }

        text =
            text.replaceAll(
                "(?i)hello\\s+boss",
                ""
            );

        text =
            text.replaceAll(
                "(?i)maya",
                ""
            );

        text =
            text.replace(
                "हेलो बॉस",
                ""
            );

        text =
            text.replace(
                "माया",
                ""
            ).trim();

        if (text.isEmpty()) {

            speak("Hello Boss. हाँ, बोलिए।");

            restart();
            return;
        }

        MayaCore.ask(
            this,
            text,
            (ok, answer) -> {

                speak(answer);

                restart();
            }
        );
    }

    void speak(String text) {

        if (tts == null) return;

        try {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "maya_reply"
            );
        } catch (Exception ignored) {}
    }

    void restart() {

        new android.os.Handler()
            .postDelayed(
                this::startListening,
                700
            );
    }

    @Override
    public int onStartCommand(
        Intent intent,
        int flags,
        int startId
    ) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (recognizer != null) {
            recognizer.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
