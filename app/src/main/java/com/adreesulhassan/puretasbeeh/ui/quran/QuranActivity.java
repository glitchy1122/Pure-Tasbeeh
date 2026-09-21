package com.adreesulhassan.puretasbeeh.ui.quran;

import android.content.Intent;
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
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.ArrayList;
import java.util.List;

public class QuranActivity extends AppCompatActivity {

    private TextView tvStatus;
    private SurahAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran);

        tvStatus = findViewById(R.id.tvStatus);
        RecyclerView rv = findViewById(R.id.rvSurahs);
        adapter = new SurahAdapter(surah -> {
            HapticHelper.contextClick(rv);
            Intent intent = new Intent(this, SurahDetailActivity.class);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NUMBER, surah.number);
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        dbHelper = DatabaseHelper.getInstance(this);
        tvStatus.setText(R.string.quran_loading);

        dbHelper.ensureCopiedAsync((success, error) -> runOnUiThread(() -> {
            if (!success) {
                tvStatus.setText(getString(R.string.quran_empty));
                return;
            }
            loadSurahs();
        }));
    }

    private void loadSurahs() {
        new Thread(() -> {
            List<DatabaseHelper.Surah> surahs = dbHelper.getAllSurahs();
            runOnUiThread(() -> {
                if (surahs.isEmpty()) {
                    tvStatus.setText(R.string.quran_empty);
                } else {
                    tvStatus.setText(getString(R.string.quran_title) + " · " + surahs.size());
                    adapter.submit(surahs);
                }
            });
        }).start();
    }

    private interface SurahClick {
        void onClick(DatabaseHelper.Surah surah);
    }

    private static final class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.Holder> {

        private final List<DatabaseHelper.Surah> items = new ArrayList<>();
        private final SurahClick click;

        SurahAdapter(SurahClick click) {
            this.click = click;
        }

        void submit(@NonNull List<DatabaseHelper.Surah> surahs) {
            items.clear();
            items.addAll(surahs);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_surah, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DatabaseHelper.Surah s = items.get(position);
            holder.number.setText(String.valueOf(s.number));
            holder.nameEn.setText(s.nameEn);
            holder.nameAr.setText(s.nameAr);
            holder.itemView.setOnClickListener(v -> click.onClick(s));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView number;
            final TextView nameEn;
            final TextView nameAr;

            Holder(@NonNull View itemView) {
                super(itemView);
                number = itemView.findViewById(R.id.tvSurahNumber);
                nameEn = itemView.findViewById(R.id.tvSurahNameEn);
                nameAr = itemView.findViewById(R.id.tvSurahNameAr);
            }
        }
    }
}
