package cn.xanderye.qq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置
 * @author XanderYe
 * @description:
 * @date 2021/4/12 10:16
 */
public class Config {
    public static final Config CONFIG = new Config();

    private String appId;

    private String daid;

    private String u1;

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CONFIG.setAppId(p.getProperty("qq.app_id"));
        CONFIG.setDaid(p.getProperty("qq.daid"));
        CONFIG.setU1(p.getProperty("qq.u1"));
    }

    private Config() {
    }

    public static Config getInstance() {
        return CONFIG;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDaid() {
        return daid;
    }

    public void setDaid(String daid) {
        this.daid = daid;
    }

    public String getU1() {
        return u1;
    }

    public void setU1(String u1) {
        this.u1 = u1;
    }
}
