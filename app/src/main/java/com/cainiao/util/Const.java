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
    public static String MESSAGE_URL;

    public static String OUTER_BUY_URL;
    public static String OUTER_DOWNLOAD_URL;
    public static String SERVICE_QQ;
    public static String SERVICE_URI;
    public static boolean DEBUG_MODE;

    public static String DB_NAME;
    public static String SP_NAME;
    public static String UPDATE_ACTION;
    public static int LOG_MAX_LINE;

    public static int WGHS;             //重置
    public static int BJSHA;             //空闲中
    public static int TLSHE;            //限时免费
    public static int BLSA;     //永久免费
    public static int AJW_VA;       //正在接单
    public static int KSHG_AW;  //接单成功
    public static String STATUS_ACTION;

    public static String DEFAULT_ID;  //默认的imei，用来判断是否进行了获取imei的操作
    public static boolean FLAG;

    public static String NEED_VERIFY_PKGS; //需要进行滑块验证码的平台包名，以后添加其他平台的时候两个平台之间可以用","分号隔开

    private static void init(){
        STATUS_ACTION = "com.cainiao.status";
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
        MESSAGE_URL="message";
        DEFAULT_ID = "cainiao_device_id";
        LOG_MAX_LINE = 30;  //日志显示最多20行，超出则进行清理
        WGHS = -1;
        BJSHA = 0;
        TLSHE = 1;
        BLSA = 2;
        AJW_VA = 3;
        KSHG_AW = 4;
        DEBUG_MODE = true;
        FLAG = true;
        NEED_VERIFY_PKGS = "io.dcloud.UNIE9BC8DE,io.dcloud.UNI89500DB,io.dcloud.UNI55AAAAA,io.dcloud.UNIE7AC320";
    }

}
