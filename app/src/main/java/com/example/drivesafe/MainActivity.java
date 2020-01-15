package com.example.drivesafe;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static android.provider.ContactsContract.ProviderStatus.STATUS;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener{

    private static final double MAX_G = 5.5;
    private static final String MSG_URL = "https://maps.google.com/?q=";

    String msg = "";
    String phnNo1, phnNo2,police;
    String name;
    boolean isDriver;
    ImageView mainButton, editButton,panicButton;
    TextView driveTextView, speedView,longtitudeView,latitudeView,unitView,sensorView;

    LocationManager locationManager;
    NotificationManager mnotificationManager;
    SensorManager sensorManager;
    Sensor accelerometer;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor meditor;

    PlacesClient placesClient;

    TextToSpeech textToSpeech;


    boolean a = false;
    private AlertDialog alertDialog;
    private static final double G = 9.8;
    private String apiKey = "AIzaSyBTOvrRB2emWAQUOEifs6y7BSKKiUL8eVc";
    private String apiKey2 = "AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8";
    boolean isDriving = false;
    double latitude = 0.0, longtitude = 0.0, curSpeed = 0.0, maxg = 0;
    int k=0,flag=0;
    private  LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        name = sharedPreferences.getString("name", "");
        phnNo1 = sharedPreferences.getString("phnNo1", "");
        phnNo2 = sharedPreferences.getString("phnNo2", "");

        if (!checkdata()) {
            startActivity(new Intent(MainActivity.this, Edit.class));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionSms();
        }
        driveTextView = findViewById(R.id.driveModeText);
        driveTextView.setText("Drive Mode: OFF");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


                speedView = findViewById(R.id.speed);
        latitudeView=findViewById(R.id.latutude);
        longtitudeView=findViewById(R.id.londtitude);
        unitView=findViewById(R.id.unit);

        mnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mainButton = findViewById(R.id.mainButton);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDriving) {
                    alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Drive Mode");
                    alertDialog.setMessage("Enable drive mode for?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Driver", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            driveMode(1);

                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Rider", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            driveMode(2);

                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                } else {
                    alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Exit Drive Mode");
                    alertDialog.setMessage("Do you want to exit from Drive Mode?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            driveMode(0);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                }
                alertDialog.show();
            }
        });
        driveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainButton.performClick();
            }
        });

        editButton = findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Edit.class));
            }
        });

        Places.initialize(getApplicationContext(), apiKey);
        placesClient = Places.createClient(this);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        panicButton = findViewById(R.id.panic);

        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Accident");
                alertDialog.setMessage("Do you want to contact the police?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accidentOccur(true);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this,"Panic Button Off",Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
                flag=0;

            }
        });

        sensorView=findViewById(R.id.minY);



        editButton.setVisibility(View.VISIBLE);
        panicButton.setVisibility(View.INVISIBLE);
        speedView.setVisibility(View.INVISIBLE);
        longtitudeView.setVisibility(View.INVISIBLE);
        latitudeView.setVisibility(View.INVISIBLE);
        unitView.setVisibility(View.INVISIBLE);
        sensorView.setVisibility(View.INVISIBLE);

    }



    private boolean checkdata() {
        if (name.equals("") || phnNo2.equals("") || phnNo1.equals(""))
            return false;
        return true;
    }

    private void driveMode(int a) {
        isDriving = !isDriving;
        if (isDriving) {
            editButton.setVisibility(View.INVISIBLE);
            panicButton.setVisibility(View.VISIBLE);
            speedView.setVisibility(View.VISIBLE);
            longtitudeView.setVisibility(View.VISIBLE);
            latitudeView.setVisibility(View.VISIBLE);
            unitView.setVisibility(View.VISIBLE);
            sensorView.setVisibility(View.VISIBLE);

            mainButton.setImageResource(R.drawable.drivingon);
            sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            driveTextView.setText("Drive Mode: ON");

        }else {
            editButton.setVisibility(View.VISIBLE);
            panicButton.setVisibility(View.INVISIBLE);
            speedView.setVisibility(View.INVISIBLE);
            longtitudeView.setVisibility(View.INVISIBLE);
            latitudeView.setVisibility(View.INVISIBLE);
            unitView.setVisibility(View.INVISIBLE);
            sensorView.setVisibility(View.INVISIBLE);

            mainButton.setImageResource(R.drawable.driving);
            changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
            sensorManager.unregisterListener(this);
            driveTextView.setText("Drive Mode: OFF");
        }
        if (a == 1) {
            isDriver=true;
            changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
        } else {
            isDriver=false;
            changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
    }

    private void changeInterruptionFiler(int interruptionFilter) {
        if (mnotificationManager.isNotificationPolicyAccessGranted()) {
            mnotificationManager.setInterruptionFilter(interruptionFilter);
        } else {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    public void sendMessageArray(String[] placeArray){
        Toast.makeText(MainActivity.this,placeArray[0],Toast.LENGTH_LONG).show();

        String placeName="";
        int length = placeArray.length>3 ? 3 : placeArray.length;


        for (int i=1;i<=length;i++){
            placeName+="\n"+i+". "+placeArray[i-1];
        }
        Log.e("Length",String.valueOf(placeName));

        sendMessage("0",placeName,"1");


    }

    public void sendMessage(String no, String finalPlaceName1,String a) {
        String policeStation=" ";
        Toast.makeText(MainActivity.this,finalPlaceName1+": "+no,Toast.LENGTH_LONG).show();
        try {
            String url=MSG_URL+latitude+","+longtitude;
            msg=name+" met an accident and need your help. \n Click the link to locate the location: "+url;

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+91"+phnNo1, null, msg, null, null);
            smsManager.sendTextMessage("+91"+phnNo2, null, msg, null, null);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        if(!(finalPlaceName1.equals(""))) {
                            sleep(2000);
                            msg = "Nearest Police Station from the accident Location  " + finalPlaceName1;
                            smsManager.sendTextMessage("+91" + phnNo1, null, msg, null, null);
                            smsManager.sendTextMessage("+91" + phnNo2, null, msg, null, null);


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();


        } catch (Exception e) {
            Toast.makeText(this, "Send Message Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionGPSCORE() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            speed();
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
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 100);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionSms() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            permissionGPS();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }
    }

    private void speedPermision() {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = (float) 0.8;
        float xValue = Math.abs(alpha * sensorEvent.values[0] + (1 - alpha) * sensorEvent.values[0]);
        float yValue = Math.abs(alpha * sensorEvent.values[1] + (1 - alpha) * sensorEvent.values[1]);
        float zValue = Math.abs(alpha * sensorEvent.values[0] + (1 - alpha) * sensorEvent.values[2]);
        double gValue = Math.sqrt((xValue * xValue) + (yValue * yValue) + (zValue * zValue)) / 9.8;
        TextView minYView = findViewById(R.id.minY);
        //Log.e("Sensor", "Sensor: X " + (xValue) + " Y:" + (yValue) + " Z:" + zValue);
        if (maxg < gValue) {
            minYView.setText(String.valueOf(gValue));
            maxg = gValue;
        }

        speedUpdate();
        if(maxg>MAX_G && k<1){
            k++;
            confirmAccident();
        }

    }

    private void confirmAccident() {
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
        final boolean[] isAccident = {true};
        Handler handler1 = new Handler();
        alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Accident Detected");
        alertDialog.setMessage("Are You Fine?");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"Fine",Toast.LENGTH_SHORT).show();
                isAccident[0] =false;
                accidentOccur(false);
                return;

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isAccident[0]=false;
                accidentOccur(true);
                return;

            }
        });
        alertDialog.show();

        handler1.postDelayed(new Runnable() {


            @Override
            public void run() {
                if (isAccident[0]) {
                    isAccident[0]=false;
                    accidentOccur(true);
                    alertDialog.dismiss();
                }

            }
        }, 15000);

        /*final Runnable r =new Runnable() {
            @Override
            public void run() {

                handler2.postDelayed(this,2000);
            }
        };*/
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(isAccident[0]) {
                        textToSpeech.speak("Accident Detected...Are You Fine????",TextToSpeech.QUEUE_FLUSH,null ,"Accident");
                        sleep(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        //textToSpeech.speak("Are You Fine?",TextToSpeech.QUEUE_FLUSH, null,"Accident");

    }

    private void accidentOccur(boolean b) {
        alertDialog.dismiss();
        if(b){
            Toast.makeText(MainActivity.this,"Accident",Toast.LENGTH_SHORT).show();
            getNearestPolice(latitude,longtitude);

        }else{
            Toast.makeText(MainActivity.this,"Drive Safe",Toast.LENGTH_SHORT).show();
        }
        maxg=0;
        k=0;
        if(isDriving){
            if(isDriver) {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
            }else {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }else {
            changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
        //
    }


    private void speedUpdate() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location locationPass = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        BigDecimal bigDecimal;
        if (locationGps != null) {
            longtitude = locationGps.getLongitude();
            latitude = locationGps.getLatitude();
            curSpeed = locationGps.getSpeed();
            curSpeed*=3.6;


            BigDecimal curSpeeds = new BigDecimal(curSpeed).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlat = new BigDecimal(latitude).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlong = new BigDecimal(longtitude).setScale(2, RoundingMode.HALF_UP);
            //speedView.setText(String.valueOf(curSpeeds));
            latitudeView.setText(String.valueOf(curlat));
            longtitudeView.setText(String.valueOf(curlong));


        } else if (locationNet != null) {
            longtitude = locationNet.getLongitude();
            latitude = locationNet.getLatitude();
            curSpeed = locationNet.getSpeed();
            curSpeed*=3.6;


            BigDecimal curSpeeds = new BigDecimal(curSpeed).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlat = new BigDecimal(latitude).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlong = new BigDecimal(longtitude).setScale(2, RoundingMode.HALF_UP);
            //speedView.setText(String.valueOf(curSpeeds));
            latitudeView.setText(String.valueOf(curlat));
            longtitudeView.setText(String.valueOf(curlong));


        } else if (locationPass != null) {
            longtitude = locationPass.getLongitude();
            latitude = locationPass.getLatitude();
            curSpeed = locationPass.getSpeed();
            curSpeed*=3.6;


            BigDecimal curSpeeds = new BigDecimal(curSpeed).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlat = new BigDecimal(latitude).setScale(2, RoundingMode.HALF_UP);
            BigDecimal curlong = new BigDecimal(longtitude).setScale(2, RoundingMode.HALF_UP);
            //speedView.setText(String.valueOf(curSpeeds));
            latitudeView.setText(String.valueOf(curlat));
            longtitudeView.setText(String.valueOf(curlong));


        } else {
            Toast.makeText(this, "wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void speed() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            // if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionGPS();

                return;

            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListeners);

        }

    }



    public void getNearestPolice(double latitude,double longitude) {

        String type = "police";
        String googlePlacesUrl="https://maps.googleapis.com/maps/api/place/search/json?location="+latitude+","+longitude+"&rankby=distance&types=police&sensor=false&key="+apiKey2;
        //"https://maps.googleapis.com/maps/api/place/search/json?location=22.6219578,88.4158157&rankby=distance&types=police&sensor=false&key=AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8"
        //Log.e("Link",googlePlacesUrl);
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();



        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, googlePlacesUrl,
                null,
                new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {

                        parseLocationResult(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        queue.add(jsonObjectRequest);

        // Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
    }
    private void parseLocationResult(JSONObject result) {
        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;


        try {
            final int[] k = {0};

            JSONArray jsonArray = result.getJSONArray("results");
            //Toast.makeText(this,result.getString(STATUS),Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,result.getString(STATUS),Toast.LENGTH_SHORT).show();
            Log.e("Place",String.valueOf(jsonArray.length()));
            String[] placeArray =new String[jsonArray.length()];
            if (result.getString(STATUS).equalsIgnoreCase("OK")) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);


                    id = place.getString("id");
                    place_id = place.getString("place_id");

                    if (!place.isNull("name")) {

                        placeName = place.getString("name");
                        placeArray[i] = placeName;

                        //return;

                        List<Place.Field> placeFields = Arrays.asList(Place.Field.PHONE_NUMBER);
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(place_id, placeFields);


                        String finalPlaceName = placeName;
                        String finalPlaceName1 = placeName;

                        //Number of Police Station
                        /*

                        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                            Log.e("Place",String.valueOf(response.getPlace()));
                            Place places = response.getPlace();
                            String no;
                            no=places.getPhoneNumber();

                            if(no!=null && k[0]==0) {
                                k[0]=1;
                                Log.e("Place: ",finalPlaceName+" "+no);
                                sendMessage(no,finalPlaceName,"1");


                            }



                         /*   try {


                                if (!(no.isEmpty())) {
                                    //


                                    police = no;
                                    flag++;
                                    sendMessage(no, finalPlaceName,"1");


                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }).addOnFailureListener((exception) -> {

                                    Toast.makeText(this,exception.getMessage(),Toast.LENGTH_LONG).show();

                        });*/

                    }


                }

                //sendMessage("","","1");
                sendMessageArray(placeArray);

            } else if (result.getString(STATUS).equalsIgnoreCase("ZERO_RESULT")) {
                Toast.makeText(getBaseContext(), "No Police Station found in 50KM radius!!!",

                        Toast.LENGTH_LONG).show();
                if (flag == 0) {
                    flag++;
                    sendMessage("", "","2");
                }
            }

        } catch (JSONException e) {


            //Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            if (flag == 0) {
                flag++;
            sendMessage("","","3");
        }}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    @Override
    public void onBackPressed() {
        if(isDriver){
            Toast.makeText(MainActivity.this,"Turn OFF Drive Mode to Exit",Toast.LENGTH_LONG).show();
        }
        else {
            finish();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}