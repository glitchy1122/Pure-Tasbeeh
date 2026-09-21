package com.adreesulhassan.puretasbeeh.ui.books;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Book catalog (titles). Full PDF downloads coming later — content from
 * Mafatih / Hisnul lives in Duas & Ahadith offline.
 */
public class BooksActivity extends AppCompatActivity {

    private final List<BookEntity> books = new ArrayList<>();
    private BookAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();
        recycler.setAdapter(adapter);

        String sect = new FiqhPreferences(this).toSectTag();
        if (SectTag.BOTH.equals(sect)) {
            AppDatabase.getInstance(this).bookDao().observeAll().observe(this, this::bindBooks);
        } else {
            AppDatabase.getInstance(this).bookDao().observeForSect(sect)
                    .observe(this, this::bindBooks);
        }
    }

    private void bindBooks(@Nullable List<BookEntity> list) {
        books.clear();
        if (list != null) {
            books.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

    private void showComingSoon(@NonNull BookEntity book) {
        new AlertDialog.Builder(this)
                .setTitle(book.bookName)
                .setMessage(R.string.books_coming_soon)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_book, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            BookEntity book = books.get(position);
            holder.name.setText(book.bookName);
            holder.author.setText(getString(R.string.by_author, book.author));
            holder.itemView.setOnClickListener(v -> {
                HapticHelper.contextClick(v);
                showComingSoon(book);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView author;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvBookName);
                author = itemView.findViewById(R.id.tvAuthor);
            }
        }
    }
}
