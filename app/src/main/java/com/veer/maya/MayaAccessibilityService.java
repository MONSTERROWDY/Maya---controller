package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityNodeInfo;

public class MayaAccessibilityService
        extends AccessibilityService {

    static MayaAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static MayaAccessibilityService getInstance() {
        return instance;
    }

    public void executeCommand(String command) {
        if (command == null) return;

        String c = command.trim();
        String l = c.toLowerCase(java.util.Locale.ROOT);

        if (l.equals("home") || l.contains("go home") || c.contains("होम")) {
            home();
            return;
        }

        if (l.equals("back") || l.contains("go back") || c.contains("बैक")) {
            back();
            return;
        }

        if (l.equals("recent") || l.contains("recent apps") || c.contains("रीसेंट")) {
            recent();
            return;
        }

        if (l.contains("notification") || c.contains("नोटिफिकेशन")) {
            openNotifications();
            return;
        }

        if (l.startsWith("type ")) {
            typeText(c.substring(5).trim());
        }
    }

    public static boolean isReady() {
        return instance != null;
    }

    public static void home() {
        if (instance != null) {
            instance.performGlobalAction(
                GLOBAL_ACTION_HOME
            );
        }
    }

    public static void back() {
        if (instance != null) {
            instance.performGlobalAction(
                GLOBAL_ACTION_BACK
            );
        }
    }

    public static void recent() {
        if (instance != null) {
            instance.performGlobalAction(
                GLOBAL_ACTION_RECENTS
            );
        }
    }

    public static void openNotifications() {
        if (instance != null) {
            instance.performGlobalAction(
                GLOBAL_ACTION_NOTIFICATIONS
            );
        }
    }

    public static void tap(float x, float y) {

        if (instance == null) return;

        Path path = new Path();
        path.moveTo(x,y);

        GestureDescription.StrokeDescription stroke =
            new GestureDescription.StrokeDescription(
                path,
                0,
                80
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

    public static boolean typeText(String text) {

        if (instance == null) return false;

        AccessibilityNodeInfo root =
            instance.getRootInActiveWindow();

        if (root == null) return false;

        AccessibilityNodeInfo node =
            findEditable(root);

        if (node == null) return false;

        android.os.Bundle args =
            new android.os.Bundle();

        args.putCharSequence(
            AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        );

        return node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            args
        );
    }

    static AccessibilityNodeInfo findEditable(
        AccessibilityNodeInfo node
    ) {

        if (node == null) return null;

        if (node.isEditable()) {
            return node;
        }

        for (int i=0;
             i<node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                node.getChild(i);

            AccessibilityNodeInfo result =
                findEditable(child);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public void onAccessibilityEvent(
        android.view.accessibility.AccessibilityEvent event
    ) {}

    @Override
    public void onInterrupt() {}
}
