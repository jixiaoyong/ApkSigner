package io.github.jixiaoyong.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * @author : jixiaoyong
 * @description ：包装之后的checkbox
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/3/2024
 */
@Composable
fun CheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    enabled: Boolean = true,
    title: String = "",
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val clickableModifier = if (enabled) {
        modifier.clickable {
            onCheckedChange?.invoke(!checked)
        }
    } else {
        modifier
    }

    val textColor = if (enabled) {
        MaterialTheme.colors.onBackground
    } else {
        MaterialTheme.colors.onBackground.copy(alpha = 0.38f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = clickableModifier
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                checkmarkColor = colors.onPrimary,
                uncheckedColor = colors.secondary,
                disabledColor = colors.onSurface.copy(alpha = 0.38f)
            ),
            onCheckedChange = if (enabled) onCheckedChange else null
        )
        Text(title, color = textColor)
    }
}