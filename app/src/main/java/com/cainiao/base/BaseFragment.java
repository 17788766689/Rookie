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
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.HttpUtil;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

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

    protected void init(View view) {}
    public abstract int getLayoutResId();
    protected void activeSuccess(){}
}
