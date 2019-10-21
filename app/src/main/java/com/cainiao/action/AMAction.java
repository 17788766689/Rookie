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
 * 爱米
 */
public class AMAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;

    @Override
    public void start(Platform platform) {
        if(platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();


//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.RECEIPTING);

        if(!isStart){    //未开始抢单
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
    private void login(){
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/buyer/user/login", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("type",0)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if(jsonObject.getIntValue("code") == 200){    //登录成功
                            sendLog("登录成功！");
                            token = jsonObject.getJSONObject("data").getString("token");
                            updateParams(mPlatform);
                            getAccount();
                        }else{
                            MyToast.error(jsonObject.getString("message"));
                            stop();
                        }
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount(){
        HttpClient.getInstance().get("/v2/account/list", mPlatform.getHost())
                .headers("X-Token",token)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        JSONArray tbData = jsonObject.getJSONObject("data").getJSONArray("list");
                        if(tbData.size() > 0){    //获取买号成功
                            JSONObject obj = tbData.getJSONObject(0);
                            mParams.setBuyerNum(new BuyerNum(obj.getString("id"),obj.getString("account")));
                            List<BuyerNum> list = new ArrayList<>();
                            for(int i = 0, len = tbData.size(); i < len; i++){
                                obj = tbData.getJSONObject(i);
                                list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                            }
                            showBuyerNum(JSON.toJSONString(list));
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                            MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                            updateStatus(mPlatform, 3); //正在接单的状态
                            startTask();
                        }else { //无可用的买号
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask(){
        HttpClient.getInstance().get("/buyer/order/check-set?client_mac=+", mPlatform.getHost())
                .headers("X-Token", token)
                .headers("Referer","https://www.huimi123.com/robwelfare")
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        HttpClient.getInstance().get("/buyer/order/list-current?page=1&size=20", mPlatform.getHost())
                                .headers("X-Token", token)
                                .headers("Referer","https://www.huimi123.com/robwelfare")
                                .headers("User-Agent","User-Agent: Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                                .execute(new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        if(TextUtils.isEmpty(response.body())) return;
                                        JSONObject array = JSONObject.parseObject(response.body());
                                        if (!(response.body().contains("[]")) && response.body().contains("任务")){
                                            sendLog("检测到任务领取中...");
                                            JSONArray taskArray = array.getJSONObject("data").getJSONArray("list");
                                            lqTask(String.valueOf(taskArray.getJSONObject(0).getString("id")));
                                            if (taskArray.size() > 1){
                                                lqTask(String.valueOf(taskArray.getJSONObject(1).getString("id")));
                                            }
                                        }else{
                                            sendLog("继续检测任务");  //继续检测任务
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if(isStart){
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
     * @param taskId  任务id
     */
    private void lqTask(String taskId){
        HttpClient.getInstance().get("/v2/task/obtain?order_id="+taskId, mPlatform.getHost())
                .headers("X-Token", token)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if(!("任务已被抢：101".equals(jsonObject.getString("message")))){    //接单成功
                            sendLog(MyApp.getContext().getString(R.string.receipt_success));
                            receiveSuccess(String.format(MyApp.getContext().getString(R.string.receipt_success_tips), mPlatform.getName()), R.raw._918renqiwang, 3000);
                            addTask(mPlatform.getName());
                            updateStatus(mPlatform, Const.RECEIPT_SUCCESS); //接单成功的状态
                            isStart = false;
                        }else{
                            sendLog(jsonObject.getString("message"));
                        }
                    }
                });
    }



    @Override
    public void stop() {
        if(!isStart) return;   //如果当前状态是未开始，则不做任何操作
        super.stop();
        isStart = false;
        //主动点击停止抢单，则还原初始状态。  注意：抢单成功之后不要直接调用stop方法，
        // 否则状态会变成初始状态而不是“抢单成功”的状态。抢单成功直接把isStart设为false即可
        updateStatus(mPlatform, Const.RESET);
    }
}
