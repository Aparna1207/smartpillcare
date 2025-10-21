package com.example.smartpillcare;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MedicineActivity extends AppCompatActivity {

    EditText etName, etDosage, etHour, etMinute, etStock, etRefill;
    Button btnSaveMed;
    FirebaseFirestore db;

    @SuppressLint({"ScheduleExactAlarm", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        etName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etDosage);
        etHour = findViewById(R.id.etHour);
        etMinute = findViewById(R.id.etMinute);
        etStock = findViewById(R.id.etStock);
        etRefill = findViewById(R.id.etRefill);
        btnSaveMed = findViewById(R.id.btnSaveMed);
        db = FirebaseFirestore.getInstance();

        btnSaveMed.setOnClickListener(v -> saveMedicine());
    }

    private void saveMedicine() {
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String hourStr = etHour.getText().toString().trim();
        String minuteStr = etMinute.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String refillStr = etRefill.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || hourStr.isEmpty() || minuteStr.isEmpty() || stockStr.isEmpty() || refillStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour, minute, stock, refill;
        try {
            hour = Integer.parseInt(hourStr);
            minute = Integer.parseInt(minuteStr);
            stock = Integer.parseInt(stockStr);
            refill = Integer.parseInt(refillStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        // create stable tracking key
        String medicineKey = name + "_taken_" + (System.currentTimeMillis() / 1000L);
        SharedPreferences prefs = getSharedPreferences("MedicinePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean(medicineKey, false).apply();

        Map<String, Object> medicine = new HashMap<>();
        medicine.put("name", name);
        medicine.put("dosage", dosage);
        medicine.put("hour", hour);
        medicine.put("minute", minute);
        medicine.put("stock_count", stock);
        medicine.put("refill_alert", refill);


        db.collection("medicines").add(medicine)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Medicine Added", Toast.LENGTH_SHORT).show();
                    scheduleReminder(name, hour, minute, stock, refill, medicineKey);
                    etName.setText(""); etDosage.setText(""); etHour.setText(""); etMinute.setText(""); etStock.setText(""); etRefill.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleReminder(String medicineName, int hour, int minute, int stock, int refill, String medicineKey) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("medicine_key", medicineKey);
        intent.putExtra("stock", stock);
        intent.putExtra("refill", refill);

        int reqCode = medicineKey.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms())
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                else {
                    // prompt user to enable exact alarms
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        }
    }
}
