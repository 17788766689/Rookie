package com.cainiao.util;

public final class Base32 {
    private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] base32Lookup = new int[]{255, 255, 26, 27, 28, 29, 30, 31, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 255, 255, 255, 255, 255, 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 255, 255, 255, 255, 255};

    private Base32() {
    }

    public static String encode(byte[] bytes) {
        int i = 0;
        int index = 0;
        StringBuffer base32 = new StringBuffer(((bytes.length + 7) * 8) / 5);
        while (i < bytes.length) {
            int digit;
            int currByte = bytes[i] >= (byte) 0 ? bytes[i] : bytes[i] + 256;
            if (index > 3) {
                int nextByte = i + 1 < bytes.length ? bytes[i + 1] >= (byte) 0 ? bytes[i + 1] : bytes[i + 1] + 256 : 0;
                digit = currByte & (255 >> index);
                index = (index + 5) % 8;
                digit = (digit << index) | (nextByte >> (8 - index));
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 31;
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
            base32.append(base32Chars.charAt(digit));
        }
        return base32.toString();
    }
}


