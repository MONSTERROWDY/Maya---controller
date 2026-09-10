package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(248,249,252);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(25,28,35);
    private static final int MUTED = Color.rgb(105,112,125);
    private static final int GOLD = Color.rgb(190,140,35);
    private static final int GREEN = Color.rgb(32,170,105);
    private static final int BORDER = Color.rgb(225,228,235);

    private LinearLayout page;
    private TextView status;
    private EditText chatInput;
    private LinearLayout chatMessages;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        Window w = getWindow();
        w.setStatusBarColor(BG);
        w.setNavigationBarColor(Color.WHITE);

        buildShell();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                if ("com.veer.maya.REPLY".equals(i.getAction())) {
                    String s = i.getStringExtra("text");
                    if (s != null) addChat(s, false);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                    receiver,
                    new IntentFilter("com.veer.maya.REPLY"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    receiver,
                    new IntentFilter("com.veer.maya.REPLY")
            );
        }

        requestPermissionsIfNeeded();
        showHome();

        try {
            MayaVoiceService.start(this);
            new android.os.Handler().postDelayed(() ->
                    MayaVoiceService.say(
                            "Hello boss, कैसे हो आप? ठीक है ना?"
                    ), 1400);
        } catch (Exception ignored) {}
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(20,18,18,12);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);

        TextView mark = text("✦", 28, GOLD, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(Color.WHITE, 50, BORDER));

        brand.addView(mark, new LinearLayout.LayoutParams(52,52));

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(12,0,0,0);

        TextView logo = text("MAYA", 24, TEXT, true);
        TextView sub = text("AI PHONE AGENT", 10, MUTED, true);

        names.addView(logo);
        names.addView(sub);
        brand.addView(names);

        header.addView(
                brand,
                new LinearLayout.LayoutParams(0,-2,1)
        );

        status = text("● READY", 11, GREEN, true);
        header.addView(status);

        root.addView(header);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(15,3,15,8);

        root.addView(
                page,
                new LinearLayout.LayoutParams(-1,0,1)
        );

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(8,8,8,10);
        nav.setBackgroundColor(Color.WHITE);

        Button home = navButton("⌂\nHOME");
        Button chat = navButton("◉\nCHAT");
        Button live = navButton("●\nLIVE");
        Button settings = navButton("⋮\nMENU");

        nav.addView(home, weight());
        nav.addView(chat, weight());
        nav.addView(live, weight());
        nav.addView(settings, weight());

        home.setOnClickListener(v -> showHome());
        chat.setOnClickListener(v -> showChat());
        live.setOnClickListener(v -> showLive());
        settings.setOnClickListener(v -> showSettings());

        root.addView(nav);
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0,68,1);
    }

    private void showHome() {
        page.removeAllViews();

        ScrollView sc = new ScrollView(this);
        LinearLayout c = vertical();
        sc.addView(c);
        page.addView(sc, new LinearLayout.LayoutParams(-1,-1));

        space(c,15);

        LinearLayout hero = card();
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(20,24,20,24);

        LinearLayout inner = vertical();
        inner.setGravity(Gravity.CENTER);

        TextView logo = text("✦",58,GOLD,true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(Color.rgb(255,250,237),100,BORDER));

        inner.addView(logo,new LinearLayout.LayoutParams(128,128));

        space(inner,15);

        TextView title = text(
                "Hello boss 👋",
                28,TEXT,true
        );
        title.setGravity(Gravity.CENTER);
        inner.addView(title);

        TextView greeting = text(
                "कैसे हो आप? ठीक है ना?",
                17,MUTED,false
        );
        greeting.setGravity(Gravity.CENTER);
        inner.addView(greeting);

        TextView desc = text(
                "MAYA आपका personal AI phone agent है।\n"
                + "आप बोलिए — MAYA समझेगी, plan बनाएगी और available Android controls से action करेगी।",
                14,MUTED,false
        );
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(10,12,10,8);
        inner.addView(desc);

        hero.addView(inner);
        c.addView(hero);

        space(c,14);

        LinearLayout actions = card();
        TextView at = text("QUICK ACTIONS",13,MUTED,true);
        actions.addView(at);

        Button live = actionButton(
                "🎙  Start Live Voice",
                "बोलकर MAYA से काम करवाएँ"
        );
        Button chat = actionButton(
                "💬  Open MAYA Chat",
                "लिखकर command दें"
        );
        Button control = actionButton(
                "📱  Enable Phone Control",
                "Accessibility से phone control"
        );

        live.setOnClickListener(v -> showLive());
        chat.setOnClickListener(v -> showChat());
        control.setOnClickListener(v -> openAccessibility());

        actions.addView(live);
        actions.addView(chat);
        actions.addView(control);

        c.addView(actions);

        space(c,14);

        LinearLayout capabilities = card();
        capabilities.addView(
                text("MAYA CAPABILITIES",13,MUTED,true)
        );

        String[] list = {
                "✓ Hindi / English / Hinglish",
                "✓ Maithili / Bhojpuri understanding",
                "✓ App open और navigation",
                "✓ Tap / click / type / scroll",
                "✓ Web search और URLs",
                "✓ Multi-step workflows",
                "✓ Calls / messages — confirmation के साथ",
                "✓ Persistent conversation memory",
                "✓ Live voice assistant",
                "✓ Accessibility phone control"
        };

        for(String s:list) {
            TextView t = text(s,14,TEXT,false);
            t.setPadding(4,9,4,9);
            capabilities.addView(t);
        }

        c.addView(capabilities);
    }

    private void showChat() {
        page.removeAllViews();

        LinearLayout c = vertical();

        TextView title = text("MAYA CHAT",22,TEXT,true);
        c.addView(title);

        TextView sub = text(
                "Natural language में command दें",
                13,MUTED,false
        );
        c.addView(sub);

        ScrollView sc = new ScrollView(this);
        chatMessages = vertical();
        chatMessages.setPadding(2,12,2,12);
        sc.addView(chatMessages);

        c.addView(
                sc,
                new LinearLayout.LayoutParams(-1,0,1)
        );

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        chatInput = new EditText(this);
        chatInput.setHint("MAYA, क्या करना है?");
        chatInput.setTextColor(TEXT);
        chatInput.setHintTextColor(MUTED);
        chatInput.setTextSize(15);
        chatInput.setSingleLine(false);
        chatInput.setPadding(16,12,16,12);
        chatInput.setBackground(round(Color.WHITE,22,BORDER));

        inputRow.addView(
                chatInput,
                new LinearLayout.LayoutParams(0,58,1)
        );

        Button send = new Button(this);
        send.setText("SEND");
        send.setTextColor(Color.WHITE);
        send.setTextSize(12);
        send.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        send.setBackground(round(GOLD,20,GOLD));

        LinearLayout.LayoutParams sp =
                new LinearLayout.LayoutParams(92,58);
        sp.setMargins(8,0,0,0);
        inputRow.addView(send,sp);

        send.setOnClickListener(v -> sendChat());

        c.addView(inputRow);
        page.addView(c);

        addChat(
                "Hello boss. बताइए क्या करना है?",
                false
        );
    }

    private void sendChat() {
        if(chatInput == null) return;

        String q = chatInput.getText().toString().trim();
        if(q.isEmpty()) return;

        addChat(q,true);
        chatInput.setText("");

        status.setText("● THINKING");
        status.setTextColor(GOLD);

        MayaCore.process(this,q);
    }

    private void addChat(String s, boolean user) {
        if(chatMessages == null) return;

        TextView t = text(
                (user ? "YOU\n" : "MAYA\n") + s,
                15,
                user ? TEXT : TEXT,
                false
        );

        t.setPadding(15,13,15,13);
        t.setBackground(
                round(
                        user ? Color.rgb(242,243,246) : Color.WHITE,
                        20,
                        BORDER
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,5,0,5);

        chatMessages.addView(t,p);
    }

    private void showLive() {
        page.removeAllViews();

        LinearLayout c = vertical();
        c.setGravity(Gravity.CENTER_HORIZONTAL);

        space(c,30);

        TextView logo = text("✦",60,GOLD,true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(Color.WHITE,100,BORDER));

        c.addView(logo,new LinearLayout.LayoutParams(150,150));

        space(c,20);

        TextView title = text("MAYA LIVE",30,TEXT,true);
        title.setGravity(Gravity.CENTER);
        c.addView(title);

        TextView desc = text(
                "Live voice mode active\n"
                + "आप normal conversation की तरह MAYA से बात कर सकते हैं।",
                15,MUTED,false
        );
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(10,12,10,25);
        c.addView(desc);

        Button start = actionButton(
                "🎙  START / RESUME LISTENING",
                "MAYA voice commands सुनेगी"
        );

        Button stop = actionButton(
                "⏹  STOP LIVE MODE",
                "Voice service बंद करें"
        );

        start.setOnClickListener(v -> {
            MayaVoiceService.setLiveMode(this,true);
            MayaVoiceService.start(this);
            MayaVoiceService.say(
                    "हाँ boss, मैं सुन रही हूँ। बोलिए।"
            );
            Toast.makeText(
                    this,
                    "MAYA Live ON",
                    Toast.LENGTH_SHORT
            ).show();
        });

        stop.setOnClickListener(v -> {
            MayaVoiceService.setLiveMode(this,false);
            Toast.makeText(
                    this,
                    "Live mode OFF",
                    Toast.LENGTH_SHORT
            ).show();
        });

        c.addView(start);
        c.addView(stop);

        space(c,20);

        LinearLayout info = card();
        info.addView(text(
                "EXAMPLE COMMANDS",
                13,MUTED,true
        ));

        String[] examples = {
                "“MAYA YouTube खोलो”",
                "“MAYA Google पर Bitcoin price search करो”",
                "“MAYA Chrome खोलो और ये चीज search करो”",
                "“MAYA वापस जाओ”",
                "“MAYA नीचे scroll करो”",
                "“MAYA आज ये करना है...”"
        };

        for(String e:examples)
            info.addView(text(e,14,TEXT,false));

        c.addView(info);

        page.addView(c);
    }

    private void showSettings() {
        page.removeAllViews();

        ScrollView sc = new ScrollView(this);
        LinearLayout c = vertical();
        sc.addView(c);
        page.addView(sc);

        c.addView(text("MAYA SETTINGS",25,TEXT,true));
        c.addView(text(
                "Assistant को अपने हिसाब से configure करें",
                13,MUTED,false
        ));

        space(c,12);

        LinearLayout ai = card();

        ai.addView(text(
                "AI MODEL & API",
                13,MUTED,true
        ));

        EditText key = new EditText(this);
        key.setHint("OpenAI API key");
        key.setSingleLine(true);
        key.setTextColor(TEXT);
        key.setHintTextColor(MUTED);
        key.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        key.setPadding(15,10,15,10);
        key.setBackground(round(Color.WHITE,18,BORDER));

        String saved = MayaCore.getKey(this);
        if(saved != null) key.setText(saved);

        ai.addView(key);

        EditText model = new EditText(this);
        model.setHint("AI model");
        model.setSingleLine(true);
        model.setTextColor(TEXT);
        model.setHintTextColor(MUTED);
        model.setPadding(15,10,15,10);
        model.setBackground(round(Color.WHITE,18,BORDER));

        model.setText(MayaCore.getModel(this));
        ai.addView(model);

        Button save = actionButton(
                "🔐 SAVE AI CONNECTION",
                "API key phone में local save होगी"
        );

        save.setOnClickListener(v -> {
            String k = key.getText().toString().trim();
            String m = model.getText().toString().trim();

            if(k.isEmpty()) {
                Toast.makeText(
                        this,
                        "API key डालिए",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            MayaCore.saveKey(this,k);
            if(!m.isEmpty()) MayaCore.saveModel(this,m);

            Toast.makeText(
                    this,
                    "AI connection saved",
                    Toast.LENGTH_SHORT
            ).show();
        });

        ai.addView(save);
        c.addView(ai);

        space(c,12);

        settingCard(
                c,
                "👤 PROFILE SETTINGS",
                "Boss profile और assistant identity"
        );

        settingCard(
                c,
                "💬 CHAT SETTINGS",
                "Conversation और response preferences"
        );

        settingCard(
                c,
                "🎙 VOICE SETTINGS",
                "Live voice, language और speech"
        );

        settingCard(
                c,
                "🧠 MEMORY",
                "MAYA की persistent conversation memory"
        );

        settingCard(
                c,
                "🔌 API & CONNECTIONS",
                "AI API और external integrations"
        );

        settingCard(
                c,
                "🧩 PLUGINS",
                "Canva, GitHub और future integrations"
        );

        Button accessibility = actionButton(
                "📱 PHONE CONTROL",
                "Android Accessibility permission खोलें"
        );
        accessibility.setOnClickListener(
                v -> openAccessibility()
        );
        c.addView(accessibility);

        settingCard(
                c,
                "🔔 NOTIFICATIONS",
                "MAYA notification behaviour"
        );

        settingCard(
                c,
                "🎨 APPEARANCE",
                "Light interface और visual preferences"
        );

        settingCard(
                c,
                "🌐 LANGUAGE",
                "Hindi / English / Hinglish / regional language"
        );

        settingCard(
                c,
                "🔒 PRIVACY & SECURITY",
                "Permissions और confirmation controls"
        );

        settingCard(
                c,
                "ℹ ABOUT MAYA",
                "MAYA personal Android AI agent"
        );

        space(c,20);
    }

    private void settingCard(
            LinearLayout parent,
            String title,
            String subtitle
    ) {
        LinearLayout box = card();

        TextView a = text(title,15,TEXT,true);
        TextView b = text(subtitle,12,MUTED,false);

        box.addView(a);
        box.addView(b);

        parent.addView(box);
        space(parent,8);
    }

    private void openAccessibility() {
        try {
            startActivity(
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
            );
        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Accessibility settings नहीं खुलीं",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void requestPermissionsIfNeeded() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },100
                );
            }
        }

        if(Build.VERSION.SDK_INT >= 33) {
            if(checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },101
                );
            }
        }
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout card() {
        LinearLayout l = vertical();
        l.setPadding(16,15,16,15);
        l.setBackground(round(CARD,22,BORDER));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,5,0,5);

        l.setLayoutParams(p);
        return l;
    }

    private Button actionButton(
            String title,
            String subtitle
    ) {
        Button b = new Button(this);
        b.setText(title + "\n" + subtitle);
        b.setTextSize(13);
        b.setTextColor(TEXT);
        b.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        b.setAllCaps(false);
        b.setPadding(15,5,15,5);
        b.setBackground(round(
                Color.rgb(249,250,252),
                18,
                BORDER
        ));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,65);
        p.setMargins(0,5,0,5);
        b.setLayoutParams(p);

        return b;
    }

    private Button navButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(10);
        b.setTextColor(TEXT);
        b.setGravity(Gravity.CENTER);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    private TextView text(
            String s,
            float size,
            int color,
            boolean bold
    ) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );
        return t;
    }

    private GradientDrawable round(
            int color,
            float radius,
            int stroke
    ) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        g.setStroke(1,stroke);
        return g;
    }

    private void space(LinearLayout l,int h) {
        View v = new View(this);
        l.addView(
                v,
                new LinearLayout.LayoutParams(1,h)
        );
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(receiver);
        } catch(Exception ignored) {}
        super.onDestroy();
    }
}
