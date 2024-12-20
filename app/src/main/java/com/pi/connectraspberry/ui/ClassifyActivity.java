package com.pi.connectraspberry.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.callback.MyItemAnimator;
import com.pi.connectraspberry.service.EventMsg;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.SocketSender;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class ClassifyActivity extends BaseActivity {

    private static final String TAG = "ClassifyActivity";
    private ByRecyclerView recyclerView;
    private List<String> folderList;
    private BaseRecyclerAdapter<String> mAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_classify;
    }

    @Override
    protected void initView() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        recyclerView = findViewById(R.id.recyclerView);

        ivBack.setOnClickListener(v -> finish());
        ivAdd.setOnClickListener(v -> addClassify());
        folderList = new ArrayList<>();

        folderList.addAll(FileUtils.getFolderList());

        initRecyclerView();
    }

    @Override
    protected void initData() {

        EventBus.getDefault().register(this);
    }

    /**
     * 添加分类
     */
    private void addClassify() {

        new XPopup.Builder(context).asInputConfirm(getResources().getString(R.string.add_new_classify), getResources().getString(R.string.please_enter_name),
                        new OnInputConfirmListener() {
                            @Override
                            public void onConfirm(String text) {
                                Log.d(TAG, "新增分类: " + text);
                                //toast("input text: " + text);
                                String specialChars = "\\s./, 。‘'*（）&%￥\\^?？#@！~：“；}\\{】【、|\\?\"<>";
                                Pattern pattern = Pattern.compile("^[^" + specialChars + "]+$");
                                Matcher matcher = pattern.matcher(text);
                                boolean matches = matcher.matches();

                                if (!matches) {
                                    showToast(getResources().getString(R.string.illegal_character));
                                    return;
                                }


                                if (!SocketSender.isConnect()) {
                                    showToast(getResources().getString(R.string.connect_lost));
                                    return;
                                }

                                boolean success = FileUtils.createFile(text);

                                Log.d(TAG, "文件夹是否创建成功:" + success);
                                if (!success) {
                                    showToast(getResources().getString(R.string.add_classify_fail));
                                    return;
                                }

                                //通知raspberry创建文件夹
                                new Thread(() -> {
                                    try {

                                        boolean b = SocketSender.sendCreateFolder(text);
                                        Log.d(TAG, "sendCreateFolder: " + b);
                                        if (b) {

                                            runOnUiThread(() -> {
                                                showToast(getResources().getString(R.string.add_classify_success));
                                                folderList.add(text);
                                                mAdapter.notifyDataSetChanged();
                                            });

                                        } else {
                                            showToast(getResources().getString(R.string.add_classify_fail));
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();

                            }
                        })
                .show();

    }

    private void initRecyclerView() {

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new BaseRecyclerAdapter<String>(R.layout.item_classify) {
            @Override
            protected void bindView(BaseByViewHolder<String> holder, String classifyBean, int position) {
                holder.setText(R.id.tvFolderName, classifyBean);

//                holder.setImageResource(R.id.ivChoose, classifyBean.isChoose() ? R.mipmap.ic_choosed : R.mipmap.ic_no_choose);
                //ImageView ivChoose = holder.getView(R.id.ivChoose);

                holder.addOnClickListener(R.id.ivChoose);


            }
        };

        recyclerView.setAdapter(mAdapter);
        mAdapter.setNewData(folderList);   // 设置第一页数据

        MyItemAnimator itemAnimator = new MyItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.setOnItemLongClickListener((view, position) -> {
            Log.e("TAG", "onItemLongClick: " + position);
            //toast("长按了第" + position + "个");
            showDeleteDialog(folderList.get(position));
            return true;
        });

        recyclerView.setOnItemChildClickListener((view, position) -> {
            Log.e("TAG", "onItemChildClick: " + position);

            switch (view.getId()) {
                case R.id.ivChoose:
                    //弹出提示，是否发送显示当前文件夹内的所有图片
                    showShowDialog(folderList.get(position));
                    break;
            }
        });

        recyclerView.setOnItemClickListener((view, position) -> {
            Log.e("TAG", "onItemClick: " + position);
            //toast("点击了第" + position + "个");

            Intent mIntent = new Intent(context, ClassifyDetailActivity.class);
            mIntent.putExtra("classifyName", folderList.get(position));
            startActivity(mIntent);

        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String message) {

        // 处理事件
        if (TextUtils.isEmpty(message))
            return;

        //Log.d(TAG, "接受到返回的消息: " + message);
        if (message.startsWith("back:")) {
            if (message.contains("convert "))
                hideProgressDialog();
        }

    }


    private void showShowDialog(String name) {
        //弹出提示，是否删除当前文件夹
        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.are_sure_show) + name + getResources().getString(R.string.are_sure_show_after),
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                //toast("click confirm");
                                Log.d(TAG, "发送显示当前文件夹内的所有图片");

                                showProgressDialog(getResources().getString(R.string.converting));
                                new Thread(() -> {
                                    boolean b = SocketSender.sendCommand(MyCommand.COMMAND_CONVERT + name);
                                    if (!b) {
                                        hideProgressDialog();
                                        showToast(getResources().getString(R.string.connect_lost));

                                    }
                                }).start();

                            }
                        })
                .show();

    }

    private void showDeleteDialog(String name) {
        //弹出提示，是否删除当前文件夹
        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.are_sure_delete) + name + getResources().getString(R.string.are_sure_delete_after),
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                //toast("click confirm");


                                if (!SocketSender.isConnect()) {
                                    showToast(getResources().getString(R.string.connect_lost));
                                    return;
                                }

                                int i = FileUtils.deleteDirectory(new File(FileUtils.getLocalBasePath() + name));
                                if (i == 0) {

                                    //通知raspberry删除文件夹
                                    new Thread(() -> {
                                        try {

                                            boolean b = SocketSender.sendDeleteFolder(name);
                                            Log.d(TAG, "sendCreateFolder: " + b);
                                            if (b) {

                                                runOnUiThread(() -> {
                                                    showToast(getResources().getString(R.string.delete_success));
                                                    folderList.remove(name);
                                                    mAdapter.notifyDataSetChanged();
                                                });
                                            } else {
                                                showToast(getResources().getString(R.string.delete_failed));
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();


                                } else if (i == -1) {
                                    showToast(getResources().getString(R.string.folder_not_exist));
                                } else if (i == -2) {
                                    showToast(getResources().getString(R.string.delete_failed));
                                }
                                Log.d(TAG, "删除当前文件夹和文件夹内的所有图片");
                            }
                        })
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
