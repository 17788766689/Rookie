package com.cainiao.action;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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
 * 征服者
 */
public class ZFZAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
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
    private void login(){
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/iop/register/loginActApp", mPlatform.getHost())
                .params("moblie", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("deviceid",Utils.md5(Build.DEVICE+Build.SERIAL))
                .params("devicename",Build.BRAND+" "+Build.MODEL+" Android "+Build.VERSION.RELEASE+" SDK "+Build.VERSION.SDK_INT)
                .headers("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Mobile/15E148 Safari/604.1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if("登录成功".equals(jsonObject.getString("msg"))){    //登录成功
                            List<String> cookies=response.headers().values("Set-Cookie");
                            for (String str : cookies){
                                cookie+=str.substring(0,str.indexOf(";"))+";";
                            }
                            sendLog("登录成功！");
                            updateParams(mPlatform);
                            MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                            updateStatus(mPlatform, 3); //正在接单的状态
                            startTask();
                        }else{
                            MyToast.error(jsonObject.getString("msg"));
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask(){
        HttpClient.getInstance().get("/iop/index/index.html", mPlatform.getHost())
                .headers("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Mobile/15E148 Safari/604.1")
                .headers("Cookie",cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        Document doc = Jsoup.parse(response.body());
                        Elements tbData = doc.select(".jin-sha-dan").select("a");
                        if (tbData.size() > 0){
                            sendLog("检测到任务领取中...");
                            getTask(tbData.get(0).attr("href"));
                        }else{
                            sendLog(MyApp.getContext().getString(R.string.receipt_continue_task));  //继续检测任务
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
     * 获取token
     */
    private void getTask(String url){
        String[] str = url.split("=");
        HttpClient.getInstance().get("/iop/index/attention.html?task_key_id="+str[1], mPlatform.getHost())
                .headers("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Mobile/15E148 Safari/604.1")
                .headers("Cookie",cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        Document doc = Jsoup.parse(response.body());
                        Elements taskToken = doc.select("input[name=token]");
                        lqTask(taskToken.val(),str[1]);
                    }
                });
    }

    /**
     * 领取任务
     * @param taskId  任务id
     */
    private void lqTask(String token,String taskId){
        HttpClient.getInstance().post("/iop/order/orderDown", mPlatform.getHost())
                .params("task_key_id",taskId)
                .params("access_token",token)
                .headers("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Mobile/15E148 Safari/604.1")
                .headers("Cookie",cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if(jsonObject.getIntValue("status") == 1){    //接单成功
                            sendLog(MyApp.getContext().getString(R.string.receipt_success));
                            receiveSuccess(String.format(MyApp.getContext().getString(R.string.receipt_success_tips), mPlatform.getName()), R.raw.zhengfuzhe, 3000);
                            addTask(mPlatform.getName());
                            updateStatus(mPlatform, Const.RECEIPT_SUCCESS); //接单成功的状态
                            isStart = false;
                        }else{
                            sendLog(jsonObject.getString("msg"));
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
