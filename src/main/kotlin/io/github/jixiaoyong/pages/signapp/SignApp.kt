package io.github.jixiaoyong.pages.signapp

import ApkSigner
import LocalWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import cafe.adriel.lyricist.strings
import io.github.jixiaoyong.beans.CommandResult
import io.github.jixiaoyong.beans.SignType
import io.github.jixiaoyong.pages.Routes
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.showToast
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.CheckBox
import io.github.jixiaoyong.widgets.HoverableTooltip
import io.github.jixiaoyong.widgets.InfoItemWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.util.*
import javax.swing.JPanel

/**
 * @author : jixiaoyong
 * @description ：签名app的地方
 * 1. 选择/拖拽APP
 * 2. 开始签名/查看签名
 * 3. 签名历史
 *
 * 自动匹配apk签名的逻辑：
 * 1. 当前只选中了一个apk
 * 2. apk在apkSignatureMap中有对应的签名，并且该签名在signInfoBeans中有效
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */

@Composable
fun PageSignApp(
    viewModel: SignAppViewModel,
    onChangePage: (String) -> Unit
) {
    val window = LocalWindow.current
    val clipboard = LocalClipboardManager.current
    val i18nStrings = strings

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val uiState by viewModel.uiState.collectAsState()

    val currentApkFilePath = uiState.apkFilePaths

    val local = uiState.signInfoResult
    when (local) {
        is CommandResult.Success<*> -> {
            Popup(onDismissRequest = {
                viewModel.changeSignInfo(CommandResult.NOT_EXECUT)
            }, alignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.onBackground.copy(0.56f))
                        .padding(horizontal = 50.dp, vertical = 65.dp)
                        .background(MaterialTheme.colors.background, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 20.dp, vertical = 15.dp)
                ) {
                    Text(
                        i18nStrings.signInfoTitle,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.W800,
                        modifier = Modifier.padding(20.dp).align(alignment = Alignment.CenterHorizontally)
                    )
                    Column(
                        modifier = Modifier.weight(1f, fill = false).heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SelectionContainer {
                            Text(
                                local.result?.toString() ?: "",
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                    TextButton(onClick = {
                        viewModel.changeSignInfo(CommandResult.NOT_EXECUT)
                    }, modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                        Text(
                            i18nStrings.confirm,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }

        is CommandResult.Error<*> -> {
            scope.launch {
                showToast(i18nStrings.checkSignFailed(local.message.toString()))
            }
        }

        else -> {}
    }

    if (local is CommandResult.EXECUTING) {
        Popup(alignment = Alignment.Center) {
            Box(
                modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.onBackground.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.size(150.dp)
                        .background(
                            color = MaterialTheme.colors.onBackground.copy(0.8f),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(80.dp).padding(10.dp))
                    Text(i18nStrings.processing, color = MaterialTheme.colors.onPrimary.copy(0.8f))
                }
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DropBoxPanel(
                    window,
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                        .background(
                            color = MaterialTheme.colors.surface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(15.dp)
                        .clickable {
                            scope.launch {
                                val chooseFileName =
                                    FileChooseUtil.chooseMultiFile(
                                        window,
                                        i18nStrings.plzChooseApkFile,
                                        filter = { _, name ->
                                            name.toLowerCase(Locale.getDefault()).endsWith(".apk")
                                        })
                                if (chooseFileName.isNullOrEmpty()) {
                                    showToast(i18nStrings.plzChooseApkFile)
                                } else {
                                    if (!uiState.signedOutputDirectory.isNullOrBlank()) {
                                        viewModel.saveSignedOutputDirectory(
                                            chooseFileName.first().substringBeforeLast(File.separator)
                                        )
                                    }
                                    viewModel.changeApkFilePath(chooseFileName)
                                    showToast(i18nStrings.changeSuccess)
                                }
                            }
                        },
                    component = JPanel(),
                    onFileDrop = {
                        scope.launch {
                            val file = it.filter() {
                                it.lowercase(Locale.getDefault()).endsWith(".apk")
                            }
                            if (file.isEmpty()) {
                                showToast(i18nStrings.plzChooseApkFile)
                            } else {
                                viewModel.changeApkFilePath(file)
                                showToast(i18nStrings.changeSuccess)
                            }
                        }
                    }
                ) {
                    Text(
                        text = i18nStrings.chooseApkFileTips,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                InfoItemWidget(
                    "${i18nStrings.currentSelectedFile}${if (currentApkFilePath.isEmpty()) "" else "(" + currentApkFilePath.size + ")"}",
                    if (currentApkFilePath.isEmpty()) i18nStrings.plzSelectApkFileFirst else currentApkFilePath.joinToString(
                        "\n"
                    ),
                    buttonTitle = i18nStrings.checkSignInfo,
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            if (currentApkFilePath.isEmpty()) {
                                showToast(i18nStrings.plzSelectApkFileFirst)
                            } else {
                                viewModel.changeSignInfo(CommandResult.EXECUTING)
                                val resultList = currentApkFilePath.map { ApkSigner.getApkSignInfo(it) }
                                val mergedResult = viewModel.mergeCommandResult(resultList, currentApkFilePath)
                                viewModel.changeSignInfo(mergedResult)
                            }
                        }
                    }
                )
                InfoItemWidget(
                    i18nStrings.currentSignInfo,
                    uiState.currentSignInfo?.toString() ?: i18nStrings.noContent,
                    onClick = {
                        onChangePage(Routes.SignInfo)
                        viewModel.removeApkSignature(uiState.apkPackageName)
                    })

                val errorTips = i18nStrings.plzChooseSignedApkOutDir
                InfoItemWidget(
                    i18nStrings.signedApkOutputDir,
                    uiState.signedOutputDirectory ?: errorTips,
                    onClick = {
                        scope.launch {
                            val outputDirectory =
                                FileChooseUtil.chooseSignDirectory(
                                    window,
                                    errorTips,
                                    uiState.signedOutputDirectory ?: currentApkFilePath.firstOrNull()
                                )
                            if (outputDirectory.isNullOrBlank()) {
                                showToast(errorTips)
                            } else {
                                viewModel.saveSignedOutputDirectory(outputDirectory)
                                showToast(i18nStrings.changeSuccess)
                            }
                        }
                    }
                )

                InfoItemWidget(
                    i18nStrings.signType,
                    null,
                    showChangeButton = false
                ) {
                    Row {
                        SignType.ALL_SIGN_TYPES.forEachIndexed { index, item ->
                            val isSelected = uiState.apkSignType.contains(item.type)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CheckBox(
                                    checked = isSelected,
                                    title = item.name,
                                    onCheckedChange = {
                                        val newTypes = mutableSetOf<Int>()
                                        newTypes.addAll(uiState.apkSignType)
                                        if (isSelected) {
                                            newTypes.remove(item.type)
                                        } else {
                                            newTypes.add(item.type)
                                        }

                                        viewModel.changeApkSignType(newTypes)
                                    })
                                HoverableTooltip(description = item.description(i18nStrings))
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        i18nStrings.isApkAlign,
                        style = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                        ),
                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                    )

                    Switch(checked = uiState.isZipAlign,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        onCheckedChange = {
                            viewModel.changeZipAlign(it)
                        })
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth().padding(5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val signedButtonEnable = (CommandResult.NOT_EXECUT == uiState.apkSignedResult) &&
                        currentApkFilePath.isNotEmpty()
                ButtonWidget(
                    {
                        scope.launch(Dispatchers.IO) {
                            if (currentApkFilePath.filter { it.lowercase(Locale.getDefault()).endsWith(".apk") }
                                    .isEmpty()
                            ) {
                                showToast(i18nStrings.plzSelectApkFileFirst)
                                return@launch
                            }

                            val localSelectedSignInfo = uiState.currentSignInfo
                            if (null == localSelectedSignInfo || !localSelectedSignInfo.isValid()) {
                                onChangePage(Routes.SignInfo)
                                showToast(i18nStrings.chooseRightSignatureTips)
                                return@launch
                            }

                            if (!ApkSigner.isInitialized()) {
                                onChangePage(Routes.SettingInfo)
                                showToast(i18nStrings.setupApksignerAndZipAlignTips)
                                return@launch
                            }

                            if (uiState.apkSignType.isEmpty()) {
                                showToast(i18nStrings.chooseSignTypeFirst)
                                return@launch
                            }

                            viewModel.changeSignApkResult(CommandResult.EXECUTING)
                            val signResult = ApkSigner.alignAndSignApk(
                                currentApkFilePath,
                                localSelectedSignInfo.keyStorePath,
                                localSelectedSignInfo.keyAlias,
                                localSelectedSignInfo.keyStorePassword,
                                localSelectedSignInfo.keyPassword,
                                signedApkDirectory = uiState.signedOutputDirectory,
                                zipAlign = uiState.isZipAlign,
                                signVersions = SignType.ALL_SIGN_TYPES.filter {
                                    uiState.apkSignType.contains(it.type)
                                },
                                onProgress = { line ->
                                    scope.launch {
                                        val old = uiState.signedLogs
                                        val newLogs = mutableListOf<String>().apply {
                                            addAll(old)
                                            add(line)
                                        }
                                        viewModel.updateSignedLogs(newLogs)
                                    }
                                }
                            )

                            val mergedResult = viewModel.mergeCommandResult(signResult, currentApkFilePath)
                            viewModel.changeSignApkResult(mergedResult)
                            val firstSuccessSignedApk =
                                signResult.firstOrNull { it is CommandResult.Success<*> } as CommandResult.Success<*>?

                            if (mergedResult is CommandResult.Success<*> && !firstSuccessSignedApk?.result?.toString()
                                    .isNullOrBlank()
                            ) {

                                launch(Dispatchers.IO) {
                                    if (uiState.isAutoMatchSignature && 1 == currentApkFilePath.size) {
                                        //  将当前签名和apk包名关联
                                        viewModel.updateApkSignatureMap(
                                            uiState.apkPackageName,
                                            localSelectedSignInfo,
                                        )
                                    }
                                }

                                val result = scaffoldState.snackbarHostState.showSnackbar(
                                    i18nStrings.chooseOpenSignedApkFile,
                                    i18nStrings.open,
                                    SnackbarDuration.Long
                                )
                                val file = File(firstSuccessSignedApk?.result?.toString() ?: "")
                                if (SnackbarResult.ActionPerformed == result && file.exists()) {
                                    Desktop.getDesktop().open(file.parentFile)
                                }
                            } else if (mergedResult is CommandResult.Error<*>) {
                                val result = scaffoldState.snackbarHostState.showSnackbar(
                                    "${i18nStrings.signedFailed}${mergedResult.message}",
                                    i18nStrings.copyErrorMsg,
                                    SnackbarDuration.Indefinite
                                )
                                if (SnackbarResult.ActionPerformed == result) {
                                    clipboard.setText(AnnotatedString("${mergedResult.message}"))
                                }
                            }
                            viewModel.changeSignApkResult(CommandResult.NOT_EXECUT)
                        }

                    },
                    enabled = signedButtonEnable,
                    title = i18nStrings.startSignApk,
                    isHighlight = true,
                    modifier = Modifier.size(250.dp, 50.dp),
                )
            }
        }
    }
}
