package io.github.jixiaoyong.pages

import LocalSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.strings
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
import kotlinx.coroutines.launch

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
    val viewModel = viewModel { MainViewModel(settings) }

    val routes = listOf(
        Triple(Icons.Default.List, strings.signConfig, Routes.SignInfo),
        Triple(Icons.Default.Lock, strings.signApp, Routes.SignApp),
        Triple(Icons.Default.Settings, strings.settingsConfig, Routes.SettingInfo),
    )

    var isDarkTheme by viewModel.isDarkTheme
    val pageIndex by viewModel.currentIndex.collectAsState()

    // 将viewModel放在这里避免切换页面时丢失
    val signInfoViewModel = viewModel { SignInfoViewModel(settings) }
    val signAppViewModel = viewModel { SignAppViewModel(settings) }

    DisposableEffect(Unit) {
        val listener: (Boolean) -> Unit = { isDark: Boolean ->
            isDarkTheme = isDark
        }

        var detector: OsThemeDetector? = null
        scope.launch(Dispatchers.Default) {
            detector = OsThemeDetector.getDetector()
            detector?.registerListener(listener)
            detector?.isDark?.let {
                isDarkTheme = it
            }
        }

        onDispose {
            detector?.removeListener(listener)
        }
    }

    ToasterUtil.init(isDarkTheme)

    AppTheme(darkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 65.dp)
                    .background(MaterialTheme.colors.secondaryVariant).padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (route in routes) {
                    val isActive = route.third == pageIndex
                    val backgroundColor = if (isActive) {
                        MaterialTheme.colors.secondary
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
                            .background(backgroundColor, RoundedCornerShape(5.dp))
                            .clickable { viewModel.changePage(route.third) }
                            .padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            route.first,
                            contentDescription = route.second,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(end = 5.dp).size(18.dp)
                        )
                        Text(route.second, color = MaterialTheme.colors.onBackground)
                    }
                }
            }

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
