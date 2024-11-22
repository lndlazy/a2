package com.pi.connectraspberry.util;

import android.widget.Toast;

import com.pi.connectraspberry.MyApplication;

public class MToast {


    public static void show(String msg) {


        if (CommUtils.isMainLooper())
            Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();


    }
}
