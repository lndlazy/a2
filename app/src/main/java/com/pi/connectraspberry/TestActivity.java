package com.pi.connectraspberry;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;
import com.pi.connectraspberry.callback.MyItemTouchHelperCallback;
import com.pi.connectraspberry.service.EventMsg;
import com.pi.connectraspberry.service.MyService;
import com.pi.connectraspberry.ui.BaseActivity;
import com.pi.connectraspberry.ui.ClassifyActivity;
import com.pi.connectraspberry.ui.SettingActivity;
import com.pi.connectraspberry.util.CommUtils;
import com.pi.connectraspberry.util.FileUtils;
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
import pub.devrel.easypermissions.EasyPermissions;

public class TestActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "TestActivity";
    private static final int RC_CAMERA_PERM = 33;
    private ByRecyclerView tvRecyclerView;
    private EditText etSecond;
    private ImageView ivPreview;
    private MyService mService;
    private boolean mBound = false;

    String[] pers = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    private ImageView ivWifi;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }


    @Override
    protected void initView() {
        //请求权限
        mRequestPermission();

        ConstraintLayout clWifi = findViewById(R.id.clWifi);
        ImageView ivSend = findViewById(R.id.ivSend);
        ivWifi = findViewById(R.id.ivWifi);
        ImageView ivSetting = findViewById(R.id.ivSetting);
        ImageView ivSelect = findViewById(R.id.ivSelect);
        ImageView ivClassify = findViewById(R.id.ivClassify);
        ImageView ivConvert = findViewById(R.id.ivConvert);
        ivPreview = findViewById(R.id.ivPreview);
        ImageView ivPre = findViewById(R.id.ivPre);
        ImageView ivNext = findViewById(R.id.ivNext);
        TextView tvSlide = findViewById(R.id.tvSlide);
        etSecond = findViewById(R.id.etSecond);
        tvRecyclerView = findViewById(R.id.tvRecyclerView);
        initRecyclerView();

        clWifi.setOnClickListener(this);
        ivSend.setOnClickListener(this);
        ivSelect.setOnClickListener(this);
        ivConvert.setOnClickListener(this);
        tvSlide.setOnClickListener(this);
        ivPre.setOnClickListener(this);
        ivPreview.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivSetting.setOnClickListener(this);
        ivClassify.setOnClickListener(this);

    }

    protected void initData() {
        //获取手机信息
        String phoneInfo = CommUtils.getPhoneInfo();

        EventBus.getDefault().register(this);

        connectService();

        Intent intent = new Intent(this, MyService.class);
        startService(intent);

    }


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

            case EventMsg.CLEAR_PIC_DATA:// 清除数据
                clearPicData();
                break;

        }

    }


    private void showPic(ImageView iv, String uri) {
        Glide.with(context)
                .load(uri)
                .placeholder(R.mipmap.ic_loading)
                .error(R.mipmap.ic_loading)
                .dontAnimate() //加载没有任何动画
                .into(iv);
    }


    private void showPic(ImageView iv, int resId) {
        Glide.with(context)
                .load(resId)
                .placeholder(R.mipmap.ic_loading)
                .error(R.mipmap.ic_loading)
                .dontAnimate() //加载没有任何动画
                .into(iv);
    }

    public void connectSuccessStatus() {

        if (CommUtils.isMainLooper()) {
            showPic(ivWifi, R.mipmap.ic_wifi_blue);

        } else
            runOnUiThread(() -> {
                showPic(ivWifi, R.mipmap.ic_wifi_blue);
            });

    }

    //未连接状态
    private void noConnectStatus() {

        if (CommUtils.isMainLooper()) {
            showPic(ivWifi, R.mipmap.ic_wifi_gray);

        } else
            runOnUiThread(() -> {
                showPic(ivWifi, R.mipmap.ic_wifi_gray);
            });
    }

    private void clearPicData() {
        try {
            alreadyList.clear();
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 申请权限
     */
    private void mRequestPermission() {

        //EasyPermissions.hasPermissions()
        if (EasyPermissions.hasPermissions(this, pers)) {
            // 权限已授予，执行相关操作
            //startCamera();
        } else {
            // 请求权限
            EasyPermissions.requestPermissions(this, "需要相机权限来拍照",
                    RC_CAMERA_PERM, pers);
        }
    }


    private MyImageAdapter mAdapter;
    protected List<String> alreadyList = new ArrayList<>();

    private float originalScale;
    private boolean isDragging;


    public static class MyImageAdapter extends BaseRecyclerAdapter<String> {

        public void restoreImageSize() {
            try {
                if (currentHolder != null) {
                    ImageView iv = currentHolder.itemView.findViewById(R.id.img);
                    iv.setScaleX(1);
                    iv.setScaleY(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public MyImageAdapter(int layoutId) {
            super(layoutId);
        }

        @Override
        protected void bindView(BaseByViewHolder<String> holder, String url, int position) {

        }

    }


    private static BaseByViewHolder<String> currentHolder;

    private void initRecyclerView() {


        // 设置自定义ItemAnimator
//        MyItemAnimator itemAnimator = new MyItemAnimator();
//        tvRecyclerView.setItemAnimator(itemAnimator);

        tvRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new MyImageAdapter(R.layout.item_preview_pic) {
            @Override
            protected void bindView(BaseByViewHolder<String> holder, String uri, int position) {
//                holder.setText(R.id.tv_text, bean);

                ImageView imageView = holder.getView(R.id.img);
                ImageView ivDelete = holder.getView(R.id.ivDelete);

                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // 记录原始缩放比例
                        originalScale = imageView.getScaleX();
                        // 放大图片
                        imageView.setScaleX(originalScale * 1.1f);
                        imageView.setScaleY(originalScale * 1.1f);
                        // 设置可拖动状态为true
                        isDragging = true;
                        currentHolder = holder;
                        return true;
                    }

                });

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //显示图片预览
                        showPicPreView(uri);

                    }
                });

                ivDelete.setOnClickListener(v -> {
                    Log.d(TAG, "删除图片:" + position);
                    alreadyList.remove(position);
                    mAdapter.notifyDataSetChanged();
                });


//                holder.addOnClickListener(R.id.ivDelete);
//                holder.addOnClickListener(R.id.img);
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.mipmap.ic_loading)
                        .error(R.mipmap.ic_loading)
                        .dontAnimate() //加载没有任何动画
                        .into(imageView);

            }
        };

        tvRecyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(alreadyList);   // 设置第一页数据
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(tvRecyclerView);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA_PERM) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                // 权限被授予，执行相关操作
                // startCamera();
            } else {
                // 权限被拒绝，显示提示信息
                // show
                //Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final int PICK_IMAGES = 22;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "结果::" + requestCode + "," + resultCode);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                Log.d(TAG, "多张图片,数量:" + data.getClipData().getItemCount());
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {

                    if (alreadyList.size() >= 9) {
                        showToast(getResources().getString(R.string.most_nine));
                        break;
                    }

                    //alreadyList.clear();
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    // 处理每张图片的Uri
//                    Log.d(TAG, "多张图片,每张的url:" + imageUri);
//                    ImageUtil.getPathFromUri(this, imageUri);
//                    if (!alreadyList.contains(imageUri))
                    alreadyList.add(ImageUtil.getPathFromUri(this, imageUri));

                }
            }
        }

        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();

//        if (alreadyList != null && alreadyList.size() > 0) {
//            for (Uri uri : alreadyList) {
//                Log.d(TAG, "图片地址:" + ImageUtil.getRealPathFromURI(context, uri));
//            }
//        }

    }


    private void showPicPreView(String uri) {

        if (uri == null) {
            showToast(getResources().getString(R.string.hasno_pic));
            return;
        }

        ivPreview.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(uri)
                .placeholder(R.mipmap.ic_loading)
                .error(R.mipmap.ic_loading)
                .dontAnimate() //加载没有任何动画
                .into(ivPreview);

    }

    private void hiddenPicPreView() {
        ivPreview.setVisibility(View.GONE);
        //把显示的图片清空掉
        ivPreview.setImageDrawable(null);
    }


    int picNum = 1;

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.clWifi://连接wifi
                connectSocket();
                break;

            case R.id.ivSend://发送图片

                if (alreadyList == null || alreadyList.isEmpty()) {
                    showToast(getResources().getString(R.string.no_pic));
                    return;
                }

                picNum = 1;

                ThreadUtil.getParallelExecutor().execute(() -> {

                    String picName;

                    for (String filePath : alreadyList) {
                        picName = "Picture_" + picNum + ".bmp";
                        picNum++;
                        ImageSender.sendPic(picName, filePath);
                    }

                });

                break;

            case R.id.ivSelect://去相册选择图片
                openGallery();
                break;

            case R.id.ivConvert://转换
                break;

            case R.id.tvSlide:
                auto();
                break;

            case R.id.ivPre://上一张
                choosePrePic();
                break;

            case R.id.ivSetting://设置
                startActivity(SettingActivity.class);
                break;

            case R.id.ivClassify://分类
                startActivity(ClassifyActivity.class);
                break;

            case R.id.ivNext://下一张
                chooseNextPic();
                break;

            case R.id.ivPreview://隐藏图片预览
                hiddenPicPreView();
                break;

        }
    }

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

    private void auto() {
        ThreadUtil.getParallelExecutor().execute(autoRunnable);
    }

    private void chooseNextPic() {
        ThreadUtil.getParallelExecutor().execute(nextRunnable);
    }

    private void choosePrePic() {
        ThreadUtil.getParallelExecutor().execute(preRunnable);
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


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            ImageSender.closeSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

