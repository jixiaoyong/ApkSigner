package io.github.jixiaoyong.utils

import androidx.compose.ui.awt.ComposeWindow
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFileChooser

/**
 * @author : jixiaoyong
 * @description ：文件工具类
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */
object FileChooseUtil {

    /**
     * @return 返回选择的文件完整路径
     */
    fun chooseSignFile(
        window: ComposeWindow,
        title: String = "请选择文件",
        filter: FilenameFilter? = null
    ): String? {
        val fileDialog = java.awt.FileDialog(window)
        fileDialog.title = title
        fileDialog.isVisible = true
        fileDialog.isMultipleMode = false
        filter?.let {
            fileDialog.setFilenameFilter(it)
        }
        return fileDialog.files?.firstOrNull()?.absolutePath
    }


    /**
     * @return 返回选择的文件夹完整路径
     */
    fun chooseSignDirectory(
        window: ComposeWindow,
        title: String = "请选择文件",
        oldDirectory: String? = null
    ): String? {
        val jFileChooser = JFileChooser()
        jFileChooser.dialogTitle = title
        jFileChooser.currentDirectory = oldDirectory?.let { File(it) }
        jFileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        jFileChooser.showDialog(window.glassPane, "选择")
        return jFileChooser.currentDirectory?.absolutePath
    }
}