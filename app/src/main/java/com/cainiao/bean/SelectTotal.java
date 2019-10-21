package com.cainiao.bean;

import java.util.List;

public class SelectTotal {

    private String code;
    private String msg;
    private List<Data> data;


    public class Data{
        private String taskName;
        private String totalNumber;
        private String todayNumber;
        private String lastTime;
        private String url;
        private String imgName;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTotalNumber() {
            return totalNumber;
        }

        public void setTotalNumber(String totalNumber) {
            this.totalNumber = totalNumber;
        }

        public String getTodayNumber() {
            return todayNumber;
        }

        public void setTodayNumber(String todayNumber) {
            this.todayNumber = todayNumber;
        }

        public String getLastTime() {
            return lastTime;
        }

        public void setLastTime(String lastTime) {
            this.lastTime = lastTime;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getImgName() {
            return imgName;
        }

        public void setImgName(String imgName) {
            this.imgName = imgName;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
}
