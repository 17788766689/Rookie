package com.cainiao.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.activity.MainActivity;
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.HttpUtil;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zinc.libpermission.annotation.Permission;
import com.zinc.libpermission.annotation.PermissionCanceled;
import com.zinc.libpermission.annotation.PermissionDenied;
import com.zinc.libpermission.bean.CancelInfo;
import com.zinc.libpermission.bean.DenyInfo;
import com.zinc.libpermission.utils.JPermissionUtil;

import java.lang.reflect.Method;

public abstract class BaseFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), null);
        init(view);
        return view;
    }

    /**
     * 激活设备
     * @param activationCode
     */
    protected void active(String activationCode){
        if(TextUtils.isEmpty(activationCode)){
            MyToast.error(getString(R.string.activation_code_empty));
            return;
        }
        HttpUtil.activation(activationCode, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if(TextUtils.isEmpty(response.body())) return;
                JSONObject object = JSONObject.parseObject(response.body());
                if(TextUtils.equals(object.getString("code"), "0")){    //激活成功
                    MyToast.success(object.getString("msg"));
                    activeSuccess();
                }else{
                    MyToast.error(object.getString("msg"));
                }
            }
        });
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

        findUserCallback();
        index();  //检查更新和通知

    }



    /**
     * 申请授权并处理“允许权限”的回调
     */
    @Permission(value={Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE })
    protected void checkPermission(){
        getDeviceId();
    }

    //用户取消授权
    @PermissionCanceled()
    private void cancel(CancelInfo cancelInfo) {
        MyToast.error(MyApp.getContext().getString(R.string.deviceId_goto_setting_power));
        //前往开启权限的界面
        JPermissionUtil.goToMenu(getActivity());
    }

    //用户拒绝授权并勾选了“不再提示”
    @PermissionDenied()
    private void deny(DenyInfo denyInfo) {
        MyToast.error(MyApp.getContext().getString(R.string.deviceId_goto_setting_power));
        //前往开启权限的界面
        JPermissionUtil.goToMenu(getActivity());
    }

    /**
     * 获取手机IMEI
     * @return
     */
    private void getDeviceId() {
        try {
            TelephonyManager manager = (TelephonyManager) MyApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
            String deviceId = (String) method.invoke(manager, 0);
            Utils.setDeviceId(deviceId);
        } catch (Exception e) {
//            LogUtil.e("获取deviceId出错：" + e.toString());
        }
        findUser();
    }


    /**
     * 检查更新
     */
    protected void index(){
        HttpUtil.index(AppUtil.getVersionCode(getActivity()), new StringCallback() {
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
                            DialogUtil.get().showNoticeDialog(getActivity(),jsonObject.getString("msg"));
                        }
                    });
                    return;
                } ; //无更新
                boolean cancelable = TextUtils.equals(jsonObject.getString("force"), "1"); //force的值为1，表示非强制更新，非强制更新的话对话框可以消失
                String title = jsonObject.getString("msg");
                String msg = jsonObject.getString("body");
                String negText = "去蓝奏云下载";
                String posText = "在线更新";
                DialogUtil.get().showDoubleBtnAlertDialog(getActivity(), cancelable, title, msg, negText, posText,
                        new View.OnClickListener() {    //蓝奏云下载
                            @Override
                            public void onClick(View view) {
                                DialogUtil.get().closeAlertDialog();
                                AppUtil.openUrlInOuter(Const.OUTER_DOWNLOAD_URL);
                                if(!cancelable) getActivity().finish();
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
                                        DialogUtil.get().showNoticeDialog(getActivity(),jsonObject.getString("msg"));
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
        AppUtil.downloadApk(getActivity(), url, cancelable);
    }

    protected void init(View view) {}
    public abstract int getLayoutResId();
    protected void activeSuccess(){}
    protected void findUserCallback(){}
}
