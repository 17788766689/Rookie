package com.cainiao.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.cainiao.bean.Platform;
import com.cainiao.util.Platforms;

import java.util.List;

/**
 * Created by 123 on 2019/9/25.
 */

public abstract class BaseActivity extends Activity {

    protected Platform mPlatform;
    protected List<Platform> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(getLayoutResId());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        init();
    }


    /**
     * 获取当前的平台
     */
    protected void getCurrPlatform(int position){
        if(mList == null) mList = Platforms.getPlatforms();
        if(position >= 0 && position < mList.size()){
            mPlatform = mList.get(position);
        }else{
            mPlatform = Platforms.getCurrPlatform();
        }
    }

    /**
     * 更新当前的平台，一般是更新平台里的数据
     * @param platform
     */
    protected void setCurrPlatform(int position, Platform platform){
        if(mList == null) mList = Platforms.getPlatforms();
        if(position >= 0 && position < mList.size()){
            mList.set(position, platform);
        }
        Platforms.setCurrPlatform(platform);
    }

    protected void init() {}

    public abstract int getLayoutResId();

}
