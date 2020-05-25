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
import com.cainiao.util.ZhuanQianBao;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 领钱宝
 */
public class LQBAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private String uid = "";
    private String buyerId = "";


    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
        updateBuyerId();

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
        sendLog(MyApp.getContext().getString(R.string.being_login));
        long n = new Date().getTime();
        String content = "{\"username\":\"" + mParams.getAccount() + "\",\"password\":\"" + Utils.md5(mParams.getPassword()) + "\",\"code\":\"\"}";
        HttpClient.getInstance().post("/login/login?timestamp="+n, mPlatform.getHost())
                .params("username", mParams.getAccount())
                .params("password", Utils.md5(mParams.getPassword()))
                .params("content",  ZhuanQianBao.axiosApi("com.homebrew.login",content))
                .headers("Referer","http://129.211.145.172/login")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 1) {//登录成功
                                uid = jsonObject.getJSONArray("data").getJSONObject(0).getString("uid");
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
        String content = "{\"uid\":\"" + uid+ "\",\"site\":\"taobao\"}";
        HttpClient.getInstance().post("/MemberCenter/getdefaultbindvestlist?timestamp="+n, mPlatform.getHost())
                .params("method",ZhuanQianBao.axiosApi("com.homebrew.getconfirmedbindvestlist",content))
                .headers("Cookie",cookie)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://129.211.145.172/view/leaflets")
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

                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    if(obj.getInteger("fblack") == 1){
                                        if(obj.getInteger("f1") != 3 && mParams.isFilterCheck()){
                                            sendLog(obj.getString("vestname")+",此号只能接浏览单,已过滤");
                                        }else{
                                            list.add(new BuyerNum(obj.getString("id"), obj.getString("vestname")));
                                        }
                                    }else if(obj.getInteger("f1") == 3){
                                        list.add(new BuyerNum(obj.getString("id"), obj.getString("vestname")));
                                    }
                                }
                                mParams.setBuyerNum(new BuyerNum(list.get(0).getId(), list.get(0).getName()));
                                updateBuyerId();
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                paidan();
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
     * 开始派单
     */
    private void paidan() {
        long n = new Date().getTime();
        String content = "{\"uid\":\"" + uid+ "\",\"vestid\":\"" + buyerId+ ",0,0,0,0\",\"missiontype\":\"2\"}";
        System.out.println(content);
        HttpClient.getInstance().post("/MemberCenter/applyforamission?timestamp="+n, mPlatform.getHost())
                .params("method",ZhuanQianBao.axiosApi("com.homebrew.applyforamission",content))
                .headers("Cookie",cookie)
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://129.211.145.172/view/leaflets")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getString("message").equals("success")){
                                sendLog("开始派单...");
                                startTask();
                            }else{
                                stop();
                            }

                        } catch (Exception e) {
                            sendLog("派单异常！");
                            stop();
                        }
                    }
                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (isStart) {
                            //取最小频率和最大频率直接的随机数值作为刷单间隔
                            int period = mRandom.nextInt(65000 - 60000) + 60000;
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
     * 开始任务
     */
    private void startTask() {
        if(isStart == false)return;
        long n = new Date().getTime();
        String content = "{\"uid\":\"" + uid+ "\"}";
        HttpClient.getInstance().post("/MemberCenter/queryapplyforamission?timestamp="+n, mPlatform.getHost())
                .params("method",ZhuanQianBao.axiosApi("com.homebrew.queryapplyforamission",content))
                .headers("Cookie",cookie)
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Referer","http://129.211.145.172/view/leaflets")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            if(response.body().indexOf("login/kickout") != -1){
                                sendLog("登录已过期,接单过程中请勿在其他地方登录");
                                return;
                            }
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONArray("data");
                            if (array.size() == 0){
                                sendLog("继续检测任务");
                                return;
                            }
                            JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);
                            if (jsonObject.getString("message").equals("success") && !data.getString("missionid").equals("0")){
                                if(data.getJSONObject("missioninfo").getDouble("itemprice") <= (mParams.getMaxPrincipal()*100)){
                                    sendLog("检测到任务领取中");
                                    lqTask(data.getString("missionid"),data.getString("listid"));
                                }else{
                                    sendLog("任务不符合要求,本金"+(data.getJSONObject("missioninfo").getDouble("itemprice")/100));
                                }
                            }else{
                                sendLog("继续检测任务");
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

    private void lqTask(String missionid,String listid){
            long n = new Date().getTime();
            String content = "{\"uid\":\"" + uid+ "\",\"site\":\"taobao\",\"missionid\":\""+missionid+"\",\"listid\":\""+listid+"\",\"vestid\":\""+buyerId+"\"}";
            HttpClient.getInstance().post("/Task/Acquiremission?timestamp="+n, mPlatform.getHost())
                    .params("method",ZhuanQianBao.axiosApi("com.homebrew.acquiremission",content))
                    .headers("Cookie",cookie)
                    .headers("Content-Type", "application/json")
                    .headers("X-Requested-With","XMLHttpRequest")
                    .headers("Referer","http://129.211.145.172/view/leaflets")
                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                                JSONObject jsonObject = JSONObject.parseObject(response.body());
                                if (jsonObject.getString("message").equals("success")){
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    if (count == 0) {
                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.linqianbao, 3000);
                                    }
                                    count++;
                                    addTask(mPlatform.getName());
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                }else if (jsonObject.getString("message").equals("exists")) {
                                   sendLog("同一个小号只能领取一次相同任务");
                                } else if (jsonObject.getString("message").equals("limited")) {
                                    sendLog("当天领取任务到上限");
                                } else if (jsonObject.getString("message").equals("have unfinished mission")) {
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    if (count == 0) {
                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.linqianbao, 3000);
                                    }
                                    count++;
                                    addTask(mPlatform.getName());
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                } else if (jsonObject.getString("message").equals("vest blocked")) {
                                    sendLog("此买号因取消任务频繁已限制领取任务24小时，请更换其他买号");
                                } else if (jsonObject.getString("message").equals("mission not exists")) {
                                    sendLog("任务已被他人领取");
                                } else if (jsonObject.getString("message").equals("listid not exists")) {
                                    sendLog("任务已被领取完毕");
                                } else if (jsonObject.getString("message").equals("vest not match")) {
                                    sendLog("小号配对不了任务，请换个小号");
                                } else if (jsonObject.getString("message").equals("shop limited")) {
                                    sendLog("你已接过该商家的其他店铺，商家要求不能重复，请领取其他任务。");
                                } else if (jsonObject.getString("message").equals("vest limited")) {
                                    sendLog("小号领取上限，请换个小号");
                                } else if (jsonObject.getString("message").equals("busy")) {
                                    sendLog("网络开小差了，请稍后再试");
                                } else if (jsonObject.getString("message").equals("beginner limited")) {
                                    sendLog("新用户，每天只能接一单，需完成3单才能正常接任务。");
                                } else if (jsonObject.getString("message").equals("refuse")) {
                                    sendLog("任务领取出错，请重新申请派单");
                                } else {
                                    sendLog("手慢了，单子被人抢了");
                                }

                            } catch (Exception e) {
                                sendLog("获取买号异常！");
                                stop();
                            }
                        }
                    });
    }


    /**
     * 更新买号
     */
    private void updateBuyerId(){
        if(mParams.getBuyerNum() != null && !TextUtils.isEmpty(mParams.getBuyerNum().getId())){
            buyerId = mParams.getBuyerNum().getId();
        }
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
