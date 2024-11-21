package com.pi.connectraspberry;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
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
import com.pi.connectraspberry.callback.MyItemTouchHelperCallback;
import com.pi.connectraspberry.ui.BaseActivity;
import com.pi.connectraspberry.ui.ClassifyActivity;
import com.pi.connectraspberry.ui.SettingActivity;
import com.pi.connectraspberry.util.CommUtils;
import com.pi.connectraspberry.util.ImageUtil;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
//        setContentView(R.layout.layout_test);

        initData();

//        test();
    }

    private void initData() {
        //请求权限
        mRequestPermission();

        ConstraintLayout clWifi = findViewById(R.id.clWifi);
        ImageView ivSend = findViewById(R.id.ivSend);
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


    String[] pers = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};

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
    protected List<Uri> alreadyList = new ArrayList<>();

    private float originalScale;
    private boolean isDragging;


    public static class MyImageAdapter extends BaseRecyclerAdapter<Uri> {

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
        protected void bindView(BaseByViewHolder<Uri> holder, Uri bean, int position) {

        }


    }


    private static BaseByViewHolder<Uri> currentHolder;

    private void initRecyclerView() {

        tvRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new MyImageAdapter(R.layout.item_preview_pic) {
            @Override
            protected void bindView(BaseByViewHolder<Uri> holder, Uri uri, int position) {
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

        // 设置自定义ItemAnimator
//        MyItemAnimator itemAnimator = new MyItemAnimator();
//        tvRecyclerView.setItemAnimator(itemAnimator);


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
                        showToast("最多只能选择9张图片");
                        break;
                    }

                    //alreadyList.clear();
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    // 处理每张图片的Uri
                    Log.d(TAG, "多张图片,每张的url:" + imageUri);
                    if (!alreadyList.contains(imageUri))
                        alreadyList.add(imageUri);

                }
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


    private void showPicPreView(Uri uri) {

        if (uri == null) {
            showToast("图片地址为空");
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
    public void onClick(View view) {


        switch (view.getId()) {

            case R.id.clWifi://连接wifi
                break;

            case R.id.ivSend://发送图片
                break;

            case R.id.ivSelect://去相册选择图片
                openGallery();
                break;

            case R.id.ivConvert://转换
                break;

            case R.id.tvSlide:
                break;

            case R.id.ivPre://上一张
                break;

            case R.id.ivSetting://设置
                startActivity(SettingActivity.class);
                break;

            case R.id.ivClassify://分类
                startActivity(ClassifyActivity.class);
                break;

            case R.id.ivNext://下一张
                break;

            case R.id.ivPreview://隐藏图片预览
                hiddenPicPreView();
                break;

        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }

}

