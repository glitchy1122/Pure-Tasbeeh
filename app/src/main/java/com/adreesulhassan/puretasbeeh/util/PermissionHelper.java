package com.adreesulhassan.puretasbeeh.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.adreesulhassan.puretasbeeh.R;

/**
 * Runtime permission helpers. Denied permissions disable that feature only;
 * critical denials show a clear "app feature won't work properly" message.
 */
public final class PermissionHelper {

    public static final int REQ_NOTIFICATIONS = 4101;
    public static final int REQ_LOCATION = 4102;

    private static final String PREFS = "pure_tasbeeh_prefs";
    private static final String KEY_PERMS_PROMPTED = "runtime_perms_prompted_v1";
    private static final String KEY_NOTIF_DENIED = "perm_notif_denied";
    private static final String KEY_LOC_DENIED = "perm_loc_denied";

    public interface BooleanResult {
        void onResult(boolean granted);
    }

    @Nullable
    private static BooleanResult pendingNotifCallback;
    @Nullable
    private static BooleanResult pendingLocCallback;

    public static boolean hasNotificationPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** App-private / cache storage needs no runtime storage permission on API 29+. */
    public static boolean canUseAppPrivateStorage() {
        return true;
    }

    public static boolean wasPermissionOnboardingShown(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_PERMS_PROMPTED, false);
    }

    public static void markPermissionOnboardingShown(@NonNull Context context) {
        prefs(context).edit().putBoolean(KEY_PERMS_PROMPTED, true).apply();
    }

    public static boolean isNotificationDeniedRemembered(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_NOTIF_DENIED, false);
    }

    public static boolean isLocationDeniedRemembered(@NonNull Context context) {
        return prefs(context).getBoolean(KEY_LOC_DENIED, false);
    }

    public static void setNotificationDenied(@NonNull Context context, boolean denied) {
        prefs(context).edit().putBoolean(KEY_NOTIF_DENIED, denied).apply();
    }

    public static void setLocationDenied(@NonNull Context context, boolean denied) {
        prefs(context).edit().putBoolean(KEY_LOC_DENIED, denied).apply();
    }

    public static void showPermissionOnboarding(@NonNull Activity activity,
                                                @Nullable Runnable onFinished) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.perm_onboarding_title)
                .setMessage(R.string.perm_onboarding_message)
                .setPositiveButton(R.string.continue_label, (d, w) -> {
                    markPermissionOnboardingShown(activity);
                    requestNotificationsIfNeeded(activity, granted -> {
                        if (!granted) {
                            setNotificationDenied(activity, true);
                            showFeatureDisabled(activity,
                                    activity.getString(R.string.perm_notif_disabled),
                                    false);
                        } else {
                            setNotificationDenied(activity, false);
                        }
                        // Location is optional — ask next, never block core app.
                        requestLocationIfNeeded(activity, locGranted -> {
                            if (!locGranted) {
                                setLocationDenied(activity, true);
                                showFeatureDisabled(activity,
                                        activity.getString(R.string.perm_location_disabled),
                                        false);
                            } else {
                                setLocationDenied(activity, false);
                            }
                            if (onFinished != null) {
                                onFinished.run();
                            }
                        });
                    });
                })
                .setNegativeButton(R.string.skip_permissions, (d, w) -> {
                    markPermissionOnboardingShown(activity);
                    setNotificationDenied(activity, true);
                    setLocationDenied(activity, true);
                    showFeatureDisabled(activity,
                            activity.getString(R.string.perm_skipped_message),
                            false);
                    if (onFinished != null) {
                        onFinished.run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void requestNotificationsIfNeeded(@NonNull Activity activity,
                                                    @NonNull BooleanResult callback) {
        if (hasNotificationPermission(activity)) {
            callback.onResult(true);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            callback.onResult(true);
            return;
        }
        pendingNotifCallback = callback;
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATIONS);
    }

    public static void requestLocationIfNeeded(@NonNull Activity activity,
                                               @NonNull BooleanResult callback) {
        if (hasLocationPermission(activity)) {
            callback.onResult(true);
            return;
        }
        pendingLocCallback = callback;
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                REQ_LOCATION);
    }

    public static boolean handleRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQ_NOTIFICATIONS) {
            boolean granted = isGranted(grantResults);
            if (pendingNotifCallback != null) {
                pendingNotifCallback.onResult(granted);
                pendingNotifCallback = null;
            }
            return true;
        }
        if (requestCode == REQ_LOCATION) {
            boolean granted = isGranted(grantResults);
            if (pendingLocCallback != null) {
                pendingLocCallback.onResult(granted);
                pendingLocCallback = null;
            }
            return true;
        }
        return false;
    }

    public static void showFeatureDisabled(@NonNull Context context,
                                           @NonNull String message,
                                           boolean critical) {
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle(critical ? R.string.perm_critical_title : R.string.perm_feature_off_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);
        if (critical) {
            b.setNegativeButton(R.string.open_app_settings, (d, w) -> openAppSettings(activity));
        }
        b.show();
    }

    public static void openAppSettings(@NonNull Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    private PermissionHelper() {
    }

    private static boolean isGranted(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
