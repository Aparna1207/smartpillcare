package com.example.smartpillcare;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
public class GeminiChatActivity extends AppCompatActivity {
    private LinearLayout chatLayout;
    private EditText inputMessage;
    private ProgressBar loadingSpinner;
    private ScrollView chatScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_chat);

        chatLayout = findViewById(R.id.chatLayout);
        inputMessage = findViewById(R.id.inputMessage);
        Button btnSend = findViewById(R.id.btnSend);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        chatScroll = findViewById(R.id.chatScroll);

        btnSend.setOnClickListener(v -> {
            String question = inputMessage.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }
            addMessage("You: " + question, true);
            inputMessage.setText("");
            loadingSpinner.setVisibility(View.VISIBLE);

            GeminiFlashHelper.askQuestion(question, new GeminiFlashHelper.GeminiResponseListener() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        addMessage("Gemini: " + response, false);
                        loadingSpinner.setVisibility(View.GONE);
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        addMessage("Error: " + error, false);
                        loadingSpinner.setVisibility(View.GONE);
                    });
                }
            });
        });
    }
    private void addMessage(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 10, 16, 10);
        tv.setTextSize(16f);
        tv.setBackgroundResource(isUser ? android.R.color.holo_blue_light : android.R.color.darker_gray);
        tv.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ((LinearLayout.LayoutParams) tv.getLayoutParams()).setMargins(8, 8, 8, 8);
        chatLayout.addView(tv);

        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }
}
