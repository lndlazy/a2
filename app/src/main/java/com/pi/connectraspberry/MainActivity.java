package com.pi.connectraspberry;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.pi.connectraspberry.service.EventMsg;
import com.pi.connectraspberry.service.MyService;
import com.pi.connectraspberry.util.ImageSender;
import com.pi.connectraspberry.util.ImageUtil;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.ThreadUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button tvChoose, tvSend, connect;
    private Button tvConvert, tvAuto, tvPre, tvNext;
    private ByRecyclerView tvRecyclerView;
    private BaseRecyclerAdapter<Uri> mAdapter;
    private Context context;
    private ProgressDialog progressDialog;

//    private ExecutorService parallelExecutor;

    private MyService mService;
    private boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Log.d(TAG, "  ======onCreate==== ");
        setContentView(R.layout.activity_main);
        initView();
        checkPermissionAndOpenGallery();

        EventBus.getDefault().register(this);

        connectService();

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        boolean empty = TextUtils.isEmpty("  ".trim());
        Log.d(TAG, "是否为空::" + empty);

//        timerCheck();
    }

//    private void timerCheck() {
//
////        Timer timer = new Timer();
//
//
//    }

    private void connectService() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "===service绑定成功===");
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "===service断开连接===");
            mBound = false;
        }
    };


    private void initView() {

        tvChoose = findViewById(R.id.tvChoose);
        tvSend = findViewById(R.id.tvSend);
        connect = findViewById(R.id.connect);
        tvConvert = findViewById(R.id.tvConvert);
        tvAuto = findViewById(R.id.tvAuto);
        tvNext = findViewById(R.id.tvNext);
        tvPre = findViewById(R.id.tvPre);
        tvChoose.setOnClickListener(v -> {
            openGallery();
        });
        connect.setOnClickListener(v -> {
            connectSocket();
        });
        tvConvert.setOnClickListener(v -> {
            convertPic();
        });
        tvAuto.setOnClickListener(v -> {
            auto();
        });
        tvNext.setOnClickListener(v -> {
            nextPic();
        });
        tvPre.setOnClickListener(v -> {
            prePic();
        });
        tvSend.setOnClickListener(v -> {
            sendPic();
        });

        noConnectStatus();
        tvRecyclerView = findViewById(R.id.tvRecyclerView);

        tvRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BaseRecyclerAdapter<Uri>(R.layout.item_pic, alreadyList) {
            @Override
            protected void bindView(BaseByViewHolder<Uri> holder, Uri uri, int position) {
//                holder.setText(R.id.tv_text, bean);

                ImageView imageView = holder.getView(R.id.img);

                Glide.with(MainActivity.this)
                        .load(uri)
                        .placeholder(R.mipmap.ic_loading)
                        .error(R.mipmap.ic_loading)
                        .dontAnimate() //加载没有任何动画
                        .into(imageView);

            }
        };

        tvRecyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(alreadyList);   // 设置第一页数据
    }

    private void prePic() {
        choosePrePic();
    }

    private void nextPic() {
        chooseNextPic();
    }

    private void auto() {
        autoPic();
    }

    /**
     * 转换图片
     */
    private void convertPic() {
        ThreadUtil.getParallelExecutor().execute(convertRunnable);
    }

    private void autoPic() {
        ThreadUtil.getParallelExecutor().execute(autoRunnable);
    }

    private void chooseNextPic() {
        ThreadUtil.getParallelExecutor().execute(nextRunnable);
    }

    private void choosePrePic() {
        ThreadUtil.getParallelExecutor().execute(preRunnable);
    }

    Runnable convertRunnable = () -> {
        boolean b = ImageSender.sendCommand(MyCommand.COMMAND_CONVERT);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable autoRunnable = () -> {
        boolean b = ImageSender.sendCommand(MyCommand.COMMAND_AUTO);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable preRunnable = () -> {
        boolean b = ImageSender.sendCommand(MyCommand.COMMAND_PRE);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable nextRunnable = () -> {
        boolean b = ImageSender.sendCommand(MyCommand.COMMAND_NEXT);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    private void showToast(String msg) {

        if (isMainLooper()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else
            runOnUiThread(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());

    }


    /**
     * 发送图片
     */
    private void sendPic() {

        if (alreadyList == null || alreadyList.size() == 0) {
            showToast("未选择图片");
            return;
        }

        if (!ImageSender.isConnect()) {
            showToast("未连接");
            return;
        }

        showProcess("发送中,请稍等...");

        ThreadUtil.getParallelExecutor().execute(() -> {

            Log.d(TAG, "开始发送,数量:" + alreadyList.size());
            try {
                for (Uri uri : alreadyList) {
                    String realPathFromURI = ImageUtil.getRealPathFromURI(context, uri);
                    boolean b = ImageSender.sendPic(realPathFromURI);
                    SystemClock.sleep(1000);
                    showToast(b ? "发送成功" : "发送失败");
                }
                Log.d(TAG, "发送完成");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "发送失败");
                showToast("发送失败");
            }
            hidProcess();
        });


    }

    private void showProcess(String msg) {

        if (isMainLooper()) {

            showAction(msg);
        } else {
            runOnUiThread(() -> {
                showAction(msg);
            });
        }

    }

    private void showAction(String msg) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(context);

        progressDialog.dismiss();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private void hidProcess() {

        if (progressDialog != null && progressDialog.isShowing()) {
            if (isMainLooper()) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            } else {

                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                });
            }
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String message) {

        // 处理事件
        if (TextUtils.isEmpty(message))
            return;


        if (message.startsWith("back:")) {
            String msg = message.substring(5);
            showToast(msg);
            return;
        }

        switch (message) {

            case EventMsg.CONNECT_SUCCESS:
                break;

            case EventMsg.CONNECT_SUCCESS_STATUS:// 显示连接成功状态
                connectSuccessStatus();
                break;

            case EventMsg.CONNECT_FAIL_STATUS:// 显示连接失败状态
                noConnectStatus();
                break;

            case EventMsg.CLEAR_PIC_DATA:// 清楚数据
                clearPicData();
                break;

        }

    }

    private void clearPicData() {
        try {
            alreadyList.clear();
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //连接socket
    private void connectSocket() {

        try {
            if (mService != null)
                mService.connectSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        parallelExecutor = ThreadUtil.getParallelExecutor();
//        parallelExecutor.execute(connectRunnable);
    }

    private boolean isMainLooper() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "===========onSaveInstanceState===========");

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "===========onRestoreInstanceState===========");


    }


    private Runnable connectRunnable = () -> {

        Log.d(TAG, "开始连接");
        if (ImageSender.isConnect()) {
            connectSuccessStatus();
            return;
        }

        boolean b = ImageSender.connectSocket(this::noConnectStatus);

        Log.d(TAG, "是否连接成功:" + b);
        if (b) {
            connectSuccessStatus();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    alreadyList.clear();
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else
            noConnectStatus();


    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        try {
//            EventBus.getDefault().unregister(this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
        Log.d(TAG, "===========onDestroy===========");
//            if (parallelExecutor != null)
//                parallelExecutor.shutdown();
//        } catch (Exception e) {
//            e.printStackTrace();
//            parallelExecutor = null;
//        }

        try {
            if (mBound && mConnection != null) {
                unbindService(mConnection);
                mBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ImageSender.closeSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void connectSuccessStatus() {

        if (isMainLooper()) {
            connect.setText("已连接");
            connect.setBackgroundColor(getResources().getColor(R.color.purple_500));
        } else

            runOnUiThread(() -> {
                connect.setText("已连接");
                connect.setBackgroundColor(getResources().getColor(R.color.purple_500));
            });

    }


    //未连接状态
    private void noConnectStatus() {

        if (isMainLooper()) {
            connect.setText("未连接");
            connect.setBackgroundColor(getResources().getColor(R.color.gray));

        } else
            runOnUiThread(() -> {
                connect.setText("未连接");
                connect.setBackgroundColor(getResources().getColor(R.color.gray));
            });
    }

    private static final int PICK_IMAGES = 1;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //openGallery();
            } else {
                // 权限被拒绝，显示提示信息
            }
        }
    }

    protected List<Uri> alreadyList = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "结果::" + requestCode + "," + resultCode);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                if (count > 0) {
                    alreadyList.clear();
                    Uri imageUri = data.getClipData().getItemAt(0).getUri();
                    // 处理每张图片的Uri
                    // Log.d(TAG, "多张图片,每张的url:" + imageUri);
                    if (!alreadyList.contains(imageUri))
                        alreadyList.add(imageUri);
                }

//                for (int i = 0; i < count; i++) {
//
//                    alreadyList.clear();
//
//                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
//                    // 处理每张图片的Uri
//                    Log.d(TAG, "多张图片,每张的url:" + imageUri);
//                    if (!alreadyList.contains(imageUri))
//                        alreadyList.add(imageUri);
//                }
            } else if (data.getData() != null) {

                alreadyList.clear();

                Uri imageUri = data.getData();
                // 处理单张图片的Uri
                Log.d(TAG, "单张图片,url:" + imageUri);
                if (!alreadyList.contains(imageUri))
                    alreadyList.add(imageUri);

            }
        }

        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();


        if (alreadyList != null && alreadyList.size() > 0) {
            for (Uri uri : alreadyList) {
                Log.d(TAG, "图片地址:" + ImageUtil.getRealPathFromURI(context, uri));
            }
        }

    }

}