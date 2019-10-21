package com.cainiao.util;


/**
 * Created by 123 on 2019/9/23.
 */

public class Const {

    static {
        init();
    }

    public static String HOST;
    public static String BASE_APP_URL;
    public static String BASE_USER_URL;

    public static String SELECT_TOTAL;
    public static String SELECT_URL;
    public static String FIND_USER_URL;
    public static String ACTIVATION_URL;
    public static String UPDATE_URL;

    public static String OUTER_BUY_URL;
    public static String OUTER_DOWNLOAD_URL;
    public static String SERVICE_QQ;
    public static String SERVICE_URI;
    public static boolean DEBUG_MODE;

    public static String DB_NAME;
    public static String SP_NAME;
    public static String UPDATE_ACTION;
    public static int LOG_MAX_LINE;

    public static int RESET;             //重置
    public static int IDLE;             //空闲中
    public static int FREE_LIMIT_TIME;  //限时免费
    public static int FREE_FOREVER;     //永久免费
    public static int RECEIPTING;       //正在接单
    public static int RECEIPT_SUCCESS;  //接单成功


    private static void init(){
        HOST = "http://www.cainiaoqd.com";
        BASE_APP_URL = HOST + "/app/";
        BASE_USER_URL = HOST + "/user/";
        OUTER_BUY_URL = "http://www.kuaifaka.com/purchasing?link=3buO9";
        OUTER_DOWNLOAD_URL = "http://www.lanzous.com/b744695";
        SERVICE_QQ = "2240295974";
        SERVICE_URI = "mqqwpa://im/chat?chat_type=wpa&uin=" + SERVICE_QQ;
        SELECT_TOTAL = "selectTotal";
        SELECT_URL = "selectUrl";
        FIND_USER_URL = "findUser";
        ACTIVATION_URL = "activation";
        UPDATE_URL = "version2";
        SP_NAME = "cainiao";
        DB_NAME = "cainiao.db";
        UPDATE_ACTION = "com.cainiao.update";
        LOG_MAX_LINE = 30;  //日志显示最多20行，超出则进行清理
        RESET = -1;
        IDLE = 0;
        FREE_LIMIT_TIME = 1;
        FREE_FOREVER = 2;
        RECEIPTING = 3;
        RECEIPT_SUCCESS = 4;
        DEBUG_MODE = true;
    }

}
