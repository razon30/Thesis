package dev.jokr.localnetworkapp.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import dev.jokr.localnetworkapp.ObservableObject;

/**
 * Created by razon30 on 26-02-17.
 */

public class MyReceive extends BroadcastReceiver {

    NetworkInfo wifiCheck;
    ConnectivityManager connectivityManager;

    public MyReceive() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        ObservableObject.getInstance().updatevalue(intent);
    }

}
