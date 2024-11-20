package com.pi.connectraspberry.callback;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.pi.connectraspberry.R;
import com.pi.connectraspberry.TestActivity;

import java.util.Collections;

import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private static final String TAG = "MyItemTouchHelper";
    private final BaseRecyclerAdapter<Uri> adapter;

    private int currentPosition = -1;

    public MyItemTouchHelperCallback(BaseRecyclerAdapter<Uri> adapter) {
        this.adapter = adapter;
    }


    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "==============>>>getMovementFlags: " + viewHolder.getAdapterPosition());
        // 允许上下拖动（UP和DOWN）
//        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        // 不允许左右滑动（暂不考虑滑动删除等操作）
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {

        Log.d(TAG, "==============>>>onMove: " + viewHolder.getAdapterPosition() + " " + target.getAdapterPosition());
        // 交换数据位置
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {

                //List<Uri> data = adapter.getData();

                Collections.swap(adapter.getData(), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(adapter.getData(), i, i - 1);
            }
        }
        // 通知适配器数据位置已改变
        adapter.notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 暂不处理滑动操作，因为我们只关注拖动

        Log.d(TAG, "==============>>>onSwiped: " + direction);

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

//        Log.d(TAG, "===onSelectedChanged===actionState:" + actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // 拖动开始，不做特殊处理，保持放大状态
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // 拖动结束，恢复图片大小
            Log.d(TAG, "拖动结束？？？？？？？？？？？？？===" + (viewHolder == null));

            try {

                if (adapter instanceof TestActivity.MyImageAdapter) {
                    TestActivity.MyImageAdapter myImageAdapter = (TestActivity.MyImageAdapter) adapter;
                    myImageAdapter.restoreImageSize();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

//            if (adapter instanceof ImageAdapter && viewHolder instanceof ImageAdapter.ImageViewHolder) {
//                ((ImageAdapter) adapter).restoreImageSize((ImageAdapter.ImageViewHolder) viewHolder);
//            }
        }
    }

//    @Override
//    public boolean isLongPressDragEnabled() {
////        return super.isLongPressDragEnabled();
//        //禁止长按item可以上下拖拽，因为我们要自定义开启拖拽的时机
//        return false;
//    }
}