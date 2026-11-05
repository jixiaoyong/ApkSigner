package io.github.jixiaoyong.pages.signapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.JPanel

/**
 * @author : jixiaoyong
 * @description ：识别拖拽文件的区域（使用 Compose 官方 Modifier.dragAndDropTarget 原生跨平台，零 Swing 侵入）
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 29/3/2024
 */

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DropBoxPanel(
    window: ComposeWindow? = null,
    modifier: Modifier = Modifier,
    component: JPanel? = null,
    onFileDrop: (List<String>) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val currentOnFileDrop by rememberUpdatedState(onFileDrop)
    var isDraggingOver by remember { mutableStateOf(false) }

    val dndTarget = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                isDraggingOver = true
            }

            override fun onExited(event: DragAndDropEvent) {
                isDraggingOver = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDraggingOver = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDraggingOver = false
                val transferable = event.awtTransferable
                try {
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                        val pathList = files?.mapNotNull { (it as? File)?.absolutePath ?: it?.toString() } ?: emptyList()
                        if (pathList.isNotEmpty()) {
                            currentOnFileDrop(pathList)
                            return true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }
        }
    }

    Box(
        modifier = modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    try {
                        event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    } catch (e: Exception) {
                        true
                    }
                },
                target = dndTarget
            )
            .then(
                if (isDraggingOver) {
                    Modifier.border(2.dp, MaterialTheme.colors.primary, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
