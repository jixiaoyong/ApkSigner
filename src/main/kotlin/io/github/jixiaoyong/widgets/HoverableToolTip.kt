package io.github.jixiaoyong.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * @author : jixiaoyong
 * @description ：根据鼠标是否悬停在某个组件上来显示或隐藏 Tooltip。
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 12/1/2024
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun HoverableTooltip(tooltip: @Composable () -> Unit, content: @Composable () -> Unit) {
    TooltipArea(tooltip, delayMillis = 100) { content() }
}


/**
 * 在 [content] （比如一个❓按钮）之上增加一个注释，当鼠标悬浮在其上时展示
 * [alwaysShow] 为true时，会一直展示[content]，否则只有注释不为空的时候才展示
 */
@Composable
fun HoverableTooltip(
    description: String?,
    alwaysShow: Boolean = false,
    content: @Composable () -> Unit
) {
    if (description.isNullOrBlank()) {
        if (alwaysShow) content()
    } else {
        HoverableTooltip(
            tooltip = {
                Row(
                    modifier = Modifier.background(
                        color = MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(5.dp)
                    ).padding(horizontal = 5.dp, vertical = 5.dp),
                ) {
                    Text(
                        description,
                        style = TextStyle(color = MaterialTheme.colors.onSecondary)
                    )
                }
            },
            content = content
        )
    }
}

/**
 * [imageVector]使用默认的❓图标，鼠标悬浮在其上时展示提示[description]
 */
@Composable
fun HoverableTooltip(description: String?, imageVector: ImageVector = Icons.Default.Info, alwaysShow: Boolean = false) {
    HoverableTooltip(description = description, alwaysShow = alwaysShow) {
        Icon(
            imageVector,
            contentDescription = description,
            tint = MaterialTheme.colors.onSecondary
        )
    }
}