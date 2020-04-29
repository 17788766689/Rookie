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

import java.io.IOException;
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
 * 易筋经
 */
public class YJJAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";
    private String token = "";
    private Integer count = 0;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();
        if(TextUtils.isEmpty(mPlatform.getCookie()) || !mPlatform.getCookie().contains("sessionid")){
            sendLog("未登录...");
            return;
        }
//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            count = 0;
            cookie = "";
            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            startTask();
            MyToast.info("已开始接单，请点击弹窗外部隐藏弹窗");
        }
    }


    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().get("/mall/task/list_get/", mPlatform.getHost())
                .headers("Cookie",mPlatform.getCookie())
                .headers("Referer","https://yijingjin.club/mall/task/list_get/")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            Document doc = Jsoup.parse(response.body());
                            Elements tbData = doc.select("#list").select(".pro-items");
                            if(tbData.size() == 0){
                                sendLog("继续检测任务");
                            }else{
                                for (int i = 0;i<tbData.size();i++){
                                    String id = tbData.get(i).select("a").attr("href");
                                   Double yj = Double.parseDouble(tbData.get(i).select("a").select(".num").text().replaceAll(" ",""));
                                   if (yj>=mParams.getMinCommission() && mParams.getMaxPrincipal() >= yj){
                                        sendLog("检测到任务领取中");
                                        id = id.substring(id.indexOf("info_get/")+9,id.length()-1);
                                        lqTask(id);
                                    }else{
                                       sendLog("佣金:"+yj+",不符合要求,已自动过滤");
                                   }
                                }
                            }

                        } catch (Exception e) {
                            sendLog("检测任务异常");  //接单异常
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
        long n = new Date().getTime();
        HttpClient.getInstance().post("/mine/task/submit"+taskId, mPlatform.getHost())
                .params("count","")
                .headers("Cookie",mPlatform.getCookie())
                .headers("Referer","https://yijingjin.club/mine/task/submit"+taskId)
                .headers("User-Agent","Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 bsl/1.0")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 200 || "有未完成的订单".equals(jsonObject.getString("message") )) {    //接单成功
                                if (count == 0) {
                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.yijinjing, 3000);
                                    addTask(mPlatform.getName());
                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                    isStart = false;
                                    count++;
                                }

                            } else {
                                sendLog(jsonObject.getString("message"));
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
