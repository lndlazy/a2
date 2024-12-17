package com.pi.connectraspberry.util;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;

import com.pi.connectraspberry.MyApplication;
import com.pi.connectraspberry.mlogger.MLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";

//    /**
//     * 系统a2目录
//     *
//     * @return
//     */
//    public static String getBasePath() {
//        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/a2/";
//    }

    /**
     * 获取应用目录
     *
     * @return
     */
    public static String getLocalBasePath() {

        String path = MyApplication.getInstance().getFilesDir() + "/a2/";

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        return path;
    }


    public static String add_1(String fileName) {

        if (TextUtils.isEmpty(fileName))
            return "";

        //在fileName后缀名上加_1
        String[] split = fileName.split("\\.");
        if (split.length == 2) {
            return split[0] + "_1." + split[1];
        } else {
            //在最后一个.后面加_1
            int lastIndexOf = fileName.lastIndexOf(".");
            return fileName.substring(0, lastIndexOf) + "_1" + fileName.substring(lastIndexOf);
        }

    }


    public static boolean deleteFile(String name) {

        boolean success;
        File file = new File(name);

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


    /**
     * 判断当前文件夹下是否有该名称的文件
     *
     * @return
     */
    public static boolean isRepeat(File file, String fileName) {

        if (!file.exists()) {
            return false;
        }

        //判断当前file文件夹下是否有该fileName名称的文件
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().equals(fileName)) {
                    return true;
                }
            }
        }

        return false;

    }


    /**
     * 创建分类目录
     *
     * @param name
     * @return
     */
    public static boolean createFile(String name) {

        //创建文件夹
        File storageDir = new File(getLocalBasePath() + name);

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        return success;
    }

    /**
     * 获取分类目录
     *
     * @return
     */
    public static List<String> getFolderList() {

        List<String> folderList = new ArrayList<>();

        File folder = new File(getLocalBasePath());

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


    public static int deleteDirectory(File directory) {
        int code = 0;
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子目录，递归调用删除方法
                        deleteDirectory(file);
                    } else {
                        // 如果是文件，直接删除
                        if (file.delete()) {
                            Log.d("DeleteDir", "已删除文件: " + file.getAbsolutePath());
                        } else {
                            Log.e("DeleteDir", "无法删除文件: " + file.getAbsolutePath());
                        }
                    }
                }
            }

            // 删除空目录（在删除完所有文件和子目录后）
            if (directory.delete()) {
                Log.d("DeleteDir", "已删除目录: " + directory.getAbsolutePath());
            } else {
                code = -2;
                Log.e("DeleteDir", "无法删除目录: " + directory.getAbsolutePath());
            }
        } else {
            code = -1;
            Log.w("DeleteDir", "目录不存在: " + directory.getAbsolutePath());
        }

        return code;
    }

    /**
     * 拷贝文件到目标目录
     *
     * @param sourceFile      原图片文件
     * @param targetDirectory 目标目录
     * @param targetFileName  目标文件名称
     * @return
     */
    public static boolean copyPic2CurrentFile(File sourceFile, String targetDirectory, String targetFileName) {

        boolean isCopySuccess = false;

        File targetFile = new File(targetDirectory, targetFileName);

        try {

            InputStream inputStream = new FileInputStream(sourceFile);
            OutputStream outputStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            isCopySuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            MLogger.e("复制文件失败" + e.getMessage());
            isCopySuccess = false;
        }

        return isCopySuccess;
    }


    public static boolean isStandardPic(String filePath) {

        String imageExtension = getFileExtension(filePath);
        if (!"bmp".equalsIgnoreCase(imageExtension)) {
            //非bmp格式图片不处理
            return false;
        }

        return isSizeNormal(filePath);

    }

    public static String getFileExtension(String filePath) {
        if (filePath != null && filePath.lastIndexOf('.') != -1) {
            return filePath.substring(filePath.lastIndexOf('.') + 1);
        }
        return "";
    }

    private static boolean isSizeNormal(String bmpFilePath) {

        try {

            FileInputStream fis = new FileInputStream(bmpFilePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fis, null, options);
            fis.close();

            int width = options.outWidth;
            int height = options.outHeight;
            System.out.println("图片宽度: " + width + ", 图片高度: " + height);

            if (width < 2160 || height < 3060)
                return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
