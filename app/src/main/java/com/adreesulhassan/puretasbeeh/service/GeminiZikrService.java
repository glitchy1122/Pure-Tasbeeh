package com.adreesulhassan.puretasbeeh.service;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.adreesulhassan.puretasbeeh.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gemini-powered zikr suggestion.
 * API key comes ONLY from BuildConfig (Secrets Gradle Plugin / local.properties).
 *
 * Expected JSON in model response:
 * { "zikr_arabic": "...", "zikr_transliteration": "...", "recommended_count": 33 }
 */
public class GeminiZikrService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String MODEL = "gemini-2.0-flash";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=";

    private final OkHttpClient client;

    public GeminiZikrService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static final class ZikrSuggestion {
        @NonNull
        public final String arabic;
        @NonNull
        public final String transliteration;
        public final int recommendedCount;

        public ZikrSuggestion(@NonNull String arabic,
                              @NonNull String transliteration,
                              int recommendedCount) {
            this.arabic = arabic;
            this.transliteration = transliteration;
            this.recommendedCount = recommendedCount > 0 ? recommendedCount : 33;
        }
    }

    public boolean hasApiKey() {
        String key = BuildConfig.GEMINI_API_KEY;
        return !TextUtils.isEmpty(key)
                && !"REPLACE_WITH_YOUR_GEMINI_API_KEY".equals(key);
    }

    @Nullable
    @WorkerThread
    public ZikrSuggestion suggestZikr(@NonNull String feeling) throws IOException {
        if (!hasApiKey()) {
            throw new IOException("GEMINI_API_KEY missing");
        }

        String prompt = "You are an Islamic zikr helper for a privacy-focused offline-first app. "
                + "The user feels: \"" + feeling.replace("\"", "'") + "\". "
                + "Reply with ONLY valid JSON (no markdown) in this exact shape: "
                + "{\"zikr_arabic\":\"...\",\"zikr_transliteration\":\"...\","
                + "\"recommended_count\":33}. "
                + "Pick an authentic short dhikr suitable for their feeling. "
                + "recommended_count should be 33, 34, 100, or similar.";

        JSONObject body = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.4);
            generationConfig.put("maxOutputTokens", 256);
            body.put("generationConfig", generationConfig);
        } catch (Exception e) {
            throw new IOException("Failed to build Gemini request", e);
        }

        Request request = new Request.Builder()
                .url(ENDPOINT + BuildConfig.GEMINI_API_KEY)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Gemini HTTP " + response.code());
            }
            String raw = response.body().string();
            return parseSuggestion(raw);
        }
    }

    @NonNull
    private ZikrSuggestion parseSuggestion(@NonNull String rawResponse) throws IOException {
        try {
            JSONObject root = new JSONObject(rawResponse);
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("No candidates");
            }
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String text = parts.getJSONObject(0).getString("text").trim();

            // Strip optional markdown fences
            if (text.startsWith("```")) {
                int firstNl = text.indexOf('\n');
                int lastFence = text.lastIndexOf("```");
                if (firstNl >= 0 && lastFence > firstNl) {
                    text = text.substring(firstNl + 1, lastFence).trim();
                }
            }

            JSONObject zikr = new JSONObject(text);
            String arabic = zikr.optString("zikr_arabic", "سُبْحَانَ اللهِ");
            String translit = zikr.optString("zikr_transliteration", "Subhanallah");
            int count = zikr.optInt("recommended_count", 33);
            return new ZikrSuggestion(arabic, translit, count);
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini zikr JSON", e);
        }
    }
}
