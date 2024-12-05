package com.pi.connectraspberry.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {


    /**
     * 获取指定文件的MD5值
     *
     * @param file 要获取MD5值的文件
     * @return 文件的MD5值，若获取失败返回null
     */
    public static String getMD5(File file) {

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(file);
                 DigestInputStream dis = new DigestInputStream(fis, md)) {
                byte[] buffer = new byte[1024];
                while (dis.read(buffer) > -1) {
                    // 持续读取文件内容到缓冲区，同时更新MessageDigest对象
                }
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    // 将每个字节转换为十六进制字符串并添加到StringBuilder
                    sb.append(String.format("%02x", b & 0xff));
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

}
