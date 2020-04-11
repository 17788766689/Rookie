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
import com.cainiao.util.HelpUtil;
import com.cainiao.util.HttpClient;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 有单做
 */
public class YDZAction extends BaseAction {
    private boolean isStart;
    private Handler mHandler;
    private Platform mPlatform;
    private Params mParams;
    private Random mRandom;
    private String cookie = "";
    private String token = "";
    private String taskToken = "";

    @Override
    public void start(Platform platform) {
        if (platform == null) return;
        mPlatform = platform;
        mParams = platform.getParams();

//        isStart = true;
//        updatePlatform(mPlatform);
//        updateStatus(platform, Const.AJW_VA);

        if (!isStart) {    //未开始抢单
            isStart = true;
            mHandler = new Handler();
            mRandom = new Random();
            updatePlatform(mPlatform);
            getToken();
        }
    }

    /**
     * 获取验证码
     */
    public void getVerifyCode(Platform platform) {
        sendMsg("get_verifycode", "http://www.127yjs.com/new_captcha.html?id=LAY-user-get-vercode&height=37&width=130&font_size=18&time=0." + new Date().getTime());
    }

    private void getToken(){
            HttpClient.getInstance().get("/portal/login/index.html", mPlatform.getHost())
                    .headers("Cookie",mPlatform.getVerifyCodeCookie())
                    .headers("Referer","http://www.127yjs.com/portal/index/index.html")
                    .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                if (TextUtils.isEmpty(response.body())) return;
                                Document doc = Jsoup.parse(response.body());
                                token = doc.select("#publicKey").val();
                                login();
                            } catch (Exception e) {
                                stop();
                                getVerifyCode(mPlatform);
                                sendLog("登录异常");  //接单异常
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                        }
                    });
    }

    /**
     * 登录
     */
    private void login() {
        sendLog(MyApp.getContext().getString(R.string.being_login));
        HttpClient.getInstance().post("/portal/login/dologin.html", mPlatform.getHost())
                .params("mobile", mParams.getAccount())
                .params("user_pass", HelpUtil.encrypt(mParams.getPassword(),token))
                .params("captcha",mParams.getVerifyCode())
                .params("_captcha_id","LAY-user-get-vercode")
                .headers("Cookie",mPlatform.getVerifyCodeCookie())
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            sendLog(jsonObject.getString("msg"));
                            if ("登录成功！".equals(jsonObject.getString("msg"))) {    //登录成功
                                List<String> list = response.headers().values("Set-Cookie");
                                for (String str : list) {
                                    cookie += str.substring(0, str.indexOf(";")) + ";";
                                }
                                updateParams(mPlatform);
                                MyToast.info(MyApp.getContext().getString(R.string.receipt_start));
                                updateStatus(mPlatform, 3); //正在接单的状态
                                getIndex();
                            } else {
                                getVerifyCode(mPlatform);
                                MyToast.error(jsonObject.getString("msg"));
                                stop();
                            }
                        } catch (Exception e) {
                            stop();
                            getVerifyCode(mPlatform);
                            sendLog("登录异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }
                });
    }


    private void getIndex(){
        HttpClient.getInstance().get("/portal/index/index.html", mPlatform.getHost())
                .headers("Cookie",cookie)
                .headers("Referer","http://www.127yjs.com/portal/index/index.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            taskToken = response.body().substring(response.body().indexOf("token=")+6,response.body().indexOf("token=")+70);
                            startTask();
                        } catch (Exception e) {
                            stop();
                            getVerifyCode(mPlatform);
                            sendLog("接单异常");  //接单异常
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog(MyApp.getContext().getString(R.string.receipt_exception) + mParams.getType());  //接单异常
                    }
                });
    }

    /**
     * 开始任务
     */
    private void startTask() {
        String url = "";
        if (mParams.getType().equals("1")){
            url = "/api.php/home/index/index?token="+taskToken;
        }else if (mParams.getType().equals("2")){
            url = "/api.php/home/index/jd?token="+taskToken;
        }
        HttpClient.getInstance().post(url, "http://api.127yjs.com/")
                .params("from","1")
                .params("sec","2")
                .headers("Cookie",cookie)
                .headers("Content-Type", "application/json")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer","http://www.127yjs.com/portal/index/index.html")
                .headers("User-Agent", "Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.186 Mobile Safari/537.36 Html5Plus/1.0")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            if (TextUtils.isEmpty(response.body())) return;
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (obj.getInteger("code") == 1) {
                                sendLog(MyApp.getContext().getString(R.string.KSHG_AW));
                                receiveSuccess(String.format(MyApp.getContext().getString(R.string.KSHG_AW_tips), mPlatform.getName()), R.raw.youdanzuo, 3000);
                                addTask(mPlatform.getName());
                                updateStatus(mPlatform, Const.KSHG_AW); //接单成功的状态
                                isStart = false;
                            } else {
                                sendLog(obj.getString("msg"));  //继续检测任务
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
