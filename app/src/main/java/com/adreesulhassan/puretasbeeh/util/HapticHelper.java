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
        perform(view, HapticFeedbackConstants.KEYBOARD_PRESS);
    }

    public static void contextClick(View view) {
        perform(view, HapticFeedbackConstants.CONTEXT_CLICK);
    }

    public static void milestone(View view) {
        perform(view, HapticFeedbackConstants.CONFIRM);
    }

    private static void perform(View view, int feedbackConstant) {
        view.performHapticFeedback(
                feedbackConstant,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }
}
