package io.github.jixiaoyong.i18n

/**
 * @author : jixiaoyong
 * @description ：每次更新语言都必须同步更新下面两个类
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/3/2024
 */

object Locales {
    const val EN = "en"
    const val ZH = "zh"
}

enum class Locale(val code: String, val languageName: String) {
    EN(Locales.EN, "English"),
    ZH(Locales.ZH, "中文 | Chinese");

    companion object {
        fun getLocale(code: String?): Locale {
            return if (null == code) EN
            else if (code.areLanguagesEqual(Locales.ZH)) ZH
            else EN
        }
    }
}

fun String.areLanguagesEqual(otherLang: String): Boolean {
    if (this == otherLang) {
        return true
    }

    val locale1 = java.util.Locale.forLanguageTag(this)
    val locale2 = java.util.Locale.forLanguageTag(otherLang)
    return locale1.language == locale2.language
}