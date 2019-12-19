package com.cainiao.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.activity.WebActivity;
import com.cainiao.view.AlertDialog;
import com.cainiao.view.LoadDialog;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.w3c.dom.Text;

/**
 * Created by WJH on 2017/8/7.
 */

public class DialogUtil {

    private android.app.AlertDialog noticeDialog = null;
    private android.app.AlertDialog verifyDialog = null;
    private WebView mWebView;

    private DialogUtil() {
    }

    private static DialogUtil dialogUtil = null;
    private LoadDialog loadDialog = null;
    private AlertDialog alertDialog = null;

    /**
     * 实例化Dialog对象
     */
    public static DialogUtil get() {
        if (dialogUtil == null) {
            synchronized (DialogUtil.class) {
                if (dialogUtil == null) {
                    dialogUtil = new DialogUtil();
                }
            }
        }
        return dialogUtil;
    }

    /**
     * 显示进度对话框
     */
    public void showLoadDialog(Context context, String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadDialog(context, 170, 170, R.layout.layout_loaddialog, R.style.LoadDialogStyle);
        }
        loadDialog.setMsg(msg.replace("\\n", "\n"));
        if (!loadDialog.isShowing() && !((Activity) context).isFinishing()) {
            loadDialog.setCancelable(false);
            loadDialog.show();
        }
    }

    /**
     * 关闭进度对话框
     */
    public void closeLoadDialog() {
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.cancel();
        }
        loadDialog = null;
    }

    /**
     * 显示提示对话框
     */
    public void showAlertDialog(Context context, String title, String msg, String btnMsg, View.OnClickListener callback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle(title)
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(false)
                    .setPositiveButton(btnMsg, callback);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }

    }

    /**
     * 显示提示对话框
     */
    public void showDoubleBtnAlertDialog(Context context, String msg, View.OnClickListener callback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle("提示")
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.confirm), callback)
                    .setNegativeButton(context.getString(R.string.cancel), null);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }
    }


    /**
     * 显示提示对话框
     */
    public void showDoubleBtnAlertDialog(Context context, boolean cancelable, String title, String msg, String negText, String posText, View.OnClickListener negCallback, View.OnClickListener posCallback,DialogInterface.OnCancelListener nullCallback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle(title)
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(cancelable)
                    .setPositiveButton(posText, posCallback)
                    .setNegativeButton(negText, negCallback)
                    .setCancelListener(nullCallback);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }
    }


    /**
     * 显示输入框对话框
     */
    public void showInputAlertDialog(Context context, String msg, String hint, String positiveStr, View.OnClickListener callback) {
        alertDialog = new AlertDialog(context).builder();
        alertDialog.setTitle("提示")
                .setMsg(msg.replace("\\n", "\n"))
                .setHint(hint)
                .setCancelable(false)
                .setPositiveButton(positiveStr, callback)
                .setNegativeButton(context.getString(R.string.cancel), null);
        alertDialog.show();
    }

    /**
     * 获取输入框内容
     *
     * @return
     */
    public String getInputStr() {
        return alertDialog == null ? "" : alertDialog.getInput();
    }


    /**
     * 关闭提示的对话框
     */
    public void closeAlertDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }

    /**
     * 显示进度对话框
     */
    public void showUpdateDialog(Context context, String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadDialog(context, 170, 170, R.layout.layout_update_dialog, R.style.LoadDialogStyle);
        }
        loadDialog.setMsg(msg.replace("\\n", "\n"));
        if (!loadDialog.isShowing() && !((Activity) context).isFinishing()) {
            loadDialog.setCancelable(false);
            loadDialog.show();
        }
    }

    /**
     * 显示公告对话框
     */
    public void showNoticeDialog(Context context, String msg) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_notice, null);
        TextView tvContent = view.findViewById(R.id.tv_content);
        TextView tvClose = view.findViewById(R.id.tv_close);
        tvContent.setText(Html.fromHtml(msg));
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noticeDialog != null) noticeDialog.cancel();
                noticeDialog = null;
            }
        });
        noticeDialog = new android.app.AlertDialog.Builder(context).setView(view).setCancelable(false).create();
        noticeDialog.show();
    }


    /**
     * 显示滑块验证码对话框
     */
    public void showVerifyDialog(Context context, String pkgName, VerifyCallback callback) {
        this.mCallback = callback;
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_verify, null);
        mWebView = view.findViewById(R.id.web_view);
        verifyDialog = new android.app.AlertDialog.Builder(context).setView(view).setCancelable(true).create();
        if("io.dcloud.UNIE9BC8DE".equals(pkgName)){// 918人气王获取token
            getToken("http://www.zhyichao.com");
        }else if ("io.dcloud.UNI89500DB".equals(pkgName)){// 欢乐购
            getToken("http://app.biaoqiandan.com");
        }else if("io.dcloud.UNIE7AC320".equals(pkgName)){ // 芒果叮咚
            getToken("http://xiaomangguo.zhyichao.com");
        }else if ("io.dcloud.UNI55AAAAA".equals(pkgName)){ // 淘抢单
            getToken("http://www.51qiangdanwang.com");
        }else if("io.dcloud.UNIB205D0A".equals(pkgName)){ // 小苹果(拼多多)
            getToken("http://www.91xiaopingguo.com");
        }

    }

    /**
     * 显示webview登录接单的对话框
     * @param context
     * @param url
     */
    public void showWebReceiptDialog(Context context, String url, LoginCallback mLoginCallback){
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_web_receipt, null);
        mWebView = view.findViewById(R.id.web_view);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.requestFocus(View.FOCUS_DOWN);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager manager = CookieManager.getInstance();
                String cookie = manager.getCookie(url);
                LogUtil.e("cookie: " + cookie);
                super.onPageFinished(view, url);
                if(!TextUtils.isEmpty(cookie) && cookie.contains("user=")){ //cookie里包含user_id，则说明已经登录，此时关闭对话框
//                   verifyDialog.cancel();
//                   verifyDialog = null;
                    if(mLoginCallback != null) mLoginCallback.onSuccess(cookie);
                    MyToast.info("如果已经登录直接按返回键关闭弹窗就可以了");
                }
            }
        });
        verifyDialog = new android.app.AlertDialog.Builder(context).setView(view).setCancelable(true).create();
        verifyDialog.show();

    }


    public void setDownloadProgress(int progress) {
        if (loadDialog != null) loadDialog.setProgress(progress);
    }


    /**
     * 918人气王系列平台
     */
    private void getToken(String url){
        HttpClient.getInstance().get("", url+"/api/index/getToken")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(TextUtils.isEmpty(response.body())) return;
                        JSONObject obj = JSONObject.parseObject(response.body());
                        JSONObject dataObj = obj.getJSONObject("data");
                        if(TextUtils.equals(dataObj.getJSONObject("data").getString("token"), "1") || TextUtils.equals(dataObj.getJSONObject("data").getString("token"), "123456789")){
                            if(mCallback != null) mCallback.onSuccess("1", "");
                        }else{
                            loadVerifyCode(JSONObject.toJSONString(dataObj));
                            verifyDialog.show();
                        }
                    }
                });
    }

    /**
     * 加载滑块验证码
     * @param paramJson
     */
    private void loadVerifyCode(String paramJson){  //918人气王登录新增两个字段：token和verifyid
        String url = "http://platform.ckzs.online/verify/v5/index.html";
        JSInterface jSInterface = new JSInterface(paramJson);
        mWebView.addJavascriptInterface(jSInterface, "android");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl(url);
    }


    class JSInterface{

        private String paramJson;
        private String token;

        public JSInterface(String paramJson){
            this.paramJson = paramJson;
            JSONObject obj = JSONObject.parseObject(paramJson);
            token = obj.getJSONObject("data").getString("token");
        }

        @JavascriptInterface
        public String getV5VerifyParamJson(){
            return paramJson;
        }

        @JavascriptInterface
        public void setV5VerifyResultJson(String verifyId) {
            if(!TextUtils.equals(verifyId, "1") && !TextUtils.equals(verifyId, "0")){ //1时为点击图片上关闭的按钮，0时为点击“确认”按钮
                if(mCallback != null) mCallback.onSuccess(token, verifyId);
            }
            verifyDialog.cancel();
            verifyDialog = null;
        }
    }

    private VerifyCallback mCallback;
    public interface VerifyCallback{
        void onSuccess(String token, String verifyId);
    }

    private LoginCallback mLoginCallback;
    public interface LoginCallback{
        void onSuccess(String cookie);
    }
}
