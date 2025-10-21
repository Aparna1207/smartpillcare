package com.example.smartpillcare;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvAge, tvContact, tvCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvContact = findViewById(R.id.tvContact);
        tvCondition = findViewById(R.id.tvCondition);

        // Load patient data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PatientData", MODE_PRIVATE);
        tvName.setText(prefs.getString("name", "N/A"));
        tvAge.setText(prefs.getString("age", "N/A"));
        tvContact.setText(prefs.getString("contact", "N/A"));
        tvCondition.setText(prefs.getString("condition", "N/A"));
    }
}
