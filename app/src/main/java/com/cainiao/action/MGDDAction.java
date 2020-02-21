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
import com.lzy.okgo.request.base.Request;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 芒果叮咚
 */
public class MGDDAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

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
        long n = new Date().getTime();
        sendLog(MyApp.getContext().getString(R.string.being_login));
        Request request = HttpClient.getInstance().post("/api/index/login", mPlatform.getHost());
        request.params("mobile", mParams.getAccount())
                .params("password", Utils.md5(mParams.getPassword()))
                .params("device_version", "")
                .params("time",n)
                .params("sign", Utils.md5("youqianyiqizhuanyoumengyiqizuo" + Utils.md5("device_version=&mobile="+mParams.getAccount()+"&password="+Utils.md5(mParams.getPassword())) + n));
        request.headers("user-agent","15(Android/7.1.1) (io.dcloud.UNIE7AC320/1.0.1) Weex/0.26.0 1080x1920");
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    if (TextUtils.isEmpty(response.body())) return;
                    JSONObject jsonObject = JSONObject.parseObject(response.body());
                    sendLog(jsonObject.getString("message"));
                    if (jsonObject.getIntValue("code") == 1) {    //登录成功
                        updateParams(mPlatform);
                        token = jsonObject.getJSONObject("data").getJSONObject("token").getString("token");
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
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/index/get_taobao_info", mPlatform.getHost())
                .params("type", "1")
                .params("page", "1")
                .params("sign", Utils.md5("renqiwangjiamifangzhiwaigua" + Utils.md5("page=1&type=1") + n))
                .params("time", n)
                .headers("Authorization", token)
                .headers("Content-Type", "application/json")
                .headers("user-agent","15(Android/7.1.1) (io.dcloud.UNIE7AC320/1.0.1) Weex/0.26.0 1080x1920")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {

                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());

                            JSONArray array = jsonObject.getJSONObject("data").getJSONArray("info");
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                                mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("name")));
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("name")));
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
        if (isStart == false)return;
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/assign/get_all_task", mPlatform.getHost())
                .headers("Authorization", token)
                .params("page", "1")
                .params("type", mParams.getType())
                .params("sign", Utils.md5("renqiwangjiamifangzhiwaigua" + Utils.md5("page=1&type=" + mParams.getType()) + n))
                .params("time", n)
                .headers("Content-Type", "application/json")
                .headers("Authorization", token)
                .headers("user-agent","15(Android/7.1.1) (io.dcloud.UNIE7AC320/1.0.1) Weex/0.26.0 1080x1920")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            if (JSONObject.parseObject(response.body()).getString("message") != null && !JSONObject.parseObject(response.body()).getString("message").equals("")){
                                sendLog(JSONObject.parseObject(response.body()).getString("message"));
                                return;
                            }
                            JSONArray array = JSONObject.parseObject(response.body()).getJSONObject("data").getJSONObject("list").getJSONArray("data");
                            for (int i = 0, len = array.size(); i < len; i++) {
                                JSONObject object = array.getJSONObject(i);
                                if (object.getIntValue("publish_status") == 0
                                        && Float.parseFloat(object.getString("brokerage")) >= mParams.getMinCommission()    //佣金金额大于最小佣金
                                        && Float.parseFloat(object.getString("return_money")) <= mParams.getMaxPrincipal()) {    //本金金额小于最大本金
                                    sendLog(String.format(MyApp.getContext().getString(R.string.receipt_get_task), object.getString("return_money"), object.getString("brokerage")));
                                    lqTask(object.getString("id"));
                                    break;
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
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/assign/accept_work", mPlatform.getHost())
                .headers("Authorization", token)
                .params("id", taskId)
                .params("sign", Utils.md5("renqiwangjiamifangzhiwaigua" + Utils.md5("id=" + taskId) + n))
                .params("time", n)
                .headers("Content-Type", "application/json")
                .headers("Authorization", token)
                .headers("user-agent","15(Android/7.1.1) (io.dcloud.UNIE7AC320/1.0.1) Weex/0.26.0 1080x1920")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 1) {    //接单成功
                                getTask();
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
     * 检查任务
     */
    private void getTask() {
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/assign/my_works", mPlatform.getHost())
                .params("status", "1")
                .params("page", "1")
                .params("sign", Utils.md5("renqiwangjiamifangzhiwaigua" + Utils.md5("page=1&status=1") + n))
                .params("time", n)
                .headers("Content-Type", "application/json")
                .headers("Authorization", token)
                .headers("user-agent","15(Android/7.1.1) (io.dcloud.UNIE7AC320/1.0.1) Weex/0.26.0 1080x1920")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());

                            JSONArray array = JSONObject.parseObject(response.body()).getJSONObject("data").getJSONObject("list").getJSONArray("data");
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0);
                                sendLog("接单成功,店铺名:"+obj.getString("shop_name"));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName())+"，店铺名:"+obj.getString("shop_name"), R.raw.manguodingdon, 3000);
                                }
                                count++;
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else { //无可用的买号
                                sendLog("空任务，过滤");
                            }
                        } catch (Exception e) {
                            stop();
                            MyToast.error("获取买号异常");
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
