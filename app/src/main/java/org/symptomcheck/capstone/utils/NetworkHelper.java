package org.symptomcheck.capstone.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Ivan on 11/11/2014.
 */
public class NetworkHelper {
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    public static boolean isOnlineOverWifi(Context context){
        ConnectivityManager cm =((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return (isOnline(context) &&
                (ConnectivityManager.TYPE_WIFI == cm.getActiveNetworkInfo().getType()));
    }
}
