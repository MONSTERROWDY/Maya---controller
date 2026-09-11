#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "=========================================="
echo "        MAYA FINAL BUILD"
echo "=========================================="

mkdir -p app/src/main/java/com/veer/maya
mkdir -p app/src/main/res/xml
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/values
mkdir -p .github/workflows

cat > settings.gradle <<'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MAYA"
include(":app")
EOF

cat > build.gradle <<'EOF'
plugins {
    id 'com.android.application' version '8.6.1' apply false
}
EOF

cat > gradle.properties <<'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
EOF

cat > app/build.gradle <<'EOF'
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.veer.maya'
    compileSdk 35

    defaultConfig {
        applicationId 'com.veer.maya'
        minSdk 26
        targetSdk 35
        versionCode Integer.parseInt(System.getenv("MAYA_VERSION_CODE") ?: "1")
        versionName System.getenv("MAYA_VERSION_NAME") ?: "2.0.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
}
EOF

cat > app/src/main/res/values/strings.xml <<'EOF'
<resources>
    <string name="app_name">MAYA</string>
    <string name="accessibility_description">MAYA Android Control Agent</string>
</resources>
EOF

cat > app/src/main/res/values/styles.xml <<'EOF'
<resources>
    <style name="AppTheme" parent="android:style/Theme.Material.NoActionBar">
        <item name="android:fontFamily">sans</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:statusBarColor">#05070A</item>
        <item name="android:navigationBarColor">#05070A</item>
        <item name="android:colorAccent">#00E5FF</item>
        <item name="android:windowActionModeOverlay">true</item>
    </style>
</resources>
EOF

cat > app/src/main/res/drawable/maya_logo.xml <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <path
        android:fillColor="#071018"
        android:pathData="M54,4A50,50 0,1 0,54 104A50,50 0,1 0,54 4"/>

    <path
        android:strokeColor="#00E5FF"
        android:strokeWidth="3"
        android:fillColor="@android:color/transparent"
        android:pathData="M54,14A40,40 0,1 0,54 94A40,40 0,1 0,54 14"/>

    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M32,68 L43,40 L54,68 L65,40 L76,68"/>

    <circle
        android:fillColor="#00E5FF"
        android:cx="54"
        android:cy="28"
        android:radius="4"/>
</vector>
EOF

cat > app/src/main/res/xml/accessibility_service_config.xml <<'EOF'
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_description"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="true"
    android:canPerformGestures="true"
    android:accessibilityFlags="flagDefault|flagReportViewIds|flagRetrieveInteractiveWindows" />
EOF

cat > app/src/main/AndroidManifest.xml <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.READ_CONTACTS"/>
    <uses-permission android:name="android.permission.CALL_PHONE"/>

    <application
        android:allowBackup="true"
        android:label="MAYA"
        android:theme="@style/AppTheme"
        android:icon="@drawable/maya_logo"
        android:usesCleartextTraffic="false">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <service
            android:name=".MayaVoiceService"
            android:exported="false"
            android:foregroundServiceType="microphone"/>

        <service
            android:name=".MayaAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="false">

            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService"/>
            </intent-filter>

            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config"/>
        </service>

    </application>
</manifest>
EOF

cat > app/src/main/java/com/veer/maya/MayaMemory.java <<'EOF'
package com.veer.maya;

import android.content.Context;
import android.content.SharedPreferences;

public class MayaMemory {

    private static final String PREF = "maya_memory";

    public static void save(Context c, String key, String value) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static String get(Context c, String key) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString(key, "");
    }

    public static void clear(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
EOF

cat > app/src/main/java/com/veer/maya/MayaAccessibilityService.java <<'EOF'
package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

public class MayaAccessibilityService extends AccessibilityService {

    private static MayaAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }

    public static MayaAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isReady() {
        return instance != null;
    }

    public boolean home() {
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }

    public boolean back() {
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }

    public boolean recent() {
        return performGlobalAction(GLOBAL_ACTION_RECENTS);
    }

    public boolean notifications() {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
    }

    public boolean typeText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo target = findEditable(root);
        if (target == null) return false;

        Bundle args = new Bundle();
        args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        return target.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
        );
    }

    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo node) {
        if (node == null) return null;

        if (node.isEditable()) return node;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findEditable(child);
            if (result != null) return result;
        }

        return null;
    }

    public boolean tap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path, 0, 100
                                )
                        )
                        .build();

        return dispatchGesture(gesture, null, null);
    }

    public void executeCommand(String command) {
        if (command == null) return;

        String c = command.toLowerCase();

        if (c.contains("home")) {
            home();
        } else if (c.contains("back")) {
            back();
        } else if (c.contains("recent")) {
            recent();
        } else if (c.contains("notification")) {
            notifications();
        }
    }
}
EOF

cat > app/src/main/java/com/veer/maya/MayaCore.java <<'EOF'
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
EOF

cat > app/src/main/java/com/veer/maya/MayaVoiceService.java <<'EOF'
package com.veer.maya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MayaVoiceService extends Service {

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;

    private WindowManager wm;
    private LinearLayout popup;
    private TextView popupStatus;
    private TextView popupChat;

    private boolean listening = false;
    private boolean liveConversation = false;
    private boolean processing = false;
    private boolean popupShowing = false;

    private final int cyan = Color.rgb(0, 229, 255);

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();
        startForeground(
                77,
                buildNotification()
        );

        tts = new TextToSpeech(
                this,
                status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(
                                new Locale("hi", "IN")
                        );
                    }
                }
        );

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer =
                    SpeechRecognizer.createSpeechRecognizer(this);

            recognizer.setRecognitionListener(
                    new RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(Bundle params) {
                            setStatus("● LISTENING");
                        }

                        @Override
                        public void onBeginningOfSpeech() {
                            setStatus("● LISTENING");
                        }

                        @Override
                        public void onRmsChanged(float rmsdB) {
                        }

                        @Override
                        public void onBufferReceived(byte[] buffer) {
                        }

                        @Override
                        public void onEndOfSpeech() {
                            setStatus("● UNDERSTANDING");
                        }

                        @Override
                        public void onError(int error) {
                            listening = false;

                            if (liveConversation &&
                                    !processing) {
                                restart();
                            }
                        }

                        @Override
                        public void onResults(Bundle results) {
                            listening = false;

                            ArrayList<String> data =
                                    results.getStringArrayList(
                                            SpeechRecognizer.RESULTS_RECOGNITION
                                    );

                            if (data == null ||
                                    data.isEmpty()) {
                                if (liveConversation) restart();
                                return;
                            }

                            String text = data.get(0);

                            handleSpeech(text);
                        }

                        @Override
                        public void onPartialResults(Bundle partialResults) {
                        }

                        @Override
                        public void onEvent(int eventType, Bundle params) {
                        }
                    }
            );
        }
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            return new Notification.Builder(
                    this,
                    "maya_voice"
            )
                    .setContentTitle("MAYA")
                    .setContentText("MAYA Voice Agent is active")
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .build();
        }

        return new Notification.Builder(this)
                .setContentTitle("MAYA")
                .setContentText("MAYA Voice Agent is active")
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel =
                    new NotificationChannel(
                            "maya_voice",
                            "MAYA Voice",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager nm =
                    getSystemService(
                            NotificationManager.class
                    );

            nm.createNotificationChannel(channel);
        }
    }

    private void handleSpeech(String raw) {
        String text =
                raw == null ? "" : raw.trim();

        if (text.isEmpty()) {
            if (liveConversation) restart();
            return;
        }

        String lower =
                text.toLowerCase();

        boolean helloMaya =
                lower.contains("hello maya") ||
                lower.contains("helo maya") ||
                lower.contains("हेलो माया") ||
                lower.contains("हैलो माया");

        boolean helloBoss =
                lower.contains("hello boss") ||
                lower.contains("हेलो बॉस");

        boolean mayaWake =
                lower.equals("maya") ||
                lower.equals("माया");

        if (helloMaya || helloBoss || mayaWake) {
            showPopup();

            if (helloMaya || mayaWake) {
                speak("हाँ बॉस, क्या करना बताइए।");
            } else {
                speak("Hello Boss. हाँ, बोलिए।");
            }

            liveConversation = true;
            restart();
            return;
        }

        if (!liveConversation) {
            return;
        }

        final String userText = text;

        if (processing) return;

        processing = true;
        setStatus("● THINKING");

        handleLocalAction(userText);

        MayaCore.ask(
                this,
                userText,
                (ok, answer) -> {

                    processing = false;

                    runOnUiThread(() -> {
                        updatePopup(
                                userText,
                                answer
                        );
                    });

                    speak(answer);
                    restart();
                }
        );
    }

    private void handleLocalAction(String text) {
        String lower =
                text.toLowerCase();

        if (lower.contains("youtube")) {
            MayaCore.openApp(
                    this,
                    "youtube"
            );
        } else if (lower.contains("instagram")) {
            MayaCore.openApp(
                    this,
                    "instagram"
            );
        } else if (lower.contains("whatsapp")) {
            MayaCore.openApp(
                    this,
                    "whatsapp"
            );
        } else if (lower.contains("telegram")) {
            MayaCore.openApp(
                    this,
                    "telegram"
            );
        } else if (lower.contains("chrome")) {
            MayaCore.openApp(
                    this,
                    "chrome"
            );
        } else if (lower.contains("gmail")) {
            MayaCore.openApp(
                    this,
                    "gmail"
            );
        } else if (lower.contains("canva")) {
            MayaCore.openApp(
                    this,
                    "canva"
            );
        } else if (lower.contains("home")) {
            MayaCore.executeLocalCommand(
                    this,
                    "home"
            );
        } else if (lower.contains("back")) {
            MayaCore.executeLocalCommand(
                    this,
                    "back"
            );
        } else if (lower.contains("recent")) {
            MayaCore.executeLocalCommand(
                    this,
                    "recent"
            );
        } else if (lower.contains("notification")) {
            MayaCore.executeLocalCommand(
                    this,
                    "notification"
            );
        }

        if (lower.contains("call") ||
                lower.contains("कॉल") ||
                lower.contains("phone")) {

            String number =
                    MayaCore.extractPhoneNumber(text);

            if (!number.isEmpty()) {
                showCallConfirmation(number);
            }
        }
    }

    private void showCallConfirmation(String number) {
        if (!popupShowing) showPopup();

        runOnUiThread(() -> {
            Button call =
                    new Button(this);

            call.setText(
                    "CALL " + number
            );

            call.setOnClickListener(v -> {
                MayaCore.dial(
                        this,
                        number
                );

                call.setText("DIALER OPENED");
            });

            if (popup != null) {
                popup.addView(call);
            }
        });
    }

    private void speak(String text) {
        if (tts == null) return;

        runOnUiThread(() -> {
            setStatus("● SPEAKING");

            tts.speak(
                    text == null ? "" : text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "maya_response"
            );
        });
    }

    private void restart() {
        if (!liveConversation ||
                recognizer == null ||
                processing) return;

        postDelayed(
                this::listen,
                700
        );
    }

    private void listen() {
        if (!liveConversation ||
                recognizer == null ||
                listening ||
                processing) {
            return;
        }

        listening = true;

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        recognizer.startListening(intent);
    }

    private void postDelayed(Runnable r, long delay) {
        new android.os.Handler(
                getMainLooper()
        ).postDelayed(r, delay);
    }

    private void setStatus(String status) {
        runOnUiThread(() -> {
            if (popupStatus != null) {
                popupStatus.setText(status);
            }
        });
    }

    private void updatePopup(
            String user,
            String answer
    ) {
        if (!popupShowing ||
                popupChat == null) return;

        popupChat.append(
                "\n\nYOU\n" +
                user +
                "\n\nMAYA\n" +
                answer
        );
    }

    private void showPopup() {
        if (popupShowing) return;

        if (!Settings.canDrawOverlays(this)) {
            return;
        }

        wm =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        popup =
                new LinearLayout(this);

        popup.setOrientation(
                LinearLayout.VERTICAL
        );

        popup.setPadding(
                30, 24, 30, 24
        );

        popup.setBackgroundColor(
                Color.rgb(8, 15, 22)
        );

        TextView title =
                new TextView(this);

        title.setText(
                "◉  MAYA  •  LIVE"
        );

        title.setTextColor(cyan);
        title.setTextSize(21);
        title.setPadding(0, 0, 0, 12);

        popupStatus =
                new TextView(this);

        popupStatus.setText(
                "● AWAKENED"
        );

        popupStatus.setTextColor(
                Color.WHITE
        );

        popupStatus.setTextSize(13);

        popupChat =
                new TextView(this);

        popupChat.setTextColor(
                Color.LTGRAY
        );

        popupChat.setTextSize(15);

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(popupChat);

        Button end =
                new Button(this);

        end.setText("END MAYA LIVE");

        end.setOnClickListener(v -> {
            liveConversation = false;
            processing = false;

            if (recognizer != null) {
                try {
                    recognizer.stopListening();
                } catch (Exception ignored) {
                }
            }

            removePopup();
        });

        popup.addView(title);
        popup.addView(popupStatus);
        popup.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
        popup.addView(end);

        WindowManager.LayoutParams lp =
                new WindowManager.LayoutParams(
                        (int)(340 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density),
                        (int)(470 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density),
                        Build.VERSION.SDK_INT >= 26
                                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                );

        lp.gravity = Gravity.CENTER;

        try {
            wm.addView(popup, lp);
            popupShowing = true;
        } catch (Exception ignored) {
        }
    }

    private void removePopup() {
        if (!popupShowing ||
                wm == null ||
                popup == null) return;

        try {
            wm.removeView(popup);
        } catch (Exception ignored) {
        }

        popupShowing = false;
        popup = null;
        popupStatus = null;
        popupChat = null;
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        liveConversation = true;
        restart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        liveConversation = false;

        if (recognizer != null) {
            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        removePopup();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
EOF

cat > app/src/main/java/com/veer/maya/MainActivity.java <<'EOF'
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
EOF

cat > .github/workflows/android.yml <<'EOF'
name: MAYA Android Build

on:
  push:
    branches:
      - main
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.7'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Install SDK
        run: |
          sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

      - name: Build APK
        env:
          MAYA_VERSION_CODE: ${{ github.run_number }}
          MAYA_VERSION_NAME: 2.0.${{ github.run_number }}
        run: |
          gradle :app:assembleDebug --no-daemon

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: MAYA-${{ github.run_number }}
          path: app/build/outputs/apk/debug/app-debug.apk

      - name: Create Release
        if: github.ref == 'refs/heads/main'
        uses: softprops/action-gh-release@v2
        with:
          tag_name: maya-${{ github.run_number }}
          name: MAYA ${{ github.run_number }}
          files: app/build/outputs/apk/debug/app-debug.apk
          generate_release_notes: true
EOF

cat > README.md <<'EOF'
# MAYA

MAYA is an Android AI voice assistant and automation controller.

Features:

- Gemini API
- OpenAI API
- Separate API token settings
- Hello Boss
- Hello Maya
- Live voice
- Floating overlay
- Text to speech
- Speech recognition
- Persistent local memory
- Accessibility Android agent
- App launching
- Home / Back / Recent / Notifications
- Call/dial workflow
- GitHub Actions APK build

API keys are stored locally in Android SharedPreferences and are not included in source code.
EOF

cat > .gitignore <<'EOF'
.gradle/
build/
app/build/
local.properties
*.iml
.idea/
.DS_Store
*.keystore
*.jks
EOF

echo ""
echo "=========================================="
echo "   FILES CREATED"
echo "=========================================="

git status --short

echo ""
echo "Checking Java/XML files..."
find app/src -type f | sort

echo ""
echo "Saving final build..."

git add .

git commit -m "feat: MAYA final consolidated AI voice agent" || true

git push origin main

echo ""
echo "=========================================="
echo "        MAYA FINAL PUSH COMPLETE"
echo "=========================================="
echo ""
echo "GitHub Actions:"
echo "https://github.com/MONSTERROWDY/Maya---controller/actions"
echo ""
echo "Repository:"
echo "https://github.com/MONSTERROWDY/Maya---controller"
echo ""
echo "Wait for Android Build to finish."
echo "=========================================="
