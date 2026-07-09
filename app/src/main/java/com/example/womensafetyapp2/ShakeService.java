package com.example.womensafetyapp2;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

public class ShakeService extends Service implements SensorEventListener {

    SensorManager sensorManager;
    Sensor accelerometer;
    long lastShakeTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, "Shake Detection Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        if (acceleration > 12) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastShakeTime > 6000) {
                lastShakeTime = currentTime;

                Toast.makeText(this, "Shake Detected in Background", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
                sendSOSFromService();
            }
        }
    }
    private void sendSOSFromService() {

        SharedPreferences sp = getSharedPreferences("ContactData", MODE_PRIVATE);
        String phone = sp.getString("phone", "");

        if (phone.isEmpty()) {
            Toast.makeText(this, "No contact saved", Toast.LENGTH_SHORT).show();
            return;
        }

        String message;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location location =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String mapLink = "https://maps.google.com/?q=" + latitude + "," + longitude;

                message = "EMERGENCY ALERT! I need help. My location: " + mapLink;
            } else {
                message = "EMERGENCY ALERT! I need help. Location not available.";
            }

        } else {
            message = "EMERGENCY ALERT! I need help. Location permission not granted.";
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, message, null, null);

        Toast.makeText(this, "Background SOS SMS Sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}