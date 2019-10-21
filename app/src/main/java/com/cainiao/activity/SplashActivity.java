package com.cainiao.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.WindowManager;

import com.cainiao.R;
import com.cainiao.base.BaseActivity;

/**
 * Created by 123 on 2019/9/25.
 */

public class SplashActivity extends BaseActivity{
    @Override
    public int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
