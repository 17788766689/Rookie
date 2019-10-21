package com.cainiao.base;

import android.app.Application;
import android.os.Build;

import com.cainiao.util.Const;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by 123 on 2019/9/23.
 */

public class MyApp extends Application{

    private static Application mContext;
    private static int mTime = 0; //剩余时间

    /**全局LiteOrm对象*/
    private static LiteOrm liteOrm;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        setDatabase();
        Utils.disable();
        disableAPIDialog();
        MyToast.init(this, Const.DEBUG_MODE,true); //用法见https://github.com/hss01248/Toasty
    }

    public static Application getContext(){return mContext;}

    /**
     * 设置剩余时间
     * @param time
     */
    public static void setTime(int time){
        mTime = time;
    }

    /**
     * 获取剩余时间
     * @return
     */
    public static int getTime(){
        return mTime;
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
