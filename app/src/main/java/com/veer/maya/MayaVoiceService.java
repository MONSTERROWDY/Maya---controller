package com.veer.maya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
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

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;

    private WindowManager wm;
    private LinearLayout popup;
    private TextView popupStatus;
    private TextView popupChat;

    private boolean listening = false;
    private boolean liveConversation = false;
    private boolean processing = false;
    private boolean popupShowing = false;

    private final int cyan =
            Color.rgb(0, 229, 255);

    private boolean enabled() {
        return getSharedPreferences(
                "maya_settings",
                MODE_PRIVATE
        ).getBoolean(
                "maya_enabled",
                true
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!enabled()) {
            stopSelf();
            return;
        }

        createChannel();

        startForeground(
                77,
                buildNotification()
        );

        tts = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        try {
                            tts.setLanguage(
                                    new Locale(
                                            "hi",
                                            "IN"
                                    )
                            );
                        } catch (Exception ignored) {
                        }
                    }
                }
        );

        if (SpeechRecognizer.isRecognitionAvailable(this)) {

            recognizer =
                    SpeechRecognizer
                            .createSpeechRecognizer(this);

            recognizer.setRecognitionListener(
                    new RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(
                                Bundle params
                        ) {
                            setStatus(
                                    "● LISTENING"
                            );
                        }

                        @Override
                        public void onBeginningOfSpeech() {
                            setStatus(
                                    "● LISTENING"
                            );
                        }

                        @Override
                        public void onRmsChanged(
                                float rmsdB
                        ) {
                        }

                        @Override
                        public void onBufferReceived(
                                byte[] buffer
                        ) {
                        }

                        @Override
                        public void onEndOfSpeech() {
                            setStatus(
                                    "● UNDERSTANDING"
                            );
                        }

                        @Override
                        public void onError(
                                int error
                        ) {

                            listening = false;

                            if (enabled() &&
                                    liveConversation &&
                                    !processing) {

                                restart();
                            }
                        }

                        @Override
                        public void onResults(
                                Bundle results
                        ) {

                            listening = false;

                            ArrayList<String> data =
                                    results.getStringArrayList(
                                            SpeechRecognizer
                                                    .RESULTS_RECOGNITION
                                    );

                            if (data == null ||
                                    data.isEmpty()) {

                                if (liveConversation) {
                                    restart();
                                }

                                return;
                            }

                            handleSpeech(
                                    data.get(0)
                            );
                        }

                        @Override
                        public void onPartialResults(
                                Bundle partialResults
                        ) {
                        }

                        @Override
                        public void onEvent(
                                int eventType,
                                Bundle params
                        ) {
                        }
                    }
            );
        }
    }

    private Notification buildNotification() {

        if (Build.VERSION.SDK_INT >= 26) {

            return new Notification.Builder(
                    this,
                    "maya_voice"
            )
                    .setContentTitle("MAYA")
                    .setContentText(
                            "MAYA Voice Agent is active"
                    )
                    .setSmallIcon(
                            android.R.drawable
                                    .ic_btn_speak_now
                    )
                    .build();
        }

        return new Notification.Builder(this)
                .setContentTitle("MAYA")
                .setContentText(
                        "MAYA Voice Agent is active"
                )
                .setSmallIcon(
                        android.R.drawable
                                .ic_btn_speak_now
                )
                .build();
    }

    private void createChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel =
                    new NotificationChannel(
                            "maya_voice",
                            "MAYA Voice",
                            NotificationManager
                                    .IMPORTANCE_LOW
                    );

            NotificationManager nm =
                    getSystemService(
                            NotificationManager.class
                    );

            if (nm != null) {
                nm.createNotificationChannel(
                        channel
                );
            }
        }
    }

    private void handleSpeech(String raw) {

        if (!enabled()) {
            stopSelf();
            return;
        }

        String text =
                raw == null
                        ? ""
                        : raw.trim();

        if (text.isEmpty()) {

            if (liveConversation) {
                restart();
            }

            return;
        }

        String lower =
                text.toLowerCase(
                        Locale.US
                );

        boolean helloMaya =
                lower.contains("hello maya") ||
                lower.contains("helo maya") ||
                lower.contains("हेलो माया") ||
                lower.contains("हैलो माया");

        boolean helloBoss =
                lower.contains("hello boss") ||
                lower.contains("हेलो बॉस");

        boolean mayaWake =
                lower.equals("maya") ||
                lower.equals("माया");

        if (helloMaya ||
                helloBoss ||
                mayaWake) {

            showPopup();

            speak(
                    helloBoss
                            ? "Hello Boss. हाँ, बोलिए।"
                            : "हाँ बॉस, क्या करना है?"
            );

            liveConversation = true;

            restart();

            return;
        }

        if (!liveConversation) {
            return;
        }

        if (processing) {
            return;
        }

        processing = true;

        setStatus(
                "● EXECUTING"
        );

        boolean local =
                handleLocalAction(
                        text
                );

        if (local) {

            processing = false;

            updatePopup(
                    text,
                    "Done. Local phone action executed."
            );

            speak(
                    "Done, Boss."
            );

            restart();

            return;
        }

        /*
         * Only unknown commands reach AI.
         * Phone actions do not waste API quota.
         */

        setStatus(
                "● AI THINKING"
        );

        MayaCore.ask(
                this,
                text,
                (ok, answer) -> {

                    processing = false;

                    new android.os.Handler(
                            getMainLooper()
                    ).post(() -> {

                        String response =
                                answer == null
                                        ? "MAYA could not respond."
                                        : answer;

                        updatePopup(
                                text,
                                response
                        );

                        speak(response);

                        restart();
                    });
                }
        );
    }

    private boolean handleLocalAction(
            String text
    ) {

        String lower =
                text.toLowerCase(
                        Locale.US
                );

        /*
         * HOME
         */

        if (containsAny(
                lower,
                "home",
                "go home",
                "होम"
        )) {

            MayaCore.executeLocalCommand(
                    this,
                    "home"
            );

            return true;
        }

        /*
         * BACK
         */

        if (containsAny(
                lower,
                "back",
                "go back",
                "पीछे"
        )) {

            MayaCore.executeLocalCommand(
                    this,
                    "back"
            );

            return true;
        }

        /*
         * RECENTS
         */

        if (containsAny(
                lower,
                "recent",
                "recents",
                "recent apps"
        )) {

            MayaCore.executeLocalCommand(
                    this,
                    "recent"
            );

            return true;
        }

        /*
         * NOTIFICATIONS
         */

        if (containsAny(
                lower,
                "notification",
                "notifications",
                "नोटिफिकेशन"
        )) {

            MayaCore.executeLocalCommand(
                    this,
                    "notification"
            );

            return true;
        }

        /*
         * QUICK SETTINGS
         */

        if (lower.contains(
                "quick settings"
        ) ||
                lower.contains(
                        "quick setting"
                )) {

            MayaAccessibilityService s =
                    MayaAccessibilityService
                            .getInstance();

            if (s != null) {
                s.quickSettings();
            }

            return true;
        }

        /*
         * SCROLL
         */

        if (containsAny(
                lower,
                "scroll down",
                "नीचे scroll",
                "नीचे स्क्रॉल"
        )) {

            MayaAccessibilityService s =
                    MayaAccessibilityService
                            .getInstance();

            if (s != null) {
                s.scrollForward();
            }

            return true;
        }

        if (containsAny(
                lower,
                "scroll up",
                "ऊपर scroll",
                "ऊपर स्क्रॉल"
        )) {

            MayaAccessibilityService s =
                    MayaAccessibilityService
                            .getInstance();

            if (s != null) {
                s.scrollBackward();
            }

            return true;
        }

        /*
         * YOUTUBE
         */

        if (lower.contains("youtube")) {

            MayaCore.openApp(
                    this,
                    "youtube"
            );

            return true;
        }

        /*
         * INSTAGRAM
         */

        if (lower.contains("instagram")) {

            MayaCore.openApp(
                    this,
                    "instagram"
            );

            return true;
        }

        /*
         * WHATSAPP
         */

        if (lower.contains("whatsapp")) {

            MayaCore.openApp(
                    this,
                    "whatsapp"
            );

            return true;
        }

        /*
         * TELEGRAM
         */

        if (lower.contains("telegram")) {

            MayaCore.openApp(
                    this,
                    "telegram"
            );

            return true;
        }

        /*
         * CHROME
         */

        if (lower.contains("chrome")) {

            MayaCore.openApp(
                    this,
                    "chrome"
            );

            return true;
        }

        /*
         * GMAIL
         */

        if (lower.contains("gmail")) {

            MayaCore.openApp(
                    this,
                    "gmail"
            );

            return true;
        }

        /*
         * CANVA
         */

        if (lower.contains("canva")) {

            MayaCore.openApp(
                    this,
                    "canva"
            );

            return true;
        }

        /*
         * GOOGLE SEARCH
         */

        if (lower.contains("google search") ||
                lower.contains("search google") ||
                lower.contains("google पर search") ||
                lower.contains("गूगल पर सर्च")) {

            String query =
                    extractSearchQuery(
                            text
                    );

            if (!query.isEmpty()) {

                MayaCore.webSearch(
                        this,
                        query
                );

                return true;
            }
        }

        /*
         * OPEN CAMERA
         */

        if (lower.contains("open camera") ||
                lower.contains("camera खोल") ||
                lower.contains("कैमरा खोल")) {

            try {

                Intent intent =
                        new Intent(
                                "android.media.action.IMAGE_CAPTURE"
                        );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(intent);

                return true;

            } catch (Exception ignored) {
            }
        }

        /*
         * CALL / DIAL
         */

        if (lower.contains("call") ||
                lower.contains("phone") ||
                lower.contains("कॉल")) {

            String number =
                    MayaCore.extractPhoneNumber(
                            text
                    );

            if (!number.isEmpty()) {

                showCallConfirmation(
                        number
                );

                return true;
            }
        }

        /*
         * CLICK BY TEXT
         */

        if (lower.startsWith("click ")) {

            String target =
                    text.substring(
                            6
                    ).trim();

            MayaAccessibilityService s =
                    MayaAccessibilityService
                            .getInstance();

            if (s != null &&
                    s.clickByText(target)) {

                return true;
            }
        }

        if (lower.startsWith("tap ")) {

            String target =
                    text.substring(
                            4
                    ).trim();

            MayaAccessibilityService s =
                    MayaAccessibilityService
                            .getInstance();

            if (s != null &&
                    s.clickByText(target)) {

                return true;
            }
        }

        return false;
    }

    private boolean containsAny(
            String text,
            String... values
    ) {

        for (String value : values) {

            if (text.contains(
                    value.toLowerCase(
                            Locale.US
                    )
            )) {
                return true;
            }
        }

        return false;
    }

    private String extractSearchQuery(
            String text
    ) {

        String lower =
                text.toLowerCase(
                        Locale.US
                );

        String[] prefixes = {
                "google search",
                "search google",
                "google पर search",
                "गूगल पर सर्च"
        };

        for (String prefix : prefixes) {

            int index =
                    lower.indexOf(
                            prefix.toLowerCase(
                                    Locale.US
                            )
                    );

            if (index >= 0) {

                return text.substring(
                        index + prefix.length()
                ).trim();
            }
        }

        return "";
    }

    private void showCallConfirmation(
            String number
    ) {

        if (!popupShowing) {
            showPopup();
        }

        new android.os.Handler(
                getMainLooper()
        ).post(() -> {

            if (popup == null) {
                return;
            }

            Button call =
                    new Button(this);

            call.setText(
                    "CALL " + number
            );

            call.setOnClickListener(
                    v -> {

                        MayaCore.dial(
                                this,
                                number
                        );

                        call.setText(
                                "DIALER OPENED"
                        );
                    }
            );

            popup.addView(call);
        });
    }

    private void speak(
            String text
    ) {

        if (tts == null) {
            return;
        }

        new android.os.Handler(
                getMainLooper()
        ).post(() -> {

            setStatus(
                    "● SPEAKING"
            );

            tts.speak(
                    text == null
                            ? ""
                            : text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "maya_response"
            );
        });
    }

    private void restart() {

        if (!enabled() ||
                !liveConversation ||
                recognizer == null ||
                processing) {

            return;
        }

        postDelayed(
                this::listen,
                700
        );
    }

    private void listen() {

        if (!enabled() ||
                !liveConversation ||
                recognizer == null ||
                listening ||
                processing) {

            return;
        }

        listening = true;

        Intent intent =
                new Intent(
                        RecognizerIntent
                                .ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent
                        .EXTRA_LANGUAGE_MODEL,
                RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        recognizer.startListening(
                intent
        );
    }

    private void postDelayed(
            Runnable runnable,
            long delay
    ) {

        new android.os.Handler(
                getMainLooper()
        ).postDelayed(
                runnable,
                delay
        );
    }

    private void setStatus(
            String status
    ) {

        new android.os.Handler(
                getMainLooper()
        ).post(() -> {

            if (popupStatus != null) {
                popupStatus.setText(status);
            }
        });
    }

    private void updatePopup(
            String user,
            String answer
    ) {

        if (!popupShowing ||
                popupChat == null) {

            return;
        }

        popupChat.append(
                "\n\nYOU\n" +
                user +
                "\n\nMAYA\n" +
                answer
        );
    }

    private void showPopup() {

        if (popupShowing) {
            return;
        }

        if (!Settings.canDrawOverlays(
                this
        )) {
            return;
        }

        wm =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        popup =
                new LinearLayout(
                        this
                );

        popup.setOrientation(
                LinearLayout.VERTICAL
        );

        popup.setPadding(
                30,
                24,
                30,
                24
        );

        popup.setBackgroundColor(
                Color.rgb(
                        8,
                        15,
                        22
                )
        );

        TextView title =
                new TextView(this);

        title.setText(
                "◉  MAYA  •  LIVE"
        );

        title.setTextColor(
                cyan
        );

        title.setTextSize(
                21
        );

        title.setPadding(
                0,
                0,
                0,
                12
        );

        popupStatus =
                new TextView(this);

        popupStatus.setText(
                "● AWAKENED"
        );

        popupStatus.setTextColor(
                Color.WHITE
        );

        popupStatus.setTextSize(
                13
        );

        popupChat =
                new TextView(this);

        popupChat.setTextColor(
                Color.LTGRAY
        );

        popupChat.setTextSize(
                15
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(
                popupChat
        );

        Button end =
                new Button(this);

        end.setText(
                "END MAYA LIVE"
        );

        end.setOnClickListener(
                v -> {

                    liveConversation =
                            false;

                    processing =
                            false;

                    if (recognizer != null) {

                        try {
                            recognizer.stopListening();
                        } catch (Exception ignored) {
                        }
                    }

                    removePopup();
                }
        );

        popup.addView(title);
        popup.addView(popupStatus);

        popup.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        popup.addView(end);

        WindowManager.LayoutParams lp =
                new WindowManager.LayoutParams(
                        (int) (
                                340 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                        ),
                        (int) (
                                470 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                        ),
                        Build.VERSION.SDK_INT >= 26
                                ? WindowManager.LayoutParams
                                        .TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams
                                        .TYPE_PHONE,
                        WindowManager.LayoutParams
                                .FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams
                                .FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                );

        lp.gravity =
                Gravity.CENTER;

        try {

            wm.addView(
                    popup,
                    lp
            );

            popupShowing =
                    true;

        } catch (Exception ignored) {
        }
    }

    private void removePopup() {

        if (!popupShowing ||
                wm == null ||
                popup == null) {

            return;
        }

        try {

            wm.removeView(
                    popup
            );

        } catch (Exception ignored) {
        }

        popupShowing =
                false;

        popup = null;
        popupStatus = null;
        popupChat = null;
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        if (!enabled()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        liveConversation =
                true;

        restart();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        liveConversation =
                false;

        if (recognizer != null) {

            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        if (tts != null) {

            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception ignored) {
            }
        }

        removePopup();

        super.onDestroy();
    }

    @Override
    public android.os.IBinder onBind(
            Intent intent
    ) {
        return null;
    }
}
