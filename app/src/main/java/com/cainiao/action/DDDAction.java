package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HttpClient;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 单多多
 */
public class DDDAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie;
    private String tokey;
    private String callback;
    private String url = "";
    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
        if(TextUtils.isEmpty(mPlatform.getCookie()) || !mPlatform.getCookie().contains("user=")){
            sendLog("未登录...");
            return;
        }
//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            cookie = "";
            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            startTask();
        }
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/member/user/login.html", "http://www.027k8.com")
                .params("mobile",mParams.getAccount())
                .params("password",mParams.getPassword())
                .params("j_verify",Utils.randomString(4))
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "http://www.027k8.com/login.html")
                .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;

                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("info"));
                            if (1 == jsonObject.getIntValue("status")) {    //登录成功
                                List<String> cookies = response.headers().values("Set-Cookie");
                                for (String str : cookies) {
                                    cookie += str.substring(0, str.indexOf(";"))+";";
                                }
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                startTask();
                            } else {
                                MyToast.error(jsonObject.getString("info"));
                                stop();
                            }
                        } catch (Exception e) {
                            stop();
                            sendLog("登录异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }
                });
    }

    private void index(){
        callback = "jQuery11130"+Utils.getRandom(16)+"_"+new Date().getTime();
        HttpClient.getInstance().get("index", "http://www.027k8.com/")
                .headers("Cookie",mPlatform.getCookie())
                .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            tokey = response.body().substring(response.body().indexOf("tokey=")+6,response.body().indexOf("=\","));
                            startTask();
                        } catch (Exception e) {
                            stop();
                            sendLog("登录异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        if (isStart == false)return;
        if("1".equals(mParams.getType()) &&(!url.equals("/index/dispatch"))){
            url = "/index/dispatch";
            index();
            return;
        } else if ("2".equals(mParams.getType())&&(!url.equals("/Jd/Index/dispatch"))){
            url = "/Jd/Index/dispatch";
            index();
            return;
        }else if("3".equals(mParams.getType())&&(!url.equals("/Pdd/Index/dispatch"))){
            url = "/Pdd/Index/dispatch";
            index();
            return;
        }
        Random rd = new Random();
        HttpClient.getInstance().get(url,"http://api.honghou8.com")
                .params("tokey",tokey)
                .params("callback",callback)
                .params("_",new Date().getTime())
                .headers("Cookie",mPlatform.getCookie())
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            callback = response.body().substring(0,response.body().indexOf("({"));
                            String data = response.body().substring(response.body().indexOf("({"),response.body().length());
                            data = data.substring(1,data.length()-1);
                            JSONObject obj = JSONObject.parseObject(data);
                            if(1 == obj.getInteger("status") || "isSuccess".equals(obj.getString("info")) || "refundSuccess".equals(obj.getString("info"))){
                                sendLog("接单成功");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.danduoduo, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            }else if(obj.getString("info").equals("needbank")){
                                sendLog("请先绑定银行卡");
                                stop();
                            }else if(obj.getString("info").equals("rateSuccess")){
                                sendLog("有订单待评价");
                                stop();
                            }else {
                                sendLog("继续检测任务");
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("接单异常！");
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
