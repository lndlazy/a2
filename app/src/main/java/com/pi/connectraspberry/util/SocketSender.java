package com.pi.connectraspberry.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pi.connectraspberry.MyApplication;
import com.pi.connectraspberry.bean.ConfigBean;
import com.pi.connectraspberry.bean.FolderBean;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
                    //Log.d(TAG, "read3的长度:" + read3 + ",时间间隔:" + (l01 - l00));

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

                        String messageFromServer = new String(cmdTypeBuffer);

//                        Log.d(TAG, "接受到了消息:" + messageFromServer + "读取间隔:" + (ssT2 - ssT1) + ",当前时间:" + new Date().toLocaleString() + ",上次" + new Date(lastReceiveTime).toLocaleString());

                        if (TextUtils.isEmpty(messageFromServer) || TextUtils.isEmpty(messageFromServer.trim())) {
                            // SystemClock.sleep(1000);
                            continue;
                        }

                        disconnectCount = 0;
                        //接受到了消息
                        lastReceiveTime = System.currentTimeMillis();
                        if (!"CMD_HEART".equals(messageFromServer))
                            Log.d(TAG, "接受到了消息:" + messageFromServer);

                        if (MyCommand.HEART.equals(messageFromServer)) {
                            //Log.d(TAG, "心跳 -> " + new Date().toLocaleString());

                        } else if (MyCommand.CMD_TOA.equals(messageFromServer)) {
                            showToast();

                        } else if (MyCommand.CMD_IMG.equals(messageFromServer)) {//图片指令

                            picCommand();

                        } else if (MyCommand.CMD_CMD.equals(messageFromServer)) {
                            // 接收指令数据长度
                            cmdCommand();

                        } else if (MyCommand.CMD_FOL.equals(messageFromServer)) {

                            folderCmd();
                        } else if (MyCommand.LOG_START.equals(messageFromServer)) {

                            //提取日志
                            extractionLog();

                        }

                    }

                }
//                }
            } catch (Exception e) {

                if (socketListener != null)
                    socketListener.onConnectLost();

                e.printStackTrace();
            }

        }
    };

    private static void extractionLog() {

        //接受日志文件名长度
        try {
            byte[] nameLen = new byte[4];
            is.read(nameLen);
            int nameLength = ByteBuffer.wrap(nameLen).getInt();
            Log.d(TAG, "日志文件名长度====>" + nameLength);
            // 接收日志名称数据
            byte[] nameLenBuffer = new byte[nameLength];
            is.read(nameLenBuffer);
            String logFileName = new String(nameLenBuffer);
            Log.d(TAG, "日志文件名称:" + logFileName);

            // 接收日志数据长度
            byte[] lengthBuffer = new byte[4];
            int read1 = is.read(lengthBuffer);
            int logDataLength = ByteBuffer.wrap(lengthBuffer).getInt();
            Log.d(TAG, "日志内容长度====>" + logDataLength + ",read1:" + read1);
            // 接收图片数据
            byte[] logBuffer = new byte[logDataLength];

            int offset = 0;
            int bytesRead;
            while ((bytesRead = is.read(logBuffer, offset, logBuffer.length - offset))!= -1) {
                offset += bytesRead;
                if (offset >= logBuffer.length) {
                    break;
                }
            }

            byte[] result;
            if (offset < logBuffer.length) {
                result = new byte[offset];
                System.arraycopy(logBuffer, 0, result, 0, offset);
            } else {
                result = logBuffer;
            }

            //int read2 = is.read(logBuffer);
            Log.d(TAG, " 读取日志内容结束:: " + result.length);
            //保存压缩文件到本地
            FileUtils.saveTextToFile(result, logFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private static void folderCmd() {
        try {
            Log.d(TAG, "接受到了图片的md5值");
            // 接收指令数据长度
            byte[] lengthBuffer = new byte[4];
            int r = is.read(lengthBuffer);
            int cmdLength = ByteBuffer.wrap(lengthBuffer).getInt();
            Log.d(TAG, "md5内容长度:" + cmdLength);
            //
            byte[] cmdBuffer = new byte[cmdLength];
            int read2 = is.read(cmdBuffer);
            //获取指令内容 文件夹内图片的md5值
            String md5Json = new String(cmdBuffer);
            Log.d(TAG, "文件夹内图片内容: " + md5Json);
            // ["fce429a4c0ad0aeca5cfd07f2f023299"]
            Map<String, Object> map2 = JSON.parseObject(md5Json, new TypeReference<Map<String, Object>>() {
            });

            List<String> keyList2 = new ArrayList<>(map2.keySet());
            //List<String> md5List = JSONObject.parseArray(md5Json, String.class);
            //FolderBean folderBean = JSONObject.parseObject(md5Json, FolderBean.class);
            EventBus.getDefault().post(keyList2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cmdCommand() {
        try {
            byte[] lengthBuffer = new byte[4];
            int r = is.read(lengthBuffer);
            int cmdLength = ByteBuffer.wrap(lengthBuffer).getInt();
            //
            byte[] cmdBuffer = new byte[cmdLength];
            int read2 = is.read(cmdBuffer);
            //获取指令内容
            String cmd = new String(cmdBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void picCommand() {
        //接受文件名长度
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showToast() throws IOException {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

    /**
     *
     * @param folderName 文件夹名称
     * @param picName 图片名称
     * @param picPath 图片路径
     * @return
     * @throws Exception
     */
    public static boolean sendPic(String folderName, String picName, String picPath) throws Exception {

        if (!isConnect())
            return false;

        File file = new File(picPath); // 替换为实际图片路径

        if (TextUtils.isEmpty(picName))
            picName = file.getName();

        Log.d(TAG, "文件夹名称:" + folderName + ",发送的文件名称:" + picName + ",文件路径:" + file.getAbsolutePath());

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        //图片文件大小
        long imgLength = file.length();
        //发送类型
        dos.write(MyCommand.PIC_START.getBytes(StandardCharsets.UTF_8));
        //发送文件夹名长度
        dos.write(longToByteArray(folderName.length()));
        //发送文件夹名
        dos.write(folderName.getBytes(StandardCharsets.UTF_8));
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

    /**
     * 发送图片的md5值
     * @param folderName 文件夹名称
     * @param picName 图片名称
     * @param picMd5 图片md5值
     * @return
     */
    public static boolean sendPicMd5(String folderName, String picName, String picMd5) throws Exception {

        if (!isConnect())
            return false;

        Log.d(TAG, "发送的文件名称:" + picName + ",文件md5:" + picMd5 + ",文件夹名称:" + folderName);

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        //发送类型
        dos.write(MyCommand.MD5_START.getBytes(StandardCharsets.UTF_8));
        //发送文件夹名长度
        dos.write(longToByteArray(folderName.length()));
        //发送文件夹名
        dos.write(folderName.getBytes(StandardCharsets.UTF_8));
        //发送文件名长度
        dos.write(longToByteArray(picName.length()));
        //发送文件名
        dos.write(picName.getBytes(StandardCharsets.UTF_8));
        //发送md5长度
        int md5Length = picMd5.length();
        Log.d(TAG, "图片md5长度" + md5Length);
        dos.write(longToByteArray(md5Length));
        //发送md5值
        dos.write(picMd5.getBytes(StandardCharsets.UTF_8));
        dos.flush();
        return true;

    }


    /**
     * 获取raspberry的folder文件夹内的所有图片
     * @param folder
     * @return
     */
    public static boolean getFolderImgs(String folder) {
        try {
            return sendCommand(MyCommand.getFolderImgs + folder);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public static boolean sendConfig(ConfigBean configBean) {

        try {

            String jsonString = JSONObject.toJSONString(configBean);
            Log.d(TAG, "发送的配置信息:" + jsonString);
            return sendCommand(MyCommand.configInfo + jsonString);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public static boolean sendCreateFolder(String fileName) {

        try {
            return sendCommand(MyCommand.createFolder + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean sendDeleteFolder(String fileName) {

        try {
            return sendCommand(MyCommand.deleteFolder + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

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

    /**
     * 清空文件夹内的图片
     */
    public static void sendClearPic(String fileName) {

        if (!isConnect())
            return;

        try {
            if (TextUtils.isEmpty(fileName))
                return;

            OutputStream outputStream = socket.getOutputStream();
            String msg = MyCommand.clearFolder + fileName;
            //命令开始
            outputStream.write(MyCommand.CMD_START.getBytes(StandardCharsets.UTF_8));
            //指令长度
            outputStream.write(longToByteArray(msg.length()));
            //指令内容
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();

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
