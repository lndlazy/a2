package com.pi.connectraspberry.ui;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.bean.ClassifyBean;
import com.pi.connectraspberry.callback.MyItemAnimator;
import com.pi.connectraspberry.callback.SwipeToDeleteCallback;

import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class ClassifyActivity extends BaseActivity {

    private static final String TAG = "ClassifyActivity";
    private ByRecyclerView recyclerView;
    private List<ClassifyBean> folderList;
    private BaseRecyclerAdapter<ClassifyBean> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_classify);

        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        recyclerView = findViewById(R.id.recyclerView);

        ivBack.setOnClickListener(v -> finish());
        ivAdd.setOnClickListener(v -> addClassify());
        folderList = new ArrayList<>();
        folderList.add(new ClassifyBean("周一", false));
        folderList.add(new ClassifyBean("周二", false));
        folderList.add(new ClassifyBean("周三", false));

        initRecyclerView();

    }

    /**
     * 添加分类
     */
    private void addClassify() {

        new XPopup.Builder(context).asInputConfirm("新增分类", "请输入分类名称",
                        new OnInputConfirmListener() {
                            @Override
                            public void onConfirm(String text) {
                                Log.d(TAG, "新增分类: " + text);
                                //toast("input text: " + text);
                            }
                        })
                .show();

    }

    private void initRecyclerView() {

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new BaseRecyclerAdapter<ClassifyBean>(R.layout.item_classify) {
            @Override
            protected void bindView(BaseByViewHolder<ClassifyBean> holder, ClassifyBean classifyBean, int position) {
                holder.setText(R.id.tvFolderName, classifyBean.getName());

//                holder.setImageResource(R.id.ivChoose, classifyBean.isChoose() ? R.mipmap.ic_choosed : R.mipmap.ic_no_choose);
                //ImageView ivChoose = holder.getView(R.id.ivChoose);

                holder.addOnClickListener(R.id.ivChoose);


            }
        };

        recyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(folderList);   // 设置第一页数据

        ItemTouchHelper.Callback callback = new SwipeToDeleteCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        MyItemAnimator itemAnimator = new MyItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.setOnItemChildClickListener((view, position) -> {
            Log.e("TAG", "onItemChildClick: " + position);


            switch (view.getId()) {
                case R.id.ivChoose:
                    //弹出提示，是否发送显示当前文件夹内的所有图片
                    showDeleteDialog(folderList.get(position).getName());
                    break;
            }
        });
    }


    private void showDeleteDialog(String name) {
        //弹出提示，是否删除当前文件夹
        new XPopup.Builder(this).asConfirm("提示", "确定要显示'" + name + "'文件夹内的图片吗？",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                //toast("click confirm");
                                Log.d(TAG, "发送显示当前文件夹内的所有图片");
                            }
                        })
                .show();

    }
}
