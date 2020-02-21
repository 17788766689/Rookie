package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.BuyerNum;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HttpClient;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 聚人气
 */
public class JRQAction extends BaseAction {
    private boolean isStart;

    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String buyerId = "";
    private String token = "";

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
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
        sendLog(MyApp.getContext().getString(R.string.being_login));
        StringBuffer time = new StringBuffer();
        time.append(new Date().getTime());
        String str = time.substring(0,10);
        StringBuffer sign = new StringBuffer();
        sign.append(str);
        sign.append("081f86689f678a886bcce3598e514bffc7383e4e0b");

        Map map = new HashMap();
        map.put("sPhone", mParams.getAccount());
        map.put("sPassword", mParams.getPassword());
        String param = JSON.toJSONString(map);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, param);

        HttpClient.getInstance().post("/api/user/login", mPlatform.getHost())
                .upRequestBody(body)
                .headers("sign", Utils.md5(sign.toString()))
                .headers("time",str)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .headers("Referer","http://sou.811712.com/")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 200) {    //登录成功
                                token = jsonObject.getJSONObject("data").getString("sToken");
                                sendLog("登录成功！");
                                updateParams(mPlatform);
                                startTask();
                            } else {
                                sendLog(jsonObject.getString("message"));
                                MyToast.error(jsonObject.getString("message"));
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        StringBuffer time = new StringBuffer();
        time.append(new Date().getTime());
        String str = time.substring(0,10);
        StringBuffer sign = new StringBuffer();
        sign.append(str);
        sign.append("081f86689f678a886bcce3598e514bffc7383e4e0b");

        HttpClient.getInstance().get("/api/taskshokey/OneButtonAutomatic", mPlatform.getHost())
                .headers("sign", Utils.md5(sign.toString()))
                .headers("time",str)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Mobile Safari/537.36")
                .headers("Referer","http://sou.811712.com/")
                .headers("token",token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject array = JSONObject.parseObject(response.body());
                            if (array.getInteger("code") == 200){
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.jurenqi, 3000);
                                    addTask("聚人气");
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                    stop();
                            }else {
                                sendLog(array.getString("message"));
                            }

                        } catch (Exception e) {
                            sendLog("检测任务异常！");
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
