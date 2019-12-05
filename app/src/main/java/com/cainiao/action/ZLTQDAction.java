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
import com.lzy.okgo.request.base.Request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 赚辣条(抢单)
 */
public class ZLTQDAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private String type;
    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            cookie = "";
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
        Request request = HttpClient.getInstance().post("/api/user/login", mPlatform.getHost());
        request.params("mobile", mParams.getAccount())
               .params("password", mParams.getPassword())
               .params("openid", "");
        request.headers("User-Agent","Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 bsl/1.0")
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "XMLHttpRequest");
        request.execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getBoolean("success")) {    //登录成功
                                sendLog("登录成功");
                                updateParams(mPlatform);
                                List<String> list = response.headers().values("Set-Cookie");
                                for (String str : list) {
                                    if(!str.substring(0, str.indexOf(";")).equals("session=")){
                                        cookie += str.substring(0, str.indexOf(";")) + ";";
                                    }
                                }
                                getAccount();
                            } else {
                                sendLog(jsonObject.getString("message"));
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
        type = mParams.getType();
        isStart = true;
        long n = new Date().getTime();
        HttpClient.getInstance().post("/api/user/buy/get-my-account", mPlatform.getHost())
                .params("cat_id", mParams.getType())
                .headers("Cookie",cookie)
                .headers("Content-Type", "application/json")
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 bsl/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());

                            JSONArray array = jsonObject.getJSONArray("data");
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
        long n = new Date().getTime();
        HttpClient.getInstance().get("/hall/list", mPlatform.getHost())
                .params("shop_type", mParams.getType())
                .params("type_id", "1")
                .params("page_index", "1")
                .headers("Cookie",cookie)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://www.zhuanlatiao.com/hall/list?shop_type="+mParams.getType()+"&type_id=1")
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 bsl/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONArray array = JSONObject.parseObject(response.body()).getJSONArray("data");
                            for (int i = 0, len = array.size(); i < len; i++) {
                                JSONObject object = array.getJSONObject(i);
                                if (object.getDouble("commission") >= mParams.getMinCommission()    //佣金金额大于最小佣金
                                        && object.getDouble("amount") <= mParams.getMaxPrincipal()) {    //本金金额小于最大本金
                                    sendLog(String.format(MyApp.getContext().getString(R.string.receipt_get_task), object.getString("amount"), object.getString("commission")));
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
                        sendLog("接单异常,接单过程中请勿在其他地方登录");  //接单异常
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
                                    if(!type.equals(mParams.getType())){
                                        isStart = false;
                                        getAccount();
                                    }else{
                                        startTask();
                                    }
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
        HttpClient.getInstance().post("/api/user/task/receive", mPlatform.getHost())
                .params("id", taskId)
                .params("account_id", mParams.getBuyerNum().getId())
                .headers("Content-Type", "application/json")
                .headers("Cookie",cookie)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 bsl/1.0")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 0) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zuanlatiao, 3000);
                                }
                                count++;
                                addTask("赚辣条");
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(jsonObject.getString("message"));
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
