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
import android.os.IBinder;
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

    private final int cyan = Color.rgb(0, 229, 255);

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();
        startForeground(
                77,
                buildNotification()
        );

        tts = new TextToSpeech(
                this,
                status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(
                                new Locale("hi", "IN")
                        );
                    }
                }
        );

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer =
                    SpeechRecognizer.createSpeechRecognizer(this);

            recognizer.setRecognitionListener(
                    new RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(Bundle params) {
                            setStatus("● LISTENING");
                        }

                        @Override
                        public void onBeginningOfSpeech() {
                            setStatus("● LISTENING");
                        }

                        @Override
                        public void onRmsChanged(float rmsdB) {
                        }

                        @Override
                        public void onBufferReceived(byte[] buffer) {
                        }

                        @Override
                        public void onEndOfSpeech() {
                            setStatus("● UNDERSTANDING");
                        }

                        @Override
                        public void onError(int error) {
                            listening = false;

                            if (liveConversation &&
                                    !processing) {
                                restart();
                            }
                        }

                        @Override
                        public void onResults(Bundle results) {
                            listening = false;

                            ArrayList<String> data =
                                    results.getStringArrayList(
                                            SpeechRecognizer.RESULTS_RECOGNITION
                                    );

                            if (data == null ||
                                    data.isEmpty()) {
                                if (liveConversation) restart();
                                return;
                            }

                            String text = data.get(0);

                            handleSpeech(text);
                        }

                        @Override
                        public void onPartialResults(Bundle partialResults) {
                        }

                        @Override
                        public void onEvent(int eventType, Bundle params) {
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
                    .setContentText("MAYA Voice Agent is active")
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .build();
        }

        return new Notification.Builder(this)
                .setContentTitle("MAYA")
                .setContentText("MAYA Voice Agent is active")
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel =
                    new NotificationChannel(
                            "maya_voice",
                            "MAYA Voice",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager nm =
                    getSystemService(
                            NotificationManager.class
                    );

            nm.createNotificationChannel(channel);
        }
    }

    private void handleSpeech(String raw) {
        String text =
                raw == null ? "" : raw.trim();

        if (text.isEmpty()) {
            if (liveConversation) restart();
            return;
        }

        String lower =
                text.toLowerCase();

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

        if (helloMaya || helloBoss || mayaWake) {
            showPopup();

            if (helloMaya || mayaWake) {
                speak("हाँ बॉस, क्या करना बताइए।");
            } else {
                speak("Hello Boss. हाँ, बोलिए।");
            }

            liveConversation = true;
            restart();
            return;
        }

        if (!liveConversation) {
            return;
        }

        final String userText = text;

        if (processing) return;

        processing = true;
        setStatus("● THINKING");

        handleLocalAction(userText);

        MayaCore.ask(
                this,
                userText,
                (ok, answer) -> {

                    processing = false;

                    runOnUiThread(() -> {
                        updatePopup(
                                userText,
                                answer
                        );
                    });

                    speak(answer);
                    restart();
                }
        );
    }

    private void handleLocalAction(String text) {
        String lower =
                text.toLowerCase();

        if (lower.contains("youtube")) {
            MayaCore.openApp(
                    this,
                    "youtube"
            );
        } else if (lower.contains("instagram")) {
            MayaCore.openApp(
                    this,
                    "instagram"
            );
        } else if (lower.contains("whatsapp")) {
            MayaCore.openApp(
                    this,
                    "whatsapp"
            );
        } else if (lower.contains("telegram")) {
            MayaCore.openApp(
                    this,
                    "telegram"
            );
        } else if (lower.contains("chrome")) {
            MayaCore.openApp(
                    this,
                    "chrome"
            );
        } else if (lower.contains("gmail")) {
            MayaCore.openApp(
                    this,
                    "gmail"
            );
        } else if (lower.contains("canva")) {
            MayaCore.openApp(
                    this,
                    "canva"
            );
        } else if (lower.contains("home")) {
            MayaCore.executeLocalCommand(
                    this,
                    "home"
            );
        } else if (lower.contains("back")) {
            MayaCore.executeLocalCommand(
                    this,
                    "back"
            );
        } else if (lower.contains("recent")) {
            MayaCore.executeLocalCommand(
                    this,
                    "recent"
            );
        } else if (lower.contains("notification")) {
            MayaCore.executeLocalCommand(
                    this,
                    "notification"
            );
        }

        if (lower.contains("call") ||
                lower.contains("कॉल") ||
                lower.contains("phone")) {

            String number =
                    MayaCore.extractPhoneNumber(text);

            if (!number.isEmpty()) {
                showCallConfirmation(number);
            }
        }
    }

    private void showCallConfirmation(String number) {
        if (!popupShowing) showPopup();

        runOnUiThread(() -> {
            Button call =
                    new Button(this);

            call.setText(
                    "CALL " + number
            );

            call.setOnClickListener(v -> {
                MayaCore.dial(
                        this,
                        number
                );

                call.setText("DIALER OPENED");
            });

            if (popup != null) {
                popup.addView(call);
            }
        });
    }

    private void speak(String text) {
        if (tts == null) return;

        runOnUiThread(() -> {
            setStatus("● SPEAKING");

            tts.speak(
                    text == null ? "" : text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "maya_response"
            );
        });
    }

    private void restart() {
        if (!liveConversation ||
                recognizer == null ||
                processing) return;

        postDelayed(
                this::listen,
                700
        );
    }

    private void listen() {
        if (!liveConversation ||
                recognizer == null ||
                listening ||
                processing) {
            return;
        }

        listening = true;

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        recognizer.startListening(intent);
    }

    private void postDelayed(Runnable r, long delay) {
        new android.os.Handler(
                getMainLooper()
        ).postDelayed(r, delay);
    }

    private void setStatus(String status) {
        runOnUiThread(() -> {
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
                popupChat == null) return;

        popupChat.append(
                "\n\nYOU\n" +
                user +
                "\n\nMAYA\n" +
                answer
        );
    }

    private void showPopup() {
        if (popupShowing) return;

        if (!Settings.canDrawOverlays(this)) {
            return;
        }

        wm =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        popup =
                new LinearLayout(this);

        popup.setOrientation(
                LinearLayout.VERTICAL
        );

        popup.setPadding(
                30, 24, 30, 24
        );

        popup.setBackgroundColor(
                Color.rgb(8, 15, 22)
        );

        TextView title =
                new TextView(this);

        title.setText(
                "◉  MAYA  •  LIVE"
        );

        title.setTextColor(cyan);
        title.setTextSize(21);
        title.setPadding(0, 0, 0, 12);

        popupStatus =
                new TextView(this);

        popupStatus.setText(
                "● AWAKENED"
        );

        popupStatus.setTextColor(
                Color.WHITE
        );

        popupStatus.setTextSize(13);

        popupChat =
                new TextView(this);

        popupChat.setTextColor(
                Color.LTGRAY
        );

        popupChat.setTextSize(15);

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(popupChat);

        Button end =
                new Button(this);

        end.setText("END MAYA LIVE");

        end.setOnClickListener(v -> {
            liveConversation = false;
            processing = false;

            if (recognizer != null) {
                try {
                    recognizer.stopListening();
                } catch (Exception ignored) {
                }
            }

            removePopup();
        });

        popup.addView(title);
        popup.addView(popupStatus);
        popup.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
        popup.addView(end);

        WindowManager.LayoutParams lp =
                new WindowManager.LayoutParams(
                        (int)(340 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density),
                        (int)(470 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density),
                        Build.VERSION.SDK_INT >= 26
                                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                );

        lp.gravity = Gravity.CENTER;

        try {
            wm.addView(popup, lp);
            popupShowing = true;
        } catch (Exception ignored) {
        }
    }

    private void removePopup() {
        if (!popupShowing ||
                wm == null ||
                popup == null) return;

        try {
            wm.removeView(popup);
        } catch (Exception ignored) {
        }

        popupShowing = false;
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
        liveConversation = true;
        restart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        liveConversation = false;

        if (recognizer != null) {
            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        removePopup();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
