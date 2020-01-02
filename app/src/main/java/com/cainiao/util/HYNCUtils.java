package com.cainiao.util;

import android.support.v4.view.InputDeviceCompat;
import android.util.Base64;

import java.security.MessageDigest;
import java.util.Date;

public class HYNCUtils {
    static String keyd;
    static byte[] strbuf;

    public static String getcryptkey(String arg7, String arg8) {
        System.out.println(arg7+arg8);
        byte[] v3_2;
        int v4_1;
        arg8 = HYNCUtils.md5(arg8);
        System.out.println(arg8);
        int v1 = 0;
        String v2 = HYNCUtils.md5(arg8.substring(0, 16));
        arg8 = HYNCUtils.md5(arg8.substring(16, 32));
        HYNCUtils.keyd = new HYNCUtils().getcryptkey();
        StringBuilder v3 = new StringBuilder();
        v3.append(v2);
        StringBuilder v4 = new StringBuilder();
        v4.append(v2);
        v4.append(HYNCUtils.keyd);
        v3.append(HYNCUtils.md5(v4.toString()));
        v2 = v3.toString();
        v3 = new StringBuilder();
        v3.append("0000000000");
        v4 = new StringBuilder();
        v4.append(arg7);
        v4.append(arg8);
        v3.append(HYNCUtils.md5(v4.toString()).substring(0, 16));
        v3.append(arg7);
        HYNCUtils.strbuf = v3.toString().getBytes();
        int v7 = 256;
        int[] v8 = new int[v7];
        int v0;
        for(v0 = 0; v0 < v7; ++v0) {
            v8[v0] = v0;
        }

        int[] v0_1 = new int[v7];
        int v3_1;
        for(v3_1 = 0; v3_1 < v7; ++v3_1) {
            v0_1[v3_1] = v2.charAt(v3_1 % v2.length());
        }

        int v2_1 = 0;
        v3_1 = 0;
        while(v2_1 < v7) {
            v3_1 = (v3_1 + v8[v2_1] + v0_1[v2_1]) % v7;
            v4_1 = v8[v2_1];
            v8[v2_1] = v8[v3_1];
            v8[v3_1] = v4_1;
            ++v2_1;
        }

        v0 = 0;
        v2_1 = 0;
        while(true) {
            v3_2 = HYNCUtils.strbuf;
            if(v1 >= v3_2.length) {
                break;
            }

            v0 = (v0 + 1) % v7;
            v2_1 = (v2_1 + v8[v0]) % v7;
            v4_1 = v8[v0];
            v8[v0] = v8[v2_1];
            v8[v2_1] = v4_1;
            v3_2[v1] = ((byte)(v3_2[v1] ^ v8[(v8[v0] + v8[v2_1]) % v7]));
            ++v1;
        }

        arg7 = Base64.encodeToString(v3_2, 2);
        return HYNCUtils.keyd + arg7;
    }

    public String getcryptkey() {
        long v0 = new Date().getTime();
        long v4 = v0 / 1000;
        StringBuilder v6 = new StringBuilder();
        double v0_1 = ((double)(v0 - 1000 * v4));
        Double.isNaN(v0_1);
        v6.append(String.valueOf(v0_1 / 1000));
        v6.append(' ');
        v6.append(v4);
        return HYNCUtils.md5(v6.toString()).substring(28);
    }

    public static String md5(String str) {
        String str2 = "";
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes("UTF8"));
            byte[] bytes = instance.digest();
            String str3 = str2;
            for (byte b : bytes) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str3);
                stringBuilder.append(Integer.toHexString((b & 255) | InputDeviceCompat.SOURCE_ANY).substring(6));
                str3 = stringBuilder.toString();
            }
            return str3;
        } catch (Exception str4) {
            str4.printStackTrace();
            return str2;
        }
    }


}
