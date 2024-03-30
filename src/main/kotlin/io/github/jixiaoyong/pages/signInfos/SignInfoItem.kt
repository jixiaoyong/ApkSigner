package io.github.jixiaoyong.pages.signInfos

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jixiaoyong.widgets.ButtonWidget

/**
 * @author : jixiaoyong
 * @description ：通用的签名信息收集组件
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 29/3/2024
 */

/**
 * 通用的签名信息收集组件
 * @param name 收集的信息名称
 * @param value 收集的信息值
 * @param isPwd 是否为密码类型
 * @param modifier Modifier
 * @param onClick 点击事件
 * @param buttonText 按钮文本
 * @param description 描述信息，默认为空，有值的话则会展示一个提示图标，鼠标悬浮时展示此文本内容
 * @param onChange 文本改变事件
 */
@Composable
fun SignInfoItem(
    name: String,
    value: String,
    isPwd: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    buttonText: String = "修改",
    description: String? = null,
    onChange: (String) -> Unit
) {

    Row(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(0.25f)) {
            Text(
                name,
                color = MaterialTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            if (!description.isNullOrBlank()) Text(
                description,
                style = TextStyle(color = Color(0xff808080), fontSize = 12.sp)
            )
        }
        Row(modifier = Modifier.weight(0.75f), verticalAlignment = Alignment.CenterVertically) {
            var isFocused by remember { mutableStateOf(false) }
            TextField(
                value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f)
                    .border(1.dp, Color(if (isFocused) 0xff007AFF else 0xFFBABEBE), shape = RoundedCornerShape(10.dp))
                    .onFocusChanged {
                        isFocused = it.isFocused
                    },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedLabelColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                ),
                keyboardOptions =
                if (isPwd) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                shape = RoundedCornerShape(size = 15.dp),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Normal, color = Color.Black)
            )
            if (null != onClick) ButtonWidget(
                onClick = onClick,
                title = buttonText
            )
        }
    }
}

@Preview
@Composable
private fun prev() {
    Column {
        SignInfoItem("Name", "Value", false) {}
        SignInfoItem("Name", "Value", true, onClick = {}) {}
    }
}