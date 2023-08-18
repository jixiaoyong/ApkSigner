package pages.signapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import pages.signInfos.SignInfoBean
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
fun PageSignApp(window: ComposeWindow, selectedSignInfo: SignInfoBean, onChangeSignInfo: () -> Unit) {

    var currentApkFilePath by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).scrollable(
            rememberScrollableState { return@rememberScrollableState 0f }, orientation = Orientation.Vertical
        )
    ) {
        DropBoxPanel(
            window,
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                .background(color = Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(15.dp))
                .padding(15.dp),
            component = JPanel(),
            onFileDrop = {
                currentApkFilePath = it.first()
            }
        )
        Text("当前选择的文件：\n${currentApkFilePath}")
        Text("当前选择的签名文件：\n${selectedSignInfo.keyNickName}--${selectedSignInfo.keyStorePath}")
        Button({ onChangeSignInfo() }) {
            Text("重选签名")
        }

        Button({
            // todo 签名apk的逻辑
        }) {
            Text("开始签名apk")
        }
    }
}

@Composable
fun DropBoxPanel(
    window: ComposeWindow,
    modifier: Modifier = Modifier,
    component: JPanel = JPanel(),
    onFileDrop: (List<String>) -> Unit
) {

    val dropBoundsBean = remember {
        mutableStateOf(DropBoundsBean())
    }

    Box(modifier = modifier.onPlaced {
        dropBoundsBean.value = DropBoundsBean(
            x = it.positionInWindow().x, y = it.positionInWindow().y, width = it.size.width, height = it.size.height
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

        Text(text = "请拖拽apk文件到这里哦", modifier = Modifier.align(alignment = Alignment.Center))

    }
}

data class DropBoundsBean(
    var x: Float = 0f, var y: Float = 0f, var width: Int = 0, var height: Int = 0
)
