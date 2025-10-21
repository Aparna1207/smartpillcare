package com.example.smartpillcare;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnPatient, btnMedicine, btnAppointment, btnAskHealthAssistant;
    private static final int REQ_PERMISSIONS = 100;
    private DrawerLayout drawerLayout;
    TextView tvDoctor, tvDate, tvTime; // for colorful appointment card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Drawer navigation handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(MainActivity.this, GetStartedActivity.class);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Buttons
        btnPatient = findViewById(R.id.btnPatient);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnAppointment = findViewById(R.id.btnAppointment);
        btnAskHealthAssistant = findViewById(R.id.btnAskHealthAssistant); // ✅ New button

        // Button click listeners
        btnPatient.setOnClickListener(v -> startActivity(new Intent(this, PatientActivity.class)));
        btnMedicine.setOnClickListener(v -> startActivity(new Intent(this, MedicineActivity.class)));
        btnAppointment.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        btnAskHealthAssistant.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GeminiChatActivity.class);
            startActivity(intent);
        }); // ✅ Handle click

        // Upcoming appointment TextViews
        tvDoctor = findViewById(R.id.tvDoctor);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);

        // Initialize notifications and permissions
        createNotificationChannel();
        requestAllPermissions();

        // Load upcoming appointment from Firestore
        loadUpcomingAppointment();
    }

    // ✅ Load upcoming appointment from Firestore
    @SuppressLint("SetTextI18n")
    private void loadUpcomingAppointment() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .orderBy("date")
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String doctor = query.getDocuments().get(0).getString("doctor_name");
                        String date = query.getDocuments().get(0).getString("date");
                        String time = query.getDocuments().get(0).getString("time");

                        tvDoctor.setText("👩‍⚕‍ Doctor: " + doctor);
                        tvDate.setText("📅 Date: " + date);
                        tvTime.setText("⏰ Time: " + time);
                    } else {
                        tvDoctor.setText("No upcoming appointments");
                        tvDate.setText("");
                        tvTime.setText("");
                    }
                })
                .addOnFailureListener(e -> tvDoctor.setText("Failed to load appointment"));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "med_channel";
            String channelName = "Medicine Reminder";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestAllPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]), REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
                if (permissions[i].equals(Manifest.permission.SEND_SMS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

