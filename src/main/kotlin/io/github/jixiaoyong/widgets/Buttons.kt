package io.github.jixiaoyong.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    isHighlight: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {

    val activeBackgroundColor = if (isHighlight) Color(0xff007AFF) else Color(0xffF2F2F7)
    val activeContentColor = if (isHighlight) Color.White else Color(0xff007AFF)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = activeBackgroundColor,
            disabledBackgroundColor = Color(0xffF2F2F7),
            contentColor = activeContentColor,
            disabledContentColor = Color(0xFFBABEBE),
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
        modifier = (modifier ?: Modifier).padding(horizontal = 5.dp).widthIn(100.dp),
        enabled = enabled,
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        if (content != null) {
            content()
        } else {
            Text(title ?: "")
        }
    }
}

@Preview
@Composable
private fun ButtonWidgetPreview() {
    Column {
        ButtonWidget(onClick = {

        }, modifier = Modifier.widthIn(300.dp)) {
            Text("123")
        }

        ButtonWidget(onClick = {
        }, enabled = false) {
            Text("123")
        }
    }
}