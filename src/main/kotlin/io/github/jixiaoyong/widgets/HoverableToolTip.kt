package io.github.jixiaoyong.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup

/**
 * @author : jixiaoyong
 * @description ：根据鼠标是否悬停在某个组件上来显示或隐藏 Tooltip。
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 12/1/2024
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoverableTooltip(tooltip: @Composable () -> Unit, content: @Composable (modifier: Modifier) -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    var iconOffset by remember { mutableStateOf(Offset.Zero) }
    var iconSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        Modifier.onPointerEvent(
            PointerEventType.Move
        ) {
        }
            .onPointerEvent(PointerEventType.Enter) {
                isHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHovered = false
            }
    ) {
        content(Modifier.onGloballyPositioned {
            iconOffset = it.positionInParent()
            iconSize = it.size
        })

        if (isHovered) Popup(
            offset = iconOffset.round() + IntOffset(
                iconSize.width,
                0
            )
        ) {
            tooltip()
        }
    }
}


/**
 * 在 [content] （比如一个❓按钮）之上增加一个注释，当鼠标悬浮在其上时展示
 * [alwaysShow] 为true时，会一直展示[content]，否则只有注释不为空的时候才展示
 */
@Composable
fun HoverableTooltip(
    description: String?,
    alwaysShow: Boolean = false,
    content: @Composable (modifier: Modifier) -> Unit
) {
    if (description.isNullOrBlank()) {
        if (alwaysShow) content(Modifier)
    } else {
        HoverableTooltip(
            tooltip = {
                Row(
                    modifier = Modifier.background(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(5.dp)
                    ).padding(horizontal = 5.dp, vertical = 5.dp),
                ) { Text(description, style = TextStyle(color = Color.White.copy(alpha = 0.5f))) }
            },
            content = content
        )
    }
}

@Composable
fun HoverableTooltip(description: String?, imageVector: ImageVector = Icons.Default.Info, alwaysShow: Boolean = false) {
    HoverableTooltip(description = description, alwaysShow = alwaysShow) { modifier ->
        Icon(
            imageVector,
            contentDescription = description,
            modifier = modifier,
            tint = Color(0xff7A7A7A)
        )
    }
}