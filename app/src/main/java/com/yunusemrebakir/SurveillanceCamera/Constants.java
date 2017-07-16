package com.yunusemrebakir.SurveillanceCamera;

import android.widget.TextView;

/**
 * Created by yunus on 5/7/16.
 */
public class Constants {
    public static boolean IS_SERVICE_RUNNING;
    public static boolean ENABLE_AUTOSTART;
    public static final String TAG = "Surveillance Camera";
    public static final String BROKER_URL = "tcp://178.233.200.35:1883";
    public static final String TOPIC = "yeb/apps/SurveillanceCamera";
    public static final int qos = 2;
    public static TextView txtIsServiceStatus;

}
