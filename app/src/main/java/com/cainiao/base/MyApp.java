package com.cainiao.base;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.tencent.bugly.crashreport.CrashReport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by 123 on 2019/9/23.
 */

public class MyApp extends Application{

    private static Application mContext;
    private static int log = 0; //剩余时间

    /**全局LiteOrm对象*/
    private static LiteOrm liteOrm;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        setDatabase();
        Utils.disable();
        disableAPIDialog();
        initBugly();
        MyToast.init(this, Const.DEBUG_MODE,true); //用法见https://github.com/hss01248/Toasty
    }

    public static Application getContext(){return mContext;}

    /**
     *
     * @param l
     */
    public static void setLog(int l){
        log = l;
    }

    /**
     * 获取剩余时间
     * @return
     */
    public static int getLog(){
        return log;
    }

    /**
     * 设置greenDao
     * 通过DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的SQLiteOpenHelper 对象。
     *可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为greenDAO 已经帮你做了。
     *注意：默认的DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
     *所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级
     */
    private void setDatabase(){
        if (liteOrm == null) {
            DataBaseConfig config = new DataBaseConfig(this, Const.DB_NAME, Const.DEBUG_MODE, 1, null);
            liteOrm = LiteOrm.newSingleInstance(config);
        }

    }

    public static LiteOrm getLiteOrm(){
        return liteOrm;
    }

    /**
     * 初始化bugly
     */
    private void initBugly(){
        // 获取当前进程名
//        String processName = AppUtil.getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程，主要是防止多进程的时候不同进程同时上报导致流量的损耗和性能的下降，这里设置只在主进程才上报
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
//        strategy.setUploadProcess(processName == null || processName.equals(getPackageName()));
        // 初始化Bugly
        CrashReport.initCrashReport(getApplicationContext(), Const.BUGLY_APP_ID, Const.DEBUG_MODE);

        //测试bugly的时候可以人为制造一个Crash，方便查看崩溃日志是否有在控制台输出以及是否有上传后台
//        CrashReport.testJavaCrash();  //用这行代码进行崩溃测试
    }

    /**
     * 反射 禁止9.0以上弹窗（Detected problems with API compatibility的错误对话框）
     */
    private void disableAPIDialog(){
        if (Build.VERSION.SDK_INT < 28) return;
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
