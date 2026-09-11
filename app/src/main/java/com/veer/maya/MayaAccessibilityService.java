package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

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
        if (instance == this) {
            instance = null;
        }
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

    public boolean quickSettings() {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
    }

    public boolean powerDialog() {
        return performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
    }

    public boolean typeText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        AccessibilityNodeInfo target = findEditable(root);

        if (target == null) {
            return false;
        }

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

    public boolean clickByText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null || text == null || text.trim().isEmpty()) {
            return false;
        }

        AccessibilityNodeInfo node =
                findNodeByText(root, text.trim());

        if (node == null) {
            return false;
        }

        if (node.isClickable()) {
            return node.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
            );
        }

        AccessibilityNodeInfo parent = node.getParent();

        if (parent != null && parent.isClickable()) {
            return parent.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
            );
        }

        return node.performAction(
                AccessibilityNodeInfo.ACTION_CLICK
        );
    }

    public boolean longClickByText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null || text == null) {
            return false;
        }

        AccessibilityNodeInfo node =
                findNodeByText(root, text.trim());

        if (node == null) {
            return false;
        }

        return node.performAction(
                AccessibilityNodeInfo.ACTION_LONG_CLICK
        );
    }

    public boolean scrollForward() {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        AccessibilityNodeInfo scrollable =
                findScrollable(root);

        if (scrollable == null) {
            return false;
        }

        return scrollable.performAction(
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        );
    }

    public boolean scrollBackward() {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        AccessibilityNodeInfo scrollable =
                findScrollable(root);

        if (scrollable == null) {
            return false;
        }

        return scrollable.performAction(
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        );
    }

    public boolean tap(float x, float y) {

        Path path = new Path();

        path.moveTo(x, y);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        100
                                )
                        )
                        .build();

        return dispatchGesture(
                gesture,
                null,
                null
        );
    }

    private AccessibilityNodeInfo findEditable(
            AccessibilityNodeInfo node
    ) {

        if (node == null) {
            return null;
        }

        if (node.isEditable()) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {

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

    private AccessibilityNodeInfo findNodeByText(
            AccessibilityNodeInfo node,
            String wanted
    ) {

        if (node == null) {
            return null;
        }

        String target =
                wanted.toLowerCase(Locale.US);

        CharSequence text =
                node.getText();

        CharSequence description =
                node.getContentDescription();

        if (text != null &&
                text.toString()
                        .toLowerCase(Locale.US)
                        .contains(target)) {

            return node;
        }

        if (description != null &&
                description.toString()
                        .toLowerCase(Locale.US)
                        .contains(target)) {

            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            AccessibilityNodeInfo result =
                    findNodeByText(
                            child,
                            wanted
                    );

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private AccessibilityNodeInfo findScrollable(
            AccessibilityNodeInfo node
    ) {

        if (node == null) {
            return null;
        }

        if (node.isScrollable()) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            AccessibilityNodeInfo result =
                    findScrollable(child);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public void executeCommand(String command) {

        if (command == null) {
            return;
        }

        String c =
                command.toLowerCase(Locale.US);

        if (c.contains("home")) {
            home();

        } else if (c.contains("back")) {
            back();

        } else if (c.contains("recent")) {
            recent();

        } else if (c.contains("notification")) {
            notifications();

        } else if (c.contains("quick setting")) {
            quickSettings();

        } else if (c.contains("scroll down")) {
            scrollForward();

        } else if (c.contains("scroll up")) {
            scrollBackward();
        }
    }
}
