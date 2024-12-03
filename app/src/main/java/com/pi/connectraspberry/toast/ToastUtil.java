package com.pi.connectraspberry.toast;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pi.connectraspberry.MyApplication;
import com.pi.connectraspberry.R;

public class ToastUtil {
//
//    private static Toast mToast;
//    private static TextView mTextView;
////    private TimeCount timeCount;
//    private String message;
//    private Handler mHandler = new Handler();
//    private boolean canceled = true;
//
//    public static void init() {
//
//        LayoutInflater inflater = (LayoutInflater) MyApplication.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        //自定义布局
//        View view = inflater.inflate(R.layout.layout_toast, null);
//        //自定义toast文本
//        mTextView = view.findViewById(R.id.tvToast);
//
//        Log.i("ToastUtil", "Toast start...");
//        if (mToast == null) {
//            mToast = new Toast(MyApplication.getInstance());
//            Log.i("ToastUtil", "Toast create...");
//        }
//        //设置toast居中显示
//        mToast.setGravity(Gravity.CENTER, 0, 0);
//        mToast.setDuration(Toast.LENGTH_SHORT);
//        mToast.setView(view);
//    }

    /**
     * 自定义居中显示toast
     */
    public static void show(String msg) {

//        if (mToast == null) {
//            init();
//        }
//        mToast = Toast.makeText(MyApplication.getInstance(), msg, Toast.LENGTH_SHORT);
//        LayoutInflater inflater = LayoutInflater.from(MyApplication.getInstance());
//        View newView = inflater.inflate(R.layout.layout_toast, null);
//        mToast.setView(newView); // This can cause the error if not done carefully
//        mToast.setGravity(Gravity.CENTER, 0, 0);
//        mToast.setText(msg);
//        mToast.show();

        Toast toast = new Toast(MyApplication.getInstance());
        LayoutInflater inflater = LayoutInflater.from(MyApplication.getInstance());
        View layout = inflater.inflate(R.layout.layout_toast, null, false);
        TextView toastTv = layout.findViewById(R.id.tvToast);
        toastTv.setText(msg);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

    }

//    /**
//     * 自定义时长、居中显示toast
//     *
//     * @param duration
//     */
//    public void show(int duration) {
//        timeCount = new TimeCount(duration, 1000);
//        Log.i("ToastUtil", "Toast show...");
//        if (canceled) {
//            timeCount.start();
//            canceled = false;
//            showUntilCancel();
//        }
//    }

//    /**
//     * 隐藏toast
//     */
//    private void hide() {
//        if (mToast != null) {
//            mToast.cancel();
//        }
//        canceled = true;
//        Log.i("ToastUtil", "Toast that customed duration hide...");
//    }
//
//    private void showUntilCancel() {
//        if (canceled) { //如果已经取消显示，就直接return
//            return;
//        }
//        mToast.show();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("ToastUtil", "Toast showUntilCancel...");
//                showUntilCancel();
//            }
//        }, Toast.LENGTH_LONG);
//    }
//
//    /**
//     * 自定义计时器
//     */
//    private class TimeCount extends CountDownTimer {
//
//        public TimeCount(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval); //millisInFuture总计时长，countDownInterval时间间隔(一般为1000ms)
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//            mTextView.setText(message + ": " + millisUntilFinished / 1000 + "s后消失");
//        }
//
//        @Override
//        public void onFinish() {
//            hide();
//        }
//    }
}
