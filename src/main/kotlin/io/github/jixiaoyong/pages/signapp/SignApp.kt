package io.github.jixiaoyong.pages.signapp

import ApkSigner
import CommandResult
import Routes
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.InfoItemWidget
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.util.*
import javax.swing.JPanel
import kotlin.math.roundToInt

/**
 * @author : jixiaoyong
 * @description ：签名app的地方
 * 1. 选择/拖拽APP
 *      选择签名文件？
 * 2. 开始签名
 * 3. 签名历史
 *
 * todo 添加可以自定义签名文件输出地址的功能
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */

@Preview()
@Composable
fun PageSignApp(
    window: ComposeWindow,
    currentApkFilePath: String?,
    settings: SettingsTool,
    onChangeApk: (String) -> Unit,
    onChangePage: (String) -> Unit
) {

    val scope = rememberCoroutineScope()
    var signLogs by remember { mutableStateOf(listOf<String>()) }
    var signApkResult: CommandResult by remember { mutableStateOf(CommandResult.NOT_EXECUT) }
    val scaffoldState = rememberScaffoldState()
    val isEnabled = remember(signApkResult) {
        CommandResult.NOT_EXECUT == signApkResult
    }
    val apkSignType by settings.signTypeList.collectAsState(setOf())
    val selectedSignInfo by settings.selectedSignInfoBean.collectAsState(null)

    var signInfoResult: CommandResult by remember { mutableStateOf(CommandResult.NOT_EXECUT) }


    val local = signInfoResult
    when (local) {
        is CommandResult.Success<*> -> {
            AlertDialog(onDismissRequest = {
                signInfoResult = CommandResult.NOT_EXECUT
            }, buttons = {

            }, title = { Text("查询签名结果") }, text = {
                Text(local.result?.toString() ?: "")
            })
        }

        is CommandResult.Error<*> -> {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("查询签名失败:${local.message}")
            }
        }

        else -> {}
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
                            shape = RoundedCornerShape(15.dp)
                        )
                        .padding(15.dp),
                    component = JPanel(),
                    onFileDrop = {
                        scope.launch {
                            val file =
                                it.firstOrNull {
                                    it.lowercase(Locale.getDefault()).endsWith(".apk")
                                }
                            if (null == file) {
                                scaffoldState.snackbarHostState.showSnackbar("请先选择正确的apk文件")
                            } else {
                                onChangeApk(file)
                                scaffoldState.snackbarHostState.showSnackbar("选择apk文件成功")
                            }
                        }
                    }
                ) {
                    Text(
                        text = "请拖拽apk文件到这里哦",
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                InfoItemWidget(
                    "当前选择的文件",
                    currentApkFilePath ?: "请先选择apk文件",
                    buttonTitle = "查看签名",
                    onClick = {
                        scope.launch {
                            if (currentApkFilePath.isNullOrBlank()) {
                                scaffoldState.snackbarHostState.showSnackbar("请先选择apk文件")
                            } else {
                                signInfoResult = ApkSigner.getApkSignInfo(currentApkFilePath)
                            }
                        }
                    }
                )
                InfoItemWidget(
                    "当前选择的签名文件",
                    selectedSignInfo?.toString() ?: "暂无",
                    onClick = {
                        onChangePage(Routes.SignInfo)
                    })

                Text(
                    "签名方案：",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                        .padding(bottom = 15.dp)
                ) {
                    SignType.ALL_SIGN_TYPES.forEachIndexed { index, item ->
                        val isSelected = apkSignType.contains(item.type)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = {
                                val newTypes = mutableSetOf<Int>()
                                newTypes.addAll(apkSignType)
                                if (isSelected) {
                                    newTypes.remove(item.type)
                                } else {
                                    newTypes.add(item.type)
                                }

                                settings.signTypeList = flowOf(newTypes)
                            })
                            Text(item.name, modifier = Modifier.padding(start = 5.dp))
                            if (!item.description.isNullOrEmpty()) Icon(Icons.Default.Info,
                                contentDescription = item.description,
                                modifier = Modifier.padding(end = 10.dp).size(18.dp).clickable {
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(item.description)
                                    }
                                })
                        }

                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colors.surface.copy(alpha = 0.3f))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ButtonWidget(
                    {
                        scope.launch {
                            if (currentApkFilePath.isNullOrBlank() || !currentApkFilePath.lowercase(
                                    Locale.getDefault()
                                )
                                    .endsWith(".apk")
                            ) {
                                scaffoldState.snackbarHostState.showSnackbar("请先选择正确的apk文件")
                                return@launch
                            }

                            val localSelectedSignInfo = selectedSignInfo
                            if (null == localSelectedSignInfo || !localSelectedSignInfo.isValid()) {
                                onChangePage(Routes.SignInfo)
                                scaffoldState.snackbarHostState.showSnackbar("请先配置正确的签名文件")
                                return@launch
                            }

                            if (!ApkSigner.isInitialized()) {
                                onChangePage(Routes.SettingInfo)
                                scaffoldState.snackbarHostState.showSnackbar("请先配置apksigner和zipalign路径")
                                return@launch
                            }

                            if (apkSignType.isEmpty()) {
                                scaffoldState.snackbarHostState.showSnackbar("请至少选择一种签名方式")
                                return@launch
                            }

                            val signResult = ApkSigner.alignAndSignApk(
                                currentApkFilePath,
                                localSelectedSignInfo.keyStorePath,
                                localSelectedSignInfo.keyAlias,
                                localSelectedSignInfo.keyStorePassword,
                                localSelectedSignInfo.keyPassword,
                                zipAlign = false,
                                signVersions = SignType.ALL_SIGN_TYPES.filter {
                                    apkSignType.contains(
                                        it.type
                                    )
                                },
                                onProgress = { line ->
                                    scope.launch {
                                        signLogs = mutableListOf<String>().apply {
                                            addAll(signLogs)
                                            add(line)
                                        }
                                    }
                                }
                            )

                            signApkResult = signResult
                            if (signResult is CommandResult.Success<*> && !signResult.result?.toString()
                                    .isNullOrBlank()
                            ) {
                                val result = scaffoldState.snackbarHostState.showSnackbar(
                                    "签名成功，是否打开签名后的文件？",
                                    "打开",
                                    SnackbarDuration.Long
                                )
                                val file = File(signResult.result?.toString() ?: "")
                                if (SnackbarResult.ActionPerformed == result && file.exists()) {
                                    Desktop.getDesktop().open(file.parentFile)
                                }
                            } else if (signResult is CommandResult.Error<*>) {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "签名失败：${signResult.message}",
                                )
                            }

                            signApkResult = CommandResult.NOT_EXECUT
                        }

                    },
                    enabled = isEnabled,
                    title = "开始签名apk",
                    modifier = Modifier.size(250.dp, 50.dp)
                        .background(
                            if (isEnabled) MaterialTheme.colors.secondary else MaterialTheme.colors.surface,
                            shape = RoundedCornerShape(15.dp)
                        ).padding(horizontal = 15.dp, vertical = 5.dp)
                )
            }

        }
    }
}

@Composable
fun DropBoxPanel(
    window: ComposeWindow,
    modifier: Modifier = Modifier,
    component: JPanel = JPanel(),
    onFileDrop: (List<String>) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    val dropBoundsBean = remember {
        mutableStateOf(DropBoundsBean())
    }

    Box(modifier = modifier.onPlaced {
        dropBoundsBean.value = DropBoundsBean(
            x = it.positionInWindow().x,
            y = it.positionInWindow().y,
            width = it.size.width,
            height = it.size.height
        )
    }) {
        LaunchedEffect(true) {
            component.setBounds(
                dropBoundsBean.value.x.roundToInt(),
                dropBoundsBean.value.y.roundToInt(),
                dropBoundsBean.value.width,
                dropBoundsBean.value.height
            )
            window.contentPane.add(component)

            object : DropTarget(component, object : DropTargetAdapter() {
                override fun drop(event: DropTargetDropEvent) {

                    event.acceptDrop(DnDConstants.ACTION_REFERENCE)
                    val dataFlavors = event.transferable.transferDataFlavors
                    dataFlavors.forEach {
                        if (it == DataFlavor.javaFileListFlavor) {
                            val list = event.transferable.getTransferData(it) as List<*>

                            val pathList = mutableListOf<String>()
                            list.forEach { filePath ->
                                pathList.add(filePath.toString())
                            }
                            onFileDrop(pathList)
                        }
                    }
                    event.dropComplete(true)
                }
            }) {

            }
        }

        SideEffect {
            component.setBounds(
                dropBoundsBean.value.x.roundToInt(),
                dropBoundsBean.value.y.roundToInt(),
                dropBoundsBean.value.width,
                dropBoundsBean.value.height
            )
        }

        DisposableEffect(true) {
            onDispose {
                window.contentPane.remove(component)
            }
        }

        content()
    }
}

data class DropBoundsBean(
    var x: Float = 0f, var y: Float = 0f, var width: Int = 0, var height: Int = 0
)
