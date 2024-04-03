package io.github.jixiaoyong.pages

import Logger
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
fun App(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()

    val routes = listOf(
        Triple(Icons.Default.List, strings.signConfig, Routes.SignInfo),
        Triple(Icons.Default.Lock, strings.signApp, Routes.SignApp),
        Triple(Icons.Default.Settings, strings.settingsConfig, Routes.SettingInfo),
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val hasSelectedSignature by viewModel.selectedSignatureInfo.collectAsState()
    var localThemeMode by remember { mutableStateOf(isDarkTheme ?: false) }

    // 将viewModel放在这里避免切换页面时丢失
    val signInfoViewModel = viewModel { SignInfoViewModel() }
    val signAppViewModel = viewModel { SignAppViewModel() }

    val navController: NavHostController = rememberNavController()

    LaunchedEffect(hasSelectedSignature) {
        if (hasSelectedSignature) {
            navigationToPage(navController, Routes.SignApp)
        }
    }

    DisposableEffect(isDarkTheme) {
        val listener: (Boolean) -> Unit = { isDark: Boolean ->
            localThemeMode = isDarkTheme ?: isDark
        }

        var detector: OsThemeDetector? = null
        scope.launch(Dispatchers.Default) {
            try {
                detector = OsThemeDetector.getDetector()
                detector?.registerListener(listener)
                detector?.isDark?.let {
                    localThemeMode = isDarkTheme ?: it
                }
            } catch (e: Exception) {
                Logger.error("detect system theme error", e)
            }
        }

        onDispose {
            detector?.removeListener(listener)
        }
    }

    ToasterUtil.init(localThemeMode)

    AppTheme(darkTheme = localThemeMode) {
        Scaffold(
            topBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.route ?: Routes.SignInfo
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 65.dp)
                            .background(MaterialTheme.colors.secondaryVariant).padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (route in routes) {
                            val isActive = route.third == currentDestination
                            val backgroundColor = if (isActive) {
                                MaterialTheme.colors.secondary
                            } else {
                                Color.Transparent
                            }

                            Row(
                                modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
                                    .background(backgroundColor, RoundedCornerShape(5.dp))
                                    .clickable {
                                        navigationToPage(navController, route.third)
                                    }
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
                }
            }) {
            Column(modifier = Modifier.fillMaxSize()) {

                NavHost(navController, startDestination = Routes.SignInfo) {
                    composable(Routes.SignInfo) {
                        PageSignInfo(signInfoViewModel)
                    }
                    composable(Routes.SignApp) {
                        PageSignApp(signAppViewModel) { route ->
                            navigationToPage(navController, route)
                        }
                    }
                    composable(Routes.SettingInfo) {
                        PageSettingInfo()
                    }
                }
            }
        }

    }
}


private fun navigationToPage(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().navigatorName) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
