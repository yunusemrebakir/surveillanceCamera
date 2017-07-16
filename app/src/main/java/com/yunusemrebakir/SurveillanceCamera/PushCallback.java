package com.yunusemrebakir.SurveillanceCamera;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class PushCallback implements MqttCallback {
    SharedPreferences settings;
    private MqttClient mqttClient;
    private ContextWrapper context;

    public PushCallback(ContextWrapper context) {

        this.context = context;
    }

    @Override
    public void connectionLost(Throwable cause) {
        //We should reconnect here
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("PushCallback", "Notification Received");
        settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        Intent resultIntent = new Intent(context, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        // PendingIntent resultPendingIntent;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        android.support.v4.app.NotificationCompat.Builder motionDetected = new android.support.v4.app.NotificationCompat.Builder(context)
                .setContentTitle("Surveillance Camera")
                .setContentText(new String(message.getPayload()))
                .setSmallIcon(R.drawable.ic_launcher_web)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 1000, 1000, 1000})
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);


        // Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.custom_sound);
        //notification.sound = soundUri;
        //notification.defaults |= Notification.DEFAULT_LIGHTS;
        //notification.defaults |= Notification.DEFAULT_SOUND;
        //notification.defaults |= Notification.DEFAULT_VIBRATE;
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;

        if (settings.getBoolean("swEnableNotifications", true)) {
            nm.notify(1, motionDetected.build());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //We do not need this because we do not publish
    }

}