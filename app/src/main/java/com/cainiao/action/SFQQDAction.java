package com.cainiao.action;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.activity.ReceiptActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 私房钱抢单
 */
public class SFQQDAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private List<String> accountList = new ArrayList<>();
    private String site = "taobao";
    private int index = 0;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
        if("2".equals(mParams.getType())){
            site = "jd";
        }else if("3".equals(mParams.getType())){
            site = "pdd";
        }
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
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/login/login", mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("password", Utils.md5(mParams.getPassword()))
                .params("code", new Random().nextInt(9999))
                .headers("Referer","http://19sf.cn/login")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 1) {//登录成功
                                List<String> list = response.headers().values("Set-Cookie");
                                for (String str : list) {
                                    cookie += str.substring(0, str.indexOf(";")) + ";";
                                }
                                sendLog("登录成功");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                sendLog("账号或者密码错误");
                                MyToast.error(jsonObject.getString("message"));
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
                        sendLog("登录异常");  //接单异常
                        stop();
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount() {

        long n = new Date().getTime();
        HttpClient.getInstance().post("/PersonalCenter/Getbindvestlist?timestamp="+new Date().getTime(), mPlatform.getHost())
                .params("site",site)
                .headers("Cookie",cookie)
                .headers("Content-Type", "application/json")
                .headers("Referer","http://19sf.cn/login")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONArray("data");
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0); //默认使用第一个买号
                                mParams.setBuyerNum(new BuyerNum("-1", "自动切换"));
                                System.out.println(mParams.isFilterCheck());
                                List<BuyerNum> list = new ArrayList<>();
                                list.add(new BuyerNum("-1", "自动切换"));
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    if(obj.getInteger("fblack") == 1){
                                        if(obj.getInteger("f1") != 3 && mParams.isFilterCheck()){
                                            sendLog(obj.getString("vestname")+",此号只能接浏览单,已过滤");
                                        }else{
                                            list.add(new BuyerNum(obj.getString("id"), obj.getString("vestname")));
                                            accountList.add(obj.getString("id"));
                                        }

                                    }else if(obj.getInteger("f1") == 3){
                                        list.add(new BuyerNum(obj.getString("id"), obj.getString("vestname")));
                                        accountList.add(obj.getString("id"));
                                    }

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
        HttpClient.getInstance().post("/MyssionHall/GetmissionlistOrVest", mPlatform.getHost())
                .params("page", 1)
                .params("missiontype", 2)
                .params("site", site)
                .params("time", new Date().getTime())
                .headers("Cookie",cookie)
                .headers("Referer","http://19sf.cn/login")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            System.out.println(mParams.getBuyerNum().getId());
                            if (TextUtils.isEmpty(response.body())) return;
                            String str = response.body().substring(response.body().indexOf("==") + 2, response.body().length());
                            JSONObject obj = JSONObject.parseObject(str);
                            JSONArray array = obj.getJSONArray("data");
                            if (array.size() > 0) {
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    System.out.println(obj.getInteger("commission"));
                                    if(obj.getDouble("commission") >= (mParams.getMinCommission()*100)){
                                        sendLog("正在领取第"+(i+1)+"任务,佣金"+(obj.getDouble("commission")/100));
                                        lqTask(obj.getString("id"),obj.getString("listid"));
                                    }else{
                                        sendLog("任务不符合要求,佣金"+(obj.getDouble("commission")/100));
                                    }
                                }
                            } else {
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
     * @param missionid
     * @param listid
     */
    private void lqTask(String missionid,String listid) {
        String vestid = "";
        if("-1".equals(mParams.getBuyerNum().getId()) && accountList.size() != 0){
            vestid = accountList.get(index);
            index++;
            if(index == accountList.size()){
                index = 0;
            }
        }else{
            vestid = mParams.getBuyerNum().getId();
        }
        long n = new Date().getTime();
        HttpClient.getInstance().post("/MyssionHall/Acquiremission?timestamp="+new Date().getTime(), mPlatform.getHost())
                .params("site", site)
                .params("missionid", missionid)
                .params("vestid",vestid)
                .params("listid",listid)
                .headers("Cookie",cookie)
                .headers("Referer","http://19sf.cn/login")
                .headers("Content-Type", "application/json")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if(jsonObject.getJSONArray("data").size() != 0){
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), "私房钱"), R.raw.sifangqian, 3000);
                                }
                                count++;
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
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
