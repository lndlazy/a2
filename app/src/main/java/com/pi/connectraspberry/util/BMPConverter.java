package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BMPConverter {


    public static void convertToBMP(Bitmap bitmap, String outputPath) {
        try {

            File f = new File(outputPath);
            //如果文件不存在则创建文件
            if (!f.exists()) {
                f.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(outputPath);
            byte[] bmpHeader = createBMPHeader(bitmap);
            fos.write(bmpHeader);
            writePixelData(bitmap, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] createBMPHeader(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int fileSize = 54 + 3 * width * height;
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

        return header;
    }

    private static void writePixelData(Bitmap bitmap, FileOutputStream fos) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                fos.write((byte) Color.blue(pixel));
                fos.write((byte) Color.green(pixel));
                fos.write((byte) Color.red(pixel));
            }
        }
    }

}
