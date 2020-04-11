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
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 小白象
 */
public class XBXAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String buyerId = "";

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
        updateBuyerId();

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
     * 获取验证码
     */
    public void getVerifyCode(Platform platform) {
        sendMsg("get_verifycode", "http://49.234.145.140/auth/getCaptcha?t=0."+new Date().getTime());
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/auth/userLogin", mPlatform.getHost())
                .params("account", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("imageCode",mParams.getVerifyCode())
                .params("deviceId","")
                .headers("Cookie",mPlatform.getVerifyCodeCookie())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("success".equals(jsonObject.getString("msg"))) {    //登录成功
                                sendLog("登录成功！");
                                List<String> cookies = response.headers().values("Set-Cookie");
                                for (String str : cookies) {
                                    cookie += str.substring(0, str.indexOf(";")) + ";";
                                }
                                token = jsonObject.getString("data");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                sendLog(jsonObject.getString("msg"));
                                MyToast.error(jsonObject.getString("msg"));
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
     * 获取买号
     */
    private void getAccount() {
        HttpClient.getInstance().post("/user/userBindAccountList", mPlatform.getHost())
                .params("type",1)
                .headers("Cookie",mPlatform.getVerifyCodeCookie())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray tbData = jsonObject.getJSONArray("data");
                            if (tbData.size() > 0) {    //获取买号成功
                                JSONObject obj = tbData.getJSONObject(0);
                                mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                updateBuyerId();
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    obj = tbData.getJSONObject(i);
                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                }
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
                            } else { //无可用的买号
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                                stop();
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
        HttpClient.getInstance().post("/user/getTaskOrderList", mPlatform.getHost())
                .params("userAccountId",buyerId)
                .params("sign","xx1")
                .headers("Referer","http://49.234.145.140/user/goGameLobby")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Cookie",mPlatform.getVerifyCodeCookie())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject array = JSONObject.parseObject(response.body());
                            JSONArray taskArray = array.getJSONArray("data");
                            if (taskArray.size() > 0) {
                                sendLog("检测到任务领取中...");
                                for (int i=0;i<taskArray.size();i++){
                                    lqTask(String.valueOf(taskArray.getJSONObject(i).getString("taskId")));
                                }
                            } else {
                                sendLog("继续检测任务");  //继续检测任务
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

    /**
     * 领取任务
     *
     * @param taskId 任务id
     */
    private void lqTask(String taskId) {
        HttpClient.getInstance().post("/user/addGradTaskOrder", mPlatform.getHost())
                .params("accountId",buyerId)
                .params("taskId",taskId)
                .params("sign","xx1")
                .headers("Cookie",mPlatform.getVerifyCodeCookie())
                .headers("Referer","http://49.234.145.140/user/goGameLobby")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 1) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.xiaobaixiang, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            } else {
                                sendLog(jsonObject.getString("msg"));
                            }
                        } catch (Exception e) {
                            sendLog("领取任务异常！");
                        }
                    }
                });
    }

    /**
     * 更新买号
     */
    private void updateBuyerId(){
        if(mParams.getBuyerNum() != null && !TextUtils.isEmpty(mParams.getBuyerNum().getId())){
            buyerId = mParams.getBuyerNum().getId();
        }
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
