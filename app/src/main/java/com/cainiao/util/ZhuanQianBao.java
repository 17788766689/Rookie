package com.cainiao.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.Security;


public class ZhuanQianBao{

	private static String a = "N8Oz2QQtMMKyDKW6OMcy2e";
    private static String key = "7C3EFBEC";
    private static String iv = "6DC35F56";

    //设置java支持PKCS7Padding
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception{

        //nonce: 7304BD3BB8A12A812CB32A2D76DAC08D -> 069F78FE
//        String content = "7304BD3BB8A12A812CB32A2D76DAC08D";
//        String content = "130D9E275DDAC8FCB50E5029FA894D814470C37918F7F98767B660B9085E48E39AAF55980DA8B5C76C2DE4ED06C8B0A0B0CE0523F218349C6C2A527803A7F29CDADBDC0DE885B1CB5391C5B5C850BE8B3CF820F00AE378DB";
//        String timestamp = "1590236162072";
//        String nonce = "069F78FE";
        String method = "com.homebrew.login";
        String userName = "15074980383";
        String pwd = "zhang123";  //原始密码
        String password = Utils.md5(pwd);  //将原始密码经过md5加密后的密码
        String content = "{\"username\":\"" + userName + "\",\"password\":\"" + password + "\",\"code\":\"\"}";
        System.out.println("content: " + axiosApi(method, content));
    }


    public static String axiosApi(String method, String data){
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = rndHexString(8);
        String md5 = Utils.md5(a + method + data + timestamp + nonce);
        nonce = encrypt(nonce, key, iv);  //注意要转成大写
        String content = encrypt(data, key, iv);  //注意要转成大写
        String returnMsg = "&method=" + method + "&content=" + content + "&timestamp=" + timestamp + "&sign=" + md5 + "&nonce=" + nonce;
        return returnMsg;
    }


    /**
     * 随机生成 num 位字符串
     * @param num
     * @return
     */
    private static String rndHexString(int num){
        String chars = "ABCDEF1234567890";
        int max = chars.length();
        String str = "";
        for(int i = 0; i < num; i++){
            str += chars.charAt((int)Math.floor(Math.random() * max));
        }
        return str;
    }


    //获取加密或解密的Cipher对象：负责完成加密或解密工作
    private static Cipher GetCipher(int opmode , String key, String iv) {
        try {
            //根据传入的秘钥内容生成符合DES加密解密格式的秘钥内容
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            //获取DES秘钥生成器对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 生成秘钥：key的长度不能够小于8位字节
            Key secretKey = keyFactory.generateSecret(dks);
            //获取DES/ECB/PKCS7Padding该种级别的加解密对象
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS7Padding");
            //初始化加解密对象【opmode:确定是加密还是解密模式；secretKey是加密解密所用秘钥】
            cipher.init(opmode, secretKey, new IvParameterSpec(iv.getBytes()));
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

        /**
      * DES算法，加密
      * 
      * @param data
      *            待加密字符串
      * @param key
      *            加密私钥，长度不能够小于8位
      * @return 加密后的字节数组，一般结合Base64编码使用
      * @throws InvalidAlgorithmParameterException
      * @throws Exception
      */
        public static String encrypt(String data , String key, String iv) {
            if (data == null || data.isEmpty()) return null;
            try {
                //获取加密对象【Cipher.ENCRYPT_MODE：指定加密模式为1】
                Cipher cipher = GetCipher(Cipher.ENCRYPT_MODE, key, iv);
                if (cipher == null) {
                    return null;
                } else {
                    //设置加密的字符串为utf-8模式并且加密，返回加密后的byte数组。
                    byte[] byteHex = cipher.doFinal(data.getBytes("UTF-8"));
                    return byteToHexString(byteHex);//对加密后的数组进制转换
                }
            } catch (Exception e) {
                e.printStackTrace();
            return data;
            }
        }

        /**
      * DES算法，解密
      * 
      * @param data
      *            待解密字符串
      * @param key
      *            解密私钥，长度不能够小于8位
      * @return 解密后的字节数组
      * @throws Exception
      * @throws Exception
      *             异常
      */
        public static String decrypt(String data , String key, String iv) throws Exception {
            if (data == null || data.isEmpty()) return null;
            try {
                //先把待解密的字符串转成Char数组类型，然后进行进制转换。
                byte[] b = decodeHex(data);
                //获取解密对象【Cipher.DECRYPT_MODE：指定解密模式为2】
                Cipher cipher = GetCipher(Cipher.DECRYPT_MODE, key, iv);
                if (cipher != null)
                    //进行解密返回utf-8类型的字符串
                    return new String(cipher.doFinal(b), "UTF-8");
                else
                    return null;
            } catch (Exception e) {
                e.printStackTrace();
                return data;
            }
        }

    /**
     * 字符串转成字节流
     */
    private static byte[] decodeHex(String src) {
        int m = 0, n = 0;
        int byteLen = src.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
            ret[i] = Byte.valueOf((byte)intVal);
        }
        return ret;
    }

     public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
        sTemp = Integer.toHexString(0xFF & bytes[i]);
        if (sTemp.length() < 2)
            sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

}

