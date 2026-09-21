package com.adreesulhassan.puretasbeeh.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.location.CityRepository;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LocationPreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;
import com.adreesulhassan.puretasbeeh.util.PermissionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class LocationSettingsActivity extends AppCompatActivity {

    private LocationPreferences prefs;
    private List<CityRepository.City> allCities = new ArrayList<>();
    private CityAdapter adapter;
    private TextView tvSelectedCity;
    private TextView tvCityHint;
    private RecyclerView rvCities;
    private EditText etSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);

        prefs = new LocationPreferences(this);
        tvSelectedCity = findViewById(R.id.tvSelectedCity);
        tvCityHint = findViewById(R.id.tvCityHint);
        etSearch = findViewById(R.id.etSearchCity);
        rvCities = findViewById(R.id.rvCities);

        adapter = new CityAdapter(city -> {
            HapticHelper.contextClick(rvCities);
            prefs.setCity(city.name, city.country, city.lat, city.lng, city.timeZoneId);
            bindSelected();
            hideCityResults();
            etSearch.setText("");
            Toast.makeText(this, getString(R.string.city_selected, city.name), Toast.LENGTH_SHORT).show();
        });
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(adapter);

        allCities = CityRepository.loadAll(this);
        bindSelected();
        hideCityResults();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Keep list hidden until Add City is pressed.
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btnAddCity).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            String q = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            if (TextUtils.isEmpty(q)) {
                Toast.makeText(this, R.string.search_city_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            List<CityRepository.City> filtered = CityRepository.filter(allCities, q);
            if (filtered.isEmpty()) {
                hideCityResults();
                Toast.makeText(this, R.string.city_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.submit(filtered);
            rvCities.setVisibility(View.VISIBLE);
            tvCityHint.setVisibility(View.GONE);
        });

        findViewById(R.id.btnUseDeviceLocation).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            requestDeviceLocation();
        });
        findViewById(R.id.btnNamazOffset).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, NamazOffsetActivity.class));
        });
        findViewById(R.id.btnAppLanguage).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            showLanguagePicker();
        });
        findViewById(R.id.btnFiqhSettings).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            startActivity(new Intent(this, FiqhSettingsActivity.class));
        });
    }

    private void showLanguagePicker() {
        LanguagePreferences langPrefs = new LanguagePreferences(this);
        final String[] codes = {
                LanguagePreferences.LANG_EN,
                LanguagePreferences.LANG_UR,
                LanguagePreferences.LANG_AR,
                LanguagePreferences.LANG_FA
        };
        final String[] labels = {
                LanguagePreferences.displayName(LanguagePreferences.LANG_EN),
                LanguagePreferences.displayName(LanguagePreferences.LANG_UR),
                LanguagePreferences.displayName(LanguagePreferences.LANG_AR),
                LanguagePreferences.displayName(LanguagePreferences.LANG_FA)
        };
        int checked = 0;
        String current = langPrefs.getLanguage();
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language_title)
                .setSingleChoiceItems(labels, checked, (d, which) -> {
                    langPrefs.setLanguage(codes[which]);
                    d.dismiss();
                    Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void hideCityResults() {
        rvCities.setVisibility(View.GONE);
        tvCityHint.setVisibility(View.VISIBLE);
        adapter.submit(new ArrayList<>());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestDeviceLocation() {
        if (PermissionHelper.isLocationDeniedRemembered(this)
                && !PermissionHelper.hasLocationPermission(this)) {
            PermissionHelper.showFeatureDisabled(
                    this,
                    getString(R.string.perm_location_disabled),
                    false);
            return;
        }
        if (PermissionHelper.hasLocationPermission(this)) {
            applyLastKnownLocation();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.perm_location_title)
                .setMessage(R.string.perm_location_rationale)
                .setPositiveButton(R.string.continue_label, (d, w) ->
                        PermissionHelper.requestLocationIfNeeded(this, granted -> {
                            PermissionHelper.setLocationDenied(this, !granted);
                            if (!granted) {
                                PermissionHelper.showFeatureDisabled(
                                        this,
                                        getString(R.string.perm_location_disabled),
                                        false);
                                return;
                            }
                            applyLastKnownLocation();
                        }))
                .setNegativeButton(R.string.cancel, (d, w) -> {
                    PermissionHelper.setLocationDenied(this, true);
                    Toast.makeText(this, R.string.perm_location_disabled, Toast.LENGTH_LONG).show();
                })
                .show();
    }

    @SuppressLint("MissingPermission")
    private void applyLastKnownLocation() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm == null) {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        Location best = null;
        for (String provider : lm.getProviders(true)) {
            try {
                Location loc = lm.getLastKnownLocation(provider);
                if (loc == null) {
                    continue;
                }
                if (best == null || loc.getAccuracy() < best.getAccuracy()) {
                    best = loc;
                }
            } catch (SecurityException ignored) {
                PermissionHelper.setLocationDenied(this, true);
                Toast.makeText(this, R.string.perm_location_disabled, Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (best == null) {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

        CityRepository.City nearest = findNearestCity(best.getLatitude(), best.getLongitude());
        String label = nearest != null
                ? nearest.name
                : getString(R.string.device_location_label);
        String country = nearest != null ? nearest.country : "";
        String tz = nearest != null ? nearest.timeZoneId : TimeZone.getDefault().getID();
        prefs.setCity(label, country, best.getLatitude(), best.getLongitude(), tz);
        bindSelected();
        Toast.makeText(this, getString(R.string.city_selected, label), Toast.LENGTH_SHORT).show();
    }

    @Nullable
    private CityRepository.City findNearestCity(double lat, double lng) {
        CityRepository.City best = null;
        double bestDist = Double.MAX_VALUE;
        for (CityRepository.City c : allCities) {
            double dLat = Math.toRadians(c.lat - lat);
            double dLng = Math.toRadians(c.lng - lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(c.lat))
                    * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double dist = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            if (dist < bestDist) {
                bestDist = dist;
                best = c;
            }
        }
        if (best != null && bestDist * 6371.0 < 150.0) {
            return best;
        }
        return null;
    }

    private void bindSelected() {
        tvSelectedCity.setText(getString(R.string.selected_city)
                + ": " + prefs.getCityName()
                + (TextUtils.isEmpty(prefs.getCountry()) ? "" : ", " + prefs.getCountry()));
    }

    private interface CityClick {
        void onClick(CityRepository.City city);
    }

    private static final class CityAdapter extends RecyclerView.Adapter<CityAdapter.Holder> {

        private final List<CityRepository.City> items = new ArrayList<>();
        private final CityClick click;

        CityAdapter(CityClick click) {
            this.click = click;
        }

        void submit(@NonNull List<CityRepository.City> cities) {
            items.clear();
            items.addAll(cities);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_city, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            CityRepository.City city = items.get(position);
            holder.label.setText(city.displayLabel());
            holder.label.setOnClickListener(v -> click.onClick(city));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView label;

            Holder(@NonNull TextView itemView) {
                super(itemView);
                label = itemView;
            }
        }
    }
}
