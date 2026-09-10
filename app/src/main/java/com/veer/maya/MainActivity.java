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

import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;

import android.text.InputType;

import android.view.Gravity;
import android.view.View;
import android.view.Window;

import android.view.inputmethod.EditorInfo;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG =
            Color.rgb(8,10,15);

    private static final int CARD =
            Color.rgb(18,22,30);

    private static final int CARD2 =
            Color.rgb(24,29,39);

    private static final int GOLD =
            Color.rgb(245,185,66);

    private static final int WHITE =
            Color.rgb(245,247,250);

    private static final int MUTED =
            Color.rgb(160,168,180);

    private static final int GREEN =
            Color.rgb(60,210,135);

    private static final int RED =
            Color.rgb(240,95,105);

    private LinearLayout root;
    private LinearLayout page;

    private TextView status;

    private EditText chatInput;
    private LinearLayout chatMessages;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(
                Color.rgb(10,13,18)
        );

        window.setNavigationBarColor(
                Color.rgb(8,10,15)
        );

        buildShell();

        receiver =
                new BroadcastReceiver(){

                    @Override
                    public void onReceive(
                            Context context,
                            Intent intent
                    ){

                        if(
                                "com.veer.maya.REPLY"
                                        .equals(intent.getAction())
                        ){

                            String text =
                                    intent.getStringExtra(
                                            "text"
                                    );

                            if(text != null){

                                addChat(
                                        text,
                                        false
                                );
                            }
                        }
                    }
                };

        registerReceiver(
                receiver,
                new IntentFilter(
                        "com.veer.maya.REPLY"
                ),
                RECEIVER_NOT_EXPORTED
        );

        requestPermissionsIfNeeded();

        showHome();
    }

    private void buildShell(){

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                BG
        );

        setContentView(root);

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                20,18,20,14
        );

        TextView logo =
                text(
                        "MAYA",
                        27,
                        GOLD,
                        true
                );

        header.addView(
                logo,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        status =
                text(
                        "● READY",
                        11,
                        MUTED,
                        true
                );

        header.addView(
                status
        );

        root.addView(
                header
        );

        page =
                new LinearLayout(this);

        page.setOrientation(
                LinearLayout.VERTICAL
        );

        page.setPadding(
                16,4,16,10
        );

        root.addView(
                page,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setPadding(
                10,8,10,12
        );

        bottom.setGravity(
                Gravity.CENTER
        );

        bottom.setBackgroundColor(
                CARD
        );

        Button home =
                navButton("⌂\nHOME");

        Button chat =
                navButton("◉\nCHAT");

        Button live =
                navButton("◌\nLIVE");

        Button settings =
                navButton("⚙\nSETTINGS");

        bottom.addView(
                home,
                weightParams()
        );

        bottom.addView(
                chat,
                weightParams()
        );

        bottom.addView(
                live,
                weightParams()
        );

        bottom.addView(
                settings,
                weightParams()
        );

        home.setOnClickListener(
                v -> showHome()
        );

        chat.setOnClickListener(
                v -> showChat()
        );

        live.setOnClickListener(
                v -> showLive()
        );

        settings.setOnClickListener(
                v -> showSettings()
        );

        root.addView(
                bottom
        );
    }

    private LinearLayout.LayoutParams weightParams(){

        return new LinearLayout.LayoutParams(
                0,
                64,
                1
        );
    }

    private void showHome(){

        page.removeAllViews();

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                vertical();

        scroll.addView(
                content
        );

        page.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        Space top =
                new Space(this);

        content.addView(
                top,
                new LinearLayout.LayoutParams(
                        1,
                        20
                )
        );

        LinearLayout orb =
                new LinearLayout(this);

        orb.setGravity(
                Gravity.CENTER
        );

        TextView circle =
                text(
                        "M",
                        54,
                        GOLD,
                        true
                );

        circle.setGravity(
                Gravity.CENTER
        );

        circle.setBackground(
                rounded(
                        CARD2,
                        200
                )
        );

        orb.addView(
                circle,
                new LinearLayout.LayoutParams(
                        138,
                        138
                )
        );

        content.addView(
                orb
        );

        TextView title =
                text(
                        "Hello, I'm MAYA",
                        27,
                        WHITE,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        content.addView(
                title
        );

        TextView subtitle =
                text(
                        "Your personal AI phone assistant",
                        15,
                        MUTED,
                        false
                );

        subtitle.setGravity(
                Gravity.CENTER
        );

        content.addView(
                subtitle
        );

        Space gap =
                new Space(this);

        content.addView(
                gap,
                new LinearLayout.LayoutParams(
                        1,
                        24
                )
        );

        LinearLayout statusCard =
                card();

        TextView st =
                text(
                        "SYSTEM STATUS",
                        12,
                        GOLD,
                        true
                );

        statusCard.addView(
                st
        );

        statusCard.addView(
                text(
                        getAccessStatus() +
                                "\n" +
                                getVoiceStatus() +
                                "\n" +
                                getApiStatus(),
                        14,
                        WHITE,
                        false
                )
        );

        content.addView(
                statusCard
        );

        Space gap2 =
                new Space(this);

        content.addView(
                gap2,
                new LinearLayout.LayoutParams(
                        1,
                        14
                )
        );

        Button start =
                primaryButton(
                        "🎙  START LIVE VOICE"
                );

        start.setOnClickListener(
                v -> startLive()
        );

        content.addView(
                start
        );

        Button openChat =
                secondaryButton(
                        "💬  OPEN MAYA CHAT"
                );

        openChat.setOnClickListener(
                v -> showChat()
        );

        content.addView(
                openChat
        );

        LinearLayout capabilities =
                card();

        capabilities.addView(
                text(
                        "WHAT MAYA CAN DO",
                        12,
                        GOLD,
                        true
                )
        );

        capabilities.addView(
                text(
                        "• Natural AI conversation\n" +
                        "• Hindi / English / Hinglish\n" +
                        "• Maithili / Bhojpuri understanding\n" +
                        "• Open apps and control visible UI\n" +
                        "• Web search\n" +
                        "• Voice conversation\n" +
                        "• Persistent conversation memory\n" +
                        "• Confirmation for sensitive actions",
                        14,
                        WHITE,
                        false
                )
        );

        content.addView(
                capabilities
        );

        refreshStatus();
    }

    private void showChat(){

        page.removeAllViews();

        LinearLayout wrapper =
                vertical();

        page.addView(
                wrapper
        );

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "MAYA CHAT",
                        22,
                        WHITE,
                        true
                );

        titleRow.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView online =
                text(
                        "● AI",
                        11,
                        GREEN,
                        true
                );

        titleRow.addView(
                online
        );

        wrapper.addView(
                titleRow
        );

        ScrollView scroll =
                new ScrollView(this);

        chatMessages =
                new LinearLayout(this);

        chatMessages.setOrientation(
                LinearLayout.VERTICAL
        );

        chatMessages.setPadding(
                2,12,2,12
        );

        scroll.addView(
                chatMessages
        );

        wrapper.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        loadHistory();

        LinearLayout composer =
                new LinearLayout(this);

        composer.setGravity(
                Gravity.CENTER_VERTICAL
        );

        composer.setPadding(
                0,8,0,0
        );

        chatInput =
                new EditText(this);

        chatInput.setSingleLine(false);

        chatInput.setMaxLines(4);

        chatInput.setHint(
                "Message MAYA..."
        );

        chatInput.setHintTextColor(
                MUTED
        );

        chatInput.setTextColor(
                WHITE
        );

        chatInput.setTextSize(
                15
        );

        chatInput.setPadding(
                18,12,18,12
        );

        chatInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        chatInput.setBackground(
                rounded(
                        CARD2,
                        28
                )
        );

        composer.addView(
                chatInput,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

        Button send =
                primarySmallButton(
                        "➤"
                );

        composer.addView(
                send,
                new LinearLayout.LayoutParams(
                        58,
                        60
                )
        );

        send.setOnClickListener(
                v -> sendMessage()
        );

        chatInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if(
                            actionId ==
                                    EditorInfo.IME_ACTION_SEND
                    ){

                        sendMessage();

                        return true;
                    }

                    return false;
                }
        );

        wrapper.addView(
                composer
        );

        refreshStatus();
    }

    private void loadHistory(){

        if(chatMessages == null)
            return;

        try{

            JSONArray history =
                    new JSONArray(
                            MayaMemory.get(this)
                    );

            if(history.length() == 0){

                addChat(
                        "नमस्ते! मैं MAYA हूँ। आप मुझसे naturally बात कर सकते हैं.",
                        false
                );

                return;
            }

            for(int i=0;
                i<history.length();
                i++){

                JSONObject item =
                        history.optJSONObject(i);

                if(item == null)
                    continue;

                String role =
                        item.optString(
                                "role"
                        );

                String value =
                        item.optString(
                                "text"
                        );

                if(
                        "user".equals(role) ||
                        "assistant".equals(role)
                ){

                    addChat(
                            value,
                            "user".equals(role)
                    );
                }
            }

        }catch(Exception e){

            addChat(
                    "MAYA ready. बताइए क्या करना है?",
                    false
            );
        }
    }

    private void sendMessage(){

        if(chatInput == null)
            return;

        String value =
                chatInput
                        .getText()
                        .toString()
                        .trim();

        if(value.isEmpty())
            return;

        addChat(
                value,
                true
        );

        chatInput.setText("");

        MayaCore.process(
                this,
                value
        );
    }

    private void addChat(
            String value,
            boolean user
    ){

        if(chatMessages == null)
            return;

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                user
                        ? Gravity.END
                        : Gravity.START
        );

        TextView bubble =
                text(
                        value,
                        15,
                        WHITE,
                        false
                );

        bubble.setPadding(
                17,13,17,13
        );

        bubble.setBackground(
                rounded(
                        user
                                ? Color.rgb(43,47,58)
                                : Color.rgb(23,29,38),
                        24
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        user
                                ? (int)(getResources()
                                .getDisplayMetrics()
                                .widthPixels * 0.78)
                                : (int)(getResources()
                                .getDisplayMetrics()
                                .widthPixels * 0.88),
                        -2
                );

        params.setMargins(
                0,5,0,5
        );

        row.addView(
                bubble,
                params
        );

        chatMessages.addView(
                row
        );
    }

    private void showLive(){

        page.removeAllViews();

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                vertical();

        scroll.addView(
                content
        );

        page.addView(
                scroll
        );

        TextView title =
                text(
                        "LIVE VOICE",
                        24,
                        WHITE,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        content.addView(
                title
        );

        TextView subtitle =
                text(
                        "Natural voice conversation with MAYA",
                        14,
                        MUTED,
                        false
                );

        subtitle.setGravity(
                Gravity.CENTER
        );

        content.addView(
                subtitle
        );

        Space space =
                new Space(this);

        content.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        22
                )
        );

        LinearLayout orb =
                new LinearLayout(this);

        orb.setGravity(
                Gravity.CENTER
        );

        TextView m =
                text(
                        "M",
                        62,
                        GOLD,
                        true
                );

        m.setGravity(
                Gravity.CENTER
        );

        m.setBackground(
                rounded(
                        CARD2,
                        200
                )
        );

        orb.addView(
                m,
                new LinearLayout.LayoutParams(
                        170,
                        170
                )
        );

        content.addView(
                orb
        );

        TextView state =
                text(
                        MayaVoiceService.me != null
                                ? "● LISTENING"
                                : "● STANDBY",
                        15,
                        MayaVoiceService.me != null
                                ? GREEN
                                : MUTED,
                        true
                );

        state.setGravity(
                Gravity.CENTER
        );

        content.addView(
                state
        );

        Space s2 =
                new Space(this);

        content.addView(
                s2,
                new LinearLayout.LayoutParams(
                        1,
                        20
                )
        );

        Button start =
                primaryButton(
                        MayaVoiceService.me == null
                                ? "🎙  START VOICE"
                                : "■  STOP VOICE"
                );

        start.setOnClickListener(
                v -> {

                    if(MayaVoiceService.me == null){

                        startLive();

                    }else{

                        MayaVoiceService.stop(
                                this
                        );

                        showLive();
                    }
                }
        );

        content.addView(
                start
        );

        Button mode =
                secondaryButton(
                        MayaVoiceService
                                .isLiveMode(this)
                                ? "LIVE MODE • ON"
                                : "WAKE WORD MODE • ON"
                );

        mode.setOnClickListener(
                v -> {

                    MayaVoiceService.setLiveMode(
                            this,
                            !MayaVoiceService
                                    .isLiveMode(this)
                    );

                    showLive();
                }
        );

        content.addView(
                mode
        );

        LinearLayout languages =
                card();

        languages.addView(
                text(
                        "LANGUAGES",
                        12,
                        GOLD,
                        true
                )
        );

        languages.addView(
                text(
                        "Hindi • English • Hinglish\n" +
                        "Maithili • Bhojpuri",
                        15,
                        WHITE,
                        false
                )
        );

        content.addView(
                languages
        );

        refreshStatus();
    }

    private void showSettings(){

        page.removeAllViews();

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                vertical();

        scroll.addView(
                content
        );

        page.addView(
                scroll
        );

        content.addView(
                text(
                        "SETTINGS",
                        25,
                        WHITE,
                        true
                )
        );

        content.addView(
                text(
                        "Configure MAYA once. Settings stay on this phone.",
                        14,
                        MUTED,
                        false
                )
        );

        LinearLayout apiCard =
                card();

        apiCard.addView(
                text(
                        "AI CONNECTION",
                        12,
                        GOLD,
                        true
                )
        );

        apiCard.addView(
                text(
                        "OpenAI API key",
                        14,
                        WHITE,
                        true
                )
        );

        EditText key =
                new EditText(this);

        key.setSingleLine(true);

        key.setText(
                MayaCore.getKey(this)
        );

        key.setHint(
                "Paste your OpenAI API key"
        );

        key.setHintTextColor(
                MUTED
        );

        key.setTextColor(
                WHITE
        );

        key.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        key.setPadding(
                16,12,16,12
        );

        key.setBackground(
                rounded(
                        CARD2,
                        18
                )
        );

        apiCard.addView(
                key,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        Button save =
                primaryButton(
                        "SAVE API KEY"
                );

        save.setOnClickListener(
                v -> {

                    String value =
                            key.getText()
                                    .toString()
                                    .trim();

                    if(value.isEmpty()){

                        Toast.makeText(
                                this,
                                "API key खाली है",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    MayaCore.saveKey(
                            this,
                            value
                    );

                    Toast.makeText(
                            this,
                            "✅ API key saved on this phone",
                            Toast.LENGTH_SHORT
                    ).show();

                    refreshStatus();
                }
        );

        apiCard.addView(
                save
        );

        Button test =
                secondaryButton(
                        "TEST API CONNECTION"
                );

        test.setOnClickListener(
                v -> MayaCore.testKey(this)
        );

        apiCard.addView(
                test
        );

        apiCard.addView(
                text(
                        "Your API key is stored locally in the app. Never put it in GitHub code.",
                        12,
                        MUTED,
                        false
                )
        );

        content.addView(
                apiCard
        );

        LinearLayout phoneCard =
                card();

        phoneCard.addView(
                text(
                        "PHONE CONTROL",
                        12,
                        GOLD,
                        true
                )
        );

        Button access =
                secondaryButton(
                        getAccessStatus()
                );

        access.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                )
        );

        phoneCard.addView(
                access
        );

        Button mic =
                secondaryButton(
                        "MICROPHONE PERMISSION"
                );

        mic.setOnClickListener(
                v -> requestMicrophone()
        );

        phoneCard.addView(
                mic
        );

        content.addView(
                phoneCard
        );

        LinearLayout memoryCard =
                card();

        memoryCard.addView(
                text(
                        "MEMORY",
                        12,
                        GOLD,
                        true
                )
        );

        Button clear =
                secondaryButton(
                        "CLEAR CONVERSATION MEMORY"
                );

        clear.setOnClickListener(
                v -> {

                    MayaMemory.clear(
                            this
                    );

                    Toast.makeText(
                            this,
                            "Memory cleared",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        memoryCard.addView(
                clear
        );

        content.addView(
                memoryCard
        );

        LinearLayout about =
                card();

        about.addView(
                text(
                        "MAYA",
                        18,
                        GOLD,
                        true
                )
        );

        about.addView(
                text(
                        "Personal AI • Voice • Memory • Phone Agent\n\n" +
                        "MAYA can work with Android Accessibility and available Android APIs. " +
                        "Android security restrictions still apply.",
                        13,
                        MUTED,
                        false
                )
        );

        content.addView(
                about
        );

        refreshStatus();
    }

    private void startLive(){

        if(
                Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
        ){

            requestMicrophone();

            return;
        }

        MayaVoiceService.setLiveMode(
                this,
                true
        );

        MayaVoiceService.start(
                this
        );

        showLive();
    }

    private void requestMicrophone(){

        if(
                Build.VERSION.SDK_INT >= 23
        ){

            if(
                    checkSelfPermission(
                            Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
            ){

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        100
                );

            }else{

                Toast.makeText(
                        this,
                        "Microphone permission already enabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void requestPermissionsIfNeeded(){

        requestMicrophone();

        if(
                Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ){

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    101
            );
        }
    }

    private String getAccessStatus(){

        String enabled =
                Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                );

        boolean on =
                enabled != null &&
                enabled.toLowerCase(
                        Locale.US
                ).contains(
                        getPackageName()
                                .toLowerCase(Locale.US)
                );

        return on
                ? "● Accessibility ON"
                : "● Accessibility OFF";
    }

    private String getVoiceStatus(){

        return MayaVoiceService.me != null
                ? "● Voice service ON"
                : "● Voice service OFF";
    }

    private String getApiStatus(){

        return MayaCore.hasKey(this)
                ? "● AI API KEY SAVED"
                : "● AI API KEY NOT SET";
    }

    private void refreshStatus(){

        if(status == null)
            return;

        boolean api =
                MayaCore.hasKey(this);

        boolean access =
                getAccessStatus()
                        .contains("ON");

        status.setText(
                "● " +
                        (api && access
                                ? "READY"
                                : "SETUP")
        );

        status.setTextColor(
                api && access
                        ? GREEN
                        : GOLD
        );
    }

    private LinearLayout vertical(){

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        return layout;
    }

    private LinearLayout card(){

        LinearLayout layout =
                vertical();

        layout.setPadding(
                18,17,18,17
        );

        layout.setBackground(
                rounded(
                        CARD,
                        24
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0,7,0,7
        );

        layout.setLayoutParams(
                params
        );

        return layout;
    }

    private TextView text(
            String value,
            float size,
            int color,
            boolean bold
    ){

        TextView view =
                new TextView(this);

        view.setText(
                value
        );

        view.setTextSize(
                size
        );

        view.setTextColor(
                color
        );

        view.setPadding(
                0,4,0,4
        );

        if(bold){

            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private Button navButton(
            String label
    ){

        Button button =
                new Button(this);

        button.setText(
                label
        );

        button.setTextSize(
                10
        );

        button.setTextColor(
                WHITE
        );

        button.setAllCaps(
                false
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                rounded(
                        CARD,
                        18
                )
        );

        return button;
    }

    private Button primaryButton(
            String label
    ){

        Button button =
                new Button(this);

        button.setText(
                label
        );

        button.setTextSize(
                13
        );

        button.setTextColor(
                Color.rgb(20,20,20)
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(
                false
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                rounded(
                        GOLD,
                        20
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        58
                );

        params.setMargins(
                0,6,0,6
        );

        button.setLayoutParams(
                params
        );

        return button;
    }

    private Button secondaryButton(
            String label
    ){

        Button button =
                new Button(this);

        button.setText(
                label
        );

        button.setTextSize(
                12
        );

        button.setTextColor(
                WHITE
        );

        button.setAllCaps(
                false
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                rounded(
                        CARD2,
                        20
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        56
                );

        params.setMargins(
                0,5,0,5
        );

        button.setLayoutParams(
                params
        );

        return button;
    }

    private Button primarySmallButton(
            String label
    ){

        Button button =
                new Button(this);

        button.setText(
                label
        );

        button.setTextSize(
                19
        );

        button.setTextColor(
                Color.rgb(20,20,20)
        );

        button.setAllCaps(
                false
        );

        button.setBackground(
                rounded(
                        GOLD,
                        22
                )
        );

        return button;
    }

    private GradientDrawable rounded(
            int color,
            float radius
    ){

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                color
        );

        drawable.setCornerRadius(
                radius
        );

        return drawable;
    }

    @Override
    protected void onResume(){

        super.onResume();

        refreshStatus();
    }

    @Override
    protected void onDestroy(){

        try{

            unregisterReceiver(
                    receiver
            );

        }catch(Exception ignored){}

        super.onDestroy();
    }
}
