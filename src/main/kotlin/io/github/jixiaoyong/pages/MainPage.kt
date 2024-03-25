package io.github.jixiaoyong.pages

import ApkSigner
import LocalSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jthemedetecor.OsThemeDetector
import io.github.jixiaoyong.base.viewModel
import io.github.jixiaoyong.pages.settings.PageSettingInfo
import io.github.jixiaoyong.pages.signInfos.PageSignInfo
import io.github.jixiaoyong.pages.signInfos.SignInfoViewModel
import io.github.jixiaoyong.pages.signapp.PageSignApp
import io.github.jixiaoyong.pages.signapp.SignAppViewModel
import io.github.jixiaoyong.theme.AppTheme
import io.github.jixiaoyong.utils.ToasterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author : jixiaoyong
 * @description ：Main Page
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
@Composable
fun App() {
    val settings = LocalSettings.current

    val scope = rememberCoroutineScope()
    val viewModel = viewModel { MainViewModel() }

    var isDarkTheme by viewModel.isDarkTheme
    val pageIndex by viewModel.currentIndex

    // 将viewModel放在这里避免切换页面时丢失
    val signInfoViewModel = viewModel { SignInfoViewModel() }
    val signAppViewModel = viewModel { SignAppViewModel() }

    LaunchedEffect(Unit) {
        scope.launch {
            val selectedSignInfo = settings.selectedSignInfoBean.first()
            if (selectedSignInfo != null) {
                viewModel.changePage(Routes.SignApp)
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
                for (route in viewModel.routes) {
                    val isActive = route.second == pageIndex
                    val backgroundColor = if (isActive) MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface
                    val textColor = if (isActive) Color.White else MaterialTheme.colors.onPrimary
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .background(backgroundColor, RoundedCornerShape(5.dp))
                            .clickable {
                                viewModel.changePage(route.second)
                            }.padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(route.first, style = TextStyle(color = textColor))
                    }
                }
            }
            Divider(color = MaterialTheme.colors.surface)

            when (pageIndex) {
                Routes.SignInfo -> PageSignInfo(signInfoViewModel)

                Routes.SignApp -> PageSignApp(signAppViewModel) { route ->
                    viewModel.changePage(route)
                }

                Routes.SettingInfo -> PageSettingInfo()
            }
        }
    }
}
