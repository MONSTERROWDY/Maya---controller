package com.veer.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MayaCommandReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.veer.maya.ACTION";
    public static final String REPLY = "com.veer.maya.REPLY";
    public static final String CONFIRM = "com.veer.maya.CONFIRM";
    public static final String CANCEL = "com.veer.maya.CANCEL";
    private static final String PREFS = "maya_controller";
    private static final String PENDING_COMMAND = "pending_command";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String a = intent.getAction();
        if (ACTION.equals(a)) {
            String command = intent.getStringExtra("command");
            if (command == null || command.trim().isEmpty()) return;
            MayaAccessibilityService s = MayaAccessibilityService.getInstance();
            if (s != null) s.executeCommand(command.trim());
            else context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(PENDING_COMMAND, command.trim()).apply();
        } else if (CONFIRM.equals(a)) {
            MayaCore.confirmPending(context);
        } else if (CANCEL.equals(a)) {
            MayaCore.cancelPending(context);
        }
    }

    public static String takePendingCommand(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String x = p.getString(PENDING_COMMAND, null);
        if (x != null) p.edit().remove(PENDING_COMMAND).apply();
        return x;
    }
}
