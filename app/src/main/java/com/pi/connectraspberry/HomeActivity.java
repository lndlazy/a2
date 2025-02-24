package com.pi.connectraspberry;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.Glide;
import com.pi.connectraspberry.bean.EventBusBean;
import com.pi.connectraspberry.bean.EventbusType;
import com.pi.connectraspberry.callback.MyItemTouchHelperCallback;
import com.pi.connectraspberry.service.EventMsg;
import com.pi.connectraspberry.service.MyService;
import com.pi.connectraspberry.toast.ToastUtil;
import com.pi.connectraspberry.ui.BaseActivity;
import com.pi.connectraspberry.ui.ClassifyActivity;
import com.pi.connectraspberry.ui.SettingActivity;
import com.pi.connectraspberry.util.BMPUtils;
import com.pi.connectraspberry.util.CommUtils;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.MD5Util;
import com.pi.connectraspberry.util.SocketSender;
import com.pi.connectraspberry.util.ImageUtil;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.ThreadUtil;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    private static final int RC_CAMERA_PERM = 33;
    private ByRecyclerView tvRecyclerView;
    //    private EditText etSecond;
    private ImageView ivPreview;
    private MyService mService;
    private boolean mBound = false;

    String[] pers = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    private ImageView ivWifi;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    String classifyName = " ";

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
        ImageView ivCleanPic = findViewById(R.id.ivCleanPic);
        ImageView ivCheck = findViewById(R.id.ivCheck);
        ivPreview = findViewById(R.id.ivPreview);
        ImageView ivPre = findViewById(R.id.ivPre);
        ImageView ivNext = findViewById(R.id.ivNext);
        TextView tvSlide = findViewById(R.id.tvSlide);
//        etSecond = findViewById(R.id.etSecond);
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
        ivCleanPic.setOnClickListener(this);
        ivCheck.setOnClickListener(this);

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

            if (EventMsg.LOG_SUCCESS.equals(msg))
                showToast(getResources().getString(R.string.log_extraction_success));
            else
                showToast(msg);

            //Log.d(TAG, "接受到返回的消息: " + message);
            if (message.contains("convert "))
                hideProgressDialog();

            return;
        }

        switch (message) {

            case EventMsg.CONNECT_SUCCESS:
                break;

//            case EventMsg.LOG_SUCCESS:
//                showToast(getResources().getString(R.string.log_extraction_success));
//                break;

            case EventMsg.CONNECT_SUCCESS_STATUS:// 显示连接成功状态
                connectSuccessStatus();
                break;

            case EventMsg.CONNECT_FAIL_STATUS:// 显示连接失败状态
                showToast(getResources().getString(R.string.connect_lost));
                noConnectStatus();
                break;

            case EventMsg.CLEAR_PIC_DATA:// 清除数据
                clearPicData();
                break;

        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusBean eventBusBean) {

        if (eventBusBean == null)
            return;

        if (TextUtils.isEmpty(eventBusBean.getType()))
            return;

        switch (eventBusBean.getType()) {

//            case EventbusType.Language:
//                //切换
////                updateLanguage(SpUtils.getLanguagePreference());
//                Log.d(TAG, "更新语言:" + eventBusBean.getMessage());
//                updateLanguage(eventBusBean.getMessage());
//                break;
            case EventbusType.CLEAR_MD5:
                //切换
//                updateLanguage(SpUtils.getLanguagePreference());
                Log.d(TAG, "清除md5");
                raspMd5List.clear();

                break;

        }

    }


    private List<String> raspMd5List = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(List<String> md5List) {

        if (md5List == null || md5List.isEmpty()) {
            return;
        }

        raspMd5List.clear();
        raspMd5List.addAll(md5List);
//        for (FolderBean folderBean : folderBeans) {
//            Log.d(TAG, "文件夹名称:" + folderBean.getFolderName() + ",md5:" + folderBean.getFolderMd5());
//            raspMd5Map.put(folderBean.getFolderMd5(), folderBean.getFolderName());
//        }

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

        hideProgressDialog();

        ToastUtil.show(getResources().getString(R.string.connect_success));
        if (CommUtils.isMainLooper()) {
            showPic(ivWifi, R.mipmap.ic_wifi_blue);

        } else
            runOnUiThread(() -> {
                showPic(ivWifi, R.mipmap.ic_wifi_blue);
            });

    }

    //未连接状态
    private void noConnectStatus() {

        hideProgressDialog();

        if (CommUtils.isMainLooper()) {
            showPic(ivWifi, R.mipmap.ic_wifi_gray);
            showToast(getResources().getString(R.string.connect_lost));
        } else
            runOnUiThread(() -> {
                showPic(ivWifi, R.mipmap.ic_wifi_gray);
                showToast(getResources().getString(R.string.connect_lost));
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
            EasyPermissions.requestPermissions(this, getString(R.string.need_photo_permission),
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
                    try {
                        Log.d(TAG, "删除图片:" + position);
                        alreadyList.remove(position);
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
    private static final int CROP_REQUEST_CODE = 23;
//    private Map<String, String> appMd5Map = new LinkedHashMap<>();

    //    private boolean hasIllegalPic = false;
    private Uri originalUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "结果::" + requestCode + "," + resultCode);
//        hasIllegalPic = false;

        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                Log.d(TAG, "多张图片,数量:" + data.getClipData().getItemCount());
                int count = data.getClipData().getItemCount();


                int beginIndex = 0;
                if (alreadyList.size() > 0)
                    beginIndex = alreadyList.size();

                for (int i = 0; i < count; i++) {

                    if (alreadyList.size() >= 9) {
                        showToast(getResources().getString(R.string.most_nine));
                        break;
                    }

                    originalUri = data.getClipData().getItemAt(i).getUri();

                    String pathFromUri = ImageUtil.getPathFromUri(this, originalUri);
                    int currentIndex = beginIndex + i;
                    Log.d(TAG, "图片路径 ：：" + pathFromUri + ",===> beginIndex:" + currentIndex);

                    alreadyList.add("");
                    //判断是否是BMP图片
                    if (FileUtils.isBMPPic(pathFromUri)) {

                        if (FileUtils.isSizeNormal(pathFromUri)) {
                            //既是BMP图片，且像素符合要求
                            addPic(currentIndex, pathFromUri);
                        } else {
                            //BMP图片不符合像素要求，裁剪图片
                            resetPic(currentIndex);
                        }

                    } else {
                        //不是BMP图片，判断尺寸是否满足
                        resetPic(currentIndex);
//                        if (FileUtils.isSizeNormal(pathFromUri)) {
//                            //尺寸满足，进行转换
//                            handleCropResult(ImageUtil.getUri(new File(pathFromUri)), null);
//                        } else {
//                            //尺寸不满足，裁剪
//                            resetPic();
//                        }

                    }


                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            //处理裁剪结果
            Uri resultUri = UCrop.getOutput(data);

            handleCropResult(resultUri, originalUri);

        } else {
            Log.d(TAG, "other ===》》 ");
        }

//        if (hasIllegalPic) {
//            showToast(getResources().getString(R.string.filter_illegal_pic));
//        }

//        for (String s : alreadyList) {
//            Log.d(TAG, "地址::" + s);
//        }

    }


    private void addPic(int index, String pathFromUri) {

        alreadyList.set(index, pathFromUri);
//        if (head)
//            alreadyList.add(0, pathFromUri);
//        else
//            alreadyList.add(pathFromUri);
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private void resetPic(int i) {
        UCrop.Options options = new UCrop.Options();
// 可以设置裁剪界面的各种属性，如裁剪框的颜色、背景色等
//                    options.setToolbarColor(ContextCompat.getColor(this, android.R.color.background_light));
//                    options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        // 设置裁剪的宽高比为 12:17
        options.setAspectRatioOptions(0, new AspectRatio("12:17", 12, 17));
        // 设置输出的分辨率为 2160*3060
        options.withMaxResultSize(2160, 3060);
//                    options.setCompressionMaxWidth(2160);
//                    options.setCompressionMaxHeight(3060);
        UCrop.of(originalUri, Uri.fromFile(new File(getCacheDir(), UUID.randomUUID() + "_" + i + ".jpg")))
                .withOptions(options)
                .start(this);
    }


    // 缩放图片
    private void handleCropResult(Uri resultUri, Uri originalUri) {
//        final Uri resultUri = UCrop.getOutput(data);

        Log.d(TAG, "resultUri:" + resultUri.getPath());

//        Log.d(TAG, "originalUri:" + originalUri + ",resultUri==>" + resultUri);
        if (resultUri != null) {
            try {
                // 获取裁剪后的图片

                BitmapLoadUtils.decodeBitmapInBackground(this, resultUri, resultUri, 2160, 3060, new BitmapLoadCallback() {
                    @Override
                    public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {

                        Log.d(TAG, "图片裁剪成功===》" + imageInputPath + ", 输出地址:" + imageOutputPath);

                        Bitmap originalBitmap = BitmapFactory.decodeFile(imageOutputPath);
                        // 设定新的像素宽度和高度
                        int newWidth = 2160;
                        int newHeight = 3060;
                        // 缩放图片
                        Bitmap scaledBitmap = ImageUtil.scaleBitmap(originalBitmap, newWidth, newHeight);
                        //保存图片
//                        String afterPath = UUID.randomUUID().toString() + ".png";
//                        ImageUtil.saveBitmapToFile(scaledBitmap, new File(getCacheDir(), afterPath));

//                        String newPath =  UUID.randomUUID() + ".bmp";
                        File file = new File(getCacheDir(), UUID.randomUUID() + ".bmp");
                        BMPUtils.convertToBMP(scaledBitmap, file.getPath());

                        //判断制作好的BMP图片是否符合像素要求
                        boolean standardPic = FileUtils.isStandardPic(file.getPath());
                        if (standardPic) {

                            //获取imageInputPath文件名的最后一位
                            int position = 0;
                            int index = imageInputPath.lastIndexOf(".");
                            String fileName = imageInputPath.substring(index - 1, index);
                            position = Integer.parseInt(fileName);

                            addPic(position, file.getPath());
                            Log.d(TAG, "  ===转成bmp格式成功===  " + file.getPath());

                        } else {
                            showToast("图片转换失败！");
                            Log.e(TAG, "  ===转成bmp格式失败!!!!!!!!!!!! ===  " + file.getPath());
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Exception bitmapWorkerException) {

                        Log.d(TAG, "裁剪失败:" + bitmapWorkerException.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                showProgressDialog(getResources().getString(R.string.connecting));
                connectSocket();
//                showToast(" connect wifi ");
                break;

            case R.id.ivSend://发送图片
                sendPic();
                break;

            case R.id.ivSelect://去相册选择图片
                openGallery();
                break;

            case R.id.ivConvert://转换
                convertPic();
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

            case R.id.ivCleanPic://清理图片
                clearPic();
                break;

            case R.id.ivCheck://检查EDP连接情况
                checkConnect();
                break;

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void checkConnect() {

        ThreadUtil.getParallelExecutor().execute(checkConnectRunnable);
    }

    private void clearPic() {

        ThreadUtil.getParallelExecutor().execute(clearPicRunnable);
    }

    private void sendPic() {

        if (alreadyList == null || alreadyList.isEmpty()) {
            showToast(getResources().getString(R.string.no_pic));
            return;
        }

        Log.d(TAG, "图片张数===>>" + alreadyList.size());

//        for (String s : alreadyList) {
//            Log.d(TAG, " 图片路径 :" + s);
//        }

        showProgressDialog(getResources().getString(R.string.sending));

//        appMd5Map.clear();
//        File file = new File(pathFromUri);
//        appMd5Map.put(MD5Util.getMD5(file), file.getAbsolutePath());

        ThreadUtil.getParallelExecutor().execute(() -> {

            try {
                boolean b = SocketSender.sendCommand(MyCommand.COMMAND_CLEAR_PIC);

                if (!b) {
                    showToast(getResources().getString(R.string.send_fail));
                    hideProgressDialog();
                    return;
                }

                picNum = 1;
                String picName = "";
                //遍历appMd5Map
                File file;
                for (String pathFromUri : alreadyList) {

                    file = new File(pathFromUri);
                    String md5 = MD5Util.getMD5(file);

                    picName = "picture_" + picNum + ".bmp";

                    if (!raspMd5List.contains(md5)) {
                        //发送图片
                        SocketSender.sendPic(classifyName, picName, file.getAbsolutePath());
                    } else {
                        SocketSender.sendPicMd5(classifyName, picName, md5);
                    }
                    picNum++;
                }

                runOnUiThread(() -> {
                    hideProgressDialog();
                    showToast(getResources().getString(R.string.send_success));
                });

            } catch (Exception e) {
                //图片发送失败
                e.printStackTrace();

                runOnUiThread(() -> {
                    hideProgressDialog();
                    showToast(getResources().getString(R.string.send_fail) + e.getMessage());
                });
            }


        });
    }

    /**
     * 转换图片
     */
    private void convertPic() {
        showProgressDialog(getResources().getString(R.string.converting));
        ThreadUtil.getParallelExecutor().execute(convertRunnable);
    }

    Runnable convertRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_CONVERT + classifyName);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable clearPicRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_CLEAR_PIC);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable checkConnectRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_CHECK_CONNECT);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable autoRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_AUTO);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable preRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_PRE);
        //showToast(b ? "发送成功" : "发送失败");
        if (!b) {
            noConnectStatus();
        }
    };

    Runnable nextRunnable = () -> {
        boolean b = SocketSender.sendCommand(MyCommand.COMMAND_NEXT);
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
            SocketSender.closeSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

