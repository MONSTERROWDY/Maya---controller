package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, chat;
    EditText input, key;
    TextView status;
    BroadcastReceiver receiver;

    @Override protected void onCreate(Bundle b){super.onCreate(b);build();
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},40);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},41);
    }

    private void build(){
        ScrollView sv=new ScrollView(this); root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(28,28,28,28);sv.addView(root);setContentView(sv);
        TextView title=t("MAYA",32);root.addView(title);root.addView(t("JARVIS-style Android AI Assistant",16));
        status=t("Status: checking...",14);root.addView(status);

        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        Button acc=btn("Accessibility");Button voice=btn("Start Voice");row.addView(acc,new LinearLayout.LayoutParams(0,60,1));row.addView(voice,new LinearLayout.LayoutParams(0,60,1));root.addView(row);
        acc.setOnClickListener(v->{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));});
        voice.setOnClickListener(v->{if(MayaVoiceService.me==null){MayaVoiceService.start(this);voice.setText("Voice ON");}else{MayaVoiceService.stop(this);voice.setText("Start Voice");}});

        root.addView(t("OpenAI API Key",18));
        key=new EditText(this);key.setHint("sk-...");key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);key.setText(MayaCore.getKey(this));root.addView(key);
        Button save=btn("Save API Key");root.addView(save);save.setOnClickListener(v->{MayaCore.saveKey(this,key.getText().toString());toast("API key saved");});

        root.addView(t("Chat",18));chat=new LinearLayout(this);chat.setOrientation(LinearLayout.VERTICAL);root.addView(chat);
        input=new EditText(this);input.setHint("MAYA, open YouTube / search... / पूछो कुछ भी");root.addView(input);
        Button send=btn("Send");root.addView(send);send.setOnClickListener(v->send());
        Button confirm=btn("CONFIRM pending action");root.addView(confirm);confirm.setOnClickListener(v->MayaCore.confirmPending(this));
        Button cancel=btn("CANCEL pending action");root.addView(cancel);cancel.setOnClickListener(v->MayaCore.cancelPending(this));
        Button memory=btn("Clear Memory");root.addView(memory);memory.setOnClickListener(v->{MayaMemory.clear(this);toast("Memory cleared");});
        add("MAYA ready. API key + Accessibility + microphone enable करें.",false);
        refresh();
    }

    private void send(){String x=input.getText().toString().trim();if(x.isEmpty())return;add(x,true);input.setText("");MayaCore.process(this,x);}
    private void add(String x,boolean user){TextView v=t((user?"YOU: ":"MAYA: ")+x,15);v.setPadding(0,12,0,12);chat.addView(v);}
    private TextView t(String x,int s){TextView v=new TextView(this);v.setText(x);v.setTextSize(s);v.setTextColor(0xFFEFEFEF);return v;}
    private Button btn(String x){Button b=new Button(this);b.setText(x);return b;}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}

    private void refresh(){boolean a=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)!=null && Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).toLowerCase(Locale.US).contains(getPackageName().toLowerCase(Locale.US));status.setText("Accessibility: "+(a?"ON":"OFF")+" | Voice: "+(MayaVoiceService.me!=null?"ON":"OFF")+" | API: "+(!MayaCore.getKey(this).isEmpty()?"SAVED":"NOT SAVED"));}

    @Override protected void onResume(){super.onResume();refresh();}
    @Override protected void onDestroy(){super.onDestroy();}
}
