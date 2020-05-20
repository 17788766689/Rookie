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
 * 知了
 */
public class ZLAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private List<Map<String, String>> accountList = new ArrayList<>();
    private int index = 0;
    private String version = "2.0.0.202003090";
    private String data = "";

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
            cookie = "";
            count = 0;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            getVersion();
        }
    }

    private void getVersion() {
        HttpClient.getInstance().get("/api/checkAppUpdate", "http://www.zhuanzhuanduoweb.com:17008")
                .params("v", version)
                .params("identify", "zl")
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") != 3) {
                               version = jsonObject.getJSONObject("data").getString("filename");
                               version = version.replace(".wgtu","");
                                if (isStart == false) {
                                    login();
                                }
                            } else {
                                if (isStart == false) {
                                    login();
                                }
                            }
                        } catch (Exception e) {
                            sendLog("获取版本异常");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("获取版本异常");  //接单异常
                    }
                });
    }

    /**
     * 登录
     */
    private void login() {
        String ip_mac = "";
        if (mParams.getImei() == null || "".equals(mParams.getImei())) {
            ip_mac = Utils.getDeviceId();
        } else if (mParams.getImei() != null && mParams.getImei().length() != 15) {
            sendLog("你输入的设备码格式不正确");
            stop();
            return;
        } else {
            ip_mac = mParams.getImei();
        }
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/api/member/login.html", mPlatform.getHost())
                .params("cellphone", mParams.getAccount())
                .params("password", mParams.getPassword())
                .params("system_type", "android")
                .params("ip_mac", ip_mac)
                .params("ver", version)
                .params("verify", "")
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getIntValue("code") == 0) {//登录成功
                                cookie = response.headers().get("Set-Cookie").toString();
                                data = jsonObject.getString("data");
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
        accountList = new ArrayList<>();
        long n = new Date().getTime();
        HttpClient.getInstance().get("/api/member/platform", mPlatform.getHost())
                .params("ver", version)
                .params("verify", data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
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
                                int count = 0;
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    if (obj.getString("statusText").equals("审核通过")) {
                                        String num = obj.getString("accept_num");
                                        String[] num1 = num.split("/");
                                        if (Integer.parseInt(num1[1]) == Integer.parseInt(num1[0])) {
                                            sendLog("买号:" + obj.getString("account") + "今日已接满,自动过滤");
                                        } else {
                                            Map<String, String> map = new HashMap<>();
                                            if (obj.getInteger("isCheckTbAccount") == 0){
                                                checkTbAccount(obj.getString("id"),obj.getString("account"),obj.getString("type"));
                                            }
                                            // 判断用户勾选了那些接单类型就获取那些买号
                                            if (obj.getInteger("isaccept") == 1) {//买号已开启接单
                                                if (obj.getInteger("type") == 1) { // 淘宝
                                                    if (mParams.isTb()) {
                                                        if (count == 0) {
                                                            count = 1;
                                                            mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        }
                                                        list.add(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        map.put("type", "1");
                                                        map.put("id", obj.getString("id"));
                                                        map.put("isaccept", "1");
                                                        map.put("account", obj.getString("account"));
                                                        accountList.add(map);
                                                    } else {
                                                        setAccount(obj.getString("id"), "0",obj.getString("account"),"1");
                                                    }

                                                } else if (obj.getInteger("type") == 2) { // 拼多多
                                                    if (mParams.isPdd()) {
                                                        if (count == 0) {
                                                            count = 1;
                                                            mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        }
                                                        list.add(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        map.put("type", "2");
                                                        map.put("id", obj.getString("id"));
                                                        map.put("isaccept", "1");
                                                        map.put("account", obj.getString("account"));
                                                        accountList.add(map);
                                                    } else {
                                                        setAccount(obj.getString("id"), "0",obj.getString("account"),"2");
                                                    }

                                                } else if (obj.getInteger("type") == 3) { // 京东
                                                    if (mParams.isPdd()) {
                                                        if (count == 0) {
                                                            count = 1;
                                                            mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        }
                                                        list.add(new BuyerNum(obj.getString("id"), obj.getString("account") + "[已开启接单]"));
                                                        map.put("type", "3");
                                                        map.put("id", obj.getString("id"));
                                                        map.put("isaccept", "1");
                                                        map.put("account", obj.getString("account"));
                                                        accountList.add(map);
                                                    } else {
                                                        setAccount(obj.getString("id"), "0",obj.getString("account"),"3");
                                                    }

                                                }
                                            } else {
                                                if (mParams.isTb() && obj.getInteger("type") == 1) { // 淘宝
                                                    if (count == 0) {
                                                        count = 1;
                                                        mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    }
                                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    map.put("type", "1");
                                                    map.put("id", obj.getString("id"));
                                                    map.put("isaccept", "0");
                                                    map.put("account", obj.getString("account"));
                                                    accountList.add(map);
                                                } else if (mParams.isPdd() && obj.getInteger("type") == 2) { // 拼多多
                                                    if (count == 0) {
                                                        count = 1;
                                                        mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    }
                                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    map.put("type", "2");
                                                    map.put("id", obj.getString("id"));
                                                    map.put("isaccept", "0");
                                                    map.put("account", obj.getString("account"));
                                                    accountList.add(map);
                                                } else if (mParams.isPdd() && obj.getInteger("type") == 3) { // 京东
                                                    if (count == 0) {
                                                        count = 1;
                                                        mParams.setBuyerNum(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    }
                                                    list.add(new BuyerNum(obj.getString("id"), obj.getString("account")));
                                                    map.put("type", "3");
                                                    map.put("id", obj.getString("id"));
                                                    map.put("isaccept", "0");
                                                    map.put("account", obj.getString("account"));
                                                    accountList.add(map);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (accountList.size() > 0) {
                                    showBuyerNum(JSON.toJSONString(list));
                                    updateStatus(mPlatform, 3); //正在接单的状态
                                    if (isStart == false) {
                                        isStart = true;
                                        sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                        MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                        startTask();
                                    }
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
     * 获取token
     * @param orderid
     * @param uuid
     */
    private void getToken(String orderid,String uuid) {
        HttpClient.getInstance().get("/api/order/getAuth", mPlatform.getHost())
                .params("ver", version)
                .params("uuid", uuid)
                .params("verify", data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());

                            StringBuilder key = new StringBuilder();
                            key.append("{\"encrypt\":\"");
                            key.append(obj.getJSONObject("data").getString("unique_key"));
                            key.append("\"}");
                            String key2 = HelpUtil.encrypt(key.toString(),obj.getJSONObject("data").getString("public_key"));
                            key = new StringBuilder();
                            key.append("{\"orderid\":\"");
                            key.append(orderid);
                            key.append("\"}");
                            lqTask(key2,HelpUtil.encrypt(key.toString(),obj.getJSONObject("data").getString("public_key").replaceAll("\n","")),key.toString(),uuid);
                        } catch (Exception e) {
                            sendLog("获取token异常");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        sendLog("获取token异常");  //接单异常
                    }
                });
    }


    private void startTask() {
        if (isStart == false) return;
        getVersion();
        long n = new Date().getTime();
        String uuid = Utils.randomString(8);
        HttpClient.getInstance().get("/api/order/getAuth", mPlatform.getHost())
                .params("ver", version)
                .params("uuid", uuid)
                .params("verify", data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject object = JSONObject.parseObject(response.body());
                            StringBuilder key = new StringBuilder();
                            key.append("{\"encrypt\":\"");
                            key.append(object.getJSONObject("data").getString("unique_key"));
                            key.append("\"}");
                            HttpClient.getInstance().get("/api/order/acceptV2", mPlatform.getHost())
                                    .params("ver", version)
                                    .params("uuid", uuid)
                                    .params("verify", data)
                                    .params("token", HelpUtil.encrypt(key.toString(), object.getJSONObject("data").getString("public_key")))
                                    .headers("Cookie", cookie)
                                    .headers("X-Requested-With", "com.zhiliao.myapp")
                                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            try {
                                                if (TextUtils.isEmpty(response.body())) return;

                                                JSONObject obj = JSON.parseObject(response.body());
                                                if (obj.getInteger("code") == 1) {
                                                    sendLog("接单频率过快,重复出现请停止接单并提高接单频率");
                                                    return;
                                                }
                                                if (obj.getString("data").equals("[]")) {
                                                    sendLog("继续检测任务");
                                                    return;
                                                }
                                                if (response.body().contains("您有未完")) {
                                                    sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                                    if (count == 0) {
                                                        receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.zhiliao, 3000);
                                                    }
                                                    count++;
                                                    addTask(mPlatform.getName());
                                                    updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                                    isStart = false;
                                                    return;
                                                }
                                                JSONObject data = JSONObject.parseObject(obj.getString("data"));
                                                if (data.getInteger("accept_code") != 0) {
                                                    if(data.getString("msg").equals("当前账号没有校验，请先校验")){
                                                        return;
                                                    }
                                                    sendLog(data.getString("msg"));
                                                    return;
                                                }
                                                // 判断是否是花呗、信用卡
                                                if (data.getInteger("ishuabei") == 0 && data.getInteger("iscreditcard") == 0 && mParams.isXh()) {// 不是花呗和信用卡
                                                    // 取消任务
                                                    sendLog("检测到任务,非花呗、信用卡任务,已自动过滤");
                                                    closeTask(data.getString("orderid"));
                                                    return;
                                                }

                                                // 判断是否为审核任务
                                                if (data.getInteger("isneedselleraudit") == 1 && mParams.isShenhe()) {
                                                    // 取消任务
                                                    sendLog("检测到审核任务,已自动过滤");
                                                    closeTask(data.getString("orderid"));
                                                    return;
                                                }

                                                if (data.getDouble("commission") >= mParams.getMinCommission() && data.getDouble("buyprice") <= mParams.getMaxPrincipal()) { // 符合本金佣金要求
                                                    getToken(data.getString("orderid"),uuid);
                                                } else {
                                                    sendLog("检测到任务,不符合要求自动过滤,佣金:" + data.getDouble("commission") + ",本金:" + data.getDouble("buyprice"));
                                                    closeTask(data.getString("orderid"));
                                                    return;
                                                }

                                            } catch (Exception e) {
                                                sendLog("检测任务失败！");
                                            }
                                        }


                                        @Override
                                        public void onError(Response<String> response) {
                                            super.onError(response);
                                            sendLog("检测任务异常");  //接单异常
                                        }
                                    });
                        } catch (Exception e) {
                            sendLog("获取token异常");
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        sendLog("获取token异常");  //接单异常
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
                .headers("X-Requested-With", "com.zhiliao.myapp")
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
                .headers("X-Requested-With", "com.zhiliao.myapp")
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
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.ruyibao, 3000);
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
    private void updateBuyerId() {
        if (accountList.size() > 0) {
            Map<String, String> map = accountList.get(mParams.getBuyerNumIndex());
            if (Integer.parseInt(map.get("isaccept")) == 1) {
                sendLog("该买号已开启接单");
                return;
            }
            sendLog("正在切换买号...请勿重复点击");
            for (int i = 0; i < accountList.size(); i++) {
                Map<String, String> account = accountList.get(i);
                if (account.get("type").equals(map.get("type")) && account.get("id").equals(map.get("id"))) {
                    setAccount(map.get("id"), "1",map.get("account"),map.get("type"));
                } else if (account.get("type").equals(map.get("type"))) {
                    setAccount(account.get("id"), "0",map.get("account"),map.get("type"));
                    try {
                        Thread.sleep(2000);
                        setAccount(map.get("id"), "1",map.get("account"),map.get("type"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private void setAccount(String id, String isaccept,String account,String type) {
        HttpClient.getInstance().get("/api/member/platform_isaccept", mPlatform.getHost())
                .params("ver", version)
                .params("id", id)
                .params("isaccept", isaccept)
                .params("verify", data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            //checkTbAccount(id,account,type);
                        } catch (Exception e) {
                            sendLog("获取版本异常");
                            stop();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("获取版本异常");  //接单异常
                        stop();
                    }
                });
    }

    private void checkTbAccount(String id, String account,String type) {
        HttpClient.getInstance().get("/api/member/checkTbAccount", mPlatform.getHost())
                .params("ver", version)
                .params("platform_id", id)
                .params("account", account)
                .params("platform_type",type)
                .params("verify", data)
                .headers("Cookie", cookie)
                .headers("X-Requested-With", "com.zhiliao.myapp")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; Pro 7 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Html5Plus/1.0 (Immersed/24.296297)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg")+",即将开始接单,请耐心等待");
                            //getAccount();
                        } catch (Exception e) {
                            sendLog("获取版本异常");
                            stop();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("获取版本异常");  //接单异常
                        stop();
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
