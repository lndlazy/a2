package com.pi.connectraspberry.ui;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.pi.connectraspberry.R;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());


    }
}
