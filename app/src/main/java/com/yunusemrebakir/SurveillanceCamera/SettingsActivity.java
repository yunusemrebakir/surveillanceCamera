package com.yunusemrebakir.SurveillanceCamera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import static com.yunusemrebakir.SurveillanceCamera.Constants.*;

public class SettingsActivity extends AppCompatActivity {
    private EditText editStreamUrl,editStreamName;
    private Switch swAutoStartService,swEnableNotifications;
    private Button btnServiceStatus;
    SharedPreferences settings;
    MyBroadcastReceiver alarm = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        txtIsServiceStatus = (TextView) findViewById(R.id.txtIsserviceStatus);
        editStreamUrl = (EditText) findViewById(R.id.editStreamUrl);
        editStreamName = (EditText) findViewById(R.id.editStreamName);
        swAutoStartService = (Switch) findViewById(R.id.swAutoStartService);
        swEnableNotifications = (Switch) findViewById(R.id.swEnableNotifications);
        btnServiceStatus = (Button) findViewById(R.id.btnServiceStatus);

        settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        /* Load settings */
        editStreamUrl.setText(settings.getString("editStreamUrl",""));
        editStreamName.setText(settings.getString("editStreamName",""));
        swAutoStartService.setChecked(settings.getBoolean("swAutoStartService",false));
        swEnableNotifications.setChecked(settings.getBoolean("swEnableNotifications",false));

        /* First time run check, Is the app running for the first time? */
        if(settings.getBoolean("isFirstTime",true)){
            /* Yes, it is running for the first time */
            /* Set default configurations */
            swEnableNotifications.setChecked(settings.getBoolean("swEnableNotifications",true));
            swAutoStartService.setChecked(settings.getBoolean("swAutoStartService",true));
            editor.putBoolean("isFirstTime",false);
            editor.commit();
        }

        if(IS_SERVICE_RUNNING)
        {
            txtIsServiceStatus.setText("RUNNING");
            txtIsServiceStatus.setTextColor(Color.BLUE);
            btnServiceStatus.setText("Kill Service");
        }else {
            txtIsServiceStatus.setText("NOT RUNNING");
            txtIsServiceStatus.setTextColor(Color.RED);
            btnServiceStatus.setText("Start Service");
        }

        swAutoStartService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();

                if(b){
                    editor.putBoolean("swAutoStartService",true).commit();
                    if(!IS_SERVICE_RUNNING) {
                        Intent intent = new Intent(getApplicationContext(), MQTTService.class);
                        startService(intent);
                        btnServiceStatus.setText("Kill Service");
                        Toast.makeText(getApplicationContext(),"Service started!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    editor.putBoolean("swAutoStartService",false).commit();
                    Toast.makeText(getApplicationContext(),"Service autostart disabled.",Toast.LENGTH_SHORT).show();
                    alarm.cancelAlarm(getApplicationContext());
                    if(IS_SERVICE_RUNNING){
                    Toast.makeText(getApplicationContext(),"Service is running. If you want to stop the service you also need the kill the service.",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        swEnableNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                if(b){
                    editor.putBoolean("swEnableNotifications",true).commit();
                }else{
                    editor.putBoolean("swEnableNotifications",false).commit();
                }
            }
        });

        btnServiceStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IS_SERVICE_RUNNING){
                Intent intent = new Intent(getApplicationContext(),MQTTService.class);
                stopService(intent);
                btnServiceStatus.setText("Start Service");

                }else{
                    Intent intent = new Intent(getApplicationContext(),MQTTService.class);
                    startService(intent);
                    btnServiceStatus.setText("Kill Service");
                }
            }
        });

    }


    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("editStreamUrl",editStreamUrl.getText().toString());
        editor.putString("editStreamName",editStreamName.getText().toString());
        editor.commit();

        /*If stream url is empty, disable start caption button. There is no url to caption */
        if(settings.getString("editStreamUrl","").length() == 0){
            MainActivity.btnStartCaption.setEnabled(false);
        }else{
            MainActivity.btnStartCaption.setEnabled(true);
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
