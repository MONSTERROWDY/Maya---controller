package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    // =========================================================
    // COLORS
    // =========================================================

    private static final int CYAN =
            Color.rgb(0, 229, 255);

    private static final int PURPLE =
            Color.rgb(155, 89, 255);

    private static final int GREEN =
            Color.rgb(0, 230, 118);

    private static final int RED =
            Color.rgb(255, 82, 82);

    private static final int WHITE =
            Color.WHITE;

    private static final int MUTED =
            Color.rgb(165, 175, 190);

    private static final int BG =
            Color.rgb(5, 7, 12);

    private static final int CARD =
            Color.rgb(14, 18, 28);

    // =========================================================
    // UI
    // =========================================================

    private LinearLayout content;

    private EditText geminiKey;
    private EditText geminiModel;

    private EditText openAIKey;
    private EditText openAIModel;

    private RadioButton geminiRadio;
    private RadioButton openAIRadio;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        requestBasicPermissions();

        showHome();
    }

    // =========================================================
    // PERMISSIONS
    // =========================================================

    private void requestBasicPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        100
                );
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        101
                );
            }
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    private void showHome() {

        LinearLayout root =
                baseLayout();

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(8)
        );

        LinearLayout brand =
                new LinearLayout(this);

        brand.setOrientation(
                LinearLayout.VERTICAL
        );

        brand.addView(
                text(
                        "MAYA",
                        30,
                        CYAN,
                        true
                )
        );

        brand.addView(
                text(
                        "CONTROL • AI PHONE AGENT",
                        11,
                        MUTED,
                        false
                )
        );

        header.addView(
                brand,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        header.addView(
                text(
                        "● ONLINE",
                        12,
                        GREEN,
                        true
                )
        );

        root.addView(header);

        // -----------------------------------------------------
        // SCROLL CONTENT
        // -----------------------------------------------------

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(8),
                dp(16),
                dp(90)
        );

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // -----------------------------------------------------
        // BOTTOM NAV
        // -----------------------------------------------------

        root.addView(
                bottomNavigation()
        );

        setContentView(root);

        loadHomeContent();
    }

    // =========================================================
    // HOME CONTENT
    // =========================================================

    private void loadHomeContent() {

        content.removeAllViews();

        content.addView(
                text(
                        "Hello, Boss",
                        25,
                        WHITE,
                        true
                )
        );

        content.addView(
                text(
                        "MAYA is ready to control your phone.",
                        14,
                        MUTED,
                        false
                )
        );

        space(12);

        // =====================================================
        // MASTER CONTROL
        // =====================================================

        LinearLayout masterCard =
                card();

        masterCard.addView(
                text(
                        "MAYA MASTER CONTROL",
                        17,
                        CYAN,
                        true
                )
        );

        masterCard.addView(
                text(
                        "Voice + Phone Agent + Background Service",
                        13,
                        MUTED,
                        false
                )
        );

        Switch masterSwitch =
                new Switch(this);

        masterSwitch.setText(
                "MAYA ON / OFF"
        );

        masterSwitch.setTextColor(
                WHITE
        );

        masterSwitch.setTextSize(15);

        boolean mayaEnabled =
                getSharedPreferences(
                        "maya_settings",
                        MODE_PRIVATE
                ).getBoolean(
                        "maya_enabled",
                        false
                );

        masterSwitch.setChecked(
                mayaEnabled
        );

        masterSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    getSharedPreferences(
                            "maya_settings",
                            MODE_PRIVATE
                    )
                            .edit()
                            .putBoolean(
                                    "maya_enabled",
                                    isChecked
                            )
                            .apply();

                    if (isChecked) {

                        startVoiceService();

                    } else {

                        stopVoiceService();
                    }
                }
        );

        masterCard.addView(
                masterSwitch
        );

        content.addView(
                masterCard
        );

        space(12);

        // =====================================================
        // AI CARD
        // =====================================================

        LinearLayout aiCard =
                card();

        aiCard.addView(
                text(
                        "MAYA AI CORE",
                        16,
                        CYAN,
                        true
                )
        );

        String provider =
                MayaCore.getProvider(this);

        aiCard.addView(
                text(
                        "Provider: "
                                + provider.toUpperCase(
                                Locale.US
                        ),
                        13,
                        WHITE,
                        false
                )
        );

        Button aiSettings =
                button(
                        "AI SETTINGS",
                        CYAN
                );

        aiSettings.setOnClickListener(
                v -> showAISettings()
        );

        aiCard.addView(
                aiSettings
        );

        Button testAI =
                button(
                        "TEST AI",
                        CYAN
                );

        testAI.setOnClickListener(
                v -> testAI()
        );

        aiCard.addView(
                testAI
        );

        content.addView(
                aiCard
        );

        space(12);

        // =====================================================
        // VOICE CARD
        // =====================================================

        LinearLayout voiceCard =
                card();

        voiceCard.addView(
                text(
                        "VOICE CONTROL",
                        16,
                        PURPLE,
                        true
                )
        );

        voiceCard.addView(
                text(
                        "Wake phrase: Hello Maya / Hello Boss",
                        13,
                        MUTED,
                        false
                )
        );

        Button voiceSettings =
                button(
                        "VOICE SETTINGS",
                        PURPLE
                );

        voiceSettings.setOnClickListener(
                v -> showVoiceSettings()
        );

        voiceCard.addView(
                voiceSettings
        );

        Button startVoice =
                button(
                        "START MAYA VOICE",
                        PURPLE
                );

        startVoice.setOnClickListener(
                v -> startVoiceService()
        );

        voiceCard.addView(
                startVoice
        );

        Button overlay =
                button(
                        "ENABLE LIVE POPUP",
                        CYAN
                );

        overlay.setOnClickListener(
                v -> requestOverlay()
        );

        voiceCard.addView(
                overlay
        );

        content.addView(
                voiceCard
        );

        space(12);

        // =====================================================
        // DEVICE STATUS
        // =====================================================

        LinearLayout stats =
                new LinearLayout(this);

        stats.setOrientation(
                LinearLayout.HORIZONTAL
        );

        stats.addView(
                statCard(
                        "BATTERY",
                        batteryPercent() + "%",
                        GREEN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        stats.addView(
                statCard(
                        "STORAGE",
                        storagePercent() + "%",
                        CYAN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        content.addView(
                stats
        );

        space(10);

        LinearLayout stats2 =
                new LinearLayout(this);

        stats2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        boolean accessibility =
                accessibilityEnabled();

        stats2.addView(
                statCard(
                        "NETWORK",
                        networkStatus(),
                        PURPLE
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        stats2.addView(
                statCard(
                        "ACCESSIBILITY",
                        accessibility
                                ? "ON"
                                : "OFF",
                        accessibility
                                ? GREEN
                                : RED
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        content.addView(
                stats2
        );

        space(14);

        // =====================================================
        // ACCESSIBILITY
        // =====================================================

        Button accessibilityButton =
                button(
                        "OPEN ACCESSIBILITY SETTINGS",
                        CYAN
                );

        accessibilityButton.setOnClickListener(
                v -> {

                    try {

                        startActivity(
                                new Intent(
                                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                                )
                        );

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Accessibility settings unavailable",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        content.addView(
                accessibilityButton
        );

        // =====================================================
        // OVERLAY
        // =====================================================

        Button overlayButton =
                button(
                        "ENABLE FLOATING OVERLAY",
                        PURPLE
                );

        overlayButton.setOnClickListener(
                v -> requestOverlay()
        );

        content.addView(
                overlayButton
        );

        space(14);

        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        content.addView(
                text(
                        "QUICK ACTIONS",
                        16,
                        WHITE,
                        true
                )
        );

        Button whatsapp =
                button(
                        "OPEN WHATSAPP",
                        WHITE
                );

        whatsapp.setOnClickListener(
                v -> MayaCore.openApp(
                        this,
                        "whatsapp"
                )
        );

        content.addView(
                whatsapp
        );

        Button instagram =
                button(
                        "OPEN INSTAGRAM",
                        WHITE
                );

        instagram.setOnClickListener(
                v -> MayaCore.openApp(
                        this,
                        "instagram"
                )
        );

        content.addView(
                instagram
        );

        Button youtube =
                button(
                        "OPEN YOUTUBE",
                        WHITE
                );

        youtube.setOnClickListener(
                v -> MayaCore.openApp(
                        this,
                        "youtube"
                )
        );

        content.addView(
                youtube
        );

        Button chrome =
                button(
                        "OPEN CHROME",
                        WHITE
                );

        chrome.setOnClickListener(
                v -> MayaCore.openApp(
                        this,
                        "chrome"
                )
        );

        content.addView(
                chrome
        );

        Button camera =
                button(
                        "OPEN CAMERA",
                        WHITE
                );

        camera.setOnClickListener(
                v -> {

                    try {

                        Intent intent =
                                new Intent(
                                        "android.media.action.IMAGE_CAPTURE"
                                );

                        startActivity(intent);

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Camera unavailable",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        content.addView(
                camera
        );

        Button dialer =
                button(
                        "OPEN DIALER",
                        WHITE
                );

        dialer.setOnClickListener(
                v -> MayaCore.dial(
                        this,
                        ""
                )
        );

        content.addView(
                dialer
        );
    }

    // =========================================================
    // AI SETTINGS
    // =========================================================

    private void showAISettings() {

        LinearLayout root =
                baseLayout();

        headerBar(
                root,
                "AI PROVIDER",
                "Gemini / OpenAI"
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        box.addView(
                text(
                        "AI ENGINE",
                        22,
                        CYAN,
                        true
                )
        );

        spaceInto(
                box,
                10
        );

        geminiRadio =
                new RadioButton(this);

        geminiRadio.setText(
                "Google Gemini"
        );

        geminiRadio.setTextColor(
                WHITE
        );

        geminiRadio.setTextSize(15);

        openAIRadio =
                new RadioButton(this);

        openAIRadio.setText(
                "OpenAI"
        );

        openAIRadio.setTextColor(
                WHITE
        );

        openAIRadio.setTextSize(15);

        String provider =
                MayaCore.getProvider(this);

        geminiRadio.setChecked(
                !"openai".equalsIgnoreCase(
                        provider
                )
        );

        openAIRadio.setChecked(
                "openai".equalsIgnoreCase(
                        provider
                )
        );

        geminiRadio.setOnClickListener(
                v -> openAIRadio.setChecked(false)
        );

        openAIRadio.setOnClickListener(
                v -> geminiRadio.setChecked(false)
        );

        box.addView(
                geminiRadio
        );

        box.addView(
                openAIRadio
        );

        spaceInto(
                box,
                15
        );

        box.addView(
                text(
                        "GEMINI API",
                        14,
                        CYAN,
                        true
                )
        );

        geminiKey =
                editText(
                        "Gemini API Key",
                        true
                );

        geminiKey.setText(
                MayaCore.getGeminiKey(
                        this
                )
        );

        box.addView(
                geminiKey
        );

        geminiModel =
                editText(
                        "Gemini Model",
                        false
                );

        geminiModel.setText(
                MayaCore.getGeminiModel(
                        this
                )
        );

        box.addView(
                geminiModel
        );

        spaceInto(
                box,
                12
        );

        box.addView(
                text(
                        "OPENAI API",
                        14,
                        PURPLE,
                        true
                )
        );

        openAIKey =
                editText(
                        "OpenAI API Key",
                        true
                );

        openAIKey.setText(
                MayaCore.getOpenAIKey(
                        this
                )
        );

        box.addView(
                openAIKey
        );

        openAIModel =
                editText(
                        "OpenAI Model",
                        false
                );

        openAIModel.setText(
                MayaCore.getOpenAIModel(
                        this
                )
        );

        box.addView(
                openAIModel
        );

        spaceInto(
                box,
                15
        );

        Button save =
                button(
                        "SAVE AI SETTINGS",
                        CYAN
                );

        save.setOnClickListener(
                v -> saveAISettings()
        );

        box.addView(
                save
        );

        Button test =
                button(
                        "TEST CONNECTION",
                        PURPLE
                );

        test.setOnClickListener(
                v -> testAI()
        );

        box.addView(
                test
        );

        scroll.addView(
                box
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(
                bottomNavigation()
        );

        setContentView(root);
    }

    // =========================================================
    // SAVE AI
    // =========================================================

    private void saveAISettings() {

        String provider =
                openAIRadio != null
                        && openAIRadio.isChecked()
                        ? "openai"
                        : "gemini";

        String gKey =
                geminiKey == null
                        ? ""
                        : geminiKey
                        .getText()
                        .toString()
                        .trim();

        String gModel =
                geminiModel == null
                        ? "gemini-3.8-flash"
                        : geminiModel
                        .getText()
                        .toString()
                        .trim();

        String oKey =
                openAIKey == null
                        ? ""
                        : openAIKey
                        .getText()
                        .toString()
                        .trim();

        String oModel =
                openAIModel == null
                        ? "gpt-4o-mini"
                        : openAIModel
                        .getText()
                        .toString()
                        .trim();

        if (gModel.isEmpty()) {

            gModel =
                    "gemini-3.8-flash";
        }

        if (oModel.isEmpty()) {

            oModel =
                    "gpt-4o-mini";
        }

        MayaCore.saveSettings(
                this,
                gKey,
                gModel,
                oKey,
                oModel,
                provider
        );

        Toast.makeText(
                this,
                "MAYA AI settings saved",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // TEST AI
    // =========================================================

    private void testAI() {

        MayaCore.ask(
                this,
                "Reply with exactly: MAYA ONLINE",
                (ok, answer) ->
                        runOnUiThread(() -> {

                            new AlertDialog.Builder(
                                    this
                            )
                                    .setTitle(
                                            ok
                                                    ? "MAYA AI"
                                                    : "AI ERROR"
                                    )
                                    .setMessage(
                                            answer == null
                                                    ? "No response"
                                                    : answer
                                    )
                                    .setPositiveButton(
                                            "OK",
                                            null
                                    )
                                    .show();
                        })
        );
    }

    // =========================================================
    // VOICE SETTINGS
    // =========================================================

    private void showVoiceSettings() {

        LinearLayout root =
                baseLayout();

        headerBar(
                root,
                "VOICE SERVICE",
                "MAYA voice control"
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        box.addView(
                text(
                        "VOICE CONTROL",
                        23,
                        PURPLE,
                        true
                )
        );

        box.addView(
                text(
                        "MAYA can listen and execute phone commands.",
                        14,
                        MUTED,
                        false
                )
        );

        spaceInto(
                box,
                15
        );

        Switch voiceSwitch =
                new Switch(this);

        voiceSwitch.setText(
                "MAYA VOICE SERVICE"
        );

        voiceSwitch.setTextColor(
                WHITE
        );

        voiceSwitch.setTextSize(15);

        boolean enabled =
                getSharedPreferences(
                        "maya_settings",
                        MODE_PRIVATE
                ).getBoolean(
                        "maya_enabled",
                        false
                );

        voiceSwitch.setChecked(
                enabled
        );

        voiceSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    getSharedPreferences(
                            "maya_settings",
                            MODE_PRIVATE
                    )
                            .edit()
                            .putBoolean(
                                    "maya_enabled",
                                    isChecked
                            )
                            .apply();

                    if (isChecked) {

                        startVoiceService();

                    } else {

                        stopVoiceService();
                    }
                }
        );

        box.addView(
                voiceSwitch
        );

        spaceInto(
                box,
                10
        );

        Button start =
                button(
                        "START VOICE SERVICE",
                        PURPLE
                );

        start.setOnClickListener(
                v -> startVoiceService()
        );

        box.addView(
                start
        );

        Button stop =
                button(
                        "STOP VOICE SERVICE",
                        RED
                );

        stop.setOnClickListener(
                v -> stopVoiceService()
        );

        box.addView(
                stop
        );

        Button overlay =
                button(
                        "LIVE FLOATING POPUP",
                        CYAN
                );

        overlay.setOnClickListener(
                v -> requestOverlay()
        );

        box.addView(
                overlay
        );

        box.addView(
                text(
                        "\nExamples:\n\n"
                                + "• Hello Maya\n"
                                + "• Open WhatsApp\n"
                                + "• Open Instagram\n"
                                + "• Open YouTube\n"
                                + "• Search Google\n"
                                + "• Call a number\n"
                                + "• Open camera\n"
                                + "• Go home\n"
                                + "• Go back\n"
                                + "• Open notifications\n"
                                + "• Scroll down\n",
                        14,
                        MUTED,
                        false
                )
        );

        scroll.addView(
                box
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(
                bottomNavigation()
        );

        setContentView(root);
    }

    // =========================================================
    // START MAYA
    // =========================================================

    private void startVoiceService() {

        try {

            getSharedPreferences(
                    "maya_settings",
                    MODE_PRIVATE
            )
                    .edit()
                    .putBoolean(
                            "maya_enabled",
                            true
                    )
                    .apply();

            Intent intent =
                    new Intent(
                            this,
                            MayaVoiceService.class
                    );

            if (Build.VERSION.SDK_INT >= 26) {

                startForegroundService(
                        intent
                );

            } else {

                startService(
                        intent
                );
            }

            Toast.makeText(
                    this,
                    "MAYA ON",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "MAYA start failed: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // STOP MAYA
    // =========================================================

    private void stopVoiceService() {

        try {

            getSharedPreferences(
                    "maya_settings",
                    MODE_PRIVATE
            )
                    .edit()
                    .putBoolean(
                            "maya_enabled",
                            false
                    )
                    .apply();

            stopService(
                    new Intent(
                            this,
                            MayaVoiceService.class
                    )
            );

            Toast.makeText(
                    this,
                    "MAYA OFF",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to stop MAYA",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // OVERLAY
    // =========================================================

    private void requestOverlay() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.canDrawOverlays(this)) {

                try {

                    Intent intent =
                            new Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse(
                                            "package:"
                                                    + getPackageName()
                                    )
                            );

                    startActivity(intent);

                } catch (Exception e) {

                    startActivity(
                            new Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            )
                    );
                }

            } else {

                Toast.makeText(
                        this,
                        "Floating overlay already enabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private LinearLayout bottomNavigation() {

        LinearLayout nav =
                new LinearLayout(this);

        nav.setOrientation(
                LinearLayout.HORIZONTAL
        );

        nav.setGravity(
                Gravity.CENTER
        );

        nav.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );

        Button home =
                navButton("HOME");

        home.setOnClickListener(
                v -> showHome()
        );

        nav.addView(
                home,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        Button ai =
                navButton("AI");

        ai.setOnClickListener(
                v -> showAISettings()
        );

        nav.addView(
                ai,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        Button voice =
                navButton("VOICE");

        voice.setOnClickListener(
                v -> showVoiceSettings()
        );

        nav.addView(
                voice,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        return nav;
    }

    // =========================================================
    // BASE LAYOUT
    // =========================================================

    private LinearLayout baseLayout() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                BG
        );

        return root;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void headerBar(
            LinearLayout root,
            String title,
            String subtitle
    ) {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.VERTICAL
        );

        bar.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(12)
        );

        bar.addView(
                text(
                        title,
                        22,
                        CYAN,
                        true
                )
        );

        bar.addView(
                text(
                        subtitle,
                        12,
                        MUTED,
                        false
                )
        );

        root.addView(
                bar
        );
    }

    // =========================================================
    // CARD
    // =========================================================

    private LinearLayout card() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        box.setBackgroundColor(
                CARD
        );

        return box;
    }

    // =========================================================
    // TEXT
    // =========================================================

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(value);

        t.setTextSize(size);

        t.setTextColor(color);

        if (bold) {

            t.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        t.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        return t;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private Button button(
            String label,
            int color
    ) {

        Button b =
                new Button(this);

        b.setText(label);

        b.setTextColor(color);

        b.setTextSize(13);

        return b;
    }

    // =========================================================
    // NAV BUTTON
    // =========================================================

    private Button navButton(
            String label
    ) {

        Button b =
                new Button(this);

        b.setText(label);

        b.setTextColor(
                WHITE
        );

        b.setTextSize(12);

        return b;
    }

    // =========================================================
    // EDIT TEXT
    // =========================================================

    private EditText editText(
            String hint,
            boolean password
    ) {

        EditText e =
                new EditText(this);

        e.setHint(hint);

        e.setHintTextColor(
                MUTED
        );

        e.setTextColor(
                WHITE
        );

        e.setTextSize(14);

        if (password) {

            e.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT
                            | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        }

        return e;
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private View statCard(
            String title,
            String value,
            int color
    ) {

        LinearLayout box =
                card();

        box.addView(
                text(
                        title,
                        11,
                        MUTED,
                        true
                )
        );

        box.addView(
                text(
                        value,
                        18,
                        color,
                        true
                )
        );

        return box;
    }

    // =========================================================
    // SPACE
    // =========================================================

    private void space(
            int value
    ) {

        View view =
                new View(this);

        content.addView(
                view,
                new LinearLayout.LayoutParams(
                        1,
                        dp(value)
                )
        );
    }

    private void spaceInto(
            LinearLayout parent,
            int value
    ) {

        View view =
                new View(this);

        parent.addView(
                view,
                new LinearLayout.LayoutParams(
                        1,
                        dp(value)
                )
        );
    }

    // =========================================================
    // DP
    // =========================================================

    private int dp(
            int value
    ) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private int batteryPercent() {

        try {

            BatteryManager manager =
                    (BatteryManager)
                            getSystemService(
                                    BATTERY_SERVICE
                            );

            return manager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // =========================================================
    // STORAGE
    // =========================================================

    private int storagePercent() {

        try {

            StatFs stat =
                    new StatFs(
                            getFilesDir()
                                    .getAbsolutePath()
                    );

            long total =
                    stat.getTotalBytes();

            long free =
                    stat.getAvailableBytes();

            if (total <= 0) {

                return 0;
            }

            long used =
                    total - free;

            return (int) (
                    (used * 100L)
                            / total
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // =========================================================
    // NETWORK
    // =========================================================

    private String networkStatus() {

        try {

            ConnectivityManager manager =
                    (ConnectivityManager)
                            getSystemService(
                                    CONNECTIVITY_SERVICE
                            );

            android.net.Network network =
                    manager.getActiveNetwork();

            if (network == null) {

                return "OFF";
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(
                            network
                    );

            if (capabilities == null) {

                return "OFF";
            }

            if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            )) {

                return "WIFI";
            }

            if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            )) {

                return "MOBILE";
            }

            return "ON";

        } catch (Exception e) {

            return "UNKNOWN";
        }
    }

    // =========================================================
    // ACCESSIBILITY
    // =========================================================

    private boolean accessibilityEnabled() {

        return MayaAccessibilityService
                .isReady();
    }
}
