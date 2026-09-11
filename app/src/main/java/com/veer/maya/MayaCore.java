package com.veer.maya;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.nio.charset.StandardCharsets;

public final class MayaCore {

    private static final String PREF = "maya_secure";
    private static final String API_KEY = "api_key";
    private static final String ENDPOINT = "endpoint";
    private static final String MODEL = "model";

    private MayaCore(){}

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void saveApi(Context c, String key) {
        prefs(c).edit().putString(API_KEY, key == null ? "" : key.trim()).apply();
    }

    public static String getApi(Context c) {
        return prefs(c).getString(API_KEY, "").trim();
    }

    public static void saveEndpoint(Context c, String endpoint) {
        prefs(c).edit().putString(ENDPOINT, endpoint.trim()).apply();
    }

    public static String getEndpoint(Context c) {
        return prefs(c).getString(
            ENDPOINT,
            "https://api.openai.com/v1/responses"
        ).trim();
    }

    public static void saveModel(Context c, String model) {
        prefs(c).edit().putString(MODEL, model.trim()).apply();
    }

    public static String getModel(Context c) {
        return prefs(c).getString(MODEL, "gpt-5.6-luna").trim();
    }

    public static boolean hasApi(Context c) {
        return !getApi(c).isEmpty();
    }

    public static void ask(
            Context context,
            String userText,
            Callback callback) {

        String key = getApi(context);

        if (key.isEmpty()) {
            callback.done(
                false,
                "API KEY MISSING\n\nSettings → API Key में अपनी API key save करें।"
            );
            return;
        }

        if (userText == null || userText.trim().isEmpty()) {
            callback.done(false, "कोई command नहीं मिली।");
            return;
        }

        new Thread(() -> {

            HttpURLConnection conn = null;

            try {
                URL url = new URL(getEndpoint(context));

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                // IMPORTANT:
                // Correct API headers. This avoids the old authentication error.
                conn.setRequestProperty(
                    "Authorization",
                    "Bearer " + key
                );

                conn.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                conn.setRequestProperty(
                    "Accept",
                    "application/json"
                );

                conn.setConnectTimeout(20000);
                conn.setReadTimeout(60000);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();

                body.put("model", getModel(context));

                JSONArray input = new JSONArray();

                JSONObject message = new JSONObject();
                message.put("role", "user");

                JSONArray content = new JSONArray();

                JSONObject text = new JSONObject();
                text.put("type", "input_text");
                text.put(
                    "text",
                    "You are MAYA, a practical Android AI assistant. " +
                    "Reply naturally in Hindi/Hinglish when appropriate. " +
                    "User command: " + userText
                );

                content.put(text);
                message.put("content", content);
                input.put(message);

                body.put("input", input);

                byte[] bytes = body.toString()
                    .getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes);
                }

                int code = conn.getResponseCode();

                InputStream stream =
                    code >= 200 && code < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

                StringBuilder result = new StringBuilder();

                if (stream != null) {
                    BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                            stream,
                            StandardCharsets.UTF_8
                        )
                    );

                    String line;

                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    br.close();
                }

                String raw = result.toString();

                if (code < 200 || code >= 300) {

                    String error = raw;

                    try {
                        JSONObject e = new JSONObject(raw);

                        if (e.has("error")) {
                            Object obj = e.get("error");

                            if (obj instanceof JSONObject) {
                                JSONObject eo = (JSONObject)obj;
                                error = eo.optString(
                                    "message",
                                    raw
                                );
                            }
                        }
                    } catch (Exception ignored) {}

                    callback.done(
                        false,
                        "API ERROR (" + code + ")\n\n" + error
                    );

                    return;
                }

                String answer = extractText(raw);

                if (answer.isEmpty()) {
                    answer = raw;
                }

                callback.done(true, answer);

            } catch (Exception e) {

                callback.done(
                    false,
                    "NETWORK/API ERROR\n\n" +
                    safeMessage(e)
                );

            } finally {

                if (conn != null) {
                    conn.disconnect();
                }
            }

        }).start();
    }

    public static void confirmPending(Context context) {
        String command = MayaCommandReceiver.takePendingCommand(context);
        if (command != null && MayaAccessibilityService.isReady()) {
            MayaAccessibilityService.getInstance().executeCommand(command);
        }
    }

    public static void cancelPending(Context context) {
        context.getSharedPreferences(
            "maya_controller",
            Context.MODE_PRIVATE
        ).edit().remove("pending_command").apply();
    }

    private static String extractText(String raw) {

        try {

            JSONObject root = new JSONObject(raw);

            String direct =
                root.optString("output_text", "");

            if (!direct.isEmpty()) {
                return direct;
            }

            JSONArray output =
                root.optJSONArray("output");

            if (output == null) {
                return "";
            }

            StringBuilder all =
                new StringBuilder();

            for (int i = 0; i < output.length(); i++) {

                JSONObject item =
                    output.optJSONObject(i);

                if (item == null) continue;

                JSONArray content =
                    item.optJSONArray("content");

                if (content == null) continue;

                for (int j = 0; j < content.length(); j++) {

                    JSONObject c =
                        content.optJSONObject(j);

                    if (c == null) continue;

                    String t =
                        c.optString("text", "");

                    if (!t.isEmpty()) {
                        if (all.length() > 0) {
                            all.append("\n");
                        }
                        all.append(t);
                    }
                }
            }

            return all.toString().trim();

        } catch (Exception e) {
            return "";
        }
    }

    private static String safeMessage(Exception e) {
        String s = e.getMessage();

        if (s == null || s.trim().isEmpty()) {
            return e.getClass().getSimpleName();
        }

        return s;
    }

    public interface Callback {
        void done(boolean success, String text);
    }
}
