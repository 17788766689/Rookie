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
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 小雨伞
 */
public class XYSAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String buyerId = "";
    private String uid = "";

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
        HttpClient.getInstance().post("/App/Public/login.html", mPlatform.getHost())
                .params("rouepage", "pages/login/login")
                .params("platform_name", "")
                .params("serverVersion", "100")
                .params("version", "979")
                .params("platform", "android")
                .params("mobile", mParams.getAccount())
                .params("password", mParams.getPassword())
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));
                            if (jsonObject.getInteger("status") == 1) {    //登录成功
                                cookie = jsonObject.getJSONObject("data").getString("random");
                                uid = jsonObject.getJSONObject("data").getString("id");
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
        HttpClient.getInstance().post("/App/User/gettaobaolist.html", mPlatform.getHost())
                .params("type", "taobao")
                .params("rouepage", "pages/updatetaobao/updatetaobao")
                .params("serverVersion", "979")
                .params("uid", uid)
                .params("random", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("Success".equals(jsonObject.getString("message")) && jsonObject.getJSONArray("data").size() > 0) {    //获取买号成功
                                JSONArray array = jsonObject.getJSONArray("data");
                                mParams.setBuyerNum(new BuyerNum(array.getJSONObject(0).getString("id"), array.getJSONObject(0).getString("tb_account")));
                                updateBuyerId();
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    list.add(new BuyerNum(array.getJSONObject(i).getString("id"), array.getJSONObject(i).getString("tb_account")));
                                }
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                receipt();
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

    private void receipt() {
        HttpClient.getInstance().post("/App/User/tasksb", mPlatform.getHost())
                .params("tb_id", buyerId)
                .params("rouepage", "pages/xtorderindex/xtorderindex")
                .params("serverVersion", "979")
                .params("uid", uid)
                .params("random", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject array = JSONObject.parseObject(response.body());
                            sendLog(array.getString("message"));
                           // if (array.getInteger("status") == 1) {
                                startTask();
                            //} else {
                                //stop();
                            //}
                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }
                });
    }


    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().post("/App/Order/gettasktj", mPlatform.getHost())
                .params("rouepage", "pages/renwuguanli/renwuguanli")
                .params("serverVersion", "979")
                .params("uid", uid)
                .params("random", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            JSONArray array = obj.getJSONArray("data");
                            if (array.getJSONObject(0).getInteger("ordercount") > 0) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.xiaoyusan, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog("暂时没有任务");
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
     * 更新买号
     */
    private void updateBuyerId() {
        if (mParams.getBuyerNum() != null && !TextUtils.isEmpty(mParams.getBuyerNum().getId())) {
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
