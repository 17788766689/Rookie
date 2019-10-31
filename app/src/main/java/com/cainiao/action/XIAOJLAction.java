package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HttpClient;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 小精灵
 */
public class XIAOJLAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";
    private String prot = ":";

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
            if(prot.equals(":")){
                getConfig();
            }else{
                login();
            }


        }
    }

    /**
     * 登录
     */
    private void getConfig() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().get("/js/config.js?V=7.2", mPlatform.getHost())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            String reData = response.body().toString();
                            int index = reData.indexOf("http://132.232.124.233:");
                            int index2 = reData.indexOf("/api");
                            prot += reData.substring(index + 23, index2) + "/api";
                            if (null != prot) {
                                login();
                            } else {
                                stop();
                                sendLog("登录异常");  //接单异常
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
     * 登录
     */
    private void login() {
        Map map = new HashMap();
        map.put("userNameOrEmailAddress", mParams.getAccount());
        map.put("password", mParams.getPassword());
        String param = JSON.toJSONString(map);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, param);
        HttpClient.getInstance().post(prot + "/TokenAuth/Authenticate", mPlatform.getHost())
                .upRequestBody(
                        body
                )
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "http://132.232.124.233/login.html")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            cookie = jsonObject.getJSONObject("result").getString("accessToken");
                            if (null != cookie) {    //登录成功
                                sendLog("登录成功");
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
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
                        super.onError(response);
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        JSONObject obj = jsonObject.getJSONObject("error");
                        sendLog(obj.getString("用户名或密码无效"));  //接单异常
                        stop();
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().post(prot + "/services/app/Orders/SeckillResult", mPlatform.getHost())
                .headers("Authorization", "Bearer " + cookie)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            JSONObject result = obj.getJSONObject("result");
                            if (result.getBoolean("haveUntreatedOrder") || result.getBoolean("haveUntreatedOrderEvaluates")) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.xiaojingling, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                HttpClient.getInstance().post(prot + "/services/app/Orders/Seckill", mPlatform.getHost())
                                        .headers("Authorization", "Bearer " + cookie)
                                        .headers("Content-Type", "application/json")
                                        .headers("X-Requested-With", "XMLHttpRequest")
                                        .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                try {
                                                   sendLog("暂时没有任务");
                                                } catch (Exception e) {
                                                    sendLog("检测任务异常");  //接单异常
                                                }
                                            }

                                            @Override
                                            public void onError(Response<String> response) {
                                                super.onError(response);
                                                sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                                            }
                                        });
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
