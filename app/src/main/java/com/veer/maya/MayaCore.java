package com.veer.maya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MayaCore {

    private static final String PREF = "maya_settings";

    /*
     * Default Gemini model.
     *
     * IMPORTANT:
     * User can change this from MAYA AI Settings.
     */
    private static final String DEFAULT_GEMINI_MODEL = "gemini-2.5-flash";

    /*
     * Fallback models.
     *
     * MAYA will try these only when the selected Gemini model
     * temporarily fails with 503/UNAVAILABLE.
     */
    private static final String[] GEMINI_FALLBACK_MODELS = {
            "gemini-2.5-flash",
            "gemini-2.0-flash"
    };

    private static final int MAX_503_RETRIES = 2;

    public interface Callback {
        void done(boolean ok, String answer);
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
        );
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    public static String getGeminiKey(Context c) {
        return prefs(c).getString(
                "gemini_key",
                ""
        );
    }

    public static String getGeminiModel(Context c) {
        String model = prefs(c).getString(
                "gemini_model",
                DEFAULT_GEMINI_MODEL
        );

        if (model == null || model.trim().isEmpty()) {
            return DEFAULT_GEMINI_MODEL;
        }

        return model.trim();
    }

    public static String getOpenAIKey(Context c) {
        return prefs(c).getString(
                "openai_key",
                ""
        );
    }

    public static String getOpenAIModel(Context c) {
        return prefs(c).getString(
                "openai_model",
                ""
        );
    }

    public static String getProvider(Context c) {
        return prefs(c).getString(
                "provider",
                "Gemini"
        );
    }

    public static void saveSettings(
            Context c,
            String geminiKey,
            String geminiModel,
            String openAIKey,
            String openAIModel,
            String provider
    ) {

        String gModel =
                geminiModel == null
                        ? ""
                        : geminiModel.trim();

        if (gModel.isEmpty()) {
            gModel = DEFAULT_GEMINI_MODEL;
        }

        String oModel =
                openAIModel == null
                        ? ""
                        : openAIModel.trim();

        String selectedProvider =
                provider == null ||
                provider.trim().isEmpty()
                        ? "Gemini"
                        : provider.trim();

        prefs(c)
                .edit()
                .putString(
                        "gemini_key",
                        geminiKey == null
                                ? ""
                                : geminiKey.trim()
                )
                .putString(
                        "gemini_model",
                        gModel
                )
                .putString(
                        "openai_key",
                        openAIKey == null
                                ? ""
                                : openAIKey.trim()
                )
                .putString(
                        "openai_model",
                        oModel
                )
                .putString(
                        "provider",
                        selectedProvider
                )
                .apply();
    }

    // ============================================================
    // MAIN AI ENTRY
    // ============================================================

    public static void ask(
            Context c,
            String text,
            Callback callback
    ) {

        new Thread(() -> {

            try {

                if (text == null) {
                    callback.done(
                            false,
                            "MAYA: Empty command."
                    );
                    return;
                }

                String cleanText =
                        text.trim();

                if (cleanText.isEmpty()) {
                    callback.done(
                            false,
                            "MAYA: Please say something."
                    );
                    return;
                }

                String provider =
                        getProvider(c);

                // ------------------------------------------------
                // OPENAI
                // ------------------------------------------------

                if ("OpenAI".equalsIgnoreCase(provider)) {

                    String key =
                            getOpenAIKey(c);

                    if (key.isEmpty()) {

                        callback.done(
                                false,
                                "OpenAI API key is not set. " +
                                "Open AI Settings and add your key."
                        );

                        return;
                    }

                    String model =
                            getOpenAIModel(c);

                    if (model.isEmpty()) {

                        callback.done(
                                false,
                                "OpenAI model is empty. " +
                                "Enter your available OpenAI model " +
                                "in AI Settings."
                        );

                        return;
                    }

                    callOpenAI(
                            c,
                            key,
                            model,
                            cleanText,
                            callback
                    );

                    return;
                }

                // ------------------------------------------------
                // GEMINI
                // ------------------------------------------------

                String key =
                        getGeminiKey(c);

                if (key.isEmpty()) {

                    callback.done(
                            false,
                            "Gemini API key is not set. " +
                            "Open AI Settings and add your key."
                    );

                    return;
                }

                String model =
                        getGeminiModel(c);

                callGeminiWithRecovery(
                        c,
                        key,
                        model,
                        cleanText,
                        callback
                );

            } catch (Exception e) {

                callback.done(
                        false,
                        "MAYA Error: " +
                        safeError(e)
                );
            }

        }).start();
    }

    // ============================================================
    // GEMINI RECOVERY SYSTEM
    // ============================================================

    private static void callGeminiWithRecovery(
            Context c,
            String key,
            String selectedModel,
            String text,
            Callback callback
    ) {

        String model =
                cleanModelName(selectedModel);

        if (model.isEmpty()) {
            model = DEFAULT_GEMINI_MODEL;
        }

        try {

            String response =
                    callGeminiRequestWithRetry(
                            key,
                            model,
                            text
                    );

            String answer =
                    parseGeminiAnswer(response);

            if (answer.isEmpty()) {

                callback.done(
                        false,
                        "Gemini returned an empty response."
                );

                return;
            }

            remember(
                    c,
                    text,
                    answer
            );

            callback.done(
                    true,
                    answer
            );

        } catch (Exception firstError) {

            String message =
                    safeError(firstError);

            /*
             * If the selected model is overloaded/unavailable,
             * try alternate Gemini models.
             */
            if (isTemporaryGeminiError(message)) {

                for (String fallback :
                        GEMINI_FALLBACK_MODELS) {

                    String fallbackClean =
                            cleanModelName(fallback);

                    /*
                     * Don't retry the exact same model.
                     */
                    if (fallbackClean.equalsIgnoreCase(model)) {
                        continue;
                    }

                    try {

                        String response =
                                callGeminiRequestWithRetry(
                                        key,
                                        fallbackClean,
                                        text
                                );

                        String answer =
                                parseGeminiAnswer(
                                        response
                                );

                        if (!answer.isEmpty()) {

                            remember(
                                    c,
                                    text,
                                    answer
                            );

                            callback.done(
                                    true,
                                    answer
                            );

                            return;
                        }

                    } catch (Exception ignored) {
                        // Continue to next fallback.
                    }
                }
            }

            callback.done(
                    false,
                    formatGeminiError(message)
            );
        }
    }

    // ============================================================
    // GEMINI REQUEST + RETRY
    // ============================================================

    private static String callGeminiRequestWithRetry(
            String key,
            String model,
            String text
    ) throws Exception {

        Exception lastError = null;

        for (int attempt = 0;
             attempt <= MAX_503_RETRIES;
             attempt++) {

            try {

                return callGeminiRequest(
                        key,
                        model,
                        text
                );

            } catch (Exception e) {

                lastError = e;

                String error =
                        safeError(e);

                /*
                 * Only retry temporary overload/unavailable
                 * situations.
                 */
                if (!isTemporaryGeminiError(error)) {
                    throw e;
                }

                if (attempt >= MAX_503_RETRIES) {
                    throw e;
                }

                /*
                 * Exponential-ish delay:
                 *
                 * attempt 0 -> 1500ms
                 * attempt 1 -> 3000ms
                 */
                long delay =
                        1500L *
                        (attempt + 1);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }

        if (lastError != null) {
            throw lastError;
        }

        throw new Exception(
                "Gemini request failed."
        );
    }

    // ============================================================
    // GEMINI HTTP REQUEST
    // ============================================================

    private static String callGeminiRequest(
            String key,
            String model,
            String text
    ) throws Exception {

        String cleanModel =
                cleanModelName(model);

        String endpoint =
                "https://generativelanguage.googleapis.com/" +
                "v1beta/models/" +
                URLEncoder.encode(
                        cleanModel,
                        "UTF-8"
                ) +
                ":generateContent";

        JSONObject root =
                new JSONObject();

        JSONArray contents =
                new JSONArray();

        JSONObject content =
                new JSONObject();

        JSONArray parts =
                new JSONArray();

        JSONObject part =
                new JSONObject();

        String prompt =
                systemPrompt() +
                "\n\nUSER:\n" +
                text;

        part.put(
                "text",
                prompt
        );

        parts.put(part);

        content.put(
                "parts",
                parts
        );

        contents.put(content);

        root.put(
                "contents",
                contents
        );

        return post(
                endpoint,
                root.toString(),
                "x-goog-api-key",
                key
        );
    }

    // ============================================================
    // GEMINI RESPONSE PARSER
    // ============================================================

    private static String parseGeminiAnswer(
            String response
    ) throws Exception {

        JSONObject obj =
                new JSONObject(response);

        if (obj.has("error")) {

            JSONObject error =
                    obj.optJSONObject("error");

            if (error != null) {

                throw new Exception(
                        "HTTP " +
                        error.optInt(
                                "code",
                                500
                        ) +
                        ": " +
                        error.optString(
                                "message",
                                "Gemini API Error"
                        )
                );
            }

            throw new Exception(
                    "Gemini API Error"
            );
        }

        StringBuilder answer =
                new StringBuilder();

        JSONArray candidates =
                obj.optJSONArray(
                        "candidates"
                );

        if (candidates != null) {

            for (
                    int i = 0;
                    i < candidates.length();
                    i++
            ) {

                JSONObject candidate =
                        candidates.optJSONObject(i);

                if (candidate == null) {
                    continue;
                }

                JSONObject content =
                        candidate.optJSONObject(
                                "content"
                        );

                if (content == null) {
                    continue;
                }

                JSONArray parts =
                        content.optJSONArray(
                                "parts"
                        );

                if (parts == null) {
                    continue;
                }

                for (
                        int j = 0;
                        j < parts.length();
                        j++
                ) {

                    JSONObject p =
                            parts.optJSONObject(j);

                    if (p == null) {
                        continue;
                    }

                    String t =
                            p.optString(
                                    "text",
                                    ""
                            );

                    if (!t.isEmpty()) {

                        if (answer.length() > 0) {
                            answer.append("\n");
                        }

                        answer.append(t);
                    }
                }
            }
        }

        return answer.toString().trim();
    }

    // ============================================================
    // OPENAI
    // ============================================================

    private static void callOpenAI(
            Context c,
            String key,
            String model,
            String text,
            Callback callback
    ) throws Exception {

        JSONObject root =
                new JSONObject();

        root.put(
                "model",
                model
        );

        root.put(
                "input",
                systemPrompt() +
                "\n\nUSER:\n" +
                text
        );

        String response =
                post(
                        "https://api.openai.com/v1/responses",
                        root.toString(),
                        "Authorization",
                        "Bearer " + key
                );

        JSONObject obj =
                new JSONObject(response);

        if (obj.has("error")) {

            JSONObject error =
                    obj.optJSONObject("error");

            String message =
                    error == null
                            ? "OpenAI API Error"
                            : error.optString(
                                    "message",
                                    "OpenAI API Error"
                            );

            callback.done(
                    false,
                    formatOpenAIError(
                            message
                    )
            );

            return;
        }

        String answer =
                obj.optString(
                        "output_text",
                        ""
                );

        /*
         * Compatibility parser for Responses API.
         */
        if (answer.isEmpty()) {

            JSONArray output =
                    obj.optJSONArray(
                            "output"
                    );

            if (output != null) {

                StringBuilder sb =
                        new StringBuilder();

                for (
                        int i = 0;
                        i < output.length();
                        i++
                ) {

                    JSONObject item =
                            output.optJSONObject(i);

                    if (item == null) {
                        continue;
                    }

                    JSONArray content =
                            item.optJSONArray(
                                    "content"
                            );

                    if (content == null) {
                        continue;
                    }

                    for (
                            int j = 0;
                            j < content.length();
                            j++
                    ) {

                        JSONObject block =
                                content.optJSONObject(j);

                        if (block == null) {
                            continue;
                        }

                        String t =
                                block.optString(
                                        "text",
                                        ""
                                );

                        if (!t.isEmpty()) {

                            if (sb.length() > 0) {
                                sb.append("\n");
                            }

                            sb.append(t);
                        }
                    }
                }

                answer =
                        sb.toString().trim();
            }
        }

        if (answer.isEmpty()) {

            callback.done(
                    false,
                    "OpenAI returned an empty response."
            );

            return;
        }

        remember(
                c,
                text,
                answer
        );

        callback.done(
                true,
                answer
        );
    }

    // ============================================================
    // GENERIC HTTP POST
    // ============================================================

    private static String post(
            String endpoint,
            String body,
            String headerName,
            String headerValue
    ) throws Exception {

        HttpURLConnection conn = null;

        try {

            URL url =
                    new URL(endpoint);

            conn =
                    (HttpURLConnection)
                            url.openConnection();

            conn.setRequestMethod(
                    "POST"
            );

            conn.setConnectTimeout(
                    20000
            );

            conn.setReadTimeout(
                    60000
            );

            conn.setDoOutput(
                    true
            );

            conn.setUseCaches(
                    false
            );

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

            String result =
                    read(stream);

            if (code < 200 || code >= 300) {

                String apiError =
                        extractApiError(
                                result
                        );

                throw new Exception(
                        "HTTP " +
                        code +
                        ": " +
                        apiError
                );
            }

            return result;

        } catch (SocketTimeoutException e) {

            throw new Exception(
                    "Network timeout. " +
                    "Please check internet connection " +
                    "and try again."
            );

        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // ============================================================
    // READ RESPONSE
    // ============================================================

    private static String read(
            InputStream stream
    ) throws Exception {

        if (stream == null) {
            return "";
        }

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

        while (
                (line = reader.readLine())
                        != null
        ) {

            sb.append(line);
        }

        reader.close();

        return sb.toString();
    }

    // ============================================================
    // API ERROR EXTRACTION
    // ============================================================

    private static String extractApiError(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Unknown API error";
        }

        try {

            JSONObject obj =
                    new JSONObject(value);

            JSONObject error =
                    obj.optJSONObject(
                            "error"
                    );

            if (error != null) {

                String message =
                        error.optString(
                                "message",
                                ""
                        );

                if (!message.isEmpty()) {
                    return message;
                }
            }

            return value;

        } catch (Exception e) {

            return value;
        }
    }

    // ============================================================
    // TEMPORARY GEMINI ERROR DETECTOR
    // ============================================================

    private static boolean isTemporaryGeminiError(
            String message
    ) {

        if (message == null) {
            return false;
        }

        String m =
                message.toLowerCase(
                        Locale.US
                );

        return
                m.contains("http 503") ||
                m.contains("503") ||
                m.contains("unavailable") ||
                m.contains("high demand") ||
                m.contains("overloaded") ||
                m.contains("temporarily") ||
                m.contains("try again later");
    }

    // ============================================================
    // USER FRIENDLY GEMINI ERROR
    // ============================================================

    private static String formatGeminiError(
            String message
    ) {

        if (message == null) {
            return "Gemini request failed.";
        }

        String m =
                message.toLowerCase(
                        Locale.US
                );

        if (m.contains("503") ||
                m.contains("high demand") ||
                m.contains("unavailable") ||
                m.contains("overloaded")) {

            return
                    "Gemini is temporarily unavailable " +
                    "or experiencing high demand. " +
                    "MAYA automatically retried the request. " +
                    "Please try again in a moment.";
        }

        if (m.contains("429") ||
                m.contains("quota") ||
                m.contains("rate limit")) {

            return
                    "Gemini API quota/rate limit reached. " +
                    "The API key may be valid, but the current " +
                    "usage limit has been reached.";
        }

        if (m.contains("401") ||
                m.contains("403") ||
                m.contains("api key") ||
                m.contains("permission")) {

            return
                    "Gemini API key or API permission problem. " +
                    "Check the key and Gemini API access.";
        }

        if (m.contains("404") ||
                m.contains("not found")) {

            return
                    "Gemini model was not found. " +
                    "Open AI Settings and select an available model.";
        }

        if (m.contains("timeout") ||
                m.contains("network")) {

            return
                    "Network connection problem. " +
                    "Check your internet connection and try again.";
        }

        return
                "MAYA Error: " +
                message;
    }

    // ============================================================
    // USER FRIENDLY OPENAI ERROR
    // ============================================================

    private static String formatOpenAIError(
            String message
    ) {

        if (message == null) {
            return "OpenAI request failed.";
        }

        String m =
                message.toLowerCase(
                        Locale.US
                );

        if (m.contains("429") ||
                m.contains("quota") ||
                m.contains("rate limit") ||
                m.contains("billing")) {

            return
                    "OpenAI quota/billing limit reached. " +
                    "Please check your OpenAI API usage.";
        }

        if (m.contains("401") ||
                m.contains("invalid api key") ||
                m.contains("authentication")) {

            return
                    "OpenAI API key is invalid or unauthorized.";
        }

        if (m.contains("404") ||
                m.contains("model")) {

            return
                    "OpenAI model was not found or is unavailable.";
        }

        return
                "OpenAI Error: " +
                message;
    }

    // ============================================================
    // SAFE ERROR
    // ============================================================

    private static String safeError(
            Exception e
    ) {

        String message =
                e.getMessage();

        if (message == null ||
                message.isEmpty()) {

            return e.getClass()
                    .getSimpleName();
        }

        return message;
    }

    // ============================================================
    // MODEL CLEANER
    // ============================================================

    private static String cleanModelName(
            String model
    ) {

        if (model == null) {
            return "";
        }

        String clean =
                model.trim();

        if (clean.startsWith(
                "models/"
        )) {

            clean =
                    clean.substring(
                            7
                    );
        }

        return clean;
    }

    // ============================================================
    // MAYA SYSTEM PROMPT
    // ============================================================

    private static String systemPrompt() {

        return
                "You are MAYA, a Hindi/Hinglish Android AI assistant. " +
                "Be concise, useful and action-oriented. " +
                "Understand natural language commands. " +
                "When the user asks for an Android action, clearly " +
                "identify the intended action. " +
                "Never claim an action was completed unless the " +
                "Android agent actually performed it. " +
                "Do not invent device state. " +
                "If an action requires Android accessibility permission, " +
                "explain that permission may be required.";
    }

    // ============================================================
    // MEMORY
    // ============================================================

    private static void remember(
            Context c,
            String question,
            String answer
    ) {

        try {

            String old =
                    MayaMemory.get(
                            c,
                            "conversation"
                    );

            if (old == null) {
                old = "";
            }

            String entry =
                    "\nUSER: " +
                    question +
                    "\nMAYA: " +
                    answer;

            String combined =
                    old + entry;

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

        } catch (Exception ignored) {
            // Memory failure must never crash AI.
        }
    }

    // ============================================================
    // APP LAUNCHER
    // ============================================================

    public static boolean openApp(
            Context c,
            String name
    ) {

        if (name == null) {
            return false;
        }

        String n =
                name.toLowerCase(
                        Locale.US
                );

        String pkg = null;

        if (n.contains("youtube")) {

            pkg =
                    "com.google.android.youtube";

        } else if (n.contains("instagram")) {

            pkg =
                    "com.instagram.android";

        } else if (n.contains("whatsapp")) {

            pkg =
                    "com.whatsapp";

        } else if (n.contains("telegram")) {

            pkg =
                    "org.telegram.messenger";

        } else if (n.contains("facebook")) {

            pkg =
                    "com.facebook.katana";

        } else if (n.contains("chrome")) {

            pkg =
                    "com.android.chrome";

        } else if (n.contains("gmail")) {

            pkg =
                    "com.google.android.gm";

        } else if (n.contains("canva")) {

            pkg =
                    "com.canva.editor";

        } else if (
                n.contains("play store") ||
                n.contains("playstore")
        ) {

            pkg =
                    "com.android.vending";
        }

        if (pkg == null) {
            return false;
        }

        try {

            Intent intent =
                    c.getPackageManager()
                            .getLaunchIntentForPackage(
                                    pkg
                            );

            if (intent == null) {
                return false;
            }

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            c.startActivity(
                    intent
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // GOOGLE SEARCH
    // ============================================================

    public static boolean webSearch(
            Context c,
            String query
    ) {

        if (query == null ||
                query.trim().isEmpty()) {

            return false;
        }

        try {

            String encoded =
                    URLEncoder.encode(
                            query.trim(),
                            "UTF-8"
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.google.com/search?q=" +
                                    encoded
                            )
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            c.startActivity(
                    intent
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // DIALER
    // ============================================================

    public static boolean dial(
            Context c,
            String number
    ) {

        if (number == null) {
            number = "";
        }

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(
                                    "tel:" + number
                            )
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            c.startActivity(
                    intent
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // LOCAL ANDROID COMMANDS
    // ============================================================

    public static void executeLocalCommand(
            Context c,
            String command
    ) {

        if (command == null) {
            return;
        }

        String x =
                command.toLowerCase(
                        Locale.US
                );

        MayaAccessibilityService service =
                MayaAccessibilityService.getInstance();

        if (service == null) {
            return;
        }

        if (x.contains("home")) {

            service.home();
            return;
        }

        if (x.contains("back")) {

            service.back();
            return;
        }

        if (x.contains("recent")) {

            service.recent();
            return;
        }

        if (x.contains("notification")) {

            service.notifications();
        }
    }

    // ============================================================
    // PHONE NUMBER EXTRACTION
    // ============================================================

    public static String extractPhoneNumber(
            String text
    ) {

        if (text == null) {
            return "";
        }

        Matcher matcher =
                Pattern.compile(
                        "(\\+?\\d[\\d\\s-]{7,})"
                ).matcher(text);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
                .replaceAll(
                        "[^0-9+]",
                        ""
                );
    }

    // ============================================================
    // PENDING COMMAND COMPATIBILITY
    // ============================================================

    public static void confirmPending(
            Context c
    ) {

        String command =
                MayaCommandReceiver
                        .takePendingCommand(c);

        if (command == null ||
                command.trim().isEmpty()) {

            return;
        }

        MayaAccessibilityService service =
                MayaAccessibilityService
                        .getInstance();

        if (service != null) {

            service.executeCommand(
                    command.trim()
            );
        }
    }

    public static void cancelPending(
            Context c
    ) {

        MayaCommandReceiver
                .takePendingCommand(c);
    }
}
