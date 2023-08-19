package pages.settings

import ApkSigner
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pages.signapp.DropBoxPanel
import pages.signapp.InfoItemWidget
import pages.utils.FileChooseUtil
import pages.utils.SettingsTool
import pages.utils.StorageKeys
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
@Preview
@Composable
fun PageSettingInfo(window: ComposeWindow, settings: SettingsTool) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val apkSign by settings.apkSigner.collectAsState(null)
    val zipAlign by settings.zipAlign.collectAsState(null)

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).scrollable(
                rememberScrollableState { return@rememberScrollableState 0f }, orientation = Orientation.Vertical
            )
        ) {
            DropBoxPanel(window,
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                    .background(color = Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(15.dp))
                    .padding(15.dp),
                component = JPanel(),
                onFileDrop = {
                    scope.launch {
                        val result = ApkSigner.init(it.first())

                        saveApkSigner(settings, ApkSigner.apkSignerPath)
                        saveZipAlign(settings, ApkSigner.zipAlignPath)

                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }) {
                Text(
                    text = "请拖拽Android SDK的build-tools文件夹到这里，以一次性修改apkSigner和zipAlign目录",
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            InfoItemWidget("apk signer目录", apkSign ?: "尚未初始化", onChange = {
                scope.launch {
                    val chooseFileName = FileChooseUtil.chooseSignFile(window, "请选择apksigner文件")
                    if (chooseFileName.isNullOrBlank()) {
                        scaffoldState.snackbarHostState.showSnackbar("请选择apksigner文件")
                    } else {
                        val result = ApkSigner.setupApkSigner(chooseFileName)
                        saveApkSigner(settings, ApkSigner.apkSignerPath)
                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }

            })
            InfoItemWidget("zip align目录", zipAlign ?: "尚未初始化", onChange = {
                scope.launch {
                    val chooseFileName = FileChooseUtil.chooseSignFile(window, "请选择zipAlign文件")
                    if (chooseFileName.isNullOrBlank()) {
                        scaffoldState.snackbarHostState.showSnackbar("请选择zipAlign文件")
                    } else {
                        val result = ApkSigner.setupZipAlign(chooseFileName)
                        saveZipAlign(settings, ApkSigner.zipAlignPath)
                        scaffoldState.snackbarHostState.showSnackbar(result ?: "修改成功")
                    }
                }
            })

        }
    }

}

private fun saveApkSigner(settings: SettingsTool, apkSigner: String?) {
    settings.save(StorageKeys.APK_SIGNER_PATH, Json.encodeToString(apkSigner))
}

private fun saveZipAlign(settings: SettingsTool, zipAlign: String?) {
    settings.save(StorageKeys.ZIP_ALIGN_PATH, Json.encodeToString(zipAlign))
}