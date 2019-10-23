package com.cainiao.base;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    public void parseData(String data){ //格式： {"msg":"你的设备未激活App","code":"1","time":"0"}
        JSONObject object = JSONObject.parseObject(data);
        String code = object.getString("code");
        String time = object.getString("time");
        String msg = object.getString("msg");
        if(Utils.isInteger(time)) MyApp.setLog(Integer.parseInt(time));

        if(TextUtils.equals(code, "2")){  //冻结
            MyToast.error(msg);
        }
        findUserCallback();
    }


    protected void init(View view) {}
    public abstract int getLayoutResId();
    protected void activeSuccess(){}
    protected void findUserCallback(){}

}
