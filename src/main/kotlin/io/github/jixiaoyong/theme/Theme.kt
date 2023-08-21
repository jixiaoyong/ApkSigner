package io.github.jixiaoyong.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


/**
 * @author : jixiaoyong
 * @description ：TODO
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/21
 */
private val background = Color(0xFFFFFFF)
private val primary = Color(0xFF03a9f4)
private val accent = Color(0xFF3ddc84)
private val textColor = Color(0xFF212121)
private val Primary100 = Color(0xFF03A9F4)
private val Primary200 = Color(0xFF008DD5)
private val Primary300 = Color(0xFF0088D0)
private val Accent100 = Color(0xFF3DDC84)
private val Accent200 = Color(0xFF00782B)
private val Text100 = Color(0xFF212121)
private val Text200 = Color(0xFF484848)
private val Bg100 = Color(0xFFE0F2F1)
private val Bg200 = Color(0xFFD6E8E7)
private val Bg300 = Color(0xFFAEBFBE)

private val DarkBackground = Color(0xFF121212)
private val DarkPrimary = Color(0xFF00BFA5)
private val DarkAccent = Color(0xFF2979FF)
private val DarkTextColor = Color(0xFFFFFFFF)
private val DarkPrimary100 = Color(0xFF00BFA5)
private val DarkPrimary200 = Color(0xFF00A189)
private val DarkPrimary300 = Color(0xFF005F4B)
private val DarkAccent100 = Color(0xFF2979FF)
private val DarkAccent200 = Color(0xFFE9FFFF)
private val DarkText100 = Color(0xFFFFFFFF)
private val DarkText200 = Color(0xFFE0E0E0)
private val DarkBg100 = Color(0xFF121212)
private val DarkBg200 = Color(0xFF212121)
private val DarkBg300 = Color(0xFF383838)

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
    primary = primary,
    secondary = accent,
    background = background,
    onBackground = Primary300,
    surface = Bg200,
    primaryVariant = Primary200,
    onPrimary = Text100,
    secondaryVariant = Accent200,
    onSecondary = Text200,
    error = Accent100,
    onError = Text100,
    onSurface = textColor,
    isLight = true
)

private val darkColors = Colors(
    primary = DarkPrimary,
    secondary = DarkAccent,
    background = DarkBackground,
    onBackground = DarkPrimary300,
    surface = DarkBg300,
    primaryVariant = DarkPrimary200,
    onPrimary = DarkText200,
    secondaryVariant = DarkAccent200,
    onSecondary = DarkText200,
    error = DarkAccent100,
    onError = DarkText100,
    onSurface = DarkTextColor,
    isLight = false
)