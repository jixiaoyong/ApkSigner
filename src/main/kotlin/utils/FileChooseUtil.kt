package utils

import androidx.compose.ui.awt.ComposeWindow
import java.awt.SystemColor.window
import java.io.FilenameFilter

/**
 * @author : jixiaoyong
 * @description ：TODO
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */
object FileChooseUtil {

    /**
     * @return 返回选择的文件
     */
    fun chooseSignFile(window: ComposeWindow, title: String = "请选择文件", filter: FilenameFilter? = null): String? {
        val fileDialog = java.awt.FileDialog(window)
        fileDialog.title = title
        fileDialog.isVisible = true
        fileDialog.isMultipleMode = false
        filter?.let {
            fileDialog.setFilenameFilter(it)
        }
        return fileDialog.files?.firstOrNull()?.absolutePath
    }

}