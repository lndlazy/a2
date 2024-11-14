package com.pi.connectraspberry.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.pi.connectraspberry.MyApplication;

public class CommUtils {

    public static String getPhoneInfo() {

        String phoneInfo = "";
        try {
            int sdkInt = Build.VERSION.SDK_INT;
            String model = Build.MODEL;
            String brand = Build.BRAND;
            phoneInfo = sdkInt + "," + model + "," + brand;
            TelephonyManager telephonyManager = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String imei = telephonyManager.getImei();
                    phoneInfo = phoneInfo + "," + imei;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneInfo;
    }

}
