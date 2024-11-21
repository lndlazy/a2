package com.pi.connectraspberry.callback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        // 设置渐隐动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0f);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束后，调用默认的删除动画方法
                dispatchRemoveFinished(holder);
            }
        });
        animator.start();
        return true;
    }
}