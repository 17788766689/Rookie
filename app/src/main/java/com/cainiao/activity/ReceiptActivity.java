package com.cainiao.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cainiao.R;
import com.cainiao.base.BaseActivity;
import com.cainiao.base.MyApp;
import com.cainiao.bean.BuyerNum;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.cainiao.service.MyService;
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.HttpUtil;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Platforms;
import com.cainiao.util.SPUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.lzy.okgo.callback.BitmapCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.Headers;

/**
 * Created by 123 on 2019/10/7.
 */

public class ReceiptActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView inVerifyCode;
    private Platform mPlatform;
    private List<Platform> mList;
    private int position;
    private TextView tvLog, tvStart, tvStop, tvBtn1, tvBtn2;
    private EditText etMinFreq, etMaxFreq, etAccount, etPwd, etMinComm, etMaxPrinc, etReceiptUrl, etVerifyCode, etSmsCode;
    private Spinner spBuyerNum, spReceiptType, spAccountType;
    private CheckBox cb1, cb2;
    private LinearLayout llAccount, llPwd, llReceiptUrl, llBuyerNum, llComm, llReceiptType, llVerifyCode, llSmsCode, llAccountType, llCheckbox, llIgnore;

    private List<BuyerNum> mBuyerNums;
    private List<String> names;
    private ArrayAdapter<String> mAdapter;
    private String buyerNumStr;
    private UpdateReceiver mReceiver;
    private Random mRandom;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_receipt;
    }

    @Override
    protected void init() {
        mList = Platforms.getPlatforms();
        mPlatform = Platforms.getCurrPlatform();
        mRandom = new Random();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);

        initView();
        initData();
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvBtn1.setOnClickListener(this);
        tvBtn2.setOnClickListener(this);
        spBuyerNum.setOnItemSelectedListener(this);
        inVerifyCode.setOnClickListener(this);
//        updateBtnStatus();

        String log = mPlatform.getLog();
        if(!TextUtils.isEmpty(log)) refreshLogView(log, false);

        mReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter(Const.UPDATE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 更新日志
     * @param msg
     */
    private void refreshLogView(String msg, boolean needNewLine){
        if(TextUtils.isEmpty(msg)) return;
        if(tvLog.getLineCount() >= Const.LOG_MAX_LINE){
            tvLog.setText("");  //清空日志
            mPlatform.setLog("");
            Platforms.setCurrPlatform(mPlatform);
        }
        /*msg = msg+="\n"+tvLog.getText().toString();
        tvLog.setText(msg);*/
        tvLog.append(msg);
        if(needNewLine) tvLog.append("\n");
        int offset = tvLog.getLineCount() * tvLog.getLineHeight();
        if(offset > tvLog.getHeight()){
            //tvLog.scrollTo(0,Utils.dp2px(10)-20);
            tvLog.scrollTo(0,offset - tvLog.getHeight() + Utils.dp2px(10));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start:
                if(!getParams()) return;
                mPlatform.setStart(true);
                mPlatform.setRefreshVerifyCode(false);
                Platforms.addRunningPlatform(mPlatform);
                Platforms.setCurrPlatform(mPlatform);
                startService(new Intent(this, MyService.class));
                break;
            case R.id.tv_stop:
                mPlatform.setStart(false);
                mPlatform.setRefreshVerifyCode(false);
                Platforms.rmRunningPlatform(mPlatform);
                Platforms.setCurrPlatform(mPlatform);
                startService(new Intent(this, MyService.class));
                break;
            case R.id.tv_btn1:
                btn1Click();
                break;
            case R.id.tv_btn2:
                btn2Click();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_verify_code:
                //getVerifyCode(mPlatform.getVerifyCodeUrl());
                mPlatform.setRefreshVerifyCode(true);
                Platforms.setCurrPlatform(mPlatform);
                Platforms.rmRunningPlatform(mPlatform);
                startService(new Intent(this,MyService.class));
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> view, View view1, int i, long l) {
        mPlatform = Platforms.getCurrPlatform();
        if(mBuyerNums == null || mBuyerNums.size() <= i || mPlatform.getParams() == null || !mPlatform.isStart()) return;
        //选择新的买号之后通知Service，使用新的买号进行刷单
        BuyerNum buyerNum = mBuyerNums.get(i);
        Params params = mPlatform.getParams();
        params.setBuyerNum(buyerNum);
        params.setBuyerNumIndex(i);
        mPlatform.setParams(params);
        refreshLogView("接单买号切换为"+ params.getBuyerNum().getName(), true);
        Platforms.setCurrPlatform(mPlatform);
        startService(new Intent(this, MyService.class));
    }

    @Override
    public void onNothingSelected(AdapterView<?> view) {
    }


    /**
     * 获取页面上用户输入的参数
     */
    private boolean getParams(){
        String minFreq = etMinFreq.getText().toString().trim();
        String maxFreq = etMaxFreq.getText().toString().trim();
        String account = etAccount.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String minComm = etMinComm.getText().toString().trim();
        String maxPrinc = etMaxPrinc.getText().toString().trim();
        String receiptUrl = etReceiptUrl.getText().toString().trim();
        String verifyCode = etVerifyCode.getText().toString().trim();
        String smsCode = etSmsCode.getText().toString().trim();

        if(!Utils.isInteger(minFreq)) minFreq = "1000";  //最小频率默认给1000ms
        if(!Utils.isInteger(maxFreq)) maxFreq = "9999"; //最大频率默认给9999ms
        if(!Utils.isInteger(minComm)) minComm = "1";    //最小佣金默认给0元
        if(!Utils.isInteger(maxPrinc)) maxPrinc = "10000"; //最大本金默认给10000元

        if(TextUtils.isEmpty(account) && llAccount.getVisibility() == View.VISIBLE){  //账号
            MyToast.error(getString(R.string.receipt_account_null));
            return false;
        }

        if(TextUtils.isEmpty(pwd) && llPwd.getVisibility() == View.VISIBLE){  //密码
            MyToast.error(getString(R.string.receipt_pwd_null));
            return false;
        }

        if(Integer.parseInt(minFreq) < 300){   //最小频率不能小于300ms
            MyToast.error(getString(R.string.receipt_min_freq_error));
            return false;
        }

        if(Integer.parseInt(minFreq) > Integer.parseInt(maxFreq)){  //最小频率不能大于最大频率
            MyToast.error(getString(R.string.receipt_freq_error));
            return false;
        }

        if(TextUtils.isEmpty(receiptUrl) && llReceiptUrl.getVisibility() == View.VISIBLE){  //接单地址
            MyToast.error(getString(R.string.receipt_url_null));
            return false;
        }

        if(TextUtils.isEmpty(verifyCode) && llVerifyCode.getVisibility() == View.VISIBLE){  //图形验证码
            MyToast.error(getString(R.string.receipt_verify_code_null));
            return false;
        }

        if(TextUtils.isEmpty(smsCode) && llSmsCode.getVisibility() == View.VISIBLE){  //短信验证码
            MyToast.error(getString(R.string.receipt_verify_code_null));
            return false;
        }


        Params params = new Params(Integer.parseInt(minFreq), Integer.parseInt(maxFreq), account, pwd);
        params.setMinCommission(Integer.parseInt(minComm));
        params.setMaxPrincipal(Integer.parseInt(maxPrinc));
        params.setReceiptUrl(receiptUrl);
        params.setType(String.valueOf(spReceiptType.getSelectedItemPosition() + 1));  //接单类型
        params.setTypeIndex(spReceiptType.getSelectedItemPosition());   //接单类型选中的下标
        if(llVerifyCode.getVisibility() == View.VISIBLE) params.setVerifyCode(verifyCode);  //图形验证码和短信验证码共用一个变量表示
        else params.setVerifyCode(smsCode);
        params.setAccountType(String.valueOf(spAccountType.getSelectedItemPosition() + 1)); //账号（或任务）类型
        params.setAccountTypeIndex(spAccountType.getSelectedItemPosition());    //账号（或任务）类型选中的下标
        params.setPrePaymentCheck(cb1.isChecked());
        params.setLabelCheck(cb2.isChecked());
        mPlatform.setParams(params);
        Platforms.setCurrPlatform(mPlatform);

        return true;
    }

    /**
     * 初始化控件
     */
    private void initView(){
        ((TextView)findViewById(R.id.tv_title)).setText(mPlatform.getName());
        tvLog = findViewById(R.id.tv_log);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);
        tvBtn1 = findViewById(R.id.tv_btn1);
        tvBtn2 = findViewById(R.id.tv_btn2);
        etMinFreq = findViewById(R.id.et_min_frequency);
        etMaxFreq = findViewById(R.id.et_max_frequency);
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        etMinComm = findViewById(R.id.et_min_commission);
        etMaxPrinc = findViewById(R.id.et_max_principal);
        etReceiptUrl = findViewById(R.id.et_receipt_url);
        etVerifyCode = findViewById(R.id.et_verify_code);
        etSmsCode = findViewById(R.id.et_sms_code);
        spBuyerNum = findViewById(R.id.sp_buyer_num);
        spReceiptType = findViewById(R.id.sp_receipt_type);
        spAccountType = findViewById(R.id.sp_account_type);
        findViewById(R.id.iv_back).setOnClickListener(this);
        llAccount = findViewById(R.id.ll_account);
        llReceiptUrl = findViewById(R.id.ll_receipt_url);
        llBuyerNum = findViewById(R.id.ll_buyer_num);
        llPwd = findViewById(R.id.ll_pwd);
        llComm = findViewById(R.id.ll_commission);
        llReceiptType = findViewById(R.id.ll_receipt_type);
        llVerifyCode = findViewById(R.id.ll_verify_code);
        llSmsCode = findViewById(R.id.ll_sms_code);
        llAccountType = findViewById(R.id.ll_account_type);
        llCheckbox = findViewById(R.id.ll_checkbox);
        llIgnore = findViewById(R.id.ll_ignore);
        cb1 = findViewById(R.id.cb1);
        cb2 = findViewById(R.id.cb2);
        inVerifyCode = findViewById(R.id.iv_verify_code);

        int resId = R.array.receipt_type_default;

        /***下面对平台进行页面的区分(默认显示接单频率、账号、密码、买号、接单类型、佣金本金、还有两个超链接，因为这是大部分的平台都共有的)  ***/
        switch (mPlatform.getPageType()){
            case 1: //代表平台：铁蚂蚁 等（频率、账号、密码、买号、接单类型）
                llComm.setVisibility(View.GONE);
                if(TextUtils.equals(mPlatform.getName(), "京东麦田")){  //TODO 这里后面建议改成包名的比较方式
                    resId = R.array.receipt_type_6;
                }else if(TextUtils.equals(mPlatform.getName(), "闲蛋生活")){
                    resId = R.array.receipt_type_9;
                }else if(TextUtils.equals(mPlatform.getName(), "优家网")){
                    resId = R.array.receipt_type_10;
                }else if(TextUtils.equals(mPlatform.getName(), "猫猫咪")){
                    resId = R.array.receipt_type_11;
                }else {
                    resId = R.array.receipt_type_1;
                }
                break;
            case 2:  //代表平台：51人气王 等（频率、账号、密码、买号、佣金本金）
                llReceiptType.setVisibility(View.GONE);
                break;
            case 3:  //代表平台：51芒果派 等（频率、账号、密码、买号）
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                break;
            case 4:  //代表平台：51赚钱 等（频率、买号、接单类型）
                resId = R.array.receipt_type_3;
                llAccount.setVisibility(View.GONE);
                llPwd.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                break;
            case 5:  //代表平台：阿西里里 等（频率、账号、密码、买号、接单类型、验证码）
                resId = R.array.receipt_type_4;
                llComm.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 6:  //代表平台：宝妈团 等（频率）
                llAccount.setVisibility(View.GONE);
                llPwd.setVisibility(View.GONE);
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                break;
            case 7:  //代表平台：大豆 等（频率、账号、密码）
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                break;
            case 8:  //代表平台：大蒜测评 等（频率、买号）
                llAccount.setVisibility(View.GONE);
                llPwd.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                break;
            case 9:  //代表平台：单多多 等(频率、接单地址、接单类型）
                resId = R.array.receipt_type_4;
                llAccount.setVisibility(View.GONE);
                llPwd.setVisibility(View.GONE);
                llBuyerNum.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llReceiptUrl.setVisibility(View.VISIBLE);
                break;
            case 10:  //代表平台：红苹果 等（频率、接单类型）
                resId = R.array.receipt_type_5;
                llAccount.setVisibility(View.GONE);
                llPwd.setVisibility(View.GONE);
                llBuyerNum.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                break;
            case 11:  //代表平台：快单 等（频率、账号、密码、买号、验证码）
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 12:  //代表平台：快客、力度 等（频率、账号、密码、账号（任务）类型）
                if(TextUtils.equals(mPlatform.getName(), "力度")){
                    resId = R.array.receipt_type_8;
                    ((TextView)findViewById(R.id.tv_type)).setText("任务类型：");
                }else{
                    resId = R.array.receipt_type_7;
                }
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llAccountType.setVisibility(View.VISIBLE);
                break;
            case 13:  //代表平台：快单 等（频率、账号、密码、验证码）
                llBuyerNum.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 14:  //代表平台：木瓜科技 等（频率、账号）
                llPwd.setVisibility(View.GONE);
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                break;
            case 15:  //代表平台：私房钱 等（频率、账号、密码、买号、接单类型、佣金本金、验证码）
                resId = R.array.receipt_type_4;
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 16:  //代表平台：淘拍拍 等（频率、账号、验证码）
                llPwd.setVisibility(View.GONE);
                llBuyerNum.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llSmsCode.setVisibility(View.VISIBLE);
                break;
            case 17:  //代表平台：星创圈 等（频率、账号、密码、佣金本金）
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                break;
            case 18:  //代表平台：星创圈 等（频率、账号、密码、佣金本金、验证码）
                llBuyerNum.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 19:  //代表平台：直升机 等（频率、账号、密码、买号、垫付单、标签单）
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llCheckbox.setVisibility(View.VISIBLE);
                break;
            case 20:  //代表平台：多多花苑 等（频率、账号、密码、不接店铺、验证码）
                llBuyerNum.setVisibility(View.GONE);
                llComm.setVisibility(View.GONE);
                llReceiptType.setVisibility(View.GONE);
                llIgnore.setVisibility(View.VISIBLE);
                llVerifyCode.setVisibility(View.VISIBLE);
                break;
            case 0:  //代表平台：欢乐购 等(频率、账号、密码、买号、接单类型、佣金本金）
                resId = R.array.receipt_type_2;
                break;
        }

        names = new ArrayList<>();
        names.add(getString(R.string.receipt_default_data));
        if(llBuyerNum.getVisibility() == View.VISIBLE){     //只有可见的时候才会显示
            mAdapter = new ArrayAdapter<>(ReceiptActivity.this,
                    R.layout.item_buyer_num, R.id.tv_buyer_num, names);
            spBuyerNum.setAdapter(mAdapter);
        }


        if(llReceiptType.getVisibility() == View.VISIBLE){  //只有可见的时候才会显示
            String[] typeArr = getResources().getStringArray(resId);
            spReceiptType.setAdapter(new ArrayAdapter<>(ReceiptActivity.this, R.layout.item_buyer_num, R.id.tv_buyer_num, Arrays.asList(typeArr)));
        }

        if(llAccountType.getVisibility() == View.VISIBLE){  //只有可见的时候才会显示
            String[] typeArr = getResources().getStringArray(resId);
            spAccountType.setAdapter(new ArrayAdapter<>(ReceiptActivity.this, R.layout.item_buyer_num, R.id.tv_buyer_num, Arrays.asList(typeArr)));
        }

    }

    /**
     * 数据回显
     */
    private void initData(){
        Params params;
        List<Params> paramsList = MyApp.getLiteOrm().query(new QueryBuilder<>(Params.class).whereEquals("pkgName", mPlatform.getPkgName()));
        if(paramsList.size() > 0){   //原来存在参数，则进行数据回显
            params = paramsList.get(0);
            etMinFreq.setText(String.valueOf(mPlatform.getParams().getMinFrequency()));
            etMaxFreq.setText(String.valueOf(mPlatform.getParams().getMaxFrequency()));
            etAccount.setText(params.getAccount());
            etPwd.setText(params.getPassword());
            etMinComm.setText(String.valueOf(params.getMinCommission()));
            etMaxPrinc.setText(String.valueOf(params.getMaxPrincipal()));
            etReceiptUrl.setText(String.valueOf(params.getReceiptUrl()));
        }else{
            etMinFreq.setText(String.valueOf(mPlatform.getParams().getMinFrequency()));
            etMaxFreq.setText(String.valueOf(mPlatform.getParams().getMaxFrequency()));
        }

        String name = mPlatform.getName();

        if(mPlatform.getSituation() == 0){  //只有app
            tvBtn1.setText(String.format(getString(R.string.receipt_download_app), name));
            tvBtn2.setText(String.format(getString(R.string.receipt_open_app), name));
        }else if(mPlatform.getSituation() == 1){  //只有网页
            tvBtn1.setText(String.format(getString(R.string.receipt_open_web_outer), name));
            tvBtn2.setText(String.format(getString(R.string.receipt_open_web_inner), name));
        }else{  //既有app又有网页
            tvBtn1.setText(String.format(getString(R.string.receipt_open_web), name));
            tvBtn2.setText(String.format(getString(R.string.receipt_open_app), name));
        }

        params = mPlatform.getParams();
        if(params == null) return;
        buyerNumStr = params.getBuyerNumStr();
        if(llBuyerNum.getVisibility() == View.VISIBLE) showBuyerNum();   //买号回显
        if(llReceiptType.getVisibility() == View.VISIBLE) spReceiptType.setSelection(params.getTypeIndex());   //接单类型回显
        if(llAccountType.getVisibility() == View.VISIBLE) spAccountType.setSelection(params.getAccountTypeIndex());   //任务（或账号）类型回显
        if(llCheckbox.getVisibility() == View.VISIBLE){
            cb1.setChecked(params.isPrePaymentCheck()); //垫付单是否选中的回显
            cb2.setChecked(params.isLabelCheck()); //标签单是否选中的回显
        }
    }

    /**
     * 点击按钮1
     */
    private void btn1Click(){
        if(mPlatform.getSituation() == 0){  //只有app，下载app
            AppUtil.openUrlInOuter(mPlatform.getDownloadUrl());
        }else{   //只有网页或者既有app又有网页，打开web
            AppUtil.openUrlInOuter(mPlatform.getWebUrl());
        }
    }

    /**
     * 点击按钮2
     */
    private void btn2Click(){
        if(mPlatform.getSituation() == 1){  //只有网页，使用app内置浏览器打开
            startActivity(new Intent(this, WebActivity.class).putExtra("url", mPlatform.getWebUrl()).putExtra("name", mPlatform.getName()));
        }else if(AppUtil.isInstalled(mPlatform.getPkgName())){   //只有app或者既有app又有网页，并且app已安装，打开app
            AppUtil.startApp(mPlatform.getPkgName());
        }else { //app没安装
            MyToast.error(String.format(getString(R.string.receipt_app_not_install), mPlatform.getName()));
        }
    }

    /**
     * 对买号的回显
     */
    private void showBuyerNum(){
        if(TextUtils.isEmpty(buyerNumStr)) return;
        mBuyerNums = JSON.parseArray(buyerNumStr, BuyerNum.class);
        names.clear();
        for(BuyerNum num : mBuyerNums){
            names.add(num.getName());
        }

        mAdapter.notifyDataSetChanged();

        Params params = mPlatform.getParams();
        if(params != null){ //买号的回显
            spBuyerNum.setSelection(params.getBuyerNumIndex());
        }
    }

    /**
     * 获取验证码
     * @param url
     */
    private void getVerifyCode(String url){
        if(TextUtils.isEmpty(url)) return;
        url += "?" + mRandom.nextInt(1000);
        HttpUtil.getVerifyCode(url, new BitmapCallback() {
            @Override
            public void onSuccess(Response<Bitmap> response) {
                Bitmap bitmap = response.body();
                if(bitmap != null) inVerifyCode.setImageBitmap(bitmap);
                Headers headers = response.headers();
                String cookie = headers.get("Set-Cookie");
                if(!TextUtils.isEmpty(cookie)){
                    mPlatform.setVerifyCodeCookie(cookie);
                    Platforms.setCurrPlatform(mPlatform);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver != null) unregisterReceiver(mReceiver);
        mPlatform = Platforms.getCurrPlatform();
        if(mPlatform == null) return;
        mPlatform.setLog(tvLog.getText().toString());
        Params params = mPlatform.getParams();
        if(!TextUtils.isEmpty(buyerNumStr)) {
            params.setBuyerNumStr(buyerNumStr);
            mPlatform.setParams(params);
        }
        mList.set(position, mPlatform);
        Platforms.setPlatforms(mList);

        Platforms.setCurrPlatform(null);
    }


    class UpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String flag = intent.getStringExtra("flag");
            String msg = intent.getStringExtra("msg");
            if(TextUtils.isEmpty(msg)) return;
            switch (flag){
                case "log": //显示日志
                    refreshLogView(LogUtil.formatLog(msg), true);
                    break;
                case "showBuyerNum":
                    buyerNumStr = msg;
                    showBuyerNum();
                    break;
                case "get_verifycode":
                    getVerifyCode(msg);
                    break;
            }
        }
    }
}
