package com.example.womensafetyapp2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btnSOS, btnAddContact, btnViewContact;
    SensorManager sensorManager;
    Sensor accelerometer;
    long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSOS = findViewById(R.id.btnSOS);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnViewContact = findViewById(R.id.btnViewContact);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Intent serviceIntent = new Intent(MainActivity.this, ShakeService.class);
        startService(serviceIntent);

        btnSOS.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, 1);

            } else {
                sendSOSMessage();
            }
        });

        btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        });

        btnViewContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewContactActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }
    private void sendSOSMessage() {

        SharedPreferences sp = getSharedPreferences("ContactData", MODE_PRIVATE);
        String phone = sp.getString("phone", "");

        if (phone.isEmpty()) {
            Toast.makeText(this, "No contact saved", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String message;

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            String mapLink = "https://maps.google.com/?q=" + latitude + "," + longitude;

            message = "EMERGENCY ALERT! I need help. My location: " + mapLink;
        } else {
            message = "EMERGENCY ALERT! I need help. Location not available.";
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, message, null, null);

        Toast.makeText(this, "SOS SMS Sent with Location", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration =
                (float) Math.sqrt(x * x + y * y + z * z);

        if (acceleration > 10) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastShakeTime > 2000) {

                lastShakeTime = currentTime;

                Toast.makeText(this,
                        "Shake Detected!",
                        Toast.LENGTH_SHORT).show();

                //sendSOSMessage();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}