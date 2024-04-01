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
import cafe.adriel.lyricist.strings
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
    val i18nStrings = strings

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
                    i18nStrings.currentSignInfo,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        fontSize = 16.sp
                    )
                )
                Text(
                    uiState.selectedSignInfo?.keyNickName ?: i18nStrings.noContent,
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
                    title = i18nStrings.changeSignInfo,
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
                            modifier = Modifier.widthIn(500.dp, 650.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = it.keyNickName,
                                    modifier = Modifier.weight(3f),
                                    maxLines = 2,
                                    color = textColor
                                )
                                Text(
                                    text = it.keyStorePath,
                                    fontSize = 10.sp,
                                    color = textColor,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.weight(6f).padding(horizontal = 5.dp)
                                )
                                HoverableTooltip(
                                    description = i18nStrings.deleteSignInfoTips,
                                    alwaysShow = true
                                ) { modifier ->
                                    IconButton(modifier = modifier, onClick = { viewModel.removeSignInfo(it) }) {
                                        Icon(Icons.Default.Delete, "delete", tint = MaterialTheme.colors.onBackground)
                                    }
                                }
                                IconButton(onClick = { viewModel.updateNewSignInfo(it) }) {
                                    Icon(Icons.Default.Edit, "edit", tint = MaterialTheme.colors.onBackground)
                                }
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.background(color = MaterialTheme.colors.secondary.copy(0.65f)))

            Column(modifier = Modifier.padding(vertical = 25.dp).fillMaxWidth()) {
                SignInfoItem(
                    i18nStrings.nickName,
                    newSignInfo.keyNickName,
                    false,
                    description = i18nStrings.nickNameDescription
                ) { nickName ->
                    viewModel.updateNewSignInfo(keyNickName = nickName)
                }
                SignInfoItem(
                    i18nStrings.filePath, newSignInfo.keyStorePath, false, onClick = {
                        scope.launch {
                            val result = FileChooseUtil.chooseSignFile(window, i18nStrings.plzSelectSignFile)
                            if (result.isNullOrBlank()) {
                                showToast(i18nStrings.plzSelectSignFile)
                            } else {
                                viewModel.updateNewSignInfo(keyStorePath = result)
                            }
                        }
                    }, buttonText = i18nStrings.chooseFile, description = i18nStrings.absolutePathOfSignFile
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
                        title = i18nStrings.saveNewSignInfo,
                        isHighlight = true,
                        modifier = Modifier.size(250.dp, 50.dp),
                        onClick = {
                            scope.launch {
                                viewModel.saveNewSignInfo()
                                val isNeedClean = scaffoldState.snackbarHostState.showSnackbar(
                                    i18nStrings.saveNewSignInfoTips,
                                    actionLabel = i18nStrings.cleanUp,
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
