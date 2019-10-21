package com.cainiao.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.fragment.CommonFragment;
import com.cainiao.fragment.CountFragment;
import com.cainiao.fragment.HomeFragment;
import com.cainiao.fragment.MakeListFragment;
import com.cainiao.fragment.MineFragment;
import com.cainiao.service.KeepAliveService;
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.HttpUtil;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.BottomBar;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

public class MainActivity extends BaseActivity {

    private BottomBar bottomBar;
    private Intent mServiceIntent;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        initView();
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
        checkUpdate();
    }

    /**
     * 检查更新
     */
    protected void checkUpdate(){
        HttpUtil.checkUpdate(AppUtil.getVersionCode(this), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
//                LogUtil.e("response: " + response.body());
                if(TextUtils.isEmpty(response.body())) return;
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                if(!TextUtils.equals(jsonObject.getString("status"), "2")) return; //无更新
                boolean cancelable = TextUtils.equals(jsonObject.getString("force"), "1"); //force的值为1，表示非强制更新，非强制更新的话对话框可以消失
                String title = jsonObject.getString("msg");
                String msg = jsonObject.getString("body");
                String negText = "去蓝奏云下载";
                String posText = "在线更新";
                DialogUtil.get().showDoubleBtnAlertDialog(MainActivity.this, cancelable, title, msg, negText, posText,
                        new View.OnClickListener() {    //蓝奏云下载
                            @Override
                            public void onClick(View view) {
                                DialogUtil.get().closeAlertDialog();
                                AppUtil.openUrlInOuter(Const.OUTER_DOWNLOAD_URL);
                                if(!cancelable) finish();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {    //在线更新
                                DialogUtil.get().closeAlertDialog();
                                update(jsonObject.getString("url"), cancelable);
                            }
                        });
            }
        });
    }

    /**
     * 执行更新操作
     * @param originalUrl 初始的url
     * @param cancelable 是否可以取消更新
     */
    private void update(String originalUrl, boolean cancelable){
        String url = Utils.getTimestampUrl(originalUrl);
        if(TextUtils.isEmpty(url)) return;
        AppUtil.downloadApk(this, url, cancelable);
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
    }

    @Override
    public void onBackPressed() {   //屏蔽返回按钮
    }
}
