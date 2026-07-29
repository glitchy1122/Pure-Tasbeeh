package com.adreesulhassan.puretasbeeh.ui.books;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.db.AppDatabase;
import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Islamic books — DownloadManager via Room download_url.
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

        AppDatabase.getInstance(this).bookDao().observeAll().observe(this, list -> {
            books.clear();
            if (list != null) {
                books.addAll(list);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void startDownload(@NonNull BookEntity book) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(book.downloadUrl));
            request.setTitle(book.bookName);
            request.setDescription(getString(R.string.downloading));
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    sanitizeFileName(book.bookName) + ".pdf");
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\- ]", "_").trim();
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
            holder.download.setOnClickListener(v -> {
                HapticHelper.contextClick(v);
                startDownload(book);
            });
            holder.itemView.setOnClickListener(v -> {
                HapticHelper.contextClick(v);
                startDownload(book);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView author;
            final TextView download;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvBookName);
                author = itemView.findViewById(R.id.tvAuthor);
                download = itemView.findViewById(R.id.btnDownload);
            }
        }
    }
}
