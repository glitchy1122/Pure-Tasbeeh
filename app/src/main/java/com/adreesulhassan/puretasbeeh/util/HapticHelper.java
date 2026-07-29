package com.adreesulhassan.puretasbeeh.util;

import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Tactile clay-feel haptics for counter taps and milestones.
 */
public final class HapticHelper {

    private HapticHelper() {
    }

    public static void tap(View view) {
        if (view == null) {
            return;
        }
        view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    public static void contextClick(View view) {
        if (view == null) {
            return;
        }
        view.performHapticFeedback(
                HapticFeedbackConstants.CONTEXT_CLICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    public static void milestone(View view) {
        if (view == null) {
            return;
        }
        view.performHapticFeedback(
                HapticFeedbackConstants.CONFIRM,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }
}
