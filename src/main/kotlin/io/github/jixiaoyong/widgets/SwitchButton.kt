package io.github.jixiaoyong.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * @author : jixiaoyong
 * @description ：自定义Switch
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2024/4/16
 */
@Composable
fun SwitchButton(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)? = null) {
    val mainColor = if (checked) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    val switchButtonWidth = 15
    var offsetLeft = animateFloatAsState(if (checked) 0f else switchButtonWidth.toFloat())
    Row(
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = mainColor,
                shape = RoundedCornerShape(25.dp)
            )
            .background(mainColor, shape = RoundedCornerShape(25.dp))
            .width(35.dp)
            .height(20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = null != onCheckedChange
            ) {
                onCheckedChange?.invoke(!checked)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .offset(offsetLeft.value.dp, 0.dp)
                .size(20.dp)
                .background(color = Color.White, shape = RoundedCornerShape(25.dp))
        )
    }
}

@Preview
@Composable
private fun PervSwitchButton() {
    var isChecked by remember { mutableStateOf(false) }
    Column {
        SwitchButton(checked = isChecked) {
            isChecked = !isChecked
        }
        SwitchButton(checked = !isChecked) {
            isChecked = !isChecked
        }
        SwitchButton(checked = isChecked)
    }
}