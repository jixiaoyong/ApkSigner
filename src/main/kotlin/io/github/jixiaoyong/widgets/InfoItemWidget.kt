package io.github.jixiaoyong.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onClick: (() -> Unit)? = null
) {
    val headerBackground = MaterialTheme.colors.surface
    val rounderRadius = 10.dp
    Column(
        modifier = Modifier.padding(vertical = 5.dp)
            .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(rounderRadius))
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .border(1.dp, color = headerBackground, shape = RoundedCornerShape(rounderRadius))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 35.dp).background(
                color = headerBackground,
                shape = RoundedCornerShape(topStart = rounderRadius, topEnd = rounderRadius)
            ).padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title, style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            )
            HoverableTooltip(
                description = description
            ) { modifier ->
                Icon(
                    Icons.Default.Info,
                    contentDescription = "description information",
                    modifier = modifier
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (showChangeButton) ButtonWidget(
                { onClick?.invoke() },
                title = buttonTitle ?: "修改"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(
                    MaterialTheme.colors.background,
                    RoundedCornerShape(bottomEnd = rounderRadius, bottomStart = rounderRadius)
                )
                .padding(vertical = 10.dp, horizontal = 5.dp)
        ) {
            Text(
                value ?: "暂无内容",
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
            )
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