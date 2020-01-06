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
 * 口袋精灵
 */
public class KDJLAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String userId;
    private String token;
    private String rToken;
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
        HttpClient.getInstance().post("/user/login", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("password", Utils.md5(Utils.md5(mParams.getPassword())))
                .params("grant_type", "password")
                .params("softversion", "7.0.0")
                .params("client_id", "cepingfu")
                .params("imeis", Utils.randomString(16))
                .params("devicenames", "MI+9")
                .params("deviceplatforms", "Android")
                .params("deviceversions", "8.0.0")
                .params("registrationid", "")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("success".equals(jsonObject.getString("status"))) {    //登录成功
                                sendLog("登录成功");
                                userId = jsonObject.getJSONObject("data").getJSONObject("info").getString("userId");
                                token = jsonObject.getJSONObject("data").getJSONObject("info").getString("access_token");
                                rToken = jsonObject.getJSONObject("data").getJSONObject("info").getString("refresh_token");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
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
     * 获取买号
     */
    private void getAccount() {
        sendLog("正在获取买号....");
        long n = new Date().getTime();
        HttpClient.getInstance().post("/orderreceive/viewaccountdetail", mPlatform.getHost())
                .params("accountid", "123456")
                .params("platid", "1")
                .params("bid", userId)
                .params("accessToken", token)
                .params("softversion", "7.0.0")
                .params("refreshToken", rToken)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONObject("data").getJSONArray("accountmsg");
                            JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                            List<BuyerNum> list = new ArrayList<>();
                            int count = 0;
                            for (int i = 0, len = array.size(); i < len; i++) {
                                obj = array.getJSONObject(i);
                                if (obj.getInteger("blacklist") == 1 && mParams.isFilterCheck()) {
                                    sendLog(obj.getString("account") + ",此号只能接浏览单,已过滤");
                                } else {
                                    if (count == 0) {
                                        count++;
                                        isStart = true;
                                        mParams.setBuyerNum(new BuyerNum(obj.getString("accountid"), obj.getString("account")));
                                        updateBuyerId();
                                    }
                                    list.add(new BuyerNum(obj.getString("accountid"), obj.getString("account")));
                                }

                            }
                            if (count > 0) {    //获取买号成功
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                Thread.sleep(3000);
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
     * 保存配置
     */
    private void savesendacc() {
        long n = new Date().getTime();
        HttpClient.getInstance().post("/orderreceive/savebuyeraccount", mPlatform.getHost())
                .params("accountid", buyerId)
                .params("type", "1")
                .params("task_attribute", "0")
                .params("is_pc", "0")
                .params("plattaskid", "1")
                .params("tasktype", "0")
                .params("payamount", "0,1550")
                .params("paymode", "1")
                .params("mll", "不限制")
                .params("all_left", "0")
                .params("all_right", "以上")
                .params("low", "-10px")
                .params("high", "265px")
                .params("bar_left", "0px")
                .params("bar_right", "275px")
                .params("mincomm", "4.00")
                .params("maxcomm", "以上")
                .params("accessToken", token)
                .params("softversion", "7.0.0")
                .params("refreshToken", rToken)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 200) {
                                sendLog("配置成功,开始接单！");
                                sendLog("可通过卡佣金不接浏览单,设置最小佣金为0时可接浏览单");
                            } else {
                                sendLog(jsonObject.getString("message"));
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("配置异常！");
                            stop();
                        }
                    }
                });
    }


    /**
     * 开始任务
     */
    private void startTask() {
        long n = new Date().getTime();
        HttpClient.getInstance().post("/orderreceive/receiveOrder", mPlatform.getHost())
                .params("platid", "1")
                .params("bid", userId)
                .params("buyeraccount[0][platid]", "1")
                .params("buyeraccount[0][accountid]", buyerId)
                .params("first", "1")
                .params("accessToken", token)
                .params("softversion", "7.0.0")
                .params("refreshToken", rToken)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 200) {    //接单成功
                                sendLog(jsonObject.getJSONObject("data").getJSONObject(userId).getString("buyercommission"));
                                if (null == jsonObject.getJSONObject("data").getJSONObject(userId).getString("buyercommission") || Double.parseDouble(jsonObject.getJSONObject("data").getJSONObject(userId).getString("buyercommission").replace("金", "")) <= mParams.getMinCommission()) {
                                    sendLog("任务不符合要求,将自动取消");
                                    qxTask(jsonObject.getJSONObject("data").getJSONObject(userId).getString("orderid"));
                                } else {
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zuanyoumi, 3000);
                                    addTask(mPlatform.getName());
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                }
                            } else if ("当前有正在进行中的任务".equals(jsonObject.getString("message"))) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zuanyoumi, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(jsonObject.getString("message"));
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常");
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
     * 取消任务
     */
    private void qxTask(String taskId) {
        long n = new Date().getTime();
        HttpClient.getInstance().post("/order/cancleOrder", mPlatform.getHost())
                .params("orderid", taskId)
                .params("cancelReason", 3)
                .params("accessToken", token)
                .params("softversion", "7.0.0")
                .params("refreshToken", rToken)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));
                        } catch (Exception e) {
                            sendLog("撤销任务异常！");
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
        long n = new Date().getTime();
        HttpClient.getInstance().post("/orderreceive/cancel_receiveOrder", "http://app.koudaijl.com")
                .params("bid", userId)
                .params("accessToken", token)
                .params("softversion", "7.0.0")
                .params("refreshToken", rToken)
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 9; MI 9 Build/PKQ1.181121.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")
                .headers("X-Request-With", "com.qazapp.buyer")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));
                        } catch (Exception e) {
                            sendLog("撤销任务异常！");
                        }
                    }
                });
    }

    /**
     * 更新买号
     */
    private void updateBuyerId() {
        if (mParams.getBuyerNum() != null && !TextUtils.isEmpty(mParams.getBuyerNum().getId())) {
            buyerId = mParams.getBuyerNum().getId();
            if (isStart) {
                savesendacc();
            }
        }
    }



}
