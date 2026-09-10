package com.veer.maya;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.rgb(7, 9, 12));

        TextView title = new TextView(this);
        title.setText("MAYA CONTROLLER");
        title.setTextColor(Color.rgb(252, 213, 53));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView info = new TextView(this);
        info.setText(
            "\nMAYA Phone Control\n\n" +
            "Accessibility permission ON karo.\n" +
            "Iske baad MAYA Android screen par actions perform kar sakti hai."
        );
        info.setTextColor(Color.WHITE);
        info.setTextSize(17);
        info.setGravity(Gravity.CENTER);

        Button button = new Button(this);
        button.setText("OPEN ACCESSIBILITY SETTINGS");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        layout.addView(title);
        layout.addView(info);
        layout.addView(button);

        setContentView(layout);
    }
}
