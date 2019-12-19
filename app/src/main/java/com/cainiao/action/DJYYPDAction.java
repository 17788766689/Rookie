package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
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
 * 电竞艺游派单
 */
public class DJYYPDAction extends BaseAction {
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
        HttpClient.getInstance().get("/iop/web/logionapp.html", mPlatform.getHost())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            Document doc = Jsoup.parse(response.body());
                            Elements taskToken = doc.select("input[name=tok]");
                            token = taskToken.val();
                        } catch (Exception e) {
                            sendLog("登录异常");
                        }
                        sendLog(MyApp.getContext().getString(R.string.being_login));
                        HttpClient.getInstance().post("/iop/register/loginActApp", mPlatform.getHost())
                                .params("moblie", mParams.getAccount())
                                .params("password", mParams.getPassword())
                                .params("tok", token)
                                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                                .headers("Origin","http://yuntao.zhengfuz.com")
                                .headers("Proxy-Connection","keep-alive")
                                .headers("Referer","http://xmt.51zugeju.com/iop/web/logionapp.html")
                                .headers("X-Requested-With","XMLHttpRequest")
                                .execute(new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        try {
                                            if (TextUtils.isEmpty(response.body())) return;
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
                                });
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().get("/iop/index/autoindex?type="+mParams.getType(), mPlatform.getHost())
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .headers("Referer","http://xmt.51zugeju.com/iop/index/index")
                .headers("Cookie", cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())){
                                sendLog("继续检测任务");
                            }else{
                                JSONObject obj = JSONObject.parseObject(response.body());
                                sendLog(obj.getString("msg"));
                                if ("1".equals(obj.getString("status"))){
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.dianjinyiyou, 3000);
                                    addTask("电竞艺游");
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                }
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
