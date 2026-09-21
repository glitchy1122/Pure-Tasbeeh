package com.adreesulhassan.puretasbeeh.ui.quran;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.DatabaseHelper;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SurahDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SURAH_NUMBER = "surah_number";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        int surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, 1);
        String lang = new LanguagePreferences(this).getLanguage();
        TextView tvTitle = findViewById(R.id.tvSurahTitle);
        TextView tvArabic = findViewById(R.id.tvSurahArabic);
        RecyclerView rv = findViewById(R.id.rvAyahs);
        AyahAdapter adapter = new AyahAdapter(lang);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.ensureCopiedAsync((ok, err) -> new Thread(() -> {
            DatabaseHelper.Surah surah = db.getSurah(surahNumber);
            List<DatabaseHelper.Ayah> ayahs = db.getAyahsForSurah(surahNumber);
            runOnUiThread(() -> {
                if (surah != null) {
                    tvTitle.setText(String.format(Locale.getDefault(),
                            "%d. %s", surah.number, surah.nameEn));
                    tvArabic.setText(surah.nameAr);
                } else {
                    tvTitle.setText(getString(R.string.quran_title));
                    tvArabic.setText("");
                }
                adapter.submit(ayahs);
            });
        }).start());
    }

    private static final class AyahAdapter extends RecyclerView.Adapter<AyahAdapter.Holder> {
        private final List<DatabaseHelper.Ayah> items = new ArrayList<>();
        private final String lang;

        AyahAdapter(@NonNull String lang) {
            this.lang = lang;
        }

        void submit(@NonNull List<DatabaseHelper.Ayah> ayahs) {
            items.clear();
            items.addAll(ayahs);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ayah, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DatabaseHelper.Ayah a = items.get(position);
            holder.number.setText(String.format(Locale.getDefault(),
                    "%d:%d", a.surahNumber, a.ayahNumber));
            holder.text.setText(a.textAr);
            String tr = a.translationFor(lang);
            if (tr.isEmpty() || LanguagePreferences.LANG_AR.equals(lang)) {
                holder.translation.setVisibility(View.GONE);
            } else {
                holder.translation.setVisibility(View.VISIBLE);
                holder.translation.setText(tr);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView number;
            final TextView text;
            final TextView translation;

            Holder(@NonNull View itemView) {
                super(itemView);
                number = itemView.findViewById(R.id.tvAyahNumber);
                text = itemView.findViewById(R.id.tvAyahText);
                translation = itemView.findViewById(R.id.tvAyahTranslation);
            }
        }
    }
}
