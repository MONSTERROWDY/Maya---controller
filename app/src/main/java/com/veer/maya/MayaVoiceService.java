package com.veer.maya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MayaVoiceService extends Service {

    SpeechRecognizer recognizer;
    TextToSpeech tts;

    WindowManager windowManager;
    LinearLayout popup;
    TextView popupStatus;
    TextView popupConversation;

    boolean liveConversation = false;
    boolean popupShowing = false;
    boolean processing = false;

    Handler handler = new Handler();

    int cyan = Color.rgb(93,220,255);
    int white = Color.WHITE;
    int muted = Color.rgb(155,170,185);
    int dark = Color.rgb(5,9,15);

    @Override
    public void onCreate() {
        super.onCreate();

        createNotification();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
            }
        });

        windowManager =
            (WindowManager)getSystemService(WINDOW_SERVICE);

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
             "MAYA Live Voice is active"
         )
         .setSmallIcon(
             android.R.drawable.ic_btn_speak_now
         );

        startForeground(
            77,
            b.build()
        );
    }

    void showPopup() {

        if (popupShowing) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 23 &&
            !Settings.canDrawOverlays(this)) {

            speak(
                "MAYA popup के लिए Display over other apps permission चाहिए।"
            );

            return;
        }

        popupShowing = true;

        popup =
            new LinearLayout(this);

        popup.setOrientation(
            LinearLayout.VERTICAL
        );

        popup.setPadding(
            dp(18),
            dp(18),
            dp(18),
            dp(14)
        );

        GradientDrawable background =
            new GradientDrawable();

        background.setColor(
            Color.rgb(7,12,20)
        );

        background.setCornerRadius(
            dp(28)
        );

        background.setStroke(
            dp(1),
            Color.rgb(55,130,165)
        );

        popup.setBackground(background);

        TextView header =
            new TextView(this);

        header.setText(
            "◉  MAYA"
        );

        header.setTextColor(cyan);
        header.setTextSize(24);
        header.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        );

        popup.addView(header);

        TextView sub =
            new TextView(this);

        sub.setText(
            "AI VOICE • LIVE CONVERSATION"
        );

        sub.setTextColor(muted);
        sub.setTextSize(10);

        popup.addView(sub);

        popupStatus =
            new TextView(this);

        popupStatus.setText(
            "● LISTENING"
        );

        popupStatus.setTextColor(cyan);
        popupStatus.setTextSize(14);
        popupStatus.setPadding(
            0,
            dp(12),
            0,
            dp(8)
        );

        popup.addView(popupStatus);

        ScrollView scroll =
            new ScrollView(this);

        popupConversation =
            new TextView(this);

        popupConversation.setText(
            "MAYA\n\nहाँ, बोलिए। मैं आपके लिए क्या कर सकती हूँ?"
        );

        popupConversation.setTextColor(white);
        popupConversation.setTextSize(15);
        popupConversation.setPadding(
            dp(12),
            dp(12),
            dp(12),
            dp(12)
        );

        GradientDrawable chatBg =
            new GradientDrawable();

        chatBg.setColor(
            Color.rgb(12,20,31)
        );

        chatBg.setCornerRadius(
            dp(18)
        );

        popupConversation.setBackground(
            chatBg
        );

        scroll.addView(
            popupConversation,
            new ScrollView.LayoutParams(
                -1,
                dp(190)
            )
        );

        popup.addView(
            scroll,
            new LinearLayout.LayoutParams(
                -1,
                dp(190)
            )
        );

        Button close =
            new Button(this);

        close.setText(
            "END MAYA LIVE"
        );

        close.setTextColor(white);
        close.setAllCaps(false);
        close.setBackgroundColor(
            Color.rgb(25,35,47)
        );

        popup.addView(close);

        close.setOnClickListener(v -> {

            liveConversation = false;
            hidePopup();

            speak(
                "ठीक है Boss. मैं ready हूँ।"
            );
        });

        WindowManager.LayoutParams lp;

        if (Build.VERSION.SDK_INT >= 26) {

            lp =
                new WindowManager.LayoutParams(
                    dp(340),
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.graphics.PixelFormat.TRANSLUCENT
                );

        } else {

            lp =
                new WindowManager.LayoutParams(
                    dp(340),
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.graphics.PixelFormat.TRANSLUCENT
                );
        }

        lp.gravity = Gravity.CENTER;
        lp.y = -dp(40);

        try {

            windowManager.addView(
                popup,
                lp
            );

        } catch (Exception e) {

            popupShowing = false;
            popup = null;
        }
    }

    void hidePopup() {

        if (popup != null && popupShowing) {

            try {
                windowManager.removeView(popup);
            } catch (Exception ignored) {}
        }

        popup = null;
        popupShowing = false;
    }

    void updatePopup(
        String user,
        String maya
    ) {

        if (popupConversation == null) {
            return;
        }

        runOnMain(() -> {

            popupConversation.setText(
                "YOU\n" +
                user +
                "\n\n" +
                "MAYA\n" +
                maya
            );

            popupStatus.setText(
                "● MAYA IS SPEAKING"
            );

            popupStatus.setTextColor(
                Color.rgb(14,203,129)
            );
        });
    }

    void runOnMain(Runnable r) {
        handler.post(r);
    }

    void startListening() {

        if (processing) {
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            speak(
                "Speech recognition available नहीं है।"
            );

            return;
        }

        try {

            if (recognizer != null) {
                recognizer.destroy();
            }

            recognizer =
                SpeechRecognizer.createSpeechRecognizer(
                    this
                );

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

                        handle(
                            list.get(0)
                        );
                    }

                    @Override
                    public void onError(int e) {
                        restart();
                    }

                    @Override
                    public void onReadyForSpeech(
                        Bundle b
                    ) {

                        if (popupShowing) {

                            popupStatus.setText(
                                "● LISTENING"
                            );

                            popupStatus.setTextColor(
                                cyan
                            );
                        }
                    }

                    @Override
                    public void onBeginningOfSpeech() {}

                    @Override
                    public void onRmsChanged(float r) {}

                    @Override
                    public void onBufferReceived(
                        byte[] b
                    ) {}

                    @Override
                    public void onEndOfSpeech() {}

                    @Override
                    public void onPartialResults(
                        Bundle b
                    ) {}

                    @Override
                    public void onEvent(
                        int a,
                        Bundle b
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
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            );

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

        String original =
            heard.trim();

        String lower =
            original.toLowerCase(
                Locale.ROOT
            );

        boolean helloMaya =
            lower.contains("hello maya") ||
            lower.contains("helo maya") ||
            original.contains("हेलो माया") ||
            original.contains("हैलो माया");

        boolean mayaWord =
            lower.contains("maya") ||
            original.contains("माया");

        if (!liveConversation &&
            !helloMaya &&
            !mayaWord) {

            restart();
            return;
        }

        String text = original;

        text =
            text.replaceAll(
                "(?i)hello\\s+maya",
                ""
            );

        text =
            text.replaceAll(
                "(?i)helo\\s+maya",
                ""
            );

        text =
            text.replaceAll(
                "(?i)maya",
                ""
            );

        text =
            text.replace(
                "हेलो माया",
                ""
            );

        text =
            text.replace(
                "हैलो माया",
                ""
            );

        text =
            text.replace(
                "माया",
                ""
            )
            .trim();

        if (!liveConversation) {

            liveConversation = true;

            showPopup();

            if (text.isEmpty()) {

                speak(
                    "हाँ, बोलिए। मैं आपके लिए क्या कर सकती हूँ?"
                );

                restart();
                return;
            }
        }

        if (text.isEmpty()) {

            restart();
            return;
        }

        processing = true;

        if (popupStatus != null) {

            popupStatus.setText(
                "● THINKING"
            );

            popupStatus.setTextColor(cyan);
        }

        MayaCore.ask(
            this,
            text,
            (ok, answer) -> {

                processing = false;

                if (popupShowing) {

                    updatePopup(
                        text,
                        answer
                    );
                }

                speak(answer);

                restart();
            }
        );
    }

    void speak(String text) {

        if (tts == null) {
            return;
        }

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

        handler.postDelayed(
            this::startListening,
            900
        );
    }

    int dp(int n) {

        return (int)(
            n *
            getResources()
                .getDisplayMetrics()
                .density
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

        liveConversation = false;

        hidePopup();

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
    public android.os.IBinder onBind(
        Intent intent
    ) {
        return null;
    }
}
