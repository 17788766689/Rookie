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

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 大师兄
 */
public class DSXAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private String userId;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String timeStr;
    private String random;
    private String devid;
    private String type;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            type = mParams.getType();
            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            login();
        }
    }

    private String timese() {
        String time = new Date().getTime() + "";
        time = time.substring(0, 10);
        return time;
    }

    private String getSign() {
        String rData = "ctime=" + timeStr + "devtype=androidrtick=" + random + "v=1.0.7d7xhk8x52wc8f7kp";
        return Utils.md5(rData);
    }

    /**
     * 登录
     */
    private void login() {
        devid = Utils.md5(new Date().getTime() + "").substring(0,30);
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/api/authorizations", mPlatform.getHost())
                .params("phone", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            System.out.println(jsonObject.getString("message"));
                            if (jsonObject.getString("access_token") != null) {    //登录成功
                                sendLog("登录成功！");
                                token = jsonObject.getString("access_token");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                sendLog("账号或者密码错误");
                                MyToast.error("账号或者密码错误");
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
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
        sendLog("正在获取买号");
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        HttpClient.getInstance().get("/api/buyers/"+type, mPlatform.getHost())
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer " + token)
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray tbData = jsonObject.getJSONArray("data");
                            if (tbData.size() > 0) {    //获取买号成功
                                List<BuyerNum> list = new ArrayList<>();
                                int count = 0;
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    if (type.equals("1")) {
                                        if (tbData.getJSONObject(i).getString("status_name").equals("审核通过") && tbData.getJSONObject(i).getString("platform_name").equals("淘宝")) {
                                            if (count == 0) {
                                                mParams.setBuyerNum(new BuyerNum(tbData.getJSONObject(i).getString("id"), tbData.getJSONObject(i).getString("name")));
                                            }
                                            list.add(new BuyerNum(tbData.getJSONObject(i).getString("id"), tbData.getJSONObject(i).getString("name")));
                                            count++;
                                        }
                                    } else {
                                        if (tbData.getJSONObject(i).getString("status_name").equals("审核通过") && tbData.getJSONObject(i).getString("platform_name").equals("京东")) {
                                            if (count == 0) {
                                                mParams.setBuyerNum(new BuyerNum(tbData.getJSONObject(i).getString("id"), tbData.getJSONObject(i).getString("name")));
                                            }
                                            list.add(new BuyerNum(tbData.getJSONObject(i).getString("id"), tbData.getJSONObject(i).getString("name")));
                                            count++;
                                        }
                                    }
                                }
                                if (count != 0) {
                                    showBuyerNum(JSON.toJSONString(list));
                                    sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                    MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                    updateStatus(mPlatform, 3); //正在接单的状态
                                    inspect();
                                } else {
                                    sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                                    stop();
                                }
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
     * 检查是否有待操作任务
     */
    private void inspect() {
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        HttpClient.getInstance().get("/api/orders/"+type+"/10", mPlatform.getHost())
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Authorization", "Bearer " + token)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONArray("data");
                            if (array.size() > 0) {    //登录成功
                                sendLog("接单成功,店铺名:"+array.getJSONObject(0).getString("shop_name"));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName())+",店铺名:"+array.getJSONObject(0).getString("shop_name"), R.raw.dashixiong, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                                stop();
                            }else{
                                startTask();
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        long time = new Date().getTime();
        HttpClient.getInstance().get("/api/tasks/"+type+"/0", mPlatform.getHost())
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Authorization", "Bearer " + token)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if(jsonObject.getJSONArray("data") != null){
                                JSONArray array = jsonObject.getJSONArray("data");
                                if (array.size() > 0) {    //登录成功

                                }else{
                                    sendLog("继续检测任务");
                                }
                            }else if(jsonObject.getInteger("code") == 1002){
                                sendLog("检测到验证码,菜鸟正在为你自动识别");
                                isStart = false;
                                getYzm();
                            }else{
                                sendLog(jsonObject.getString("message"));
                            }

                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }
                    @Override
                    public void onFinish() {
                        if (!mParams.getType().equals(type)) {
                            isStart = false;
                            startTask();
                        }
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
     * 获取验证码
     */
    private  void getYzm() {
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        HttpClient.getInstance().post("/api/captchas/", mPlatform.getHost())
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer " + token)
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            String conten = jsonObject.getString("captcha_image_content");
                            conten = conten.substring(conten.indexOf("base64,")+7,conten.length());
                            System.out.println(conten);
                            sbYzm(conten);
                        } catch (Exception e) {
                            sendLog("获取验证码异常");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
                        } catch (Exception e) {
                            sendLog("获取验证码异常");
                            stop();
                        }
                    }
                });
    }

    /**
     * 识别验证码
     */
    private  void sbYzm(String conten) {
        HttpClient.getInstance().post("/ocr/dsxOcr","http://192.168.1.124:8080")
                .params("base64", conten)
                .headers("Content-Type", "application/json")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;

                        } catch (Exception e) {
                            sendLog("识别验证码异常");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
                        } catch (Exception e) {
                            sendLog("获取验证码异常");
                            stop();
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
        timeStr = timese();
        random = Utils.getRandomNickname(10);
        HttpClient.getInstance().post("/api/orders/", mPlatform.getHost())
                .params("buyer_id", mParams.getBuyerNum().getId())
                .params("task_no", taskId)
                .params("devtype", "android")
                .params("devid", devid)
                .params("v", "1.0.7")
                .params("devname", "XiaoMI9/8.0.0")
                .params("ctime", timeStr)
                .params("rtick", random)
                .params("sign", getSign())
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer " + token)
                .headers("X-Requested-With", "io.dcloud.UNI1520480")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;

                        } catch (Exception e) {
                            sendLog("领取任务异常！");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        try {
                            super.onError(response);
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("message"));  //接单异常
                        } catch (Exception e) {
                            sendLog("领取任务异常！");
                            stop();
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
