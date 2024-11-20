//package com.pi.connectraspberry.animate;
//
//import android.util.Log;
//import android.widget.ImageView;
//
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.pi.connectraspberry.R;
//
//public class MyItemAnimator extends DefaultItemAnimator {
//    private static final String TAG = "MyItemAnimator";
//
//    @Override
//    public boolean animateMove(RecyclerView.ViewHolder viewHolder, int fromX, int fromY, int toX, int toY) {
//        // 在移动（拖动）项目时添加阴影效果
//        //Log.d(TAG, "animateMove: ");
//        //viewHolder.itemView.setElevation(18f); // 设置阴影高度，可根据需要调整
//        return super.animateMove(viewHolder, fromX, fromY, toX, toY);
//    }
//
//    @Override
//    public void endAnimation(RecyclerView.ViewHolder item) {
//        // 动画结束后，清除阴影
//        Log.d(TAG, "endAnimation: ");
//        ImageView iv = item.itemView.findViewById(R.id.img);
//        iv.setScaleX(1);
//        iv.setScaleY(1);
//        //item.itemView.setElevation(0f);
//        super.endAnimation(item);
//    }
//}
