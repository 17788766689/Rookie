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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 爱米
 */
public class AMAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "Platform=Android;";
    private String xtoken = "";
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
            cookie = "Platform=Android;";
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            getToken();
        }
    }

    private void getToken() {
        HttpClient.getInstance().get("/v2/public/option-website", mPlatform.getHost())
                .headers("Platform", "android")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            List<String> cookies = response.headers().values("Set-Cookie");
                            int count = 0;
                            for (String str : cookies) {
                                if (count == 0) {
                                    count = 1;
                                    xtoken = str.substring(str.indexOf("=") + 1, str.indexOf(";"));
                                }
                                cookie += str.substring(0, str.indexOf(";")) + ";";
                            }
                            login();
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }
                });
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/buyer/user/login", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("type", 0)
                .headers("Platform", "Android")
                .headers("X-XSRF-TOKEN", xtoken)
                .headers("X-Token", token)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "https://www.huimi123.com/login")
                .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 200) {    //登录成功
                                sendLog("登录成功！");
                                token = jsonObject.getJSONObject("data").getString("token");
                                cookie += "token="+token+";";
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                MyToast.error(jsonObject.getString("message"));
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
        sendLog("正在获取买号...");
        HttpClient.getInstance().get("/v2/account/list?type=1&status=1", mPlatform.getHost())
                .headers("Platform", "Android")
                .headers("X-XSRF-TOKEN", xtoken)
                .headers("X-Token", token)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "https://www.huimi123.com/")
                .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        //try {
                        if (TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        JSONArray tbData = jsonObject.getJSONObject("data").getJSONArray("list");
                        if (tbData.size() > 0) {    //获取买号成功


                            int count = 0;
                            List<BuyerNum> list = new ArrayList<>();
                            for (int i = 0, len = tbData.size(); i < len; i++) {
                                JSONObject obj = tbData.getJSONObject(i);
                                if (obj.getInteger("status") == 1) {
                                    if (count == 0) {
                                        count++;
                                        mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                        updateBuyerId();
                                        isStart = true;
                                        try {
                                            Thread.sleep(2000);
                                            startTask();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                }else{
                                    sendLog("买号:"+obj.getString("account")+"状态异常,自动过滤");
                                }

                            }
                            showBuyerNum(JSON.toJSONString(list));
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                            MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                            updateStatus(mPlatform, 3); //正在接单的状态

                        } else { //无可用的买号
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                            stop();
                        }
                      /*  } catch (Exception e) {
                            sendLog("获取买号异常！");
                            stop();
                        }*/
                    }
                });
    }

    private void setDefault() {
        {
            Map map = new HashMap();
            map.put("status", "1");
            map.put("type", "1");
            map.put("account_id", buyerId);
            String param = JSON.toJSONString(map);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, param);
            HttpClient.getInstance().post("/v2/account/set-default", mPlatform.getHost())
                    .upRequestBody(body)
                    .headers("Platform", "Android")
                    .headers("X-XSRF-TOKEN", xtoken)
                    .headers("X-Token", token)
                    .headers("Cookie", cookie)
                    .headers("X-Requested-With", "XMLHttpRequest")
                    .headers("Referer", "https://www.huimi123.com/")
                    .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
                                updateStatus(mPlatform, 3); //正在接单的状态

                            } catch (Exception e) {
                                sendLog("获取买号异常！");
                                stop();
                            }
                        }
                    });
        }
    }

    /**
     * 开始任务
     */
    private void startTask() {
        {
            HttpClient.getInstance().get("/v2/account/check-set", mPlatform.getHost())
                    .headers("Platform", "Android")
                    .headers("X-XSRF-TOKEN", xtoken)
                    .headers("X-Token", token)
                    .headers("Cookie", cookie)
                    .headers("X-Requested-With", "XMLHttpRequest")
                    .headers("Referer", "https://www.huimi123.com/")
                    .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            HttpClient.getInstance().get("/buyer/order/list-current?page=1&size=20", mPlatform.getHost())
                                    .headers("Platform", "Android")
                                    .headers("X-XSRF-TOKEN", xtoken)
                                    .headers("X-Token", token)
                                    .headers("Cookie", cookie)
                                    .headers("X-Requested-With", "XMLHttpRequest")
                                    .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            try {
                                                if (TextUtils.isEmpty(response.body())) return;
                                                JSONObject array = JSONObject.parseObject(response.body());
                                                if (!(response.body().contains("[]")) && response.body().contains("任务")) {
                                                    sendLog("检测到任务领取中...");
                                                    JSONArray taskArray = array.getJSONObject("data").getJSONArray("list");
                                                    lqTask(String.valueOf(taskArray.getJSONObject(0).getString("id")));
                                                    if (taskArray.size() > 1) {
                                                        lqTask(String.valueOf(taskArray.getJSONObject(1).getString("id")));
                                                    }
                                                } else if (200 == array.getInteger("code")) {
                                                    sendLog("继续检测任务");  //继续检测任务
                                                } else {
                                                    sendLog(array.getString("message"));
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

    }

    /**
     * 领取任务
     *
     * @param taskId 任务id
     */
    private void lqTask(String taskId) {
        HttpClient.getInstance().get("/v2/task/obtain?order_id=" + taskId, mPlatform.getHost())
                .headers("Platform", "Android")
                .headers("X-XSRF-TOKEN", xtoken)
                .headers("X-Token", token)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "https://www.huimi123.com/")
                .headers("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-cn; MI 6 Build/OPR1.170623.027) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 200 || "请先完成进行中的任务方可继续接取".equals(jsonObject.getString("message"))) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.aimi, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            } else {
                                sendLog(jsonObject.getString("message"));
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
    private void updateBuyerId() {
        if (mParams.getBuyerNum() != null && !TextUtils.isEmpty(mParams.getBuyerNum().getId())) {
            buyerId = mParams.getBuyerNum().getId();
        }
        if (isStart) {
            setDefault();
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
