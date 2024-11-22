package com.pi.connectraspberry;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class MyApplication extends Application {

    public static MyApplication instance;


    public static MyApplication getInstance() {


        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;


        Logger.addLogAdapter(new DiskLogAdapter());

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)//（可选）是否显示线程信息。默认值true
                .methodCount(0)           //（可选）要显示的方法行数。默认值2
                .methodOffset(7)       //（可选）隐藏内部方法调用到偏移量。默认值5
//                .logStrategy(customLog)//（可选）更改要打印的日志策略。默认LogCat （即android studio的日志输出Logcat）
                .tag("My custom tag")   //  //（可选）每个日志的全局标记。默认PRETTY_LOGGER .build
                .build();
//        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));//logcat日志
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));

//        Logger.clearLogAdapters();//移除所有日志适配器。如果你只想移除DiskLogAdapter，你需要在下一步把其他的添加回去。这有点蠢。
//        Logger.addLogAdapter(new AndroidLogAdapter());//比如这样

    }
}
