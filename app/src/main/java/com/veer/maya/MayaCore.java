package com.veer.maya;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MayaCore {

    private static final String PREF = "maya_ai";
    private static final String KEY = "openai_key";
    private static final String MODEL = "model";

    public static String getKey(Context c) {
        return c.getSharedPreferences(PREF,0)
                .getString(KEY,"");
    }

    public static void saveKey(Context c,String key) {
        c.getSharedPreferences(PREF,0)
                .edit()
                .putString(KEY,key)
                .apply();
    }

    public static String getModel(Context c) {
        return c.getSharedPreferences(PREF,0)
                .getString(MODEL,"gpt-5.6-luna");
    }

    public static void saveModel(Context c,String model) {
        c.getSharedPreferences(PREF,0)
                .edit()
                .putString(MODEL,model)
                .apply();
    }

    public static void process(Context c,String userText) {
        if(userText == null || userText.trim().isEmpty())
            return;

        String key = getKey(c);

        if(key.isEmpty()) {
            reply(
                    c,
                    "Boss, पहले Settings → AI Model & API में अपनी API key set कर दीजिए।"
            );
            return;
        }

        new Thread(() -> {
            try {
                String result = askAI(c,key,userText);
                handleAIResult(c,result);
            } catch(Exception e) {
                reply(
                        c,
                        "Boss, AI connection में problem आई: "
                        + safeError(e)
                );
            }
        }).start();
    }

    private static String askAI(
            Context c,
            String key,
            String user
    ) throws Exception {

        URL url = new URL(
                "https://api.openai.com/v1/responses"
        );

        HttpURLConnection con =
                (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setConnectTimeout(20000);
        con.setReadTimeout(60000);
        con.setDoOutput(true);

        con.setRequestProperty(
                "Authorization",
                "Bearer " + key
        );

        con.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        String system =
                "You are MAYA, Boss's personal Android AI phone agent. "
                + "Your goal is to DO the requested task using available Android controls, "
                + "not merely explain how to do it. "
                + "Understand Hindi, English, Hinglish, Maithili and Bhojpuri. "
                + "Interpret natural language commands and convert them into ordered actions. "
                + "For multi-step tasks use multiple actions in correct order. "
                + "Never invent phone numbers, contacts or private data. "
                + "Calls, messages, purchases, deletion or other high-impact actions require confirmation. "
                + "Return ONLY valid JSON. "
                + "Schema: "
                + "{\"say\":\"short response\","
                + "\"memory\":\"important fact or empty\","
                + "\"actions\":["
                + "{\"type\":\"open_app\",\"value\":\"YouTube\"}"
                + "]} "
                + "Allowed action types: "
                + "open_app, tap, type, scroll, back, home, recent, "
                + "web_search, open_url, dial, message, keyevent, wait. "
                + "For tap use the visible text in value. "
                + "For type use value as text. "
                + "For scroll value up or down. "
                + "For wait value milliseconds. "
                + "For keyevent use Android keyevent number. "
                + "For web_search value is the query. "
                + "For open_url value is the complete URL.";

        JSONObject body = new JSONObject();

        body.put(
                "model",
                getModel(c)
        );

        JSONArray input = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role","system");
        systemMsg.put("content",system);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role","user");
        userMsg.put("content",user);

        input.put(systemMsg);
        input.put(userMsg);

        body.put("input",input);

        OutputStream os = con.getOutputStream();
        os.write(body.toString().getBytes("UTF-8"));
        os.close();

        int code = con.getResponseCode();

        InputStream stream =
                code >= 200 && code < 300
                ? con.getInputStream()
                : con.getErrorStream();

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                "UTF-8"
                        )
                );

        StringBuilder out = new StringBuilder();
        String line;

        while((line=br.readLine())!=null)
            out.append(line);

        br.close();
        con.disconnect();

        if(code < 200 || code >= 300)
            throw new Exception(
                    "HTTP " + code + ": " + out
            );

        return out.toString();
    }

    private static void handleAIResult(
            Context c,
            String raw
    ) {

        try {
            JSONObject response =
                    new JSONObject(raw);

            String outputText =
                    extractText(response);

            if(outputText == null)
                throw new Exception("AI output empty");

            outputText =
                    cleanJson(outputText);

            JSONObject ai =
                    new JSONObject(outputText);

            String say =
                    ai.optString(
                            "say",
                            "ठीक है boss."
                    );

            String memory =
                    ai.optString(
                            "memory",
                            ""
                    );

            if(!memory.isEmpty()) {
                saveMemory(c,memory);
            }

            JSONArray actions =
                    ai.optJSONArray("actions");

            List<JSONObject> safe =
                    new ArrayList<>();

            List<JSONObject> dangerous =
                    new ArrayList<>();

            if(actions != null) {
                for(int i=0;i<actions.length();i++) {

                    JSONObject a =
                            actions.optJSONObject(i);

                    if(a == null) continue;

                    String type =
                            a.optString("type","");

                    if(isDangerous(type))
                        dangerous.add(a);
                    else
                        safe.add(a);
                }
            }

            reply(c,say);

            for(JSONObject a:safe) {
                executeAction(c,a);

                try {
                    Thread.sleep(850);
                } catch(Exception ignored) {}
            }

            if(!dangerous.isEmpty()) {

                StringBuilder pending =
                        new StringBuilder();

                pending.append(
                        "\n\nBoss, ये action करने से पहले आपकी confirmation चाहिए:\n"
                );

                for(JSONObject a:dangerous) {
                    pending.append(
                            "• "
                    ).append(
                            describe(a)
                    ).append("\n");
                }

                pending.append(
                        "\nConfirm बोलें तो आगे करूँगी।"
                );

                savePending(c,dangerous);

                reply(c,pending.toString());
            }

        } catch(Exception e) {

            reply(
                    c,
                    "Boss, मैंने response समझने की कोशिश की लेकिन "
                    + "AI output सही format में नहीं आया।"
            );
        }
    }

    private static String extractText(
            JSONObject response
    ) {

        String direct =
                response.optString(
                        "output_text",
                        ""
                );

        if(!direct.isEmpty())
            return direct;

        JSONArray output =
                response.optJSONArray("output");

        if(output == null)
            return null;

        StringBuilder all =
                new StringBuilder();

        for(int i=0;i<output.length();i++) {

            JSONObject item =
                    output.optJSONObject(i);

            if(item == null) continue;

            JSONArray content =
                    item.optJSONArray("content");

            if(content == null) continue;

            for(int j=0;j<content.length();j++) {

                JSONObject part =
                        content.optJSONObject(j);

                if(part == null) continue;

                String type =
                        part.optString("type","");

                if(
                        "output_text".equals(type) ||
                        "text".equals(type)
                ) {
                    all.append(
                            part.optString("text","")
                    );
                }
            }
        }

        return all.length()==0
                ? null
                : all.toString();
    }

    private static String cleanJson(String s) {

        s=s.trim();

        if(s.startsWith("```")) {
            int first =
                    s.indexOf("\n");

            int last =
                    s.lastIndexOf("```");

            if(first>=0 && last>first)
                s=s.substring(
                        first+1,
                        last
                );
        }

        int a=s.indexOf("{");
        int b=s.lastIndexOf("}");

        if(a>=0 && b>a)
            s=s.substring(a,b+1);

        return s.trim();
    }

    private static boolean isDangerous(String type) {

        return
                "dial".equals(type) ||
                "message".equals(type) ||
                "purchase".equals(type) ||
                "delete".equals(type);
    }

    private static String describe(JSONObject a) {

        String type =
                a.optString("type","action");

        String value =
                a.optString("value","");

        if("dial".equals(type))
            return "Call " + value;

        if("message".equals(type))
            return "Message " + value;

        return type + " " + value;
    }

    private static void executeAction(
            Context c,
            JSONObject a
    ) {

        try {

            String type =
                    a.optString("type","");

            String value =
                    a.optString("value","");

            switch(type) {

                case "open_app":
                    MayaAccessibilityService.openApp(
                            c,
                            value
                    );
                    break;

                case "tap":
                    MayaAccessibilityService.tapText(
                            value
                    );
                    break;

                case "type":
                    MayaAccessibilityService.typeText(
                            value
                    );
                    break;

                case "scroll":
                    MayaAccessibilityService.scroll(
                            value
                    );
                    break;

                case "back":
                    MayaAccessibilityService.globalBack();
                    break;

                case "home":
                    MayaAccessibilityService.globalHome();
                    break;

                case "recent":
                    MayaAccessibilityService.globalRecent();
                    break;

                case "web_search":
                    openSearch(c,value);
                    break;

                case "open_url":
                    openUrl(c,value);
                    break;

                case "keyevent":
                    try {
                        Runtime.getRuntime().exec(
                                new String[]{
                                        "input",
                                        "keyevent",
                                        value
                                }
                        );
                    } catch(Exception ignored) {}
                    break;

                case "wait":
                    try {
                        Thread.sleep(
                                Long.parseLong(value)
                        );
                    } catch(Exception ignored) {}
                    break;

                case "dial":
                    Intent dial =
                            new Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse(
                                            "tel:" + value
                                    )
                            );
                    dial.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );
                    c.startActivity(dial);
                    break;

                case "message":
                    Intent sms =
                            new Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse(
                                            "smsto:" + value
                                    )
                            );
                    sms.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );
                    c.startActivity(sms);
                    break;
            }

        } catch(Exception ignored) {}
    }

    private static void openSearch(
            Context c,
            String q
    ) {

        try {
            String url =
                    "https://www.google.com/search?q="
                    + Uri.encode(q);

            openUrl(c,url);
        } catch(Exception ignored) {}
    }

    private static void openUrl(
            Context c,
            String url
    ) {

        try {

            Intent i =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            i.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            c.startActivity(i);

        } catch(Exception ignored) {}
    }

    private static void saveMemory(
            Context c,
            String value
    ) {

        c.getSharedPreferences(
                "maya_memory",
                0
        ).edit()
                .putString(
                        "last_memory",
                        value
                )
                .apply();
    }

    private static void savePending(
            Context c,
            List<JSONObject> actions
    ) {

        JSONArray a = new JSONArray();

        for(JSONObject o:actions)
            a.put(o);

        c.getSharedPreferences(
                "maya_pending",
                0
        ).edit()
                .putString(
                        "actions",
                        a.toString()
                )
                .apply();
    }

    public static void confirmPending(Context c) {

        String raw =
                c.getSharedPreferences(
                        "maya_pending",
                        0
                ).getString(
                        "actions",
                        ""
                );

        if(raw.isEmpty()) {
            reply(
                    c,
                    "Boss, अभी कोई pending action नहीं है।"
            );
            return;
        }

        try {

            JSONArray a =
                    new JSONArray(raw);

            for(int i=0;i<a.length();i++) {

                JSONObject o =
                        a.optJSONObject(i);

                if(o != null)
                    executeAction(c,o);
            }

            c.getSharedPreferences(
                    "maya_pending",
                    0
            ).edit().clear().apply();

            reply(
                    c,
                    "ठीक है boss, action कर दिया।"
            );

        } catch(Exception e) {

            reply(
                    c,
                    "Boss, pending action execute नहीं हो पाया।"
            );
        }
    }

    public static void cancelPending(Context c) {

        c.getSharedPreferences(
                "maya_pending",
                0
        ).edit().clear().apply();

        reply(
                c,
                "ठीक है boss, action cancel कर दिया।"
        );
    }

    private static void reply(
            Context c,
            String text
    ) {

        new Handler(
                Looper.getMainLooper()
        ).post(() -> {

            try {
                MayaVoiceService.say(text);
            } catch(Exception ignored) {}

            Intent i =
                    new Intent(
                            "com.veer.maya.REPLY"
                    );

            i.setPackage(
                    c.getPackageName()
            );

            i.putExtra("text",text);

            c.sendBroadcast(i);
        });
    }

    private static String safeError(Exception e) {

        String s=e.getMessage();

        if(s==null || s.isEmpty())
            return "unknown error";

        if(s.length()>300)
            s=s.substring(0,300);

        return s;
    }
}
