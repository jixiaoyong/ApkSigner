import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.lyricist.Lyricist
import cafe.adriel.lyricist.rememberStrings
import io.github.jixiaoyong.beans.AppState
import io.github.jixiaoyong.i18n.EnStrings
import io.github.jixiaoyong.i18n.Locales
import io.github.jixiaoyong.i18n.Strings
import io.github.jixiaoyong.i18n.ZhStrings
import io.github.jixiaoyong.pages.App
import io.github.jixiaoyong.utils.AppProcessUtil
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.widgets.ButtonWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

val LocalWindow = compositionLocalOf<ComposeWindow> { error("No Window provided") }
val LocalSettings = compositionLocalOf<SettingsTool> { error("No SettingsTool provided") }
val LocalStrings = compositionLocalOf<Lyricist<Strings>> { error("No SettingsTool provided") }

fun main() =
    application {
        val windowState = rememberWindowState(height = 650.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "APK Signer",
            icon = painterResource("/imgs/icon.png"),
            state = windowState,
        ) {
            var appState by remember { mutableStateOf<AppState>(AppState.Idle) }
            var checkDualRunning by remember { mutableStateOf(true) }

            val lyricist = rememberStrings(mapOf(Locales.ZH to ZhStrings, Locales.EN to EnStrings))

            LaunchedEffect(Unit) {
                window.minimumSize = window.size
            }
            CompositionLocalProvider(
                LocalWindow provides window,
                LocalSettings provides SettingsTool(scope = rememberCoroutineScope()),
                LocalStrings provides lyricist
            ) {
                LaunchedEffect(checkDualRunning) {
                    appState = AppState.Loading
                    val isAppRunning = withContext(Dispatchers.IO) {
                        AppProcessUtil.isDualAppRunning("ApkSigner")
                    }
                    appState =
                        if (isAppRunning) {
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
                        App()
                    }
                }
            }
        }
    }

@Composable
fun LoadingPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(LocalStrings.current.strings.loading, Modifier.padding(top = 10.dp))
    }
}

@Composable
fun AlreadyExistsPage(tryAgainFunc: () -> Unit) {
    val i18nString = LocalStrings.current.strings
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
            Modifier.widthIn(300.dp).heightIn(200.dp).background(
                MaterialTheme.colors.surface.copy(0.8f),
                RoundedCornerShape(10.dp),
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Warning,
                tint = MaterialTheme.colors.error,
                contentDescription = "already exists",
                modifier = Modifier.size(50.dp),
            )
            Text(i18nString.alreadyRunning, Modifier.padding(vertical = 20.dp))
            Row {
                ButtonWidget(onClick = { exitProcess(0) }) {
                    Text(i18nString.exit)
                }
                ButtonWidget(onClick = tryAgainFunc) {
                    Text(i18nString.retry)
                }
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
