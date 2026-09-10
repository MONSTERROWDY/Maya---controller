package com.veer.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class MayaCommandReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.veer.maya.ACTION";

    private static final String PREFS = "maya_controller";
    private static final String PENDING_COMMAND = "pending_command";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!ACTION.equals(intent.getAction())) {
            return;
        }

        String command = intent.getStringExtra("command");

        if (command == null || command.trim().isEmpty()) {
            return;
        }

        command = command.trim();

        MayaAccessibilityService service =
                MayaAccessibilityService.getInstance();

        if (service != null) {

            service.executeCommand(command);

            Toast.makeText(
                    context,
                    "MAYA: " + command,
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            context.getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
            ).edit()
            .putString(PENDING_COMMAND, command)
            .apply();

            Toast.makeText(
                    context,
                    "MAYA command saved. Waiting for Accessibility...",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public static String takePendingCommand(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        String command =
                prefs.getString(PENDING_COMMAND, null);

        if (command != null) {

            prefs.edit()
                    .remove(PENDING_COMMAND)
                    .apply();
        }

        return command;
    }
}
