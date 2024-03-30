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
    settingsConfig = "设置信息"
)
