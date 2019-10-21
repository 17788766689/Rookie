package com.cainiao.bean;

/**
 * Created by 123 on 2019/9/26.
 */

public class Mine {

    private int resId;
    private String title;
    private String msg;
    private String tips;

    public Mine(int resId, String title, String msg, String tips) {
        this.resId = resId;
        this.title = title;
        this.msg = msg;
        this.tips = tips;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
