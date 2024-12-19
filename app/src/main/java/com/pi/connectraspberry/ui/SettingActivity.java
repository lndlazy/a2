package com.pi.connectraspberry.ui;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.pi.connectraspberry.R;
import com.pi.connectraspberry.HomeActivity;
import com.pi.connectraspberry.bean.ConfigBean;
import com.pi.connectraspberry.bean.EventBusBean;
import com.pi.connectraspberry.bean.EventbusType;
import com.pi.connectraspberry.util.FileUtils;
import com.pi.connectraspberry.util.MyCommand;
import com.pi.connectraspberry.util.SocketSender;
import com.pi.connectraspberry.util.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
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
    private boolean auto;//是否是自动切换模式
    private TextView tvMode;//自动、手动模式
    private TextView tvLanguage;//语言
    private LinearLayout llSetting;
    private ConstraintLayout clSat;

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
        tvMode = findViewById(R.id.tvMode);

        tvTime = findViewById(R.id.tvTime);
        tvHue = findViewById(R.id.tvHue);
        tvSat = findViewById(R.id.tvSat);
        tvBright = findViewById(R.id.tvBright);
        tvContrast = findViewById(R.id.tvContrast);
        tvLanguage = findViewById(R.id.tvLanguage);
        llSetting = findViewById(R.id.llSetting);

        ConstraintLayout clTime = findViewById(R.id.clTime);
        ConstraintLayout clHue = findViewById(R.id.clHue);
        clSat = findViewById(R.id.clSat);
        ConstraintLayout clBright = findViewById(R.id.clBright);
        ConstraintLayout clContrast = findViewById(R.id.clContrast);
        ConstraintLayout clSwitch = findViewById(R.id.clSwitch);
        ConstraintLayout clLanguage = findViewById(R.id.clLanguage);
        ConstraintLayout clClearData = findViewById(R.id.clClearData);
        ConstraintLayout clGetLog = findViewById(R.id.clGetLog);

        clTime.setOnClickListener(this);
        clHue.setOnClickListener(this);
        clSat.setOnClickListener(this);
        clBright.setOnClickListener(this);
        clContrast.setOnClickListener(this);
        tvReset.setOnClickListener(this);
        clSwitch.setOnClickListener(this);
        clLanguage.setOnClickListener(this);
        clClearData.setOnClickListener(this);
        clGetLog.setOnClickListener(this);

    }

    @Override
    protected void initData() {

        seconds = SpUtils.getConfig(SpUtils.PLAY_INTERVAL, 60);
        hue = SpUtils.getConfig(SpUtils.HUE, 0);
        sat = SpUtils.getConfig(SpUtils.SAT, 0);
        bright = SpUtils.getConfig(SpUtils.BRIGHT, 0);
        contrast = SpUtils.getConfig(SpUtils.CONTRAST, 0);
        auto = SpUtils.getBoolean(SpUtils.SWITCH, true);

        tvTime.setText(seconds + "s");
        tvHue.setText(hue + "");
        tvSat.setText(sat + "");
        tvBright.setText(bright + "");
        tvContrast.setText(contrast + "");
        tvMode.setText(auto ? getResources().getString(R.string.automatic_mode) : getResources().getString(R.string.manual_mode));

        String languagePreference = SpUtils.getLanguagePreference();

        switch (languagePreference) {

            case "zh":
                tvLanguage.setText("中文");

                break;
            case "en":
                tvLanguage.setText("English");

                break;
            case "ja":
                tvLanguage.setText("日本語");
                break;

        }

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

            case R.id.clSwitch:
                //模式切换
                modeSwitch();
                break;

            case R.id.tvReset:
                //重置設置
                resetConfig();
                break;

            case R.id.clLanguage:
                //选择语言
                choose();
                break;

            case R.id.clClearData:
                //清除数据
                showClearDataDialog();
                break;

            case R.id.clGetLog:
                //提取日志
                extractionLog();
                break;

        }

    }

    private void extractionLog() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.get_log_msg), new OnConfirmListener() {
            @Override
            public void onConfirm() {
                //toast("click confirm");
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        boolean b = SocketSender.sendCommand(MyCommand.Extraction_log);

                        if (b) {
                            //showToast(getResources().getString(R.string.clear_success));

                        } else {
                            showToast(getResources().getString(R.string.extraction_fail));
                        }

                    }
                }).start();
            }
        }).show();


    }

    private void showClearDataDialog() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.clear_data_msg), this::clearData).show();

    }

    /**
     * 清除数据
     */
    private void clearData() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean b = SocketSender.sendCommand(MyCommand.CLEAR_DATA);

                if (b) {
                    showToast(getResources().getString(R.string.clear_success));
                    EventBus.getDefault().post(new EventBusBean(EventbusType.CLEAR_MD5, ""));
                    //删除所有目录
                    clearFiles();
                } else {
                    showToast(getResources().getString(R.string.clear_fail));
                }

            }
        }).start();

    }


    private void clearFiles() {

        File folder = new File(FileUtils.getLocalBasePath());

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 如果是子文件夹，递归调用删除方法
                        String name = file.getName();
                        Log.d(TAG, "文件名称：" + name);
                        FileUtils.deleteDirectory(new File(FileUtils.getLocalBasePath() + name));

                    }
                }
            }

        }

    }

    private void choose() {
        new XPopup.Builder(this).atView(clSat).asCenterList(getResources().getString(R.string.choose_language), new String[]{"中文", "English", "日本語"}, new OnSelectListener() {
            @Override
            public void onSelect(int position, String text) {
                //toast("click " + text);

                String languageCode = "en";
                switch (position) {

                    case 0:
                        languageCode = "zh";

                        break;
                    case 1:
                        languageCode = "en";
//                        SpUtils.setLanguagePreference("en");
                        //updateLanguage("en");
                        break;
                    case 2:
//                        SpUtils.setLanguagePreference("ja");
                        //updateLanguage("ja");
                        languageCode = "ja";
                        break;

                }
                SpUtils.setLanguagePreference(languageCode);
//                updateLanguage(languageCode);
                tvLanguage.setText(text);
//                EventBus.getDefault().post(new EventBusBean(EventbusType.Language, languageCode));
                Log.d(TAG, "选中的是:" + text + "," + position);
                restart_app_notice();
            }
        }).show();

    }

    private void restart_app_notice() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.restart_app), new OnConfirmListener() {
            @Override
            public void onConfirm() {

                Intent intent = new Intent(context, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        }).show();
    }


    /**
     * 模式切换
     */
    private void modeSwitch() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(auto ? R.string.switch_to_manual : R.string.switch_to_auto), new OnConfirmListener() {
            @Override
            public void onConfirm() {
                //toast("click confirm");
                syncConfig2Raspberry(SpUtils.SWITCH, auto ? 1 : 0);
            }
        }).show();
    }

    /**
     * 重置設置
     */
    private void resetConfig() {

        new XPopup.Builder(this).asConfirm(getResources().getString(R.string.attention), getResources().getString(R.string.reset_confirm), new OnConfirmListener() {
            @Override
            public void onConfirm() {
                //toast("click confirm");

                syncConfig2Raspberry("reset", 0);
            }
        }).show();

    }

    /**
     * 播放时长设置
     */
    private void setPicPlayTimes() {

        showEditConfig(getResources().getString(R.string.set_play_interval), seconds, 60, 999, getResources().getString(R.string.play_interval_msg), value -> {

            syncConfig2Raspberry(SpUtils.PLAY_INTERVAL, value);

        });
    }

    private void syncConfig2Raspberry(String type, int value) {
        //通知raspberry pi更新配置
//        ThreadUtil.getParallelExecutor().execute(configRunnable);
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean b = SocketSender.sendConfig(getConfigBean(type, value));

                if (b) {
                    runOnUiThread(() -> setNowValue(type, value));
                } else {
                    showToast(getResources().getString(R.string.set_fail));
                }

            }
        }).start();

    }

    private ConfigBean getConfigBean(String type, int value) {

        ConfigBean configBean = new ConfigBean();
        configBean.setSeconds(SpUtils.getConfig(SpUtils.PLAY_INTERVAL, 60));
        configBean.setHue(SpUtils.getConfig(SpUtils.HUE, 0));
        configBean.setSat(SpUtils.getConfig(SpUtils.SAT, 0));
        configBean.setBright(SpUtils.getConfig(SpUtils.BRIGHT, 0));
        configBean.setContrast(SpUtils.getConfig(SpUtils.CONTRAST, 0));
        configBean.setAuto(SpUtils.getBoolean(SpUtils.SWITCH, true));//0是自动， 1是手动

        switch (type) {
            case SpUtils.SAT:
                configBean.setSat(value);
                break;
            case SpUtils.HUE:
                configBean.setHue(value);
                break;
            case SpUtils.BRIGHT:
                configBean.setBright(value);
                break;
            case SpUtils.CONTRAST:
                configBean.setContrast(value);
                break;
            case SpUtils.PLAY_INTERVAL:
                configBean.setSeconds(value);
                break;
            case SpUtils.SWITCH:
                configBean.setAuto(value == 0);
                break;
            case "reset":
                configBean.setSeconds(60);
                configBean.setHue(0);
                configBean.setSat(0);
                configBean.setBright(0);
                configBean.setContrast(0);
                configBean.setAuto(true);
                break;
        }

        return configBean;
    }


    private void setNowValue(String type, int value) {

        showToast(getResources().getString(R.string.set_success));

        switch (type) {

            case SpUtils.SAT:
                sat = value;
                SpUtils.putConfig(SpUtils.SAT, sat);
                tvSat.setText(sat + "");
                break;

            case SpUtils.HUE:
                hue = value;
                SpUtils.putConfig(SpUtils.HUE, hue);
                tvHue.setText(hue + "");
                break;

            case SpUtils.BRIGHT:
                bright = value;
                SpUtils.putConfig(SpUtils.BRIGHT, bright);
                tvBright.setText(bright + "");
                break;

            case SpUtils.CONTRAST:
                contrast = value;
                SpUtils.putConfig(SpUtils.CONTRAST, contrast);
                tvContrast.setText(contrast + "");
                break;

            case SpUtils.PLAY_INTERVAL:
                seconds = value;
                SpUtils.putConfig(SpUtils.PLAY_INTERVAL, seconds);
                tvTime.setText(seconds + "s");
                break;

            case SpUtils.SWITCH:
                auto = value == 0;
                SpUtils.putBoolean(SpUtils.SWITCH, auto);
                tvMode.setText(auto ? getResources().getString(R.string.automatic_mode) : getResources().getString(R.string.manual_mode));
                break;

            case "reset":
                SpUtils.putConfig(SpUtils.PLAY_INTERVAL, 60);
                SpUtils.putConfig(SpUtils.HUE, 0);
                SpUtils.putConfig(SpUtils.SAT, 0);
                SpUtils.putConfig(SpUtils.BRIGHT, 0);
                SpUtils.putConfig(SpUtils.CONTRAST, 0);
                SpUtils.putBoolean(SpUtils.SWITCH, true);

                seconds = 60;
                hue = 0;
                sat = 0;
                bright = 0;
                contrast = 0;
                auto = true;

                tvTime.setText(60 + "s");
                tvHue.setText(0 + "");
                tvSat.setText(0 + "");
                tvBright.setText(0 + "");
                tvContrast.setText(0 + "");
                tvMode.setText(auto ? getResources().getString(R.string.automatic_mode) : getResources().getString(R.string.manual_mode));

                break;
        }
    }


//    Runnable configRunnable = () -> {
//        boolean b = SocketSender.sendConfig();
//
//        if (b) {
//            showToast(getResources().getString(R.string.set_success));
//        } else {
//            showToast(getResources().getString(R.string.set_fail));
//        }
//    };


    /**
     * 饱和度调整范围为-100至100
     */
    private void setPicSat() {

        showEditConfig(getResources().getString(R.string.set_sat), sat, -100, 100, getResources().getString(R.string.set_sat_msg), value -> {

            syncConfig2Raspberry(SpUtils.SAT, value);
        });
    }

    /**
     * 色调调整偏移范围为0至360
     */
    private void setPicHue() {

        showEditConfig(getResources().getString(R.string.set_hue), hue, 0, 360, getResources().getString(R.string.set_hue_msg), value -> {

            syncConfig2Raspberry(SpUtils.HUE, value);
        });

    }

    /**
     * 对比度调整范围为-100至100
     */
    private void setPicContrast() {

        showEditConfig(getResources().getString(R.string.set_contrast), contrast, -100, 100, getResources().getString(R.string.set_contrast_msg), value -> {

            syncConfig2Raspberry(SpUtils.CONTRAST, value);
        });
    }

    /**
     * 亮度调整范围为-100至100
     */
    private void setPicBright() {

        showEditConfig(getResources().getString(R.string.set_bright), bright, -100, 100, getResources().getString(R.string.set_bright_msg), value -> {

            syncConfig2Raspberry(SpUtils.BRIGHT, value);
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

            if (listener != null) listener.onConfig(Integer.parseInt(etInput.getText().toString()));

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
