package io.github.jixiaoyong.utils

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFileChooser

/**
 * @author : jixiaoyong
 * @description ：文件与目录选择工具类（全异步挂起执行，避免阻塞 Compose UI 协程导致嵌套重入崩溃）
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */
object FileChooseUtil {

    /** @return 返回选择的文件完整路径 */
    suspend fun chooseSignFile(
        window: ComposeWindow,
        title: String = "请选择文件",
        filter: FilenameFilter? = null
    ): String? = withContext(Dispatchers.IO) {
        val fileDialog = java.awt.FileDialog(window).apply {
            this.title = title
            this.isMultipleMode = false
            filter?.let { this.setFilenameFilter(it) }
        }
        fileDialog.isVisible = true
        fileDialog.files?.firstOrNull()?.absolutePath
    }

    /**
     * 选择多个文件
     * @return 返回选择的文件完整路径
     */
    suspend fun chooseMultiFile(
        window: ComposeWindow,
        title: String = "请选择文件(可多选)",
        filter: FilenameFilter? = null
    ): List<String>? = withContext(Dispatchers.IO) {
        val fileDialog = java.awt.FileDialog(window).apply {
            this.title = title
            this.isMultipleMode = true
            this.filenameFilter = filter
        }
        fileDialog.isVisible = true
        fileDialog.files?.mapNotNull { it.absolutePath }
    }

    /** @return 返回选择的文件夹完整路径 */
    suspend fun chooseSignDirectory(
        window: ComposeWindow,
        title: String = "请选择文件夹",
        oldDirectory: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val jFileChooser = JFileChooser().apply {
            dialogTitle = title
            currentDirectory = oldDirectory?.let { File(it) }
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }
        val result = jFileChooser.showOpenDialog(window)
        if (result == JFileChooser.APPROVE_OPTION) {
            jFileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}
