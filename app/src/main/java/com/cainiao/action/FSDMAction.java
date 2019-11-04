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
import com.cainiao.util.LogUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.google.gson.JsonObject;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
 * 丰收大麦
 */
public class FSDMAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String yzmKey;
    private String accessToken;
    private String secret;
    private String userId;
    private String platform;
    private String status;
    private String shopName;

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
     * 丰收大麦
     */
    public void getVerifyCode(Platform platform) {
        HttpClient.getInstance().get(":8081/workerLogin?time=" + new Date().getTime(), "http://wx.xdhfnch.cn")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (TextUtils.isEmpty(response.body())) return;
                        String reData = response.body().toString();
                        int index = reData.indexOf("captcha/gen_img?key=");
                        yzmKey = reData.substring(index + 20, index + 67);
                        String imgUrl = "http://api.fsdmff.cn:18087/captcha/gen_img?key=" + yzmKey + "&time=" + new Date();
                        sendMsg("get_verifycode", imgUrl);
                    }
                });
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post(":18087/auth/login_4_worker_web", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("pwd", mParams.getPassword())
                .params("captcha", mParams.getVerifyCode())
                .params("captchaKey", yzmKey)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (2000 == jsonObject.getIntValue("code")) {    //登录成功
                                sendLog("登录成功！");
                                accessToken = jsonObject.getJSONObject("data").getString("accessToken");
                                secret = jsonObject.getJSONObject("data").getString("secret");
                                userId = jsonObject.getJSONObject("data").getString("userId");
                                platform = jsonObject.getJSONObject("data").getString("platform");
                                status = jsonObject.getJSONObject("data").getString("status");
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
                            } else {
                                sendLog(jsonObject.getString("msg"));
                                MyToast.error(jsonObject.getString("msg"));
                                getVerifyCode(mPlatform);
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
        HttpClient.getInstance().get(":18087/task/list_plan?pageNum=1&type=1&pageSize=10&keyLike=&completeFlag=0&cancelFlag=0", mPlatform.getHost())
                .headers("x-rn-access-token", accessToken)
                .headers("x-rn-user-id", userId)
                .headers("x-rn-platform", platform)
                .headers("Referer", "http://wx.fsdmff.cn:8081/workerIndex")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject array = JSONObject.parseObject(response.body());
                            System.out.println(JSON.toJSONString(mParams.getShopName()));
                            if (null != array && 2000 == array.getIntValue("code") && array.getJSONObject("data").getJSONArray("list").size() > 0) {
                                sendLog("检测到任务领取中...");
                                lqTask(String.valueOf(array.getJSONObject("data").getJSONArray("list").getJSONObject(0).getIntValue("id")));
                                shopName = array.getJSONObject("data").getJSONArray("list").getJSONObject(0).getString("shopName");

                                if(shopName != null && shopName.equals(mParams.getAccount())){

                                }
                            } else {
                                sendLog(array.getString("msg"));  //继续检测任务
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

    /**
     * 领取任务
     *
     * @param taskId 任务id
     */
    private void lqTask(String taskId) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, taskId);
        HttpClient.getInstance().post(":18087/task/accept_task", mPlatform.getHost())
                .upRequestBody(body)
                .headers("x-rn-access-token", accessToken)
                .headers("x-rn-user-id", userId)
                .headers("x-rn-platform", platform)
                .headers("Referer", "http://wx.fsdmff.cn:8081/workerIndex")
                .headers("Content-Type", "application/json;charset=UTF-8")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (2000 == jsonObject.getIntValue("code")) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW)+"店铺名:"+shopName);
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.fongshoudamai, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(jsonObject.getString("msg"));
                            }
                        } catch (Exception e) {
                            sendLog("领取任务异常");  //接单异常
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
