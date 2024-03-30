package io.github.jixiaoyong.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @author : jixiaoyong
 * @description ：展示标题和内容的widget
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */

@Composable
fun InfoItemWidget(
    title: String,
    value: String?,
    description: String? = null,
    buttonTitle: String? = null,
    showChangeButton: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    val rounderRadius = 10.dp
    Column(modifier = Modifier.padding(horizontal = 5.dp).padding(top = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                .background(Color(0xffE7E7E7), RoundedCornerShape(5.dp)).padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    title,
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colors.onPrimary
                    )
                )

                if (!description.isNullOrBlank()) Text(
                    description,
                    style = TextStyle(color = Color(0xff808080), fontSize = 12.sp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showChangeButton) ButtonWidget(
                { onClick?.invoke() },
                title = buttonTitle ?: "修改"
            )
        }

        if (null != content) {
            content()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        MaterialTheme.colors.background,
                        RoundedCornerShape(bottomEnd = rounderRadius, bottomStart = rounderRadius)
                    )
                    .padding(vertical = 15.dp, horizontal = 10.dp)
            ) {
                Text(
                    value ?: "暂无内容",
                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                )
            }
        }
    }
}

@Preview
@Composable
private fun InfoItemWidgetPreview() {

    Column {
        InfoItemWidget(
            title = "标题标题标题标题标题",
            value = "内容内容内容内容内容内容内容内容内容内容内容",
            description = "描述信息",
            buttonTitle = "修改"
        ) {}

        InfoItemWidget(
            title = "标题标题标题标题标题",
            value = "内容内容内容内容内容内容内容内容内容内容内容",
        ) {}

    }
}