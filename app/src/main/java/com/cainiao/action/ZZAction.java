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
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Random;

/**
 * 宅仔
 */
public class ZZAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String token;
    private String type = "1";

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
        HttpClient.getInstance().post("/iop/register/loginActApp", mPlatform.getHost())
                .params("moblie", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("devicename","" )
                .params("deviceid","" )
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Mobile Safari/537.36")
                .headers("Origin", "http://xk.51zugeju.com")
                .headers("Proxy-Connection", "keep-alive")
                .headers("Referer", "http://xk.51zugeju.com/iop/web/logionapp.html")
                .headers("X-Requested-With", "XMLHttpRequest")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.body().indexOf("btwaf") != -1){
                            token = response.body().toString().substring(response.body().indexOf("btwaf=")+6,response.body().indexOf("btwaf=")+14);
                            HttpClient.getInstance().post("/iop/register/loginActApp?btwaf="+token, mPlatform.getHost())
                                    .params("moblie", mParams.getAccount())
                                    .params("password", mParams.getPassword())
                                    .params("devicename","" )
                                    .params("deviceid","" )
                                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Mobile Safari/537.36")
                                    .headers("Origin", "http://xk.51zugeju.com")
                                    .headers("Proxy-Connection", "keep-alive")
                                    .headers("Referer", "http://xk.51zugeju.com/iop/web/logionapp.html")
                                    .headers("X-Requested-With", "XMLHttpRequest")
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            try {
                                                if (TextUtils.isEmpty(response.body())) return;
                                                if(response.body().indexOf("频繁") != -1){
                                                    sendLog("请求过于频繁,请几分钟后重试");
                                                    return;
                                                }
                                                JSONObject jsonObject = JSONObject.parseObject(response.body());
                                                if ("登录成功".equals(jsonObject.getString("msg"))) {    //登录成功
                                                    List<String> cookies = response.headers().values("Set-Cookie");
                                                    for (String str : cookies) {
                                                        cookie += str.substring(0, str.indexOf(";")) + ";";
                                                    }
                                                    sendLog("登录成功");
                                                    updateParams(mPlatform);
                                                    MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                                    updateStatus(mPlatform, 3); //正在接单的状态
                                                    startTask();
                                                } else {
                                                    MyToast.error(jsonObject.getString("msg"));
                                                    sendLog(jsonObject.getString("msg"));
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
                                            sendLog("如果一直异常,请停止接单,点击app内或者浏览器打开宅仔输入验证码");
                                        }
                                    });

                        }else {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
                                if(response.body().indexOf("频繁") != -1){
                                    sendLog("请求过于频繁,请几分钟后重试");
                                    return;
                                }
                                JSONObject jsonObject = JSONObject.parseObject(response.body());
                                if ("登录成功".equals(jsonObject.getString("msg"))) {    //登录成功
                                    List<String> cookies = response.headers().values("Set-Cookie");
                                    for (String str : cookies) {
                                        cookie += str.substring(0, str.indexOf(";")) + ";";
                                    }
                                    sendLog("登录成功");
                                    updateParams(mPlatform);
                                    MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                    updateStatus(mPlatform, 3); //正在接单的状态
                                    startTask();
                                } else {
                                    MyToast.error(jsonObject.getString("msg"));
                                    sendLog(jsonObject.getString("msg"));
                                    stop();
                                }
                            } catch (Exception e) {
                                sendLog("如果一直异常,请停止接单,点击app内或者浏览器打开宅仔输入验证码");
                                stop();
                            }
                        }
                    }
                });

    }

    /**
     * 开始任务
     */
    private void startTask() {
        if(isStart == false)return;
        HttpClient.getInstance().get("/iop/index/autoindex?type="+mParams.getType() , mPlatform.getHost())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Mobile Safari/537.36")
                .headers("Referer", "http://xk.51zugeju.com/iop/index/index")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Cookie", cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.body().indexOf("btwaf") != -1){
                            token = response.body().toString().substring(response.body().indexOf("btwaf=")+6,response.body().indexOf("btwaf=")+14);
                            HttpClient.getInstance().post("/iop/index/autoindex?type="+mParams.getType()+"&btwaf="+token, mPlatform.getHost())
                                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Mobile Safari/537.36")
                                    .headers("Referer", "http://xk.51zugeju.com/iop/index/index")
                                    .headers("X-Requested-With", "XMLHttpRequest")
                                    .headers("Cookie", cookie)
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            try {
                                                if (TextUtils.isEmpty(response.body())) {
                                                    sendLog("继续检测任务");
                                                } else {
                                                    JSONObject obj = JSONObject.parseObject(response.body());
                                                    sendLog(obj.getString("msg"));
                                                    if ("1".equals(obj.getString("status"))) {
                                                        sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zhaizai, 3000);
                                                        addTask(mPlatform.getName());
                                                        updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                                        isStart = false;
                                                    }
                                                }
                                            } catch (Exception e) {
                                                sendLog("如果一直异常,请停止接单,点击app内或者浏览器打开宅仔输入验证码");
                                            }
                                        }
                                    });
                        }else {
                            try {
                                if (TextUtils.isEmpty(response.body())) {
                                    sendLog("继续检测任务");
                                } else {
                                    JSONObject obj = JSONObject.parseObject(response.body());
                                    sendLog(obj.getString("msg"));
                                    if ("1".equals(obj.getString("status"))) {
                                        sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zhaizai, 3000);
                                        addTask(mPlatform.getName());
                                        updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                        isStart = false;
                                    }
                                }
                            } catch (Exception e) {
                                sendLog(""+response.body());
                            }
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("请点击浏览器打开宅仔然后输入验证码就可以了");
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
