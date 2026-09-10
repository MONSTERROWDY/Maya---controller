package com.veer.maya;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, content, chat;
    EditText input, key;
    TextView status, modeTitle;
    BroadcastReceiver receiver;
    int gold=Color.rgb(240,185,11), bg=Color.rgb(7,9,12), panel=Color.rgb(17,21,28), text=Color.rgb(239,239,239), muted=Color.rgb(155,160,170);

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        build();
        receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
            if("com.veer.maya.REPLY".equals(i.getAction())){
                String x=i.getStringExtra("text"); if(x!=null) add(x,false);
                refresh();
            }
        }};
        registerReceiver(receiver,new IntentFilter("com.veer.maya.REPLY"),RECEIVER_NOT_EXPORTED);
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},40);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},41);
    }

    private void build(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        setContentView(root);
        LinearLayout top=new LinearLayout(this); top.setPadding(24,24,24,14); top.setOrientation(LinearLayout.VERTICAL);
        TextView brand=t("MAYA",30,gold,true); top.addView(brand);
        TextView sub=t("PERSONAL AI • PHONE AGENT",11,muted,false); top.addView(sub);
        status=t("● INITIALIZING",11,muted,false); status.setPadding(0,8,0,0); top.addView(status);
        root.addView(top);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(18,8,18,8);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=new LinearLayout(this); nav.setPadding(10,8,10,10); nav.setBackgroundColor(panel);
        Button home=navBtn("HOME"), chatBtn=navBtn("CHAT"), live=navBtn("LIVE"), settings=navBtn("SETTINGS");
        nav.addView(home,new LinearLayout.LayoutParams(0,58,1));nav.addView(chatBtn,new LinearLayout.LayoutParams(0,58,1));
        nav.addView(live,new LinearLayout.LayoutParams(0,58,1));nav.addView(settings,new LinearLayout.LayoutParams(0,58,1));root.addView(nav);
        home.setOnClickListener(v->showHome()); chatBtn.setOnClickListener(v->showChat()); live.setOnClickListener(v->showLive()); settings.setOnClickListener(v->showSettings());
        showHome();
    }

    private void clear(){content.removeAllViews();}
    private void showHome(){
        clear();
        TextView orb=t("◉",86,gold,true); orb.setGravity(Gravity.CENTER); content.addView(orb,new LinearLayout.LayoutParams(-1,170));
        TextView h=t("What can I do for you?",24,text,true);h.setGravity(Gravity.CENTER);content.addView(h);
        TextView p=t("Talk naturally. MAYA can plan tasks, control the visible phone UI, open apps and help with messages, email, web and media workflows.",14,muted,false);p.setGravity(Gravity.CENTER);p.setPadding(18,10,18,25);content.addView(p);
        Button b=big("START LIVE");b.setOnClickListener(v->startLive());content.addView(b);
        Button c=big("OPEN CHAT");c.setOnClickListener(v->showChat());content.addView(c);
        refresh();
    }

    private void showChat(){
        clear();
        TextView h=t("CHAT",22,text,true);content.addView(h);
        ScrollView sv=new ScrollView(this); chat=new LinearLayout(this);chat.setOrientation(LinearLayout.VERTICAL);chat.setPadding(4,12,4,12);sv.addView(chat);
        content.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout row=new LinearLayout(this); input=new EditText(this); input.setHint("Message MAYA…");input.setTextColor(text);input.setHintTextColor(muted);input.setBackgroundColor(panel);input.setPadding(16,12,16,12);
        row.addView(input,new LinearLayout.LayoutParams(0,60,1));Button send=navBtn("SEND");row.addView(send,new LinearLayout.LayoutParams(92,60));content.addView(row);
        send.setOnClickListener(v->send()); input.setOnEditorActionListener((v,a,e)->{send();return true;});
        add("Ready. Tell me what you want to do.",false);refresh();
    }

    private void showLive(){
        clear();
        modeTitle=t("LIVE MODE",24,text,true);modeTitle.setGravity(Gravity.CENTER);content.addView(modeTitle);
        TextView orb=t("◉",110,gold,true);orb.setGravity(Gravity.CENTER);content.addView(orb,new LinearLayout.LayoutParams(-1,220));
        TextView info=t("Continuous conversation - Hindi / English / Maithili / Bhojpuri / Hinglish",15,muted,false);info.setGravity(Gravity.CENTER);content.addView(info);
        Button start=big(MayaVoiceService.me==null?"START LIVE VOICE":"STOP LIVE VOICE");content.addView(start);
        start.setOnClickListener(v->{if(MayaVoiceService.me==null)startLive();else{MayaVoiceService.stop(this);showLive();}});
        Button wake=big(MayaVoiceService.isLiveMode(this)?"LIVE MODE ON":"WAKE WORD MODE");content.addView(wake);
        wake.setOnClickListener(v->{MayaVoiceService.setLiveMode(this,!MayaVoiceService.isLiveMode(this));showLive();});
        refresh();
    }

    private void showSettings(){
        clear();
        TextView h=t("SETTINGS",22,text,true);content.addView(h);
        TextView k=t("OpenAI API key",14,muted,false);content.addView(k);
        key=new EditText(this);key.setText(MayaCore.getKey(this));key.setTextColor(text);key.setHint("Saved locally on this phone");key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);content.addView(key);
        Button save=big("SAVE API KEY");content.addView(save);save.setOnClickListener(v->{MayaCore.saveKey(this,key.getText().toString());toast("API key saved locally");refresh();});
        Button acc=big("ACCESSIBILITY SETTINGS");content.addView(acc);acc.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        Button clearMem=big("CLEAR MEMORY");content.addView(clearMem);clearMem.setOnClickListener(v->{MayaMemory.clear(this);toast("Memory cleared");});
        TextView note=t("MAYA uses Android Accessibility for visible-screen control. Android security restrictions still apply.",13,muted,false);note.setPadding(0,20,0,0);content.addView(note);
        refresh();
    }

    private void startLive(){if(MayaVoiceService.me==null)MayaVoiceService.start(this);MayaVoiceService.setLiveMode(this,true);showLive();}
    private void send(){if(input==null)return;String x=input.getText().toString().trim();if(x.isEmpty())return;add(x,true);input.setText("");MayaCore.process(this,x);}
    private void add(String x,boolean user){if(chat==null)return;TextView v=t((user?"YOU":"MAYA")+"\n"+x,15,user?text:gold,false);v.setPadding(16,14,16,14);v.setBackgroundColor(panel);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,6,0,6);chat.addView(v,lp);}
    private TextView t(String x,int s,int c,boolean bold){TextView v=new TextView(this);v.setText(x);v.setTextSize(s);v.setTextColor(c);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private Button navBtn(String x){Button b=new Button(this);b.setText(x);b.setTextSize(11);b.setTextColor(text);return b;}
    private Button big(String x){Button b=navBtn(x);b.setTextSize(12);b.setBackgroundColor(panel);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,58);lp.setMargins(0,8,0,8);b.setLayoutParams(lp);return b;}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}
    private void refresh(){boolean a=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)!=null && Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).toLowerCase(Locale.US).contains(getPackageName().toLowerCase(Locale.US));if(status!=null)status.setText("● ACCESS "+(a?"ON":"OFF")+"  •  VOICE "+(MayaVoiceService.me!=null?"ON":"OFF")+"  •  API "+(!MayaCore.getKey(this).isEmpty()?"READY":"SETUP"));}
    @Override protected void onResume(){super.onResume();refresh();}
    @Override protected void onDestroy(){try{unregisterReceiver(receiver);}catch(Exception ignored){}super.onDestroy();}
}
