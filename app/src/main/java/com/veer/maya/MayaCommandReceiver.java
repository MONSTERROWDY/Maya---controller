package com.veer.maya;
import android.content.*;import android.widget.Toast;
public class MayaCommandReceiver extends BroadcastReceiver{public static final String ACTION="com.veer.maya.ACTION";static final String PREFS="maya_controller",PENDING="pending_command";
@Override public void onReceive(Context c,Intent i){if(ACTION.equals(i.getAction())){String cmd=i.getStringExtra("command");if(cmd==null||cmd.trim().isEmpty())return;MayaAccessibilityService s=MayaAccessibilityService.getInstance();if(s!=null)s.executeCommand(cmd.trim());else c.getSharedPreferences(PREFS,0).edit().putString(PENDING,cmd.trim()).apply();Toast.makeText(c,"MAYA: "+cmd,Toast.LENGTH_SHORT).show();}}
public static String takePendingCommand(Context c){SharedPreferences p=c.getSharedPreferences(PREFS,0);String x=p.getString(PENDING,null);if(x!=null)p.edit().remove(PENDING).apply();return x;}}
