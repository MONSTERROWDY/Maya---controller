package com.veer.maya;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import java.util.List;
import java.util.Locale;

public class MayaAccessibilityService extends AccessibilityService {
    private static MayaAccessibilityService instance;
    private WindowManager wm;
    private TextView bubble;

    public static MayaAccessibilityService getInstance() { return instance; }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        try {
            AccessibilityServiceInfo i = getServiceInfo();
            if (i != null) {
                i.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
                setServiceInfo(i);
            }
        } catch (Exception ignored) {}
        showBubble();
        Toast.makeText(this, "MAYA Controller CONNECTED", Toast.LENGTH_SHORT).show();
        String pending = MayaCommandReceiver.takePendingCommand(this);
        if (pending != null && !pending.isEmpty()) {
            final String p = pending;
            new Handler().postDelayed(() -> executeCommand(p), 400);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}

    @Override public boolean onUnbind(Intent intent) {
        hideBubble();
        instance = null;
        return super.onUnbind(intent);
    }

    private void showBubble() {
        if (bubble != null) return;
        try {
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            bubble = new TextView(this);
            bubble.setText("MAYA");
            bubble.setTextColor(0xFFFFFFFF);
            bubble.setTextSize(13);
            bubble.setGravity(Gravity.CENTER);
            bubble.setBackgroundColor(0xDD11151C);
            bubble.setPadding(24, 12, 24, 12);
            bubble.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.END;
            lp.x = 18; lp.y = 90;
            wm.addView(bubble, lp);
        } catch (Exception ignored) {}
    }

    private void hideBubble() {
        try { if (wm != null && bubble != null) wm.removeView(bubble); } catch (Exception ignored) {}
        bubble = null;
    }

    public void executeCommand(String raw) {
        if (raw == null) return;
        String c = raw.trim();
        if (c.isEmpty()) return;
        String l = c.toLowerCase(Locale.US);
        if (l.equals("home") || l.contains("go home") || l.contains("होम")) { performGlobalAction(GLOBAL_ACTION_HOME); return; }
        if (l.equals("back") || l.contains("go back") || l.contains("वापस")) { performGlobalAction(GLOBAL_ACTION_BACK); return; }
        if (l.equals("recent") || l.equals("recents") || l.contains("recent apps")) { performGlobalAction(GLOBAL_ACTION_RECENTS); return; }
        if (l.equals("notifications")) { performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS); return; }
        if (l.startsWith("open ")) { openApp(c.substring(5).trim()); return; }
        if (l.startsWith("launch ")) { openApp(c.substring(7).trim()); return; }
        if (l.startsWith("tap ")) { clickText(c.substring(4).trim()); return; }
        if (l.startsWith("click ")) { clickText(c.substring(6).trim()); return; }
        if (l.startsWith("type ")) { typeText(c.substring(5)); return; }
        if (l.startsWith("scroll down")) { scroll(false); return; }
        if (l.startsWith("scroll up")) { scroll(true); }
    }

    private void openApp(String name) {
        String p = packageFor(name);
        if (p == null) { Toast.makeText(this, "App mapping not found: " + name, Toast.LENGTH_SHORT).show(); return; }
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(p);
            if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
            else Toast.makeText(this, "App not installed: " + name, Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "Unable to open " + name, Toast.LENGTH_SHORT).show(); }
    }

    private String packageFor(String name) {
        String n = name.toLowerCase(Locale.US).trim();
        if (n.contains("youtube")) return "com.google.android.youtube";
        if (n.contains("instagram")) return "com.instagram.android";
        if (n.contains("facebook")) return "com.facebook.katana";
        if (n.contains("whatsapp")) return "com.whatsapp";
        if (n.contains("telegram")) return "org.telegram.messenger";
        if (n.contains("chrome")) return "com.android.chrome";
        if (n.contains("gmail")) return "com.google.android.gm";
        if (n.contains("canva")) return "com.canva.editor";
        if (n.contains("settings")) return "com.android.settings";
        if (n.contains("play store") || n.contains("playstore")) return "com.android.vending";
        return null;
    }

    private void clickText(String target) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(target);
        if (nodes == null) return;
        for (AccessibilityNodeInfo n : nodes) {
            if (n == null) continue;
            if (n.isClickable() && n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return;
            AccessibilityNodeInfo p = n.getParent();
            if (p != null && p.isClickable() && p.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return;
        }
    }

    private void typeText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo f = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (f == null) return;
        Bundle b = new Bundle();
        b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        f.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b);
    }

    private void scroll(boolean up) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) performScroll(root, up);
    }

    private boolean performScroll(AccessibilityNodeInfo n, boolean up) {
        if (n.isScrollable()) return n.performAction(up ? AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD : AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        for (int i=0;i<n.getChildCount();i++) { AccessibilityNodeInfo c=n.getChild(i); if(c!=null && performScroll(c,up)) return true; }
        return false;
    }
}
