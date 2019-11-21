package com.cainiao.base;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.HttpUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseFragment extends Fragment {

    private boolean update;

    private static final int PERMISSION_REQUEST_CODE = 201;
    private static final int PERMISSION_SETTING_CODE = 202;
    private List<String> mPermissionList = new ArrayList<>();


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
        if(Const.FLAG) checkUpdate();  //检查更新和通知
    }




    /**
     * 检查权限
     */
    protected void checkPermission(boolean update) {
        this.update = update;
        mPermissionList.clear();
        String[] permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        /** 判断哪些权限未授予，以便必要的时候重新申请*/
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        /**
         * 判断存储委授予权限的集合是否为空
         */
        if (mPermissionList.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //未授予的权限为空，表示都授予了
            getDeviceId();
        } else {
            permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 获取手机IMEI
     * @return
     */
    public void getDeviceId() {
        try {
            TelephonyManager manager = (TelephonyManager) MyApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
            String deviceId = (String) method.invoke(manager, 0);
            Utils.setDeviceId(deviceId);
//            LogUtil.e("deviceId：" + deviceId);
        } catch (Exception e) {
//            LogUtil.e("获取deviceId出错：" + e.toString());
        }finally {
            findUser();
        }
    }


    /**
     * 检查更新
     */
    protected void checkUpdate(){
        Const.FLAG = false;
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != PERMISSION_REQUEST_CODE) return;
        for (int i = 0; i < grantResults.length; i++) {  //读取IMEI权限授予回调
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                MyToast.error(getString(R.string.deviceId_not_allow));
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                getActivity().startActivityForResult(intent, PERMISSION_SETTING_CODE);
                return;
            }
        }
        getDeviceId();
    }

    public List<String> getPermissionList(){return mPermissionList;}

    protected void init(View view) {}
    public abstract int getLayoutResId();
    protected void activeSuccess(){}
    protected void findUserCallback(){}
}
