package com.cainiao.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cainiao.bean.Platform;
import com.cainiao.util.Platforms;

public class MyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Platform platform = Platforms.getCurrPlatform();
        if(platform == null || platform.getAction() == null) return START_STICKY;

        if(platform.isStart()){
            try {
                platform.getAction().start(platform);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            platform.getAction().stop();
        }

        return START_STICKY;
    }
}
