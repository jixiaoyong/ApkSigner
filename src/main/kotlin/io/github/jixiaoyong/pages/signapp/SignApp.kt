package io.github.jixiaoyong.pages.signapp

import ApkSigner
import CommandResult
import Logger
import Routes
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import io.github.jixiaoyong.pages.signInfos.SignInfoBean
import io.github.jixiaoyong.widgets.ButtonWidget
import io.github.jixiaoyong.widgets.InfoItemWidget
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
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
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/18
 */

@Preview
@Composable
fun PageSignApp(
    window: ComposeWindow,
    selectedSignInfo: SignInfoBean?,
    currentApkFilePath: String?,
    onChangeApk: (String) -> Unit,
    onChangePage: (String) -> Unit
) {

    val scope = rememberCoroutineScope()
    var signLogs by remember { mutableStateOf(listOf<String>()) }
    var signApkResult: CommandResult by remember { mutableStateOf(CommandResult.NOT_EXECUT) }
    val scaffoldState = rememberScaffoldState()
    val logScrollState = rememberScrollState()

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).scrollable(
                rememberScrollableState { return@rememberScrollableState 0f },
                orientation = Orientation.Vertical
            ),
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
                        val file = it.firstOrNull { it.toLowerCase().endsWith(".apk") }
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
            InfoItemWidget("当前选择的文件", currentApkFilePath ?: "", showChangeButton = false)
            InfoItemWidget(
                "当前选择的签名文件",
                selectedSignInfo?.keyNickName + selectedSignInfo?.keyStorePath,
                onChange = {
                    onChangePage(Routes.SignInfo)
                })

            ButtonWidget({
                scope.launch {
                    if (currentApkFilePath.isNullOrBlank() || !currentApkFilePath.toLowerCase()
                            .endsWith(".apk")
                    ) {
                        scaffoldState.snackbarHostState.showSnackbar("请先选择正确的apk文件")
                        return@launch
                    }

                    if (selectedSignInfo?.isValid() != true) {
                        onChangePage(Routes.SignInfo)
                        scaffoldState.snackbarHostState.showSnackbar("请先配置正确的签名文件")
                        return@launch
                    }

                    if (!ApkSigner.isInitialized()) {
                        onChangePage(Routes.SettingInfo)
                        scaffoldState.snackbarHostState.showSnackbar("请先配置apksigner和zipalign路径")
                        return@launch
                    }

                    signApkResult = ApkSigner.alignAndSignApk(
                        currentApkFilePath,
                        selectedSignInfo.keyStorePath,
                        selectedSignInfo.keyAlias,
                        selectedSignInfo.keyStorePassword,
                        selectedSignInfo.keyPassword,
                        onProgress = { line ->
                            scope.launch {
                                signLogs = mutableListOf<String>().apply {
                                    addAll(signLogs)
                                    add(line)
                                }
                            }
                        }
                    )

                    Logger.log("result:$signApkResult")
                    if (signApkResult is CommandResult.Success<*>) {
                        val result = scaffoldState.snackbarHostState.showSnackbar(
                            "签名成功，是否打开签名后的文件？",
                            "打开",
                            SnackbarDuration.Long
                        )

                        if (SnackbarResult.ActionPerformed == result) {
                            // todo 打开文件夹
                        }
                    }

                    signApkResult = CommandResult.NOT_EXECUT
                }
            }, enabled = CommandResult.NOT_EXECUT == signApkResult, title = "开始签名apk")

            LazyColumn(
                modifier = Modifier.scrollable(
                    logScrollState,
                    orientation = Orientation.Vertical,
                    reverseDirection = true
                ).fillMaxWidth()
                    .heightIn(100.dp, 200.dp)
                    .padding(vertical = 5.dp)
                    .background(MaterialTheme.colors.surface)
            ) {
                items(signLogs) {
                    Text(it)
                }
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

            val target = object : DropTarget(component, object : DropTargetAdapter() {
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
