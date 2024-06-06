package io.github.jixiaoyong.pages.settings

import ApkSigner
import LocalDatastore
import LocalLyricist
import LocalWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.lyricist.strings
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import io.github.jixiaoyong.BuildConfig
import io.github.jixiaoyong.base.viewModel
import io.github.jixiaoyong.i18n.Locale
import io.github.jixiaoyong.pages.signapp.DropBoxPanel
import io.github.jixiaoyong.utils.FileChooseUtil
import io.github.jixiaoyong.utils.ToastConfig
import io.github.jixiaoyong.utils.showToast
import io.github.jixiaoyong.widgets.*
import kotlinx.coroutines.launch
import java.io.File
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

const val PROJECT_WEBSITE = "https://github.com/jixiaoyong/ApkSigner"

@Composable
fun PageSettingInfo() {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val window = LocalWindow.current
    val i18nString = strings
    val lyricist = LocalLyricist.current
    val repository = LocalDatastore.current

    val viewModel = viewModel { SettingInfoViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    val resetConfig = uiState.resetInfo
    var showThemeModeDialog by remember { mutableStateOf(false) }

    val themeMode = uiState.isDarkMode
    var localThemeMode by remember { mutableStateOf(themeMode) }
    val themeModeDesc = when (themeMode) {
        true -> i18nString.darkMode
        false -> i18nString.lightMode
        else -> i18nString.themeModeAuto
    }

    LaunchedEffect(themeMode) {
        localThemeMode = themeMode ?: localThemeMode
    }

    PopWidget(title = i18nString.confirmReset,
        show = resetConfig.showResetDialog,
        onDismiss = { viewModel.toggleResetDialog() },
        onConfirm = {
            viewModel.runRestConfig()
            viewModel.toggleResetDialog()
        }) {
        Column(modifier = Modifier.widthIn(350.dp).padding(start = 30.dp)) {
            Text(i18nString.confirmResetTips)
            CheckBox(checked = resetConfig.resetSignInfo,
                title = i18nString.signConfig,
                modifier = Modifier.fillMaxWidth(),
                onCheckedChange = {
                    viewModel.updateResetConfig(resetSignInfo = !resetConfig.resetSignInfo)
                })
            CheckBox(checked = resetConfig.resetApkTools, modifier = Modifier.fillMaxWidth(),
                title = i18nString.signToolsConfigResetTips,
                onCheckedChange = { viewModel.updateResetConfig(resetApkTools = !resetConfig.resetApkTools) })
            CheckBox(checked = resetConfig.resetSignTypes, modifier = Modifier.fillMaxWidth(),
                title = i18nString.signType,
                onCheckedChange = { viewModel.updateResetConfig(resetSignTypes = !resetConfig.resetSignTypes) })
            CheckBox(checked = resetConfig.resetSignedDirectory, modifier = Modifier.fillMaxWidth(),
                title = i18nString.signedApkOutputDir,
                onCheckedChange = { viewModel.updateResetConfig(resetSignedDirectory = !resetConfig.resetSignedDirectory) })
        }
    }

    var currentLanguage by remember { mutableStateOf(lyricist.languageTag) }
    PopWidget(title = i18nString.changeLanguage,
        show = resetConfig.showChangeLanguageDialog,
        onDismiss = { viewModel.toggleLanguageDialog() },
        onConfirm = {
            lyricist.languageTag = currentLanguage
            viewModel.changeLanguage(currentLanguage)
            viewModel.toggleLanguageDialog()
        }) {
        Column(modifier = Modifier.wrapContentWidth()) {
            Locale.values().map { item ->
                CheckBox(checked = item.code == currentLanguage,
                    title = item.languageName,
                    modifier = Modifier.padding(start = 50.dp).fillMaxWidth(),
                    onCheckedChange = {
                        if (it) {
                            currentLanguage = item.code
                        }
                    })
            }
        }
    }

    PopWidget(
        title = i18nString.themeMode,
        show = showThemeModeDialog,
        onDismiss = { showThemeModeDialog = false },
        onConfirm = {
            viewModel.changeThemeMode(localThemeMode)
            showThemeModeDialog = false
        }) {
        Column {
            val themeModeMap =
                mapOf(true to i18nString.darkMode, false to i18nString.lightMode, null to i18nString.themeModeAuto)

            themeModeMap.forEach { pair ->
                CheckBox(checked = localThemeMode == pair.key,
                    title = pair.value,
                    modifier = Modifier.padding(start = 50.dp).fillMaxWidth(),
                    onCheckedChange = {
                        if (it) {
                            localThemeMode = pair.key
                        }
                    })
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp).verticalScroll(rememberScrollState())
        ) {
            DropBoxPanel(window,
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(10.dp)
                    .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(10.dp)).padding(15.dp)
                    .clickable {
                        scope.launch {
                            val oldDirectory = uiState.apkSign?.substringBeforeLast(File.separator)
                            val chooseFileName = FileChooseUtil.chooseSignDirectory(
                                window, i18nString.chooseBuildTools, oldDirectory
                            )
                            if (chooseFileName.isNullOrBlank()) {
                                showToast(i18nString.chooseBuildTools, ToastConfig.DURATION.Long)
                            } else {
                                viewModel.setupBuildToolsConfig(chooseFileName)
                            }
                        }
                    },
                component = JPanel(),
                onFileDrop = { scope.launch { viewModel.setupBuildToolsConfig(it.first()) } }) {
                Text(
                    text = i18nString.chooseBuildToolsTips,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }

            InfoItemWidget(i18nString.apksignerDirectory,
                uiState.apkSign ?: i18nString.notInit,
                description = i18nString.chooseApksignerTips,
                icon = FontAwesomeIcons.Solid.UserShield,
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, i18nString.plzChooseApksigner)
                        if (chooseFileName.isNullOrBlank()) {
                            showToast(i18nString.plzChooseApksigner, ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupApkSigner(chooseFileName)
                            viewModel.saveApkSigner(ApkSigner.apkSignerPath)
                            showToast(result ?: i18nString.changeSuccess)
                        }
                    }

                })
            InfoItemWidget(i18nString.zipDirectory,
                uiState.zipAlign ?: i18nString.notInit,
                description = i18nString.chooseZipTips,
                icon = FontAwesomeIcons.Solid.AlignLeft,
                onClick = {
                    scope.launch {
                        val chooseFileName = FileChooseUtil.chooseSignFile(window, i18nString.plzChooseZip)
                        if (chooseFileName.isNullOrBlank()) {
                            showToast(i18nString.plzChooseZip, ToastConfig.DURATION.Long)
                        } else {
                            val result = ApkSigner.setupZipAlign(chooseFileName)
                            viewModel.saveZipAlign(ApkSigner.zipAlignPath)
                            showToast(result ?: i18nString.changeSuccess)
                        }
                    }
                })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    FontAwesomeIcons.Solid.CheckSquare,
                    contentDescription = i18nString.autoMatchSignature, tint = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(end = 10.dp).size(25.dp)
                )

                Column(modifier = Modifier.weight(1f, true)) {
                    Text(
                        i18nString.autoMatchSignature,
                        style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    )
                    Text(
                        i18nString.autoMatchSignatureTips, style = TextStyle(
                            fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colors.onSecondary
                        )
                    )
                }
                SwitchButton(
                    uiState.isAutoMatchSignature,
                ) { autoMatch -> viewModel.saveAutoMatchSignature(autoMatch) }
            }

            if (uiState.isAutoMatchSignature) Column {
                Divider(
                    modifier = Modifier.padding(horizontal = 15.dp)
                        .background(color = MaterialTheme.colors.secondary.copy(0.65f))
                )
                InfoItemWidget(i18nString.aaptDirectory,
                    uiState.aapt ?: i18nString.notInit,
                    description = i18nString.aaptDirectoryTips,
                    onClick = {
                        scope.launch {
                            val chooseFileName = FileChooseUtil.chooseSignFile(window, i18nString.chooseAaptDirectory)
                            if (chooseFileName.isNullOrBlank()) {
                                showToast(i18nString.chooseAaptDirectory, ToastConfig.DURATION.Long)
                            } else {
                                val result = ApkSigner.setupAapt(chooseFileName)
                                viewModel.saveAapt(ApkSigner.aaptPath)
                                showToast(result ?: i18nString.changeSuccess)
                            }
                        }
                    })
            }

            InfoItemWidget(
                i18nString.currentLanguageTitle,
                Locale.getLocale(lyricist.languageTag).languageName,
                icon = FontAwesomeIcons.Solid.Language,
                onClick = {
                    viewModel.toggleLanguageDialog()
                })

            InfoItemWidget(
                i18nString.themeMode,
                icon = FontAwesomeIcons.Solid.Sun,
                value = null,
                description = themeModeDesc,
                onClick = {
                    showThemeModeDialog = true
                }) {}

            InfoItemWidget(
                i18nString.logFileDirectory,
                icon = FontAwesomeIcons.Solid.FileAlt,
                value = null,
                description = uiState.logFileDirectory,
                buttonTitle = i18nString.open,
                onClick = {
                    val result = viewModel.openLogDirectory()
                    if (!result) {
                        showToast(i18nString.openLogDirectoryFailed)
                    }else{
                        showToast(i18nString.openLogDirectorySucceed)
                    }
                }) {}

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 80.dp)) {
                ButtonWidget(
                    onClick = { viewModel.toggleResetDialog() },
                    title = i18nString.reset,
                    modifier = Modifier.size(250.dp, 50.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val str = i18nString.appIntro(BuildConfig.PACKAGE_VERSION, PROJECT_WEBSITE)
                val startIndex = str.indexOf(PROJECT_WEBSITE)
                val endIndex = startIndex + PROJECT_WEBSITE.length

                val annotatedString = buildAnnotatedString {
                    append(str)
                    addStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colors.primary, textDecoration = TextDecoration.Underline
                        ), start = startIndex, end = endIndex
                    )
                    addStringAnnotation("URL", PROJECT_WEBSITE, startIndex, endIndex)
                }
                val uriHandler = LocalUriHandler.current
                ClickableText(text = annotatedString, style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colors.onSecondary
                ), onClick = { offset ->
                    annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
                })
            }
        }
    }

}
