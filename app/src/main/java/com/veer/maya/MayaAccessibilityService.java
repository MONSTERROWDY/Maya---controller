package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.PixelFormat;

import android.os.Bundle;
import android.os.Handler;

import android.view.Gravity;
import android.view.WindowManager;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MayaAccessibilityService
        extends AccessibilityService {

    private static MayaAccessibilityService instance;

    private WindowManager windowManager;
    private TextView bubble;

    public static MayaAccessibilityService getInstance(){
        return instance;
    }

    @Override
    protected void onServiceConnected(){

        super.onServiceConnected();

        instance = this;

        try{

            AccessibilityServiceInfo info =
                    getServiceInfo();

            if(info != null){

                info.flags |=
                        AccessibilityServiceInfo
                                .FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

                setServiceInfo(info);
            }

        }catch(Exception ignored){}

        showBubble();

        Toast.makeText(
                this,
                "MAYA Controller connected",
                Toast.LENGTH_SHORT
        ).show();

        String pending =
                MayaCommandReceiver
                        .takePendingCommand(this);

        if(pending != null &&
                !pending.isEmpty()){

            new Handler().postDelayed(
                    () -> executeCommand(pending),
                    500
            );
        }
    }

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event
    ){}

    @Override
    public void onInterrupt(){}

    @Override
    public boolean onUnbind(Intent intent){

        hideBubble();

        instance = null;

        return super.onUnbind(intent);
    }

    private void showBubble(){

        if(bubble != null) return;

        try{

            windowManager =
                    (WindowManager)
                            getSystemService(
                                    WINDOW_SERVICE
                            );

            bubble =
                    new TextView(this);

            bubble.setText("M");
            bubble.setTextColor(Color.WHITE);
            bubble.setTextSize(16);
            bubble.setGravity(Gravity.CENTER);
            bubble.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            bubble.setBackgroundColor(
                    Color.rgb(35, 38, 48)
            );

            bubble.setPadding(
                    28,18,28,18
            );

            bubble.setOnClickListener(
                    v -> {

                        Intent i =
                                new Intent(
                                        this,
                                        MainActivity.class
                                );

                        i.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );

                        startActivity(i);
                    }
            );

            WindowManager.LayoutParams lp =
                    new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                    );

            lp.gravity =
                    Gravity.TOP | Gravity.END;

            lp.x = 18;
            lp.y = 110;

            windowManager.addView(
                    bubble,
                    lp
            );

        }catch(Exception ignored){}
    }

    private void hideBubble(){

        try{

            if(windowManager != null &&
                    bubble != null){

                windowManager.removeView(
                        bubble
                );
            }

        }catch(Exception ignored){}

        bubble = null;
    }

    public void executeCommand(String raw){

        if(raw == null) return;

        String command = raw.trim();

        if(command.isEmpty()) return;

        String lower =
                command.toLowerCase(Locale.US);

        if(lower.equals("home") ||
                lower.contains("go home") ||
                lower.contains("होम")){

            performGlobalAction(
                    GLOBAL_ACTION_HOME
            );

            return;
        }

        if(lower.equals("back") ||
                lower.contains("go back") ||
                lower.contains("वापस")){

            performGlobalAction(
                    GLOBAL_ACTION_BACK
            );

            return;
        }

        if(lower.equals("recent") ||
                lower.equals("recents") ||
                lower.contains("recent apps")){

            performGlobalAction(
                    GLOBAL_ACTION_RECENTS
            );

            return;
        }

        if(lower.equals("notifications")){

            performGlobalAction(
                    GLOBAL_ACTION_NOTIFICATIONS
            );

            return;
        }

        if(lower.startsWith("open ")){

            openApp(
                    command.substring(5).trim()
            );

            return;
        }

        if(lower.startsWith("launch ")){

            openApp(
                    command.substring(7).trim()
            );

            return;
        }

        if(lower.startsWith("tap ")){

            clickText(
                    command.substring(4).trim()
            );

            return;
        }

        if(lower.startsWith("click ")){

            clickText(
                    command.substring(6).trim()
            );

            return;
        }

        if(lower.startsWith("type ")){

            typeText(
                    command.substring(5)
            );

            return;
        }

        if(lower.startsWith("scroll down")){

            scroll(false);

            return;
        }

        if(lower.startsWith("scroll up")){

            scroll(true);
        }
    }

    private void openApp(String name){

        String packageName =
                packageFor(name);

        if(packageName == null){

            Toast.makeText(
                    this,
                    "App mapping not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try{

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if(intent != null){

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(intent);

            }else{

                Toast.makeText(
                        this,
                        "App not installed",
                        Toast.LENGTH_SHORT
                ).show();
            }

        }catch(Exception e){

            Toast.makeText(
                    this,
                    "Unable to open app",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String packageFor(String name){

        String n =
                name.toLowerCase(Locale.US)
                        .trim();

        if(n.contains("youtube"))
            return "com.google.android.youtube";

        if(n.contains("instagram"))
            return "com.instagram.android";

        if(n.contains("facebook"))
            return "com.facebook.katana";

        if(n.contains("whatsapp"))
            return "com.whatsapp";

        if(n.contains("telegram"))
            return "org.telegram.messenger";

        if(n.contains("chrome"))
            return "com.android.chrome";

        if(n.contains("gmail"))
            return "com.google.android.gm";

        if(n.contains("canva"))
            return "com.canva.editor";

        if(n.contains("settings"))
            return "com.android.settings";

        if(n.contains("play store") ||
                n.contains("playstore"))
            return "com.android.vending";

        return null;
    }

    private void clickText(String target){

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root == null) return;

        List<AccessibilityNodeInfo> nodes =
                root.findAccessibilityNodeInfosByText(
                        target
                );

        if(nodes == null) return;

        for(AccessibilityNodeInfo node : nodes){

            if(node == null) continue;

            if(node.isClickable()){

                if(node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                )) return;
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            if(parent != null &&
                    parent.isClickable()){

                if(parent.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                )) return;
            }
        }
    }

    private void typeText(String text){

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root == null) return;

        AccessibilityNodeInfo field =
                root.findFocus(
                        AccessibilityNodeInfo.FOCUS_INPUT
                );

        if(field == null) return;

        Bundle bundle =
                new Bundle();

        bundle.putCharSequence(
                AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        field.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                bundle
        );
    }

    private void scroll(boolean up){

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root != null){

            performScroll(root, up);
        }
    }

    private boolean performScroll(
            AccessibilityNodeInfo node,
            boolean up
    ){

        if(node.isScrollable()){

            return node.performAction(
                    up
                            ? AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                            : AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            );
        }

        for(int i=0;i<node.getChildCount();i++){

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if(child != null &&
                    performScroll(child,up)){

                return true;
            }
        }

        return false;
    }
}
