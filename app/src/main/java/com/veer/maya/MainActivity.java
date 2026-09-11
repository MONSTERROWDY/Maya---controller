package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private EditText geminiKey;
    private EditText geminiModel;
    private EditText openAIKey;
    private EditText openAIModel;

    private RadioButton geminiRadio;
    private RadioButton openAIRadio;

    private TextView status;

    private final int cyan =
            Color.rgb(0, 229, 255);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        buildUI();

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    100
            );
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
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

    private TextView label(String text) {
        TextView v =
                new TextView(this);

        v.setText(text);
        v.setTextColor(cyan);
        v.setTextSize(13);
        v.setPadding(0, 14, 0, 5);

        return v;
    }

    private EditText field(
            String hint,
            String value,
            boolean password
    ) {
        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(
                Color.rgb(130, 145, 155)
        );

        if (password) {
            e.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        }

        e.setPadding(
                18, 8, 18, 8
        );

        return e;
    }

    private Button button(
            String text
    ) {
        Button b =
                new Button(this);

        b.setText(text);
        b.setTextColor(Color.WHITE);

        return b;
    }

    private void buildUI() {

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                28, 30, 28, 35
        );

        root.setBackgroundColor(
                Color.rgb(4, 8, 12)
        );

        ImageView logo =
                new ImageView(this);

        logo.setImageResource(
                com.veer.maya.R.drawable.maya_logo
        );

        root.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        150
                )
        );

        TextView title =
                new TextView(this);

        title.setText(
                "M A Y A"
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setTextColor(
                Color.WHITE
        );

        title.setTextSize(30);

        root.addView(title);

        TextView sub =
                new TextView(this);

        sub.setText(
                "AI BRAIN  •  VOICE  •  MEMORY  •  ANDROID AGENT"
        );

        sub.setGravity(
                Gravity.CENTER
        );

        sub.setTextColor(cyan);
        sub.setTextSize(11);

        root.addView(sub);

        status =
                new TextView(this);

        status.setText(
                "● MAYA READY"
        );

        status.setGravity(
                Gravity.CENTER
        );

        status.setTextColor(
                Color.rgb(0, 255, 180)
        );

        status.setTextSize(14);

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        65
                )
        );

        root.addView(
                label("AI PROVIDER")
        );

        RadioGroup providers =
                new RadioGroup(this);

        providers.setOrientation(
                RadioGroup.HORIZONTAL
        );

        geminiRadio =
                new RadioButton(this);

        geminiRadio.setText("Gemini");
        geminiRadio.setTextColor(Color.WHITE);

        openAIRadio =
                new RadioButton(this);

        openAIRadio.setText("OpenAI");
        openAIRadio.setTextColor(Color.WHITE);

        providers.addView(geminiRadio);
        providers.addView(openAIRadio);

        geminiRadio.setChecked(true);

        root.addView(providers);

        root.addView(
                label("GEMINI API TOKEN")
        );

        geminiKey =
                field(
                        "Paste Gemini API Token",
                        MayaCore.getGeminiKey(this),
                        true
                );

        root.addView(geminiKey);

        root.addView(
                label("GEMINI MODEL")
        );

        geminiModel =
                field(
                        "Gemini model",
                        MayaCore.getGeminiModel(this),
                        false
                );

        root.addView(geminiModel);

        root.addView(
                label("OPENAI API TOKEN")
        );

        openAIKey =
                field(
                        "Paste OpenAI API Token",
                        MayaCore.getOpenAIKey(this),
                        true
                );

        root.addView(openAIKey);

        root.addView(
                label("OPENAI MODEL")
        );

        openAIModel =
                field(
                        "Your available OpenAI API model",
                        MayaCore.getOpenAIModel(this),
                        false
                );

        root.addView(openAIModel);

        Button save =
                button(
                        "SAVE API SETTINGS"
                );

        save.setOnClickListener(v -> {

            String provider =
                    geminiRadio.isChecked()
                            ? "Gemini"
                            : "OpenAI";

            MayaCore.saveSettings(
                    this,
                    geminiKey.getText().toString(),
                    geminiModel.getText().toString(),
                    openAIKey.getText().toString(),
                    openAIModel.getText().toString(),
                    provider
            );

            status.setText(
                    "● SETTINGS SAVED"
            );
        });

        root.addView(save);

        Button test =
                button(
                        "TEST SELECTED AI"
                );

        test.setOnClickListener(v -> {

            status.setText(
                    "● TESTING AI..."
            );

            MayaCore.ask(
                    this,
                    "Reply with exactly: MAYA ONLINE",
                    (ok, answer) -> runOnUiThread(() -> {

                        status.setText(
                                ok
                                        ? "● AI ONLINE"
                                        : "● AI ERROR"
                        );

                        Toast.makeText(
                                this,
                                answer,
                                Toast.LENGTH_LONG
                        ).show();
                    })
            );
        });

        root.addView(test);

        root.addView(
                label("VOICE & ANDROID AGENT")
        );

        Button voice =
                button(
                        "START HELLO BOSS / MAYA LIVE VOICE"
                );

        voice.setOnClickListener(v -> {

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

            Intent i =
                    new Intent(
                            this,
                            MayaVoiceService.class
                    );

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(i);
            } else {
                startService(i);
            }

            status.setText(
                    "● MAYA VOICE ACTIVE"
            );
        });

        root.addView(voice);

        Button overlay =
                button(
                        "ENABLE MAYA FLOATING POPUP"
                );

        overlay.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= 23 &&
                    !Settings.canDrawOverlays(this)) {

                Intent i =
                        new Intent(
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
                        "MAYA overlay already enabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        root.addView(overlay);

        Button accessibility =
                button(
                        "ENABLE MAYA ANDROID ACCESSIBILITY AGENT"
                );

        accessibility.setOnClickListener(v -> {

            Intent i =
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    );

            startActivity(i);
        });

        root.addView(accessibility);

        Button memory =
                button(
                        "CLEAR MAYA MEMORY"
                );

        memory.setOnClickListener(v -> {

            MayaMemory.clear(this);

            Toast.makeText(
                    this,
                    "MAYA memory cleared",
                    Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(memory);

        TextView help =
                new TextView(this);

        help.setText(
                "\nVOICE EXAMPLES\n\n" +
                "Hello Boss\n" +
                "Hello Maya\n" +
                "MAYA YouTube खोलो\n" +
                "MAYA Chrome खोलो\n" +
                "MAYA वापस जाओ\n" +
                "MAYA home जाओ\n" +
                "MAYA 98XXXXXXXX को call लगाओ\n" +
                "MAYA मुझे समझाओ...\n"
        );

        help.setTextColor(
                Color.LTGRAY
        );

        help.setTextSize(14);

        root.addView(help);

        scroll.addView(root);

        setContentView(scroll);
    }
}
