package com.cainiao.util;

import com.cainiao.R;
import com.cainiao.action.AMAction;
import com.cainiao.action.BMTAction;
import com.cainiao.action.BSLMAction;
import com.cainiao.action.CXYAction;
import com.cainiao.action.DDAction;
import com.cainiao.action.DDDAction;
import com.cainiao.action.DDHYAction;
import com.cainiao.action.DJYYPDAction;
import com.cainiao.action.DKXMFAction;
import com.cainiao.action.DMNNAction;
import com.cainiao.action.DSAction;
import com.cainiao.action.DSHZAction;
import com.cainiao.action.FCSAction;
import com.cainiao.action.FSDMAction;
import com.cainiao.action.GBWSSAction;
import com.cainiao.action.GLAction;
import com.cainiao.action.GYTAction;
import com.cainiao.action.HCKJAction;
import com.cainiao.action.HGAction;
import com.cainiao.action.HHJAction;
import com.cainiao.action.HLGAction;
import com.cainiao.action.HSSGTAction;
import com.cainiao.action.HZWAction;
import com.cainiao.action.JCYAction;
import com.cainiao.action.JDMTAction;
import com.cainiao.action.JHQAction;
import com.cainiao.action.JLZAction;
import com.cainiao.action.JNZAction;
import com.cainiao.action.JQMAction;
import com.cainiao.action.JRQAction;
import com.cainiao.action.JXKJAction;
import com.cainiao.action.JXLYAction;
import com.cainiao.action.KDCAction;
import com.cainiao.action.KDJLAction;
import com.cainiao.action.KKAction;
import com.cainiao.action.KXGAction;
import com.cainiao.action.LDDAction;
import com.cainiao.action.LHMAction;
import com.cainiao.action.MGDDAction;
import com.cainiao.action.MGKJAction;
import com.cainiao.action.MLRJAction;
import com.cainiao.action.MMMAction;
import com.cainiao.action.MMMDkAction;
import com.cainiao.action.MTAction;
import com.cainiao.action.MTYAction;
import com.cainiao.action.NMPAction;
import com.cainiao.action.NMSAction;
import com.cainiao.action.NNAction;
import com.cainiao.action.NYGAction;
import com.cainiao.action.PPDAction;
import com.cainiao.action.QTLYAction;
import com.cainiao.action.RRLTAction;
import com.cainiao.action.RYBAction;
import com.cainiao.action.SFQQDAction;
import com.cainiao.action.SLBAction;
import com.cainiao.action.SMKJAction;
import com.cainiao.action.SYCAction;
import com.cainiao.action.TANGSENGAction;
import com.cainiao.action.TBQAction;
import com.cainiao.action.TMMAction;
import com.cainiao.action.TMYAction;
import com.cainiao.action.TMYPDAction;
import com.cainiao.action.TQDAction;
import com.cainiao.action.TSKJAction;
import com.cainiao.action.TXLYAction;
import com.cainiao.action.WDBAction;
import com.cainiao.action.WWDAction;
import com.cainiao.action.XBXAction;
import com.cainiao.action.XHZAction;
import com.cainiao.action.XIAOJLAction;
import com.cainiao.action.XJLAction;
import com.cainiao.action.XLAction;
import com.cainiao.action.XLSAction;
import com.cainiao.action.XMCAction;
import com.cainiao.action.XMGAction;
import com.cainiao.action.XNGAction;
import com.cainiao.action.XPGPDDAction;
import com.cainiao.action.XQWAction;
import com.cainiao.action.XXKJAction;
import com.cainiao.action.XYSAction;
import com.cainiao.action.XZGAction;
import com.cainiao.action.YBAction;
import com.cainiao.action.YDZAction;
import com.cainiao.action.YJJAction;
import com.cainiao.action.YLDAction;
import com.cainiao.action.YMBAction;
import com.cainiao.action.YMCAction;
import com.cainiao.action.ZCMAction;
import com.cainiao.action.ZFZPDAction;
import com.cainiao.action.ZLAction;
import com.cainiao.action.ZLTQDAction;
import com.cainiao.action.ZQQAction;
import com.cainiao.action.ZQZAction;
import com.cainiao.action.ZZAction;
import com.cainiao.action._51MGPAction;
import com.cainiao.action._918RQWAction;
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

    public static void setPlatforms(List<Platform> platforms) {
        mList = platforms;
    }

    public static Platform getCurrPlatform() {
        return mPlatform;
    }

    public static void setCurrPlatform(Platform platform) {
        mPlatform = platform;
    }

    public static void addRunningPlatform(Platform platform) {
        if (mRunningPlaforms == null) mRunningPlaforms = new ArrayList<>();
        mRunningPlaforms.add(platform);
    }

    public static void rmRunningPlatform(Platform platform) {
        if (mRunningPlaforms != null) mRunningPlaforms.remove(platform);
    }

    /**
     * 获取常用的平台
     *
     * @return
     */
    public static List<Platform> getLatestPlaforms() {
        if (mLatestList == null) {
            mLatestList = new ArrayList<>();
        } else {
            mLatestList.clear();
        }
        List<Platform> list = DbUtil.query(new QueryBuilder<>(Platform.class).orderBy("lastTime desc"));
        if (list != null && list.size() > 0) mLatestList.addAll(list);
        return mLatestList;
    }


    public static List<Platform> getRunningPlaforms() {
        return mRunningPlaforms;
    }

    //int resId, String name, int headerId, int status
    public static List<Platform> getPlatforms() {
        if (mList == null) {
            mList = new ArrayList<>();
            /******************************************   抢单平台   **************************************************/
            //第一行
            mList.add(new Platform(new Params(12000, 15000), R.mipmap.tiemayi, "铁蚂蚁(派单)", "com.p3066672015", "https://api.damingduo.cn/", "", "http://www.3318pk.com/invite/register.html?rCode=60645053", 0, Const.BLSA, 2, 1, new TMYPDAction()));
            mList.add(new Platform(new Params(12000, 15000), R.mipmap.tiemayi, "铁蚂蚁(抢单)", "com.p3066672015.rpm", "https://api.damingduo.cn/", "", "http://www.3318pk.com/invite/register.html?rCode=60645053", 0, Const.BLSA, 2, 1, new TMYAction()));
            mList.add(new Platform(new Params(1500, 2000), R.mipmap.yunbao  , "云宝", "com.krldcu.kwwfteg", "http://m.fanpnsn.cn", "http://m.fanpnsn.cn/Other/DownloadApp", "", 0, Const.TLSHE, 0, 2, new YBAction()));
            mList.add(new Platform(new Params(2000, 4000), R.mipmap.dingsheng, "鼎盛", "com.dingsheng", "http://ds.beeftea.cn", "", "http://ds.beeftea.cn/Login/Index", 0, Const.BJSHA, 1, 2, new DSAction()));
            mList.add(new Platform(new Params(1500, 2000), R.mipmap.maotouying  , "猫头鹰", "com.maotouying", "http://m.haoyun2019.com", "", "http://m.haoyun2019.com/Login", 0, Const.TLSHE, 1, 2, new MTYAction()));
            mList.add(new Platform(new Params(2000, 2500), R.mipmap.baishoulianmeng, "百手联盟", "com.baishoulianmeng", "http://aaa.66145.cn", "", "http://aaa.66145.cn/index.php?g=Wap&m=Login&a=logon", 0, Const.TLSHE, 1, 7, new BSLMAction()));
            mList.add(new Platform(new Params(2000, 2500), R.mipmap.youmaicai, "油麦菜", "com.youmaicai", "http://aaa.yyoumaicai.com", "", "http://aaa.yyoumaicai.com/index.php?g=Wap&m=index&a=notice", 0, Const.TLSHE, 1, 7, new YMCAction()));
            mList.add(new Platform(new Params(10000, 12000), R.mipmap.huahuajie, "花花街", "com.xmyyhhj.nrv", "http://www.qianqianke.cn", "", "", 0, Const.TLSHE, 0, 7, new HHJAction()));
            mList.add(new Platform(new Params(60000, 100000), R.mipmap.xiaonangua, "小南瓜", "com.xiaonangua", "http://86.xiaonangua68.com", "", "http://86.xiaonangua68.com/home/index.html#/login", 1, Const.TLSHE, 1, 7, new XNGAction()));


            mList.add(new Platform(new Params(5000, 8000), R.mipmap.jianghuquan, "江湖圈", "com.jianghuquan", "http://www.177wt.cn:8888", "", "http://www.177wt.cn/order-client/page/login/login.html?from=2", 0, Const.TLSHE, 1, 7, new JHQAction()));
            mList.add(new Platform(new Params(2000, 4000), R.mipmap.tangseng  , "唐僧", "com.tangseng", "http://ts.tangsengshuo.shop", "", "http://ts.tangsengshuo.shop/home/initLogin.do", 0, Const.TLSHE, 1, 2, new TANGSENGAction()));
            mList.add(new Platform(new Params(20000, 25000), R.mipmap.jucaiyuan, "聚财源", "com.jucaiyuan", "http://jcy.solid88.cn", "", "http://jcy.solid88.cn/login?returnUrl=/", 0, Const.BJSHA, 1, 2, new JCYAction()));
//
            mList.add(new Platform(new Params(10000, 12000), R.mipmap.chouxiaoya, "丑小鸭", "com.chouxiaoya", "https://api.b5gw.cn:6448", "", "https://wx.b5gw.cn:6443/#/Login", 0, Const.TLSHE, 1, 13, new CXYAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.hongguan, "红馆", "com.hongguang.hg", "http://175.24.154.31", "http://175.24.154.12/", "", 0, Const.TLSHE, 0, 2, new HGAction()));
            mList.add(new Platform(new Params(32000, 35000), R.mipmap.zhiliao, "知了", "com.zhiliao.myapp", "http://27.159.65.16:27008", "http://wo-com.club/Q3WX", "http://27.159.65.16:9000/#/login", 0, Const.TLSHE, 1, 23, new ZLAction()));
            mList.add(new Platform(new Params(32000, 35000), R.mipmap.ruyibao, "如意宝", "com.tech.guo", "http://www.juziliuliangapp.com:27008", "http://wo-com.club/Q3WX", "http://www.juziliuliangapp.com:9000/#/login", 0, Const.TLSHE, 1, 23, new RYBAction()));
            mList.add(new Platform(new Params(32000, 35000), R.mipmap.xiaolu, "小鹿", "com.xmtyq.xl", "http://27.159.65.48:17008", "http://wo-com.club/Q3WX", "http://27.159.65.48:9000/#/login", 0, Const.TLSHE, 1, 23, new XLAction()));
            // mList.add(new Platform(new Params(3000, 5000), R.mipmap.qiandou, "钱逗", "com.qiandou", "http://hsh5110.cn", "http://wo-com.club/Q3WX", "http://hsh5110.cn/mm/b_person_center.asp", 0, Const.TLSHE, 1, 24, new ZQQAction()));
            mList.add(new Platform(new Params(5000, 10000), R.mipmap.jurenqi, "聚人气(派单)", "com.jurenqi", "http://www.811712.com", "http://wo-com.club/Q3WX", "http://sou.811712.com/#/index", 0, Const.TLSHE, 1, 7, new JRQAction()));
            mList.add(new Platform(new Params(32000, 35000), R.mipmap.zuanquanquan, "钻圈圈", "com.lizi.myapp", "http://120.41.41.106:17008", "http://wo-com.club/Q3WX", "", 0, Const.TLSHE, 2, 23, new ZQQAction()));
            mList.add(new Platform(new Params(30000, 35000), R.mipmap.jinniuzuo, "金牛座", "uni.UNI92D001F", "http://m.q22q22.com", "http://wo-com.cljiub/Q3WX", "http://m.q22q22.com/login.html#/", 0, Const.TLSHE, 1, 2, new JNZAction()));
            mList.add(new Platform(new Params(30000, 50000), R.mipmap.xiaoyusan, "小雨伞", "com.licnet.xys", "http://test.58quanzhong.com", "http://m.58quanzhong.com/", "", 0, Const.TLSHE, 0, 3, new XYSAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.gaoyontuan, "高佣团", "uni.UNICF28D01", "http://www.gaoyongtuan.com", "https://fir.im/vq2j", "", 0, Const.TLSHE, 0, 0, new GYTAction()));
            mList.add(new Platform(new Params(5000, 8000), R.mipmap.haizeiwang, "海贼王", "com.dcloud.H504A5Cll", "http://api.91haizeiwang.com", "https://fir.im/1ghc", "", 0, Const.TLSHE, 3, 21, new HZWAction()));
            mList.add(new Platform(new Params(10000, 12000), R.mipmap.yunmeibei, "云美贝", "com.dcloud.JKYXXMCNU", "http://1.yunmeibei.cn", "", "http://1.yunmeibei.cn/index.html", 0, Const.TLSHE, 3, 0, new YMBAction()));
            mList.add(new Platform(new Params(12000, 15000), R.mipmap.wangdianbao, "旺店宝", "com.a2398577387.kfg", "https://api.naomiebie.cn", "", "www.cainiaoqd.com", 0, Const.TLSHE, 2, 0, new WDBAction()));
            ///mList.add(new Platform(new Params(3000, 5000), R.mipmap.heima, "黑马", "com.heima", "http://www.heima911.com", "", "http://www.heima911.com/mm/main", 0, Const.TLSHE, 1, 1, new HMAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.xiaopingguopdd, "小苹果(拼多多)", "io.dcloud.UNIB205D0A", "http://hpg.918money.cn:82", "https://fir.im/s6qm", "", 0, Const.TLSHE, 0, 0, new XPGPDDAction()));
            mList.add(new Platform(new Params(4000, 5000), R.mipmap.xiaozhanggui, "小掌柜", "com.xiaozhanggui", "http://xzg.46137.cn/", "", "http://xzg.707607.cn/login?returnUrl=/", 0, Const.BJSHA, 1, 2, new XZGAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.huanlegou, "欢乐购", "io.dcloud.UNI89500DB", "http://bqd.918money.cn", "https://fir.im/tbrp", "", 0, Const.BJSHA, 0, 0, new HLGAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.taoqiangdan, "淘抢单", "io.dcloud.UNI55AAAAA", "http://tqd.918money.cn:81", "https://fir.im/l5y2", "", 0, Const.BJSHA, 0, 0, new TQDAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.mangguodingdong, "芒果叮咚", "io.dcloud.UNIE7AC320", "http://bdf.918money.cn", "https://fir.im/r7na", "", 0, Const.BJSHA, 0, 0, new MGDDAction()));
            //第二行
            mList.add(new Platform(new Params(5000, 8000), R.mipmap._918renqiwang, "918人气王", "io.dcloud.UNIE9BC8DE", "http://rqw.918money.cn", "https://fir.im/regy", "", 0, Const.BJSHA, 0, 0, new _918RQWAction()));
            mList.add(new Platform(new Params(4000, 5000), R.mipmap._51mangguopai, "51芒果派", "com._51mangguopai", "http://madou.fl1m.cn", "", "http://madou.fl1m.cn/login?returnUrl=/", 0, Const.BJSHA, 1, 2, new _51MGPAction()));
//            //第三行
            mList.add(new Platform(new Params(5000, 8000), R.mipmap.aimi, "爱米", "com.lzy.lovemi", "https://www.huimi123.com", "", "https://www.huimi123.com/login", 0, Const.BJSHA, 0, 3, new AMAction()));
            mList.add(new Platform(new Params(60000, 65000), R.mipmap.baomatuan, "宝妈团", "com.baomatuan", "http://www.rongyao999.cn:8008", "", "http://www.rongyao999.cn:8008/Wap/UserLogin/Login", 0, Const.TLSHE, 1, 13, new BMTAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.baibaizhuan, "白白赚", "com.platform8", "", "", "",0, Const.TLSHE, 0, 3, null));
//            //第四行l
            mList.add(new Platform(new Params(5000, 8000), R.mipmap.dadou, "大豆", "com.dadou", "http://www.am789.top:8888", "", "http://www.ym789.xyz/order-client/page/login/login.html?from=2", 0, Const.BJSHA, 1, 7, new DDAction()));
            mList.add(new Platform(new Params(15000,17000),R.mipmap.damuniuniu, "达姆牛牛", "com.damuniuniu", "http://wx.yrewalib.cn", "", "http://wx.yrewalib.cn/wap/#/",0, Const.TLSHE, 1, 7, new DMNNAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.dasuanceping, "大蒜测评", "com.platform8", "", "", "",0, Const.TLSHE, 0, 8, null));
//           mList.add(new Platform(new Params(3000,5000),R.mipmap.dashixiong, "大师兄", "io.dcloud.UNI1520480", "http://www.5188ab.com", "https://active.clewm.net/BdDBb1?qrurl=https://c3.clewm.net", "",0, Const.TLSHE, 0, 0, new DSXAction()));
//            //第五行
            //mList.add(new Platform(new Params(3000, 5000), R.mipmap.dashuwang, "大树王", "com.dashuwang", "http://www.dashuwong.com", "", "http://www.dashuwong.com/mm/main", 0, Const.TLSHE, 1, 3, new DSWAction()));
            mList.add(new Platform(new Params(10000,10100),R.mipmap.danduoduo, "单多多", "com.danduoduo", "http://api.honghou8.com", "", "http://www.027k8.com/member/index/index.html", 0, Const.TLSHE, 1, 15, new DDDAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.danzhuan, "单赚", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.dingdangmao, "叮当猫", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            //第六行
            mList.add(new Platform(new Params(2000, 3000), R.mipmap.facaishu, "发财树", "com.wangzai.wealth", "http://122.112.163.12:8080", "", "", 0, Const.BJSHA, 0, 2, new FCSAction()));
            mList.add(new Platform(new Params(2000, 3000), R.mipmap.guli, "谷粒", "com.guli", "http://www.gulis.cn", "", "http://www.gulis.cn/Index/", 0, Const.TLSHE, 1, 7, new GLAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.gebiwangshu, "隔壁王叔叔", "com.gebiwanshu", "http://www.haisirui.xin", "", "http://www.haisirui.xin", 0, Const.TLSHE, 1, 7, new GBWSSAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.gongxiangke, "共享客", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            //第七行
//            mList.add(new Platform(new Params(0,0),R.mipmap.huanqiuzhongbang, "环球众帮", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
            mList.add(new Platform(new Params(2500, 4000), R.mipmap.hongseshuiguotan, "红色水果堂", "com.hongseshuiguotan", "http://www.3j5ni.cn/", "", "http://www.3j5ni.cn/index/Apprentice/receive_task", 0, Const.TLSHE, 1, 7, new HSSGTAction()));
            mList.add(new Platform(new Params(500,1000),R.mipmap.huzhudianshang, "互助电商", "com.huzudianshan", "http://20190707.me", "", "http://20190707.me/user/newHome", 0, Const.TLSHE, 1, 7, new DSHZAction()));
//            //第八行
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.jinlizhuan, "锦鲤赚", "com.jinlizuan", "https://jlapi.6ji6.com", "", "https://jlapi.6ji6.com", 0, Const.TLSHE, 1, 7, new JLZAction()));
            mList.add(new Platform(new Params(2000, 3500), R.mipmap.jingdongmaitian, "京东麦田", "com.jingdongmaitian", "http://www.jdmaitian.cn", "", "http://www.jdmaitian.cn/", 0, Const.TLSHE, 1, 1, new JDMTAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.jutaozhan, "聚淘栈", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 5, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.kuaidan, "快单", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 11, null));
//            //第九行
            mList.add(new Platform(new Params(2000, 3000), R.mipmap.kuaike, "快客", "com.kuaike", "http://api.mobile.jerrywl.com", "", "http://m.kke44.com/html/apprenticeLogin.html", 0, Const.TLSHE, 1, 12, new KKAction()));
            mList.add(new Platform(new Params(8000, 10000), R.mipmap.kaixinguo, "开心果", "com.koudaizhuan.ycha", "http://118.89.131.202", "http://118.89.132.110/?uid=hJwfB3&channel=ych", "", 0, Const.BJSHA, 0, 2, new KXGAction()));
            mList.add(new Platform(new Params(1000,2000),R.mipmap.leduoduo, "乐多多", "com.leduoduo", "http://www.zhupw.com", "", "http://www.zhupw.com/index", 0, Const.TLSHE, 1, 13, new LDDAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.linghuomai, "灵活买", "com.linghuomai.app", "https://www.linghuomai.com", "https://fir.im/hxkv", "", 0, Const.BJSHA, 0, 3, new LHMAction()));
//            //第十行
//            mList.add(new Platform(new Params(0,0),R.mipmap.lingqugo, "领趣GO", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 2, null));
////            mList.add(new Platform(new Params(0,0),R.mipmap.lanmao, "懒猫", "com.platform8", "", "https://fir.im/hxkv", "", 0, Const.TLSHE, 0, 0, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.lidu, "力度", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 12, null));
            mList.add(new Platform(new Params(5000, 8000), R.mipmap.muguakeji, "木瓜科技", "com.muguakeji", "http://60.205.186.218:8888", "", "http://60.205.186.218/order-client/page/login/login.html?from=2", 0, Const.TLSHE, 1, 7, new MGKJAction()));
//            //第十一行
            mList.add(new Platform(new Params(3200, 4500), R.mipmap.miaomiaomiao, "喵喵喵", "com.miaomiaomiao", "http://sdjh.daiyun057.cn", "", "http://sdjh.daiyun057.cn/login?returnUrl=/", 0, Const.TLSHE, 1, 2, new MMMAction()));
//            //第十二行
//            mList.add(new Platform(new Params(0,0),R.mipmap.mengxiangjia, "梦想家", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.mingyun, "鸣云", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.maijiale, "麦家乐", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.niurouxuetan, "牛肉学堂", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 10, null));
//            //第十三行
            mList.add(new Platform(new Params(2000, 4000), R.mipmap.ningmengpai, "柠檬派", "com.ningmengpai", "http://nmp.76426.cn", "", "http://nmp.76426.cn/login?returnUrl=/", 0, Const.BJSHA, 1, 2, new NMPAction()));
            mList.add(new Platform(new Params(6000, 8000), R.mipmap.niuyouguo, "牛油果", "io.dcloud.UNI1E9B644", "http://129.211.53.162", "", "http://129.211.53.162/h5/#/", 0, Const.BJSHA, 2, 3, new NYGAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.qingningmeng, "青柠檬", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            //第十四行
//            mList.add(new Platform(new Params(0,0),R.mipmap.panguoguo, "胖果果", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
            mList.add(new Platform(new Params(4000, 5000), R.mipmap.paipaidan, "派派单", "com.paipaidan", "http://ppd.609145.cn", "", "http://ppd.609145.cn", 0, Const.BJSHA, 0, 2, new PPDAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.renqidou, "人气豆", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            //第十五行
//            mList.add(new Platform(new Params(0,0),R.mipmap.renqiyun, "人气云", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 11, null));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.renrenletao, "人人乐淘", "cn.rrletao.app.rrlt", "https://www.rrletao.cn", "", "", 0, Const.BJSHA, 0, 3, new RRLTAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.ruyi, "如意", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 8, null));
            mList.add(new Platform(new Params(500, 1000), R.mipmap.suanluobo, "酸罗卜", "com.suanluobo", "http://lemonbaby.com.cn", "", "http://lemonbaby.com.cn/binding/noBinding", 0, Const.TLSHE, 1, 7, new SLBAction()));

            //            //第十六行
//            mList.add(new Platform(new Params(0,0),R.mipmap.shengduoduo, "升多多", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.m5ipmap.shangke, "尚客", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
            mList.add(new Platform(new Params(60000, 65000), R.mipmap.sifangqian, "私房钱(抢单)", "com.dispatch.homebrew", "http://19sf.cn", "", "http://19sf.cn/main/index", 0, Const.BJSHA, 2, 0, new SFQQDAction()));
            //mList.add(new Platform(new Params(2500,3000),R.mipmap.tianshi, "天时", "com.tianshi", "http://tianshizixun.com", "", "http://tianshizixun.com/user/wap/user.do", 0, Const.TLSHE, 1, 17, new TSAction()));
//            //第十七行
//            mList.add(new Platform(new Params(0,0),R.mipmap.taopaipai, "淘拍拍", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 16, null));
            mList.add(new Platform(new Params(2000, 4000), R.mipmap.wanwanduo, "旺旺多", "com.wanwanduo", "http://dkb.j66q66.com", "", "http://dkb.j66q66.com/login?returnUrl=/", 0, Const.BJSHA, 1, 3, new WWDAction()));
            mList.add(new Platform(new Params(2000, 3000), R.mipmap.xiaojingling, "小精灵", "com.xiaojingling", "http://132.232.124.233", "", "http://132.232.124.233", 0, Const.BJSHA, 1, 7, new XIAOJLAction()));
//            //第十八行
            mList.add(new Platform(new Params(4000, 6000), R.mipmap.xiaoheizhu, "小黑猪", "com.xiaoheizhu", "https://hzapi.ka5a.com", "", "http://hz.ka5a.com/#/login", 0, Const.TLSHE, 1, 7, new XHZAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaotudou, "小土豆", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaopeiqi, "小佩奇", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.xiaobaixiang, "小白象", "com.xiaobaixiang", "http://49.234.145.140", "", "http://49.234.145.140/auth/goLoginPage", 0, Const.TLSHE, 1, 3, new XBXAction()));
//            //第十九行

            mList.add(new Platform(new Params(2000, 3000), R.mipmap.xiaojinli, "小锦鲤", "com.xiaojinli", "http://118.25.168.82", "", "http://118.25.168.82/auth/goLoginPage", 0, Const.BJSHA, 1, 3, new XJLAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaozhushou, "小助手", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaomifengtuiguan, "小蜜蜂推广", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            //第二十行
              mList.add(new Platform(new Params(60000,70000),R.mipmap.xiaoqingwa, "小青蛙", "com.xiaoqingwa", "http://www.shenjimao.com", "", "http://www.shenjimao.com/wap/", 0, Const.TLSHE, 1, 7, new XQWAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaopingguo, "小苹果", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiaogou360, "小狗360", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiandanshenghuo, "闲蛋生活", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 1, null));
//            //第二十一行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xiangchiniurou, "想吃牛肉", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xubaofang, "寻宝房", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xinshijie, "新世界", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.xishuashua, "嘻唰唰", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 7, null));
//            //第二十二行
//            mList.add(new Platform(new Params(0,0),R.mipmap.xinchuangquan, "星创圈", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 17, null));
            //mList.add(new Platform(new Params(500, 800), R.mipmap.xilanhua, "西蓝花", "com.xilanhua.app", "http://www.8933linghuzhe.com", "https://fir.im/174x", "", 0, Const.TLSHE, 0, 3, new LHMAction()));
//              mList.add(new Platform(new Params(0,0),R.mipmap.youjiawang, "优家网", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 1, null));
            mList.add(new Platform(new Params(65000,70000),R.mipmap.yanleduo, "养乐多", "com.yangleduo", "http://wx.cqytjsgc.com", "", "http://wx.cqytjsgc.com/wap", 0, Const.BJSHA, 1, 7, new YLDAction()));
//            //第二十三行
//            mList.add(new Platform(new Params(0,0),R.mipmap.youdanwang, "有单网", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 3, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.youhuicha, "优惠查", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 6, null));
            mList.add(new Platform(new Params(3000, 4000), R.mipmap.youdanzuo, "有单做", "com.youdanzuo", "http://www.127yjs.com", "", "http://www.127yjs.com/", 0, Const.BJSHA, 0, 15, new YDZAction()));
            //          mList.add(new Platform(new Params(0,0),R.mipmap.yueguangbaohe, "月光宝盒", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 8, null));
//            //第二十四行
             mList.add(new Platform(new Params(3000,5000),R.mipmap.zuanlatiao, "赚辣条(抢单)", "com.ktvdos.kkqbfcd", "http://app.51hlz.com", "", "http://www.zhuanlatiao.com/login?back_url=%2F", 0, Const.TLSHE, 2, 0, new ZLTQDAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.yuanjihua, "源计划", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 18, null));
//            mList.add(new Platform(new Params(0,0),R.mipmap.zhaocaizhu, "招财猪", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
            mList.add(new Platform(new Params(60000, 65000), R.mipmap.zhaocaimao, "财神爷", "com.zhaocaimao", "http://zcm.zcm2019.com", "", "http://zcm.zcm2019.com/login?returnUrl=/", 0, Const.BJSHA, 1, 2, new ZCMAction()));
//            //第二十五行
//            mList.add(new Platform(new Params(0,0),R.mipmap.zuankeban, "赚客班", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 13, null));
           mList.add(new Platform(new Params(7000,10000),R.mipmap.koudaijingling, "口袋精灵", "com.qazapp.buyer", "http://app.zhuanyoumi.com", "https://www.zhuanyoumi.com/webshow.htm", "", 0, Const.BJSHA, 0, 2, new KDJLAction()));
//            mList.add(new Platform(new Params(0,0),R.mipmap.ziningmeng, "紫柠檬", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 16, null));
            mList.add(new Platform(new Params(15000,17000),R.mipmap.zhangquanzhong, "涨权重", "com.zhangquanzhon", "http://wx.99liuping.com", "", "http://wx.99liuping.com/wap/", 0, Const.BJSHA, 1, 7, new ZQZAction()));
//            //第二十六行
//            mList.add(new Platform(new Params(0,0),R.mipmap.zhishengji, "直升机", "com.platform8", "", "", "", 0, Const.TLSHE, 0, 19, null));
              mList.add(new Platform(new Params(15000,20000),R.mipmap.taobaoquan, "淘宝圈", "com.platform8", "http://www.tbquan88.com", "", "http://www.tbquan88.com/1/user#", 0, Const.TLSHE, 1, 7, new TBQAction()));
//            //第二十七行
//
//            /******************************************   打款平台   **************************************************/
//            //第一行
            mList.add(new Platform(new Params(2500, 4000), R.mipmap.duoduohuayuan, "多多花苑", "com.duoduohuayuan", "http://api.xdhfnch.cn:18082", "", "http://wx.xdhfnch.cn:8081/workerLogin", 1, Const.BJSHA, 1, 20, new DDHYAction()));
            mList.add(new Platform(new Params(3000, 4000), R.mipmap.fongshoudamai, "丰收大麦", "com.fongshoudamai", "http://api.fsdmff.cn", "", "http://wx.fsdmff.cn:8081/workerLogin", 1, Const.BJSHA, 1, 20, new FSDMAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.jiuxingkeji, "九鑫科技", "com.jiuxingkeji", "http://jx.xmaylt.cc:8080", "", "http://jx.xmaylt.cc:8080/workerLogin", 1, Const.BJSHA, 1, 7, new JXKJAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.hongchangkeji, "鸿昌科技", "com.hongchangkeji", "http://134.175.32.52:8080", "", "http://134.175.32.52:8080/workerLogin", 1, Const.BJSHA, 1, 7, new HCKJAction()));//            //第二行
            mList.add(new Platform(new Params(10000, 12000), R.mipmap.jinxiuleyuan, "响叮当", "com.jinxiuleyuan", "https://api.qvmqfle.cn:8442", "", "https://wx.qvmqfle.cn/#/Login", 1, Const.BJSHA, 1, 13, new JXLYAction()));
            mList.add(new Platform(new Params(2500, 4000), R.mipmap.ningmengshu, "柠檬树", "com.ningmengshu", "http://shimokj.com:8080", "", "http://shimokj.com/user/#/login", 1, Const.TLSHE, 1, 7, new NMSAction()));
            mList.add(new Platform(new Params(3000, 4000), R.mipmap.maomaomi, "猫猫咪打款", "com.maomaomi", "http://www.wushanba.com", "", "http://www.wushanba.com/wap/Login.aspx", 1, Const.TLSHE, 1, 1, new MMMDkAction()));
            mList.add(new Platform(new Params(2500, 4000), R.mipmap.qiutianleyuan, "励志心情(秋天)", "com.qiutianleyuan", "http://api.pk1165.com:8080", "", "http://wx.pk1165.com:8081/workerLogin", 1, Const.BJSHA, 1, 20, new QTLYAction()));
//            //第三行
////            mList.add(new Platform(new Params(0,0),R.mipmap.qishikeji, "骑士科技", "com.platform8", "", "", "", 1, Const.TLSHE, 0, 7, null));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.sanmukeji, "三木科技", "com.sanmukeji", "http://fjsmkj.cn:8080", "", "http://fjsmkj.cn/user/#/Task", 1, Const.BJSHA, 1, 15, new SMKJAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.siyecao, "四叶草", "com.siyecao", "http://xtg.tenggang.net:8080", "", "http://xtg.tenggang.net:8080/workerLogin", 1, Const.BJSHA, 1, 7, new SYCAction()));
//            //第四行
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.tianxileyuan, "天玺乐园", "com.tianxileyuan", "http://cs.8818s.cn:8080", "", "http://cs.8818s.cn/user/#/login", 1, Const.BJSHA, 1, 7, new TXLYAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.teshuakeji, "特刷科技", "com.teshuakeji", "http://yc.xmaylt.cc:8080", "", "http://yc.xmaylt.cc:8080/workerLogin", 1, Const.BJSHA, 1, 7, new TSKJAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.taomimi, "淘米米", "com.taomimi", "http://taomimi8.com:8080", "", "http://taomimi8.com/user/#/Task", 1, Const.BJSHA, 1, 7, new TMMAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.xiaomache, "小马车", "com.xiaomache", "http://lvyuanfengdu.cn:8080", "", "http://lvyuanfengdu.cn/user/#/Task", 1, Const.BJSHA, 1, 15, new XMCAction()));
//            //第四行
//            mList.add(new Platform(new Params(2500,3000),R.mipmap.weitaokeji, "微淘科技", "com.weitaokeji", "http://macworksdc.com:8080", "", "http://macworksdc.com:8080/workerLogin", 1, Const.TLSHE, 0, 7, new WTKJAction()));
            mList.add(new Platform(new Params(2500, 4000), R.mipmap.xiaomifengdakuang, "打款小蜜蜂", "com.dakuanxiaomifeng", "http://api.g49l0.cn:1180", "", "http://wx.g49l0.cn:8080/workerLogin", 1, Const.BJSHA, 1, 20, new DKXMFAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.xiaolanshu, "小蓝书", "com.xiaolanshu", "http://maibaikj.cn:8080", "", "http://maibaikj.cn/user/#/Login", 1, Const.TLSHE, 1, 7, new XLSAction()));
            //mList.add(new Platform(new Params(3000, 5000), R.mipmap.zhengfuzhe, "征服者(抢单)", "com.zhengfuz.app", "http://app.zhengfuz.com", "", "", 1, Const.BJSHA, 0, 7, new ZFZAction()));
            mList.add(new Platform(new Params(10000, 10100), R.mipmap.zhengfuzhe, "征服者", "com.zhengfuz.app.xmx5zak", "http://app.zhengfuz.com/", "", "", 1, Const.BJSHA, 0, 15, new ZFZPDAction()));
            mList.add(new Platform(new Params(8000, 8100), R.mipmap.xiaomogu, "小蘑菇", "com.xiaomogu", "http://yuntao.zhengfuz.com", "", "http://yuntao.zhengfuz.com/iop/web/logionapp.html", 1, Const.BJSHA, 1, 15, new XMGAction()));
            mList.add(new Platform(new Params(2500, 3000), R.mipmap.kadingche, "卡丁车", "com.kadingche", "http://106.53.20.113:8080", "", "http://cxxychuangxin.cn/user/", 1, Const.BJSHA, 1, 12, new KDCAction()));
            //mList.add(new Platform(new Params(3000, 4000), R.mipmap.dianjinyiyou, "电竞艺游(抢单)", "com.dianjinyiyou", "http://xmt.51zugeju.com", "", "http://xmt.51zugeju.com/iop/index/index", 1, Const.TLSHE, 1, 15, new DJYYAction()));
            mList.add(new Platform(new Params(6000, 8000), R.mipmap.dianjinyiyou, "电竞艺游", "com.dianjinyiyouPD", "http://xmt.51zugeju.com", "", "http://xmt.51zugeju.com/iop/index/index", 1, Const.TLSHE, 1, 15, new DJYYPDAction()));
            mList.add(new Platform(new Params(20000, 25000), R.mipmap.meiliriji, "美丽日记", "com.meiliriji", "http://www.838304.cn", "", "http://www.838304.cn/home/index.html#/", 1, Const.TLSHE, 1, 15, new MLRJAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.maitian, "麦田", "io.dcloud.yuji1548", "http://www.sswxt.com/com", "https://fir.im/k2tw", "", 1, Const.TLSHE, 3, 22, new MTAction()));
            mList.add(new Platform(new Params(8000, 10000), R.mipmap.zhaizai, "宅仔", "com.zhaizai", "http://xk.51zugeju.com", "", "http://xk.51zugeju.com/iop/web/logionapp.html", 1, Const.TLSHE, 1, 15, new ZZAction()));
           mList.add(new Platform(new Params(3000, 5000), R.mipmap.yijingjin, "易筋经", "com.yijingjin", "https://yijingjin.club", "", "https://yijingjin.club/", 1, Const.TLSHE, 1, 9, new YJJAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.jiqimao, "机器猫", "com.jiqimao", "http://xiao.toponeculture.xyz/", "", "http://xiao.toponeculture.xyz/home/web/login.html", 1, Const.TLSHE, 1, 15, new JQMAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.naonao, "闹闹", "io.ionic.wanwanshuadanptai", "https://bd.huaweixin.net/", "", "http://bd.huaweixin.net/sosform/upfile.php", 1, Const.TLSHE, 2, 3, new NNAction()));
            //mList.add(new Platform(new Params(3000, 5000), R.mipmap.xiaohouzi, "小猴子", "com.xiaohouzi", "https://lm.da-k.com/", "", "https://lm.da-k.com/index/login.html", 1, Const.TLSHE, 1, 15, new XIAOHZAction()));
            mList.add(new Platform(new Params(3000, 5000), R.mipmap.xixikeji, "熙喜科技", "com.zhengfuz.app", "http://xishuashua.51zugeju.com", "", "http://xishuashua.51zugeju.com/iop/index/index", 1, Const.TLSHE, 0, 15, new XXKJAction()));

            initLatestStatus();
        }
        return mList;
    }

    /**
     * 初始化（还原）常用平台的初始状态
     */
    private static void initLatestStatus() {
        List<Platform> list = getLatestPlaforms();
        for (int i = 0, len = list.size(); i < len; i++) {
            Platform platform = list.get(i);
            platform.setStatus(platform.getOriginalStatus());   //还原初始状态
            list.set(i, platform);
        }
        DbUtil.update(list);
    }

    public static Map<String, Integer> getPlatformIcons() {
        Map<String, Integer> map = new HashMap<>();
        map.put("51mangguopai", R.mipmap._51mangguopai);
        map.put("wangdianbao", R.mipmap.wangdianbao);
        map.put("xiaopingguopdd", R.mipmap.xiaopingguopdd);
        map.put("aimi", R.mipmap.aimi);
        map.put("baomatuan", R.mipmap.baomatuan);
        map.put("dadou", R.mipmap.dadou);
        map.put("damuniuniu", R.mipmap.damuniuniu);
        map.put("danduoduo", R.mipmap.danduoduo);
        map.put("danzuan", R.mipmap.danzhuan);
        map.put("dashixiong", R.mipmap.dashixiong);
        map.put("dasuanceping", R.mipmap.dasuanceping);
        map.put("dashuwang", R.mipmap.dashuwang);
        map.put("dingdanmao", R.mipmap.dingdangmao);
        map.put("duoduohuayuan", R.mipmap.duoduohuayuan);
        map.put("facaishu", R.mipmap.facaishu);
        map.put("fongshoudamai", R.mipmap.fongshoudamai);
        map.put("gebiwanshushu", R.mipmap.gebiwangshu);
        map.put("guli", R.mipmap.guli);
        map.put("hongchangkeji", R.mipmap.hongchangkeji);
        map.put("hongseshuiguotan", R.mipmap.hongseshuiguotan);
        map.put("huanqiuzonbang", R.mipmap.huanqiuzhongbang);
        map.put("huzudianshan", R.mipmap.huzhudianshang);
        map.put("jingdongmaitian", R.mipmap.jingdongmaitian);
        map.put("jinlizhuan", R.mipmap.jinlizhuan);
        map.put("jinlizuan", R.mipmap.jinlizhuan);
        map.put("jinxiuleyuan", R.mipmap.jinxiuleyuan);
        map.put("jiuxingkeji", R.mipmap.jiuxingkeji);
        map.put("kaixinguo", R.mipmap.kaixinguo);
        map.put("kuaidan", R.mipmap.kuaidan);
        map.put("kuaike", R.mipmap.kuaike);
        map.put("lanmao", R.mipmap.lanmao);
        map.put("leduoduo", R.mipmap.leduoduo);
        map.put("linghuomai", R.mipmap.linghuomai);
        map.put("logo", R.mipmap.logo);
        map.put("maijiale", R.mipmap.maijiale);
        map.put("maomaomi", R.mipmap.maomaomi);
        map.put("maotouying", R.mipmap.maotouying);
        map.put("mengxiangjia", R.mipmap.mengxiangjia);
        map.put("miaomiaomiao", R.mipmap.miaomiaomiao);
        map.put("mingyun", R.mipmap.mingyun);
        map.put("muguakeji", R.mipmap.muguakeji);
        map.put("ningmengpai", R.mipmap.ningmengpai);
        map.put("ningmengshu", R.mipmap.ningmengshu);
        map.put("niurouxuetan", R.mipmap.niurouxuetan);
        map.put("niuyouguo", R.mipmap.niuyouguo);
        map.put("paipaidan", R.mipmap.paipaidan);
        map.put("panguoguo", R.mipmap.panguoguo);
        map.put("qingningmeng", R.mipmap.qingningmeng);
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
        map.put("teshuakeji", R.mipmap.teshuakeji);
        map.put("tianshi", R.mipmap.tianshi);
        map.put("tianxileyuan", R.mipmap.tianxileyuan);
        map.put("tiemayi", R.mipmap.tiemayi);
        map.put("wanwanduo", R.mipmap.wanwanduo);
        map.put("weitaokeji", R.mipmap.weitaokeji);
        map.put("xiandanshenghuo", R.mipmap.xiandanshenghuo);
        map.put("xiangchiniurou", R.mipmap.xiangchiniurou);
        map.put("xiaobaixiang", R.mipmap.xiaobaixiang);
        map.put("xiaogou360", R.mipmap.xiaogou360);
        map.put("xiaoheizhu", R.mipmap.xiaoheizhu);
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
        map.put("xinchuangquan", R.mipmap.xinchuangquan);
        map.put("xinshijie", R.mipmap.xinshijie);
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
        map.put("mangguodingdong", R.mipmap.mangguodingdong);
        map.put("kadingche", R.mipmap.kadingche);
        return map;
    }

}
