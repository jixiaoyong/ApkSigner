package pages.signInfos

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * @author : jixiaoyong
 * @description ：签名文件信息页面
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */
@Preview()
@Composable
fun PageSignInfo(selectedSignInfo: SignInfoBean, onSignInfoChange: (SignInfoBean) -> Unit) {
    val dropdownMenu = remember { DropdownMenuState() }
    var signInfos by remember {
        mutableStateOf(
            listOf(
                SignInfoBean(
                    "密钥1",
                    "",
                    "",
                    "",
                    ""
                ),

                )
        )
    }

    LaunchedEffect(Unit) {
        if (!selectedSignInfo.isValid()) {
            // select first sign key if no one was selected or selected one isn't valid.
            onSignInfoChange(signInfos[0])
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            .scrollable(
                rememberScrollableState { return@rememberScrollableState 0f },
                orientation = Orientation.Vertical
            )
    ) {

        Row(
            modifier = Modifier.padding(vertical = 10.dp).background(Color.White, shape = RoundedCornerShape(15.dp))
                .padding(horizontal = 15.dp, vertical = 3.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("当前签名：", style = TextStyle(fontWeight = FontWeight.Bold))
            Text(selectedSignInfo.keyNickName + selectedSignInfo.keyStorePath)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                dropdownMenu.status = DropdownMenuState.Status.Open(Offset(50.0f, 80.0f))
            }) {
                Text("重新选择签名")
            }
        }

        DropdownMenu(dropdownMenu, onDismissRequest = { dropdownMenu.status = DropdownMenuState.Status.Closed }) {
            signInfos.forEach {
                DropdownMenuItem(onClick = {
                    onSignInfoChange(it)
                    dropdownMenu.status = DropdownMenuState.Status.Closed
                }) {
                    Text(text = it.keyNickName)
                }
            }
        }

        Column(
            modifier = Modifier.padding(vertical = 10.dp)
                .background(Color.White, RoundedCornerShape(15.dp)).padding(horizontal = 15.dp, vertical = 15.dp)
                .fillMaxWidth()
        ) {
            var newSignInfo by remember { mutableStateOf(SignInfoBean()) }
            SignInfoItem("签名别名", newSignInfo.keyNickName, false) { nickName ->
                newSignInfo = newSignInfo.copy(keyNickName = nickName)
            }
            SignInfoItem("文件路径", newSignInfo.keyStorePath, false) { keyStorePath ->
                newSignInfo = newSignInfo.copy(keyStorePath = keyStorePath)
            }
            SignInfoItem("keyStorePassword", newSignInfo.keyStorePassword, true) { keyStorePassword ->
                newSignInfo = newSignInfo.copy(keyStorePassword = keyStorePassword)
            }
            SignInfoItem("keyAlias", newSignInfo.keyAlias, false) { keyAlias ->
                newSignInfo = newSignInfo.copy(keyAlias = keyAlias)
            }
            SignInfoItem("keyPassword", newSignInfo.keyPassword, true) { keyPassword ->
                newSignInfo = newSignInfo.copy(keyPassword = keyPassword)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(enabled = newSignInfo.isValid(), onClick = {
                    signInfos = signInfos.toMutableList().apply { add(newSignInfo) }
                    // todo save sign info to local storage
                    //  saveSignInfo(newSignInfo)
                }) {
                    Text("保存新签名文件")
                }
            }

        }
    }
}


@Preview
@Composable
private fun SignInfoItem(name: String, value: String, isPwd: Boolean, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(0.3f))
        TextField(
            value,
            onValueChange = onChange,
            modifier = Modifier.weight(0.7f),
            keyboardOptions = if (isPwd) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
        )
    }
}

data class SignInfoBean(
    var keyNickName: String = "",
    var keyStorePath: String = "",
    var keyStorePassword: String = "",
    var keyAlias: String = "",
    var keyPassword: String = "",
) {
    fun isValid(): Boolean {
        return keyNickName.isNotEmpty() && keyStorePath.isNotEmpty() && keyStorePassword.isNotEmpty() && keyAlias.isNotEmpty() && keyPassword.isNotEmpty()
    }
}