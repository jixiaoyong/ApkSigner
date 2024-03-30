package io.github.jixiaoyong.pages.signInfos

import LocalWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jixiaoyong.beans.SignInfoBean
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.showToast
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.HoverableTooltip
import kotlinx.coroutines.launch

/**
 * @author : jixiaoyong
 * @description ：签名文件信息页面
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */
@Composable
fun PageSignInfo(viewModel: SignInfoViewModel) {
    val window = LocalWindow.current

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val newSignInfo = uiState.newSignInfo
    val dropdownMenu = remember { DropdownMenuState() }

    LaunchedEffect(uiState.signInfoList) {
        val firstSignInfo = uiState.signInfoList.firstOrNull()
        if (uiState.selectedSignInfo?.isValid() != true && firstSignInfo?.isValid() == true) {
            // select first sign key if no one is selected or selected one isn't valid.
            viewModel.saveSelectedSignInfo(firstSignInfo)
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {

            var selectedSignInfoLayoutOffset by remember { mutableStateOf(Offset.Zero) }

            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        selectedSignInfoLayoutOffset = it.positionInParent() + Offset(0f, it.size.height.toFloat())
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "当前签名: ",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        fontSize = 16.sp
                    )
                )
                Text(
                    uiState.selectedSignInfo?.keyNickName ?: "暂无",
                    style = TextStyle(
                        lineBreak = LineBreak.Paragraph,
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Text(
                    uiState.selectedSignInfo?.keyStorePath ?: "",
                    style = TextStyle(
                        lineBreak = LineBreak.Paragraph,
                        color = MaterialTheme.colors.secondary,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                ButtonWidget(
                    onClick = {
                        dropdownMenu.status = DropdownMenuState.Status.Open(selectedSignInfoLayoutOffset)
                    },
                    title = "重新选择签名",
                )
            }

            MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp))) {
                DropdownMenu(
                    dropdownMenu,
                    onDismissRequest = { dropdownMenu.status = DropdownMenuState.Status.Closed },
                    modifier = Modifier.background(
                        MaterialTheme.colors.background.copy(0.8f),
                        shape = RoundedCornerShape(10.dp)
                    )
                        .border(1.dp, MaterialTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
                ) {
                    uiState.signInfoList.forEach {
                        val isSelected = uiState.selectedSignInfo == it
                        val textColor = if (isSelected) MaterialTheme.colors.primary
                        else MaterialTheme.colors.onBackground
                        DropdownMenuItem(
                            onClick = {
                                viewModel.saveSelectedSignInfo(it)
                                dropdownMenu.status = DropdownMenuState.Status.Closed
                            },
                            modifier = Modifier.widthIn(450.dp, 600.dp),

                            ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = it.keyNickName,
                                    modifier = Modifier.weight(2f),
                                    maxLines = 1,
                                    color = textColor
                                )
                                Text(
                                    text = it.keyStorePath,
                                    fontSize = 10.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(6f).padding(horizontal = 5.dp)
                                )
                                HoverableTooltip(
                                    description = "删除此工具存储的签名信息，不会删除apk签名文件",
                                    alwaysShow = true
                                ) { modifier ->
                                    IconButton(modifier = modifier, onClick = { viewModel.removeSignInfo(it) }) {
                                        Icon(Icons.Default.Delete, "")
                                    }
                                }
                                IconButton(onClick = { viewModel.updateNewSignInfo(it) }) {
                                    Icon(Icons.Default.Edit, "edit")
                                }
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.background(color = MaterialTheme.colors.secondary.copy(0.65f)))

            Column(modifier = Modifier.padding(vertical = 25.dp).fillMaxWidth()) {
                SignInfoItem(
                    "签名别名",
                    newSignInfo.keyNickName,
                    false,
                    description = "备注名称，用来区分不同签名"
                ) { nickName ->
                    viewModel.updateNewSignInfo(keyNickName = nickName)
                }
                SignInfoItem(
                    "文件路径", newSignInfo.keyStorePath, false, onClick = {
                        scope.launch {
                            val result = FileChooseUtil.chooseSignFile(window, "请选择Android签名文件")
                            if (result.isNullOrBlank()) {
                                showToast("请选择Android签名文件")
                            } else {
                                viewModel.updateNewSignInfo(keyStorePath = result)
                            }
                        }
                    }, buttonText = "选择文件", description = "签名文件的有效绝对路径"
                ) { keyStorePath ->
                    viewModel.updateNewSignInfo(keyStorePath = keyStorePath)
                }

                SignInfoItem(
                    "keyStorePassword", newSignInfo.keyStorePassword, true
                ) { keyStorePassword ->
                    viewModel.updateNewSignInfo(keyStorePassword = keyStorePassword)
                }
                SignInfoItem("keyAlias", newSignInfo.keyAlias, false) { keyAlias ->
                    viewModel.updateNewSignInfo(keyAlias = keyAlias)
                }
                SignInfoItem("keyPassword", newSignInfo.keyPassword, true) { keyPassword ->
                    viewModel.updateNewSignInfo(keyPassword = keyPassword)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonWidget(
                        enabled = newSignInfo.isValid(),
                        title = "保存新签名信息",
                        isHighlight = true,
                        modifier = Modifier.size(250.dp, 50.dp),
                        onClick = {
                            scope.launch {
                                viewModel.saveNewSignInfo(newSignInfo)
                                val isNeedClean = scaffoldState.snackbarHostState.showSnackbar(
                                    "🎉保存成功！\n请点击【重新选择签名】按钮查看，是否清除已填写内容？",
                                    actionLabel = "清空",
                                    duration = SnackbarDuration.Short
                                )
                                if (SnackbarResult.ActionPerformed == isNeedClean) {
                                    viewModel.updateNewSignInfo(SignInfoBean())
                                }
                            }
                        },
                    )
                }

            }
        }
    }

}
