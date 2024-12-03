package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.pi.connectraspberry.MyApplication;
import com.pi.connectraspberry.bean.ConfigBean;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

public class SocketSender {

    private static final String SERVER_IP = "10.0.0.1"; // 替换为树莓派的IP
    private static final int SERVER_PORT = 12345;
    private static final String TAG = "ImageSender";
    private static Socket socket;
    private static Timer timer;
    private static BufferedReader in;
    private static InputStream is;
    private static final int TIME_OUT = 5;

    private static boolean isRunning = true;

    private static SocketListener socketListener;

    static volatile long lastReceiveTime = System.currentTimeMillis();


    static int disconnectCount = 0;

    /**
     * 接受消息
     */
    static Runnable backNewsRun = new Runnable() {
        @Override
        public void run() {
            byte[] receiveByte;
            int available;
            try {
                while (isRunning) {

                    long l00 = System.currentTimeMillis();
                    byte[] cmdTypeBuffer = new byte[9];
                    int read3 = is.read(cmdTypeBuffer);
                    long l01 = System.currentTimeMillis();
                    Log.d(TAG, "read3的长度:" + read3 + ",时间间隔:" + (l01 - l00));

                    if (System.currentTimeMillis() - lastReceiveTime > TIME_OUT * 1000) {
                        Log.e(TAG, " 超时   断开连接 ????   ");
                        disconnectCount++;
                    }

                    if (disconnectCount > 3) {
                        Log.e(TAG, "  超过三次，断开连接   ");
                        closeSocket();
                        if (socketListener != null)
                            socketListener.onConnectLost();
                        return;
                    }

                    if (read3 > 0) {
                        disconnectCount = 0;
                        //接受到了消息
                        lastReceiveTime = System.currentTimeMillis();
                        String messageFromServer = new String(cmdTypeBuffer);

//                        Log.d(TAG, "接受到了消息:" + messageFromServer + "读取间隔:" + (ssT2 - ssT1) + ",当前时间:" + new Date().toLocaleString() + ",上次" + new Date(lastReceiveTime).toLocaleString());
                        Log.d(TAG, "接受到了消息:" + messageFromServer);

                        if (TextUtils.isEmpty(messageFromServer) || TextUtils.isEmpty(messageFromServer.trim())) {
                            // SystemClock.sleep(1000);
                            continue;
                        }

                        if (MyCommand.HEART.equals(messageFromServer)) {
                            Log.d(TAG, "心跳 -> " + new Date().toLocaleString());

                        } else if (MyCommand.CMD_TOA.equals(messageFromServer)) {
                            //吐司
                            byte[] lengthBuffer = new byte[4];
                            int r = is.read(lengthBuffer);
                            int cmdLength = ByteBuffer.wrap(lengthBuffer).getInt();
                            // 接收图片数据
                            byte[] cmdBuffer = new byte[cmdLength];
                            int read2 = is.read(cmdBuffer);
                            String notice = new String(cmdBuffer);
                            Log.d(TAG, "发送eventbus=>" + notice);
                            EventBus.getDefault().post("back:" + notice);

                        } else if (MyCommand.CMD_IMG.equals(messageFromServer)) {//图片指令

                            //接受文件名长度
                            byte[] nameLen = new byte[4];
                            is.read(nameLen);
                            int nameLength = ByteBuffer.wrap(nameLen).getInt();
                            Log.d(TAG, "文件名长度====>" + nameLength);
                            // 接收图片名称数据
                            byte[] nameLenBuffer = new byte[nameLength];
                            is.read(nameLenBuffer);
                            String fileName = new String(nameLenBuffer);
                            Log.d(TAG, "文件名称:" + fileName);

                            // 接收图片数据长度
                            byte[] lengthBuffer = new byte[4];
                            int read1 = is.read(lengthBuffer);
                            Log.d(TAG, "read1====>" + read1);
                            int imageLength = ByteBuffer.wrap(lengthBuffer).getInt();
                            Log.d(TAG, "图片长度imageLength====>" + imageLength);
                            // 接收图片数据
                            byte[] imageBuffer = new byte[imageLength];
                            int read2 = is.read(imageBuffer);
                            Log.d(TAG, " 读取图片结束:: " + read2);
                            //保存图片到相册
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBuffer, 0, imageBuffer.length);
                            ImageUtil.saveImageToGallery(MyApplication.getInstance(), bitmap, fileName);

                        } else if (MyCommand.CMD_CMD.equals(messageFromServer)) {
                            // 接收指令数据长度
                            //long l1 = System.currentTimeMillis();
                            byte[] lengthBuffer = new byte[4];
                            // long l2 = System.currentTimeMillis();
                            int r = is.read(lengthBuffer);
//                            long l3 = System.currentTimeMillis();
                            //Log.d(TAG, "r====>" + r);
                            int cmdLength = ByteBuffer.wrap(lengthBuffer).getInt();
//                            long l4 = System.currentTimeMillis();
                            //Log.d(TAG, "指令长度cmdLength====>" + cmdLength);
                            // 接收图片数据
                            byte[] cmdBuffer = new byte[cmdLength];
                            int read2 = is.read(cmdBuffer);
//                            long l5 = System.currentTimeMillis();
                            String cmd = new String(cmdBuffer);
//                            long l6 = System.currentTimeMillis();
                            //Log.d(TAG, " 指令内容:: " + cmd);

//                            if ("HEART".equals(cmd)) {
//                                Log.d(TAG, "心跳 -> " + new Date().toLocaleString() + ",时间间隔" + (l2 - l1) + "," + (l3 - l2) + "," + (l4 - l3) + "," + (l5 - l4) + "," + (l6 - l5) + "," + (l6 - l1));
//                            } else {

//                            }

                        }

                    }

                }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    public static boolean connectSocket(SocketListener listener) {

        try {
            socketListener = listener;

            if (socket == null)
                socket = new Socket(SERVER_IP, SERVER_PORT);
            // 接收心跳包
            if (timer == null)
                timer = new Timer();

            disconnectCount = 0;
//            InputStream is = socket.getInputStream();
            is = socket.getInputStream();

            //发送手机型号等信息
            String phoneInfo = CommUtils.getPhoneInfo();
            Log.d(TAG, "手机信息:" + phoneInfo);

            sendCommand(MyCommand.mobileInfo + phoneInfo);
//            dos.write(("STR:mobile:" + phoneInfo).getBytes(StandardCharsets.UTF_8));

            lastReceiveTime = System.currentTimeMillis();
            isRunning = true;
            ExecutorService executor = ThreadUtil.getParallelExecutor();
            executor.execute(backNewsRun);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean isConnect() {

        if (socket == null)
            return false;

        return socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown();

    }

    public static boolean sendPic(String picName, String picPath) throws Exception {

        if (!isConnect())
            return false;

        File file = new File(picPath); // 替换为实际图片路径

        if (TextUtils.isEmpty(picName))
            picName = file.getName();

        Log.d(TAG, "发送的文件名称:" + picName + ",文件路径:" + file.getAbsolutePath());

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        //图片文件大小
        long imgLength = file.length();
        //发送类型
        dos.write(MyCommand.PIC_START.getBytes(StandardCharsets.UTF_8));
        //发送文件名长度
        dos.write(longToByteArray(picName.length()));
        //发送文件名
        dos.write(picName.getBytes(StandardCharsets.UTF_8));
        //发送文件长度
        Log.d(TAG, "图片文件长度" + imgLength);
        dos.write(longToByteArray(imgLength));
        //发送文件内容
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }
        fis.close();
        dos.flush();
        return true;


    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public static byte[] longToByteArray(long value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) value;
        return byteArray;
    }

    /**
     * 发送指令
     *
     * @param msg
     */
    public static boolean sendCommand(String msg) {

        if (!isConnect())
            return false;

        try {
            if (TextUtils.isEmpty(msg))
                return false;

            OutputStream outputStream = socket.getOutputStream();
            //String str = "STR:" + commandConvert;
            //命令开始
            outputStream.write(MyCommand.CMD_START.getBytes(StandardCharsets.UTF_8));
            //指令长度
            outputStream.write(longToByteArray(msg.length()));
            //指令内容
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean sendConfig() {

        try {

            ConfigBean configBean = new ConfigBean();
            configBean.setSeconds(SpUtils.getConfig(SpUtils.PLAY_INTERVAL, 0));
            configBean.setHue(SpUtils.getConfig(SpUtils.HUE, 0));
            configBean.setSat(SpUtils.getConfig(SpUtils.SAT, 0));
            configBean.setBright(SpUtils.getConfig(SpUtils.BRIGHT, 0));
            configBean.setContrast(SpUtils.getConfig(SpUtils.CONTRAST, 0));
            configBean.setAuto(true);

            String jsonString = JSONObject.toJSONString(configBean);
            Log.d(TAG, "发送的配置信息:" + jsonString);
            return sendCommand(MyCommand.configInfo + jsonString);


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public static void closeSocket() {

        try {

            isRunning = false;
            Log.d(TAG, "关闭连接");
            if (socket != null) {
                socket.close();
            }

            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket = null;
            timer = null;
        }

    }


//    public static void convertToBmp(String inputPath, String outputPath) {
//        try {
//            Bitmap bitmap = BitmapFactory.decodeFile(inputPath);
//            FileOutputStream fos = new FileOutputStream(outputPath);
//            bitmap.compress(Bitmap.CompressFormat.BMP, 100, fos);
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("ImageConverter", "转换失败：" + e.getMessage());
//        }
//    }

}
