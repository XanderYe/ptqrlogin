package cn.xanderye.qq;

import cn.xanderye.util.QQUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yezhendong
 * @description:
 * @date 2021/4/6 9:25
 */
public class QQCookie {

    private static final QQCookie QQ_COOKIE = new QQCookie();

    private String pSkey;

    private String pUin;

    private String skey;

    private String uin;

    private QQCookie() {

    }

    public static QQCookie getInstance() {
        return QQ_COOKIE;
    }

    public String getpSkey() {
        return pSkey;
    }

    public void setpSkey(String pSkey) {
        this.pSkey = pSkey;
    }

    public String getpUin() {
        return pUin;
    }

    public void setpUin(String pUin) {
        this.pUin = pUin;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getQQ() {
        return QQUtil.uinToQQ(uin);
    }

    @Override
    public String toString() {
        return "QQCookie{" +
                "pSkey='" + pSkey + '\'' +
                ", pUin='" + pUin + '\'' +
                ", skey='" + skey + '\'' +
                ", uin='" + uin + '\'' +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("p_skey", pSkey);
        map.put("p_uin", pUin);
        map.put("skey", skey);
        map.put("uin", uin);
        return map;
    }
}
