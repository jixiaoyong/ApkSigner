package io.github.jixiaoyong.i18n

import cafe.adriel.lyricist.LyricistStrings

/**
 * @author : jixiaoyong
 * @description ：中文翻译
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/3/2024
 */
@LyricistStrings(languageTag = Locales.ZH, default = true)
internal val ZhStrings = Strings(
    loading = "加载中……",
    alreadyRunning = "ApkSigner已经启动了，请不要重复启动",
    exit = "退出",
    retry = "重试",
    signConfig = "签名信息",
    signApp = "签名APP",
    settingsConfig = "设置信息",
    confirm = "确定",
    change = "修改",
    noContent = "暂无内容",
    currentSignInfo = "当前签名: ",
    changeSignInfo = "重新选择签名",
    deleteSignInfoTips = "删除此工具存储的签名信息，不会删除apk签名文件",
    nickName = "签名别名",
    nickNameDescription = "备注名称，用来区分不同签名",
    filePath = "文件路径",
    plzSelectSignFile = "请选择Android签名文件",
    chooseFile = "选择文件",
    absolutePathOfSignFile = "签名文件的有效绝对路径",
    saveNewSignInfo = "保存新签名信息",
    saveNewSignInfoTips = "🎉保存成功！\n请点击【重新选择签名】按钮查看，是否清除已填写内容？",
    cleanUp = "清空",
    resetSuccess = "重置成功",
    confirmReset = "确认重置吗",
    confirmResetTips = "重置会清除以下内容，请谨慎操作",
    signToolsConfigResetTips = "签名工具配置(不会删除文件)",
    signType = "签名方案",
    signedApkOutputDir = "签名文件输出目录",
    chooseBuildTools = "请选择build-tools目录",
    chooseBuildToolsTips = "请拖拽Android SDK的build-tools的子文件夹到这里，以一次性修改apkSigner和zipAlign目录",
    apksignerDirectory = "apksigner目录",
    plzChooseApksigner = "请选择apksigner文件",
    chooseApksignerTips = "请选择Android SDK中build-tools目录apksigner文件",
    notInit = "尚未初始化",
    changeSuccess = "修改成功",
    zipDirectory = "zipalign目录",
    chooseZipTips = "请选择Android SDK中build-tools目录zipalign文件",
    plzChooseZip = "请选择zipAlign文件",
    autoMatchSignature = "自动匹配签名",
    autoMatchSignatureTips = "当只有一个apk文件时，则自动尝试匹配上次使用的签名信息",
    aaptDirectory = "aapt目录",
    chooseAaptDirectory = "请选择aapt文件",
    aaptDirectoryTips = "若要自动匹配签名，请正确配置Android SDK中build-tools目录aapt文件",
    reset = "重置",
    appIntro = { version, webSite ->
        "这是一个本地可视化签名APK的小工具。为了避免泄漏密钥等信息，本工具不会联网。\n" +
                "当前版本：$version，查看最新版本请点击访问：$webSite"
    },
)
