package com.example.smartpillcare;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class AppointmentActivity extends AppCompatActivity {

    EditText etDoctor, etDate, etTime, etNotes;
    Button btnSaveApp;
    CalendarView calendarView;
    FirebaseFirestore db;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        etDoctor = findViewById(R.id.etDoctor);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        btnSaveApp = findViewById(R.id.btnSaveApp);
        calendarView = findViewById(R.id.calendarView);
        db = FirebaseFirestore.getInstance();

        // Date picker
        etDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                calendar.set(y, m, d);
                etDate.setText(sdf.format(calendar.getTime()));
            }, year, month, day);
            dpd.show();
        });

        // Save appointment
        btnSaveApp.setOnClickListener(v -> saveAppointment());

        // Calendar click to show info
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            @SuppressLint("DefaultLocale") String dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            db.collection("appointments")
                    .whereEqualTo("date", dateStr)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (var doc : queryDocumentSnapshots.getDocuments()) {
                                sb.append("Dr. ").append(doc.getString("doctor_name"))
                                        .append(" at ").append(doc.getString("time"))
                                        .append("\nNotes: ").append(doc.getString("notes"))
                                        .append("\n\n");
                            }
                            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "No appointment", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveAppointment() {
        String doctor = etDoctor.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (doctor.isEmpty() || dateStr.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill Doctor, Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("doctor_name", doctor);
        appointment.put("date", dateStr);
        appointment.put("time", time);
        appointment.put("notes", notes);

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Appointment Saved", Toast.LENGTH_SHORT).show();
                    etDoctor.setText("");
                    etDate.setText("");
                    etTime.setText("");
                    etNotes.setText("");

                    // Return to MainActivity with refresh signal
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
