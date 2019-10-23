package com.cainiao.activity;

import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.base.BaseActivity;

public class WebActivity extends BaseActivity {

    private WebView mWebView;
    private String name;
    private String url;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_web;
    }

    @Override
    protected void init() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("name");
        if(TextUtils.isEmpty(url)) return;

        if(!TextUtils.isEmpty(name)){
            ((TextView)findViewById(R.id.tv_title)).setText(String.format(getString(R.string.web_title), name));
        }

        mWebView = findViewById(R.id.web_view);
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
        });
    }
}
