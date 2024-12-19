package com.pi.connectraspberry.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pi.connectraspberry.R;
import com.pi.connectraspberry.util.SocketSender;
import com.pi.connectraspberry.util.ThreadUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;

public class MyService extends Service {

    private static String TAG = "MyService";

    private ExecutorService parallelExecutor;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知渠道，针对Android 8.0及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "channel_id",
                    "Boundless",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        } else {
            Notification notification = new NotificationCompat.Builder(this, "channel_id")
                    .setContentTitle("Boundless")
//                .setContentText("Service is running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(1, notification);

        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                while (true) {
//
//                    try {
//
////                        ExecutorService parallelExecutor = ThreadUtil.getParallelExecutor();
////
////                        boolean terminated = parallelExecutor.isTerminated();
////                        boolean shutdown = parallelExecutor.isShutdown();
//
////                        Log.d(TAG, "线程池状态:" + terminated + ",shutdown:" + shutdown);
//                        //Log.d(TAG, "检测 检测 xxxxxx :");
//
//                        SystemClock.sleep(1000 * 2);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        }).start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        //确保服务运行在前台
//        Notification notification = new Notification.Builder(this, "channel_id")
//                .setContentTitle("交通安全管控正在运行")
//                .setContentText("正在运行")
//                .setSmallIcon(R.drawable.ic_launcher) // 设置图标，需替换为实际资源ID
//                .build();
//        startForeground(CORE_SERVICE_ID, notification);
//        stopSelf();
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //连接socket
    public void connectSocket() {
        parallelExecutor = ThreadUtil.getParallelExecutor();
        parallelExecutor.execute(connectRunnable);
    }


    private Runnable connectRunnable = () -> {

        Log.d(TAG, "开始连接");
        if (SocketSender.isConnect()) {
            Log.d(TAG, "已连接，不用再连接");
            sendConnectSuccess();
            return;
        }

        boolean b = SocketSender.connectSocket(this::noConnect);

        Log.d(TAG, "是否连接成功:" + b);
        if (b) {
            sendConnectSuccess();
            //EventBus.getDefault().post(EventMsg.CLEAR_PIC_DATA);

        } else
            noConnect();

    };

    private void sendConnectSuccess() {
        EventBus.getDefault().post(EventMsg.CONNECT_SUCCESS_STATUS);
    }

    private void noConnect() {
        EventBus.getDefault().post(EventMsg.CONNECT_FAIL_STATUS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "===========onDestroy===========");
            if (parallelExecutor != null)
                parallelExecutor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            parallelExecutor = null;
        }

    }
}