package com.cainiao.bean;

import android.text.TextUtils;

import com.cainiao.base.BaseAction;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

@Table("platform")
public class Platform implements Serializable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private Long id;
    private int resId;  //平台icon资源ID
    private String name; //平台名称
    @Unique
    private String pkgName;  //平台包名
    @Ignore
    private String host;  //平台的主机名（url）
    @Ignore
    private String downloadUrl;  //下载地址（url）
    @Ignore
    private String webUrl;  //官网地址（url）
    private int headerId; //每个item对应的HeaderId，用来分组，默认是0
    private int originalStatus; //记录item初始状态，用来还原status的状态（因为status的状态会不断地改变）
    private int status; //item状态，分为 0: "空闲中", 1: "限时免费", 2: "永久免费", 3: "正在接单", 4: "接单成功" 五种
    @Ignore
    private int situation;  //平台的情况，分为0: “只有app”， 1: “只有网页”和 2: “既有app也有网页”三种情况
    @Ignore
    private int pageType;   //页面类型
    @Ignore
    private boolean isStart;
    private long lastTime;
    @Ignore //忽略字段，将不存储到数据库
    private String log; //抢单日志
    @Ignore
    private Params params;
    @Ignore
    private BaseAction action;
    @Ignore
    private String[] headerArr = new String[]{"抢单平台", "打款平台"};
    @Ignore
    public static String[] statusArr = new String[]{"空闲中", "限时免费", "永久免费", "正在接单", "接单成功"};
    @Ignore
    private int minFrequency;  //接单最小频率
    @Ignore
    private int maxFrequency;  //接单最大频率

    public Platform(){}

    public Platform(Params params, int resId, String name, String pkgName, String host, String downloadUrl, String webUrl, int headerId, int status, int situation, int pageType, BaseAction action) {
        this.params = params;
        this.resId = resId;
        this.name = name;
        this.pkgName = pkgName;
        this.host = host;
        this.downloadUrl = downloadUrl;
        this.webUrl = webUrl;
        this.headerId = headerId;
        this.originalStatus = status;
        this.status = status;
        this.situation = situation;
        this.action = action;
        this.pageType = pageType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOriginalStatus() {
        return originalStatus;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        if(!TextUtils.isEmpty(log)) this.log = log;
    }

    public BaseAction getAction() {
        return action;
    }

    public void setAction(BaseAction action) {
        this.action = action;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public int getSituation() {
        return situation;
    }

    public void setSituation(int situation) {
        this.situation = situation;
    }

    public int getPageType() {
        return pageType;
    }

    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public String getStatusTip(int status) {
        if(status >= statusArr.length) return "";
        return statusArr[status];
    }

    public String getHeaderTip(int headerId) {
        if(headerId >= headerArr.length) return "";
        return headerArr[headerId];
    }
}
