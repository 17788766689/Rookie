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
 * 发财树
 */
public class FCSAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String token = "";
    private String userId;
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
        Map map = new HashMap();
        map.put("telephone",mParams.getAccount());
        map.put("password",mParams.getPassword());
        String param = JSON.toJSONString(map);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, param);
        HttpClient.getInstance().post("/tcbuyer/ajaxLogin", mPlatform.getHost())
                .upRequestBody(body)
                .headers("Content-Type","application/json")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if("登录成功".equals(jsonObject.getString("message"))){    //登录成功
                            sendLog("登录成功！");
                            token = jsonObject.getString("token");
                            userId = jsonObject.getString("buyerId");
                            updateParams(mPlatform);
                            getAccount();
                        }else{
                            sendLog(jsonObject.getString("message"));
                            MyToast.error(jsonObject.getString("message"));
                            stop();
                        }
                    }
                });
    }

    /**
     * 获取买号
     */
    private void getAccount(){
        Map map = new HashMap();
        map.put("buyerId",Integer.valueOf(userId));
        String param = JSON.toJSONString(map);
        MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSONType, param);
        HttpClient.getInstance().post("/tcbuyer/person/findAccount", mPlatform.getHost())
               .upRequestBody(body)
                .headers("Content-Type","application/json")
                .headers("Cookie","JSESSIONID="+token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        JSONArray tbData = jsonObject.getJSONArray("Accountlist");
                        if(tbData.size() > 0){    //获取买号成功
                            JSONObject index = tbData.getJSONObject(0);
                            mParams.setBuyerNum(new BuyerNum(index.getString("id"), index.getString("acountName")));
                            List<BuyerNum> list = new ArrayList<>();
                            for(int i = 0, len = tbData.size(); i < len; i++){
                                index = tbData.getJSONObject(i);
                                list.add(new BuyerNum(index.getString("id"), index.getString("acountName")));
                            }
                            showBuyerNum(JSON.toJSONString(list));
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                            MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                            updateStatus(mPlatform, 3); //正在接单的状态
                            startTask();
                        }else { //无可用的买号
                            sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_fail));
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask(){
        long time = new Date().getTime();
        Map map = new HashMap();
        map.put("deviceType",2);
        map.put("accountId",mParams.getBuyerNum().getId());
        map.put("taskType",1);
        map.put("pageNo",1);
        map.put("time",time);
        map.put("buyerId",userId);
        map.put("platform",1);
        map.put("token",Utils.md5("TCfghFGH123!@#"+time+userId));
        String param = JSON.toJSONString(map);
        MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSONType, param);
        HttpClient.getInstance().post("/tcbuyer/task/getTaskHallList", mPlatform.getHost())
                .upRequestBody(body)
                .headers("Content-Type","application/json")
                .headers("Cookie","JSESSIONID="+token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        JSONArray array = jsonObject.getJSONArray("taskList");
                        if (array.size()>0){
                            sendLog("检测到任务领取中...");
                            for(int i = 0, len = array.size(); i < len; i++){
                                JSONObject object = array.getJSONObject(i);
                                if(object.getDouble("principal") <= mParams.getMaxPrincipal()){
                                    sendLog(String.format(MyApp.getContext().getString(R.string.receipt_get_task), object.getString("principal"), object.getString("commission")));
                                    lqTask(object.getString("subtaskId"));
                                }else{
                                    sendLog("不符合本金条件过滤掉。。");
                                }
                            }
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
     * 领取任务
     * @param taskId  任务id
     */
    private void lqTask(String taskId){
        Map map = new HashMap();
        map.put("deviceType",2);
        map.put("accountId",mParams.getBuyerNum().getId());
        map.put("subtaskId",taskId);
        map.put("buyerId",userId);
        map.put("platform",1);
        String param = JSON.toJSONString(map);
        MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSONType, param);
        HttpClient.getInstance().post("/tcbuyer/task/receiptTask", mPlatform.getHost())
                .upRequestBody(body)
                .headers("Content-Type","application/json")
                .headers("Cookie","JSESSIONID="+token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if("领取成功".equals(jsonObject.getString("message"))){    //接单成功
                            sendLog(MyApp.getContext().getString(R.string.receipt_success));
                            receiveSuccess(String.format(MyApp.getContext().getString(R.string.receipt_success_tips), mPlatform.getName()), R.raw.facaishu, 3000);
                            addTask(mPlatform.getName());
                            updateStatus(mPlatform, Const.RECEIPT_SUCCESS); //接单成功的状态
                            isStart = false;
                        }else{
                            sendLog(jsonObject.getString("message"));
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
