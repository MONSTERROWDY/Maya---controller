package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MayaCore {

    private static final String PREF = "maya_settings";

    public interface Callback {
        void done(boolean ok, String answer);
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static String getGeminiKey(Context c) {
        return prefs(c).getString("gemini_key", "");
    }

    public static String getGeminiModel(Context c) {
        return prefs(c).getString("gemini_model", "gemini-3.8-flash");
    }

    public static String getOpenAIKey(Context c) {
        return prefs(c).getString("openai_key", "");
    }

    public static String getOpenAIModel(Context c) {
        return prefs(c).getString("openai_model", "");
    }

    public static String getProvider(Context c) {
        return prefs(c).getString("provider", "Gemini");
    }

    public static void saveSettings(
            Context c,
            String geminiKey,
            String geminiModel,
            String openAIKey,
            String openAIModel,
            String provider
    ) {
        prefs(c).edit()
                .putString("gemini_key", geminiKey == null ? "" : geminiKey.trim())
                .putString("gemini_model", geminiModel == null ? "" : geminiModel.trim())
                .putString("openai_key", openAIKey == null ? "" : openAIKey.trim())
                .putString("openai_model", openAIModel == null ? "" : openAIModel.trim())
                .putString("provider", provider == null ? "Gemini" : provider)
                .apply();
    }

    public static void ask(Context c, String text, Callback callback) {
        new Thread(() -> {
            try {
                String provider = getProvider(c);

                if ("OpenAI".equalsIgnoreCase(provider)) {
                    String key = getOpenAIKey(c);

                    if (key.isEmpty()) {
                        callback.done(false,
                                "OpenAI API Token सेट नहीं है। Settings में token डालिए।");
                        return;
                    }

                    String model = getOpenAIModel(c);

                    if (model.isEmpty()) {
                        callback.done(false,
                                "OpenAI Model खाली है। Settings में अपना उपलब्ध API model डालिए।");
                        return;
                    }

                    callOpenAI(c, key, model, text, callback);

                } else {
                    String key = getGeminiKey(c);

                    if (key.isEmpty()) {
                        callback.done(false,
                                "Gemini API Token सेट नहीं है। Settings में token डालिए।");
                        return;
                    }

                    String model = getGeminiModel(c);

                    if (model.isEmpty()) {
                        model = "gemini-3.8-flash";
                    }

                    callGemini(c, key, model, text, callback);
                }

            } catch (Exception e) {
                callback.done(false, "MAYA Error: " + safeError(e));
            }
        }).start();
    }

    private static void callGemini(
            Context c,
            String key,
            String model,
            String text,
            Callback callback
    ) throws Exception {

        String cleanModel = model.startsWith("models/")
                ? model.substring(7)
                : model;

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + URLEncoder.encode(cleanModel, "UTF-8")
                        + ":generateContent";

        JSONObject root = new JSONObject();

        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();

        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();

        String prompt = systemPrompt() + "\n\nUSER:\n" + text;

        part.put("text", prompt);
        parts.put(part);

        content.put("parts", parts);
        contents.put(content);

        root.put("contents", contents);

        String response = post(
                endpoint,
                root.toString(),
                "x-goog-api-key",
                key
        );

        JSONObject obj = new JSONObject(response);

        if (obj.has("error")) {
            JSONObject error = obj.optJSONObject("error");
            callback.done(false,
                    error == null
                            ? "Gemini API Error"
                            : error.optString("message", "Gemini API Error"));
            return;
        }

        String answer = "";

        JSONArray candidates = obj.optJSONArray("candidates");

        if (candidates != null && candidates.length() > 0) {
            JSONObject candidate = candidates.optJSONObject(0);

            if (candidate != null) {
                JSONObject contentObj =
                        candidate.optJSONObject("content");

                if (contentObj != null) {
                    JSONArray p =
                            contentObj.optJSONArray("parts");

                    if (p != null) {
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < p.length(); i++) {
                            JSONObject pp = p.optJSONObject(i);

                            if (pp != null) {
                                String t = pp.optString("text", "");

                                if (!t.isEmpty()) {
                                    if (sb.length() > 0) sb.append("\n");
                                    sb.append(t);
                                }
                            }
                        }

                        answer = sb.toString();
                    }
                }
            }
        }

        if (answer.isEmpty()) {
            answer = "Gemini ने कोई text response नहीं दिया।";
        }

        remember(c, text, answer);
        callback.done(true, answer);
    }

    private static void callOpenAI(
            Context c,
            String key,
            String model,
            String text,
            Callback callback
    ) throws Exception {

        JSONObject root = new JSONObject();

        root.put("model", model);
        root.put(
                "input",
                systemPrompt() + "\n\nUSER:\n" + text
        );

        String response = post(
                "https://api.openai.com/v1/responses",
                root.toString(),
                "Authorization",
                "Bearer " + key
        );

        JSONObject obj = new JSONObject(response);

        if (obj.has("error")) {
            JSONObject error = obj.optJSONObject("error");

            callback.done(false,
                    error == null
                            ? "OpenAI API Error"
                            : error.optString(
                                    "message",
                                    "OpenAI API Error"
                            ));
            return;
        }

        String answer = obj.optString("output_text", "");

        if (answer.isEmpty()) {
            JSONArray output = obj.optJSONArray("output");

            if (output != null) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < output.length(); i++) {
                    JSONObject item = output.optJSONObject(i);

                    if (item == null) continue;

                    JSONArray content =
                            item.optJSONArray("content");

                    if (content == null) continue;

                    for (int j = 0; j < content.length(); j++) {
                        JSONObject block =
                                content.optJSONObject(j);

                        if (block != null) {
                            String t =
                                    block.optString("text", "");

                            if (!t.isEmpty()) {
                                if (sb.length() > 0) sb.append("\n");
                                sb.append(t);
                            }
                        }
                    }
                }

                answer = sb.toString();
            }
        }

        if (answer.isEmpty()) {
            answer = "OpenAI ने कोई text response नहीं दिया।";
        }

        remember(c, text, answer);
        callback.done(true, answer);
    }

    private static String post(
            String endpoint,
            String body,
            String headerName,
            String headerValue
    ) throws Exception {

        URL url = new URL(endpoint);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);

        conn.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        conn.setRequestProperty(
                "Accept",
                "application/json"
        );

        conn.setRequestProperty(
                headerName,
                headerValue
        );

        byte[] data =
                body.getBytes("UTF-8");

        OutputStream os =
                conn.getOutputStream();

        os.write(data);
        os.flush();
        os.close();

        int code =
                conn.getResponseCode();

        InputStream stream =
                code >= 400
                        ? conn.getErrorStream()
                        : conn.getInputStream();

        String result = read(stream);

        if (code < 200 || code >= 300) {
            throw new Exception(
                    "HTTP " + code + ": " + extractApiError(result)
            );
        }

        return result;
    }

    private static String read(InputStream stream)
            throws Exception {

        if (stream == null) return "";

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                "UTF-8"
                        )
                );

        StringBuilder sb =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();

        return sb.toString();
    }

    private static String extractApiError(String value) {
        try {
            JSONObject obj =
                    new JSONObject(value);

            JSONObject error =
                    obj.optJSONObject("error");

            if (error != null) {
                return error.optString(
                        "message",
                        value
                );
            }

            return value;

        } catch (Exception e) {
            return value;
        }
    }

    private static String safeError(Exception e) {
        String m = e.getMessage();

        if (m == null || m.isEmpty()) {
            return e.getClass().getSimpleName();
        }

        return m;
    }

    private static String systemPrompt() {
        return
                "You are MAYA, a Hindi/Hinglish Android AI assistant. " +
                "Be concise, useful and action-oriented. " +
                "When the user asks for an Android action, clearly identify " +
                "the intended action. Never claim an action was completed " +
                "unless the Android agent actually performed it.";
    }

    private static void remember(
            Context c,
            String question,
            String answer
    ) {
        String old =
                MayaMemory.get(c, "conversation");

        String entry =
                "\nUSER: " + question +
                "\nMAYA: " + answer;

        String combined =
                (old + entry);

        if (combined.length() > 12000) {
            combined =
                    combined.substring(
                            combined.length() - 12000
                    );
        }

        MayaMemory.save(
                c,
                "conversation",
                combined
        );
    }

    public static boolean openApp(Context c, String name) {
        String n = name.toLowerCase();

        String pkg = null;

        if (n.contains("youtube")) {
            pkg = "com.google.android.youtube";
        } else if (n.contains("instagram")) {
            pkg = "com.instagram.android";
        } else if (n.contains("whatsapp")) {
            pkg = "com.whatsapp";
        } else if (n.contains("telegram")) {
            pkg = "org.telegram.messenger";
        } else if (n.contains("facebook")) {
            pkg = "com.facebook.katana";
        } else if (n.contains("chrome")) {
            pkg = "com.android.chrome";
        } else if (n.contains("gmail")) {
            pkg = "com.google.android.gm";
        } else if (n.contains("canva")) {
            pkg = "com.canva.editor";
        } else if (n.contains("play store") ||
                   n.contains("playstore")) {
            pkg = "com.android.vending";
        }

        if (pkg == null) return false;

        try {
            Intent i =
                    c.getPackageManager()
                            .getLaunchIntentForPackage(pkg);

            if (i == null) return false;

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean webSearch(
            Context c,
            String query
    ) {
        try {
            Intent i =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.google.com/search?q="
                                            + URLEncoder.encode(
                                                    query,
                                                    "UTF-8"
                                            )
                            )
                    );

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean dial(
            Context c,
            String number
    ) {
        try {
            Intent i =
                    new Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:" + number)
                    );

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static void executeLocalCommand(
            Context c,
            String command
    ) {
        String x =
                command.toLowerCase();

        if (x.contains("home")) {
            MayaAccessibilityService s =
                    MayaAccessibilityService.getInstance();

            if (s != null) s.home();

            return;
        }

        if (x.contains("back")) {
            MayaAccessibilityService s =
                    MayaAccessibilityService.getInstance();

            if (s != null) s.back();

            return;
        }

        if (x.contains("recent")) {
            MayaAccessibilityService s =
                    MayaAccessibilityService.getInstance();

            if (s != null) s.recent();

            return;
        }

        if (x.contains("notification")) {
            MayaAccessibilityService s =
                    MayaAccessibilityService.getInstance();

            if (s != null) s.notifications();
        }
    }

    public static String extractPhoneNumber(
            String text
    ) {
        Matcher m =
                Pattern.compile(
                        "(\\+?\\d[\\d\\s-]{7,})"
                ).matcher(text);

        if (!m.find()) return "";

        return m.group(1)
                .replaceAll("[^0-9+]", "");
    }
}
