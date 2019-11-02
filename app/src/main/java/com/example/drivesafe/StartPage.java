package com.example.drivesafe;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class StartPage extends AppCompatActivity {

    Button button2,button1;
    private NotificationManager mnotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        button2 = findViewById(R.id.button2);
        button1=findViewById(R.id.r);
        button1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O_MR1)
            @Override
            public void onClick(View view) {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        });
        mnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        button2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O_MR1)
            @Override
            public void onClick(View view) {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void changeInterruptionFiler(int interruptionFilterNone) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(mnotificationManager.isNotificationPolicyAccessGranted()){
                mnotificationManager.setInterruptionFilter(interruptionFilterNone);
            }else {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }

        }

    }
}
