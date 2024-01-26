<div align="center">

![icon](./src/main/resources/imgs/icon.png)

# ApkSigner

![GitHub release (with filter)](https://img.shields.io/github/v/release/jixiaoyong/ApkSigner) ![GitHub Release Date - Published_At](https://img.shields.io/github/release-date/jixiaoyong/ApkSigner) ![Github All Releases](https://img.shields.io/github/downloads/jixiaoyong/apksigner/total.svg)

A tool for Android developers to sign their applications with GUI software.

一款供 Android 开发者用来可视化签名的工具。

基于 [JetBrains Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)

![Support Mac](https://img.shields.io/badge/Mac-grey?logo=apple)
![Support Windows](https://img.shields.io/badge/Windows-blue?logo=windows)
![Static Badge](https://img.shields.io/badge/Ubuntu-%23E95420?logo=Ubuntu&logoColor=white)

[![download button](./docs/shortscreen/download.svg)](https://github.com/jixiaoyong/ApkSigner/releases)

</div>

<br/>
<br/>
<br/>

起因是 Flutter 打包出来的工程在 360 加固时提示“没有签名”，但 360 加固提供的签名过程又过于繁琐，故此开发这样一个小工具，使用图形界面来签名
APK 文件。

本软件基于 JetBrains Compose Multiplatform 进行开发，支持 macOS、Windows、Linux。由于我日常开发主要基于 macOS，因此在 Windows
和 Linux
上可能存在一些尚未发现的兼容性问题。如果你发现了这样的问题，敬请[告知](https://github.com/jixiaoyong/ApkSigner/issues)
，我会尽快进行适配。同时也非常欢迎你提交[PR](https://github.com/jixiaoyong/ApkSigner/pulls)。

## 主要功能

本 APP 是为了方便您通过 UI 操作签名 APK，支持管理、切换多个签名，一键签名 APK。
本身并不提供生成签名文件的功能，要使用此工具，您还需要提前准备好如下内容：

- 签名文件，一般为.keystore 文件或者.jks 格式，用来签名 APK
- apksigner 和 zipalign 文件，用来签名、对齐 APK，一般在 Android SDK 中的 build-tools 文件夹下某个版本中
- Java开发环境，签名 APK 的 apksigner 需要读取 JAVA_HOME 配置

🚧 目前界面功能还在持续完善中，基础签名功能可用。

- [x] 增/删/切换签名信息（只会修改 APP 自身配置，不会修改您的文件）
- [x] 签名 APK 文件，支持 V1，V2，V3，V4 方案
- [x] 指定签名 APK 输出目录
- [x] 查看 APK 已有签名信息
- [x] 支持 Light 和 Dark 主题，并可自动切换
- [x] 可选对齐与否
- [x] 支持多文件签名，查看签名
- [ ] 优化签名配置
- [ ] 美化主题

## 界面预览

<br/>

![sign_info](./docs/shortscreen/sign_info.png)
![sign_app](./docs/shortscreen/sign_app.png)
![sign_settings](./docs/shortscreen/sign_settings.png)

## 使用

以下方式选择你喜欢的即可

### 1. 运行 .dmg 或 .msi 等系统特定软件包【推荐👍】

- 直接运行本仓库提供的对应软件包

点击下载[📦 最新 Release 软件包 (.dmg/.msi/.deb)](https://github.com/jixiaoyong/ApkSigner/releases)

- 或下载源码自助打包：

```groovy
packageReleaseDeb
packageReleaseDmg
packageReleaseMsi
packageReleaseDistributionForCurrentOS
```

### 2. 编译源码

- 下载源码后，使用 IDEA 打开并运行 Main.kt 文件即可启动图形化界面。

### 3. 运行 jar 包

1. 自行使用 jar 打包命令`./gradlew packageReleaseUberJarForCurrentOS`打包

2. 运行`java -jar xxx.jar`即可启动软件

针对 MAC 用户，可以考虑使用 [jar2app](https://github.com/dante-biase/jar2app)
将 jar 打包为 app 使用，避免繁琐的 mac 系统签名过程。
