package com.example.smartpillcare;

import androidx.annotation.NonNull;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class GeminiFlashHelper {

    // 🔑 Replace YOUR_API_KEY with your Gemini Flash API key
    private static final String API_KEY = "AIzaSyDAxCxoQSuMSWcXIpI_qwSyK7Yr_5cv4rE";
    private static final String MODEL = "gemini-2.0-flash";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL + ":generateContent?key=" + API_KEY;

    public interface GeminiResponseListener {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void askQuestion(String question, GeminiResponseListener listener) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", question);
            parts.put(textPart);

            content.put("parts", parts);
            contents.put(content);
            json.put("contents", contents);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    listener.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        try {
                            JSONObject obj = new JSONObject(result);
                            String reply = obj.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            listener.onSuccess(reply);
                        } catch (Exception e) {
                            listener.onFailure("Parsing error: " + e.getMessage());
                        }
                    } else {
                        listener.onFailure("Error: " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            listener.onFailure(e.getMessage());
        }
    }
}
