package io.github.jixiaoyong.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * @author : jixiaoyong
 * @description ：展示标题和内容的Button
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/21
 */

@Composable
fun ButtonWidget(
    onClick: () -> Unit,
    modifier: Modifier? = null,
    title: String? = null,
    enabled: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val modifier = modifier ?: Modifier
        .padding(horizontal = 5.dp)
        .background(
            if (enabled) MaterialTheme.colors.secondary else MaterialTheme.colors.surface,
            shape = RoundedCornerShape(10.dp)
        ).padding(horizontal = 15.dp, vertical = 10.dp)
    Box(
        modifier = modifier.clickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (null != content) {
            content()
        } else {
            Text(title ?: "", style = TextStyle(color = MaterialTheme.colors.onSecondary))
        }
    }
}

@Preview
@Composable
fun InfoItemWidgetPreview() {
    ButtonWidget(onClick = {

    }) {
        Text("123")
    }
}