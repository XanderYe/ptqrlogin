# ptqrlogin
## 描述
使用javafx实现扫码登录QQ获取cookies

## 使用说明
1. 默认使用空间的应用id，如需修改应用id则修改[config.properties](src/main/resources/config.properties) 
2. 在[Main.java](src/main/java/cn/xanderye/Main.java)的doSomeThing方法中编写获取到cookie后的操作，cookie调用[Cookie.java](src/main/java/cn/xanderye/qq/QQCookie.java)的单例
