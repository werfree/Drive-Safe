package com.example.drivesafe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class Splash extends AppCompatActivity {

    private AlertDialog  alertDialog;
    private ImageButton next;
    private NotificationManager mnotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        next=findViewById(R.id.next);
        next.setVisibility(View.INVISIBLE);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionSms();
            }
        });
        permissionSms();

    }


    private void permissionGPSCORE() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionDND();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionGPS() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionGPSCORE();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionSms(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            permissionGPS();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }

    }
    private void permissionDND() {
        NotificationManager mnotificationManager = mnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mnotificationManager.isNotificationPolicyAccessGranted()) {
            Thread thread = new Thread() {

                @Override
                public void run() {
                    try {
                        sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        startActivity(new Intent(Splash.this,MainActivity.class));
                        finish();

                    }
                }
            };
            thread.start();
        }


         else {
            alertDialog=new AlertDialog.Builder(Splash.this).create();
            alertDialog.setTitle("Do Not Disturb Access");
            alertDialog.setMessage("Please allow Do Not Disturb access,to allow the app to function properly");
            alertDialog.setMessage("Please allow Do Not Disturb access,to allow the app to function properly");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                    next.setVisibility(View.VISIBLE);

                }
            });
            alertDialog.show();

        }

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0) {
                    permissionGPS();
                } else {
                    Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_LONG).show();
                    permissionSms();
                }
                break;
            case 100:
                if (grantResults.length > 0) {
                    permissionGPSCORE();
                } else {
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_LONG).show();
                    permissionGPS();
                }
                break;
            case 1000:
                if (grantResults.length > 0) {
                    Toast.makeText(this, "Location C Permission Denied", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Location C Permission Denied", Toast.LENGTH_LONG).show();
                    permissionGPSCORE();
                }
                break;
        }
    }
}
