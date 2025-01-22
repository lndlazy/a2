package com.pi.connectraspberry.ui;


import com.pi.connectraspberry.R;
import com.pi.connectraspberry.ui.custom.CustomCropImageView;

public class EditActivity  extends BaseActivity{


    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit;
    }

    @Override
    protected void initView() {


       CustomCropImageView tvCIV = findViewById(R.id.tvCIV);



    }

    @Override
    protected void initData() {

    }


}
