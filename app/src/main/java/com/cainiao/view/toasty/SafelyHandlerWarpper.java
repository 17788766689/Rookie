package com.cainiao.view.toasty;

import android.os.Handler;
import android.os.Message;

/**
 * Created by huangshuisheng on 2018/5/22.
 */

public class SafelyHandlerWarpper extends Handler{
    private Handler impl;

    public SafelyHandlerWarpper(Handler impl) {
        this.impl = impl;
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        impl.handleMessage(msg);//需要委托给原Handler执行
    }

}
