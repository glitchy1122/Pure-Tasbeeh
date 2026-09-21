package com.adreesulhassan.puretasbeeh.ui.ahadith;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.data.prefs.LanguagePreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Sect-filtered Ahadith library — Arabic fixed, translation follows app language.
 */
public class AhadithActivity extends AppCompatActivity {

    private final List<HadithEntity> items = new ArrayList<>();
    private HadithAdapter adapter;
    private String lang = LanguagePreferences.LANG_EN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ahadith);

        lang = new LanguagePreferences(this).getLanguage();
        FiqhPreferences fiqh = new FiqhPreferences(this);
        String sect = fiqh.toSectTag();

        TextView empty = findViewById(R.id.tvEmpty);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HadithAdapter();
        recycler.setAdapter(adapter);

        LiveData<List<HadithEntity>> live = SectTag.BOTH.equals(sect)
                ? AppDatabase.getInstance(this).hadithDao().observeAll()
                : AppDatabase.getInstance(this).hadithDao().observeForSect(sect);
        live.observe(this, list -> {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String next = new LanguagePreferences(this).getLanguage();
        if (!next.equals(lang)) {
            lang = next;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class HadithAdapter extends RecyclerView.Adapter<HadithAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hadith, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            HadithEntity h = items.get(position);
            holder.arabic.setText(h.arabicText);
            holder.translation.setText(h.translationFor(lang));
            holder.source.setText(h.source);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView arabic;
            final TextView translation;
            final TextView source;

            VH(@NonNull View itemView) {
                super(itemView);
                arabic = itemView.findViewById(R.id.tvHadithArabic);
                translation = itemView.findViewById(R.id.tvHadithTranslation);
                source = itemView.findViewById(R.id.tvHadithSource);
            }
        }
    }
}
