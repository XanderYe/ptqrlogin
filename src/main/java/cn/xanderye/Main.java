package cn.xanderye;

import cn.xanderye.entity.Friend;
import cn.xanderye.util.HttpUtil;
import cn.xanderye.util.QQUtil;
import cn.xanderye.util.StatisticUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author XanderYe
 * @date 2020/2/6
 */
public class Main extends Application {

    public final static String UIN = "uin";
    public final static String SKEY = "skey";

    private static String qrsig = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Map<String, String> qrCode = getQrCode();
        if (qrCode != null) {
            String qrPath = qrCode.get("qrPath");
            qrsig = qrCode.get("qrsig");
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(70, 30, 50, 30));
            Image image = new Image("file:" + qrPath);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(111);
            imageView.setFitHeight(111);
            root.setTop(imageView);
            BorderPane.setAlignment(imageView, Pos.BOTTOM_CENTER);
            Label label = new Label("请扫码登录");
            root.setBottom(label);
            BorderPane.setAlignment(label, Pos.TOP_CENTER);
            primaryStage.setTitle("QQ扫码登录");
            primaryStage.setScene(new Scene(root, 300, 300));

            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> System.exit(-1));
            schedule(primaryStage, imageView);
        } else {
            System.out.println("图片获取失败");
        }
    }

    /**
     * 获取二维码
     *
     * @return void
     * @author XanderYe
     * @date 2020-03-14
     */
    private Map<String, String> getQrCode() {
        Map<String, String> qrCode = new HashMap<>();
        String t = Double.toString(Math.random());
        String url = "https://ssl.ptlogin2.qq.com/ptqrshow?appid=549000912&e=2&l=M&s=3&d=72&v=4&t=" + t + "&daid=5&pt_3rd_aid=0";
        byte[] data = HttpUtil.download(url, null, null);
        if (data != null && data.length > 0) {
            Map<String, String> cookies = HttpUtil.getCookies();
            if (cookies != null) {
                String qrsig = cookies.get("qrsig");
                String userPath = System.getProperty("user.dir");
                String qrPath = userPath + File.separator + "qrcode.png";
                try {
                    FileImageOutputStream imageOutput = new FileImageOutputStream(new File(qrPath));
                    imageOutput.write(data, 0, data.length);
                    imageOutput.close();
                    qrCode.put("qrPath", qrPath);
                    qrCode.put("qrsig", qrsig);
                    return qrCode;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 定时器
     *
     * @param primaryStage
     * @param imageView
     * @return void
     * @author XanderYe
     * @date 2020-03-14
     */
    private void schedule(Stage primaryStage, ImageView imageView) {
        AtomicBoolean isLogin = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
        // 开一个线程挂后台
        executorService.execute(() -> {
            // 定时检查是否登录
            scheduledService.scheduleAtFixedRate(() -> {
                if (loginListener(imageView)) {
                    // 标志登录
                    isLogin.set(true);
                    // 隐藏窗口
                    Platform.runLater(primaryStage::hide);
                    // 结束定时器
                    scheduledService.shutdown();
                }
            }, 0, 3, TimeUnit.SECONDS);
            try {
                scheduledService.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 判断是否已登录
            if (isLogin.get()) {
                Map<String, String> cookies = HttpUtil.getCookies();
                if (cookies != null) {
                    String uin = cookies.get(UIN);
                    String skey = cookies.get(SKEY);
                    if (StringUtils.isEmpty(uin) || StringUtils.isEmpty(skey)) {
                        System.out.println("cookies获取失败");
                    } else {
                        statistic(uin, skey);
                    }
                } else {
                    System.out.println("cookies获取失败");
                }
            }
        });
        executorService.shutdown();
    }

    /**
     * 登录监听
     * @param imageView
     * @return boolean
     * @author XanderYe
     * @date 2020-03-14
     */
    private boolean loginListener(ImageView imageView) {
        String ptqrtoken = QQUtil.hash33(qrsig);
        String action = "0-1-" + System.currentTimeMillis();
        String url = "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=https%3A%2F%2Fqzs.qq.com%2Fqzone%2Fv5%2Floginsucc.html%3Fpara%3Dizone&ptqrtoken=" + ptqrtoken + "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=" + action + "&js_ver=20021917&js_type=1&login_sig=&pt_uistyle=40&aid=549000912&daid=5&has_onekey=1";
        Map<String, Object> cookies = new HashMap<>();
        cookies.put("qrsig", qrsig);
        String result = HttpUtil.doPostWithCookie(url, null, cookies, null);
        if (result.contains("二维码未失效")){
            System.out.println("二维码未失效");
        } else if (result.contains("二维码已失效")) {
            System.out.println("二维码已失效， 重新生成");
            Map<String, String> qrCode = getQrCode();
            if (qrCode != null) {
                imageView.setImage(new Image("file:" + qrCode.get("qrPath")));
                qrsig = qrCode.get("qrsig");
            }
        } else if (result.contains("登录成功")) {
            return true;
        } else {
            System.out.println(result);
        }
        return false;
    }

    /**
     * 统计方法
     *
     * @param uin
     * @param skey
     * @return void
     * @author XanderYe
     * @date 2020-03-14
     */
    private void statistic(String uin, String skey) {
        int type = 1;
        System.out.print("请输入统计类型（1：好友；2：附近的人）：");
        Scanner scanner = new Scanner(System.in);
        String tp = scanner.nextLine();
        try {
            int t = Integer.parseInt(tp);
            if (t == 1 || t == 2) {
                type = t;
            }
        } catch (Exception ignored) {
        }
        System.out.println();
        Map<String, List<Friend>> friendList = StatisticUtil.getModelData(uin, skey, type);
        StatisticUtil.statistic(friendList);
        System.out.print("请输入机型查询用户：");
        while (scanner.hasNext()) {
            String model = scanner.nextLine();
            if (StringUtils.isNotEmpty(model)) {
                System.out.println(StatisticUtil.getFriends(friendList, model));
            } else {
                System.out.println("输入错误");
            }
            System.out.print("请输入机型查询用户：");
        }
    }


}
