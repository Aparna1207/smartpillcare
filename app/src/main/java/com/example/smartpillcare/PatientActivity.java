package com.example.smartpillcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class PatientActivity extends AppCompatActivity {

    EditText etName, etAge, etContact, etCondition;
    Button btnSave;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etContact = findViewById(R.id.etContact);
        etCondition = findViewById(R.id.etCondition);
        btnSave = findViewById(R.id.btnSavePatient);

        db = FirebaseFirestore.getInstance();

        // Load previous data from SharedPreferences (optional)
        SharedPreferences prefs = getSharedPreferences("PatientData", MODE_PRIVATE);
        etName.setText(prefs.getString("name", ""));
        etAge.setText(prefs.getString("age", ""));
        etContact.setText(prefs.getString("contact", ""));
        etCondition.setText(prefs.getString("condition", ""));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String condition = etCondition.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill Name, Age and Contact", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Firestore
            Map<String, Object> patient = new HashMap<>();
            patient.put("name", name);
            patient.put("age", age);
            patient.put("contact", contact);
            patient.put("condition", condition);

            db.collection("patients").add(patient)
                    .addOnSuccessListener(doc -> Toast.makeText(this, "Patient saved in Firestore", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            // Save to SharedPreferences for ProfileActivity
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", name);
            editor.putString("age", ageStr);
            editor.putString("contact", contact);
            editor.putString("condition", condition);
            editor.apply();

            Toast.makeText(this, "Patient data saved locally", Toast.LENGTH_SHORT).show();

            // Clear fields (optional)
            etName.setText("");
            etAge.setText("");
            etContact.setText("");
            etCondition.setText("");
        });
    }
}
