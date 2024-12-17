package com.pi.connectraspberry.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pi.connectraspberry.R;
import com.pi.connectraspberry.toast.ToastUtil;
import com.pi.connectraspberry.util.CommUtils;
import com.pi.connectraspberry.util.SpUtils;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    protected Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏导航栏
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        context = this;

        setContentView(getLayoutId());
        initView();
        initData();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = SpUtils.getLanguagePreference(); // 从 SharedPreferences等地方获取语言代码
        Log.d(TAG, "获取保存的语言代码: " + languageCode);
        Locale locale = new Locale(languageCode);
        Context context = new ContextWrapper(newBase);
        context.getResources().getConfiguration().locale = locale;
        super.attachBaseContext(context);
    }


    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        v.clearFocus();
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

//    protected void updateLanguage(String languageCode) {
//        Locale locale = new Locale(languageCode);
//// 创建ContextWrapper对象
//        Context context = new ContextWrapper(this);
//        Configuration configuration = context.getResources().getConfiguration();
//        configuration.locale = locale;
//        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
//    }

    protected void showToast(String msg) {

        if (CommUtils.isMainLooper()) {

            ToastUtil.show(msg);
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else
            runOnUiThread(() -> ToastUtil.show(msg));
        // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());

    }

    protected void startActivity(Class<?> clazz) {
        Intent intent = new Intent(context, clazz);
        startActivity(intent);
    }

    protected ProgressDialog dialog;

    protected void showProgressDialog(String msg) {
        hideProgressDialog();
        if (CommUtils.isMainLooper()) {
            showProgress(msg);
        } else
            runOnUiThread(() -> showProgress(msg));
    }

    private void showProgress(String msg) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
        }
        dialog.setMessage(msg);
        if (!dialog.isShowing())
            dialog.show();
    }

    protected void hideProgressDialog() {

        if (CommUtils.isMainLooper()) {
            hideProgress();
        } else
            runOnUiThread(() -> hideProgress());
    }

    private void hideProgress() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        hideProgressDialog();

    }
}
