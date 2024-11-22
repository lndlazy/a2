package com.pi.connectraspberry.ui;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.pi.connectraspberry.R;

public class SettingActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {

    }
}
