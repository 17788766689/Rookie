package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HYNCUtils;
import com.cainiao.util.HttpClient;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.Random;

/**
 * 小青蛙
 */
public class XQWAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            cookie = "";
            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            login();
        }
    }

    /**
     * 登录
     */
    private void login() {
        String r = HYNCUtils.md5("account"+mParams.getAccount()+"password"+mParams.getPassword()+"~244!@#~$$~");
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/user/login", mPlatform.getHost())
                .params("account", mParams.getAccount())
                .params("password", mParams.getPassword())
                .headers("Cookie","tmp_cc="+r)
                .headers("Referer","http://www.shenjimao.com/wap/")
                .headers("Content-Type", "application/json")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; 8 Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.143 Crosswalk/24.53.595.0 XWEB/112 MMWEBSDK/180803 Mobile Safari/537.36 MMWEBID/4719 MicroMessenger/6.7.3.1360(0x260703B3) NetType/WIFI Language/zh_CN Process/tools")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if ("登录成功".equals(jsonObject.getString("msg"))) {    //登录成功
                                cookie = jsonObject.getJSONObject("data").getString("token");
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
                            } else {
                                MyToast.error(jsonObject.getString("msg"));
                                stop();
                            }
                        } catch (Exception e) {
                            stop();
                            sendLog("登录异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        if(isStart == false){
            return;
        }
        HttpClient.getInstance().post("/task", mPlatform.getHost())
                .headers("Auth-Token", cookie)
                .headers("Referer","http://www.shenjimao.com/home/")
                .headers("Content-Type", "application/json")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; 8 Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.143 Crosswalk/24.53.595.0 XWEB/112 MMWEBSDK/180803 Mobile Safari/537.36 MMWEBID/4719 MicroMessenger/6.7.3.1360(0x260703B3) NetType/WIFI Language/zh_CN Process/tools")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (obj.getString("msg").equals("领取成功")) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.xiaoqingwa, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(obj.getString("msg"));  //继续检测任务
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (isStart) {
                            //取最小频率和最大频率直接的随机数值作为刷单间隔
                            int period = mRandom.nextInt(mParams.getMaxFrequency() - mParams.getMinFrequency()) + mParams.getMinFrequency();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startTask();
                                }
                            }, period);
                        }
                    }
                });
    }


    @Override
    public void stop() {
        if (!isStart) return;   //如果当前状态是未开始，则不做任何操作
        super.stop();
        isStart = false;
        //主动点击停止抢单，则还原初始状态。  注意：抢单成功之后不要直接调用stop方法，
        // 否则状态会变成初始状态而不是“抢单成功”的状态。抢单成功直接把isStart设为false即可
        updateStatus(mPlatform, Const.WGHS);
    }
}
