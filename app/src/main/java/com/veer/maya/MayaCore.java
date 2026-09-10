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

import java.nio.charset.StandardCharsets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MayaCore {

    private static final String PREFS =
            "maya_ai";

    private static final String KEY =
            "openai_key";

    private static final String PENDING =
            "pending_actions";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private MayaCore(){}

    public static String getKey(Context context){

        return context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .getString(KEY, "");
    }

    public static void saveKey(
            Context context,
            String key
    ){

        if(key == null) key = "";

        context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        KEY,
                        key.trim()
                )
                .apply();
    }

    public static boolean hasKey(Context context){

        return !getKey(context).trim().isEmpty();
    }

    public static void process(
            Context context,
            String user
    ){

        if(user == null ||
                user.trim().isEmpty()) return;

        Context app =
                context.getApplicationContext();

        MayaMemory.add(
                app,
                "user",
                user
        );

        if(!hasKey(app)){

            reply(
                    app,
                    "OpenAI API key अभी सेट नहीं है। Settings → AI Connection में key save करें।"
            );

            return;
        }

        EXECUTOR.execute(
                () -> callAI(app,user.trim())
        );
    }

    public static void testKey(
            Context context
    ){

        Context app =
                context.getApplicationContext();

        String key = getKey(app);

        if(key.trim().isEmpty()){

            reply(
                    app,
                    "API key खाली है। पहले key save करें।"
            );

            return;
        }

        EXECUTOR.execute(
                () -> {

                    try{

                        JSONObject body =
                                new JSONObject();

                        body.put(
                                "model",
                                "gpt-5.6-luna"
                        );

                        body.put(
                                "input",
                                "Reply only with: MAYA API OK"
                        );

                        body.put(
                                "instructions",
                                "Return a short confirmation."
                        );

                        HttpURLConnection connection =
                                (HttpURLConnection)
                                        new URL(
                                                "https://api.openai.com/v1/responses"
                                        ).openConnection();

                        connection.setRequestMethod(
                                "POST"
                        );

                        connection.setConnectTimeout(
                                20000
                        );

                        connection.setReadTimeout(
                                60000
                        );

                        connection.setRequestProperty(
                                "Authorization",
                                "Bearer " + key
                        );

                        connection.setRequestProperty(
                                "Content-Type",
                                "application/json"
                        );

                        connection.setDoOutput(true);

                        try(OutputStream output =
                                    connection.getOutputStream()){

                            output.write(
                                    body.toString()
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            );
                        }

                        int code =
                                connection.getResponseCode();

                        String response =
                                readResponse(
                                        connection,
                                        code
                                );

                        if(code >= 200 &&
                                code < 300){

                            reply(
                                    app,
                                    "✅ API connection successful. MAYA AI is ready."
                            );

                        }else{

                            String message =
                                    extractError(response);

                            reply(
                                    app,
                                    "❌ API error " +
                                            code +
                                            ": " +
                                            message
                            );
                        }

                        connection.disconnect();

                    }catch(Exception e){

                        reply(
                                app,
                                "❌ Connection failed. Internet/API access check करें."
                        );
                    }
                }
        );
    }

    private static void callAI(
            Context context,
            String user
    ){

        try{

            JSONObject body =
                    new JSONObject();

            body.put(
                    "model",
                    "gpt-5.6-luna"
            );

            body.put(
                    "instructions",
                    systemPrompt()
            );

            body.put(
                    "input",
                    "Memory:\n" +
                            MayaMemory.get(context) +
                            "\n\nUser:\n" +
                            user
            );

            HttpURLConnection connection =
                    (HttpURLConnection)
                            new URL(
                                    "https://api.openai.com/v1/responses"
                            ).openConnection();

            connection.setRequestMethod(
                    "POST"
            );

            connection.setConnectTimeout(
                    20000
            );

            connection.setReadTimeout(
                    60000
            );

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + getKey(context)
            );

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            connection.setDoOutput(true);

            try(OutputStream output =
                        connection.getOutputStream()){

                output.write(
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );
            }

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            if(code >= 400){

                reply(
                        context,
                        "OpenAI API error " +
                                code +
                                ": " +
                                extractError(response)
                );

                return;
            }

            JSONObject result =
                    new JSONObject(response);

            String text =
                    extractText(result);

            JSONObject answer;

            try{

                answer =
                        new JSONObject(text);

            }catch(Exception e){

                answer =
                        new JSONObject();

                answer.put(
                        "say",
                        text
                );

                answer.put(
                        "memory",
                        new JSONArray()
                );

                answer.put(
                        "actions",
                        new JSONArray()
                );
            }

            String say =
                    answer.optString(
                            "say",
                            "ठीक है।"
                    );

            JSONArray memory =
                    answer.optJSONArray(
                            "memory"
                    );

            if(memory != null){

                for(int i=0;
                    i<memory.length();
                    i++){

                    MayaMemory.add(
                            context,
                            "memory",
                            memory.optString(i)
                    );
                }
            }

            JSONArray actions =
                    answer.optJSONArray(
                            "actions"
                    );

            if(actions == null)
                actions = new JSONArray();

            JSONArray safe =
                    new JSONArray();

            JSONArray dangerous =
                    new JSONArray();

            for(int i=0;
                i<actions.length();
                i++){

                JSONObject action =
                        actions.optJSONObject(i);

                if(action == null) continue;

                String type =
                        action.optString(
                                "type"
                        );

                if(
                        "dial".equals(type) ||
                        "message".equals(type) ||
                        "send_message".equals(type) ||
                        "purchase".equals(type) ||
                        "delete".equals(type)
                ){

                    dangerous.put(action);

                }else{

                    safe.put(action);
                }
            }

            runActions(
                    context,
                    safe
            );

            if(dangerous.length() > 0){

                savePending(
                        context,
                        dangerous
                );

                reply(
                        context,
                        say +
                                "\n\n⚠️ यह action करने से पहले आपकी confirmation चाहिए."
                );

            }else{

                reply(
                        context,
                        say
                );
            }

            connection.disconnect();

        }catch(Exception e){

            reply(
                    context,
                    "MAYA connection problem आया। Internet, API key या model access check करें."
            );
        }
    }

    private static String systemPrompt(){

        return
                "You are MAYA, a practical personal Android AI assistant. " +
                "Understand Hindi, English, Hinglish, Maithili and Bhojpuri. " +
                "Answer naturally in the user's language. " +
                "Return ONLY valid JSON with keys say, memory and actions. " +
                "Allowed actions: " +
                "open_app, tap, type, scroll, back, home, recent, web_search, dial, message. " +
                "Use multiple actions when a task needs multiple steps. " +
                "Never invent phone numbers or claim an action succeeded unless the controller can execute it. " +
                "Calls, messages, purchases, deleting data and other high-impact actions require confirmation. " +
                "Keep say concise.";
    }

    private static String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception{

        InputStream stream =
                code >= 400
                        ? connection.getErrorStream()
                        : connection.getInputStream();

        if(stream == null)
            return "";

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while((line = reader.readLine()) != null){

            result.append(line);
        }

        reader.close();

        return result.toString();
    }

    private static String extractError(
            String raw
    ){

        try{

            JSONObject object =
                    new JSONObject(raw);

            JSONObject error =
                    object.optJSONObject(
                            "error"
                    );

            if(error != null){

                String message =
                        error.optString(
                                "message",
                                ""
                        );

                if(!message.isEmpty())
                    return message;
            }

        }catch(Exception ignored){}

        if(raw == null ||
                raw.trim().isEmpty())
            return "Unknown API error";

        return raw.length() > 300
                ? raw.substring(0,300)
                : raw;
    }

    private static String extractText(
            JSONObject response
    ){

        try{

            JSONArray output =
                    response.optJSONArray(
                            "output"
                    );

            if(output != null){

                for(int i=0;
                    i<output.length();
                    i++){

                    JSONObject item =
                            output.optJSONObject(i);

                    if(item == null) continue;

                    JSONArray content =
                            item.optJSONArray(
                                    "content"
                            );

                    if(content == null)
                        continue;

                    for(int j=0;
                        j<content.length();
                        j++){

                        JSONObject part =
                                content.optJSONObject(j);

                        if(part == null)
                            continue;

                        if(
                                "output_text".equals(
                                        part.optString("type")
                                )
                        ){

                            return part.optString(
                                    "text",
                                    ""
                            );
                        }
                    }
                }
            }

        }catch(Exception ignored){}

        return
                "{\"say\":\"Response नहीं मिला\",\"memory\":[],\"actions\":[]}";
    }

    private static void runActions(
            Context context,
            JSONArray actions
    ){

        Handler handler =
                new Handler(
                        Looper.getMainLooper()
                );

        for(int i=0;
            i<actions.length();
            i++){

            final JSONObject action =
                    actions.optJSONObject(i);

            final long delay =
                    i * 900L;

            handler.postDelayed(
                    () -> {

                        if(action != null)
                            executeAction(
                                    context,
                                    action
                            );

                    },
                    delay
            );
        }
    }

    private static void executeAction(
            Context context,
            JSONObject action
    ){

        try{

            String type =
                    action.optString(
                            "type"
                    );

            MayaAccessibilityService service =
                    MayaAccessibilityService
                            .getInstance();

            if(
                    "open_app".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "open " +
                                action.optString(
                                        "name"
                                )
                );

            }else if(
                    "tap".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "tap " +
                                action.optString(
                                        "text"
                                )
                );

            }else if(
                    "type".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "type " +
                                action.optString(
                                        "text"
                                )
                );

            }else if(
                    "scroll".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "scroll " +
                                action.optString(
                                        "direction"
                                )
                );

            }else if(
                    "back".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "back"
                );

            }else if(
                    "home".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "home"
                );

            }else if(
                    "recent".equals(type) &&
                    service != null
            ){

                service.executeCommand(
                        "recent"
                );

            }else if(
                    "web_search".equals(type)
            ){

                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.google.com/search?q=" +
                                                Uri.encode(
                                                        action.optString(
                                                                "query"
                                                        )
                                                )
                                )
                        );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                context.startActivity(
                        intent
                );
            }

        }catch(Exception ignored){}
    }

    private static void savePending(
            Context context,
            JSONArray actions
    ){

        context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        PENDING,
                        actions.toString()
                )
                .apply();
    }

    public static boolean hasPending(
            Context context
    ){

        return !context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .getString(
                        PENDING,
                        ""
                )
                .isEmpty();
    }

    public static void confirmPending(
            Context context
    ){

        String raw =
                context
                        .getSharedPreferences(
                                PREFS,
                                Context.MODE_PRIVATE
                        )
                        .getString(
                                PENDING,
                                ""
                        );

        if(raw.isEmpty()){

            reply(
                    context,
                    "कोई pending action नहीं है."
            );

            return;
        }

        context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .remove(PENDING)
                .apply();

        try{

            runActions(
                    context,
                    new JSONArray(raw)
            );

            reply(
                    context,
                    "Confirmed. Action शुरू कर रही हूँ."
            );

        }catch(Exception e){

            reply(
                    context,
                    "Pending action पढ़ नहीं पाई."
            );
        }
    }

    public static void cancelPending(
            Context context
    ){

        context
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .edit()
                .remove(PENDING)
                .apply();

        reply(
                context,
                "ठीक है, action cancel कर दिया."
        );
    }

    private static void reply(
            Context context,
            String text
    ){

        MayaMemory.add(
                context,
                "assistant",
                text
        );

        Intent intent =
                new Intent(
                        "com.veer.maya.REPLY"
                );

        intent.setPackage(
                context.getPackageName()
        );

        intent.putExtra(
                "text",
                text
        );

        context.sendBroadcast(
                intent
        );

        MayaVoiceService.say(text);
    }
}
