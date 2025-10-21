package com.example.smartpillcare;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
public class MissedDoseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineKey = intent.getStringExtra("medicine_key");
        String medicineName = intent.getStringExtra("medName");

        SharedPreferences prefs = context.getSharedPreferences("MedicinePrefs", Context.MODE_PRIVATE);
        boolean alreadyTaken = prefs.getBoolean(medicineKey, false);
        if (alreadyTaken) return; // Do nothing if already taken

        // Send SMS to caretaker
        String caretakerNumber = "9842509337"; // replace with actual number
        String message = "Patient missed taking " + medicineName;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(caretakerNumber, null, message, null, null);
    }
}
