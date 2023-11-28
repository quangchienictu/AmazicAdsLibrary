package com.amazic.ads.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtil {
    private static boolean isConnectedNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = manager.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    private static boolean isActiveInternetConnection(Context context) {
        if (isConnectedNetwork(context))
            try {
                HttpURLConnection urlConnection =
                        (HttpURLConnection) new URL("https://www.google.com").openConnection();
                urlConnection.setRequestProperty("User-Agent", "Test");
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(2000);
                urlConnection.connect();
                return urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
            }
        return false;
    }

    public static boolean isNetworkActive(Context context) {
        return context != null && isConnectedNetwork(context);
    }
}
