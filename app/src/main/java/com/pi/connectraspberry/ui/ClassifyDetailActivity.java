package com.pi.connectraspberry.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.Glide;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.bean.FolderBean;
import com.pi.connectraspberry.callback.MyItemTouchHelperCallback;
import com.pi.connectraspberry.mlogger.MLogger;
import com.pi.connectraspberry.service.EventMsg;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.ImageUtil;
import com.pi.connectraspberry.util.MD5Util;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.SocketSender;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

/**
 * 分类详情
 */
public class ClassifyDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ClassifyDetail";
    private ByRecyclerView recyclerView;
    private ImageView ivPreview, ivSend;
    protected List<String> alreadyList = new ArrayList<>();
    private String classifyName;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_calssify_detail;
    }

    @Override
    protected void initView() {

        ivPreview = findViewById(R.id.ivPreview);

        classifyName = getIntent().getStringExtra("classifyName");
        Log.d(TAG, "分类名称: " + classifyName);
        TextView tvTop = findViewById(R.id.tvTop);
        tvTop.setText(classifyName);

        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        ImageView ivSend = findViewById(R.id.ivSend);
        ivBack.setOnClickListener(v -> finish());

        ivPreview.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivSend.setOnClickListener(this);

        initRecyclerView();

    }

    @Override
    protected void initData() {

        EventBus.getDefault().register(this);

        new Thread(() -> {

            queryImagesInDirectory(FileUtils.getLocalBasePath() + classifyName);
            runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
            });

        }).start();

        new Thread(() -> SocketSender.getFolderImgs(classifyName)).start();

    }


    private void queryImagesInDirectory(String targetDirectory) {

        File directory = new File(targetDirectory);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isBMPFile(file)) {
                        alreadyList.add(file.getAbsolutePath());
                    }
                }
            }
        }

    }

    private boolean isBMPFile(File file) {
        String fileName = file.getName();
        return fileName.toLowerCase().endsWith(".bmp");
    }


    private static BaseByViewHolder<String> currentHolder;
    private DetailImageAdapter mAdapter;
    private float originalScale;
    boolean isSendSuccess = false;

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivPreview://隐藏图片预览
                hiddenPicPreView();
                break;

            case R.id.ivAdd://添加图片
                openGallery();
                break;

            case R.id.ivSend://发送图片

                showLoadingDialog();
                new Thread(() -> {

                    try {
                        SocketSender.sendClearPic(classifyName);
                        syncPics();
                        isSendSuccess = true;
                    } catch (Exception e) {
                        showToast(getResources().getString(R.string.send_fail));
                        e.printStackTrace();
                        isSendSuccess = false;
                    }

                    runOnUiThread(() -> {
                        hideLoadingDialog();
                        showToast(getResources().getString(isSendSuccess ? R.string.send_success : R.string.send_fail));
                    });
                }).start();

                break;

        }

    }

    int picNum = 1;

    /**
     * 与raspberry同步文件夹图片
     */
    private void syncPics() throws Exception {

        picNum = 1;
        String picName = "";
        File file;
        for (String pathFromUri : alreadyList) {

            file = new File(pathFromUri);
            String md5 = MD5Util.getMD5(file);

            picName = "picture_" + picNum + ".bmp";
            Log.d(TAG, "md5值:" + md5 + ",图片名称:" + picName);
            if (!raspMd5List.contains(md5)) {
                Log.d(TAG, "不包含直接发图片");
                //发送图片
                SocketSender.sendPic(classifyName, picName, file.getAbsolutePath());
            } else {
                Log.d(TAG, "已包含 发md5 ");
                SocketSender.sendPicMd5(classifyName, picName, md5);
            }
            picNum++;

        }

    }

    private static final int PICK_IMAGES = 32;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }

    ProgressDialog progressDialog;

    private void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(true);
        }
        progressDialog.show();
    }

    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "结果::" + requestCode + "," + resultCode);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                Log.d(TAG, "多张图片,数量:" + data.getClipData().getItemCount());
                int count = data.getClipData().getItemCount();

                hideLoadingDialog();
                showLoadingDialog();

                try {

                    copyPicture(data, count);
                } catch (Exception e) {
                    MLogger.e("复制图片失败:" + e.getMessage());
                    e.printStackTrace();
                }
                hideLoadingDialog();
            }
        }

        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();

    }

    private void copyPicture(Intent data, int count) {


        boolean hasIllegalPic = false;
        for (int i = 0; i < count; i++) {

            if (alreadyList.size() >= 9) {
                showToast(getResources().getString(R.string.most_nine));
                break;
            }

            //alreadyList.clear();
            Uri imageUri = data.getClipData().getItemAt(i).getUri();
            //原文件路径
            String imgPath = ImageUtil.getPathFromUri(this, imageUri);

            boolean standardPic = FileUtils.isStandardPic(imgPath);
            if (!standardPic) {
                //非bmp格式图片不处理
                hasIllegalPic = true;
                continue;
            }

            File imgFile = new File(imgPath);
            //目标文件目录
            String targetPath = FileUtils.getLocalBasePath() + classifyName;

            //判断目录下是否已经有同名文件
            String fileName = imgFile.getName();
            Log.d(TAG, "图片名称:" + fileName);

            boolean repeat = FileUtils.isRepeat(new File(targetPath), fileName);

            String targetFileName = repeat ? FileUtils.add_1(fileName) : fileName;

            //如果文件夹里还是有同名的文件，就在文件名后面再加_1
            while (FileUtils.isRepeat(new File(targetPath), targetFileName)) {
                targetFileName = FileUtils.add_1(targetFileName);
            }

            boolean b = FileUtils.copyPic2CurrentFile(imgFile, targetPath, targetFileName);

            if (b) {
                //图片拷贝成功
                File newFile = new File(targetPath, targetFileName);
                alreadyList.add(newFile.getAbsolutePath());

                Log.d(TAG, "复制成功==目标文件名称:" + targetFileName);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(targetPath))));

            } else {
                showToast(getResources().getString(R.string.copy_failed));
            }

        }

        if (hasIllegalPic) {
            showToast(getResources().getString(R.string.filter_illegal_pic));
        }

    }


    public static class DetailImageAdapter extends BaseRecyclerAdapter<String> {

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

        public DetailImageAdapter(int layoutId) {
            super(layoutId);
        }

        @Override
        protected void bindView(BaseByViewHolder<String> holder, String bean, int position) {

        }

    }

    private void initRecyclerView() {

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new DetailImageAdapter(R.layout.item_preview_pic) {
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

                        currentHolder = holder;
                        return true;
                    }

                });

                imageView.setOnClickListener(v -> {
                    //显示图片预览
                    showPicPreView(uri);

                });

                ivDelete.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "删除图片:" + position);

                        deletePic(uri, position);

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

        recyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(alreadyList);   // 设置第一页数据
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


    private List<String> raspMd5List = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(List<String> md5List) {

        Log.d(TAG, "接收到的md5List==>:" + md5List.size());
        if (md5List == null || md5List.isEmpty()) {
            return;
        }

        raspMd5List.clear();
        raspMd5List.addAll(md5List);

    }

    private void deletePic(String path, int position) {
        //删除图片
        boolean b = FileUtils.deleteFile(path);
        MLogger.d(TAG + ",要删除的图片路径:" + path + " 结果:" + b);
        if (!b) {
            showToast(getResources().getString(R.string.delete_failed));
            return;
        }
        alreadyList.remove(position);
        mAdapter.notifyDataSetChanged();
        //图片===》/storage/emulated/0/Pictures/a2/firday/picture_2.bmp

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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

    }
}
