package com.pi.connectraspberry.ui;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.util.DensityUtil;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.SocketSender;
import com.pi.connectraspberry.util.SpUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingActivity extends BaseActivity implements View.OnClickListener {


    private TextView tvTime;
    private TextView tvHue;
    private TextView tvSat;
    private TextView tvBright;
    private TextView tvContrast;


    private int seconds;//播放时长
    private int hue;//色调调整偏移范围为0至360
    private int sat;//饱和度调整范围为-100至100
    private int bright;//亮度调整范围为-100至100
    private int contrast;//对比度调整范围为-100至100


    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {

        getWindow().setSoftInputMode(SOFT_INPUT_STATE_HIDDEN);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        TextView tvReset = findViewById(R.id.tvReset);

        tvTime = findViewById(R.id.tvTime);
        tvHue = findViewById(R.id.tvHue);
        tvSat = findViewById(R.id.tvSat);
        tvBright = findViewById(R.id.tvBright);
        tvContrast = findViewById(R.id.tvContrast);

        ConstraintLayout clTime = findViewById(R.id.clTime);
        ConstraintLayout clHue = findViewById(R.id.clHue);
        ConstraintLayout clSat = findViewById(R.id.clSat);
        ConstraintLayout clBright = findViewById(R.id.clBright);
        ConstraintLayout clContrast = findViewById(R.id.clContrast);

        clTime.setOnClickListener(this);
        clHue.setOnClickListener(this);
        clSat.setOnClickListener(this);
        clBright.setOnClickListener(this);
        clContrast.setOnClickListener(this);
        tvReset.setOnClickListener(this);

    }

    @Override
    protected void initData() {

        seconds = SpUtils.getConfig(SpUtils.PLAY_INTERVAL, 60);
        hue = SpUtils.getConfig(SpUtils.HUE, 0);
        sat = SpUtils.getConfig(SpUtils.SAT, 0);
        bright = SpUtils.getConfig(SpUtils.BRIGHT, 0);
        contrast = SpUtils.getConfig(SpUtils.CONTRAST, 0);

        tvTime.setText(seconds + "s");
        tvHue.setText(hue + "");
        tvSat.setText(sat + "");
        tvBright.setText(bright + "");
        tvContrast.setText(contrast + "");

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.clBright:
                //亮度调整范围为-100至100
                setPicBright();
                break;

            case R.id.clContrast:
                //对比度调整范围为-100至100
                setPicContrast();
                break;

            case R.id.clHue:
                //色调调整偏移范围为0至360
                setPicHue();
                break;

            case R.id.clSat:
                //饱和度调整范围为-100至100
                setPicSat();
                break;

            case R.id.clTime:
                //播放时长
                setPicPlayTimes();
                break;

            case R.id.tvReset:
                //重置設置
                resetConfig();
                break;

        }

    }

    /**
     * 重置設置
     */
    private void resetConfig() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.reset_confirm),
                        new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                //toast("click confirm");
                                SpUtils.putConfig(SpUtils.PLAY_INTERVAL, 60);
                                SpUtils.putConfig(SpUtils.HUE, 0);
                                SpUtils.putConfig(SpUtils.SAT, 0);
                                SpUtils.putConfig(SpUtils.BRIGHT, 0);
                                SpUtils.putConfig(SpUtils.CONTRAST, 0);

                                seconds = 60;
                                hue = 0;
                                sat = 0;
                                bright = 0;
                                contrast = 0;

                                tvTime.setText(60 + "s");
                                tvHue.setText(0 + "");
                                tvSat.setText(0 + "");
                                tvBright.setText(0 + "");
                                tvContrast.setText(0 + "");

                                syncConfig2Raspberry();
                            }
                        })
                .show();

    }

    /**
     * 播放时长设置
     */
    private void setPicPlayTimes() {

        showEditConfig(getResources().getString(R.string.set_play_interval), seconds, 60, 999, getResources().getString(R.string.play_interval_msg), value -> {
            seconds = value;
            SpUtils.putConfig(SpUtils.PLAY_INTERVAL, seconds);
            syncConfig2Raspberry();
            tvTime.setText(seconds + "s");
        });
    }

    private void syncConfig2Raspberry() {

        //通知raspberry pi更新配置
        boolean b = SocketSender.sendConfig();

        if (b) {
            showToast(getResources().getString(R.string.set_success));
        } else {
            showToast(getResources().getString(R.string.set_fail));
        }

    }

    /**
     * 饱和度调整范围为-100至100
     */
    private void setPicSat() {

        showEditConfig(getResources().getString(R.string.set_sat), sat, -100, 100, getResources().getString(R.string.set_sat_msg), value -> {
            sat = value;
            SpUtils.putConfig(SpUtils.SAT, sat);
            tvSat.setText(sat + "");
            syncConfig2Raspberry();
        });
    }

    /**
     * 色调调整偏移范围为0至360
     */
    private void setPicHue() {

        showEditConfig(getResources().getString(R.string.set_hue), hue, 0, 360, getResources().getString(R.string.set_hue_msg), value -> {
            hue = value;
            SpUtils.putConfig(SpUtils.HUE, hue);
            tvHue.setText(hue + "");
            syncConfig2Raspberry();
        });

    }

    /**
     * 对比度调整范围为-100至100
     */
    private void setPicContrast() {

        showEditConfig(getResources().getString(R.string.set_contrast), contrast, -100, 100, getResources().getString(R.string.set_contrast_msg), value -> {
            contrast = value;
            SpUtils.putConfig(SpUtils.CONTRAST, contrast);
            tvContrast.setText(contrast + "");
            syncConfig2Raspberry();
        });
    }

    /**
     * 亮度调整范围为-100至100
     */
    private void setPicBright() {

        showEditConfig(getResources().getString(R.string.set_bright), bright, -100, 100, getResources().getString(R.string.set_bright_msg), value -> {
            bright = value;
            SpUtils.putConfig(SpUtils.BRIGHT, bright);
            tvBright.setText(bright + "");
            syncConfig2Raspberry();
        });

    }

    private void showEditConfig(String title, int value, int min, int max, String msg, OnConfigListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_config_set, null);

        builder.setView(dialogView);

        builder.setCancelable(true);
        TextView tTitle = dialogView.findViewById(R.id.tvTitle);
        EditText etInput = dialogView.findViewById(R.id.etInput);
        TextView tMsg = dialogView.findViewById(R.id.tvMsg);
        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        TextView tvOk = dialogView.findViewById(R.id.tvOk);

        tTitle.setText(title);
        etInput.setText(value + "");
        tMsg.setText(msg);
        AlertDialog dialog = builder.create();

        tvCancel.setOnClickListener(v -> {
            hiddenConfigDialog(dialog);
        });

        tvOk.setOnClickListener(v -> {

            if (etInput.getText().toString().isEmpty()) {
                showToast(getResources().getString(R.string.input_empty));
                return;
            }

            try {
                int va = Integer.parseInt(etInput.getText().toString());

                if (va < min || va > max) {
                    showToast(getResources().getString(R.string.input_range) + min + "-" + max);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(getResources().getString(R.string.input_error));
                return;
            }

            if (listener != null)
                listener.onConfig(Integer.parseInt(etInput.getText().toString()));

            hiddenConfigDialog(dialog);
        });

        dialog.show();

    }

    private static void hiddenConfigDialog(AlertDialog d) {
        if (d != null && d.isShowing()) {
            d.dismiss();
        }
    }

    interface OnConfigListener {
        void onConfig(int value);
    }

}
