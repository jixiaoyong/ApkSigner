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
    primary = Color(0xFF03a9f4),
    secondary = Color(0xFF3ddc84),
    background = Color(0xFFFFFFF),
    onBackground = Color(0xFF0088D0),
    surface = Color(0xFFD6E8E7),
    primaryVariant = Color(0xFF008DD5),
    onPrimary = Color(0xFF212121),
    secondaryVariant = Color(0xFF00782B),
    onSecondary = Color(0xFF484848),
    error = Color(0xFF3DDC84),
    onError = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    isLight = true
)

private val darkColors = Colors(
    primary = Color(0xFF00BFA5),
    secondary = Color(0xFF2979FF),
    background = Color(0xFF121212),
    onBackground = Color(0xFF005F4B),
    surface = Color(0xFF383838),
    primaryVariant = Color(0xFF00A189),
    onPrimary = Color(0xFFE0E0E0),
    secondaryVariant = Color(0xFFE9FFFF),
    onSecondary = Color(0xFFE0E0E0),
    error = Color(0xFF2979FF),
    onError = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    isLight = false
)