package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Locale;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(3, 7, 16);
    private final int CARD = Color.rgb(7, 18, 34);
    private final int CARD2 = Color.rgb(9, 25, 45);
    private final int CYAN = Color.rgb(0, 210, 255);
    private final int BLUE = Color.rgb(25, 105, 255);
    private final int PURPLE = Color.rgb(145, 40, 255);
    private final int GREEN = Color.rgb(0, 235, 150);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(155, 175, 195);

    private LinearLayout content;
    private TextView pageTitle;
    private TextView globalStatus;

    private EditText geminiKey;
    private EditText geminiModel;
    private EditText openAIKey;
    private EditText openAIModel;

    private RadioButton geminiRadio;
    private RadioButton openAIRadio;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        requestBasicPermissions();
        buildApp();
    }

    private void requestBasicPermissions() {

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );
        }
    }

    private GradientDrawable bg(
            int color,
            int strokeColor,
            int radius
    ) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));

        if (strokeColor != Color.TRANSPARENT) {
            d.setStroke(dp(1), strokeColor);
        }

        return d;
    }

    private TextView text(
            String value,
            float size,
            int color
    ) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private TextView titleText(String value) {
        TextView t = text(value, 23, WHITE);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private Button actionButton(
            String icon,
            String title,
            String subtitle,
            View.OnClickListener listener
    ) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(10), dp(8), dp(10));
        box.setBackground(bg(CARD2, Color.rgb(0, 100, 180), 14));
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(listener);

        TextView i = text(icon, 25, WHITE);
        i.setGravity(Gravity.CENTER);

        TextView t = text(title, 13, WHITE);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView s = text(subtitle, 9, MUTED);
        s.setGravity(Gravity.CENTER);

        box.addView(i);
        box.addView(t);
        box.addView(s);

        return convertToButtonLike(box);
    }

    private Button convertToButtonLike(LinearLayout box) {

        Button b = new Button(this);
        b.setText("");
        b.setBackground(box.getBackground());
        b.setPadding(0, 0, 0, 0);

        b.setOnClickListener(box.getOnClickListener());

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(Gravity.CENTER);
        wrapper.setPadding(dp(8), dp(10), dp(8), dp(10));
        wrapper.setBackground(box.getBackground());
        wrapper.setClickable(true);
        wrapper.setFocusable(true);
        wrapper.setOnClickListener(box.getOnClickListener());

        for (int x = 0; x < box.getChildCount(); x++) {
            View child = box.getChildAt(x);
            if (child instanceof TextView) {
                TextView old = (TextView) child;
                TextView copy = text(
                        old.getText().toString(),
                        old.getTextSize() /
                                getResources()
                                        .getDisplayMetrics().scaledDensity,
                        old.getCurrentTextColor()
                );
                copy.setGravity(Gravity.CENTER);
                wrapper.addView(copy);
            }
        }

        return makeButtonFromView(wrapper);
    }

    private Button makeButtonFromView(View view) {

        Button b = new Button(this);
        b.setText("");
        b.setPadding(0, 0, 0, 0);
        b.setBackground(view.getBackground());
        b.setAllCaps(false);

        b.setOnClickListener(view.getOnClickListener());

        return b;
    }

    private LinearLayout card() {

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(14), dp(15), dp(14));
        c.setBackground(
                bg(CARD, Color.rgb(0, 92, 165), 16)
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(0, dp(7), 0, dp(7));
        c.setLayoutParams(lp);

        return c;
    }

    private LinearLayout horizontalCardRow() {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(2f);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        row.setLayoutParams(lp);

        return row;
    }

    private TextView section(String value) {

        TextView t = text(
                value,
                17,
                WHITE
        );

        t.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        t.setPadding(
                dp(2),
                dp(15),
                dp(2),
                dp(7)
        );

        return t;
    }

    private void buildApp() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.addView(
                buildHeader(),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(66)
                )
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(13),
                dp(3),
                dp(13),
                dp(18)
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

        root.addView(buildBottomNavigation());

        setContentView(root);

        showHome();
    }

    private View buildHeader() {

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(5)
        );
        header.setBackgroundColor(BG);

        ImageView logo = new ImageView(this);

        try {
            logo.setImageResource(
                    R.drawable.maya_logo
            );
        } catch (Exception ignored) {
        }

        LinearLayout.LayoutParams logoLp =
                new LinearLayout.LayoutParams(
                        dp(43),
                        dp(43)
                );

        header.addView(logo, logoLp);

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(dp(8), 0, 0, 0);

        TextView maya = text(
                "MAYA  CONTROL",
                17,
                WHITE
        );

        maya.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView sub = text(
                "YOUR AI LIFE COMPANION",
                8,
                CYAN
        );

        names.addView(maya);
        names.addView(sub);

        header.addView(
                names,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView online = text(
                "● Online",
                10,
                GREEN
        );

        online.setGravity(Gravity.CENTER);
        online.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(5)
        );

        online.setBackground(
                bg(
                        Color.rgb(5, 45, 38),
                        Color.rgb(0, 150, 110),
                        20
                )
        );

        header.addView(online);

        return header;
    }

    private View buildBottomNavigation() {

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );
        nav.setBackground(
                bg(
                        Color.rgb(4, 11, 22),
                        Color.rgb(0, 75, 130),
                        18
                )
        );

        nav.addView(
                navButton(
                        "⌂",
                        "Home",
                        v -> showHome()
                ),
                weightParams()
        );

        nav.addView(
                navButton(
                        "▦",
                        "Dashboard",
                        v -> showDashboard()
                ),
                weightParams()
        );

        nav.addView(
                navButton(
                        "◉",
                        "Memory",
                        v -> showMemory()
                ),
                weightParams()
        );

        nav.addView(
                navButton(
                        "⚙",
                        "Settings",
                        v -> showSettings()
                ),
                weightParams()
        );

        return nav;
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(
                0,
                dp(62),
                1
        );
    }

    private View navButton(
            String icon,
            String name,
            View.OnClickListener listener
    ) {

        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setClickable(true);
        b.setOnClickListener(listener);

        TextView i = text(
                icon,
                23,
                CYAN
        );

        i.setGravity(Gravity.CENTER);

        TextView n = text(
                name,
                9,
                WHITE
        );

        n.setGravity(Gravity.CENTER);

        b.addView(i);
        b.addView(n);

        return b;
    }

    private void clearContent() {
        content.removeAllViews();
    }

    private void showHome() {

        clearContent();

        pageTitle = titleText("Home");
        content.addView(pageTitle);

        TextView hello = text(
                "Hello, User",
                25,
                WHITE
        );

        hello.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        content.addView(hello);

        TextView intro = text(
                "I'm MAYA, your AI assistant.\nHow can I help you today?",
                14,
                MUTED
        );

        intro.setPadding(0, dp(2), 0, dp(12));
        content.addView(intro);

        LinearLayout statusCard = card();

        TextView st = text(
                "● MAYA ONLINE",
                14,
                GREEN
        );

        st.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        statusCard.addView(st);

        TextView st2 = text(
                "Think  •  Speak  •  Control",
                12,
                CYAN
        );

        st2.setPadding(0, dp(7), 0, 0);
        statusCard.addView(st2);

        content.addView(statusCard);

        LinearLayout row1 = horizontalCardRow();

        row1.addView(
                statusSmallCard(
                        "✦",
                        "AI Provider",
                        MayaCore.getProvider(this),
                        CYAN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                )
        );

        row1.addView(
                statusSmallCard(
                        "🎙",
                        "Voice Service",
                        "Ready",
                        GREEN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                )
        );

        content.addView(row1);

        LinearLayout row2 = horizontalCardRow();

        row2.addView(
                statusSmallCard(
                        "◉",
                        "Memory",
                        "Ready",
                        CYAN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                )
        );

        row2.addView(
                statusSmallCard(
                        "☎",
                        "Phone Control",
                        "Ready",
                        GREEN
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                )
        );

        content.addView(row2);

        LinearLayout row3 = horizontalCardRow();

        row3.addView(
                statusSmallCard(
                        "♿",
                        "Accessibility",
                        isAccessibilityEnabled()
                                ? "Active"
                                : "Off",
                        isAccessibilityEnabled()
                                ? GREEN
                                : MUTED
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                )
        );

        content.addView(row3);

        TextView voiceTitle = section(
                "MAYA VOICE"
        );

        content.addView(voiceTitle);

        LinearLayout voice = new LinearLayout(this);
        voice.setOrientation(LinearLayout.VERTICAL);
        voice.setGravity(Gravity.CENTER);
        voice.setPadding(
                dp(10),
                dp(16),
                dp(10),
                dp(16)
        );

        voice.setBackground(
                bg(
                        Color.rgb(5, 13, 30),
                        Color.rgb(45, 90, 210),
                        24
                )
        );

        TextView mic = text(
                "◉",
                58,
                CYAN
        );

        mic.setGravity(Gravity.CENTER);

        TextView tap = text(
                "Tap to Speak",
                16,
                WHITE
        );

        tap.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tap.setGravity(Gravity.CENTER);

        TextView wake = text(
                "Say  \"Hello Maya\"  or  \"Hello Boss\"",
                11,
                MUTED
        );

        wake.setGravity(Gravity.CENTER);

        voice.addView(mic);
        voice.addView(tap);
        voice.addView(wake);

        voice.setClickable(true);
        voice.setOnClickListener(v -> startVoice());

        content.addView(
                voice,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(190)
                )
        );

        TextView quick = section(
                "Quick Actions"
        );

        content.addView(quick);

        LinearLayout q1 = horizontalCardRow();

        q1.addView(
                quickCard(
                        "▦",
                        "Apps",
                        "Open apps",
                        v -> showApps()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        q1.addView(
                quickCard(
                        "☎",
                        "Phone",
                        "Make a call",
                        v -> openDialer()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        q1.addView(
                quickCard(
                        "●",
                        "Messages",
                        "Send SMS",
                        v -> openMessages()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        q1.addView(
                quickCard(
                        "▣",
                        "Camera",
                        "Take photo",
                        v -> openCamera()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        content.addView(q1);
    }

    private View statusSmallCard(
            String icon,
            String name,
            String value,
            int valueColor
    ) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(
                dp(10),
                dp(7),
                dp(5),
                dp(5)
        );

        box.setBackground(
                bg(
                        CARD2,
                        Color.rgb(0, 80, 145),
                        13
                )
        );

        TextView top = text(
                icon + "  " + name,
                10,
                MUTED
        );

        TextView val = text(
                value,
                13,
                valueColor
        );

        val.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        box.addView(top);
        box.addView(val);

        return box;
    }

    private View quickCard(
            String icon,
            String name,
            String sub,
            View.OnClickListener click
    ) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(
                dp(3),
                dp(6),
                dp(3),
                dp(5)
        );

        box.setBackground(
                bg(
                        CARD2,
                        Color.rgb(0, 92, 165),
                        13
                )
        );

        box.setClickable(true);
        box.setOnClickListener(click);

        TextView i = text(
                icon,
                21,
                CYAN
        );

        i.setGravity(Gravity.CENTER);

        TextView n = text(
                name,
                10,
                WHITE
        );

        n.setGravity(Gravity.CENTER);

        TextView s = text(
                sub,
                7,
                MUTED
        );

        s.setGravity(Gravity.CENTER);

        box.addView(i);
        box.addView(n);
        box.addView(s);

        return box;
    }

    private void showDashboard() {

        clearContent();

        content.addView(
                titleText("Dashboard")
        );

        LinearLayout info = horizontalCardRow();

        info.addView(
                dashboardMetric(
                        "🔋",
                        "Battery",
                        getBatteryLevel() + "%"
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(82),
                        1
                )
        );

        info.addView(
                dashboardMetric(
                        "▣",
                        "Storage",
                        getStorageInfo()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(82),
                        1
                )
        );

        content.addView(info);

        LinearLayout info2 = horizontalCardRow();

        info2.addView(
                dashboardMetric(
                        "⌁",
                        "Network",
                        getNetworkName()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(82),
                        1
                )
        );

        info2.addView(
                dashboardMetric(
                        "●",
                        "Location",
                        "Enabled"
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(82),
                        1
                )
        );

        content.addView(info2);

        content.addView(
                section("Quick Actions")
        );

        LinearLayout q1 = horizontalCardRow();

        q1.addView(
                quickCard(
                        "▦",
                        "Apps",
                        "Launch app",
                        v -> showApps()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        q1.addView(
                quickCard(
                        "☎",
                        "Call",
                        "Make phone call",
                        v -> openDialer()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        content.addView(q1);

        LinearLayout q2 = horizontalCardRow();

        q2.addView(
                quickCard(
                        "●",
                        "Messages",
                        "Send message",
                        v -> openMessages()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        q2.addView(
                quickCard(
                        "▣",
                        "Camera",
                        "Take a photo",
                        v -> openCamera()
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(85),
                        1
                )
        );

        content.addView(q2);

        content.addView(
                section("Recent Activity")
        );

        addActivity(
                "▶",
                "You opened YouTube",
                "Recent"
        );

        addActivity(
                "☎",
                "MAYA Phone Control ready",
                "Now"
        );

        addActivity(
                "◉",
                "MAYA memory is ready",
                "Now"
        );
    }

    private View dashboardMetric(
            String icon,
            String name,
            String value
    ) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(6), dp(6), dp(6), dp(6));

        box.setBackground(
                bg(
                        CARD2,
                        Color.rgb(0, 85, 160),
                        14
                )
        );

        TextView i = text(icon, 22, CYAN);
        i.setGravity(Gravity.CENTER);

        TextView n = text(name, 9, MUTED);
        n.setGravity(Gravity.CENTER);

        TextView v = text(value, 13, WHITE);
        v.setGravity(Gravity.CENTER);
        v.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        box.addView(i);
        box.addView(n);
        box.addView(v);

        return box;
    }

    private void addActivity(
            String icon,
            String name,
            String time
    ) {

        LinearLayout a = card();

        TextView t = text(
                icon + "   " + name,
                13,
                WHITE
        );

        TextView tm = text(
                time,
                9,
                MUTED
        );

        a.addView(t);
        a.addView(tm);

        content.addView(a);
    }

    private void showMemory() {

        clearContent();

        content.addView(
                titleText("MAYA Memory")
        );

        LinearLayout hero = card();
        hero.setGravity(Gravity.CENTER);

        TextView brain = text(
                "◉",
                60,
                PURPLE
        );

        brain.setGravity(Gravity.CENTER);

        TextView h = text(
                "MAYA MEMORY",
                19,
                WHITE
        );

        h.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        h.setGravity(Gravity.CENTER);

        TextView d = text(
                "Learns from your behavior,\npreferences and conversations.",
                11,
                MUTED
        );

        d.setGravity(Gravity.CENTER);

        hero.addView(brain);
        hero.addView(h);
        hero.addView(d);

        content.addView(hero);

        memoryItem(
                "♟",
                "Personal Information",
                "Name, preferences, habits"
        );

        memoryItem(
                "▣",
                "Conversation History",
                "Past chats and commands"
        );

        memoryItem(
                "✦",
                "Smart Suggestions",
                "Learn from your patterns"
        );

        Button clear = new Button(this);
        clear.setText("MANAGE / CLEAR MEMORY");
        clear.setTextColor(WHITE);
        clear.setAllCaps(false);
        clear.setBackground(
                bg(
                        Color.rgb(95, 20, 230),
                        CYAN,
                        25
                )
        );

        clear.setOnClickListener(v -> {

            MayaMemory.clear(this);

            Toast.makeText(
                    this,
                    "MAYA memory cleared",
                    Toast.LENGTH_SHORT
            ).show();
        });

        content.addView(
                clear,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );
    }

    private void memoryItem(
            String icon,
            String title,
            String description
    ) {

        LinearLayout box = card();

        TextView t = text(
                icon + "   " + title,
                14,
                WHITE
        );

        t.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView d = text(
                description,
                10,
                MUTED
        );

        box.addView(t);
        box.addView(d);

        content.addView(box);
    }

    private void showSettings() {

        clearContent();

        content.addView(
                titleText("Settings")
        );

        settingsItem(
                "✦",
                "AI & API Settings",
                "Gemini / OpenAI",
                v -> showAISettings()
        );

        settingsItem(
                "🎙",
                "Voice & Audio",
                "MAYA Voice Service",
                v -> showVoiceSettings()
        );

        settingsItem(
                "▦",
                "Accessibility",
                "MAYA Android Agent",
                v -> openAccessibility()
        );

        settingsItem(
                "▣",
                "Permissions",
                "App permissions",
                v -> openAppSettings()
        );

        settingsItem(
                "◉",
                "Appearance",
                "MAYA futuristic UI",
                v -> Toast.makeText(
                        this,
                        "MAYA appearance is active",
                        Toast.LENGTH_SHORT
                ).show()
        );

        settingsItem(
                "ⓘ",
                "About",
                "MAYA Control v6",
                v -> showAbout()
        );

        LinearLayout brand = card();
        brand.setGravity(Gravity.CENTER);

        TextView b1 = text(
                "◆ MAYA CONTROL ◆",
                19,
                CYAN
        );

        b1.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView b2 = text(
                "Your AI Life Companion",
                11,
                MUTED
        );

        brand.addView(b1);
        brand.addView(b2);

        content.addView(brand);
    }

    private void settingsItem(
            String icon,
            String title,
            String description,
            View.OnClickListener click
    ) {

        LinearLayout box = card();
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setClickable(true);
        box.setOnClickListener(click);

        TextView i = text(
                icon,
                26,
                CYAN
        );

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(
                dp(13),
                0,
                0,
                0
        );

        TextView t = text(
                title,
                14,
                WHITE
        );

        t.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView d = text(
                description,
                10,
                MUTED
        );

        names.addView(t);
        names.addView(d);

        box.addView(
                i,
                new LinearLayout.LayoutParams(
                        dp(35),
                        -2
                )
        );

        box.addView(
                names,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView arrow = text(
                "›",
                25,
                CYAN
        );

        box.addView(arrow);

        content.addView(box);
    }

    private void showAISettings() {

        clearContent();

        content.addView(
                titleText("AI Provider")
        );

        LinearLayout provider = card();

        geminiRadio = new RadioButton(this);
        geminiRadio.setText("  Google Gemini");
        geminiRadio.setTextColor(WHITE);
        geminiRadio.setTextSize(16);

        openAIRadio = new RadioButton(this);
        openAIRadio.setText("  OpenAI (ChatGPT)");
        openAIRadio.setTextColor(WHITE);
        openAIRadio.setTextSize(16);

        provider.addView(geminiRadio);
        provider.addView(openAIRadio);

        String current =
                MayaCore.getProvider(this);

        if ("OpenAI".equalsIgnoreCase(current)) {
            openAIRadio.setChecked(true);
        } else {
            geminiRadio.setChecked(true);
        }

        content.addView(provider);

        content.addView(
                section("Gemini API Configuration")
        );

        geminiKey = passwordField(
                "Gemini API Key",
                MayaCore.getGeminiKey(this)
        );

        content.addView(geminiKey);

        geminiModel = normalField(
                "Gemini Model",
                MayaCore.getGeminiModel(this)
        );

        content.addView(geminiModel);

        content.addView(
                section("OpenAI API Configuration")
        );

        openAIKey = passwordField(
                "OpenAI API Key",
                MayaCore.getOpenAIKey(this)
        );

        content.addView(openAIKey);

        openAIModel = normalField(
                "OpenAI Model",
                MayaCore.getOpenAIModel(this)
        );

        content.addView(openAIModel);

        Button save = new Button(this);
        save.setText("SAVE SETTINGS");
        save.setTextColor(WHITE);
        save.setAllCaps(false);
        save.setTextSize(15);
        save.setBackground(
                bg(
                        Color.rgb(90, 10, 230),
                        CYAN,
                        25
                )
        );

        save.setOnClickListener(v -> {

            String selected =
                    geminiRadio.isChecked()
                            ? "Gemini"
                            : "OpenAI";

            MayaCore.saveSettings(
                    this,
                    geminiKey.getText().toString(),
                    geminiModel.getText().toString(),
                    openAIKey.getText().toString(),
                    openAIModel.getText().toString(),
                    selected
            );

            Toast.makeText(
                    this,
                    "MAYA AI settings saved",
                    Toast.LENGTH_SHORT
            ).show();
        });

        content.addView(
                save,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        Button test = new Button(this);
        test.setText("TEST SELECTED AI");
        test.setTextColor(WHITE);
        test.setAllCaps(false);

        test.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Testing " +
                            MayaCore.getProvider(this) +
                            "...",
                    Toast.LENGTH_SHORT
            ).show();

            MayaCore.ask(
                    this,
                    "Reply with exactly: MAYA ONLINE",
                    (ok, answer) ->
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            this,
                                            answer,
                                            Toast.LENGTH_LONG
                                    ).show()
                            )
            );
        });

        content.addView(test);
    }

    private EditText normalField(
            String hint,
            String value
    ) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setTextSize(13);
        e.setSingleLine(true);

        e.setPadding(
                dp(15),
                0,
                dp(15),
                0
        );

        e.setBackground(
                bg(
                        CARD2,
                        Color.rgb(0, 95, 170),
                        13
                )
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                );

        lp.setMargins(
                0,
                dp(5),
                0,
                dp(7)
        );

        e.setLayoutParams(lp);

        return e;
    }

    private EditText passwordField(
            String hint,
            String value
    ) {

        EditText e =
                normalField(hint, value);

        e.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        return e;
    }

    private void showVoiceSettings() {

        clearContent();

        content.addView(
                titleText("Voice Service")
        );

        LinearLayout hero = card();
        hero.setGravity(Gravity.CENTER);

        TextView mic = text(
                "◉",
                70,
                CYAN
        );

        mic.setGravity(Gravity.CENTER);

        TextView title = text(
                "Voice Recognition",
                17,
                WHITE
        );

        title.setGravity(Gravity.CENTER);

        TextView status = text(
                "Ready to listen",
                11,
                GREEN
        );

        status.setGravity(Gravity.CENTER);

        hero.addView(mic);
        hero.addView(title);
        hero.addView(status);

        content.addView(hero);

        voiceSetting(
                "Wake Word",
                "\"Hello Maya\"",
                true
        );

        voiceSetting(
                "Voice Response",
                "Natural & Friendly",
                true
        );

        voiceSetting(
                "Language",
                "Hindi / English",
                false
        );

        Button start = new Button(this);
        start.setText("START MAYA VOICE");
        start.setTextColor(WHITE);
        start.setAllCaps(false);
        start.setBackground(
                bg(
                        Color.rgb(80, 15, 230),
                        CYAN,
                        25
                )
        );

        start.setOnClickListener(
                v -> startVoice()
        );

        content.addView(
                start,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        Button overlay = new Button(this);
        overlay.setText(
                "ENABLE FLOATING MAYA POPUP"
        );
        overlay.setAllCaps(false);
        overlay.setTextColor(WHITE);

        overlay.setOnClickListener(
                v -> enableOverlay()
        );

        content.addView(overlay);
    }

    private void voiceSetting(
            String name,
            String value,
            boolean enabled
    ) {

        LinearLayout row = card();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);

        TextView n = text(
                name,
                13,
                WHITE
        );

        TextView v = text(
                value,
                9,
                MUTED
        );

        names.addView(n);
        names.addView(v);

        row.addView(
                names,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Switch sw = new Switch(this);
        sw.setChecked(enabled);

        row.addView(sw);

        content.addView(row);
    }

    private void showApps() {

        final String[] apps = {
                "YouTube",
                "Instagram",
                "WhatsApp",
                "Telegram",
                "Chrome",
                "Gmail",
                "Canva"
        };

        new AlertDialog.Builder(this)
                .setTitle("MAYA Apps")
                .setItems(
                        apps,
                        (dialog, which) -> {
                            String app =
                                    apps[which]
                                            .toLowerCase(
                                                    Locale.US
                                            );

                            MayaCore.openApp(
                                    this,
                                    app
                            );
                        }
                )
                .show();
    }

    private void openDialer() {

        try {
            Intent i = new Intent(
                    Intent.ACTION_DIAL
            );

            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Dialer unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openMessages() {

        try {
            Intent i = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("sms:")
            );

            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Messaging app unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openCamera() {

        try {
            Intent i = new Intent(
                    "android.media.action.IMAGE_CAPTURE"
            );

            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Camera unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void startVoice() {

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    200
            );

            return;
        }

        try {

            Intent i = new Intent(
                    this,
                    MayaVoiceService.class
            );

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(i);
            } else {
                startService(i);
            }

            Toast.makeText(
                    this,
                    "MAYA Voice Service started",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to start MAYA Voice",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void enableOverlay() {

        if (Build.VERSION.SDK_INT >= 23 &&
                !Settings.canDrawOverlays(this)) {

            Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse(
                            "package:" +
                                    getPackageName()
                    )
            );

            startActivity(i);

        } else {

            Toast.makeText(
                    this,
                    "MAYA overlay is already enabled",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openAccessibility() {

        try {

            Intent i = new Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
            );

            startActivity(i);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Accessibility settings unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openAppSettings() {

        try {

            Intent i = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse(
                            "package:" +
                                    getPackageName()
                    )
            );

            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "App settings unavailable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showAbout() {

        new AlertDialog.Builder(this)
                .setTitle("MAYA CONTROL")
                .setMessage(
                        "MAYA Control\n\n" +
                        "Your AI Life Companion\n\n" +
                        "Think • Speak • Control\n\n" +
                        "AI Brain • Voice • Memory • " +
                        "Android Agent"
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private int getBatteryLevel() {

        android.os.BatteryManager bm =
                (android.os.BatteryManager)
                        getSystemService(
                                BATTERY_SERVICE
                        );

        if (bm == null) return 0;

        return bm.getIntProperty(
                android.os.BatteryManager
                        .BATTERY_PROPERTY_CAPACITY
        );
    }

    private String getStorageInfo() {

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

            if (total <= 0) return "Ready";

            long used = total - free;

            int percent =
                    (int)
                            ((used * 100L) /
                                    total);

            return percent + "%";

        } catch (Exception e) {
            return "Ready";
        }
    }

    private String getNetworkName() {

        try {

            ConnectivityManager cm =
                    (ConnectivityManager)
                            getSystemService(
                                    CONNECTIVITY_SERVICE
                            );

            if (cm == null) return "Offline";

            Network network =
                    cm.getActiveNetwork();

            if (network == null) {
                return "Offline";
            }

            NetworkCapabilities caps =
                    cm.getNetworkCapabilities(
                            network
                    );

            if (caps == null) {
                return "Online";
            }

            if (caps.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            )) {
                return "Wi-Fi";
            }

            if (caps.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            )) {
                return "Mobile";
            }

            return "Online";

        } catch (Exception e) {
            return "Online";
        }
    }

    private boolean isAccessibilityEnabled() {

        try {

            String enabled =
                    Settings.Secure.getString(
                            getContentResolver(),
                            Settings.Secure
                                    .ENABLED_ACCESSIBILITY_SERVICES
                    );

            if (enabled == null) {
                return false;
            }

            return enabled.toLowerCase(
                            Locale.US
                    )
                    .contains(
                            getPackageName()
                                    .toLowerCase(
                                            Locale.US
                                    )
                    );

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (content != null) {
            /*
             * Keep the currently selected page stable.
             * Accessibility status is refreshed when Home
             * or Dashboard is opened again.
             */
        }
    }
}
