package io.github.jixiaoyong.pages.signInfos

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
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
fun PageSignInfo(window: ComposeWindow, settings: SettingsTool, newSignInfo: MutableState<SignInfoBean>) {
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
                .verticalScroll(rememberScrollState())
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                            Spacer(modifier = Modifier.weight(1f).widthIn(100.dp))
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
                            IconButton(onClick = {
                                newSignInfo.value = it
                            }) {
                                Icon(Icons.Default.Edit, "edit")
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
                SignInfoItem(
                    "签名别名",
                    newSignInfo.value.keyNickName,
                    false,
                    description = "备注名称，用来区分多个不同签名"
                ) { nickName ->
                    newSignInfo.value = newSignInfo.value.copy(keyNickName = nickName)
                }
                SignInfoItem(
                    "文件路径", newSignInfo.value.keyStorePath, false, onClick = {
                        scope.launch {
                            val result =
                                FileChooseUtil.chooseSignFile(window, "请选择Android签名文件")
                            if (result.isNullOrBlank()) {
                                scaffoldState.snackbarHostState.showSnackbar("请选择Android签名文件")
                            } else {
                                newSignInfo.value = newSignInfo.value.copy(keyStorePath = result)
                            }
                        }
                    }, buttonText = "选择文件", description = "签名文件的绝对路径"
                ) { keyStorePath ->
                    newSignInfo.value = newSignInfo.value.copy(keyStorePath = keyStorePath)
                }

                SignInfoItem(
                    "keyStorePassword", newSignInfo.value.keyStorePassword, true
                ) { keyStorePassword ->
                    newSignInfo.value = newSignInfo.value.copy(keyStorePassword = keyStorePassword)
                }
                SignInfoItem("keyAlias", newSignInfo.value.keyAlias, false) { keyAlias ->
                    newSignInfo.value = newSignInfo.value.copy(keyAlias = keyAlias)
                }
                SignInfoItem("keyPassword", newSignInfo.value.keyPassword, true) { keyPassword ->
                    newSignInfo.value = newSignInfo.value.copy(keyPassword = keyPassword)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(enabled = newSignInfo.value.isValid(), onClick = {
                        // save sign info to local storage
                        val newSignInfos = mutableListOf<SignInfoBean>()
                        newSignInfos.addAll(signInfoList)
                        val indexOfSignInfo =
                            newSignInfos.indexOfFirst { it.isSameOne(newSignInfo.value) }
                        if (-1 != indexOfSignInfo) {
                            newSignInfos[indexOfSignInfo] = newSignInfo.value
                        } else {
                            newSignInfos.add(newSignInfo.value)
                        }
                        settings.save(StorageKeys.SIGN_INFO_LIST, gson.toJson(newSignInfos))
                        scope.launch {
                            val isNeedClean = scaffoldState.snackbarHostState.showSnackbar(
                                "🎉保存成功！\n请点击【重新选择签名】按钮查看，是否清除已填写内容？",
                                actionLabel = "清空",
                                duration = SnackbarDuration.Short
                            )
                            if (SnackbarResult.ActionPerformed == isNeedClean) {
                                newSignInfo.value = SignInfoBean()
                            }
                        }
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
@Preview
@Composable
private fun SignInfoItem(
    name: String,
    value: String,
    isPwd: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    buttonText: String = "修改",
    description: String? = null,
    onChange: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    var iconOffset by remember { mutableStateOf(Offset.Zero) }
    var iconSize by remember { mutableStateOf(IntSize.Zero) }
    val isShowDescription = description.isNullOrBlank().not()

    Row(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 5.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(modifier = Modifier.weight(0.25f)) {
            Text(name)
            if (isShowDescription) Icon(
                Icons.Default.Info,
                contentDescription = "description information",
                modifier = Modifier.hoverable(interactionSource).onGloballyPositioned {
                    iconOffset = it.positionInParent()
                    iconSize = it.size
                }
            )
            if (isShowDescription && isHovered) Popup(
                offset = iconOffset.round() + IntOffset(
                    iconSize.width,
                    0
                )
            ) {
                Row(
                    modifier = Modifier.background(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(5.dp)
                    ).padding(horizontal = 5.dp, vertical = 5.dp),
                ) { Text(description ?: "", style = TextStyle(color = Color.White.copy(alpha = 0.5f))) }
            }
        }
        Row(modifier = Modifier.weight(0.75f), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f).border(
                    1.dp,
                    color = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(5.dp)
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedLabelColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                ),
                keyboardOptions =
                if (isPwd) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
                shape = RoundedCornerShape(size = 15.dp),
            )
            if (null != onClick) ButtonWidget(
                onClick = onClick,
                title = buttonText
            )
        }
    }
}