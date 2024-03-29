package io.github.jixiaoyong.pages.settings

import ApkSigner
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
import androidx.compose.ui.graphics.Color
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
import io.github.jixiaoyong.utils.*
import io.github.jixiaoyong.widgets.ButtonWidget
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

    val viewModel = viewModel { SettingInfoViewModel(settings) }
    val uiState by viewModel.uiState.collectAsState()
    val resetConfig = uiState.resetInfo

    if (resetConfig.showResetDialog) {
        AlertDialog(onDismissRequest = {
            viewModel.toggleResetDialog()
        }, confirmButton = {
            ButtonWidget(onClick = {
                viewModel.runRestConfig()
                showToast("重置成功")
            }, title = "确定")

        }, title = {
            Text("确定重置吗")
        }, text = {
            Column {
                Text("重置会清除以下内容，请谨慎操作")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetConfig.resetSignInfo,
                        onCheckedChange = {
                            viewModel.updateResetConfig(resetSignInfo = !resetConfig.resetSignInfo)
                        })
                    Text("签名信息")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetConfig.resetApkTools,
                        onCheckedChange = { viewModel.updateResetConfig(resetApkTools = !resetConfig.resetApkTools) })
                    Text("签名工具配置(不会删除文件)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetConfig.resetSignTypes,
                        onCheckedChange = { viewModel.updateResetConfig(resetSignTypes = !resetConfig.resetSignTypes) })
                    Text("签名方案")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = resetConfig.resetSignedDirectory,
                        onCheckedChange = { viewModel.updateResetConfig(resetSignedDirectory = !resetConfig.resetSignedDirectory) })
                    Text("签名文件输出目录")
                }
            }
        })
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DropBoxPanel(window,
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                    .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(15.dp))
                    .padding(15.dp)
                    .clickable {
                        scope.launch {
                            val oldDirectory = uiState.apkSign?.substringBeforeLast(File.separator)
                            val chooseFileName =
                                FileChooseUtil.chooseSignDirectory(
                                    window,
                                    "请选择build-tools目录",
                                    oldDirectory
                                )
                            if (chooseFileName.isNullOrBlank()) {
                                showToast("请选择build-tools目录", ToastConfig.DURATION.Long)
                            } else {
                                viewModel.setupBuildToolsConfig(chooseFileName)
                            }
                        }
                    },
                component = JPanel(),
                onFileDrop = { scope.launch { viewModel.setupBuildToolsConfig(it.first()) } }) {
                Text(
                    text = "请拖拽Android SDK的build-tools的子文件夹到这里，以一次性修改apkSigner和zipAlign目录",
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            InfoItemWidget("apk signer目录",
                uiState.apkSign ?: "尚未初始化",
                description = "请选择Android SDK中build-tools目录apksigner文件",
                onClick = {
                    scope.launch {
                        val chooseFileName =
                            FileChooseUtil.chooseSignFile(window, "请选择apksigner文件")
                        if (chooseFileName.isNullOrBlank()) {
                            showToast("请选择apksigner文件", ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupApkSigner(chooseFileName)
                            viewModel.saveApkSigner(ApkSigner.apkSignerPath)
                            showToast(result ?: "修改成功")
                        }
                    }

                })
            InfoItemWidget("zipalign目录", uiState.zipAlign ?: "尚未初始化",
                description = "请选择Android SDK中build-tools目录zipalign文件",
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, "请选择zipAlign文件")
                        if (chooseFileName.isNullOrBlank()) {
                            showToast("请选择zipAlign文件", ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupZipAlign(chooseFileName)
                            viewModel.saveZipAlign(ApkSigner.zipAlignPath)
                            showToast(result ?: "修改成功")
                        }
                    }
                })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f, true)) {
                    Text(
                        "是否自动匹配签名",
                        style = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.onPrimary
                        )
                    )
                    Text("当只有一个apk文件时，则自动尝试匹配上次使用的签名信息")
                }
                Switch(
                    uiState.isAutoMatchSignature,
                    { autoMatch -> viewModel.saveAutoMatchSignature(autoMatch) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.secondary)
                )
            }

            if (uiState.isAutoMatchSignature) InfoItemWidget("aapt目录", uiState.aapt ?: "尚未初始化",
                description = "若要自动匹配签名，请正确配置Android SDK中build-tools目录aapt文件",
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, "请选择aapt文件")
                        if (chooseFileName.isNullOrBlank()) {
                            showToast("请选择aapt文件", ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupAapt(chooseFileName)
                            viewModel.saveAapt(ApkSigner.aaptPath)
                            showToast(result ?: "修改成功")
                        }
                    }
                })


            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 80.dp)) {
                ButtonWidget(
                    onClick = { viewModel.toggleResetDialog() },
                    title = "重置",
                    modifier = Modifier.size(250.dp, 50.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val str = "这是一个本地可视化签名APK的小工具。为了避免泄漏密钥等信息，本工具不会联网。\n" +
                        "当前版本：${uiState.version}，查看最新版本请点击访问：$PROJECT_WEBSITE"
                val startIndex = str.indexOf(PROJECT_WEBSITE)
                val endIndex = startIndex + PROJECT_WEBSITE.length

                val annotatedString = buildAnnotatedString {
                    append(str)
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xff64B5F6),
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
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
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
