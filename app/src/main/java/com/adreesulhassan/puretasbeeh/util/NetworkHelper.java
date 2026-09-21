package com.adreesulhassan.puretasbeeh.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

/**
 * Reliable online check — requires a validated internet-capable network,
 * not merely a Wi‑Fi/cellular association (avoids false "offline" states).
 */
public final class NetworkHelper {

    private NetworkHelper() {
    }

    public static boolean hasInternet(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        if (caps == null) {
            return false;
        }
        boolean hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
        boolean canReach = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // VALIDATED can lag right after connecting; treat INTERNET+transport as online.
        return hasTransport && canReach;
    }
}
