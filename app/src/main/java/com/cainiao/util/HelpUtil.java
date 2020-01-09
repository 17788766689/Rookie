package com.cainiao.util;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.Key;

import javax.crypto.Cipher;

public class HelpUtil {
    public static String encrypt(String arg5, String arg6) {
        String v0 = "";
        String v1 = "\n";
        try {
            PublicKey v6 = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(arg6.replaceAll(v1, v0), 0)));
            Cipher v3 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            v3.init(1, ((Key)v6));
            return Base64.encodeToString(v3.doFinal(arg5.getBytes("UTF-8")), 0).replaceAll(v1, v0);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}


