package com.cainiao.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.base.MyApp;
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
import com.cainiao.view.toasty.MyToast;
import com.cainiao.view.toasty.Toasty;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.List;


public class MainActivity extends BaseActivity {

    private BottomBar bottomBar;
    private Intent mServiceIntent;
    private UpdateStatusReceiver mReceiver;

    private String deviceId;
    private static final int REQUEST_PERMISSION_SETTING = 202;
    private static final int REQUEST_PERMISSION_WRITE_EXSTORAGE = 203;


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
        checkPermission(Manifest.permission.READ_PHONE_STATE);
    }


    /**
     * 检查设备是否已经激活
     */
    public void findUser(){
        HttpUtil.findUser(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                parseData(response.body());
            }
        });
    }

    /**
     * 解析从服务器返回的数据
     * @param data
     */
    private void parseData(String data){ //格式： {"msg":"你的设备未激活App","code":"1","time":"0"}
        JSONObject object = JSONObject.parseObject(data);
        String code = object.getString("code");
        String time = object.getString("time");
        String msg = object.getString("msg");
        if(Utils.isInteger(time)) MyApp.setLog(Integer.parseInt(time));

        if(TextUtils.equals(code, "2")){  //冻结
            MyToast.error(msg);
        }

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

//        findUserCallback();
    }

    /**
     * 检查是否授予权限
     */
    protected void checkPermission(String permission){
        int requestCode = 0;
        if(TextUtils.equals(permission, Manifest.permission.READ_PHONE_STATE)){
            requestCode = REQUEST_PERMISSION_SETTING;
        }else if(TextUtils.equals(permission, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            requestCode = REQUEST_PERMISSION_WRITE_EXSTORAGE;
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){ //6.0以下的设备或是6.0以上的已授权，直接获取
            if(requestCode == REQUEST_PERMISSION_SETTING){ //获取IMEI
//                LogUtil.e("getDeviceId.............");
                getDeviceId();
            }else{  //下载更新包
//                LogUtil.e("index.............");
                index();
            }
        }else{ //未授权，则申请授权
//            LogUtil.e("申请授权............." + requestCode);
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    /**
     * 获取手机IMEI
     * @return
     */
    @SuppressLint("MissingPermission")
    private void getDeviceId() {
        try {
            TelephonyManager manager = (TelephonyManager) MyApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
            deviceId = (String) method.invoke(manager, 0);
            Utils.setDeviceId(deviceId);
            findUser();
        } catch (Exception e) {
//            LogUtil.e("获取deviceId出错：" + e.toString());
        }
//        LogUtil.e("deviceId: " + deviceId);
    }


    /**
     * 检查更新
     */
    protected void index(){
        HttpUtil.index(AppUtil.getVersionCode(this), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if(TextUtils.isEmpty(response.body())) return;
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                if(!TextUtils.equals(jsonObject.getString("status"), "2")){
                    HttpUtil.message( new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if(TextUtils.isEmpty(response.body())) return;
                            JSONObject jsonObject = JSONObject.parseObject(response.body());
                            DialogUtil.get().showNoticeDialog(MainActivity.this,jsonObject.getString("msg"));
                        }
                    });
                    return;
                } ; //无更新
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
                        },new DialogInterface.OnCancelListener(){
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                HttpUtil.message( new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        if(TextUtils.isEmpty(response.body())) return;
                                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                                        DialogUtil.get().showNoticeDialog(MainActivity.this,jsonObject.getString("msg"));
                                    }
                                });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        LogUtil.e("onRequestPermissionsResult....." + requestCode);
        if(requestCode != REQUEST_PERMISSION_SETTING && requestCode != REQUEST_PERMISSION_WRITE_EXSTORAGE) return;  //如果不是读取设备码的requestCode，则不进行处理
        String permission = "";

        if(requestCode == REQUEST_PERMISSION_SETTING){
            permission = Manifest.permission.READ_PHONE_STATE;
        }else{
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }

        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
//        LogUtil.e("showRequestPermission....." + showRequestPermission);
        if (showRequestPermission){
            MyToast.error(MyApp.getContext().getString(R.string.deviceId_goto_setting_power));
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, requestCode);
        }else if(requestCode == REQUEST_PERMISSION_SETTING){ //获取deviceId
            getDeviceId();
        }else{
            index();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != REQUEST_PERMISSION_SETTING && requestCode != REQUEST_PERMISSION_WRITE_EXSTORAGE) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            MyToast.error(MyApp.getContext().getString(R.string.deviceId_not_allow));
            //用户不授权，直接退出App
            finish();
            System.exit(-1);
        }else if(requestCode == REQUEST_PERMISSION_SETTING){
            getDeviceId();
        }else{
            index();
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
}
