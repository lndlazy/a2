package com.pi.connectraspberry.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CustomCropImageView extends ImageView {

    private RectF mCropRect;
    private float mAspectRatio = 2160f / 3060f;
    private Paint mCropBorderPaint;
    private Paint mMaskPaint;

    public CustomCropImageView(Context context) {
        this(context, null);
    }

    public CustomCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCropBorderPaint = new Paint();
        mCropBorderPaint.setColor(Color.WHITE);
        mCropBorderPaint.setStyle(Paint.Style.STROKE);
        mCropBorderPaint.setStrokeWidth(5);

        mMaskPaint = new Paint();
        mMaskPaint.setColor(Color.BLACK);
        mMaskPaint.setAlpha(128);

        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 初始化裁剪区域
        int cropWidth = w;
        int cropHeight = (int) (cropWidth / mAspectRatio);
        if (cropHeight > h) {
            cropHeight = h;
            cropWidth = (int) (cropHeight * mAspectRatio);
        }
        int left = (w - cropWidth) / 2;
        int top = (h - cropHeight) / 2;
        mCropRect = new RectF(left, top, left + cropWidth, top + cropHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制遮罩
        Path maskPath = new Path();
        maskPath.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        maskPath.addRect(mCropRect, Path.Direction.CCW);
        canvas.drawPath(maskPath, mMaskPaint);

        // 绘制裁剪框
        canvas.drawRect(mCropRect, mCropBorderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理触摸事件，调整裁剪框位置和大小
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检查触摸点是否在裁剪框内或边缘
                break;
            case MotionEvent.ACTION_MOVE:
                // 根据触摸点移动调整裁剪框
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    public Bitmap getCroppedBitmap() {
        // 获取裁剪后的图片
        Bitmap originalBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        int left = (int) mCropRect.left;
        int top = (int) mCropRect.top;
        int right = (int) mCropRect.right;
        int bottom = (int) mCropRect.bottom;
        return Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top);
    }
}