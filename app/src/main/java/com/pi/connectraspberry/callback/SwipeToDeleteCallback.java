package com.pi.connectraspberry.callback;

import android.util.Log;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.pi.connectraspberry.bean.ClassifyBean;

import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {
    private static final String TAG = "SwipeToDeleteCallback";
    private final BaseRecyclerAdapter<ClassifyBean> adapter;

    public SwipeToDeleteCallback(BaseRecyclerAdapter<ClassifyBean> adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 允许左右滑动删除，不允许上下拖动（如果需要拖动功能可以添加拖动标志）
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int dragFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        // 因为我们只关注滑动删除，这里返回false
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 获取被滑动删除的项目位置
        //int position = viewHolder.getAdapterPosition();

        int position = viewHolder.getPosition();
        Log.d(TAG, "==============>>>onSwiped: " + position);
        // 从数据集中删除该项目
//        adapter.getdata().remove(position);
        adapter.getData().remove(position);
        //((ArrayList<Object>) adapter.getItemList()).remove(position);
        // 通知适配器数据已改变
        adapter.notifyItemRemoved(position);
    }
}