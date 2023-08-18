import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pages.settings.PageSettingInfo
import pages.signInfos.PageSignInfo
import pages.signInfos.SignInfoBean
import pages.signapp.PageSignApp

object Routes {
    const val SignInfo = "signInfo"
    const val SignApp = "signApp"
    const val SettingInfo = "settingInfo"
}

@Composable
@Preview
fun App(window: ComposeWindow) {

    val pageIndex = remember {
        mutableStateOf(Routes.SignInfo)
    }

    val routes = remember {
        listOf(
            Pair("✍️签名信息", Routes.SignInfo),
            Pair("🔒签名APP", Routes.SignApp),
            Pair("⚙️设置信息", Routes.SettingInfo)
        )
    }

    var selectedSignInfo by remember { mutableStateOf(SignInfoBean()) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(color = Color.White, shape = RectangleShape),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (route in routes) {
                    Button(onClick = {
                        pageIndex.value = route.second
                    }) {
                        Text(route.first)
                    }
                }
            }
            when (pageIndex.value) {
                Routes.SignInfo -> PageSignInfo(selectedSignInfo) {
                    selectedSignInfo = it
                }

                Routes.SignApp -> PageSignApp(window, selectedSignInfo) {
                    pageIndex.value = Routes.SignInfo
                }

                Routes.SettingInfo -> PageSettingInfo()
            }
        }

    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "APK Signer") {
        App(window)
    }
}
