package com.cainiao.activity;

import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.util.LogUtil;
import com.cainiao.view.toasty.MyToast;

public class WebReceiptActivity extends BaseActivity {

    private WebView mWebView;
    private String name;
    private String url;
    private int position;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_web_receipt;
    }

    @Override
    protected void init() {
        url = getIntent().getStringExtra("url");
        position = getIntent().getIntExtra("position", 0);

        if(TextUtils.isEmpty(url)) return;

        getCurrPlatform(position);

        mWebView = findViewById(R.id.web_view);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        //WebView加载web资源
        mWebView.loadUrl(url);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager manager = CookieManager.getInstance();
                String cookie = manager.getCookie(url);
//                LogUtil.e("cookie: " + cookie);
                super.onPageFinished(view, url);
                if(!TextUtils.isEmpty(cookie) && (cookie.contains("user=") || cookie.contains("sessionid") || cookie.contains("home_user_name") || cookie.contains("ASP.NET_SessionId"))){ //cookie里包含user_id，则说明已经登录，此时关闭对话框
                    mPlatform.setCookie(cookie);
                    MyToast.info("如果已经登录直接按返回键关闭弹窗就可以了");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        setCurrPlatform(position, mPlatform);
        setResult(RESULT_OK);
        super.onDestroy();
    }
}
