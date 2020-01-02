package com.cainiao.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;

import com.cainiao.base.MyApp;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static char sHexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String deviceId = Const.DEFAULT_ID;

    public static void setDeviceId(String id) {
        deviceId = id;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取：Pseudo-Unique ID, 这个在任何Android手机中都有效
     *
     * @return
     */
    public static String getUuid() {
        DeviceUtil.getInstance().init(MyApp.getContext());
        String android = DeviceUtil.getInstance().getDeviceId();
        return md5(android + deviceId);
    }

    /**
     * @param arg3
     * @param arg4
     * @return SHA加密
     */
    public static String HMACSHA256(String arg3, String arg4) throws Exception {
        String v0 = "HmacSHA256";
        try {
            Mac v1 = Mac.getInstance(v0);
            v1.init(new SecretKeySpec(arg4.getBytes(), v0));
            arg3 = Base64.encodeToString(v1.doFinal(arg3.getBytes("UTF-8")), 2);
        } catch (Exception ex) {
            arg3 = "";
        }
        return arg3;
    }

    /**
     * @param arg5
     * @return 随机数
     */
    public static String randomString(int arg5) {
        Random v0 = new Random();
        StringBuffer v1 = new StringBuffer();
        int v2;
        for (v2 = 0; v2 < arg5; ++v2) {
            v1.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(v0.nextInt(62)));
        }

        return v1.toString();
    }

    /**
     * md5加密
     *
     * @param source
     * @return
     */
    public static String md5(String source) {
        try {
            byte[] bytes = source.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            md.update(bytes);
            // 获得密文
            byte[] mdBytes = md.digest();
            // 把密文转换成十六进制的字符串形式
            int length = mdBytes.length;
            char[] chars = new char[length * 2];
            int k = 0;
            for (int i = 0; i < length; i++) {
                byte byte0 = mdBytes[i];
                chars[k++] = sHexDigits[byte0 >>> 4 & 0xf];
                chars[k++] = sHexDigits[byte0 & 0xf];
            }
            return new String(chars);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将str内容复制到剪贴板
     *
     * @param str
     */
    public static void setClipboardStr(String str) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) MyApp.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", str);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    /**
     * 判断str是否是整数
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        return TextUtils.isEmpty(str) ? false : str.matches("[0-9]+");
    }

    public static int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, MyApp.getContext().getResources().getDisplayMetrics());
    }

    /**
     * 获取时间戳 URL签名
     *
     * @param url
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getTimestampUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";
        String encryptKey = "d198309bbdb905b80f99ccbafb99ce9bb6b8b298";
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
        String path = urlObj.getPath();

        long timestampNow = System.currentTimeMillis() / 1000 + 3600;
        String expireHex = Long.toHexString(timestampNow);

        String toSignStr = String.format("%s%s%s", encryptKey, path, expireHex);
        String signedStr = md5(toSignStr);

        String signedUrl = null;
        if (urlObj.getQuery() != null) {
            signedUrl = String.format("%s&sign=%s&t=%s", url, signedStr, expireHex);
        } else {
            signedUrl = String.format("%s?sign=%s&t=%s", url, signedStr, expireHex);
        }

        return signedUrl;
    }

    /**
     * 禁用掉Xposed
     */
    public static void disable() {
        try {
            Field field = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedBridge")
                    .getField("disableHooks");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getRandomNickname(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }

    public static String parmEncryption() {
        String timestampStr = String.valueOf(System.currentTimeMillis() / 1000);
        if (timestampStr.length() == 10) {
            timestampStr = timestampStr.substring(5) + timestampStr.substring(0, 5);
        }
        String beforeRandomStr = getRandomNum(4);
        return getBase32encoding(beforeRandomStr + timestampStr + getRandomNum(6));
    }

    public static String getRandomNum(int num) {
        String dataCode = "";
        for (int i = 0; i < num; i++) {
            dataCode = dataCode + String.valueOf(Math.round(Math.random() * 9.0d));
        }
        return dataCode;
    }

    public static String getBase32encoding(String originString) {
        return Base32.encode(originString.getBytes());
    }


}
