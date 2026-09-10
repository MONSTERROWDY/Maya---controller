package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import java.util.HashMap;
import java.util.Map;

public class MayaAccessibilityService
        extends AccessibilityService {

    public static MayaAccessibilityService instance;

    private WindowManager wm;
    private TextView bubble;

    private static final Map<String,String> APPS =
            new HashMap<>();

    static {
        APPS.put("youtube","com.google.android.youtube");
        APPS.put("यूट्यूब","com.google.android.youtube");

        APPS.put("chrome","com.android.chrome");
        APPS.put("क्रोम","com.android.chrome");

        APPS.put("whatsapp","com.whatsapp");
        APPS.put("व्हाट्सएप","com.whatsapp");

        APPS.put("telegram","org.telegram.messenger");
        APPS.put("टेलीग्राम","org.telegram.messenger");

        APPS.put("instagram","com.instagram.android");
        APPS.put("इंस्टाग्राम","com.instagram.android");

        APPS.put("facebook","com.facebook.katana");
        APPS.put("फेसबुक","com.facebook.katana");

        APPS.put("gmail","com.google.android.gm");
        APPS.put("जीमेल","com.google.android.gm");

        APPS.put("canva","com.canva.editor");
        APPS.put("कैनवा","com.canva.editor");

        APPS.put("settings","com.android.settings");
        APPS.put("सेटिंग","com.android.settings");

        APPS.put("play store","com.android.vending");
        APPS.put("play store","com.android.vending");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        instance=this;

        try {
            createBubble();
        } catch(Exception ignored) {}
    }

    private void createBubble() {

        if(bubble != null)
            return;

        wm =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        bubble =
                new TextView(this);

        bubble.setText("✦\nMAYA");
        bubble.setTextSize(10);
        bubble.setTextColor(
                android.graphics.Color.rgb(
                        80,60,20
                )
        );
        bubble.setGravity(Gravity.CENTER);
        bubble.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        );

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                android.graphics.Color.WHITE
        );
        bg.setCornerRadius(80);
        bg.setStroke(
                3,
                android.graphics.Color.rgb(
                        210,165,65
                )
        );

        bubble.setBackground(bg);

        bubble.setOnClickListener(
                v -> {
                    Intent i =
                            new Intent(
                                    this,
                                    MainActivity.class
                            );
                    i.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );
                    startActivity(i);
                }
        );

        int type;

        if(Build.VERSION.SDK_INT >= 26)
            type =
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        else
            type =
                    WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams p =
                new WindowManager.LayoutParams(
                        76,
                        76,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                );

        p.gravity =
                Gravity.RIGHT | Gravity.CENTER_VERTICAL;

        p.x=12;
        p.y=0;

        wm.addView(bubble,p);
    }

    public static void openApp(
            Context c,
            String app
    ) {

        if(instance != null) {
            instance.openAppInternal(app);
            return;
        }

        String key =
                app == null
                ? ""
                : app.toLowerCase().trim();

        String pkg=APPS.get(key);

        if(pkg==null)
            return;

        try {

            Intent i =
                    c.getPackageManager()
                            .getLaunchIntentForPackage(pkg);

            if(i != null) {
                i.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );
                c.startActivity(i);
            }

        } catch(Exception ignored) {}
    }

    private void openAppInternal(String app) {

        String key =
                app == null
                ? ""
                : app.toLowerCase().trim();

        String pkg=APPS.get(key);

        if(pkg==null) {

            for(String k:APPS.keySet()) {
                if(key.contains(k)) {
                    pkg=APPS.get(k);
                    break;
                }
            }
        }

        if(pkg==null)
            return;

        try {

            Intent i =
                    getPackageManager()
                            .getLaunchIntentForPackage(pkg);

            if(i != null) {
                i.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );
                startActivity(i);
            }

        } catch(Exception ignored) {}
    }

    public static boolean tapText(String text) {

        if(instance == null)
            return false;

        return instance.tapTextInternal(text);
    }

    private boolean tapTextInternal(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root == null)
            return false;

        AccessibilityNodeInfo n =
                findNode(root,text);

        if(n == null)
            return false;

        boolean result=false;

        try {
            if(n.isClickable())
                result=n.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );
            else {
                AccessibilityNodeInfo p=n.getParent();

                if(p != null && p.isClickable())
                    result=p.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK
                    );
            }
        } catch(Exception ignored) {}

        return result;
    }

    private AccessibilityNodeInfo findNode(
            AccessibilityNodeInfo root,
            String text
    ) {

        if(root == null)
            return null;

        CharSequence nodeText =
                root.getText();

        if(nodeText != null &&
                nodeText.toString()
                        .equalsIgnoreCase(text))
            return root;

        CharSequence desc =
                root.getContentDescription();

        if(desc != null &&
                desc.toString()
                        .equalsIgnoreCase(text))
            return root;

        for(int i=0;i<root.getChildCount();i++) {

            AccessibilityNodeInfo child =
                    root.getChild(i);

            AccessibilityNodeInfo found =
                    findNode(child,text);

            if(found != null)
                return found;
        }

        return null;
    }

    public static boolean typeText(String text) {

        if(instance == null)
            return false;

        return instance.typeTextInternal(text);
    }

    private boolean typeTextInternal(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root == null)
            return false;

        AccessibilityNodeInfo focused =
                root.findFocus(
                        AccessibilityNodeInfo.FOCUS_INPUT
                );

        if(focused == null)
            return false;

        try {

            android.os.Bundle b =
                    new android.os.Bundle();

            b.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
            );

            return focused.performAction(
                    AccessibilityNodeInfo.ACTION_SET_TEXT,
                    b
            );

        } catch(Exception ignored) {
            return false;
        }
    }

    public static boolean scroll(String direction) {

        if(instance == null)
            return false;

        return instance.scrollInternal(direction);
    }

    private boolean scrollInternal(String direction) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if(root == null)
            return false;

        boolean down =
                !"up".equalsIgnoreCase(
                        direction
                );

        AccessibilityNodeInfo n =
                findScrollable(root);

        if(n == null)
            return false;

        try {
            return n.performAction(
                    down
                    ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            );
        } catch(Exception e) {
            return false;
        }
    }

    private AccessibilityNodeInfo findScrollable(
            AccessibilityNodeInfo n
    ) {

        if(n == null)
            return null;

        if(n.isScrollable())
            return n;

        for(int i=0;i<n.getChildCount();i++) {

            AccessibilityNodeInfo f =
                    findScrollable(
                            n.getChild(i)
                    );

            if(f != null)
                return f;
        }

        return null;
    }

    public static void globalBack() {
        if(instance != null)
            instance.performGlobalAction(
                    GLOBAL_ACTION_BACK
            );
    }

    public static void globalHome() {
        if(instance != null)
            instance.performGlobalAction(
                    GLOBAL_ACTION_HOME
            );
    }

    public static void globalRecent() {
        if(instance != null)
            instance.performGlobalAction(
                    GLOBAL_ACTION_RECENTS
            );
    }

    public static void swipe(
            float x1,
            float y1,
            float x2,
            float y2,
            long duration
    ) {

        if(instance == null)
            return;

        if(Build.VERSION.SDK_INT < 24)
            return;

        Path path = new Path();
        path.moveTo(x1,y1);
        path.lineTo(x2,y2);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(
                        path,
                        0,
                        duration
                );

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(stroke)
                        .build();

        instance.dispatchGesture(
                gesture,
                null,
                null
        );
    }

    @Override
    public void onAccessibilityEvent(
            android.view.accessibility.AccessibilityEvent event
    ) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {

        try {
            if(bubble != null && wm != null)
                wm.removeView(bubble);
        } catch(Exception ignored) {}

        bubble=null;
        instance=null;

        super.onDestroy();
    }
}
