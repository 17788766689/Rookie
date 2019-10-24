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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 人人乐淘
 */
public class RRLTAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;

    @Override
    public void start(Platform platform) throws Exception {
        if (platform == null) return;
//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);
        if (!isStart) {    //未开始抢单
            mPlatform = platform;
            mParams = platform.getParams();
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
    private void login() throws Exception {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        String n = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String key2 = Utils.HMACSHA256(Utils.HMACSHA256(Utils.md5(mParams.getPassword()).toLowerCase(), mParams.getAccount()), n);
        HttpClient.getInstance().post("/api/buyer/login", mPlatform.getHost())
                .params("phone", mParams.getAccount())
                .params("password", key2)
                .params("verLevel", "2")
                .params("salt", n)
                .params("devMac", Utils.randomString(14))
                .params("rememberMe", "true")
                .headers("Accept", "application/json, text/javascript, */*; q=0.01")
                .headers("X-Requested-With", "XMLHttpRequest")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("rspCode") == 0) {    //登录成功
                                cookie = response.headers().get("Set-Cookie").toString();
                                sendLog("登录成功！");
                                updateParams(mPlatform);
                                JSONArray tbData = jsonObject.getJSONObject("buyer").getJSONArray("taoBaoInfos");
                                if (tbData.size() > 0) {    //获取买号成功
                                    JSONObject obj = tbData.getJSONObject(0); ////默认使用第一个买号
                                    mParams.setBuyerNum(new BuyerNum(obj.getString("tbNo"), obj.getString("tbNo")));
                                    List<BuyerNum> list = new ArrayList<>();
                                    for (int i = 0, len = tbData.size(); i < len; i++) {
                                        obj = tbData.getJSONObject(i);
                                        list.add(new BuyerNum(obj.getString("tbNo"), obj.getString("tbNo")));
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
                            } else {
                                sendLog(jsonObject.getString("rspDesc"));
                                MyToast.error(jsonObject.getString("rspDesc"));
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
        HttpClient.getInstance().post("/api/buyer/queryTaskOrderList", mPlatform.getHost())
                .params("page", "1")
                .params("rows", "10")
                .params("taskType", "PT")
                .params("tbNo", mParams.getBuyerNum().getName())
                .headers("Cookie", cookie)
                .headers("Accept", "application/json, text/javascript, */*; q=0.01")
                .headers("X-Requested-With", "XMLHttpRequest")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONArray array = JSONObject.parseObject(response.body()).getJSONArray("rows");
                            for (int i = 0, len = array.size(); i < len; i++) {
                                JSONObject object = array.getJSONObject(i);
                                if (object.getBoolean("clickable")
                                        && Float.parseFloat(object.getString("commission")) >= mParams.getMinCommission()    //佣金金额大于最小佣金
                                        && Float.parseFloat(object.getString("payment")) <= mParams.getMaxPrincipal()) {    //本金金额小于最大本金
                                    sendLog(String.format(MyApp.getContext().getString(R.string.receipt_get_task), object.getString("payment"), object.getString("commission")));
                                    lqTask(object.getString("id"));
                                }
                            }
                            sendLog(MyApp.getContext().getString(R.string.receipt_continue_task));  //继续检测任务
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
        HttpClient.getInstance().post("/api/buyer/acceptTaskOrder", mPlatform.getHost())
                .params("taskOrderId", taskId)
                .params("tbNo", mParams.getBuyerNum().getName())
                .params("operationIp", "10.0.0.1")
                .params("version", "1.0.0")
                .headers("Cookie", cookie)
                .headers("Accept", "application/json, text/javascript, */*; q=0.01")
                .headers("X-Requested-With", "XMLHttpRequest")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("您当前有接单未做的任务，请先完成".equals(jsonObject.getString("rspDesc")) || "操作成功".equals(jsonObject.getString("rspDesc"))) {   //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.renrenletao, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else if (jsonObject.getString("rspDesc").equals("每天最多只能接3单")) {
                                sendLog(jsonObject.getString("rspDesc").toString());
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.renrenletao, 3000);
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(jsonObject.getString("rspDesc"));
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
