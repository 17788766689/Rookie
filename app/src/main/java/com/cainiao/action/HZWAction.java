package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 海贼王
 */
public class HZWAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";
    private String token;
    private String userId;
    private Integer count;

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
        count = 0;
        sendLog("正在登录...");
        HttpClient.getInstance().post("/user/login", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("password", mParams.getPassword())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (0 == jsonObject.getInteger("code")) {    //登录成功
                                sendLog("登录成功");
                                List<String> cookies = response.headers().values("Set-Cookie");
                                for (String str : cookies) {
                                    cookie += str.substring(0, str.indexOf(";"));
                                }
                                token = jsonObject.getJSONObject("data").getString("token");
                                userId = jsonObject.getJSONObject("data").getString("id");
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
                            } else {
                                sendLog(jsonObject.getString("msg"));
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
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONObject obj = jsonObject.getJSONObject("error");
                            sendLog(obj.getString("details"));  //接单异常
                            stop();
                        } catch (Exception e) {
                            stop();
                            sendLog("登录异常");  //接单异常
                        }
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount(String id) {
        long n = new Date().getTime();
        HttpClient.getInstance().get("/order/order-details", mPlatform.getHost())
                .params("token", token)
                .params("userId", userId)
                .params("type", "2")
                .params("id", id)
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONObject array = jsonObject.getJSONObject("data");
                            if (jsonObject.getInteger("code") == 0) {    //获取买号成功
                                if(count == 0){
                                    count++;
                                    lqTask(array.getString("taoname"),id);
                                }
                            } else { //无可用的买号
                               sendLog(jsonObject.getString("msg"));
                            }
                        } catch (Exception e) {
                            sendLog("获取买号异常！");
                            stop();
                        }
                    }

                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().get("/order/index", mPlatform.getHost())
                .params("token", token)
                .params("page", "1")
                .params("pageSize", "20")
                .params("type", "2")
                .headers("Cookie", cookie)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            JSONArray array = obj.getJSONObject("data").getJSONArray("data");
                            for (int i = 0, len = array.size(); i < len; i++) {
                                JSONObject object = array.getJSONObject(i);
                                if (object.getInteger("detection") == 1) {
                                    if(object.getDouble("commission") >= mParams.getMinCommission() && object.getDouble("commodity_price") <= mParams.getMaxPrincipal()){
                                        sendLog("检测到任务领取中");
                                        getAccount(object.getString("id"));
                                    }
                                }
                            }
                            sendLog("继续检测任务");
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

    /**
     * 领取任务
     * @param taobaoName
     * @param taskId
     */
    private void lqTask(String taobaoName,String taskId) {
        long n = new Date().getTime();
        HttpClient.getInstance().get("/order/order", mPlatform.getHost())
                .params("token", token)
                .params("type", "2")
                .params("taoname", taobaoName)
                .params("id",taskId)
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 0) {    //接单成功
                                sendLog("接单成功,可前往App进行做单");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.haizeiwang, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(jsonObject.getString("msg"));
                            }
                        } catch (Exception e) {
                            sendLog("领取任务异常！");
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
