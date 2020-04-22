package com.cainiao.util;

import android.content.Context;
import android.text.TextUtils;

import com.cainiao.base.MyApp;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by 123 on 2019/9/23.
 */

public class HttpClient {

  private static final long TIMEOUT = 10000;
  private static HttpClient sInstance;
  private OkHttpClient mOkHttpClient;
  private String mLanguage;//语言
  private String mUrl;

  private HttpClient() {
    mUrl = Const.BASE_APP_URL;
  }

  public static HttpClient getInstance() {
    if (sInstance == null) {
      synchronized (HttpClient.class) {
        if (sInstance == null) {
          sInstance = new HttpClient();
        }
      }
    }
    return sInstance;
  }

  public void init() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
    loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
    loggingInterceptor.setColorLevel(Level.INFO);
    builder.addInterceptor(loggingInterceptor);

    builder.readTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
    builder.writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
    builder.connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS);

    HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
    builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
    builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
    mOkHttpClient = builder.build();
  }

  public GetRequest get(String serviceName, String baseUrl) {
    if(TextUtils.isEmpty(baseUrl)) baseUrl = mUrl;
    return OkGo.get(baseUrl + serviceName)
            .headers("Connection","keep-alive")
            .tag(serviceName)
            .params("language", mLanguage);
  }

  public PostRequest post(String serviceName, String baseUrl) {
    if(TextUtils.isEmpty(baseUrl)) baseUrl = mUrl;
    return OkGo.post(baseUrl + serviceName)
            .headers("Connection","keep-alive")
            .tag(serviceName)
            .params("language", mLanguage);
  }

  public void cancel(String tag) {
    OkGo.cancelTag(mOkHttpClient, tag);
  }

  public void setLanguage(String language) {
    mLanguage = language;
  }

}