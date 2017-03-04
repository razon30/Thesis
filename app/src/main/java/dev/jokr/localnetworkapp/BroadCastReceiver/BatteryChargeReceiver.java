package dev.jokr.localnetworkapp.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * Created by razon30 on 27-02-17.
 */

public class BatteryChargeReceiver extends BroadcastReceiver {


    private static GetChargeInterface listener;

    public interface GetChargeInterface {
        public void BatteryCharge(String charge);
    }

    public void setListener(GetChargeInterface listener) {
        BatteryChargeReceiver .listener = listener;
    }



    @Override
    public void onReceive(Context context, Intent intent) {



            /*
                BatteryManager
                    The BatteryManager class contains strings and constants used for values in the
                    ACTION_BATTERY_CHANGED Intent, and provides a method for querying battery
                    and charging properties.
            */
            /*
                public static final String EXTRA_SCALE
                    Extra for ACTION_BATTERY_CHANGED: integer containing the maximum battery level.
                    Constant Value: "scale"
            */
        // Get the battery scale
        double scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        // Display the battery scale in TextView

            /*
                public static final String EXTRA_LEVEL
                    Extra for ACTION_BATTERY_CHANGED: integer field containing the current battery
                    level, from 0 to EXTRA_SCALE.

                    Constant Value: "level"
            */
        // get the battery level
        double level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        // Display the battery level in TextView

        // Calculate the battery charged percentage
        double percentage =(double) (level/ (double) scale)*100;

        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int averageCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            if (listener!=null) {
                listener.BatteryCharge(averageCurrent+"");
            }
        }else {

            if (listener != null) {

                listener.BatteryCharge(percentage + "%");
            }
        }

    }


}
