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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.lyricist.strings

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
    val i18nString = strings
    Column(modifier = Modifier.padding(horizontal = 10.dp).padding(top = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(8f)) {
                Text(
                    title,
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                )

                if (!description.isNullOrBlank()) Text(
                    description,
                    style = TextStyle(color = MaterialTheme.colors.onSecondary, fontSize = 12.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }

            if (showChangeButton) Row(
                modifier = Modifier.weight(3f, fill = true),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonWidget(
                    { onClick?.invoke() },
                    title = buttonTitle ?: i18nString.change,
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        if (null != content) {
            content()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 5.dp, bottom = 15.dp)
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(5.dp))
                    .padding(vertical = 15.dp, horizontal = 10.dp)
            ) {
                Text(
                    value ?: i18nString.noContent,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
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