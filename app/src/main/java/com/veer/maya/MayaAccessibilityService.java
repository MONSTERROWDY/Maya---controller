package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MayaAccessibilityService extends AccessibilityService {

    private static MayaAccessibilityService instance;

    public static MayaAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        instance = this;

        Toast.makeText(
                this,
                "MAYA Controller CONNECTED",
                Toast.LENGTH_SHORT
        ).show();

        String pending =
                MayaCommandReceiver.takePendingCommand(this);

        if (pending != null && !pending.isEmpty()) {

            final String command = pending;

            new Handler().postDelayed(
                    () -> executeCommand(command),
                    500
            );
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // MAYA screen control is handled here.
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {

        instance = null;

        return super.onUnbind(intent);
    }

    public void executeCommand(String rawCommand) {

        if (rawCommand == null) {
            return;
        }

        String command = rawCommand.trim();

        if (command.isEmpty()) {
            return;
        }

        String lower =
                command.toLowerCase(Locale.US);

        if (
                lower.equals("home") ||
                lower.contains("go home") ||
                lower.contains("होम")
        ) {

            performGlobalAction(
                    GLOBAL_ACTION_HOME
            );

            return;
        }

        if (
                lower.equals("back") ||
                lower.contains("go back") ||
                lower.contains("वापस")
        ) {

            performGlobalAction(
                    GLOBAL_ACTION_BACK
            );

            return;
        }

        if (
                lower.equals("recent") ||
                lower.equals("recents") ||
                lower.contains("recent apps")
        ) {

            performGlobalAction(
                    GLOBAL_ACTION_RECENTS
            );

            return;
        }

        if (lower.startsWith("open ")) {

            String appName =
                    command.substring(5).trim();

            openApp(appName);

            return;
        }

        if (lower.startsWith("launch ")) {

            String appName =
                    command.substring(7).trim();

            openApp(appName);

            return;
        }

        if (lower.startsWith("tap ")) {

            String text =
                    command.substring(4).trim();

            clickText(text);

            return;
        }

        if (lower.startsWith("click ")) {

            String text =
                    command.substring(6).trim();

            clickText(text);

            return;
        }

        if (lower.startsWith("type ")) {

            String text =
                    command.substring(5);

            typeText(text);

            return;
        }

        if (lower.startsWith("scroll down")) {

            scroll(false);

            return;
        }

        if (lower.startsWith("scroll up")) {

            scroll(true);
        }
    }

    private void openApp(String appName) {

        String packageName =
                getPackageNameForApp(appName);

        if (packageName == null) {
            return;
        }

        try {

            Intent launchIntent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (launchIntent != null) {

                launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(launchIntent);
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open " + appName,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String getPackageNameForApp(String name) {

        String n =
                name.toLowerCase(Locale.US).trim();

        if (n.contains("youtube")) {
            return "com.google.android.youtube";
        }

        if (n.contains("instagram")) {
            return "com.instagram.android";
        }

        if (n.contains("facebook")) {
            return "com.facebook.katana";
        }

        if (n.contains("whatsapp")) {
            return "com.whatsapp";
        }

        if (n.contains("telegram")) {
            return "org.telegram.messenger";
        }

        if (n.contains("chrome")) {
            return "com.android.chrome";
        }

        if (n.contains("gmail")) {
            return "com.google.android.gm";
        }

        if (n.contains("settings")) {
            return "com.android.settings";
        }

        if (
                n.contains("play store") ||
                n.contains("playstore")
        ) {
            return "com.android.vending";
        }

        return null;
    }

    private void clickText(String target) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return;
        }

        List<AccessibilityNodeInfo> nodes =
                root.findAccessibilityNodeInfosByText(
                        target
                );

        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (AccessibilityNodeInfo node : nodes) {

            if (node == null) {
                continue;
            }

            if (node.isClickable()) {

                node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );

                return;
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            if (
                    parent != null &&
                    parent.isClickable()
            ) {

                parent.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );

                return;
            }
        }
    }

    private void typeText(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return;
        }

        AccessibilityNodeInfo focused =
                root.findFocus(
                        AccessibilityNodeInfo.FOCUS_INPUT
                );

        if (focused == null) {
            return;
        }

        Bundle args = new Bundle();

        args.putCharSequence(
                AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        focused.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
        );
    }

    private void scroll(boolean up) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return;
        }

        performScroll(root, up);
    }

    private boolean performScroll(
            AccessibilityNodeInfo node,
            boolean up
    ) {

        if (node.isScrollable()) {

            int action =
                    up
                    ? AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    : AccessibilityNodeInfo.ACTION_SCROLL_FORWARD;

            return node.performAction(action);
        }

        for (
                int i = 0;
                i < node.getChildCount();
                i++
        ) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (
                    child != null &&
                    performScroll(child, up)
            ) {
                return true;
            }
        }

        return false;
    }
}
