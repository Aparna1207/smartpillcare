package com.example.smartpillcare;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import java.util.Locale;
public class ReminderService extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private String medicineName;
    private boolean lowStock;
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        medicineName = intent.getStringExtra("medicine_name");
        lowStock = intent.getBooleanExtra("low_stock", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification.Builder(this, "med_channel")
                    .setContentTitle("Medicine Reminder")
                    .setContentText("Speaking reminder...")
                    .setSmallIcon(R.drawable.icon)
                    .build());
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(1.0f);
            speakMedicineReminder();
        }
    }

    private void speakMedicineReminder() {
        if (medicineName == null) medicineName = "medicine";

        String text = "Time to take " + medicineName;
        if (lowStock) {
            text += " — Stock is low! Please refill soon.";
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MED_REMINDER");

        String finalText = text;
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                count++;
                if (count < 3) {
                    tts.speak(finalText, TextToSpeech.QUEUE_FLUSH, null, "MED_REMINDER");
                } else {
                    stopSelf();
                }
            }

            @Override
            public void onError(String utteranceId) {
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
