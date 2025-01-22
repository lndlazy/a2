package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BMPConverter {


    private static final String TAG = "BMPConverter";

    public static void convertToBMP(Bitmap source, String outputPath) {
        try {

            File f = new File(outputPath);
            //如果文件不存在则创建文件
            if (!f.exists()) {
                f.createNewFile();
                Log.d(TAG, "文件不存在，创建文件");
            }

            Bitmap.Config config = Bitmap.Config.ARGB_8888; // 24位
            Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), config);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(source, 0, 0, null);


            FileOutputStream fos = new FileOutputStream(outputPath);
            byte[] bmpHeader = createBMPHeader(bitmap);
            fos.write(bmpHeader);
            Log.d(TAG, "写入头信息成功");
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pixels = new int[w * h];
//            byte[] bytes = addBMP_RGB_8888(pixels, w, h);
            writePixelData(bitmap, fos);

//            fos.write(bytes);
            Log.d(TAG, "写入图片数据成功");

            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void convertToBMP(String bitmapPath, String outputPath) {
//        try {
//
//            File f = new File(outputPath);
//            //如果文件不存在则创建文件
//            if (!f.exists()) {
//                f.createNewFile();
//                Log.d(TAG, "文件不存在，创建文件");
//            }
//
//
//
//            FileOutputStream fos = new FileOutputStream(outputPath);
//            byte[] bmpHeader = createBMPHeader(bitmap);
//            fos.write(bmpHeader);
//            Log.d(TAG, "写入头信息成功");
//            int w = bitmap.getWidth();
//            int h = bitmap.getHeight();
//            int[] pixels = new int[w * h];
//            byte[] bytes = addBMP_RGB_8888(pixels, w, h);
//            //writePixelData(bitmap, fos);
//
//            fos.write(bytes);
//            Log.d(TAG, "写入图片数据成功");
//
//            fos.flush();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static byte[] createBMPHeader(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int fileSize = 54 + 3 * width * height;
        Log.d(TAG, " 文件 长度 ::: " + fileSize + ", 宽：" + width + ",高: " + height);
        byte[] header = new byte[54];

        // BMP文件头
        header[0] = 'B';
        header[1] = 'M';
        header[2] = (byte) (fileSize);
        header[3] = (byte) (fileSize >> 8);
        header[4] = (byte) (fileSize >> 16);
        header[5] = (byte) (fileSize >> 24);
        header[10] = 54; // 数据偏移
        header[14] = 40; // 信息头大小
        header[18] = (byte) (width);
        header[19] = (byte) (width >> 8);
        header[20] = (byte) (width >> 16);
        header[21] = (byte) (width >> 24);
        header[22] = (byte) (height);
        header[23] = (byte) (height >> 8);
        header[24] = (byte) (height >> 16);
        header[25] = (byte) (height >> 24);
        header[26] = 1;  // 颜色平面数
        header[28] = 24; // 每个像素的位数
//        header[34] = (byte) (width * height); // 每个像素的位数

        Log.d(TAG, "   设置头信息成功  ");
        return header;
//        ByteBuffer buffer = ByteBuffer.allocate(54);
//
//        // BMP 文件头
//        buffer.put((byte) 'B');
//        buffer.put((byte) 'M');
//        buffer.putInt(fileSize);
//        buffer.putInt(0);
//        buffer.putInt(54);
//        buffer.putInt(40);
//        buffer.putInt(width);
//        buffer.putInt(height);
//        buffer.putShort((short) 1);
//        buffer.putShort((short) 24);
//        buffer.putShort((short) 0);
//        buffer.putInt(0);
//        buffer.putInt(0);
//        buffer.putInt(0);
//        buffer.putInt(0);
//
//        return buffer.array();
    }

    private static void writePixelData(Bitmap bitmap, FileOutputStream fos) throws Exception {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Log.d(TAG, "图片的宽高::" + width + ",heigh：：" + height);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
//                fos.write((byte) Color.blue(pixel));
//                fos.write((byte) Color.green(pixel));
//                fos.write((byte) Color.red(pixel));


                fos.write((byte) Color.red(pixel));
                fos.write((byte) Color.green(pixel));
                fos.write((byte) Color.blue(pixel));
//                int alpha = Color.alpha(color);
            }
        }
    }


    private static byte[] addBMP_RGB_8888(int[] b, int w, int h) {


        int len = b.length;
        byte[] buffer = new byte[w * h * 3];
        int offset = 0;
        for (int i = len - 1; i >= w; i -= w) {

            int end = i, start = i - w + 1;

            for (int j = start; j <= end; j++) {

                buffer[offset] = (byte) (b[j] >> 0);
                buffer[offset + 1] = (byte) (b[j] >> 8);
                buffer[offset + 2] = (byte) (b[j] >> 16);
                offset += 3;
            }

        }


        return buffer;


    }


}
