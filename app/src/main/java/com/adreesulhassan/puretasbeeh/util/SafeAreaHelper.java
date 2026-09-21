package com.adreesulhassan.puretasbeeh.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.adreesulhassan.puretasbeeh.R;

/**
 * With targetSdk 35+ every activity is laid out edge to edge, so screens must add
 * system-bar and cutout insets themselves. This keeps each layout's own
 * {@code screen_margin} padding and adds the insets on top of it.
 */
public final class SafeAreaHelper {

    private SafeAreaHelper() {
    }

    public static void apply(@NonNull Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup) || ((ViewGroup) content).getChildCount() == 0) {
            return;
        }
        View root = ((ViewGroup) content).getChildAt(0);
        if (Boolean.TRUE.equals(root.getTag(R.id.tag_safe_area_applied))) {
            return;
        }
        root.setTag(R.id.tag_safe_area_applied, Boolean.TRUE);

        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(
                    baseLeft + bars.left,
                    baseTop + bars.top,
                    baseRight + bars.right,
                    baseBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
