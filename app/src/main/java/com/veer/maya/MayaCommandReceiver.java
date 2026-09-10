package com.veer.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MayaCommandReceiver extends BroadcastReceiver {

    public static final String ACTION =
            "com.veer.maya.ACTION";

    public static final String REPLY =
            "com.veer.maya.REPLY";

    public static final String CONFIRM =
            "com.veer.maya.CONFIRM";

    public static final String CANCEL =
            "com.veer.maya.CANCEL";

    private static final String PREFS =
            "maya_controller";

    private static final String PENDING =
            "pending_command";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ){

        if(intent == null) return;

        String action = intent.getAction();

        if(ACTION.equals(action)){

            String command =
                    intent.getStringExtra("command");

            if(command == null ||
                    command.trim().isEmpty()) return;

            MayaAccessibilityService service =
                    MayaAccessibilityService.getInstance();

            if(service != null){

                service.executeCommand(
                        command.trim()
                );

            }else{

                context
                        .getSharedPreferences(
                                PREFS,
                                Context.MODE_PRIVATE
                        )
                        .edit()
                        .putString(
                                PENDING,
                                command.trim()
                        )
                        .apply();
            }

        }else if(CONFIRM.equals(action)){

            MayaCore.confirmPending(context);

        }else if(CANCEL.equals(action)){

            MayaCore.cancelPending(context);
        }
    }

    public static String takePendingCommand(
            Context context
    ){

        SharedPreferences p =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        String command =
                p.getString(PENDING, null);

        if(command != null){

            p.edit()
                    .remove(PENDING)
                    .apply();
        }

        return command;
    }
}
