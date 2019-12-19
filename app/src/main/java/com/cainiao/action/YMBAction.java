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
 * 云美贝
 */
public class YMBAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String buyerId = "";
    private  String type = "2";

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
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/app.ashx?action=login_submit", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("isAdmin", 0)
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://m.yunmeibei.cn/login.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msgbox"));
                            if (jsonObject.getIntValue("msg") == 1) {    //登录成功
                                token = response.headers().get("Set-Cookie").toString();
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                MyToast.error(jsonObject.getString("msgbox"));
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
        HttpClient.getInstance().get("/app.ashx?action=GetListByNoTask&currPage=1&pageSize=10&type=1&taskType=1&accountId=0&sort=1&_="+new Date().getTime(), mPlatform.getHost())
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://m.yunmeibei.cn/index.html")
                .headers("Cookie",token)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray tbData = jsonObject.getJSONArray("dataCount2");
                            if (tbData.size() > 0) {    //获取买号成功
                                JSONObject obj = tbData.getJSONObject(0);
                                mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("accountname")));
                                updateBuyerId();
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    obj = tbData.getJSONObject(i);
                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("accountname")));
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
        if(mParams.getType().equals("2")){
            type = "1";
        }
        HttpClient.getInstance().get("/app.ashx?action=GetListByNoTask&currPage=1&pageSize=10&type="+type+"&taskType=1&accountId=0&sort=1&_="+new Date().getTime(), mPlatform.getHost())
                .headers("Cookie", token)
                .headers("Referer", "http://m.yunmeibei.cn/index.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response){
                        JSONObject obj = JSONObject.parseObject(response.body());
                        JSONArray array = obj.getJSONArray("dataList");
                        if(array.size() > 0){
                            for (int i = 0, len = array.size(); i < len; i++) {
                                JSONObject object = array.getJSONObject(i);
                                if(object.getDouble("commission_price") >= mParams.getMinCommission() && object.getDouble("product_price") <= mParams.getMaxPrincipal()){
                                    sendLog("检测到任务领取中");
                                    lqTask(object.getString("id"));
                                }
                            }
                        }else{
                            sendLog("继续检测任务");
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
        HttpClient.getInstance().get("/app.ashx?action=updateOrderStatus", mPlatform.getHost())
                .params("id",taskId)
                .params("channelId","3")
                .params("status","1")
                .params("strValue",","+buyerId+",2")
                .headers("Cookie", token)
                .headers("Referer", "http://m.yunmeibei.cn/index.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("IsSuccess") == 1) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.yunmeibei, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            } else {
                                sendLog(jsonObject.getString("message"));
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
