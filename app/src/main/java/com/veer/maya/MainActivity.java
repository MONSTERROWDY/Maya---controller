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
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

    private static final int CYAN = Color.rgb(0, 229, 255);
    private static final int PURPLE = Color.rgb(155, 89, 255);
    private static final int GREEN = Color.rgb(0, 230, 118);
    private static final int RED = Color.rgb(255, 82, 82);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(165, 175, 190);
    private static final int BG = Color.rgb(5, 7, 12);
    private static final int CARD = Color.rgb(14, 18, 28);

    private LinearLayout content;
    private TextView titleText;
    private TextView subtitleText;

    private EditText geminiKey;
    private EditText geminiModel;
    private EditText openAIKey;
    private EditText openAIModel;

    private RadioButton geminiRadio;
    private RadioButton openAIRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        showHome();
    }

    // ---------------------------------------------------------
    // MAIN HOME
    // ---------------------------------------------------------

    private void showHome() {

        LinearLayout root = baseLayout();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(20), dp(20), dp(8));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);

        TextView logo = text("MAYA", 30, CYAN, true);
        TextView version = text("CONTROL • AI PHONE AGENT", 11, MUTED, false);

        brand.addView(logo);
        brand.addView(version);

        header.addView(
                brand,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        TextView status = text("● ONLINE", 12, GREEN, true);
        header.addView(status);

        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(90));

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(-1, 0, 1)
        );

        LinearLayout bottom = bottomNavigation();
        root.addView(bottom);

        setContentView(root);

        loadHomeContent();
    }

    private void loadHomeContent() {

        content.removeAllViews();

        TextView greeting = text(
                "Hello, Boss",
                25,
                WHITE,
                true
        );

        content.addView(greeting);

        subtitleText = text(
                "MAYA is ready to control your phone.",
                14,
                MUTED,
                false
        );

        content.addView(subtitleText);

        space(12);

        LinearLayout aiCard = card();

        TextView aiTitle = text(
                "MAYA AI CORE",
                16,
                CYAN,
                true
        );

        aiCard.addView(aiTitle);

        String provider = MayaCore.getProvider(this);

        TextView providerText = text(
                "Provider: " + provider.toUpperCase(Locale.US),
                13,
                WHITE,
                false
        );

        aiCard.addView(providerText);

        Button testAI = button(
                "TEST AI",
                CYAN
        );

        testAI.setOnClickListener(v -> testAI());

        aiCard.addView(testAI);

        content.addView(aiCard);

        space(12);

        LinearLayout voiceCard = card();

        TextView voiceTitle = text(
                "VOICE CONTROL",
                16,
                PURPLE,
                true
        );

        voiceCard.addView(voiceTitle);

        TextView voiceInfo = text(
                "Wake phrase: Hello Maya / Boss",
                13,
                MUTED,
                false
        );

        voiceCard.addView(voiceInfo);

        Button startVoice = button(
                "START MAYA VOICE",
                PURPLE
        );

        startVoice.setOnClickListener(v -> startVoiceService());

        voiceCard.addView(startVoice);

        Button overlay = button(
                "ENABLE LIVE POPUP",
                CYAN
        );

        overlay.setOnClickListener(v -> requestOverlay());

        voiceCard.addView(overlay);

        content.addView(voiceCard);

        space(12);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

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

        content.addView(stats);

        space(10);

        LinearLayout stats2 = new LinearLayout(this);
        stats2.setOrientation(LinearLayout.HORIZONTAL);

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
                        accessibilityEnabled() ? "ON" : "OFF",
                        accessibilityEnabled() ? GREEN : RED
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        content.addView(stats2);

        space(14);

        TextView quickTitle = text(
                "QUICK ACTIONS",
                16,
                WHITE,
                true
        );

        content.addView(quickTitle);

        Button whatsapp = button(
                "OPEN WHATSAPP",
                WHITE
        );

        whatsapp.setOnClickListener(
                v -> MayaCore.openApp(this, "whatsapp")
        );

        content.addView(whatsapp);

        Button instagram = button(
                "OPEN INSTAGRAM",
                WHITE
        );

        instagram.setOnClickListener(
                v -> MayaCore.openApp(this, "instagram")
        );

        content.addView(instagram);

        Button youtube = button(
                "OPEN YOUTUBE",
                WHITE
        );

        youtube.setOnClickListener(
                v -> MayaCore.openApp(this, "youtube")
        );

        content.addView(youtube);

        Button camera = button(
                "OPEN CAMERA",
                WHITE
        );

        camera.setOnClickListener(
                v -> {
                    Intent intent = new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    );
                    startActivity(intent);
                }
        );

        content.addView(camera);

        Button dialer = button(
                "OPEN DIALER",
                WHITE
        );

        dialer.setOnClickListener(
                v -> MayaCore.dial(this, "")
        );

        content.addView(dialer);
    }

    // ---------------------------------------------------------
    // AI SETTINGS
    // ---------------------------------------------------------

    private void showAISettings() {

        LinearLayout root = baseLayout();

        headerBar(
                root,
                "AI PROVIDER",
                "Gemini / OpenAI"
        );

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        TextView title = text(
                "AI ENGINE",
                22,
                CYAN,
                true
        );

        box.addView(title);

        spaceInto(box, 10);

        geminiRadio = new RadioButton(this);
        geminiRadio.setText("Google Gemini");
        geminiRadio.setTextColor(WHITE);
        geminiRadio.setTextSize(15);

        openAIRadio = new RadioButton(this);
        openAIRadio.setText("OpenAI");
        openAIRadio.setTextColor(WHITE);
        openAIRadio.setTextSize(15);

        String provider = MayaCore.getProvider(this);

        geminiRadio.setChecked(
                !"openai".equalsIgnoreCase(provider)
        );

        openAIRadio.setChecked(
                "openai".equalsIgnoreCase(provider)
        );

        geminiRadio.setOnClickListener(
                v -> openAIRadio.setChecked(false)
        );

        openAIRadio.setOnClickListener(
                v -> geminiRadio.setChecked(false)
        );

        box.addView(geminiRadio);
        box.addView(openAIRadio);

        spaceInto(box, 15);

        TextView gTitle = text(
                "GEMINI API",
                14,
                CYAN,
                true
        );

        box.addView(gTitle);

        geminiKey = editText(
                "Gemini API Key",
                true
        );

        geminiKey.setText(
                MayaCore.getGeminiKey(this)
        );

        box.addView(geminiKey);

        geminiModel = editText(
                "Gemini Model",
                false
        );

        geminiModel.setText(
                MayaCore.getGeminiModel(this)
        );

        box.addView(geminiModel);

        spaceInto(box, 12);

        TextView oTitle = text(
                "OPENAI API",
                14,
                PURPLE,
                true
        );

        box.addView(oTitle);

        openAIKey = editText(
                "OpenAI API Key",
                true
        );

        openAIKey.setText(
                MayaCore.getOpenAIKey(this)
        );

        box.addView(openAIKey);

        openAIModel = editText(
                "OpenAI Model",
                false
        );

        openAIModel.setText(
                MayaCore.getOpenAIModel(this)
        );

        box.addView(openAIModel);

        spaceInto(box, 15);

        Button save = button(
                "SAVE AI SETTINGS",
                CYAN
        );

        save.setOnClickListener(
                v -> saveAISettings()
        );

        box.addView(save);

        Button test = button(
                "TEST CONNECTION",
                PURPLE
        );

        test.setOnClickListener(
                v -> testAI()
        );

        box.addView(test);

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(bottomNavigation());

        setContentView(root);
    }

    private void saveAISettings() {

        String provider =
                openAIRadio != null &&
                openAIRadio.isChecked()
                        ? "openai"
                        : "gemini";

        String gKey =
                geminiKey == null
                        ? ""
                        : geminiKey.getText().toString().trim();

        String gModel =
                geminiModel == null
                        ? "gemini-3.8-flash"
                        : geminiModel.getText().toString().trim();

        String oKey =
                openAIKey == null
                        ? ""
                        : openAIKey.getText().toString().trim();

        String oModel =
                openAIModel == null
                        ? "gpt-4o-mini"
                        : openAIModel.getText().toString().trim();

        if (gModel.isEmpty()) {
            gModel = "gemini-3.8-flash";
        }

        if (oModel.isEmpty()) {
            oModel = "gpt-4o-mini";
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

    private void testAI() {

        String message =
                "Reply with exactly: MAYA ONLINE";

        MayaCore.ask(
                this,
                message,
                (ok, answer) -> runOnUiThread(() -> {

                    if (ok) {

                        new AlertDialog.Builder(this)
                                .setTitle("MAYA AI")
                                .setMessage(answer)
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();

                    } else {

                        new AlertDialog.Builder(this)
                                .setTitle("AI ERROR")
                                .setMessage(
                                        answer == null
                                                ? "AI request failed."
                                                : answer
                                )
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                })
        );
    }

    // ---------------------------------------------------------
    // VOICE
    // ---------------------------------------------------------

    private void showVoiceSettings() {

        LinearLayout root = baseLayout();

        headerBar(
                root,
                "VOICE SERVICE",
                "MAYA voice control"
        );

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        TextView title = text(
                "VOICE CONTROL",
                23,
                PURPLE,
                true
        );

        box.addView(title);

        TextView info = text(
                "MAYA can listen for commands and execute phone actions.",
                14,
                MUTED,
                false
        );

        box.addView(info);

        spaceInto(box, 15);

        Switch voiceSwitch = new Switch(this);
        voiceSwitch.setText("VOICE SERVICE");
        voiceSwitch.setTextColor(WHITE);
        voiceSwitch.setTextSize(15);
        voiceSwitch.setChecked(true);

        box.addView(voiceSwitch);

        spaceInto(box, 10);

        Button start = button(
                "START VOICE SERVICE",
                PURPLE
        );

        start.setOnClickListener(
                v -> startVoiceService()
        );

        box.addView(start);

        Button stop = button(
                "STOP VOICE SERVICE",
                RED
        );

        stop.setOnClickListener(
                v -> stopVoiceService()
        );

        box.addView(stop);

        Button overlay = button(
                "LIVE FLOATING POPUP",
                CYAN
        );

        overlay.setOnClickListener(
                v -> requestOverlay()
        );

        box.addView(overlay);

        TextView examples = text(
                "\nExamples:\n\n" +
                        "• Hello Maya\n" +
                        "• Open WhatsApp\n" +
                        "• Open Instagram\n" +
                        "• Search YouTube\n" +
                        "• Call a number\n" +
                        "• Open camera\n",
                14,
                MUTED,
                false
        );

        box.addView(examples);

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(bottomNavigation());

        setContentView(root);
    }

    private void startVoiceService() {

        try {

            Intent intent =
                    new Intent(
                            this,
                            MayaVoiceService.class
                    );

            if (android.os.Build.VERSION.SDK_INT >= 26) {

                startForegroundService(intent);

            } else {

                startService(intent);
            }

            Toast.makeText(
                    this,
                    "MAYA Voice Started",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Voice start error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void stopVoiceService() {

        try {

            Intent intent =
                    new Intent(
                            this,
                            MayaVoiceService.class
                    );

            stopService(intent);

            Toast.makeText(
                    this,
                    "MAYA Voice Stopped",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to stop voice",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ---------------------------------------------------------
    // ACCESSIBILITY / PHONE CONTROL
    // ---------------------------------------------------------

    private void showPhoneControl() {

        LinearLayout root = baseLayout();

        headerBar(
                root,
                "PHONE CONTROL",
                "MAYA Agent"
        );

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        TextView title = text(
                "PHONE AGENT",
                23,
                CYAN,
                true
        );

        box.addView(title);

        TextView state = text(
                accessibilityEnabled()
                        ? "Accessibility Service: ACTIVE"
                        : "Accessibility Service: NOT ACTIVE",
                14,
                accessibilityEnabled()
                        ? GREEN
                        : RED,
                true
        );

        box.addView(state);

        spaceInto(box, 15);

        Button accessibility = button(
                "OPEN ACCESSIBILITY SETTINGS",
                CYAN
        );

        accessibility.setOnClickListener(
                v -> openAccessibility()
        );

        box.addView(accessibility);

        Button whatsapp = button(
                "OPEN WHATSAPP",
                WHITE
        );

        whatsapp.setOnClickListener(
                v -> MayaCore.openApp(this, "whatsapp")
        );

        box.addView(whatsapp);

        Button instagram = button(
                "OPEN INSTAGRAM",
                WHITE
        );

        instagram.setOnClickListener(
                v -> MayaCore.openApp(this, "instagram")
        );

        box.addView(instagram);

        Button youtube = button(
                "OPEN YOUTUBE",
                WHITE
        );

        youtube.setOnClickListener(
                v -> MayaCore.openApp(this, "youtube")
        );

        box.addView(youtube);

        Button browser = button(
                "WEB SEARCH",
                WHITE
        );

        browser.setOnClickListener(
                v -> MayaCore.webSearch(
                        this,
                        "latest technology news"
                )
        );

        box.addView(browser);

        Button call = button(
                "DIAL NUMBER",
                GREEN
        );

        call.setOnClickListener(
                v -> showCallDialog()
        );

        box.addView(call);

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(bottomNavigation());

        setContentView(root);
    }

    private void showCallDialog() {

        final EditText input =
                new EditText(this);

        input.setHint("Enter phone number");
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);

        LinearLayout box =
                new LinearLayout(this);

        box.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        box.addView(
                input,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        new AlertDialog.Builder(this)
                .setTitle("MAYA CALL")
                .setView(box)
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "CALL",
                        (dialog, which) -> {

                            String number =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (!number.isEmpty()) {
                                MayaCore.dial(
                                        this,
                                        number
                                );
                            }
                        }
                )
                .show();
    }

    private void openAccessibility() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open settings",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean accessibilityEnabled() {

        try {

            String enabled =
                    Settings.Secure.getString(
                            getContentResolver(),
                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                    );

            if (enabled == null) {
                return false;
            }

            return enabled.toLowerCase(Locale.US)
                    .contains(
                            getPackageName()
                                    .toLowerCase(Locale.US)
                    );

        } catch (Exception e) {

            return false;
        }
    }

    // ---------------------------------------------------------
    // MEMORY
    // ---------------------------------------------------------

    private void showMemory() {

        LinearLayout root = baseLayout();

        headerBar(
                root,
                "MAYA MEMORY",
                "Memory & context"
        );

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        TextView title = text(
                "MEMORY SYSTEM",
                23,
                PURPLE,
                true
        );

        box.addView(title);

        TextView info = text(
                "MAYA memory stores conversation context used by the assistant.",
                14,
                MUTED,
                false
        );

        box.addView(info);

        spaceInto(box, 15);

        Button clear = button(
                "CLEAR MAYA MEMORY",
                RED
        );

        clear.setOnClickListener(
                v -> confirmClearMemory()
        );

        box.addView(clear);

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(bottomNavigation());

        setContentView(root);
    }

    private void confirmClearMemory() {

        new AlertDialog.Builder(this)
                .setTitle("Clear Memory?")
                .setMessage(
                        "All stored MAYA conversation memory will be cleared."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "CLEAR",
                        (dialog, which) -> {

                            try {

                                MayaMemory.clear(this);

                                Toast.makeText(
                                        this,
                                        "MAYA memory cleared",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } catch (Exception e) {

                                Toast.makeText(
                                        this,
                                        "Memory clear failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .show();
    }

    // ---------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------

    private void showSettings() {

        LinearLayout root = baseLayout();

        headerBar(
                root,
                "SETTINGS",
                "MAYA configuration"
        );

        ScrollView scroll = new ScrollView(this);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(100)
        );

        TextView title = text(
                "MAYA SETTINGS",
                23,
                CYAN,
                true
        );

        box.addView(title);

        Button ai = button(
                "AI PROVIDER",
                CYAN
        );

        ai.setOnClickListener(
                v -> showAISettings()
        );

        box.addView(ai);

        Button voice = button(
                "VOICE SERVICE",
                PURPLE
        );

        voice.setOnClickListener(
                v -> showVoiceSettings()
        );

        box.addView(voice);

        Button phone = button(
                "PHONE CONTROL",
                GREEN
        );

        phone.setOnClickListener(
                v -> showPhoneControl()
        );

        box.addView(phone);

        Button memory = button(
                "MEMORY",
                WHITE
        );

        memory.setOnClickListener(
                v -> showMemory()
        );

        box.addView(memory);

        Button overlay = button(
                "OVERLAY PERMISSION",
                CYAN
        );

        overlay.setOnClickListener(
                v -> requestOverlay()
        );

        box.addView(overlay);

        Button about = button(
                "ABOUT MAYA",
                WHITE
        );

        about.setOnClickListener(
                v -> showAbout()
        );

        box.addView(about);

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        root.addView(bottomNavigation());

        setContentView(root);
    }

    private void showAbout() {

        new AlertDialog.Builder(this)
                .setTitle("MAYA CONTROL")
                .setMessage(
                        "MAYA — AI Voice Phone Agent\n\n" +
                                "AI • Voice • Memory • Accessibility • Phone Control\n\n" +
                                "Version 2.0.0"
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // ---------------------------------------------------------
    // OVERLAY
    // ---------------------------------------------------------

    private void requestOverlay() {

        try {

            if (android.os.Build.VERSION.SDK_INT >= 23) {

                if (!Settings.canDrawOverlays(this)) {

                    Intent intent =
                            new Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse(
                                            "package:" +
                                                    getPackageName()
                                    )
                            );

                    startActivity(intent);

                    return;
                }
            }

            Toast.makeText(
                    this,
                    "Overlay permission is enabled",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open overlay settings",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ---------------------------------------------------------
    // BOTTOM NAVIGATION
    // ---------------------------------------------------------

    private LinearLayout bottomNavigation() {

        LinearLayout nav = new LinearLayout(this);

        nav.setOrientation(
                LinearLayout.HORIZONTAL
        );

        nav.setGravity(Gravity.CENTER);

        nav.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(6)
        );

        nav.setBackgroundColor(
                Color.rgb(9, 12, 20)
        );

        Button home = navButton("HOME");
        home.setOnClickListener(
                v -> showHome()
        );

        Button dashboard = navButton("DASHBOARD");
        dashboard.setOnClickListener(
                v -> showHome()
        );

        Button memory = navButton("MEMORY");
        memory.setOnClickListener(
                v -> showMemory()
        );

        Button settings = navButton("SETTINGS");
        settings.setOnClickListener(
                v -> showSettings()
        );

        nav.addView(
                home,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        nav.addView(
                dashboard,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        nav.addView(
                memory,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        nav.addView(
                settings,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        return nav;
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private LinearLayout baseLayout() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        return root;
    }

    private void headerBar(
            LinearLayout root,
            String title,
            String subtitle
    ) {

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.VERTICAL
        );

        header.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(10)
        );

        titleText = text(
                title,
                24,
                CYAN,
                true
        );

        subtitleText = text(
                subtitle,
                12,
                MUTED,
                false
        );

        header.addView(titleText);
        header.addView(subtitleText);

        root.addView(header);
    }

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

        box.setBackgroundColor(CARD);

        return box;
    }

    private LinearLayout statCard(
            String name,
            String value,
            int accent
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(Gravity.CENTER);

        box.setPadding(
                dp(10),
                dp(14),
                dp(10),
                dp(14)
        );

        box.setBackgroundColor(CARD);

        TextView n = text(
                name,
                10,
                MUTED,
                true
        );

        n.setGravity(Gravity.CENTER);

        TextView v = text(
                value,
                17,
                accent,
                true
        );

        v.setGravity(Gravity.CENTER);

        box.addView(n);
        box.addView(v);

        return box;
    }

    private Button button(
            String label,
            int color
    ) {

        Button b =
                new Button(this);

        b.setText(label);
        b.setTextColor(color);
        b.setTextSize(13);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        b.setAllCaps(false);

        b.setBackgroundColor(
                Color.rgb(20, 25, 38)
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        lp.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        b.setLayoutParams(lp);

        return b;
    }

    private Button navButton(
            String label
    ) {

        Button b =
                new Button(this);

        b.setText(label);
        b.setTextColor(MUTED);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setBackgroundColor(
                Color.TRANSPARENT
        );

        return b;
    }

    private TextView text(
            String value,
            float size,
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

        return t;
    }

    private EditText editText(
            String hint,
            boolean password
    ) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(WHITE);
        e.setTextSize(14);
        e.setSingleLine(true);

        if (password) {

            e.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        }

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        lp.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        e.setLayoutParams(lp);

        return e;
    }

    private void space(int size) {

        View v = new View(this);

        content.addView(
                v,
                new LinearLayout.LayoutParams(
                        1,
                        dp(size)
                )
        );
    }

    private void spaceInto(
            LinearLayout box,
            int size
    ) {

        View v = new View(this);

        box.addView(
                v,
                new LinearLayout.LayoutParams(
                        1,
                        dp(size)
                )
        );
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // ---------------------------------------------------------
    // DEVICE STATUS
    // ---------------------------------------------------------

    private int batteryPercent() {

        try {

            BatteryManager bm =
                    (BatteryManager)
                            getSystemService(
                                    BATTERY_SERVICE
                            );

            return bm.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private int storagePercent() {

        try {

            StatFs stat =
                    new StatFs(
                            getFilesDir()
                                    .getAbsolutePath()
                    );

            long total =
                    stat.getTotalBytes();

            long available =
                    stat.getAvailableBytes();

            if (total <= 0) {
                return 0;
            }

            long used =
                    total - available;

            return (int)
                    ((used * 100L) / total);

        } catch (Exception e) {

            return 0;
        }
    }

    private String networkStatus() {

        try {

            ConnectivityManager cm =
                    (ConnectivityManager)
                            getSystemService(
                                    CONNECTIVITY_SERVICE
                            );

            NetworkCapabilities nc =
                    cm.getNetworkCapabilities(
                            cm.getActiveNetwork()
                    );

            if (nc == null) {
                return "OFF";
            }

            if (nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            )) {

                return "WIFI";
            }

            if (nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            )) {

                return "MOBILE";
            }

            return "ON";

        } catch (Exception e) {

            return "OFF";
        }
    }
}
