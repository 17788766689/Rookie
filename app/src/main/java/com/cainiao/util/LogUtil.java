package com.cainiao.util;

import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by WJH on 2017/6/28.
 */

public class LogUtil {

    /**日志输出的tag*/
    private static final String TAG = "cainiao";
    /**用来控制日志输入的当前级别*/
    private static final int CUR_LEVEL = 5;
    /**verbose级别的log*/
    private static final int V_LEVEL = 1;
    /**debug级别的log*/
    private static final int D_LEVEL = 2;
    /**info级别的log*/
    private static final int I_LEVEL = 3;
    /**warn级别的log*/
    private static final int W_LEVEL = 4;
    /**error级别的log*/
    private static final int E_LEVEL = 5;

    private static StringBuffer sb;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LogUtil() {throw new UnsupportedOperationException("Could not instantiate in a private constructor!");}

    /**打印verbose级别的log*/
    public static void v(String text){
        if (Const.DEBUG_MODE && CUR_LEVEL >= V_LEVEL){
            Log.v(TAG, text);
        }
    }

    /**打印debug级别的log*/
    public static void d(String text){
        if (Const.DEBUG_MODE && CUR_LEVEL >= D_LEVEL){
            Log.d(TAG, text);
        }
    }

    /**打印info级别的log*/
    public static void i(String text){
        if (Const.DEBUG_MODE && CUR_LEVEL >= I_LEVEL){
            Log.i(TAG, text);
        }
    }

    /**打印warn级别的log*/
    public static void w(String text){
        if (Const.DEBUG_MODE && CUR_LEVEL >= W_LEVEL){
            Log.w(TAG, text);
        }
    }

    /**打印error级别的log*/
    public static void e(String text){
        if (Const.DEBUG_MODE && CUR_LEVEL >= E_LEVEL){
            Log.e(TAG, text);
        }
    }

    public static String formatLog(String text){
        if(TextUtils.isEmpty(text)) return "";
        return new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": " + text;
    }
}
