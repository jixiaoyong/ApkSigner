package io.github.jixiaoyong.i18n

import cafe.adriel.lyricist.LyricistStrings

/**
 * @author : jixiaoyong
 * @description ：英文翻译
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/3/2024
 */
@LyricistStrings(languageTag = Locales.EN)
internal val EnStrings = Strings(
    loading = "Loading...",
    alreadyRunning = "Apk Signer has been started, please do not start it again.",
    exit = "Quit",
    retry = "Retry",
    signConfig = "Signature information",
    signApp = "Signature APP",
    settingsConfig = "Settings Config"
)

//internal val EnStrings = Strings(
//    simple = "Hello Compose!",
//
//    annotated = buildAnnotatedString {
//        withStyle(SpanStyle(color = Color.Red)) { append("Hello ") }
//        withStyle(SpanStyle(fontWeight = FontWeight.Light)) { append("Compose!") }
//    },
//
//    parameter = { locale ->
//        "Current locale: $locale"
//    },
//
//    plural = { count ->
//        val value = when (count) {
//            0 -> "no"
//            1, 2 -> "a few"
//            in 3..10 -> "a bunch of"
//            else -> "a lot of"
//        }
//        "I have $value apples"
//    },
//
//    list = listOf("Avocado", "Pineapple", "Plum")
//)