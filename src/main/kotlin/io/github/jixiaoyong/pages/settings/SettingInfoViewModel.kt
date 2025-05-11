package io.github.jixiaoyong.pages.settings

import ApkSigner
import Logger
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.data.SettingPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS.*
import org.jetbrains.skiko.hostOs

/**
 * @author : jixiaoyong
 * @description ：setting info view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SettingInfoViewModel(private val repository: SettingPreferencesRepository) : BaseViewModel() {

    private val uiStateFlow: MutableStateFlow<SettingInfoUiState> =
        MutableStateFlow(SettingInfoUiState())
    val uiState = uiStateFlow.asStateFlow()

    override fun onInit() {

        combine(
            repository.apkSigner,
            repository.zipAlign,
            repository.aapt,
            repository.isAutoMatchSignature,
            repository.isDarkMode,
        ) { apkSigner, zipAlign, aapt, isAutoMatchSignature, isDarkMode ->
            uiStateFlow.value.copy(
                apkSign = apkSigner,
                zipAlign = zipAlign,
                aapt = aapt,
                isAutoMatchSignature = isAutoMatchSignature,
                isDarkMode = isDarkMode
            )
        }
            .onEach { uiStateFlow.emit(it) }
            .launchIn(viewModelScope)

        uiStateFlow.update {
            val logFile = Logger.getLogFileDirectory().toString()
            it.copy(logFileDirectory = logFile)
        }

        viewModelScope.launch {
            repository.javaHome.collect { jdkInfo ->
                val jdkInfoData =
                    if (jdkInfo != null) {
                        jdkInfo
                    } else {
                        val javaHomeEnv = System.getenv("JAVA_HOME")
                        val javaVersion = getJdkVersion(javaHomeEnv)
                        JdkInfo(javaHome = javaHomeEnv, jdkVersion = javaVersion)
                    }
                uiStateFlow.update { it.copy(javaHome = jdkInfoData) }
            }
        }
    }

    fun toggleResetDialog() {
        uiStateFlow.value =
            uiStateFlow.value.copy(
                resetInfo =
                    uiStateFlow.value.resetInfo.copy(
                        showResetDialog =
                            !uiStateFlow.value.resetInfo.showResetDialog
                    )
            )
    }

    fun toggleLanguageDialog() {
        uiStateFlow.value =
            uiStateFlow.value.copy(
                resetInfo =
                    uiStateFlow.value.resetInfo.copy(
                        showChangeLanguageDialog =
                            !uiStateFlow
                                .value
                                .resetInfo
                                .showChangeLanguageDialog
                    )
            )
    }

    /** Updates the reset configuration values based on the provided parameters. null 表示不修改现有值 */
    fun updateResetConfig(
        resetSignInfo: Boolean? = null,
        resetApkTools: Boolean? = null,
        resetSignTypes: Boolean? = null,
        resetSignedDirectory: Boolean? = null,
    ) {
        val oldResetInfo = uiStateFlow.value.resetInfo
        uiStateFlow.value =
            uiStateFlow.value.copy(
                resetInfo =
                    oldResetInfo.copy(
                        resetSignInfo = resetSignInfo ?: oldResetInfo.resetSignInfo,
                        resetApkTools = resetApkTools ?: oldResetInfo.resetApkTools,
                        resetSignTypes = resetSignTypes
                            ?: oldResetInfo.resetSignTypes,
                        resetSignedDirectory = resetSignedDirectory
                            ?: oldResetInfo.resetSignedDirectory
                    )
            )
    }

    fun runRestConfig() {
        viewModelScope.launch {
            val resetConfig = uiStateFlow.value.resetInfo

            if (resetConfig.resetSignInfo) {
                repository.saveSelectedSignInfo(null)
                repository.saveSignInfoList(null)
            }
            if (resetConfig.resetApkTools) {
                repository.saveApkSignPath(null)
                repository.saveZipAlignPath(null)
                repository.saveAaptPath(null)
            }
            if (resetConfig.resetSignTypes) {
                repository.setSignTypeList(emptySet())
            }
            if (resetConfig.resetSignedDirectory) {
                repository.saveSignedDirectory(null)
            }
        }
    }

    fun setupBuildToolsConfig(buildToolDir: String): String? {
        val result = ApkSigner.init(buildToolDir)
        saveApkSigner(ApkSigner.apkSignerPath)
        saveZipAlign(ApkSigner.zipAlignPath)
        saveAapt(ApkSigner.aaptPath)
        return result
    }

    fun saveApkSigner(apkSigner: String?) {
        viewModelScope.launch { repository.saveApkSignPath(apkSigner) }
    }

    fun saveZipAlign(zipAlign: String?) {
        viewModelScope.launch { repository.saveZipAlignPath(zipAlign) }
    }

    fun saveJavaHome(javaHomePath: String?) {
        if (javaHomePath.isNullOrBlank()) {
            viewModelScope.launch { repository.saveJavaHome(null) }
            return
        }

        val javaBin = when (hostOs) {
            MacOS, Linux -> "$javaHomePath/bin/java"
            Windows -> "$javaHomePath\\bin\\java.exe"
            else -> null
        }
        val javaFile = javaBin?.let { java.io.File(it) }
        val jdkVersion = getJdkVersion(javaHomePath)

        val jdkInfo = if (javaFile != null && javaFile.exists() && javaFile.canExecute()) {
            JdkInfo(javaHome = javaHomePath, jdkVersion = jdkVersion)
        } else {
            null
        }

        viewModelScope.launch { repository.saveJavaHome(jdkInfo) }
    }

    fun saveAapt(aapt: String?) {
        viewModelScope.launch { repository.saveAaptPath(aapt) }
    }

    fun saveAutoMatchSignature(autoMatch: Boolean) {
        viewModelScope.launch { repository.saveAutoMatchSignature(autoMatch) }
    }

    fun changeLanguage(currentLanguage: String) {
        viewModelScope.launch { repository.setLanguage(currentLanguage) }
    }

    /** @param isDarkMode null 表示跟随系统，true 表示深色模式，false 表示浅色模式 */
    fun changeThemeMode(isDarkMode: Boolean?) {
        viewModelScope.launch { repository.changeThemeMode(isDarkMode) }
    }

    fun openLogDirectory(): Boolean {
        val path = uiStateFlow.value.logFileDirectory
        val command =
            when (hostOs) {
                MacOS -> "/usr/bin/open"
                Windows -> "explorer"
                Linux -> "/usr/bin/xdg-open"
                else -> return false
            }
        val cmd = arrayOf(command, path)
        val result = Runtime.getRuntime().exec(cmd).waitFor()
        Logger.log("open log directory($hostOs: ${result}): ${cmd.joinToString(" ")}")
        // Explorer always return 1 whether succeed or failed
        // issue: https://github.com/microsoft/WSL/issues/6565
        return result == 0 || Windows == hostOs
    }

    private fun getJdkVersion(javaHomePath: String?): String? {
        if (javaHomePath.isNullOrBlank()) return null
        val javaBin = when (hostOs) {
            MacOS, Linux -> "$javaHomePath/bin/java"
            Windows -> "$javaHomePath\\bin\\java.exe"
            else -> null
        }
        val javaFile = javaBin?.let { java.io.File(it) }
        if (javaFile != null && javaFile.exists() && javaFile.canExecute()) {
            try {
                val process = ProcessBuilder(javaFile.absolutePath, "-version")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                val versionRegex = Regex("\"([^\"]+)\"")
                return versionRegex.find(output)?.groups?.get(1)?.value
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }
}

data class SettingInfoUiState(
    val apkSign: String? = null,
    val zipAlign: String? = null,
    val javaHome: JdkInfo? = null,
    val aapt: String? = null,
    val isAutoMatchSignature: Boolean = false,
    val isDarkMode: Boolean? = null,
    val resetInfo: SettingInfoResetState = SettingInfoResetState(),
    val logFileDirectory: String? = null,
)

data class SettingInfoResetState(
    val showResetDialog: Boolean = false,
    val resetSignInfo: Boolean = false,
    val resetApkTools: Boolean = false,
    val resetSignTypes: Boolean = false,
    val resetSignedDirectory: Boolean = false,
    val showChangeLanguageDialog: Boolean = false
)

data class JdkInfo(val javaHome: String, val jdkVersion: String? = null)
