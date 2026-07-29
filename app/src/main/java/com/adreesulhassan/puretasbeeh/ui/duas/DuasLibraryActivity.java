package com.adreesulhassan.puretasbeeh.ui.duas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adreesulhassan.puretasbeeh.R;
import com.adreesulhassan.puretasbeeh.data.entity.ContentCategory;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;
import com.adreesulhassan.puretasbeeh.data.prefs.FiqhPreferences;
import com.adreesulhassan.puretasbeeh.util.HapticHelper;

/**
 * Dynamic Duas & Munajat library — 1 header (single fiqh) or 2 headers (Both).
 */
public class DuasLibraryActivity extends AppCompatActivity {

    public static final String EXTRA_SECT = "extra_sect";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TITLE = "extra_title";

    private FiqhPreferences prefs;
    private String activeSect;
    private TextView headerSingle;
    private LinearLayout headerDualRow;
    private LinearLayout categoryContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duas_library);
        prefs = new FiqhPreferences(this);

        headerSingle = findViewById(R.id.headerSingle);
        headerDualRow = findViewById(R.id.headerDualRow);
        categoryContainer = findViewById(R.id.categoryContainer);

        findViewById(R.id.headerJafriya).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            showCategoriesFor(SectTag.SHIA);
        });
        findViewById(R.id.headerHanfiya).setOnClickListener(v -> {
            HapticHelper.contextClick(v);
            showCategoriesFor(SectTag.SUNNI);
        });

        bindCategoryClicks();
        renderForPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderForPreference();
    }

    private void renderForPreference() {
        if (prefs.isSingleFiqh()) {
            headerDualRow.setVisibility(View.GONE);
            headerSingle.setVisibility(View.VISIBLE);
            categoryContainer.setVisibility(View.VISIBLE);

            boolean jafriya = FiqhPreferences.FIQH_JAFRIYA.equals(prefs.getFiqhOrDefault());
            headerSingle.setText(jafriya
                    ? R.string.duas_jafriya_header
                    : R.string.duas_hanfiya_header);
            activeSect = jafriya ? SectTag.SHIA : SectTag.SUNNI;
        } else {
            headerSingle.setVisibility(View.GONE);
            headerDualRow.setVisibility(View.VISIBLE);
            // Categories hidden until a fiqh header is tapped
            if (activeSect == null) {
                categoryContainer.setVisibility(View.GONE);
            }
        }
    }

    private void showCategoriesFor(String sect) {
        activeSect = sect;
        categoryContainer.setVisibility(View.VISIBLE);
    }

    private void bindCategoryClicks() {
        findViewById(R.id.catNamaz).setOnClickListener(v ->
                openList(ContentCategory.NAMAZ, getString(R.string.cat_namaz), v));
        findViewById(R.id.catSpecial).setOnClickListener(v ->
                openList(ContentCategory.SPECIAL, getString(R.string.cat_special), v));
        findViewById(R.id.catMonths).setOnClickListener(v ->
                openList(ContentCategory.MONTHS, getString(R.string.cat_months), v));
        findViewById(R.id.catDaily).setOnClickListener(v ->
                openList(ContentCategory.DAILY, getString(R.string.cat_daily), v));
    }

    private void openList(String category, String title, View v) {
        HapticHelper.contextClick(v);
        if (activeSect == null) {
            activeSect = prefs.toSectTag();
            if (SectTag.BOTH.equals(activeSect)) {
                activeSect = SectTag.SHIA;
            }
        }
        Intent i = new Intent(this, DuaListActivity.class);
        i.putExtra(EXTRA_SECT, activeSect);
        i.putExtra(EXTRA_CATEGORY, category);
        i.putExtra(EXTRA_TITLE, title);
        startActivity(i);
    }
}
