package com.adreesulhassan.puretasbeeh.data.location;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Offline major-city list loaded from {@code assets/cities.json}. */
public final class CityRepository {

    public static final class City {
        @NonNull
        public final String name;
        @NonNull
        public final String country;
        public final double lat;
        public final double lng;
        @NonNull
        public final String timeZoneId;

        public City(@NonNull String name, @NonNull String country,
                    double lat, double lng, @NonNull String timeZoneId) {
            this.name = name;
            this.country = country;
            this.lat = lat;
            this.lng = lng;
            this.timeZoneId = timeZoneId;
        }

        @NonNull
        public String displayLabel() {
            return name + ", " + country;
        }
    }

    private CityRepository() {
    }

    @NonNull
    public static List<City> loadAll(@NonNull Context context) {
        List<City> cities = new ArrayList<>();
        try (InputStream in = context.getAssets().open("cities.json");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                cities.add(new City(
                        o.getString("name"),
                        o.getString("country"),
                        o.getDouble("lat"),
                        o.getDouble("lng"),
                        o.optString("tz", "UTC")
                ));
            }
        } catch (Exception ignored) {
            // Fallback single city if asset missing
            cities.add(new City("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"));
        }
        return cities;
    }

    @NonNull
    public static List<City> filter(@NonNull List<City> all, @NonNull String query) {
        String q = query.trim().toLowerCase(Locale.US);
        if (q.isEmpty()) {
            return new ArrayList<>(all);
        }
        List<City> out = new ArrayList<>();
        for (City c : all) {
            if (c.name.toLowerCase(Locale.US).contains(q)
                    || c.country.toLowerCase(Locale.US).contains(q)) {
                out.add(c);
            }
        }
        return out;
    }
}
