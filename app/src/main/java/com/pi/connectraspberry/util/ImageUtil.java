package com.pi.connectraspberry.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {

    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }


//    public static void convertImageToBmp(String inputImagePath, String outputBmpPath) {
//        try {
//            // 从文件路径读取原始图片
//            Bitmap originalBitmap = BitmapFactory.decodeFile(inputImagePath);
//            File outputFile = new File(outputBmpPath);
//            FileOutputStream fos = new FileOutputStream(outputFile);
//            // 将原始图片压缩为BMP格式并保存
//            originalBitmap.compress(Bitmap.CompressFormat.BMP, 100, fos);
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 保存树莓派发送过来的图片
     *
     * @param context
     * @param bitmap
     * @param imageFileName
     */
    public static void saveImageToGallery(Context context, Bitmap bitmap, String imageFileName) {
        String savedImagePath = null;
        //String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/a2");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddPic(context, savedImagePath);
        }
    }

    /**
     * 图片添加到系统相册
     *
     * @param context
     * @param imagePath
     */
    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    public static String getPathFromUri(Context context, Uri uri) {
        String path = null;
        if ("content".equals(uri.getScheme())) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        path = cursor.getString(columnIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        } else if ("file".equals(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }

    public static List<String> findBMPImages(String targetDirectory) {
        List<String> bmpImagePaths = new ArrayList<>();
        File directory = new File(targetDirectory);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isBMPFile(file)) {
                        bmpImagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return bmpImagePaths;
    }

    private static boolean isBMPFile(File file) {
        String fileName = file.getName();
        return fileName.toLowerCase().endsWith(".bmp");
    }

}
