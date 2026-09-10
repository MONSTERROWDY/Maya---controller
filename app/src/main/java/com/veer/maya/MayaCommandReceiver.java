package com.veer.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MayaCommandReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.veer.maya.ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!ACTION.equals(intent.getAction())) {
            return;
        }

        String command = intent.getStringExtra("command");

        if (command == null || command.trim().isEmpty()) {
            return;
        }

        MayaAccessibilityService service =
                MayaAccessibilityService.getInstance();

        if (service != null) {
            service.executeCommand(command);
        }
    }
}
