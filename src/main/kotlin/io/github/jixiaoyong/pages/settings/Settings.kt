package io.github.jixiaoyong.pages.settings

import ApkSigner
import LocalI18nStrings
import LocalSettings
import LocalWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jixiaoyong.base.viewModel
import io.github.jixiaoyong.pages.signapp.DropBoxPanel
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.ToastConfig
import io.github.jixiaoyong.utils.showToast
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.CheckBox
import io.github.jixiaoyong.widgets.InfoItemWidget
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JPanel

/**
 * @author : jixiaoyong
 * @description ：设置页面
 *
 * 配置apksigner，java，keytool等环境变量
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */

private const val PROJECT_WEBSITE = "https://github.com/jixiaoyong/ApkSigner"

@Composable
fun PageSettingInfo() {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val window = LocalWindow.current
    val settings = LocalSettings.current
    val i18nStringTool = LocalI18nStrings.current
    val i18nString = i18nStringTool.strings

    val viewModel = viewModel { SettingInfoViewModel(settings) }
    val uiState by viewModel.uiState.collectAsState()
    val resetConfig = uiState.resetInfo

    if (resetConfig.showResetDialog) {
        MaterialTheme(
            typography = MaterialTheme.typography.copy(
                body1 = TextStyle.Default.copy(color = MaterialTheme.colors.onBackground)
            ),
        ) {
            AlertDialog(onDismissRequest = {
                viewModel.toggleResetDialog()
            }, confirmButton = {
                ButtonWidget(onClick = {
                    viewModel.runRestConfig()
                    showToast(i18nString.resetSuccess)
                }, title = i18nString.confirm)

            }, title = {
                Text(i18nString.confirmReset)
            }, text = {
                Column {
                    Text(i18nString.confirmResetTips)
                    CheckBox(
                        checked = resetConfig.resetSignInfo,
                        title = i18nString.signConfig,
                        onCheckedChange = {
                            viewModel.updateResetConfig(resetSignInfo = !resetConfig.resetSignInfo)
                        })
                    CheckBox(
                        checked = resetConfig.resetApkTools,
                        title = i18nString.signToolsConfigResetTips,
                        onCheckedChange = { viewModel.updateResetConfig(resetApkTools = !resetConfig.resetApkTools) })

                    CheckBox(
                        checked = resetConfig.resetSignTypes,
                        title = i18nString.signType,
                        onCheckedChange = { viewModel.updateResetConfig(resetSignTypes = !resetConfig.resetSignTypes) })
                    CheckBox(
                        checked = resetConfig.resetSignedDirectory,
                        title = i18nString.signedApkOutputDir,
                        onCheckedChange = { viewModel.updateResetConfig(resetSignedDirectory = !resetConfig.resetSignedDirectory) })
                }
            })
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DropBoxPanel(window,
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                    .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(10.dp))
                    .padding(15.dp)
                    .clickable {
                        scope.launch {
                            val oldDirectory = uiState.apkSign?.substringBeforeLast(File.separator)
                            val chooseFileName =
                                FileChooseUtil.chooseSignDirectory(
                                    window,
                                    i18nString.chooseBuildTools,
                                    oldDirectory
                                )
                            if (chooseFileName.isNullOrBlank()) {
                                showToast(i18nString.chooseBuildTools, ToastConfig.DURATION.Long)
                            } else {
                                viewModel.setupBuildToolsConfig(chooseFileName)
                            }
                        }
                    },
                component = JPanel(),
                onFileDrop = { scope.launch { viewModel.setupBuildToolsConfig(it.first()) } }) {
                Text(
                    text = i18nString.chooseBuildToolsTips,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            InfoItemWidget(i18nString.apksignerDirectory,
                uiState.apkSign ?: i18nString.notInit,
                description = i18nString.chooseApksignerTips,
                onClick = {
                    scope.launch {
                        val chooseFileName =
                            FileChooseUtil.chooseSignFile(window, i18nString.plzChooseApksigner)
                        if (chooseFileName.isNullOrBlank()) {
                            showToast(i18nString.plzChooseApksigner, ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupApkSigner(chooseFileName)
                            viewModel.saveApkSigner(ApkSigner.apkSignerPath)
                            showToast(result ?: i18nString.changeSuccess)
                        }
                    }

                })
            InfoItemWidget(i18nString.zipDirectory, uiState.zipAlign ?: i18nString.notInit,
                description = i18nString.chooseZipTips,
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, i18nString.plzChooseZip)
                        if (chooseFileName.isNullOrBlank()) {
                            showToast(i18nString.plzChooseZip, ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupZipAlign(chooseFileName)
                            viewModel.saveZipAlign(ApkSigner.zipAlignPath)
                            showToast(result ?: i18nString.changeSuccess)
                        }
                    }
                })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f, true)) {
                    Text(
                        i18nString.autoMatchSignature,
                        style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    )
                    Text(
                        i18nString.autoMatchSignatureTips,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onSecondary
                        )
                    )
                }
                Switch(
                    uiState.isAutoMatchSignature,
                    { autoMatch -> viewModel.saveAutoMatchSignature(autoMatch) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                )
            }

            if (uiState.isAutoMatchSignature) InfoItemWidget(i18nString.aaptDirectory,
                uiState.aapt ?: i18nString.notInit,
                description = i18nString.aaptDirectoryTips,
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, i18nString.chooseAaptDirectory)
                        if (chooseFileName.isNullOrBlank()) {
                            showToast(i18nString.chooseAaptDirectory, ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupAapt(chooseFileName)
                            viewModel.saveAapt(ApkSigner.aaptPath)
                            showToast(result ?: i18nString.changeSuccess)
                        }
                    }
                })


            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 80.dp)) {
                ButtonWidget(
                    onClick = { viewModel.toggleResetDialog() },
                    title = i18nString.reset,
                    modifier = Modifier.size(250.dp, 50.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val str = i18nString.appIntro(uiState.version, PROJECT_WEBSITE)
                val startIndex = str.indexOf(PROJECT_WEBSITE)
                val endIndex = startIndex + PROJECT_WEBSITE.length

                val annotatedString = buildAnnotatedString {
                    append(str)
                    addStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        ), start = startIndex, end = endIndex
                    )
                    addStringAnnotation("URL", PROJECT_WEBSITE, startIndex, endIndex)
                }
                val uriHandler = LocalUriHandler.current
                ClickableText(
                    text = annotatedString,
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSecondary.copy(alpha = 0.5f)
                    ),
                    onClick = { offset ->
                        annotatedString
                            .getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { stringAnnotation ->
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    }
                )
            }
        }
    }

}
