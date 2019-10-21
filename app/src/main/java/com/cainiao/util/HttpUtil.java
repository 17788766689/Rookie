package com.cainiao.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.cainiao.R;
import com.cainiao.base.MyApp;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.callback.ProgressCallback;

/**
 * Created by 123 on 2019/9/23.
 */

public class HttpUtil {

    /**
     * 初始化
     */
    public static void init() {
        HttpClient.getInstance().init();
    }

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }


    /**
     * 查询接单统计
     */
    public static void selectTotal(String selectName, StringCallback callback) {
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            return;
        }
        HttpClient.getInstance().get(Const.SELECT_TOTAL, null)
                .params("taskName", selectName)
                .execute(callback);

    }


    /**
     * 查询做单列表
     */
    public static void selectUrl(String selectName, StringCallback callback) {
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            return;
        }
        HttpClient.getInstance().get(Const.SELECT_URL, null)
                .params("taskName", selectName)
                .execute(callback);
    }

    /**
     * 根据uuid查询设备是否激活
     */
    public static void findUser(StringCallback callback) {
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            return;
        }
        HttpClient.getInstance().get(Const.FIND_USER_URL, Const.BASE_USER_URL)
                .params("uuid", Utils.getUuid())
                .execute(callback);
    }

    /**
     * 激活设备
     * @param activationCode
     * @param callback
     */
    public static void activation(String activationCode, StringCallback callback) {
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            return;
        }
        HttpClient.getInstance().get(Const.ACTIVATION_URL, Const.BASE_USER_URL)
                .params("uuid", Utils.getUuid())
                .params("activationCode", activationCode)
                .params("type", "2")
                .execute(callback);
    }


    /**
     * 检查更新
     * @param versionCode
     * @param callback
     */
    public static void checkUpdate(int versionCode, StringCallback callback) {
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            return;
        }
        HttpClient.getInstance().get(Const.UPDATE_URL, null)
                .params("ver", versionCode)
                .execute(callback);
    }


    /*
    * 判断设备 是否使用代理上网，方法名称叫available是为了防止别人分析出有代理检测
    * */
    public static boolean available(Context context) {
        String proxyAddress;
        int proxyPort;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return false;
        //return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 判断网络是否连接
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isNetworkAvailable() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo() {
        return ((ConnectivityManager) MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }


    /**断点下载文件*/
    public static void download(Activity aty, String url, String saveFileDir, String saveFileName, ProgressCallback callback){
        if(!isNetworkAvailable() || available(MyApp.getContext())){
            MyToast.error(MyApp.getContext().getString(R.string.network_unavailable));
            DialogUtil.get().closeLoadDialog();
            return;
        }
        DownloadFileInfo fileInfo = new DownloadFileInfo(url,saveFileDir,saveFileName, callback);
        HttpInfo info = HttpInfo.Builder().addDownloadFile(fileInfo).build();
        OkHttpUtil.getDefault(aty).doDownloadFileAsync(info);
//        OkHttpUtil.Builder().setReadTimeout(120).build().doDownloadFileAsync(info);
    }

}
