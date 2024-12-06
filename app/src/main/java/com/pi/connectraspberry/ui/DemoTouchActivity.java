package com.pi.connectraspberry.ui;

import android.graphics.Paint;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.pi.connectraspberry.R;

public class DemoTouchActivity extends  BaseActivity{

    private TextView textView;
    private Paint paint;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {
        textView = findViewById(R.id.textView);

    }

    @Override
    protected void initData() {
        paint = textView.getPaint();
    }



//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//
//        int[] location = new int[2];
//        textView.getLocationOnScreen(location);
//        int left = location[0];
//        int top = location[1];
//
//        if (x >= left && x <= left + textView.getWidth() && y >= top && y <= top + textView.getHeight()) {
//            Layout layout = textView.getLayout();
//            int line = layout.getLineForVertical(y - top);
//            int offset = layout.getOffsetForHorizontal(line, x - left);
//
//            // 获取TextView的Paint对象，用于测量字符宽度
//            Paint paint = textView.getPaint();
//            // 记录当前位置到字符起始位置的宽度累计值
//            float widthSum = 0;
//            // 从当前偏移量往前逐个字符检查宽度
//            for (int i = offset; i >= 0; i--) {
//                String charText = String.valueOf(textView.getText().toString().charAt(i));
//                widthSum += paint.measureText(charText);
//                if (widthSum <= x - left) {
//                    offset = i;
//                } else {
//                    break;
//                }
//            }
//
//            String text = textView.getText().toString();
//            if (offset >= 0 && offset < text.length()) {
//                String clickedText = text.substring(offset);
//                int endIndex = clickedText.indexOf("\n");
//                if (endIndex!= -1) {
//                    clickedText = clickedText.substring(0, endIndex);
//                }
//                Log.d("ClickedText", "点击位置的文字: " + clickedText);
//            }
//        }
//        return super.onTouchEvent(event);
//    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int[] location = new int[2];
        textView.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + textView.getWidth();
        int bottom = top + textView.getHeight();
        if (x >= left && x <= right && y >= top && y <= bottom) {
            // 计算点击位置在文本中的偏移量
            Layout layout = textView.getLayout();
            int offset = layout.getOffsetForHorizontal(layout.getLineForVertical(y - top), x - left);
            String text = textView.getText().toString();
            if (offset >= 0 && offset < text.length()) {
                String clickedText = text.substring(offset);
                int endIndex = clickedText.indexOf("\n");
                if (endIndex!= -1) {
//                    clickedText = clickedText.substring(0, endIndex);
                    clickedText = clickedText.substring(0, offset);
                }

                Log.d("ClickedText", "点击位置的文字: " + clickedText);
            }
        }
        return super.onTouchEvent(event);
    }
}
