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
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 旺店宝
 */
public class WDBAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private String userId = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String buyerId = "";
    private Integer count;

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
            count = 0;
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
        HttpClient.getInstance().post("/api/Login/LoginByMobile", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("client_id", "BF7817FD2E8651B6FC4C102F607EA1CD")
                .params("client_secret", "AFB5D053C0D6EE9E9B2796333AB2EAC8")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if (jsonObject.getIntValue("errcode") == 0) {    //登录成功
                                updateParams(mPlatform);
                                token = jsonObject.getJSONObject("obj").getString("Token");
                                userId = jsonObject.getJSONObject("obj").getString("UserId");
                                getAccount();
                            } else {
                                MyToast.error(jsonObject.getString("msg"));
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("登录异常");  //接单异常
                        stop();
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount() {
        {
            long n = new Date().getTime();
            HttpClient.getInstance().post("/api/Member/GetBindPlatformAccountList", mPlatform.getHost())
                    .params("UserId", userId)
                    .params("Token", token)
                    .params("PlatId", 1)
                    .headers("Authorization", token)
                    .headers("Content-Type", "application/json")
                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                                JSONObject jsonObject = JSONObject.parseObject(response.body());
                                JSONArray array = jsonObject.getJSONArray("obj");

                                if (array.size() > 0) {    //获取买号成功
                                    JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                                    List<BuyerNum> list = new ArrayList<>();
                                    mParams.setBuyerNum(new BuyerNum(obj.getString("Id"), obj.getString("PlatAccount")));
                                    updateBuyerId();
                                    for (int i = 0, len = array.size(); i < len; i++) {
                                        obj = array.getJSONObject(i);
                                        list.add(new BuyerNum(obj.getString("Id"), obj.getString("PlatAccount")));
                                    }
                                    showBuyerNum(JSON.toJSONString(list));
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
    }


    /**
     * 开始任务
     */
    private void startTask(){
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/Task/NewsSystemSendTask", mPlatform.getHost())
                .params("UserId", userId)
                .params("Token", token)
                .params("TaskType", mParams.getType())
                .params("PlatIdList", 1+",")
                .params("MaxAdvancePayMoney", 5000)
                .params("VersionControl","1.0.4")
                .params("AccountIdList", buyerId+",")
                .headers("Content-Type", "application/json")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if(obj.getInteger("errcode") == 0 || obj.getInteger("errcode") == 3){
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.wangdianbao, 3000);
                                }
                                count++;
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            }else{
                                sendLog(obj.getString("msg"));
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
