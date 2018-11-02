package gr.cryptocurrencies.bitcoinpos.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Utilities {


    public static boolean isNetworkConnectionAvailable(Context context) {
        boolean isConnectedToWifi = false;
        boolean isConnectedToMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    isConnectedToWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    isConnectedToMobile = true;
        }
        return isConnectedToWifi || isConnectedToMobile;
    }

}
