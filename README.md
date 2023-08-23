![icon](./docs/shortscreen/icon.png)

# ApkSigner

A tool for Android developers to sign their applications with GUI software.

一款供Android开发者用来可视化签名的工具。

基于

🚧 目前界面功能还在持续完善中，基础签名功能可用。

TODO：

- [x] 添加打开已签名文件的功能；

- [ ] 添加可以自定义签名文件输出地址的功能；

- [x] 添加删除签名功能。

# 主要功能

支持在设置多个签名，并且可以切换。

起因是 Flutter 打包出来的工程在360加固时提示“没有签名”，但360加固提供的签名过程又过于繁琐，故此开发这样一个小工具，使用图形界面来签名APK文件。

![sign_info](./docs/shortscreen/sign_info.png)
![sign_app](./docs/shortscreen/sign_app.png)
![sign_settings](./docs/shortscreen/sign_settings.png)


# 使用

以下方式选择你喜欢的即可

## 编译源码

- [x] 下载源码后，使用 IDEA 打开并运行 Main.kt 文件即可启动图形化界面。

## 运行 jar 包

- [ ] 使用本仓库提供的 .jar 包，运行`java -jar xxx.jar`即可启动软件

- [x] 自行使用jar打包命令`./gradlew packageReleaseUberJarForCurrentOS`打包

## 运行 .app 或 .msi 等系统特定软件包

- [ ] 直接运行本仓库提供的对应软件包

或下载源码自助打包：

```groovy
packageReleaseDeb
packageReleaseDistributionForCurrentOS
packageReleaseDmg
packageReleaseMsi
```

针对 MAC 用户，可以考虑使用 [jar2app](https://github.com/dante-biase/jar2app)
将 jar 打包为 app 使用，避免繁琐的 mac 系统签名过程。
