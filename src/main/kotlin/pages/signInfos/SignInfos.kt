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
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import utils.FileChooseUtil
import utils.SettingsTool
import utils.StorageKeys

/**
 * @author : jixiaoyong
 * @description ：签名文件信息页面
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */
@Preview()
@Composable
fun PageSignInfo(window: ComposeWindow, settings: SettingsTool) {
    val selectedSignInfo by settings.selectedSignInfoBean.collectAsState(null)
    val signInfoList by settings.signInfoBeans.collectAsState(listOf())
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val dropdownMenu = remember { DropdownMenuState() }

    LaunchedEffect(Unit) {
        val firstSignInfo = signInfoList.firstOrNull()
        if (selectedSignInfo?.isValid() != true && firstSignInfo?.isValid() == true) {
            // select first sign key if no one was selected or selected one isn't valid.
            onSignInfoChanged(settings, firstSignInfo)
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
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
                Text("当前签名:", style = TextStyle(fontWeight = FontWeight.Bold))
                Text(selectedSignInfo?.keyNickName + selectedSignInfo?.keyStorePath)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    dropdownMenu.status = DropdownMenuState.Status.Open(Offset(50.0f, 80.0f))
                }) {
                    Text("重新选择签名")
                }
            }

            DropdownMenu(dropdownMenu, onDismissRequest = { dropdownMenu.status = DropdownMenuState.Status.Closed }) {
                signInfoList.forEach {
                    DropdownMenuItem(onClick = {
                        onSignInfoChanged(settings, it)
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
                SignInfoItem(
                    "文件路径",
                    newSignInfo.keyStorePath,
                    false,
                    onClick = {
                        scope.launch {
                            val result = FileChooseUtil.chooseSignFile(window, "请选择Android签名文件")
                            if (result.isNullOrBlank()) {
                                scaffoldState.snackbarHostState.showSnackbar("请选择Android签名文件")
                            } else {
                                newSignInfo = newSignInfo.copy(keyStorePath = result)
                            }
                        }
                    }, buttonText = "选择文件"
                ) { keyStorePath ->
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
                        // save sign info to local storage
                        val newSignInfos = mutableListOf<SignInfoBean>()
                        newSignInfos.addAll(signInfoList)
                        newSignInfos.add(newSignInfo)
                        settings.save(StorageKeys.SIGN_INFO_LIST, Json.encodeToString(newSignInfos))
                        newSignInfo = SignInfoBean()
                    }) {
                        Text("保存新签名文件")
                    }
                }

            }
        }
    }
}

private fun onSignInfoChanged(settings: SettingsTool, signInfoBean: SignInfoBean?) {
    settings.save(StorageKeys.SIGN_INFO_SELECT, Json.encodeToString(signInfoBean))
}

@Preview
@Composable
private fun SignInfoItem(
    name: String,
    value: String,
    isPwd: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    buttonText: String = "修改",
    onChange: (String) -> Unit
) {
    Row(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 5.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(0.3f))
        Row(modifier = Modifier.weight(0.7f)) {
            TextField(
                value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                keyboardOptions = if (isPwd) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
            )
            if (null != onClick) Button(onClick = onClick, modifier = Modifier.padding(horizontal = 5.dp)) {
                Text(buttonText)
            }
        }
    }
}

@Serializable
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
