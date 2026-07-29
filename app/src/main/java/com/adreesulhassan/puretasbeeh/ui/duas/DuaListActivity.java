package com.adreesulhassan.puretasbeeh.ui.duas;

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
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.ArrayList;
import java.util.List;

public class DuaListActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT_ID = "extra_content_id";

    private final List<ContentEntity> items = new ArrayList<>();
    private DuaAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua_list);

        String sect = getIntent().getStringExtra(DuasLibraryActivity.EXTRA_SECT);
        String category = getIntent().getStringExtra(DuasLibraryActivity.EXTRA_CATEGORY);
        String title = getIntent().getStringExtra(DuasLibraryActivity.EXTRA_TITLE);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title != null ? title : getString(R.string.menu_duas));
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DuaAdapter();
        recycler.setAdapter(adapter);

        if (sect == null || category == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        AppDatabase.getInstance(this)
                .contentDao()
                .observeByCategoryAndSect(category, sect)
                .observe(this, list -> {
                    items.clear();
                    if (list != null) {
                        items.addAll(list);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private class DuaAdapter extends RecyclerView.Adapter<DuaAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dua, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ContentEntity item = items.get(position);
            holder.title.setText(item.title);
            holder.arabic.setText(item.arabicText);
            holder.itemView.setOnClickListener(v -> {
                HapticHelper.contextClick(v);
                Intent i = new Intent(DuaListActivity.this, DuaDetailActivity.class);
                i.putExtra(EXTRA_CONTENT_ID, item.id);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView arabic;

            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvItemTitle);
                arabic = itemView.findViewById(R.id.tvItemArabic);
            }
        }
    }
}
