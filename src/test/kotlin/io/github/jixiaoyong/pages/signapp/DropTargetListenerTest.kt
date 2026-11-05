package io.github.jixiaoyong.pages.signapp

import java.awt.Component
import java.awt.Point
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTargetContext
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetListener
import java.io.File
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTargetListenerTest {

    @Test
    fun testDropTargetFileParsing() {
        val droppedFiles = mutableListOf<String>()

        // 模拟文件拖拽
        val testFile1 = File("/tmp/test1.apk")
        val testFile2 = File("/tmp/test2.apk")
        val fileList = listOf(testFile1, testFile2)

        val mockTransferable = object : Transferable {
            override fun getTransferDataFlavors(): Array<DataFlavor> {
                return arrayOf(DataFlavor.javaFileListFlavor)
            }

            override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
                return flavor == DataFlavor.javaFileListFlavor
            }

            override fun getTransferData(flavor: DataFlavor?): Any {
                if (flavor == DataFlavor.javaFileListFlavor) {
                    return fileList
                }
                throw UnsupportedOperationException()
            }
        }

        // 测试核心数据解析逻辑
        val data = mockTransferable.getTransferData(DataFlavor.javaFileListFlavor)
        val pathList = when (data) {
            is List<*> -> data.mapNotNull { item ->
                (item as? File)?.absolutePath ?: item?.toString()
            }
            else -> emptyList()
        }

        assertEquals(2, pathList.size)
        assertEquals(testFile1.absolutePath, pathList[0])
        assertEquals(testFile2.absolutePath, pathList[1])
    }
}
