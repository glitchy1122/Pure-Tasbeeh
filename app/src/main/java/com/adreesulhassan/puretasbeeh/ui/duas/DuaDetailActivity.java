package com.adreesulhassan.puretasbeeh.ui.duas;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;

public class DuaDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua_detail);

        long id = getIntent().getLongExtra(DuaListActivity.EXTRA_CONTENT_ID, -1);
        String lang = new LanguagePreferences(this).getLanguage();
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvArabic = findViewById(R.id.tvArabic);
        TextView tvTranslation = findViewById(R.id.tvTranslation);

        if (id < 0) {
            finish();
            return;
        }

        AppDatabase.io().execute(() -> {
            ContentEntity entity = AppDatabase.getInstance(this).contentDao().getById(id);
            runOnUiThread(() -> {
                if (entity == null) {
                    finish();
                    return;
                }
                tvTitle.setText(entity.title);
                tvArabic.setText(entity.arabicText);
                tvTranslation.setText(entity.translationFor(lang));
            });
        });
    }
}
