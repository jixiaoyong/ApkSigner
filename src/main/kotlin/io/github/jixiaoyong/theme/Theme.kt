package io.github.jixiaoyong.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


/**
 * @author : jixiaoyong
 * @description ：主题配置
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/21
 */

@Composable
fun AppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = getColors(darkTheme),
        typography = MaterialTheme.typography,
    ) {
        content()
    }
}

private fun getColors(
    darkTheme: Boolean
) = if (darkTheme) darkColors else lightColors


private val lightColors = Colors(
    primary = Color(0xff007AFF),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xffF2F2F7),
    onSurface = Color(0xff3F403F),
    primaryVariant = Color(0xff0056A3),
    secondary = Color(0xFFBABEBE),
    secondaryVariant = Color(0xFFE7E7E7),
    error = Color(0xffD32F2F),
    onSecondary = Color(0xff808080),
    onError = Color.White,
    isLight = true
)


private val darkColors = Colors(
    primary = Color(0xff007AFF),
    onPrimary = Color(0xffDFE1E5),
    background = Color(0xff1E1F22),
    onBackground = Color(0xffDFDFDF),
    surface = Color(0xff3F403F),
    onSurface = Color(0xFFBABEBE),
    primaryVariant = Color(0xff0056A3),
    secondary = Color(0xff4B4B4B),
    secondaryVariant = Color(0xff343434),
    error = Color(0xffD32F2F),
    onSecondary = Color(0xff808080),
    onError = Color.White,
    isLight = false
)