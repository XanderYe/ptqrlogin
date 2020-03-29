package cn.xanderye;

import cn.xanderye.util.HttpUtil;
import cn.xanderye.util.QQUtil;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

    /**
     * 应用ID
     */
    private final static String APP_ID = "549000912";
    private final static String DAID = "5";
    /**
     * 来源地址
     */
    private final static String U1 = "https://qzs.qq.com/qzone/v5/loginsucc.html?para=izone";

    private static String qrsig = "";

    /**
     * 获取到cookie后需要执行的方法
     * @param
     * @return void
     * @author XanderYe
     * @date 2020-03-29
     */
    private void doSomeThing(Map<String, String> cookies) {

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        InputStream inputStream = getQrCode();
        if (inputStream != null) {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(70, 30, 50, 30));
            Image image = new Image(inputStream);
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
    private InputStream getQrCode() {
        String t = Double.toString(Math.random());
        String url = "https://ssl.ptlogin2.qq.com/ptqrshow?appid=" + APP_ID + "&e=2&l=M&s=3&d=72&v=4&t=" + t + "&daid=" + DAID + "&pt_3rd_aid=0";
        byte[] data = HttpUtil.download(url, null, null);
        if (data != null && data.length > 0) {
            Map<String, String> cookies = HttpUtil.getCookies();
            if (cookies != null) {
                qrsig = cookies.get("qrsig");
                return new ByteArrayInputStream(data);
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
                    doSomeThing(cookies);
                } else {
                    System.out.println("cookies获取失败");
                }
            }
        });
        executorService.shutdown();
    }

    /**
     * 登录监听
     *
     * @param imageView
     * @return boolean
     * @author XanderYe
     * @date 2020-03-14
     */
    private boolean loginListener(ImageView imageView) {
        String ptqrtoken = QQUtil.hash33(qrsig);
        String action = "0-1-" + System.currentTimeMillis();
        String url = "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=" + U1 + "&ptqrtoken=" + ptqrtoken + "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=" + action + "&js_ver=20021917&js_type=1&login_sig=&pt_uistyle=40&aid=" + APP_ID + "&daid=" + DAID + "&has_onekey=1";
        Map<String, Object> cookies = new HashMap<>();
        cookies.put("qrsig", qrsig);
        String result = HttpUtil.doPostWithCookie(url, null, cookies, null);
        if (result.contains("二维码未失效")) {
            System.out.println("二维码未失效");
        } else if (result.contains("二维码认证中")) {
            System.out.println("手机扫码成功");
        } else if (result.contains("登录成功")) {
            return true;
        } else if (result.contains("二维码已失效")) {
            System.out.println("二维码已失效， 重新生成");
            InputStream inputStream = getQrCode();
            if (inputStream != null) {
                imageView.setImage(new Image(inputStream));
            }
        } else {
            System.out.println(result);
        }
        return false;
    }

}
