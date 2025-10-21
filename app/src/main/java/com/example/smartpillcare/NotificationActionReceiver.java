package com.example.smartpillcare;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;

public class NotificationActionReceiver extends BroadcastReceiver {

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medKey = intent.getStringExtra("medicine_key");
        String medName = intent.getStringExtra("medName");
        if (medKey == null && medName != null) medKey = medName + "_taken_fallback";
        if (medName == null) medName = "Medicine";

        if ("ACTION_TAKEN".equals(action)) {
            SharedPreferences prefs = context.getSharedPreferences("MedicinePrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean(medKey, true).apply();

            // Cancel missed alarm
            assert medKey != null;
            PendingIntent missedPending = PendingIntent.getBroadcast(
                    context,
                    medKey.hashCode() ^ 0x2222,
                    new Intent(context, MissedDoseReceiver.class),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (missedPending != null) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (am != null) am.cancel(missedPending);
            }

            NotificationManagerCompat.from(context).cancel(medKey.hashCode());
            Toast.makeText(context, medName + " marked as taken", Toast.LENGTH_SHORT).show();

        } else if ("ACTION_SNOOZE".equals(action)) {
            // Snooze for 5 minutes
            long snoozeAt = System.currentTimeMillis() + 5 * 60 * 1000L;
            Intent reminder = new Intent(context, ReminderReceiver.class);
            reminder.putExtra("medicine_name", medName);
            reminder.putExtra("medicine_key", medKey);

            assert medKey != null;
            PendingIntent p = PendingIntent.getBroadcast(
                    context,
                    medKey.hashCode() ^ 0x3333,
                    reminder,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeAt, p);

            NotificationManagerCompat.from(context).cancel(medKey.hashCode());
            Toast.makeText(context, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show();
        }
    }
}
