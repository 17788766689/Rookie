package com.cainiao.action;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
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
 * 淘宝圈
 */
public class TBQAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private String cookie = "";
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
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
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/1/user/login_handler", mPlatform.getHost())
                .params("user_name", mParams.getAccount())
                .params("password", mParams.getPassword())
                .headers("Referer","http://www.tbquan88.com/1/user")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 0) {    //登录成功
                                List<String> list = response.headers().values("Set-Cookie");
                                for (String str : list) {
                                    cookie += str.substring(0, str.indexOf(";")) + ";";
                                }
                                sendLog("登录成功！");
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
                });
    }

    /**
     * 获取买号
     */
    private void getAccount() {
        HttpClient.getInstance().get("/1/pages/binded_account", mPlatform.getHost())
                .headers("Cookie", cookie)
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            Document doc = Jsoup.parse(response.body());
                            Elements tbData = doc.select("ul").select("li");
                            if (tbData.size() > 0) {    //获取买号成功
                                int count =0;
                                List<BuyerNum> list = new ArrayList<>();
                                for (int i = 0, len = tbData.size(); i < len; i++) {
                                    System.out.println(tbData.get(i).select(".col-30").select(".unbind-account").text());
                                    if("//cdn.tbquan88.com/binary/taobao_bind_ok.png".equals(tbData.get(i).select("img").attr("src"))){
                                        String href = tbData.get(i).select("a").attr("href");
                                        href = href.substring(href.indexOf("=")+1,href.length());
                                        if(count == 0){
                                            mParams.setBuyerNum(new BuyerNum(href, tbData.get(i).select(".item-title").text()));
                                        }
                                        count++;
                                        list.add(new BuyerNum(href, tbData.get(i).select(".item-title").text()));
                                    }
                                }
                                showBuyerNum(JSON.toJSONString(list));
                                sendLog(MyApp.getContext().getString(R.string.receipt_get_buyer_success));
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                isSuccess();
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

    private void isSuccess(){
        HttpClient.getInstance().post("/1/task/get_user_status", mPlatform.getHost())
                .params("task_type", "DIANFU")
                .headers("Cookie", cookie)
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Content-Type", "application/x-www-form-urlencoded ")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (obj.getJSONObject("msg").getIntValue("code") == 1) {
                                sendLog("接单成功");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.dashuwang, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else if (obj.getJSONObject("msg").getIntValue("code") == 2){
                                startTask();
                            }else {
                                jiance();
                            }
                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }
                });
    }

    private void jiance(){
        HttpClient.getInstance().get("/1/task/engine_start_df", mPlatform.getHost())
                .headers("Cookie", cookie)
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                           if (response.body().contains("请选择")){
                               Document doc = Jsoup.parse(response.body());
                               Elements tbData = doc.select(".btn-task-accept");
                               for (int i = 0, len = tbData.size(); i < len; i++) {
                                   if (tbData.get(i).attr("data-nick").equals(mParams.getBuyerNum().getName().replace("花呗已绑定",""))){
                                       mParams.getBuyerNum().setId(tbData.get(i).attr("data-tb_id"));
                                   }
                               }
                               new Handler().postDelayed(new Runnable() {
                                   @Override
                                   public void run() {
                                       getKey();
                                   }
                               },3000);
                           }else{
                               sendLog("继续检测任务！");
                               new Handler().postDelayed(new Runnable() {
                                   @Override
                                   public void run() {
                                       jiance();
                                   }
                               },3500);
                           }
                        } catch (Exception e) {
                            sendLog("检测任务异常！");
                        }
                    }
                });
    }

    private void getKey(){
        HttpClient.getInstance().post("/captcha/get", "http://154.204.41.165")
                .params("app_key", "0a017c93a3309fd092a61e048c850155")
                .params("challenge", "eb6daf666cc8da269070f56b441f5780")
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 1000) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        addTaskLine(jsonObject.getJSONObject("result").getString("challenge"));
                                    }
                                },4000);
                            } else {
                                sendLog("6秒后继续检测任务");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        jiance();
                                    }
                                },6000);
                            }
                        } catch (Exception e) {
                            sendLog("加入任务异常！");
                            stop();
                        }
                    }
                });
    }

    private void addTaskLine(String challenge){
        HttpClient.getInstance().post("/1/task/engine_start_handle?task_type=DIANFU", mPlatform.getHost())
                .params("tb_nick", mParams.getBuyerNum().getName().replace("花呗已绑定",""))
                .params("tb_id", mParams.getBuyerNum().getId())
                .params("challenge",challenge)
                .headers("Cookie", cookie)
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .headers("Connection","Keep-Alive")
                .headers("Accept-Encoding","gzip")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; 15 Build/NGI77B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            if (jsonObject.getInteger("code") == 0) {
                                startTask();
                            } else {
                                sendLog("6秒后继续检测任务");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        jiance();
                                    }
                                },6000);
                            }
                        } catch (Exception e) {
                            sendLog("接单异常！");
                            stop();
                        }
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        HttpClient.getInstance().post("/1/task/get_user_status", mPlatform.getHost())
                .params("task_type", "DIANFU")
                .headers("Cookie", cookie)
                .headers("Referer","http://www.tbquan88.com/1/main")
                .headers("X-Requested-With","XMLHttpRequest")
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (obj.getJSONObject("msg").getIntValue("code") == 1) {
                                sendLog("接单成功");
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.dashuwang, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog("继续检测任务");  //继续检测任务
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
