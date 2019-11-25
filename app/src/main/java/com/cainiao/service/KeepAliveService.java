package com.cainiao.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.cainiao.R;
import com.cainiao.activity.MainActivity;
import com.cainiao.base.BaseAction;
import com.cainiao.bean.Platform;
import com.cainiao.util.LogUtil;
import com.cainiao.util.NotifyUtil;


/**
 * 防止后台杀死进程服务
 * Created by 123 on 2019/9/23.
 */

public class KeepAliveService extends Service{
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PowerManager.WakeLock wakelock;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = NotifyUtil.getNotification(this, getString(R.string.app_name) + "正在运行", "点击返回" + getString(R.string.app_name), "101", "cainiao101");
        startForeground(101, notification);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(mAudioFocusChange, 3, 1);
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
        startPlayMusic();
        if(wakelock == null){
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CaiNiaoWakelockTag");
            wakelock.acquire();
        }
        return START_STICKY;
    }

    private void startPlayMusic() {
        MediaPlayer mediaPlayer = mMediaPlayer;
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
//            LogUtil.e("启动后台播放音乐");
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        MediaPlayer mediaPlayer = mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }


    private AudioManager.OnAudioFocusChangeListener mAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int i) {
            if (i == -2) {
                return;
            }
            if (i == -1) {
                mAudioManager.abandonAudioFocus(mAudioFocusChange);
            } else if (i == 1) {
                try {
                    startPlayMusic();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        if(wakelock != null){
            wakelock.release();
            wakelock = null;
        }
    }
}
