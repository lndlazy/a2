package com.pi.connectraspberry.ui;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.bumptech.glide.Glide;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.TestActivity;
import com.pi.connectraspberry.callback.MyItemTouchHelperCallback;
import com.pi.connectraspberry.mlogger.MLogger;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

/**
 * 分类详情
 */
public class ClassifyDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ClassifyDetail";
    private ByRecyclerView recyclerView;
    private ImageView ivPreview;
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
        ivBack.setOnClickListener(v -> finish());

        ivPreview.setOnClickListener(this);
        ivAdd.setOnClickListener(this);

        initRecyclerView();

    }


    @Override
    protected void initData() {

        queryImagesInDirectory(FileUtils.getLocalBasePath() + classifyName);
        mAdapter.notifyDataSetChanged();

    }


    private void queryImagesInDirectory(String targetDirectory) {

        List<String> bmpImages = ImageUtil.findBMPImages(targetDirectory);

        alreadyList.addAll(bmpImages);


//        ContentResolver contentResolver = getContentResolver();
//        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        String[] projection = {MediaStore.Images.Media.DATA};
//        String selection = MediaStore.Images.Media.DATA + " LIKE?";
//        String[] selectionArgs = new String[]{"%" + targetDirectory + "/%"};
//        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
////                cursor.getNotificationUri()
//                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                Log.d(TAG, "图片===》" + imagePath);
//
//                //long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//                Uri imageUri = Uri.parse("file://" + imagePath);
//                //Uri imageUri = ContentUris.withAppendedId(uri, imageId);
//                alreadyList.add(imageUri);
//
//                //imagePaths.add(imagePath);
//            }
//            cursor.close();
//        }
    }


    private static BaseByViewHolder<String> currentHolder;
    private DetailImageAdapter mAdapter;
    private float originalScale;

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivPreview://隐藏图片预览
                hiddenPicPreView();
                break;

            case R.id.ivAdd://添加图片

                openGallery();

                break;


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

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(true);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
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

                hideProgressDialog();
                showProgressDialog();
                try {
                    copyPicture(data, count);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
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

    private void copyPicture(Intent data, int count) {
        for (int i = 0; i < count; i++) {

            if (alreadyList.size() >= 9) {
                showToast(getResources().getString(R.string.most_nine));
                break;
            }

            //alreadyList.clear();
            Uri imageUri = data.getClipData().getItemAt(i).getUri();
            // 处理每张图片的Uri
//            Log.d(TAG, "多张图片,每张的url:" + imageUri);
//                    if (!alreadyList.contains(imageUri))
//                    alreadyList.add(imageUri);
//                    String path = imageUri.getPath();
//
//                    Log.d(TAG, "图片地址:" + path);


            //原文件路径
            String imgPath = ImageUtil.getPathFromUri(this, imageUri);
            File imgFile = new File(imgPath);
            //目标文件目录
            String targetPath = FileUtils.getLocalBasePath() + classifyName;

            //判断目录下是否已经有同名文件
            String fileName = imgFile.getName();
            Log.d(TAG, "图片名称:" + fileName);
            boolean repeat = FileUtils.isRepeat(new File(targetPath), fileName);
            String targetFileName = repeat ? FileUtils.add_1(fileName) : fileName;

            boolean b = FileUtils.copyPic2CurrentFile(imgFile, targetPath, targetFileName);

            if (b) {
                //图片拷贝成功
                alreadyList.add(new File(targetPath, targetFileName).getAbsolutePath());
                Log.d(TAG, "复制成功==目标文件名称:" + targetFileName);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(targetPath))));

            } else {
                showToast(getResources().getString(R.string.copy_failed));
            }

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

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //显示图片预览
                        showPicPreView(uri);

                    }
                });

                ivDelete.setOnClickListener(v -> {
                    Log.d(TAG, "删除图片:" + position);

                    deletePic(uri, position);
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


}
