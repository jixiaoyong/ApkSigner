package io.github.jixiaoyong.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.lyricist.strings

/**
 * @author : jixiaoyong
 * @description ：封装通用的弹窗组件
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2/4/2024
 */

/**
 * 渲染一个带有标题、内容以及可选确认和取消按钮的弹出窗口小部件。
 *
 * @param title 弹出窗口小部件的标题。
 * @param content 弹出窗口小部件的内容。
 * @param onDismiss 当弹出窗口关闭时要调用的回调函数。
 * @param onConfirm 当确认按钮被点击时要调用的回调函数。
 * @param show 是否应该显示弹出窗口小部件。默认为false。
 * @param confirmButton 可选的确认按钮内容。
 * @param cancelButton 可选的取消按钮内容。
 * @param properties 弹出窗口小部件的属性。
 */
@Composable
fun PopWidget(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    show: Boolean = false,
    confirmButton: @Composable (() -> Unit)? = null,
    cancelButton: @Composable (() -> Unit)? = null,
    properties: PopupProperties = PopupProperties()
) {
    if (show) Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        val scrollable = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.onBackground.copy(0.56f))
                .padding(horizontal = 50.dp, vertical = 65.dp)
                .wrapContentSize()
                .widthIn(300.dp, 450.dp)
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 20.dp, vertical = 15.dp)
        ) {
            title()
            Column(
                modifier = Modifier.weight(1f, fill = false)
                    .wrapContentSize().heightIn(max = 450.dp).verticalScroll(scrollable)
                    .padding(top = 20.dp, bottom = 30.dp)
            ) { content() }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            ) {
                cancelButton?.let {
                    Row(modifier = Modifier.clickable {
                        onDismiss()
                    }) { it() }
                }
                confirmButton?.let {
                    Row(modifier = Modifier.clickable {
                        onDismiss()
                        onConfirm()
                    }) { it() }
                }
            }
        }
    }
}

/**
 * 一个组合函数，显示带有标题、内容和按钮的弹出窗口小部件。
 *
 * @param show 弹出窗口小部件是否应该显示。默认为false。
 * @param title 弹出窗口小部件的标题。
 * @param onDismiss 当弹出窗口小部件被关闭时要调用的回调函数。
 * @param onConfirm 当单击确认按钮时要调用的回调函数。
 * @param confirmButton 显示在确认按钮上的文本。默认为“确认”。
 * @param cancelButton 显示在取消按钮上的文本。默认为“取消”。
 * @param properties 弹出窗口小部件的属性。默认为一个空的[PopupProperties]对象。
 * @param content 弹出窗口小部件的内容。
 */
@Composable
fun PopWidget(
    show: Boolean = false,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButton: String = strings.confirm,
    cancelButton: String? = strings.cancel,
    properties: PopupProperties = PopupProperties(),
    content: @Composable () -> Unit,
) {
    PopWidget(
        title = {
            Text(
                text = title,
                style = TextStyle(color = MaterialTheme.colors.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 5.dp)
            )
        },
        content = content,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        show = show,
        confirmButton = {
            Text(
                confirmButton,
                textAlign = TextAlign.Center,
                style = TextStyle(color = MaterialTheme.colors.primary, fontSize = 16.sp),
                modifier = Modifier.widthIn(100.dp).padding(horizontal = 5.dp)
            )
        },
        cancelButton = {
            cancelButton?.let {
                Text(
                    it,
                    style = TextStyle(fontSize = 16.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(100.dp).padding(horizontal = 5.dp)
                )
            }
        },
        properties = properties
    )

}