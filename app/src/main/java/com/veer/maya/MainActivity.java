package com.veer.maya;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        buildUI();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (status != null) {
            updateStatus();
        }
    }

    private void buildUI() {

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                40, 40, 40, 40
        );

        layout.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        layout.setBackgroundColor(
                Color.rgb(7, 9, 12)
        );

        TextView title =
                new TextView(this);

        title.setText(
                "MAYA CONTROLLER"
        );

        title.setTextColor(
                Color.rgb(252, 213, 53)
        );

        title.setTextSize(28);

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(0, 0, 0, 20);

        layout.addView(title);

        status =
                new TextView(this);

        status.setTextColor(Color.WHITE);

        status.setTextSize(18);

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                0, 10, 0, 25
        );

        layout.addView(status);

        Button accessibility =
                makeButton(
                        "ENABLE / OPEN ACCESSIBILITY"
                );

        accessibility.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                            );

                    startActivity(intent);
                }
        );

        layout.addView(accessibility);

        Button refresh =
                makeButton("REFRESH STATUS");

        refresh.setOnClickListener(
                v -> updateStatus()
        );

        layout.addView(refresh);

        Button youtube =
                makeButton("▶ OPEN YOUTUBE");

        youtube.setOnClickListener(
                v -> sendCommand("open YouTube")
        );

        layout.addView(youtube);

        Button home =
                makeButton("⌂ HOME");

        home.setOnClickListener(
                v -> sendCommand("home")
        );

        layout.addView(home);

        Button back =
                makeButton("← BACK");

        back.setOnClickListener(
                v -> sendCommand("back")
        );

        layout.addView(back);

        Button recent =
                makeButton("RECENT APPS");

        recent.setOnClickListener(
                v -> sendCommand("recent")
        );

        layout.addView(recent);

        Button instagram =
                makeButton("OPEN INSTAGRAM");

        instagram.setOnClickListener(
                v -> sendCommand("open Instagram")
        );

        layout.addView(instagram);

        Button whatsapp =
                makeButton("OPEN WHATSAPP");

        whatsapp.setOnClickListener(
                v -> sendCommand("open WhatsApp")
        );

        layout.addView(whatsapp);

        Button telegram =
                makeButton("OPEN TELEGRAM");

        telegram.setOnClickListener(
                v -> sendCommand("open Telegram")
        );

        layout.addView(telegram);

        Button chrome =
                makeButton("OPEN CHROME");

        chrome.setOnClickListener(
                v -> sendCommand("open Chrome")
        );

        layout.addView(chrome);

        TextView info =
                new TextView(this);

        info.setText(
                "\nMAYA Phone Control\n\n" +
                "Accessibility ON होने के बाद " +
                "MAYA Android screen पर actions perform कर सकती है."
        );

        info.setTextColor(Color.LTGRAY);

        info.setTextSize(15);

        info.setGravity(
                Gravity.CENTER
        );

        layout.addView(info);

        scroll.addView(layout);

        setContentView(scroll);

        updateStatus();
    }

    private Button makeButton(String text) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(15);

        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0, 8, 0, 8
        );

        button.setLayoutParams(params);

        return button;
    }

    private void sendCommand(String command) {

        Intent intent =
                new Intent(
                        MayaCommandReceiver.ACTION
                );

        intent.setPackage(
                getPackageName()
        );

        intent.putExtra(
                "command",
                command
        );

        sendBroadcast(intent);
    }

    private void updateStatus() {

        String enabled =
                Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure
                                .ENABLED_ACCESSIBILITY_SERVICES
                );

        String myService =
                new ComponentName(
                        this,
                        MayaAccessibilityService.class
                ).flattenToString();

        boolean isEnabled =
                enabled != null &&
                enabled.contains(myService);

        boolean connected =
                MayaAccessibilityService
                        .getInstance() != null;

        if (connected) {

            status.setText(
                    "🟢 MAYA CONTROLLER CONNECTED"
            );

        } else if (isEnabled) {

            status.setText(
                    "🟡 ACCESSIBILITY ON\n" +
                    "Service reconnecting..."
            );

        } else {

            status.setText(
                    "🔴 ACCESSIBILITY OFF"
            );
        }
    }
}
