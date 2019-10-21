package com.cainiao.bean;

/**
 * 买号实体类
 */
public class BuyerNum {

    private String id;
    private String name;

    public BuyerNum(){}

    public BuyerNum(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
