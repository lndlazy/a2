package com.pi.connectraspberry.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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


    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 创建Matrix对象
        Matrix matrix = new Matrix();
        // 设置缩放比例
        matrix.postScale(scaleWidth, scaleHeight);
        // 生成新的Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


    public static void saveBitmapToFile(Bitmap bitmap, File imageFile) {
        // 获取外部存储目录，通常是SD卡
//        File directory = Environment.getExternalStorageDirectory();
//        // 创建保存文件的目录
//        File imageFolder = new File(directory, UUID.randomUUID().toString() "MyImages");
//        if (!imageFolder.exists()) {
//            imageFolder.mkdirs();
//        }
//        // 创建保存文件
//        File imageFile = new File(imageFolder, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            // 保存为PNG格式，可根据需要修改为JPEG等格式
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//        public static void main(String[] args) {
//            // 假设这里有一个图片文件的路径
//            String imagePath = "path/to/your/image.jpg";
//            // 加载原始图片
//            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
//            // 设定保存的文件名
//            String fileName = "saved_image.png";
//            saveBitmapToFile(originalBitmap, fileName);
//        }


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
