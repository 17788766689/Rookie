package com.cainiao.util;

import com.cainiao.R;
import com.cainiao.action.AMAction;
import com.cainiao.action.FSDMAction;
import com.cainiao.action.HLGAction;
import com.cainiao.action.LHMAction;
import com.cainiao.action.MFWAction;
import com.cainiao.action.MGDDAction;
import com.cainiao.action.RRLTAction;
import com.cainiao.action.TMYAction;
import com.cainiao.action.TQDAction;
import com.cainiao.action.TXLYAction;
import com.cainiao.action.WWDAction;
import com.cainiao.action.ZCMAction;
import com.cainiao.action.ZFZAction;
import com.cainiao.action._51MGPAction;
import com.cainiao.action._918RQWAction;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Params;
import com.cainiao.bean.Platform;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Platforms {

    private static List<Platform> mList;    //所有的平台
    private static List<Platform> mLatestList;  //最近使用的平台
    private static List<Platform> mRunningPlaforms;     //正在进行抢单任务的平台

    private static Platform mPlatform;

    public static void setPlatforms(List<Platform> platforms){
        mList = platforms;
    }

    public static Platform getCurrPlatform(){
        return mPlatform;
    }

    public static void setCurrPlatform(Platform platform){
        mPlatform = platform;
    }

    public static void addRunningPlatform(Platform platform){
        if(mRunningPlaforms == null) mRunningPlaforms = new ArrayList<>();
        mRunningPlaforms.add(platform);
    }

    public static void rmRunningPlatform(Platform platform){
        if(mRunningPlaforms != null) mRunningPlaforms.remove(platform);
    }

    /**
     * 获取常用的平台
     * @return
     */
    public static List<Platform> getLatestPlaforms(){
        if(mLatestList == null) {
            mLatestList = new ArrayList<>();
        }else {
            mLatestList.clear();
        }
        List<Platform> list = DbUtil.query(new QueryBuilder<>(Platform.class).orderBy("lastTime desc"));
        if(list != null && list.size() > 0) mLatestList.addAll(list);
        return mLatestList;
    }


    public static List<Platform> getRunningPlaforms(){
        return mRunningPlaforms;
    }

    //int resId, String name, int headerId, int status
    public static List<Platform> getPlatforms(){
        if(mList == null){
            mList = new ArrayList<>();
            /******************************************   抢单平台   **************************************************/
            //第一行
            mList.add(new Platform(new Params(2000,3000),R.mipmap.tiemayi, "铁蚂蚁", "com.p3066672015.rpm", "https://api.5586pk.com", "", "http://www.3318pk.com/invite/register.html?rCode=60645053",0, Const.FREE_FOREVER, 2, 1, new TMYAction()));
            mList.add(new Platform(new Params(3000,5000),R.mipmap.huanlegou, "欢乐购", "io.dcloud.UNI89500DB", "http://app.biaoqiandan.com", "https://fir.im/tbrp", "",0, Const.FREE_LIMIT_TIME, 0, 0, new HLGAction()));
            mList.add(new Platform(new Params(3000,5000),R.mipmap.taoqiangdan, "淘抢单", "io.dcloud.UNI55AAAAA", "http://www.51qiangdanwang.com", "https://fir.im/l5y2", "",0, Const.FREE_LIMIT_TIME, 0, 0, new TQDAction()));
            mList.add(new Platform(new Params(3000,5000),R.mipmap.mangguodingdong, "芒果叮咚", "io.dcloud.UNIE7AC320", "https://xiaomangguo.lingchendan.com", "https://fir.im/r7na", "",  0, Const.FREE_LIMIT_TIME, 0, 0, new MGDDAction()));
            //第二行
            mList.add(new Platform(new Params(3000,5000),R.mipmap._918renqiwang, "918人气王", "io.dcloud.UNIE9BC8DE", "http://www.918dainizhuan.com", "https://fir.im/regy", "",0, Const.FREE_LIMIT_TIME, 0, 0, new _918RQWAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap._51renqiwang, "51人气王", "com.platform4", "", "", "",0, Const.FREE_LIMIT_TIME, 0, 2, null));
            mList.add(new Platform(new Params(4000,5000),R.mipmap._51mangguopai, "51芒果派", "com._51mangguopai", "http://madou.fl1m.cn", "", "http://madou.fl1m.cn/login?returnUrl=/",0, Const.FREE_LIMIT_TIME, 1, 3, new _51MGPAction()));
            //mList.add(new Platform(new Params(0,0),R.mipmap._51zhuanqian, "51赚钱", "com.platform6", "", "", "",0, Const.IDLE, 0, 4, null));
//            //第三行
            mList.add(new Platform(new Params(3500,5000),R.mipmap.aimi, "爱米", "com.aimi", "https://www.huimi123.com", "", "https://www.huimi123.com/login",0, Const.IDLE, 1, 3, new AMAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.axilili, "阿西里里", "com.platform8", "", "", "",0, Const.IDLE, 0, 5, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.baomatuan, "宝妈团", "com.platform8", "", "", "",0, Const.IDLE, 0, 6, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.baibaizhuan, "白白赚", "com.platform8", "", "", "",0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            //第四行
//            mList.add(new Platform(new Params(0,0),R.mipmap.dadou, "大豆", "com.platform8", "", "", "",0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.damuniuniu, "达姆牛牛", "com.platform8", "", "", "",0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.dasuanceping, "大蒜测评", "com.platform8", "", "", "",0, Const.IDLE, 0, 8, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.dashixiong, "大师兄", "com.platform8", "", "", "",0, Const.IDLE, 0, 3, null));
//            //第五行
//            mList.add(new Platform(new Params(0,0),R.mipmap.dashuwang, "大树王", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.danduoduo, "单多多", "com.platform8", "", "", "", 0, Const.IDLE, 0, 9, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.danzhuan, "单赚", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.dingdangmao, "叮当猫", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            //第六行
//            mList.add(new Platform(new Params(0,0),R.mipmap.facaishu, "发财树", "com.platform8", "", "", "", 0, Const.IDLE, 0, 2, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.guli, "谷粒", "com.platform8", "", "", "", 0, Const.IDLE, 0, 6, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.gebiwangshushu, "隔壁王叔叔", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.gongxiangke, "共享客", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第七行
//            mList.add(new Platform(new Params(0,0),R.mipmap.hongpingguo, "红苹果", "com.platform8", "", "", "", 0, Const.IDLE, 0, 10, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.huanqiuzhongbang, "环球众帮", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.hongseshuiguotan, "红色水果堂", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.huzhudianshang, "互助电商", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第八行
//            mList.add(new Platform(new Params(0,0),R.mipmap.jinlizhuan, "锦鲤赚", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.jingdongmaitian, "京东麦田", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 1, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.jutaozhan, "聚淘栈", "com.platform8", "", "", "", 0, Const.IDLE, 0, 5, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.kuaidan, "快单", "com.platform8", "", "", "", 0, Const.IDLE, 0, 11, null));
//            //第九行
//            mList.add(new Platform(new Params(0,0),R.mipmap.kuaike, "快客", "com.platform8", "", "", "", 0, Const.IDLE, 0, 12, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.kaixinguo, "开心果", "com.platform8", "", "", "", 0, Const.IDLE, 0, 2, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.leduoduo, "乐多多", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
             mList.add(new Platform(new Params(500,800),R.mipmap.linghuomai, "灵活买", "com.linghuomai.app", "http://www.linghuomai.com", "https://fir.im/hxkv", "", 0, Const.FREE_LIMIT_TIME, 0, 3, new LHMAction()));
//            //第十行
//            mList.add(new Platform(new Params(0,0),R.mipmap.lingqugo, "领趣GO", "com.platform8", "", "", "", 0, Const.IDLE, 0, 2, null));
////            mList.add(new Platform(new Params(0,0),R.mipmap.lanmao, "懒猫", "com.platform8", "", "https://fir.im/hxkv", "", 0, Const.FREE_LIMIT_TIME, 0, 0, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.lidu, "力度", "com.platform8", "", "", "", 0, Const.IDLE, 0, 12, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.muguakeji, "木瓜科技", "com.platform8", "", "", "", 0, Const.IDLE, 0, 14, null));
//            //第十一行
            mList.add(new Platform(new Params(500,800),R.mipmap.mifengwo, "蜜蜂窝", "com.zhizuanling.app", "https://www.991zhizuanling.com", "", "", 0, Const.IDLE, 0, 3, new MFWAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.maotouying, "猫头鹰", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.miaomiaomiao, "喵喵喵", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.mizhifu, "米之富", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十二行
//            mList.add(new Platform(new Params(0,0),R.mipmap.mengxiangjia, "梦想家", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.mingyun, "鸣云", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.maijiale, "麦家乐", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.niurouxuetan, "牛肉学堂", "com.platform8", "", "", "", 0, Const.IDLE, 0, 10, null));
//            //第十三行
            mList.add(new Platform(new Params(2000,4000),R.mipmap.ningmengpai, "柠檬派", "com.ningmengpai", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.niuyouguo, "牛油果", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.pinjinbi, "拼金币", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.qingningmeng, "青柠檬", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十四行
//            mList.add(new Platform(new Params(0,0),R.mipmap.panguoguo, "胖果果", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.paipaidan, "派派单", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.erliudianzi, "二流电子", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.renqidou, "人气豆", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十五行
//            mList.add(new Platform(new Params(0,0),R.mipmap.renqiyun, "人气云", "com.platform8", "", "", "", 0, Const.IDLE, 0, 11, null));
            mList.add(new Platform(new Params(500,800),R.mipmap.renrenletao, "人人乐淘", "cn.rrletao.app.rrlt", "https://www.rrletao.cn", "", "", 0, Const.IDLE, 0, 3, new RRLTAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.ruyi, "如意", "com.platform8", "", "", "", 0, Const.IDLE, 0, 8, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.suanluobo, "酸罗卜", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十六行
//            mList.add(new Platform(new Params(0,0),R.mipmap.shengduoduo, "升多多", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.shangke, "尚客", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.sifangqian, "私房钱", "com.platform8", "", "", "", 0, Const.IDLE, 0, 15, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.tianshi, "天时", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十七行
//            mList.add(new Platform(new Params(0,0),R.mipmap.taopaipai, "淘拍拍", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 16, null));
            mList.add(new Platform(new Params(2000,4000),R.mipmap.wanwanduo, "旺旺多", "com.wanwanduo", "http://dkb.j66q66.com", "", "http://dkb.j66q66.com/login?returnUrl=/", 0, Const.IDLE, 1, 3, new WWDAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaobaitu, "小白兔", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaojingling, "小精灵", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第十八行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaoheizhu, "小黑猪", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaotudou, "小土豆", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaopeiqi, "小佩奇", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaobaixiang, "小白象", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            //第十九行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaomogu, "小蘑菇", "com.platform8", "", "", "", 0, Const.IDLE, 0, 6, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaojinli, "小锦鲤", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaozhushou, "小助手", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaomifengtuiguan, "小蜜蜂推广", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第二十行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaoqingwa, "小青蛙", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaopingguo, "小苹果", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaogou360, "小狗360", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiandanshenghuo, "闲蛋生活", "com.platform8", "", "", "", 0, Const.IDLE, 0, 1, null));
//            //第二十一行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiangchiniurou, "想吃牛肉", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xubaofang, "寻宝房", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xinshijie, "新世界", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xishuashua, "嘻唰唰", "com.platform8", "", "", "", 0, Const.IDLE, 0, 7, null));
//            //第二十二行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xinchuangquan, "星创圈", "com.platform8", "", "", "", 0, Const.IDLE, 0, 17, null));
////            mList.add(new Platform(new Params(0,0),R.mipmap.xilanhua, "西兰花", "com.platform8", "", "", "", 0, Const.IDLE, 0, 0, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.youjiawang, "优家网", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 1, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.yanleduo, "养乐多", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            //第二十三行
//            mList.add(new Platform(new Params(0,0),R.mipmap.youdanwang, "有单网", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.youhuicha, "优惠查", "com.platform8", "", "", "", 0, Const.IDLE, 0, 6, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.youdanzuo, "有单做", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.yueguangbaohe, "月光宝盒", "com.platform8", "", "", "", 0, Const.IDLE, 0, 8, null));
//            //第二十四行
//            mList.add(new Platform(new Params(0,0),R.mipmap.zuanlatiao, "赚辣条", "com.platform8", "", "", "", 0, Const.IDLE, 0, 6, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.yuanjihua, "源计划", "com.platform8", "", "", "", 0, Const.IDLE, 0, 18, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.zhaocaizhu, "招财猪", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
            mList.add(new Platform(new Params(3200,4500),R.mipmap.zhaocaimao, "招财猫", "com.zhaocaimao", "http://zcm.zcm2019.com", "", "http://zcm.zcm2019.com/login?returnUrl=/", 0, Const.FREE_LIMIT_TIME, 1, 3, new ZCMAction()));
//            //第二十五行
//            mList.add(new Platform(new Params(0,0),R.mipmap.zuankeban, "赚客班", "com.platform8", "", "", "", 0, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.zuanyoumi, "口袋精灵", "com.platform8", "", "", "", 0, Const.IDLE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.ziningmeng, "紫柠檬", "com.platform8", "", "", "", 0, Const.IDLE, 0, 16, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.zhangquanzhong, "涨权重", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            //第二十六行
//            mList.add(new Platform(new Params(0,0),R.mipmap.zhishengji, "直升机", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 19, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.meifankeji, "魅凡科技", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.taobaoquan, "淘宝圈", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.shuishanggongchuang, "水商共创", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//            //第二十七行
//            mList.add(new Platform(new Params(0,0),R.mipmap.shenghuidaren, "省惠达人", "com.platform8", "", "", "", 0, Const.FREE_LIMIT_TIME, 0, 7, null));
//
//            /******************************************   打款平台   **************************************************/
//            //第一行
//            mList.add(new Platform(new Params(0,0),R.mipmap.duoduohuayuan, "多多花苑", "com.platform8", "", "", "", 1, Const.IDLE, 0, 20, null));
            mList.add(new Platform(new Params(3000,4000),R.mipmap.fongshoudamai, "丰收大麦", "com.fongshoudamai", "http://api.fsdmff.cn", "", "http://wx.xdhfnch.cn:8081/workerLogin", 1, Const.IDLE, 1, 20, new FSDMAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.jiuxingkeji, "九鑫科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.hongchangkeji, "鸿昌科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            //第二行
//            mList.add(new Platform(new Params(0,0),R.mipmap.jinxiuleyuan, "锦绣乐园", "com.platform8", "", "", "", 1, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.ningmengshu, "柠檬树", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.maomaomi, "猫猫咪打款", "com.platform8", "", "", "", 1, Const.IDLE, 0, 1, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.qiutianleyuan, "秋天乐园", "com.platform8", "", "", "", 1, Const.IDLE, 0, 13, null));
//            //第三行
////            mList.add(new Platform(new Params(0,0),R.mipmap.qishikeji, "骑士科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.qilin, "麒麟", "com.platform8", "", "", "", 1, Const.FREE_LIMIT_TIME, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.sanmukeji, "三木科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.siyecao, "四叶草", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            //第四行
            mList.add(new Platform(new Params(2500,3000),R.mipmap.tianxileyuan, "天玺乐园", "com.platform8", "http://cs.8818s.cn:8080", "", "http://cs.8818s.cn/user/#/login", 1, Const.IDLE, 1, 7, new TXLYAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.teshuakeji, "特刷科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.taomimi, "淘米米", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaomache, "小马车", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            //第四行
//            mList.add(new Platform(new Params(0,0),R.mipmap.weitaokeji, "微淘科技", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaomifengdakuang, "打款小蜜蜂", "com.platform8", "", "", "", 1, Const.IDLE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaolanshu, "小蓝书", "com.platform8", "", "", "", 1, Const.IDLE, 0, 7, null));
            mList.add(new Platform(new Params(1800,3000),R.mipmap.zhengfuzhe, "征服者", "com.zhengfuz.app.xmx5zak", "http://app.zhengfuz.com", "", "", 1, Const.FREE_LIMIT_TIME, 0, 7, new ZFZAction()));


            initLatestStatus();
        }
        return mList;
    }

    /**
     * 初始化（还原）常用平台的初始状态
     */
    private static void initLatestStatus(){
        List<Platform> list =  getLatestPlaforms();
        for(int i = 0, len = list.size(); i < len; i++){
            Platform platform = list.get(i);
            platform.setStatus(platform.getOriginalStatus());   //还原初始状态
            list.set(i, platform);
        }
        DbUtil.update(list);
    }

    public static Map<String, Integer> getPlatformIcons(){
        Map<String, Integer> map = new HashMap<>();
        map.put("51mangguopai", R.mipmap._51mangguopai);
        map.put("51renqiwang", R.mipmap._51renqiwang);
        map.put("51zuanqian", R.mipmap._51zhuanqian);
        map.put("aimi", R.mipmap.aimi);
        map.put("axilili", R.mipmap.axilili);
        map.put("baibaizuan", R.mipmap.baibaizhuan);
        map.put("baomatuan", R.mipmap.baomatuan);
        map.put("dadou", R.mipmap.dadou);
        map.put("damuniuniu", R.mipmap.damuniuniu);
        map.put("danduoduo", R.mipmap.danduoduo);
        map.put("danzuan", R.mipmap.danzhuan);
        map.put("dapingguo", R.mipmap.dapingguo);
        map.put("dashixiong", R.mipmap.dashixiong);
        map.put("dasuanceping", R.mipmap.dasuanceping);
        map.put("dashuwang", R.mipmap.dashuwang);
        map.put("dingdanmao", R.mipmap.dingdangmao);
        map.put("duoduohuayuan", R.mipmap.duoduohuayuan);
        map.put("ershixion", R.mipmap.ershixion);
        map.put("facaishu", R.mipmap.facaishu);
        map.put("fongshoudamai", R.mipmap.fongshoudamai);
        map.put("gebiwanshushu", R.mipmap.gebiwangshu);
        map.put("gongxiangke", R.mipmap.gongxiangke);
        map.put("gubiwanshushu", R.mipmap.gubiwanshushu);
        map.put("guli", R.mipmap.guli);
        map.put("hongchangkeji", R.mipmap.hongchangkeji);
        map.put("hongpingguo", R.mipmap.hongpingguo);
        map.put("hongseshuiguotan", R.mipmap.hongseshuiguotan);
        map.put("huanqiuzonbang", R.mipmap.huanqiuzhongbang);
        map.put("huzudianshan", R.mipmap.huzhudianshang);
        map.put("jingdongmaitian", R.mipmap.jingdongmaitian);
        map.put("jinlizhuan", R.mipmap.jinlizhuan);
        map.put("jinlizuan", R.mipmap.jinlizhuan);
        map.put("jinxiuleyuan", R.mipmap.jinxiuleyuan);
        map.put("jiuxingkeji", R.mipmap.jiuxingkeji);
        map.put("jutaozhan", R.mipmap.jutaozhan);
        map.put("kaixinguo", R.mipmap.kaixinguo);
        map.put("kuaidan", R.mipmap.kuaidan);
        map.put("kuaike", R.mipmap.kuaike);
        map.put("lanmao", R.mipmap.lanmao);
        map.put("leduoduo", R.mipmap.leduoduo);
        map.put("lidu", R.mipmap.lidu);
        map.put("linghuomai", R.mipmap.linghuomai);
        map.put("lingqugo", R.mipmap.lingqugo);
        map.put("logo", R.mipmap.logo);
        map.put("maijiale", R.mipmap.maijiale);
        map.put("maomaomi", R.mipmap.maomaomi);
        map.put("maotouying", R.mipmap.maotouying);
        map.put("meifankeji", R.mipmap.meifankeji);
        map.put("mengxiangjia", R.mipmap.mengxiangjia);
        map.put("miaomiaomiao", R.mipmap.miaomiaomiao);
        map.put("mifengwo", R.mipmap.mifengwo);
        map.put("mingyun", R.mipmap.mingyun);
        map.put("mizhifu", R.mipmap.mizhifu);
        map.put("muguakeji", R.mipmap.muguakeji);
        map.put("ningmengpai", R.mipmap.ningmengpai);
        map.put("ningmengshu", R.mipmap.ningmengshu);
        map.put("niurouxuetan", R.mipmap.niurouxuetan);
        map.put("niuyouguo", R.mipmap.niuyouguo);
        map.put("paipaidan", R.mipmap.paipaidan);
        map.put("panguoguo", R.mipmap.panguoguo);
        map.put("pengyouquan", R.mipmap.pengyouquan);
        map.put("pinjinbi", R.mipmap.pinjinbi);
        map.put("qianshouwenhua", R.mipmap.qianshouwenhua);
        map.put("qilin", R.mipmap.qilin);
        map.put("qingningmeng", R.mipmap.qingningmeng);
        map.put("qishikeji", R.mipmap.qishikeji);
        map.put("qiutianleyuan", R.mipmap.qiutianleyuan);
        map.put("renqidou", R.mipmap.renqidou);
        map.put("renqiyun", R.mipmap.renqiyun);
        map.put("renrenletao", R.mipmap.renrenletao);
        map.put("ruyi", R.mipmap.ruyi);
        map.put("sanmukeji", R.mipmap.sanmukeji);
        map.put("shangke", R.mipmap.shangke);
        map.put("shengduoduo", R.mipmap.shengduoduo);
        map.put("sifangqian", R.mipmap.sifangqian);
        map.put("siyecao", R.mipmap.siyecao);
        map.put("suanluobo", R.mipmap.suanluobo);
        map.put("suishouzuan", R.mipmap.suishouzuan);
        map.put("taobaoquan", R.mipmap.taobaoquan);
        map.put("taomimi", R.mipmap.taomimi);
        map.put("taopaipai", R.mipmap.taopaipai);
        map.put("taosange", R.mipmap.taosange);
        map.put("teshuakeji", R.mipmap.teshuakeji);
        map.put("tianshi", R.mipmap.tianshi);
        map.put("tianxileyuan", R.mipmap.tianxileyuan);
        map.put("tiemayi", R.mipmap.tiemayi);
        map.put("wanwanduo", R.mipmap.wanwanduo);
        map.put("weitaokeji", R.mipmap.weitaokeji);
        map.put("xiandanshenghuo", R.mipmap.xiandanshenghuo);
        map.put("xiangchiniurou", R.mipmap.xiangchiniurou);
        map.put("xiaobaitu", R.mipmap.xiaobaitu);
        map.put("xiaobaixiang", R.mipmap.xiaobaixiang);
        map.put("xiaogou360", R.mipmap.xiaogou360);
        map.put("xiaoheizhu", R.mipmap.xiaoheizhu);
        map.put("xiaohuamao", R.mipmap.xiaohuamao);
        map.put("xiaojingling", R.mipmap.xiaojingling);
        map.put("xiaojinli", R.mipmap.xiaojinli);
        map.put("xiaolanshu", R.mipmap.xiaolanshu);
        map.put("xiaomache", R.mipmap.xiaomache);
        map.put("xiaomifengdakuang", R.mipmap.xiaomifengdakuang);
        map.put("xiaomifengtuiguan", R.mipmap.xiaomifengtuiguan);
        map.put("xiaomogu", R.mipmap.xiaomogu);
        map.put("xiaopeiqi", R.mipmap.xiaopeiqi);
        map.put("xiaopingguo", R.mipmap.xiaopingguo);
        map.put("xiaoqingwa", R.mipmap.xiaoqingwa);
        map.put("xiaotudou", R.mipmap.xiaotudou);
        map.put("xiaozhushou", R.mipmap.xiaozhushou);
        map.put("xihongshi", R.mipmap.xihongshi);
        map.put("xilanhua", R.mipmap.xilanhua);
        map.put("xinchuangquan", R.mipmap.xinchuangquan);
        map.put("xinhong3", R.mipmap.xinhong3);
        map.put("xinshijie", R.mipmap.xinshijie);
        map.put("xishuashua", R.mipmap.xishuashua);
        map.put("xubaofang", R.mipmap.xubaofang);
        map.put("yanleduo", R.mipmap.yanleduo);
        map.put("youdanwang", R.mipmap.youdanwang);
        map.put("youdanzuo", R.mipmap.youdanzuo);
        map.put("youhuicha", R.mipmap.youhuicha);
        map.put("youjiawang", R.mipmap.youjiawang);
        map.put("yuanjihua", R.mipmap.yuanjihua);
        map.put("yueguangbaohe", R.mipmap.yueguangbaohe);
        map.put("zhangquanzhong", R.mipmap.zhangquanzhong);
        map.put("zhaocaimao", R.mipmap.zhaocaimao);
        map.put("zhaocaizhu", R.mipmap.zhaocaizhu);
        map.put("zhengfuzhe", R.mipmap.zhengfuzhe);
        map.put("zhishengji", R.mipmap.zhishengji);
        map.put("ziningmeng", R.mipmap.ziningmeng);
        map.put("zuankeban", R.mipmap.zuankeban);
        map.put("zuanlatiao", R.mipmap.zuanlatiao);
        map.put("zuanyoumi", R.mipmap.zuanyoumi);
        map.put("mangguodingdong", R.mipmap.mangguodingdong);
        return map;
    }

}
