import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jthemedetecor.OsThemeDetector
import io.github.jixiaoyong.pages.settings.PageSettingInfo
import io.github.jixiaoyong.pages.signInfos.PageSignInfo
import io.github.jixiaoyong.pages.signapp.PageSignApp
import io.github.jixiaoyong.theme.AppTheme
import io.github.jixiaoyong.utils.SettingsTool
import kotlinx.coroutines.flow.first

object Routes {
    const val SignInfo = "signInfo"
    const val SignApp = "signApp"
    const val SettingInfo = "settingInfo"
}

@Composable
@Preview
fun App(window: ComposeWindow) {
    val scope = rememberCoroutineScope()
    val settings = remember {
        SettingsTool(scope)
    }

    val routes = remember {
        listOf(
            Pair("\uD83D\uDD10 签名信息", Routes.SignInfo),
            Pair("\uD83D\uDD12 签名APP", Routes.SignApp),
            Pair("\uD83D\uDEE0 设置信息", Routes.SettingInfo)
        )
    }


    val pageIndex = remember {
        mutableStateOf(Routes.SignInfo)
    }

    var currentApkFilePath by remember { mutableStateOf<String?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }

    val detector: OsThemeDetector = OsThemeDetector.getDetector()
    detector.registerListener { isDark ->
        isDarkTheme = isDark
    }


    LaunchedEffect(Unit) {
        settings.apkSigner.first()?.let { ApkSigner.setupApkSigner(it) }
        settings.zipAlign.first()?.let { ApkSigner.setupZipAlign(it) }
    }

    AppTheme(darkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 65.dp)
                    .background(
                        color = MaterialTheme.colors.surface.copy(alpha = .2f),
                        shape = RectangleShape
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (route in routes) {
                    val isActive = route.second == pageIndex.value
                    val backgroundColor = if (isActive) MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface
                    val textColor = if (isActive) Color.White else MaterialTheme.colors.onPrimary
                    Row(
                        modifier = Modifier.background(backgroundColor, RoundedCornerShape(15.dp))
                            .clickable {
                                pageIndex.value = route.second
                            }.padding(horizontal = 35.dp, vertical = 15.dp)
                    ) {
                        Text(route.first, style = TextStyle(color = textColor))
                    }
                }
            }
            when (pageIndex.value) {
                Routes.SignInfo -> PageSignInfo(window, settings)

                Routes.SignApp -> PageSignApp(
                    window,
                    currentApkFilePath,
                    settings,
                    {
                        currentApkFilePath = it
                    }) { route ->
                    pageIndex.value = route
                }

                Routes.SettingInfo -> PageSettingInfo(window, settings)
            }
        }

    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "APK Signer",
        icon = painterResource("/imgs/icon.png")
    ) {
        App(window)
    }
}
