import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jthemedetecor.OsThemeDetector
import io.github.jixiaoyong.beans.AppState
import io.github.jixiaoyong.pages.settings.PageSettingInfo
import io.github.jixiaoyong.pages.signInfos.PageSignInfo
import io.github.jixiaoyong.pages.signInfos.SignInfoBean
import io.github.jixiaoyong.pages.signapp.PageSignApp
import io.github.jixiaoyong.theme.AppTheme
import io.github.jixiaoyong.utils.AppProcessUtil
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.ToasterUtil
import io.github.jixiaoyong.widgets.ButtonWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

/**
 * todo add ViewModel support
 */

object Routes {
    const val SignInfo = "signInfo"
    const val SignApp = "signApp"
    const val SettingInfo = "settingInfo"
}

@Composable
fun App(window: ComposeWindow) {
    val scope = rememberCoroutineScope()
    val settings = remember { SettingsTool(scope) }

    val routes = remember {
        listOf(
            Pair("\uD83D\uDD10 签名信息", Routes.SignInfo),
            Pair("\uD83D\uDD12 签名APP", Routes.SignApp),
            Pair("\uD83D\uDEE0 设置信息", Routes.SettingInfo)
        )
    }

    val pageIndex = remember { mutableStateOf(Routes.SignInfo) }
    // 将选择的apk信息提级避免切换页面时丢失
    val currentApkFilePath = remember { mutableStateOf<List<String>>(emptyList()) }
    val currentSingleApkPackageName = remember { mutableStateOf<String?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }
    val newSignInfo = remember { mutableStateOf(SignInfoBean()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val selectedSignInfo = settings.selectedSignInfoBean.first()
            if (selectedSignInfo != null) {
                pageIndex.value = Routes.SignApp
            }
        }
    }

    DisposableEffect(Unit) {
        val listener: (Boolean) -> Unit = { isDark: Boolean ->
            isDarkTheme = isDark
        }

        var detector: OsThemeDetector? = null
        scope.launch(Dispatchers.Default) {
            detector = OsThemeDetector.getDetector()
            detector?.registerListener(listener)
        }

        onDispose {
            detector?.removeListener(listener)
        }
    }

    ToasterUtil.init(isDarkTheme)

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            settings.apkSigner.first()?.let { ApkSigner.setupApkSigner(it) }
            settings.zipAlign.first()?.let { ApkSigner.setupZipAlign(it) }
            settings.aapt.first()?.let { ApkSigner.setupAapt(it) }
        }
    }

    AppTheme(darkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = 65.dp)
                    .background(MaterialTheme.colors.background)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (route in routes) {
                    val isActive = route.second == pageIndex.value
                    val backgroundColor = if (isActive) MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface
                    val textColor = if (isActive) Color.White else MaterialTheme.colors.onPrimary
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .background(backgroundColor, RoundedCornerShape(5.dp))
                            .clickable {
                                pageIndex.value = route.second
                            }.padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(route.first, style = TextStyle(color = textColor))
                    }
                }
            }
            Divider(color = MaterialTheme.colors.surface)

            when (pageIndex.value) {
                Routes.SignInfo -> PageSignInfo(window, settings, newSignInfo)

                Routes.SignApp -> PageSignApp(
                    window,
                    settings,
                    currentApkFilePath,
                    currentSingleApkPackageName
                ) { route ->
                    pageIndex.value = route
                }

                Routes.SettingInfo -> PageSettingInfo(window, settings)
            }
        }

    }
}

@Composable
fun LoadingPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text("Loading...", Modifier.padding(top = 10.dp))
    }
}

@Composable
fun AlreadyExistsPage(tryAgainFunc: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(300.dp).heightIn(200.dp).background(
                MaterialTheme.colors.surface.copy(0.8f),
                RoundedCornerShape(10.dp)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Warning,
                tint = Color.Red,
                contentDescription = "already exists",
                modifier = Modifier.size(50.dp)
            )
            Text("ApkSigner已经启动了，请不要重复启动", Modifier.padding(vertical = 20.dp))
            Row {
                ButtonWidget(onClick = { exitProcess(0) }) {
                    Text("退出")
                }
                ButtonWidget(onClick = tryAgainFunc) {
                    Text("重试")
                }
            }
        }
    }
}


fun main() = application {
    val windowState = rememberWindowState(height = 650.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "APK Signer",
        icon = painterResource("/imgs/icon.png"),
        state = windowState
    ) {
        var appState by remember { mutableStateOf<AppState>(AppState.Idle) }
        var checkDualRunning by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            window.minimumSize = window.size
        }

        LaunchedEffect(checkDualRunning) {
            appState = AppState.Loading
            val isAppRunning = withContext(Dispatchers.IO) {
                AppProcessUtil.isDualAppRunning("ApkSigner")
            }
            appState = if (isAppRunning) {
                AppState.AlreadyExists
            } else {
                AppState.Success
            }
        }

        when (appState) {
            is AppState.Idle, AppState.Loading -> {
                LoadingPage()
            }

            is AppState.AlreadyExists -> {
                AlreadyExistsPage {
                    checkDualRunning = !checkDualRunning
                }
            }

            AppState.Success -> {
                App(window)
            }
        }
    }
}


@Preview
@Composable
private fun PrevLoading() {
    LoadingPage()
}

@Preview
@Composable
private fun PrevAlreadyExists() {
    AlreadyExistsPage {}
}