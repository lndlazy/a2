package com.pi.connectraspberry.util;

import android.content.Context;
import android.nfc.Tag;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pi.connectraspberry.MyApplication;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommUtils {


    private static final String TAG = "CommUtils";

    public static boolean isMainLooper() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static String getPhoneInfo() {

        String phoneInfo = "";
        try {
            int sdkInt = Build.VERSION.SDK_INT;
            String model = Build.MODEL;
            String brand = Build.BRAND;
            String device = Build.DEVICE;
            String product = Build.PRODUCT;
            String board = Build.BOARD;
            String manufacturer = Build.MANUFACTURER;
            String fingerprint = Build.FINGERPRINT;
            phoneInfo = brand + "," + sdkInt + "," + model + "," + device + "," + product + "," + manufacturer + "," + board + "," + fingerprint;
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

    // 自定义比较器
    static Comparator<String> comparator = (s1, s2) -> {
        // 提取字符串中的数字部分
        int num1 = extractNumber(s1);
        int num2 = extractNumber(s2);
        // 比较数字大小
        Log.d(TAG, "排序前::" + num1 + "::" + num2);
        return Integer.compare(num1, num2);
    };

    public static void sortListByNumber(List<String> list) {

        // 使用自定义比较器对集合进行排序
        Collections.sort(list, comparator);

        for (String s : list) {
            Log.d("排序后::tttt::", s);
        }

    }

    private static int extractNumber(String str) {
        // 找到下划线的位置
        try {
            int i = str.lastIndexOf(".");
            str = str.substring(0, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int index = str.lastIndexOf("_");
        if (index != -1) {
            // 提取下划线后面的数字部分
            String numberStr = str.substring(index + 1);
            try {
                // 将数字字符串转换为整数
                return Integer.parseInt(numberStr);
            } catch (Exception e) {
                // 如果转换失败，返回 0
                return 0;
            }
        }
        // 如果没有找到下划线，返回 0
        return 0;
    }

    public static String extractStr(String str) {
        // 找到下划线的位置
        try {
            int i = str.lastIndexOf(".");
            str = str.substring(0, i);
            Log.d(TAG, "裁剪后str：：" + str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int index = str.lastIndexOf("_");
        if (index != -1) {
            // 提取下划线后面的数字部分
            return str.substring(index + 1);
        }
        // 如果没有找到下划线，返回 0
        return "";
    }

}
