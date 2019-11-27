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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 京东麦田
 */
public class JDMTAction extends BaseAction {
    private boolean isStart;
    String type = "";
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
        HttpClient.getInstance().post("/passport/placeOtherLogin.html", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("pwd", mParams.getPassword())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            List<String> cookies = response.headers().values("Set-Cookie");
                            cookie = "";
                            for (String str : cookies) {
                                cookie += str.substring(0, str.indexOf(";"));
                            }
                            if (Boolean.valueOf(response.body())) {    //登录成功
                                OkHttpClient okHttpClient = new OkHttpClient().newBuilder().followRedirects(false).build(); // OkHttpClient对象
                                RequestBody formBody = new FormBody.Builder().add("User[Username]", mParams.getAccount()).add("User[PassWord]",mParams.getPassword()).build(); // 表单键值对
                                Request request = new Request.Builder().url(mPlatform.getHost()+"/passport/login.html").post(formBody).addHeader("Cookie",cookie).build(); // 请求
                                okHttpClient.newCall(request).enqueue(new Callback() {// 回调
                                    public void onFailure(Call call, IOException e) {
                                        System.out.println(e.getMessage());//失败后的回调
                                    }

                                    @Override
                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                        cookie = "";
                                        List<String> cookies = response.headers().values("Set-Cookie");
                                        for (String str : cookies) {
                                            cookie += str.substring(0, str.indexOf(";"));
                                        }
                                        sendLog("登录成功！");
                                        updateParams(mPlatform);
                                        getAccount();
                                    }
                                });

                                /*HttpClient.getInstance().post("/passport/login.html", mPlatform.getHost())
                                        .params("User[Username]", mParams.getAccount())
                                        .params("User[PassWord]", mParams.getPassword())
                                        .headers("Cookie",cookie)
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                System.out.println(response.code()+"----");
                                                System.out.println(response.headers().get("Set-Cookie")+"----");
                                                *//*cookie = "";
                                                for (String str : cookies) {
                                                    cookie += str.substring(0, str.indexOf(";"));
                                                }
                                                sendLog("登录成功！");
                                                updateParams(mPlatform);
                                                getAccount();*//*
                                            }
                                        });*/
                            } else {
                                sendLog("账号或密码错误");
                                MyToast.error("账号或密码错误");
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }
                });
    }

    List<BuyerNum> list = null;

    /**
     * 获取买号
     */
    private void getAccount() {
        sendLog("正在获取买号");
        type = "";
        if (Integer.valueOf(mParams.getType()) == 1){
            type = "4";
        }else if (Integer.valueOf(mParams.getType()) == 2){
            type = "2";
        }else{
            type = "6";
        }
        HttpClient.getInstance().post("/site/QiangRenWu.html", mPlatform.getHost())
                .params("checkBase","DOIT")
                .params("platform",type)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.92 Mobile Safari/537.36 Html5Plus/1.0")
                .headers("Cookie", cookie)
                .headers("Accept","text/html")
                .headers("X-Requested-With","XMLHttpRequest")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            Document doc = Jsoup.parse(response.body());
                            Elements tbData = doc.select("input[type='radio']");
                            if (tbData.size() > 0) {    //获取买号成功
                                mParams.setBuyerNum(new BuyerNum(tbData.get(0).attr ("value"), tbData.get(0).attr("value")));
                                list = new ArrayList<>();
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    list.add(new BuyerNum(tbData.get(i).attr("value"), tbData.get(i).attr("value")));
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

    private int ii = 0;
    //自动切换买号
    private void upByerAccount(){
        ii++;
        if (ii < list.size()){
            mParams.setBuyerNum(new BuyerNum(list.get(ii).getId(), list.get(ii).getName()));
            BuyerNum bb = list.get(ii);
            list.remove(ii);
            list.add(0, bb);
            showBuyerNum(JSON.toJSONString(list));
        }else{
            ii = 0;
        }
    }

    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().get("/site/taobaoTask.html", mPlatform.getHost())
                .headers("Cookie", cookie)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.92 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            Document doc = Jsoup.parse(response.body());
                            Elements tbData = doc.select(".taskTask");
                            if (tbData.size() > 0) {
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    if (Integer.valueOf(tbData.get(i).attr("platform")) == Integer.valueOf(type)) {
                                        lqTask(tbData.get(i).attr("lang"));
                                    }
                                }
                                upByerAccount();
                            } else {
                                upByerAccount();
                                sendLog(MyApp.getContext().getString(R.string.receipt_continue_task));  //继续检测任务
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

    /**
     * 领取任务
     *
     * @param taskId 任务id
     */
    private void lqTask(String taskId) {
        HttpClient.getInstance().post("/site/QiangRenWu.html", mPlatform.getHost())
                .params("taskerWangwang", mParams.getBuyerNum().getId())
                .params("taskid", taskId)
                .params("willdianfu","0")
                .headers("Cookie", cookie)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.92 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            if ("SUCCESS".equals(response.body().toString())) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.jingdongmaitian, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            } else {
                                sendLog(mParams.getBuyerNum().getId()+"-"+response.body().toString());
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
