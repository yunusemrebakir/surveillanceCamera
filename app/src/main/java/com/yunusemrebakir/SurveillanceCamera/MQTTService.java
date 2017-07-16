package com.yunusemrebakir.SurveillanceCamera;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static com.yunusemrebakir.SurveillanceCamera.Constants.BROKER_URL;
import static com.yunusemrebakir.SurveillanceCamera.Constants.IS_SERVICE_RUNNING;
import static com.yunusemrebakir.SurveillanceCamera.Constants.TOPIC;
import static com.yunusemrebakir.SurveillanceCamera.Constants.qos;
import static com.yunusemrebakir.SurveillanceCamera.Constants.txtIsServiceStatus;


public class MQTTService extends Service {
    public static MqttClient mqttClient;
    MyBroadcastReceiver alarm = new MyBroadcastReceiver();
    public static final String clientId = Build.SERIAL;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service Started ", "OK");

        try {
            mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
            MqttConnectOptions connOptions = new MqttConnectOptions();
            connOptions.setCleanSession(false);
            connOptions.setKeepAliveInterval(20 * 60);
            mqttClient.setCallback(new PushCallback(this));
            mqttClient.connect(connOptions);
            mqttClient.subscribe(TOPIC, qos);

            alarm.setAlarm(this);
            Log.d("Service class ", "Alarm started");
            IS_SERVICE_RUNNING = true;

            if (txtIsServiceStatus != null) {
                txtIsServiceStatus.setText("RUNNING");
                txtIsServiceStatus.setTextColor(Color.BLUE);
            }
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        try {
            mqttClient.disconnect(0);
            IS_SERVICE_RUNNING = false;
            txtIsServiceStatus.setText("NOT RUNNING");
            txtIsServiceStatus.setTextColor(Color.RED);

        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}