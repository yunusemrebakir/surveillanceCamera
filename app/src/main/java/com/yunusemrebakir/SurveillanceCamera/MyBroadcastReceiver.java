package com.yunusemrebakir.SurveillanceCamera;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

import static com.yunusemrebakir.SurveillanceCamera.Constants.BROKER_URL;
import static com.yunusemrebakir.SurveillanceCamera.Constants.IS_SERVICE_RUNNING;
import static com.yunusemrebakir.SurveillanceCamera.Constants.TOPIC;
import static com.yunusemrebakir.SurveillanceCamera.Constants.qos;


public class MyBroadcastReceiver extends BroadcastReceiver {
    ContextWrapper contextWrapper;
    SharedPreferences settings;

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                    Intent i = new Intent(context, MQTTService.class);
                    context.startService(i);
                }
            }
        }

        if (!IS_SERVICE_RUNNING && settings.getBoolean("swAutoStartService", true)) {
            Intent i = new Intent(context, MQTTService.class);
            context.startService(i);
            Log.d("Broadcast Receiver", "Service probably killed by system, RELAUNCHED");
        }
        if (MQTTService.mqttClient != null) {     //To prevent NPE on mqttClient
            if (!MQTTService.mqttClient.isConnected()) {
                Log.d("Service Status: ", "NOT_CONNECTED - trying to reconnect");
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTTService");
                wl.acquire();

                try {
                    MQTTService.mqttClient = new MqttClient(BROKER_URL, MQTTService.clientId, new MemoryPersistence());
                    MqttConnectOptions connOptions = new MqttConnectOptions();
                    connOptions.setCleanSession(false);
                    connOptions.setKeepAliveInterval(20 * 60);
                    MQTTService.mqttClient.setCallback(new PushCallback(contextWrapper));
                    MQTTService.mqttClient.connect(connOptions);
                    MQTTService.mqttClient.subscribe(TOPIC, qos);
                    Log.d("Service Status: ", "SUCCESSFULLY_CONNECTED");

                } catch (MqttException e) {
                    e.printStackTrace();
                }
                wl.release();

            } else {
                Log.d("Service Status: ", "ALREADY_CONNECTED");
                MqttMessage message = new MqttMessage();
                message.setPayload(new String("").getBytes());
                try {
                    if (isServerAvailable()) {
                        MQTTService.mqttClient.publish("yeb/general/keepAlive", message);
                        Log.d("Service Status: ", "PUBLISH_SUCCESSFUL");
                    }

                } catch (MqttPersistenceException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setAlarm(Context context) {
        Intent i = new Intent(context, MyBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000 * 60 * 15, 1000 * 60 * 15, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, MyBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }


    private boolean isServerAvailable() {
        // System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 178.233.200.35");
            int mExitValue = mIpAddrProcess.waitFor();
            // System.out.println(" mExitValue "+mExitValue);
            if (mExitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception:" + ignore);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        }
        return false;
    }


}
