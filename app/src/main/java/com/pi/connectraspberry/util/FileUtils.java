package com.pi.connectraspberry.util;

import android.os.Environment;
import android.util.Log;

import com.pi.connectraspberry.mlogger.MLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static boolean deleteFile(String name) {

        boolean success;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/a2/" + name);

        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "已成功删除文件: " + name);
                MLogger.d("已成功删除文件: " + name);
                success = true;
            } else {
                success = false;
                Log.e(TAG, "无法删除文件: " + name);
                MLogger.e("无法删除文件: " + name);
            }
        } else {
            success = false;
            Log.w(TAG, "文件不存在: " + name);
            MLogger.e("文件不存在: " + name);

        }

        return success;
    }


    public static boolean createFile(String name) {

        //创建文件夹
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/a2/" + name);

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        return success;
    }


    public static List<String> getFolderList() {

        List<String> folderList = new ArrayList<>();

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/a2");

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子文件夹，递归调用删除方法
                        String name = file.getName();
                        Log.d(TAG, "文件名称：" + name);
                        folderList.add(name);

                    }
                }
            }

        } else {
            MLogger.e("遍历获取文件夹名称 a2 文件夹不存在");
        }

        return folderList;
    }


}
