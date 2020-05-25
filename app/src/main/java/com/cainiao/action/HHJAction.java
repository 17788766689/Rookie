package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HttpClient;
import com.cainiao.view.toasty.MyToast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.Random;

/**
 * 花花街
 */
public class HHJAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";
    private String userId = "";

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
        HttpClient.getInstance().post("/index.php/User/Public/login/", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("password", mParams.getPassword())
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if (1 == jsonObject.getInteger("status")) {    //登录成功
                                cookie = response.headers().get("Set-Cookie").toString();
                                userId = jsonObject.getString("userid");
                                HttpClient.getInstance().post("/index.php/User/Task/getorderlist/", mPlatform.getHost())
                                        .params("username", mParams.getAccount())
                                        .params("userid", userId)
                                        .params("status",2)
                                        .params("pt_id",1)
                                        .headers("Cookie", cookie)
                                        .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                try {
                                                    if (TextUtils.isEmpty(response.body())) return;
                                                    JSONObject jsonObject = JSONObject.parseObject(response.body());
                                                    if (1 == jsonObject.getInteger("status")) {
                                                        sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.guli, 3000);
                                                        addTask(mPlatform.getName());
                                                        updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                                        isStart = false;
                                                    }else{
                                                        updateParams(mPlatform);
                                                        MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                                        updateStatus(mPlatform, 3); //正在接单的状态
                                                        startTask();
                                                    }
                                                } catch (Exception e) {
                                                    stop();
                                                    sendLog("登录异常");  //接单异常
                                                }
                                            }
                                        });
                            } else {
                                MyToast.error("账号或者密码错误");
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
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().post("/index.php/user/Task/ceshi/", mPlatform.getHost())
                .params("israndom",1)
                .params("platform",2)
                .params("platform_id",1)
                .params("type",1)
                .params("username",mParams.getAccount())
                .params("userid",userId)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getIntValue("status")) {
                                sendLog("检测到任务领取中");
                                JSONArray taskId = obj.getJSONArray("data");
                                lqTask(obj.getJSONObject("acount").getString("accname"),taskId.getJSONArray(0).getJSONObject(0).getString("id"));
                            } else {
                                sendLog("继续检测任务");
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

    public void lqTask(String accname,String taskId){
        HttpClient.getInstance().post("/index.php/User/Task/receipt", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("userid", userId)
                .params("id",taskId)
                .params("accname",accname)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if (1 == jsonObject.getInteger("status")) {    //登录成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.guli, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            }
                        } catch (Exception e) {
                            stop();
                            sendLog("登录异常");  //接单异常
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
