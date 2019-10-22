package com.cainiao.base;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.cainiao.R;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.util.HttpClient;
import com.cainiao.util.LogUtil;
import com.cainiao.util.NotifyUtil;
import com.cainiao.util.Platforms;
import com.cainiao.view.toasty.MyToast;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.io.Serializable;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class BaseAction implements Serializable {


    public void start(Platform platform) throws Exception {}
    public void stop(){
        MyToast.warn(MyApp.getContext().getString(R.string.receipt_stop));
        sendLog(MyApp.getContext().getString(R.string.receipt_stop));
    }

    public void getVerifyCode(Platform platform){}

    /**
     * 发送日志到界面，如果是其他平台，则只更新日志内容
     * @param log
     */
    protected void sendLog(String log){
        List<Platform> list = Platforms.getPlatforms();
        Platform mPlatform = Platforms.getCurrPlatform();

        if(mPlatform != null && mPlatform.getAction() != null && this.getClass().equals(mPlatform.getAction().getClass())){  //当前平台的日志才会发送广播通知更新
            sendMsg("log", log);
        }else{
            List<Platform> mRunnings = Platforms.getRunningPlaforms();
            for(Platform platform : mRunnings){
                if(this.getClass().equals(platform.getAction().getClass())){
                    platform.setLog(platform.getLog() + LogUtil.formatLog(log) + "\n");
                    int position = list.indexOf(platform);
                    list.set(position, platform);
                    Platforms.setPlatforms(list);
                    break;
                }
            }
        }

    }

    /**
     * 发送买号的信息到页面，进行更新
     * @param jsonStr
     */
    protected void showBuyerNum(String jsonStr){
        sendMsg("showBuyerNum", jsonStr);
    }

    /**
     * 发送广播，把消息传递到Activity
     * @param flag
     * @param msg
     */
    protected void sendMsg(String flag, String msg){
        Intent intent = new Intent(Const.UPDATE_ACTION);
        intent.putExtra("flag", flag);
        intent.putExtra("msg", msg);
        MyApp.getContext().sendBroadcast(intent);
    }

    /**
     * 接单成功执行逻辑
     * @param content   通知栏显示的内容
     * @param voiceResId    语音提示的资源id
     * @param milliseconds  震动持续时长
     */
    protected void receiveSuccess(String content, int voiceResId, long milliseconds){
        //显示通知
        NotificationManager manager = (NotificationManager) MyApp.getContext().getSystemService(NOTIFICATION_SERVICE);
        Notification notification = NotifyUtil.getNotification(MyApp.getContext(), "菜鸟接单成功", content, "100", "cainiao100");
        manager.notify(100,notification);
        //播放语音
        MediaPlayer player = MediaPlayer.create(MyApp.getContext(), voiceResId);
        player.start();
        //震动
        Vibrator vib = (Vibrator) MyApp.getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * 把当前任务添加到后台，做统计
     */
    protected void addTask(String taskName){
        HttpClient.getInstance().post("addTask", "http://www.cainiaoqd.com/app/")
                .params("taskName", taskName)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                    }
                });
    }

    /**
     * 更新常用的平台
     */
    protected void updatePlatform(Platform mPlatform){
        List<Platform> platforms = MyApp.getLiteOrm().query(new QueryBuilder<>(Platform.class).whereEquals("pkgName", mPlatform.getPkgName()));
        if(platforms.size() > 0){   //原来存在，则更新
            Platform platform = platforms.get(0);
            platform.setLastTime(System.currentTimeMillis());
            MyApp.getLiteOrm().update(platform);
        }else{  //不存在，则添加
            mPlatform.setLastTime(System.currentTimeMillis());
            MyApp.getLiteOrm().save(mPlatform);
        }
    }

    /**
     * 更新参数到数据库
     * @param mPlatform
     */
    protected void updateParams(Platform mPlatform){
        List<Params> paramsList = MyApp.getLiteOrm().query(new QueryBuilder<>(Params.class).whereEquals("pkgName", mPlatform.getPkgName()));
        Params params = mPlatform.getParams();
        if(paramsList.size() > 0){   //原来存在，则更新
            Params p = paramsList.get(0);
            p.setMinFrequency(params.getMinFrequency());
            p.setMaxFrequency(params.getMaxFrequency());
            p.setAccount(params.getAccount());
            p.setPassword(params.getPassword());
            p.setMinCommission(params.getMinCommission());
            p.setMaxPrincipal(params.getMaxPrincipal());
            MyApp.getLiteOrm().update(p);
        }else{  //不存在，则添加
            params.setPkgName(mPlatform.getPkgName());
            MyApp.getLiteOrm().save(params);
        }

        updateStatus(mPlatform, Const.RECEIPTING); //接单中的状态
    }

    /**
     * 更新平台的状态
     * @param platform
     * @param status
     */
    protected void updateStatus(Platform platform, int status){
        if(platform == null) return;
        if(status < 0) status = platform.getOriginalStatus();   //传递过来的status为负数，则还原初始状态

        if (status == Const.RECEIPT_SUCCESS){//接单成功
            platform.setStart(false);
        }
        //更新首页的状态
        List<Platform> mList = Platforms.getPlatforms();
        int position = mList.indexOf(platform);
        if(position < 0) return;
        platform.setStatus(status);
        mList.set(position, platform);
        Platforms.setPlatforms(mList);

        //更新常用的状态
        mList = MyApp.getLiteOrm().query(new QueryBuilder<>(Platform.class).whereEquals("pkgName", platform.getPkgName()));
        if(mList == null || mList.size() <= 0) return;
        Platform p = mList.get(0);
        p.setStatus(status);
        MyApp.getLiteOrm().update(p);
    }

}
