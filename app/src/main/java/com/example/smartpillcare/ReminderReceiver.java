package com.example.smartpillcare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine_name");
        String medicineKey = intent.getStringExtra("medicine_key");
        int stock = intent.getIntExtra("stock", -1);
        int refill = intent.getIntExtra("refill", -1);

        if (medicineName == null) medicineName = "Medicine";
        if (medicineKey == null) medicineKey = medicineName + "_taken_fallback";

        SharedPreferences prefs = context.getSharedPreferences("MedicinePrefs", Context.MODE_PRIVATE);
        boolean alreadyTaken = prefs.getBoolean(medicineKey, false);
        if (alreadyTaken) return;

        // --- TAKEN PendingIntent ---
        Intent takenAction = new Intent(context, NotificationActionReceiver.class);
        takenAction.setAction("ACTION_TAKEN");
        takenAction.putExtra("medicine_key", medicineKey);
        takenAction.putExtra("medName", medicineName);

        PendingIntent pTaken = PendingIntent.getBroadcast(
                context,
                medicineKey.hashCode() & 0x7FFFFFFF,
                takenAction,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- SNOOZE PendingIntent ---
        Intent snoozeAction = new Intent(context, NotificationActionReceiver.class);
        snoozeAction.setAction("ACTION_SNOOZE");
        snoozeAction.putExtra("medicine_key", medicineKey);
        snoozeAction.putExtra("medName", medicineName);

        PendingIntent pSnooze = PendingIntent.getBroadcast(
                context,
                (medicineKey.hashCode() & 0x7FFFFFFF) + 1000,
                snoozeAction,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        boolean lowStock = (stock != -1 && refill != -1 && stock <= refill);
        String content = lowStock ? ("Time to take " + medicineName + " — Low stock!") : ("Time to take " + medicineName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "med_channel")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(lowStock ? "Low stock alert" : "Medicine Reminder")
                .setContentText(content)
                .setAutoCancel(true)
                .addAction(R.drawable.ok, "Taken", pTaken)
                .addAction(R.drawable.snooze, "Snooze", pSnooze)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            nm.notify(medicineKey.hashCode(), builder.build());
        }

        // --- Start TTS Service ---
        Intent ttsIntent = new Intent(context, ReminderService.class);
        ttsIntent.putExtra("medicine_name", medicineName);
        ttsIntent.putExtra("low_stock", lowStock);
        context.startService(ttsIntent);

        // --- Schedule missed dose after 2 minutes ---
        long missDelay = System.currentTimeMillis() + 2 * 60 * 1000;
        Intent missedIntent = new Intent(context, MissedDoseReceiver.class);
        missedIntent.putExtra("medName", medicineName);
        missedIntent.putExtra("medicine_key", medicineKey);

        PendingIntent missedPending = PendingIntent.getBroadcast(
                context,
                medicineKey.hashCode() ^ 0x2222,
                missedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, missDelay, missedPending);
        }
    }
}
