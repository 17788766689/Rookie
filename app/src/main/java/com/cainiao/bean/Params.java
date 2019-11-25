package com.cainiao.bean;

import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;

/**
 * 抢到需要用到的参数
 * Created by 123 on 2019/10/9.
 */
@Table("params")
public class Params {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private Long id;
    @Unique
    private String pkgName;    //平台包名，作为回显关联用
    private int minFrequency;  //接单最小频率
    private int maxFrequency;  //接单最大频率
    private String account;     //帐号
    private String password;    //密码
    @Ignore
    private BuyerNum buyerNum;    //买号
    private int minCommission;  //最小佣金
    private int maxPrincipal;   //最大本金
    private String receiptUrl;  //接单地址
    @Ignore
    private String type;        //接单类型
    @Ignore
    private int typeIndex;      //记录接单类型选中的下标，用来做回显
    @Ignore
    private String buyerNumStr;  //记录买号的字符串
    @Ignore
    private int buyerNumIndex;  //记录买号选中的下标
    @Ignore
    private String verifyCode;  //验证码
    @Ignore
    private String accountType;  //任务（或账号）类型
    @Ignore
    private int accountTypeIndex;  //记录任务（或账号）类型选中的下标
    @Ignore
    private boolean prePaymentCheck;  //垫付单是否选中
    @Ignore
    private boolean labelCheck;  //标签单是否选中
    @Ignore
    private boolean filterCheck = true;

    private String shopName;// 不接店铺


    public Params(){}

    public Params(int minFrequency, int maxFrequency){
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
    }

    public Params(int minFrequency, int maxFrequency, String account, String password) {
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.account = account;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public int getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(int minFrequency) {
        this.minFrequency = minFrequency;
    }

    public int getMaxFrequency() {
        return maxFrequency;
    }

    public void setMaxFrequency(int maxFrequency) {
        this.maxFrequency = maxFrequency;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BuyerNum getBuyerNum() {
        return buyerNum;
    }

    public void setBuyerNum(BuyerNum buyerNum) {
        this.buyerNum = buyerNum;
    }

    public int getMinCommission() {
        return minCommission;
    }

    public void setMinCommission(int minCommission) {
        this.minCommission = minCommission;
    }

    public int getMaxPrincipal() {
        return maxPrincipal;
    }

    public void setMaxPrincipal(int maxPrincipal) {
        this.maxPrincipal = maxPrincipal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public String getBuyerNumStr() {
        return buyerNumStr;
    }

    public void setBuyerNumStr(String buyerNumStr) {
        this.buyerNumStr = buyerNumStr;
    }

    public int getBuyerNumIndex() {
        return buyerNumIndex;
    }

    public void setBuyerNumIndex(int buyerNumIndex) {
        this.buyerNumIndex = buyerNumIndex;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getAccountTypeIndex() {
        return accountTypeIndex;
    }

    public void setAccountTypeIndex(int accountTypeIndex) {
        this.accountTypeIndex = accountTypeIndex;
    }

    public boolean isPrePaymentCheck() {
        return prePaymentCheck;
    }

    public void setPrePaymentCheck(boolean prePaymentCheck) {
        this.prePaymentCheck = prePaymentCheck;
    }

    public boolean isLabelCheck() {
        return labelCheck;
    }

    public void setLabelCheck(boolean labelCheck) {
        this.labelCheck = labelCheck;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setFilterCheck(boolean filterCheck) {
        this.filterCheck = filterCheck;
    }

    public boolean isFilterCheck() {
        return filterCheck;
    }


}
