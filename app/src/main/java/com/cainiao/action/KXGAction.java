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
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 开心果
 */
public class KXGAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private int count = 0;
    private String userId;
    private String token;
    private String uuid;
    private JSONArray accountArray;
    private int type = 2;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

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
        uuid = Utils.md5(new Date().getTime() + "");
        sendLog(MyApp.getContext().getString(R.string.being_login));
        String url = "/u!login.htm?version=338&imei=" + uuid + "&imeimsg=8;5.1.1&sign=0&username=" + mParams.getAccount() + "&password=" + Utils.md5(mParams.getPassword())+"&stoken="+Utils.parmEncryption();
        HttpClient.getInstance().get(url, mPlatform.getHost())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if (jsonObject.getIntValue("status") == 0) {    //登录成功
                                userId = jsonObject.getJSONObject("user").getString("userid");
                                token = jsonObject.getJSONObject("user").getString("token");
                                updateParams(mPlatform);
                                getAccount();
                            } else {
                                MyToast.error(jsonObject.getString("msg"));
                                type = 0;
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("登录异常！");
                            type = 0;
                            stop();
                        }
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount() {
        long n = new Date().getTime();
        HttpClient.getInstance().get("/account!getbindbuyuserlist.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid=" + userId + "&token=" + token + "&plat=1&oid=0&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
//                        LogUtil.e("response: " + response.body());
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            JSONArray array = jsonObject.getJSONArray("list");
                            accountArray = array;
                            if (array.size() > 0) {    //获取买号成功
                                JSONObject obj = array.getJSONObject(0); ////默认使用第一个买号
                                mParams.setBuyerNum(new BuyerNum("0", obj.getString("tbuser")));
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = array.size(); i < len; i++) {
                                    obj = array.getJSONObject(i);
                                    list.add(new BuyerNum(i + "", obj.getString("tbuser")));
                                }
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                savesendacc();
                            } else { //无可用的买号
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                                type = 0;
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("获取买号异常！");
                            type = 0;
                            stop();
                        }
                    }
                });
    }

    /**
     * 保存配置
     */
    private void savesendacc() {

        long n = new Date().getTime();
        HttpClient.getInstance().get("/account!savesendacc.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid=" + userId + "&token=" + token + "&plat=1&bindid=" + accountArray.getJSONObject(mParams.getBuyerNumIndex()).getString("id") + "&devicetype=0&paytype=0&backtype=0&specialtype=1&tmallplat=0&price_min=" + mParams.getMinCommission() + "&price_max=1550&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("成功".equals(jsonObject.getString("msg"))) {
                                sendLog("配置成功,开始接单！");
                                sendLog("可通过卡本金不接浏览单,设置最小佣金为0时可接浏览单");
                               testing();
                            } else {
                                sendLog("配置失败！");
                                type = 0;
                                stop();
                            }
                        } catch (Exception e) {
                            sendLog("配置异常！");
                            type = 0;
                            stop();
                        }
                    }
                });
    }

    /**
     * 延时60秒后开始抢单
     */
    private void delayed(){
        Timer timer=new Timer();//实例化Timer类
        timer.schedule(new TimerTask(){
            public void run(){
                deliverTask();
                timer.cancel();
            }},65000);//五百毫秒
    }

    /**
     * 检查是否正在接单状态
     */
    private void testing(){
        HttpClient.getInstance().get("/task!waitresult.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid=" + userId + "&token=" + token+"&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());

                            if ("900".equals(jsonObject.getString("status"))) {
                                deliverTask();
                            } else {
                                startTask();
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }
                });
    }


    /**
     * 开始派单
     */
    private void deliverTask() {
        long n = new Date().getTime();
        isStart = true;
        HttpClient.getInstance().get("/task!trigger.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid="+userId+"&token="+token+"&clientid=99726ee9f79e9c74633f4c80cb941fc0&bindid_1="+accountArray.getJSONObject(mParams.getBuyerNumIndex()).getString("id")+"&stoken="+Utils.parmEncryption(),mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if ("成功".equals(jsonObject.getString("msg"))) {
                                //sendLog("请求成功！");
                                startTask();
                            } else if("1001".equals(jsonObject.getString("status"))){
                                sendLog("请求失败！"+jsonObject.getString("msg")+",请在一分钟后再次开始接单,请勿在他处登录");
                                sendLog("将在65秒后开始抢单");
                                delayed();
                            }else{
                                type = 0;
                                sendLog("请求失败！"+jsonObject.getString("msg"));
                                stop();
                            }
                        } catch (Exception e) {
                            type = 0;
                            sendLog("请求异常！");
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
        HttpClient.getInstance().post("/task!getorderbuylist.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid=" + userId + "&token=" + token + "&status=0&pageIndex=1&pageSize=20&isflow=0&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {

                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getString("msg").equals("成功") && jsonObject.getInteger("totalCount") > 0) {    //接单成功
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                if (count == 0) {
                                    receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.kaixinguo, 3000);
                                }
                                count++;
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else if(jsonObject.getString("status").equals("102")){
                                sendLog(jsonObject.getString("msg")+",请重新登录！");
                                type = 0;
                                stop();
                            }else {
                                HttpClient.getInstance().get("/task!waitresult.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid=" + userId + "&token=" + token+"&stoken"+Utils.parmEncryption(), mPlatform.getHost())
                                        .headers("Content-Type", "application/json")
                                        .execute(new StringCallback() {
                                            @Override
                                            public void onSuccess(Response<String> response) {
                                                try {
                                                    if (TextUtils.isEmpty(response.body())) return;
                                                    JSONObject jsonObject = JSONObject.parseObject(response.body());

                                                    if(jsonObject.getInteger("status")==0&&jsonObject.getJSONObject("msg").getInteger("price") >= mParams.getMinCommission()){
                                                        sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                                        if (count == 0) {
                                                            receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.kaixinguo, 3000);
                                                        }
                                                        count++;
                                                        addTask(mPlatform.getName());
                                                        updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                                        isStart = false;
                                                    }else{
                                                        if("800".equals(jsonObject.getString("status"))){
                                                            sendLog(jsonObject.getString("msg"));
                                                        }else if(jsonObject.getInteger("status") == 0){
                                                            sendLog("垫付"+jsonObject.getJSONObject("msg").getInteger("price")+"元，不符合要求,自动取消");
                                                            qxTask(jsonObject.getJSONObject("msg").getString("id"));

                                                        }else if(jsonObject.getInteger("status") == 900){
                                                            isStart = false;
                                                            deliverTask();
                                                        }else{
                                                            sendLog(jsonObject.getString("msg"));
                                                        }
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
                        } catch (Exception e) {
                            sendLog("检测任务异常");
                        }
                    }
                });
    }

    /**
     * 取消任务
     *
     * @param taskId 任务id
     */
    private void qxTask(String taskId) {

        long n = new Date().getTime();
        HttpClient.getInstance().post("/task!buycencaltask.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid="+userId+"&token="+token+"&id="+taskId+"&isflow=1&cancel_type=3&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            type = 1;
                            end();
                        } catch (Exception e) {
                            sendLog("撤销任务异常！");
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
        long n = new Date().getTime();
        if(type != 0){
            end();
        }

    }

    public void end(){
        HttpClient.getInstance().post("/task!canceltrigger.htm?version=338&imei=" + uuid +"&imeimsg=8;5.1.1&sign=0&userid="+userId+"&token="+token+"&stoken="+Utils.parmEncryption(), mPlatform.getHost())
                .headers("Content-Type", "application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if(type == 1){
                                deliverTask();
                                updateStatus(mPlatform, 3);
                            }
                        } catch (Exception e) {
                            sendLog("停止接单异常！");
                        }
                    }
                });
    }
}
