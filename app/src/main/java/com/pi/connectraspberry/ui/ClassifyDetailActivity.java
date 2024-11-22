package com.pi.connectraspberry.ui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
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

    protected List<Uri> alreadyList = new ArrayList<>();
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
        ivBack.setOnClickListener(v -> finish());

        ivPreview.setOnClickListener(this);

        initRecyclerView();

    }


    @Override
    protected void initData() {

        queryImagesInDirectory("a2/" + classifyName);
        mAdapter.notifyDataSetChanged();

    }


    private void queryImagesInDirectory(String targetDirectory) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.DATA + " LIKE?";
        String[] selectionArgs = new String[]{"%" + targetDirectory + "/%"};
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
//                cursor.getNotificationUri()
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                Log.d(TAG, "图片===》" + imagePath);

                //long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri imageUri = Uri.parse("file://" + imagePath);
                //Uri imageUri = ContentUris.withAppendedId(uri, imageId);
                alreadyList.add(imageUri);

                //imagePaths.add(imagePath);
            }
            cursor.close();
        }
    }


    private static BaseByViewHolder<Uri> currentHolder;
    private DetailImageAdapter mAdapter;
    private float originalScale;

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivPreview://隐藏图片预览
                hiddenPicPreView();
                break;


        }


    }

    public static class DetailImageAdapter extends BaseRecyclerAdapter<Uri> {

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
        protected void bindView(BaseByViewHolder<Uri> holder, Uri bean, int position) {

        }

    }

    private void initRecyclerView() {

        recyclerView = findViewById(R.id.recyclerView);


        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new DetailImageAdapter(R.layout.item_preview_pic) {
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

        recyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(alreadyList);   // 设置第一页数据
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void showPicPreView(Uri uri) {

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
