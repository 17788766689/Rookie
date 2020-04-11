package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

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
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 铁蚂蚁
 */
public class TMYPDAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private String userId = "";
    private Platform mPlatform;
    private String buyerId = "";
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    int index = 0;
    private String appid = "A604895728415127";
    private String platId = "";
    private String accountStr = "";

    @Override
    public void start(Platform platform) {
        if (platform == null || platform.getParams() == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        updateBuyerId();

        if (!isStart) {    //未开始抢单

            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            login();
        }
    }

    private String getSign(Long time){
        String clientId = "6DCD1E064A77590CF7478914C6900B3D";
        String clientSecrect = "7AC7AF2C3598C13744949CB0625C3D3A";
        String apiKey = "DtFr5#c1_36El@1a1_u7tFRtt_r@RgK9_hYfe@3S2";
        return Utils.md5(appid+clientId+clientSecrect+apiKey+time);
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        long n = new Date().getTime();
        HttpClient.getInstance().post("api/Login/LoginByMobile", mPlatform.getHost())
                .params("Mobile", mParams.getAccount())
                .params("PassWord", mParams.getPassword())
                .params("client_id", "6DCD1E064A77590CF7478914C6900B3D")
                .params("client_secret", "7AC7AF2C3598C13744949CB0625C3D3A")
                .headers("Timetamp",n+"")
                .headers("Sign",getSign(n))
                .headers("AppId",appid)
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
                                startTask();
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
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/Member/GetBindPlatformAccountList", mPlatform.getHost())
                .params("UserId", userId)
                .params("Token", token)
                .params("PlatId", platId)
                .headers("Authorization", token)
                .headers("Content-Type", "application/json")
                .headers("Timetamp",n+"")
                .headers("Sign",getSign(n))
                .headers("AppId",appid)
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
                                mParams.setBuyerNum(new BuyerNum("-1","自动选择" ));
                                updateBuyerId();
                                List<BuyerNum> list = new ArrayList<>();
                                list.add(new BuyerNum("-1", "自动选择"));
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    list.add(new BuyerNum(obj.getString("Id"), obj.getString("PlatAccount")));
                                    accountStr += obj.getString("Id")+",";
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
        //if (isStart == false)return;
        String AccountIdList = "";
        if (!mParams.getType().equals("1") && !(platId.equals("5"))){
            platId = "5";
            getAccount();
            return;
        }else if(mParams.getType().equals("1") && platId.equals("5")){
            platId = "1";
            getAccount();
            return;
        }else if(mParams.getType().equals("1") && platId.equals("")){
            platId = "1";
            getAccount();
            return;
        }
        String PlatIdList = "1";
        if (mParams.getType().equals("2")){
            PlatIdList = "5";
        }

        if (buyerId.equals("-1")){
            AccountIdList = accountStr;
        }else{
            AccountIdList = buyerId+",";
        }
        System.out.println(accountStr);
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/Task/NewsSystemSendTask", mPlatform.getHost())
                .params("UserId", userId)
                .params("Token", token)
                .params("TaskType", 1)
                .params("PlatIdList", PlatIdList+",")
                .params("MaxAdvancePayMoney", 5000)
                .params("AccountIdList", AccountIdList)
                .params("AppVersion","0.0.20")
                .headers("Content-Type", "application/json")
                .headers("Timetamp",n+"")
                .headers("Sign",getSign(n))
                .headers("AppId",appid)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if(obj.getJSONObject("obj") != null && obj.getInteger("errcode") != 0){
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.tiemayi, 3000);
                                }
                                count++;
                                addTask("铁蚂蚁");
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            }else if(obj.getInteger("errcode") == 3){
                                HttpClient.getInstance().post("/api/Task/QueryTaskReceivingStatus", mPlatform.getHost())
                                        .params("UserId", userId)
                                        .params("Token", token)
                                        .headers("Authorization", token)
                                        .headers("Content-Type", "application/json")
                                        .headers("Timetamp",n+"")
                                        .headers("Sign",getSign(n))
                                        .headers("AppId",appid)
                                        .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                try {
                                                    if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                                                    JSONObject jsonObject = JSONObject.parseObject(response.body());
                                                   if (jsonObject.getString("msg").equals("接单成功")){
                                                       sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                                       if (count == 0) {
                                                           receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.tiemayi, 3000);
                                                       }
                                                       count++;
                                                       addTask("铁蚂蚁");
                                                       updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                                       isStart = false;
                                                   }

                                                } catch (Exception e) {
                                                    sendLog("检测任务异常！");
                                                    stop();
                                                }
                                            }
                                        });
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
