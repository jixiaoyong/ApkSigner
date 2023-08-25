package io.github.jixiaoyong.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Preview
@Composable
fun InfoItemWidget(
    title: String,
    description: String?,
    buttonTitle: String? = null,
    showChangeButton: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.padding(vertical = 10.dp)
            .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(15.dp))
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(15.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 35.dp).background(
                color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
            ).padding(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "$title：", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            if (showChangeButton) ButtonWidget(
                { onClick?.invoke() },
                title = buttonTitle ?: "\uD83D\uDCDD修改"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(
                    MaterialTheme.colors.background,
                    RoundedCornerShape(bottomEnd = 15.dp, bottomStart = 15.dp)
                )
                .padding(vertical = 10.dp, horizontal = 5.dp)
        ) {
            Text(
                description ?: "暂无内容",
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
            )
        }
    }
}
