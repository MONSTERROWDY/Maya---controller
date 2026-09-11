package com.veer.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MayaBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        String action =
                intent.getAction();

        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) &&
                !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        boolean enabled =
                context.getSharedPreferences(
                        "maya_settings",
                        Context.MODE_PRIVATE
                ).getBoolean(
                        "maya_enabled",
                        false
                );

        if (!enabled) {
            return;
        }

        /*
         * Android versions with strict background
         * microphone restrictions may refuse a microphone
         * foreground service directly from boot.
         *
         * We remember the ON state here.
         * The app can start the voice service when Android
         * allows the user-initiated foreground transition.
         */

        if (Build.VERSION.SDK_INT < 26) {

            Intent service =
                    new Intent(
                            context,
                            MayaVoiceService.class
                    );

            context.startService(
                    service
            );
        }
    }
}
