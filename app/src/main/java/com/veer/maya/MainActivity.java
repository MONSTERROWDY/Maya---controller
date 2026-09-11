package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends Activity {

    LinearLayout root;
    TextView status;
    TextView response;
    EditText input;
    EditText api;
    EditText endpoint;
    EditText model;
    HudView hud;

    Handler handler = new Handler();

    int cyan = Color.rgb(93,220,255);
    int white = Color.WHITE;
    int muted = Color.rgb(150,165,180);
    int panel = Color.rgb(13,19,27);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                1001
            );
        }

        buildUI();
    }

    TextView tv(String text, float size, int color) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(18,12,18,12);
        return t;
    }

    Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(white);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.rgb(24,34,45));
        return b;
    }

    void buildUI() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(4,7,11));

        ScrollView scroll = new ScrollView(this);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(14,14,14,24);

        TextView title =
            tv("MAYA", 30, white);

        title.setTypeface(
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        );

        page.addView(title);

        TextView sub =
            tv("VOICE • BRAIN • AGENT • MEMORY", 11, cyan);

        page.addView(sub);

        hud = new HudView();
        page.addView(
            hud,
            new LinearLayout.LayoutParams(
                -1,
                dp(280)
            )
        );

        status =
            tv("HELLO BOSS • READY", 14, cyan);

        status.setGravity(Gravity.CENTER);
        page.addView(status);

        response =
            tv("MAYA online.\nSay: Hello Boss", 15, white);

        response.setBackgroundColor(panel);
        page.addView(response);

        input =
            new EditText(this);

        input.setHint(
            "Type a command…"
        );

        input.setHintTextColor(muted);
        input.setTextColor(white);
        input.setSingleLine(false);
        input.setBackgroundColor(panel);

        page.addView(input);

        Button send =
            button("SEND COMMAND");

        page.addView(send);

        Button voice =
            button("START HELLO BOSS VOICE");

        page.addView(voice);

        Button accessibility =
            button("ANDROID AGENT / ACCESSIBILITY");

        page.addView(accessibility);

        TextView settingsTitle =
            tv("AI CONNECTION",18,white);

        page.addView(settingsTitle);

        api = new EditText(this);
        api.setHint("API Key");
        api.setHintTextColor(muted);
        api.setTextColor(white);
        api.setSingleLine(true);
        api.setInputType(129);
        api.setText(
            MayaCore.getApi()
        );

        page.addView(api);

        endpoint = new EditText(this);
        endpoint.setHint("API Endpoint");
        endpoint.setHintTextColor(muted);
        endpoint.setTextColor(white);
        endpoint.setSingleLine(true);
        endpoint.setText(
            MayaCore.getEndpoint()
        );

        page.addView(endpoint);

        model = new EditText(this);
        model.setHint("Model");
        model.setHintTextColor(muted);
        model.setTextColor(white);
        model.setSingleLine(true);
        model.setText(
            MayaCore.getModel()
        );

        page.addView(model);

        Button save =
            button("SAVE API SETTINGS");

        page.addView(save);

        Button test =
            button("TEST API CONNECTION");

        page.addView(test);

        Button update =
            button("CHECK FOR MAYA UPDATE");

        page.addView(update);

        scroll.addView(page);
        root.addView(scroll);

        setContentView(root);

        send.setOnClickListener(v -> {

            String text =
                input.getText().toString().trim();

            if (text.isEmpty()) return;

            setState("THINKING");

            MayaCore.ask(
                this,
                text,
                (ok, answer) -> runOnUiThread(() -> {

                    response.setText(answer);

                    setState(
                        ok
                        ? "COMPLETED"
                        : "ERROR"
                    );
                })
            );
        });

        voice.setOnClickListener(v -> {

            setState("AWAKENED");

            Intent i =
                new Intent(
                    this,
                    MayaVoiceService.class
                );

            if (android.os.Build.VERSION.SDK_INT >= 26) {
                startForegroundService(i);
            } else {
                startService(i);
            }
        });

        accessibility.setOnClickListener(v -> {

            try {
                startActivity(
                    new Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                );
            } catch (Exception ignored) {}
        });

        save.setOnClickListener(v -> {

            MayaCore.saveApi(
                this,
                api.getText().toString()
            );

            MayaCore.saveEndpoint(
                this,
                endpoint.getText().toString()
            );

            MayaCore.saveModel(
                this,
                model.getText().toString()
            );

            Toast.makeText(
                this,
                "API SETTINGS SAVED",
                Toast.LENGTH_SHORT
            ).show();

            setState("READY");
        });

        test.setOnClickListener(v -> {

            setState("VERIFYING");

            MayaCore.ask(
                this,
                "Reply with exactly: MAYA API OK",
                (ok, answer) -> runOnUiThread(() -> {

                    response.setText(answer);

                    Toast.makeText(
                        this,
                        ok
                        ? "API CONNECTION OK"
                        : "API CONNECTION FAILED",
                        Toast.LENGTH_LONG
                    ).show();

                    setState(
                        ok
                        ? "COMPLETED"
                        : "ERROR"
                    );
                })
            );
        });

        update.setOnClickListener(v ->
            checkUpdate()
        );
    }

    void setState(String s) {
        status.setText(s);
        hud.state = s;
        hud.invalidate();
    }

    void checkUpdate() {

        new Thread(() -> {

            try {

                URL u = new URL(
                    "https://api.github.com/repos/" +
                    "MONSTERROWDY/Maya---controller/releases/latest"
                );

                HttpURLConnection c =
                    (HttpURLConnection)u.openConnection();

                c.setRequestProperty(
                    "Accept",
                    "application/vnd.github+json"
                );

                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);

                Scanner sc =
                    new Scanner(c.getInputStream())
                    .useDelimiter("\\A");

                String raw =
                    sc.hasNext() ? sc.next() : "";

                JSONObject j =
                    new JSONObject(raw);

                String tag =
                    j.optString("tag_name","");

                runOnUiThread(() -> {

                    Toast.makeText(
                        this,
                        tag.isEmpty()
                        ? "No release found"
                        : "Latest MAYA: " + tag,
                        Toast.LENGTH_LONG
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                    Toast.makeText(
                        this,
                        "Update check failed",
                        Toast.LENGTH_SHORT
                    ).show()
                );
            }

        }).start();
    }

    int dp(int n) {
        return (int)(
            n * getResources()
            .getDisplayMetrics()
            .density
        );
    }

    class HudView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String state = "READY";
        float pulse = 0;

        HudView() {
            super(MainActivity.this);
            p.setTypeface(
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            );

            handler.post(new Runnable() {
                @Override
                public void run() {
                    pulse += 0.05f;
                    invalidate();
                    handler.postDelayed(this, 40);
                }
            });
        }

        @Override
        protected void onDraw(Canvas c) {

            super.onDraw(c);

            float cx = getWidth()/2f;
            float cy = getHeight()/2f;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);

            p.setColor(
                Color.rgb(35,100,130)
            );

            for (int i=0;i<5;i++) {

                float r =
                    48 + i*25 +
                    (float)Math.sin(
                        pulse+i
                    )*4;

                c.drawCircle(
                    cx,
                    cy,
                    r,
                    p
                );
            }

            p.setStyle(Paint.Style.FILL);

            p.setColor(
                Color.rgb(35,130,170)
            );

            c.drawCircle(
                cx,
                cy,
                40 + (float)Math.sin(pulse)*4,
                p
            );

            p.setColor(Color.BLACK);

            c.drawCircle(
                cx,
                cy,
                28,
                p
            );

            p.setColor(cyan);

            p.setTextSize(13);

            String s = state;

            float w =
                p.measureText(s);

            c.drawText(
                s,
                cx-w/2,
                cy+5,
                p
            );

            p.setTextSize(9);
            p.setColor(muted);

            String info =
                "MAYA • ANDROID AI CORE";

            float iw =
                p.measureText(info);

            c.drawText(
                info,
                cx-iw/2,
                getHeight()-18,
                p
            );
        }
    }
}
