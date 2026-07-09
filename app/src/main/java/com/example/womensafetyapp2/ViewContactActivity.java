package com.example.womensafetyapp2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewContactActivity extends AppCompatActivity {

    TextView tvName, tvPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);

        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);

        SharedPreferences sp = getSharedPreferences("ContactData", MODE_PRIVATE);

        String name = sp.getString("name", "No Contact Saved");
        String phone = sp.getString("phone", "No Phone Number");

        tvName.setText("Name: " + name);
        tvPhone.setText("Phone: " + phone);
    }
}