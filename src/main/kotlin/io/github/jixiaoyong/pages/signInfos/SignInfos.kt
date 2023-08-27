package io.github.jixiaoyong.pages.signInfos

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.StorageKeys
import io.github.jixiaoyong.utils.gson
import io.github.jixiaoyong.widgets.ButtonWidget
import kotlinx.coroutines.launch

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
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).scrollable(
                rememberScrollableState { return@rememberScrollableState 0f },
                orientation = Orientation.Vertical
            )
        ) {

            Row(
                modifier = Modifier.padding(vertical = 10.dp)
                    .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(15.dp))
                    .padding(horizontal = 15.dp, vertical = 10.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "当前签名: ", style = TextStyle(
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface
                    )
                )
                Text(
                    selectedSignInfo?.toString() ?: "暂无",
                    style = TextStyle(lineBreak = LineBreak.Paragraph),
                    modifier = Modifier.weight(1f)
                )
                ButtonWidget(onClick = {
                    dropdownMenu.status = DropdownMenuState.Status.Open(Offset(50.0f, 50.0f))
                }, title = "重新选择签名")
            }

            DropdownMenu(dropdownMenu,
                onDismissRequest = { dropdownMenu.status = DropdownMenuState.Status.Closed }) {
                signInfoList.forEach {
                    DropdownMenuItem(onClick = {
                        onSignInfoChanged(settings, it)
                        dropdownMenu.status = DropdownMenuState.Status.Closed
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = it.keyNickName)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                val tempList = signInfoList.toMutableList()
                                tempList.remove(it)
                                settings.save(
                                    StorageKeys.SIGN_INFO_LIST,
                                    gson.toJson(tempList)
                                )
                                if (it == selectedSignInfo) {
                                    onSignInfoChanged(settings, null)
                                }
                            }) {
                                Icon(Icons.Default.Delete, "")
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(vertical = 10.dp)
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(15.dp))
                    .padding(horizontal = 15.dp, vertical = 15.dp).fillMaxWidth()
            ) {
                var newSignInfo by remember { mutableStateOf(SignInfoBean()) }
                SignInfoItem("签名别名", newSignInfo.keyNickName, false) { nickName ->
                    newSignInfo = newSignInfo.copy(keyNickName = nickName)
                }
                SignInfoItem(
                    "文件路径", newSignInfo.keyStorePath, false, onClick = {
                        scope.launch {
                            val result =
                                FileChooseUtil.chooseSignFile(window, "请选择Android签名文件")
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

                SignInfoItem(
                    "keyStorePassword", newSignInfo.keyStorePassword, true
                ) { keyStorePassword ->
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
                        settings.save(StorageKeys.SIGN_INFO_LIST, gson.toJson(newSignInfos))
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
    val json = if (null == signInfoBean) null else gson.toJson(signInfoBean)
    settings.save(StorageKeys.SIGN_INFO_SELECT, json)
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
        Row(modifier = Modifier.weight(0.7f), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = MaterialTheme.colors.background),
                keyboardOptions = if (isPwd) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
            )
            if (null != onClick) ButtonWidget(
                onClick = onClick,
                title = buttonText
            )
        }
    }
}