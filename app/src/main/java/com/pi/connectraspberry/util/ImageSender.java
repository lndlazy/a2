package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

public class ImageSender {

    private static final String SERVER_IP = "10.0.0.1"; // 替换为树莓派的IP
    private static final int SERVER_PORT = 12345;
    private static final String TAG = "ImageSender";
    private static Socket socket;
    private static Timer timer;
    private static BufferedReader in;
    private static InputStream is;
    private static final int TIME_OUT = 8;

    private static boolean isRunning = true;

    private static SocketListener socketListener;

    static volatile long lastReceiveTime = System.currentTimeMillis();

    static Runnable backNewsRun = new Runnable() {
        @Override
        public void run() {
            byte[] receiveByte;
            int available;
            try {
                while (isRunning) {

                    long sTime = System.currentTimeMillis();
                    available = is.available();
                    long eTime = System.currentTimeMillis();
                    //Log.d(TAG, "当前时间:" + System.currentTimeMillis() +",上次时间:" + lastReceiveTime);
                    if ((System.currentTimeMillis() - lastReceiveTime) > 1000 * TIME_OUT) {
                        //3秒没有数据，说明是断开了
                        Log.d(TAG, "超时未接受到消息，断开," + "读取的时间间隔:" + (eTime - sTime) + "," + new Date().toLocaleString() + "," + new Date(lastReceiveTime).toLocaleString());
                        if (socketListener != null)
                            socketListener.onConnectLost();

                        closeSocket();
                        return;
                    }

                    if (available > 0) {

                        //接受到了消息
                        lastReceiveTime = System.currentTimeMillis();
                        receiveByte = new byte[available];
                        long ssT1 = System.currentTimeMillis();
                        int read = is.read(receiveByte);
                        long ssT2 = System.currentTimeMillis();
                        String messageFromServer = new String(receiveByte);
                        Log.d(TAG, "接受到了消息:" + messageFromServer + "读取间隔:" + (ssT2 - ssT1) + ",当前时间:" + new Date().toLocaleString() + ",上次" + new Date(lastReceiveTime).toLocaleString());

                        if (TextUtils.isEmpty(messageFromServer) || TextUtils.isEmpty(messageFromServer.trim())) {
                            SystemClock.sleep(1000);
                            continue;
                        }

                        Log.d(TAG, "发送eventbus");
                        EventBus.getDefault().post("back:" + messageFromServer);

                    }

                }
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

//            InputStream is = socket.getInputStream();
            is = socket.getInputStream();

            lastReceiveTime = System.currentTimeMillis();
            isRunning = true;
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

    public static boolean sendPic(String filePath) {

        try {

            if (!isConnect())
                return false;

            File file = new File(filePath); // 替换为实际图片路径
            Log.d(TAG, "发送的文件名称:" + file.getName());
            // 发送文件名
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//            byte[] fileNameBytes = file.getName().getBytes(StandardCharsets.UTF_8);

            // 发送文件名长度（4字节）
            //dos.write(intToByteArray(fileNameBytes.length));

            //发送文件名
//            dos.writeUTF(file.getName());
            dos.write(file.getName().getBytes(StandardCharsets.UTF_8));

//            dos.write('\n'); // 用换行符分隔文件名

            // 发送文件内容
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 5];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            // 发送结束符
//            dos.write("END".getBytes(StandardCharsets.UTF_8));
//            dos.write("".getBytes(StandardCharsets.UTF_8)); // 用换行符分隔文件名
            fis.close();
            dos.flush();
//            dos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 发送指令
     *
     * @param commandConvert
     */
    public static boolean sendCommand(String commandConvert) {

        if (!isConnect())
            return false;

        try {
            OutputStream outputStream = socket.getOutputStream();
            String str = "STR:" + commandConvert;
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            return true;
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
