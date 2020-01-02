package com.cainiao.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.base.BaseFragment;
import com.cainiao.bean.Platform;
import com.cainiao.fragment.CommonFragment;
import com.cainiao.fragment.CountFragment;
import com.cainiao.fragment.HomeFragment;
import com.cainiao.fragment.MakeListFragment;
import com.cainiao.fragment.MineFragment;
import com.cainiao.service.KeepAliveService;
import com.cainiao.util.Const;
import com.cainiao.util.DbUtil;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.Platforms;
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
        updateCommonPlatforms();
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

    /**
     * 启动app时更新常用平台的信息，（与首页的信息同步）
     */
    private void updateCommonPlatforms(){
        List<Platform> latestPlatforms = Platforms.getLatestPlaforms();
        List<Platform> allPlatforms = Platforms.getPlatforms();
        for(Platform latestPlatform : latestPlatforms){ //遍历常用的平台
            for(Platform allPlatform : allPlatforms){ //遍历所有的平台
                if(TextUtils.equals(latestPlatform.getPkgName(), allPlatform.getPkgName())){
                    latestPlatform.setName(allPlatform.getName());  //更新平台名称
                    latestPlatform.setResId(allPlatform.getResId());  //更新平台图标
                    latestPlatform.setStatus(allPlatform.getStatus());  //更新平台状态（限时免费、永久免费或空闲中）
                    //TODO 以后有其他信息更新也需要在这里往下写
                    DbUtil.update(latestPlatform); //全部要更新的信息在内存中更新完之后要更新到数据库
                }
            }
        }

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
