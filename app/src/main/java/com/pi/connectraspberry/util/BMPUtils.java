package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.Buffer;

public class BMPUtils {

    private static final String TAG = "BMPUtils";

    public static void convertToBMP(Bitmap bitmap, String outputPath) {

        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        byte[] rgb = addBMP_RGB_888(pixels, w, h);
        byte[] header = addBMPImageHeader(rgb.length);
        byte[] infos = addBMPImageInfosHeader(rgb, w, h);

        byte[] buffer = new byte[54 + rgb.length];
        System.arraycopy(header, 0, buffer, 0, header.length);
        System.arraycopy(infos, 0, buffer, 14, infos.length);
        System.arraycopy(rgb, 0, buffer, 54, rgb.length);

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(buffer);
        } catch (Exception e) {
            System.err.println("写入文件时发生异常: " + e.getMessage());
        }
//        try {
//            Log.d(TAG, "文件长度::" + buffer.length);
////            FileOutputStream fos = new FileOutputStream(outputPath);
////            fos.write(buffer);
//
//            if (FileOutputStream fos = new FileOutputStream(outputPath);
//            BufferedOutputStream bos = new Buffer())
//
//        } catch (Exception e) {
//// TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }


    //BMP文件头
    private static byte[] addBMPImageHeader(int size) {
        byte[] buffer = new byte[14];
        buffer[0] = 0x42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size);
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        buffer[10] = 0x36;
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        return buffer;
    }


    //BMP文件信息头
    private static byte[] addBMPImageInfosHeader(byte[] rgb, int w, int h) {
        byte[] buffer = new byte[40];
        buffer[0] = 0x28;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x00;
        buffer[4] = (byte) (w);
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);
        buffer[8] = (byte) (h);
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);  //23-26：位图高度
        buffer[12] = 0x01; // 颜色平面数
        buffer[13] = 0x00;    //28位
        buffer[14] = 0x18;// 每个像素的位数
        buffer[15] = 0x00;
        //31-34：压缩说明：有0（不压缩），1（RLE 8，8位RLE压缩），2（RLE 4，4位RLE压缩，3（Bitfields，位域存放）
        // 。RLE简单地说是采用像素数+像素值的方式进行压缩。T408采用的是位域存放方式，用两个字节表示一个像素，位域分配为r5b6g5。图中0300 0000为00000003h=3。
        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = 0x00;
        buffer[19] = 0x00;
        //35-38：用字节数表示的位图数据的大小，该数必须是4的倍数
        buffer[20] = (byte) (rgb.length);
        buffer[21] = (byte) (rgb.length >> 8);
        buffer[22] = (byte) (rgb.length >> 16);
        buffer[23] = (byte) (rgb.length >> 24);
//        buffer[20] = 0x00;
//        buffer[21] = 0x00;
//        buffer[22] = 0x00;
//        buffer[23] = 0x00;
        //39-42：水平分辨率，每秒的像素数，该数必须为0  A00F 0000为0000 0FA0h=4000
        buffer[24] = (byte) 0xE0;
        buffer[25] = 0x01;
        buffer[26] = 0x00;
        buffer[27] = 0x00;
//        buffer[24] = 0x00;
//        buffer[25] = 0x00;
//        buffer[26] = 0x00;
//        buffer[27] = 0x00;
        //43-46：垂直分辨率
//        buffer[28] = 0x02;
//        buffer[29] = 0x03;
        buffer[28] = 0x00;
        buffer[29] = 0x00;
        buffer[30] = 0x00;
        buffer[31] = 0x00;
        //47-50：位图使用的颜色索引数。设为0的话，则说明使用所有调色板项。
        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00;
        buffer[35] = 0x00;
        //51-54：对图象显示有重要影响的颜色索引的数目。如果是0，表示都重要。
        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00;
        buffer[39] = 0x00;
        return buffer;
    }


    private static byte[] addBMP_RGB_888(int[] b, int w, int h) {
        int len = b.length;
        System.out.println("长度:::" + len);
        byte[] buffer = new byte[w * h * 3];
        int offset = 0;
        for (int i = len - 1; i >= w; i -= w) {
//DIB文件格式最后一行为第一行，每行按从左到右顺序
            int end = i, start = i - w + 1;
            for (int j = start; j <= end; j++) {
                buffer[offset] = (byte) (b[j]);
                buffer[offset + 1] = (byte) (b[j] >> 8);
                buffer[offset + 2] = (byte) (b[j] >> 16);
                offset += 3;
            }
        }
        return buffer;
    }


}
