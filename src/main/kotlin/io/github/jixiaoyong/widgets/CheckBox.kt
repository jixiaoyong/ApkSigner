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
    title: String = "",
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable {
            onCheckedChange?.invoke(!checked)
        }) {
        Checkbox(
            checked = checked,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                checkmarkColor = colors.onPrimary,
                uncheckedColor = colors.secondary
            ),
            onCheckedChange = onCheckedChange
        )
        Text(title, color = MaterialTheme.colors.onBackground)
    }
}