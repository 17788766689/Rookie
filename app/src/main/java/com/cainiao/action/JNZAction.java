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
import com.cainiao.util.HelpUtil;
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
 * 金牛座
 */
public class JNZAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private String data = "";
    private String version = "20200108";
    private String buyerId = "";
    private List<String> accountList;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);
        updateBuyerId();

        if (!isStart) {    //未开始抢单
            isStart = true;
            accountList = new ArrayList<>();
            cookie = "";
            count = 0;
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
        HttpClient.getInstance().post("/buyer/login/loginByPwd", mPlatform.getHost())
                .params("account", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("token", "-")
                .params("version",version)
                .params("_time",new Date().getTime())
                .params("terminal","2")
                .params("terminalSerial","-")
                .headers("Referer","http://m.q22q22.com/login.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 0) {//登录成功
                                data = jsonObject.getJSONObject("data").getString("token");
                                sendLog("登录成功");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                sendLog(jsonObject.getString("msg"));
                                MyToast.error(jsonObject.getString("msg"));
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
        sendLog("正在获取买号");
        long n = new Date().getTime();
        HttpClient.getInstance().post("/buyer/info/getBindInfo", mPlatform.getHost())
                .params("token", data)
                .params("version", version)
                .params("_time",new Date().getTime())
                .params("terminal",2)
                .params("terminalSerial","-")
                .headers("Referer","http://m.q22q22.com/login.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONArray("data");
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                                mParams.setBuyerNum(new BuyerNum("-1", "自动切换"));
                                updateBuyerId();
                                List<BuyerNum> list = new ArrayList<>();
                                list.add(new BuyerNum("-1", "自动切换"));
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    if(obj.getInteger("accountStatus") == 1){
                                        if(obj.getInteger("frequency") < obj.getInteger("acceptNumber")){
                                            accountList.add(obj.getString("accountId"));
                                            list.add(new BuyerNum(obj.getString("accountId"), obj.getString("bindAccountName")));
                                        }else{
                                            sendLog(obj.getString("bindAccountName")+"今日已接满,自动过滤");
                                        }
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

    private void startTask() {
        if (isStart == false) return;
        long n = new Date().getTime();
        if(count < accountList.size()){
            buyerId = accountList.get(count);
            count++;
        }else{
            count = 0;
        }
        HttpClient.getInstance().post("/buyer/order/getAuth", mPlatform.getHost())
                .params("token", data)
                .params("version", version)
                .params("_time",new Date().getTime())
                .params("terminal",2)
                .params("terminalSerial","-")
                .params("accountId",buyerId)
                .headers("Referer","http://m.q22q22.com/login.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject object = JSONObject.parseObject(response.body());

                            if(object.getInteger("code") == 404){
                               stop();
                            }else if(object.getInteger("code") == 1){
                                sendLog("接单成功");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.jinniuzuo, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            }else if(object.getString("msg").equals("先休息一下咯")){
                                sendLog("暂时没有任务");
                            }else{
                                sendLog(object.getString("msg"));
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常");
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        sendLog("检测任务异常");  //接单异常
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
     * 取消任务
     *
     * @param orderId
     */
    private void closeTask(String orderId) {
        HttpClient.getInstance().get("/api/order/noOrder", mPlatform.getHost())
                .params("orderid", orderId)
                .params("verify", data)
                .params("ver", version)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.xmtyq.zzd")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;

                        } catch (Exception e) {
                            sendLog("取消任务异常！");
                        }
                    }
                });
    }

    /**
     * 领取任务
     * @param token
     * @param orderid
     */
    private void lqTask(String token,String token2,String orderid ,String uuid) {
        long n = new Date().getTime();
        HttpClient.getInstance().get("/api/order/sureOrderV2", mPlatform.getHost())
                .params("token", token)
                .params("data", token2)
                .params("uuid", uuid)
                .params("ver",version)
                .params("verify",data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.xmtyq.zzd")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if(jsonObject.getInteger("code") == 0){
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zuanquanquan, 3000);
                                }
                                count++;
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            }else{
                                sendLog("继续检测任务");
                            }

                        } catch (Exception e) {
                            sendLog("领取任务异常！");
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
