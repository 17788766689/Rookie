package com.cainiao.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.base.BaseFragment;
import com.cainiao.fragment.CommonFragment;
import com.cainiao.fragment.CountFragment;
import com.cainiao.fragment.HomeFragment;
import com.cainiao.fragment.MakeListFragment;
import com.cainiao.fragment.MineFragment;
import com.cainiao.service.KeepAliveService;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.view.BottomBar;
import com.cainiao.view.toasty.MyToast;

import java.util.List;


public class MainActivity extends BaseActivity {

    private BottomBar bottomBar;
    private Intent mServiceIntent;
    private UpdateStatusReceiver mReceiver;
    private static final int PERMISSION_SETTING_CODE = 202;

    class UpdateStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(bottomBar == null) return;
            List<Fragment> fragments = bottomBar.getFragments();
            for(Fragment fragment : fragments){
                if(fragment instanceof HomeFragment || fragment instanceof CommonFragment){ //更新首页和常用的平台的状态
                    fragment.onResume();
                }
            }
        }
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        initView();
        mReceiver = new UpdateStatusReceiver();
        IntentFilter filter = new IntentFilter(Const.STATUS_ACTION);
        registerReceiver(mReceiver,filter);
    }

    /**
     * 初始化控件
     */
    private void initView(){
        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.fl_container)
                .setTitleBeforeAndAfterColor("#999999", "#037BFF")
                .addItem(HomeFragment.class,
                        "首页",
                        R.mipmap.ic_tab_home_normal,
                        R.mipmap.ic_tab_home_selected)
                .addItem(CommonFragment.class,
                        "常用",
                        R.mipmap.ic_tab_common_normal,
                        R.mipmap.ic_tab_common_selected)
                .addItem(MakeListFragment.class,
                        "做单",
                        R.mipmap.ic_tab_makelist_normal,
                        R.mipmap.ic_tab_makelist_selected)
                .addItem(CountFragment.class,
                        "统计",
                        R.mipmap.ic_tab_count_normal,
                        R.mipmap.ic_tab_count_selected)
                .addItem(MineFragment.class,
                        "我的",
                        R.mipmap.ic_tab_mine_normal,
                        R.mipmap.ic_tab_mine_selected)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mServiceIntent != null){
            stopService(mServiceIntent);
            mServiceIntent = null;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceIntent == null){
            mServiceIntent = new Intent(this, KeepAliveService.class);
            startService(mServiceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogUtil.get().closeAlertDialog();
        DialogUtil.get().closeLoadDialog();
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {   //屏蔽返回按钮
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(bottomBar.getCurrentFragment() == null || requestCode != PERMISSION_SETTING_CODE) return;
        BaseFragment fragment = (BaseFragment) bottomBar.getCurrentFragment();
        for (String permission : fragment.getPermissionList()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) { //还有未申请的权限
                MyToast.error(getString(R.string.deviceId_not_allow));
                finish();
                return;
            }
        }
        fragment.getDeviceId();
    }
}
