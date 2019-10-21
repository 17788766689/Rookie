package com.cainiao.view.toasty;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public class MyToast {
     static Context context;
    private static boolean isDebug;
    private static Handler mainHanlder;

    private static List<Toast> mToasts;
    private static Toast toast;


    private static Handler getMainHanlder(){
        if(mainHanlder == null){
            mainHanlder = new Handler(Looper.getMainLooper());
        }
        return mainHanlder;
    }

    private static boolean isMainThread(){
        long threadId = Thread.currentThread().getId();
        if(context  == null){
            return false;
        }
        long mainThreadId = context.getMainLooper().getThread().getId();
        return threadId == mainThreadId;
    }

    private static void runSafe(final Runnable runnable){
        if(isMainThread()){
            try {
                runnable.run();
            }catch (Exception e){
                e.printStackTrace();
            }

        }else {
            getMainHanlder().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     *
     * @param context applicationcontext
     * @param isDebug 是测试环境还是正式环境
     * @param showInCenter 显示在什么地方.默认在底部,可以设置为屏幕中央.全局起作用
     */
    public static void init(Application context, boolean isDebug, boolean showInCenter){
        MyToast.context = context;
        MyToast.isDebug = isDebug;
        Toasty.isCenter = showInCenter;
        context.registerActivityLifecycleCallbacks(new ToastyActivityLifeCallback());
        mToasts = new ArrayList<>();
    }

    public static void setDefaultSuccessColor(String color){
        if(!TextUtils.isEmpty(color)){
            ToasyDefaultConfig.COLOR_SUCCESS = color;
        }
    }
    public static void setDefaultInfoColor(String color){
        if(!TextUtils.isEmpty(color)){
            ToasyDefaultConfig.COLOR_INFO = color;
        }
    }
    public static void setDefaultWarnColor(String color){
        if(!TextUtils.isEmpty(color)){
            ToasyDefaultConfig.COLOR_WARING = color;
        }
    }
    public static void setDefaultErrorColor(String color){
        if(!TextUtils.isEmpty(color)){
            ToasyDefaultConfig.COLOR_ERROR = color;
        }
    }
    public static void setDefaultTextColor(String color){
        if(!TextUtils.isEmpty(color)){
            ToasyDefaultConfig.COLOR_DEFAULT_TEXT = color;
        }
    }


    public static void success(int stringResId){
        success(ToastyUtils.getStr(stringResId));
    }

    public static void success(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }

        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.success(context, text, Toast.LENGTH_SHORT, true));
                show();
            }
        });

    }

    public static void error(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.error(context, text, Toast.LENGTH_SHORT, true));
                show();
            }
        });

    }

    public static void error(int stringResId){
        error(ToastyUtils.getStr(stringResId));
    }

    public static void info(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.info(context, text, Toast.LENGTH_SHORT, true));
                show();
            }
        });

    }

    public static void info(int stringResId){
        info(ToastyUtils.getStr(stringResId));
    }

    public static void warn(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.warning(context, text, Toast.LENGTH_SHORT, true));
                show();
            }
        });

    }

    public static void warn(int stringResId){
        warn(ToastyUtils.getStr(stringResId));
    }


    public static void show(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.normal(context, text, Toast.LENGTH_SHORT));
                show();
            }
        });

    }

    public static void show(final CharSequence text , final int resId){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.normal(context, text, context.getResources().getDrawable(resId)));
                show();
            }
        });

    }

    public static void show(int stringResId){
        show(ToastyUtils.getStr(stringResId));
    }

    public static void debug(CharSequence text) {
        if(TextUtils.isEmpty(text)){
            return;
        }
        if(isDebug){
            show(text);
        }
    }

    public static void debug(int stringResId){
        debug(ToastyUtils.getStr(stringResId));
    }


    /*
    public static void successBig(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.successBig(context, text, Toast.LENGTH_SHORT));
                show();
            }
        });
    }

    public static void successBig(int stringResId){
        successBig(ToastyUtils.getStr(stringResId));
    }

    public static void errorBig(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.errorBig(context, text, Toast.LENGTH_SHORT));
                show();
            }
        });
    }

    public static void errorBig(int stringResId){
        errorBig(ToastyUtils.getStr(stringResId));
    }
*/
    public static void successL(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.success(context, text, Toast.LENGTH_LONG, true));
                show();
            }
        });

    }

    public static void successL(int stringResId){
        successL(ToastyUtils.getStr(stringResId));
    }

    public static void errorL(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.error(context, text, Toast.LENGTH_LONG, true));
                show();
            }
        });

    }

    public static void errorL(int stringResId){
        errorL(ToastyUtils.getStr(stringResId));
    }

    public static void infoL(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.info(context, text, Toast.LENGTH_LONG, true));
                show();
            }
        });

    }

    public static void infoL(int stringResId){
        infoL(ToastyUtils.getStr(stringResId));
    }


    public static void warnL(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.warning(context, text, Toast.LENGTH_LONG, true));
                show();
            }
        });

    }

    public static void warnL(int stringResId){
        warnL(ToastyUtils.getStr(stringResId));
    }


    public static void showL(final CharSequence text){
        if(TextUtils.isEmpty(text)){
            return;
        }
        runSafe(new Runnable() {
            @Override
            public void run() {
                mToasts.add(Toasty.normal(context, text, Toast.LENGTH_LONG));
                show();
            }
        });

    }

    public static void showL(int stringResId){
        showL(ToastyUtils.getStr(stringResId));
    }




    public static void debugL(CharSequence text) {
        if(TextUtils.isEmpty(text)){
            return;
        }
        if(isDebug){
            showL(text);
        }

    }

    public static void debugL(int stringResId){
        debugL(ToastyUtils.getStr(stringResId));
    }

    /**
     * 显示toast
     */
    private static void show(){
        if(mToasts.size() <= 0 || toast != null) return;
        toast = mToasts.remove(0);
        setDismissListener();
        toast.show();
    }

    /**
     * 监听toast消失事件
     */
    private static void setDismissListener(){
        View toastLayout = toast.getView();
        toastLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                toastDismiss();
            }
        });
    }

    /**
     * toast消失处理的逻辑
     */
    private static void toastDismiss(){
        if(mToasts.size() > 0){
            toast = mToasts.remove(0);
            setDismissListener();
            toast.show();
        }else{
            toast = null;
        }
    }
}
