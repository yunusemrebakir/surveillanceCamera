package com.yunusemrebakir.SurveillanceCamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver{
    MyBroadcastReceiver alarm = new MyBroadcastReceiver();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            Intent i = new Intent(context,MQTTService.class);
            context.startService(i);
            alarm.setAlarm(context);
        }
    }

}
