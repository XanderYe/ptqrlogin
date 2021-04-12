package cn.xanderye.util;

public class QQUtil {

    public static String hash33(String qrsig) {
        int e = 0;
        for (int i = 0; i < qrsig.length(); i++) {
            e += (e << 5) + qrsig.charAt(i);
        }
        return String.valueOf(2147483647 & e);
    }

    public static String getGTK(String skey) {
        int hash = 5381;
        for (int i = 0; i < skey.length(); ++i) {
            hash += (hash << 5) + skey.charAt(i);
        }
        return String.valueOf(hash & 0x7fffffff);
    }

    public static String uinToQQ(String uin) {
        int startIndex = 1;
        for (int i = 1; i < uin.length(); i++) {
            if (uin.charAt(i) != '0') {
                break;
            }
            startIndex++;
        }
        return uin.substring(startIndex);
    }
}
