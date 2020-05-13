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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 闹闹
 */
public class NNAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;

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
            login();
        }
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("public/index.php/apis/login", mPlatform.getHost())
                .params("user", mParams.getAccount())
                .params("pass", mParams.getPassword())
                .headers("X-Reuqested-With","io.ionic.wanwanshuadanptai")
                .headers("Referer","http://localhost/login")
                .headers("User-Agent","Mozilla/5.0 (Linux; U; Android 5.0; zh-cn; vivo X5Pro D Build/LRX21M) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.100 U3/0.8.0 Mobile Safari/534.30 AliApp(TB/6.5.2) WindVane/8.0.0 1080X1920 GCanvas/1.4.2.21")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if (jsonObject.getInteger("errno") == 0) {    //登录成功
                                cookie = jsonObject.getJSONObject("data").getString("uid");
                                updateParams(mPlatform);
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
                });
    }

    /**
     * 获取买号
     */
    private void getAccount() {
        HttpClient.getInstance().post("/public/index.php/apis/total_wang", mPlatform.getHost())
                .params("uid",cookie)
                .headers("X-Reuqested-With","io.ionic.wanwanshuadanptai")
                .headers("Referer","http://localhost/jiedan")
                .headers("User-Agent","Mozilla/5.0 (Linux; U; Android 5.0; zh-cn; vivo X5Pro D Build/LRX21M) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.100 U3/0.8.0 Mobile Safari/534.30 AliApp(TB/6.5.2) WindVane/8.0.0 1080X1920 GCanvas/1.4.2.21")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());

                            JSONArray array = jsonObject.getJSONArray("data");
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                                mParams.setBuyerNum(new BuyerNum(obj.getString("wangwang"), obj.getString("wangwang")));
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    list.add(new BuyerNum(obj.getString("wangwang"), obj.getString("wangwang")));
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
        HttpClient.getInstance().post("/public/index.php/apis/pai_dan", mPlatform.getHost())
                .params("uid",cookie)
                .params("wang", mParams.getBuyerNum().getId())
                .params("version","1.0.3")
                .params("addr","东城区锡拉胡同")
                .params("addrs","北京市")
                .headers("X-Reuqested-With","io.ionic.wanwanshuadanptai")
                .headers("Referer","http://localhost/jiedan")
                .headers("User-Agent","Mozilla/5.0 (Linux; U; Android 5.0; zh-cn; vivo X5Pro D Build/LRX21M) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.100 U3/0.8.0 Mobile Safari/534.30 AliApp(TB/6.5.2) WindVane/8.0.0 1080X1920 GCanvas/1.4.2.21")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject array = JSONObject.parseObject(response.body());
                            if (array.getInteger("errno") == 3) {
                                sendLog(array.getString("msg"));
                                sendLog("接单成功");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.naonao, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else if(array.getInteger("errno") == 0){
                                lqTask(array.getJSONObject("data").getString("task_num"));
                            }else {
                                sendLog(array.getString("msg"));  //继续检测任务
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

    public void lqTask(String task_num){
            HttpClient.getInstance().post("/public/index.php/apis/jie_dan", mPlatform.getHost())
                    .params("uid",cookie)
                    .params("wang", mParams.getBuyerNum().getId())
                    .params("task_num",task_num)
                    .headers("X-Reuqested-With","io.ionic.wanwanshuadanptai")
                    .headers("Referer","http://localhost/jiedan")
                    .headers("User-Agent","Mozilla/5.0 (Linux; U; Android 5.0; zh-cn; vivo X5Pro D Build/LRX21M) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.100 U3/0.8.0 Mobile Safari/534.30 AliApp(TB/6.5.2) WindVane/8.0.0 1080X1920 GCanvas/1.4.2.21")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
                                JSONObject array = JSONObject.parseObject(response.body());
                                if (array.getInteger("errno") == 0) {
                                    sendLog(array.getString("msg"));
                                    sendLog("接单成功");
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.naonao, 3000);
                                    addTask(mPlatform.getName());
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                } else {
                                    sendLog(array.getString("msg"));  //继续检测任务
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
