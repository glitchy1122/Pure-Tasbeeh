package com.adreesulhassan.puretasbeeh.ui.settings;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

public class FiqhSettingsActivity extends AppCompatActivity {

    private FiqhPreferences prefs;
    private RadioGroup radioGroup;
    private RadioButton radioJafriya;
    private RadioButton radioHanfiya;
    private RadioButton radioBoth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiqh_settings);

        prefs = new FiqhPreferences(this);
        radioGroup = findViewById(R.id.radioGroup);
        radioJafriya = findViewById(R.id.radioJafriya);
        radioHanfiya = findViewById(R.id.radioHanfiya);
        radioBoth = findViewById(R.id.radioBoth);

        String current = prefs.getFiqhOrDefault();
        switch (current) {
            case FiqhPreferences.FIQH_JAFRIYA:
                radioJafriya.setChecked(true);
                break;
            case FiqhPreferences.FIQH_HANFIYA:
                radioHanfiya.setChecked(true);
                break;
            case FiqhPreferences.FIQH_BOTH:
                radioBoth.setChecked(true);
                break;
            default:
                radioBoth.setChecked(true);
                break;
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            HapticHelper.milestone(v);
            String value;
            int checked = radioGroup.getCheckedRadioButtonId();
            if (checked == R.id.radioJafriya) {
                value = FiqhPreferences.FIQH_JAFRIYA;
            } else if (checked == R.id.radioHanfiya) {
                value = FiqhPreferences.FIQH_HANFIYA;
            } else {
                value = FiqhPreferences.FIQH_BOTH;
            }
            prefs.setFiqh(value);
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
