package io.github.jixiaoyong.pages.settings

import ApkSigner
import Logger
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.data.SettingPreferencesRepository
import io.github.jixiaoyong.utils.showToast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS.*
import org.jetbrains.skiko.hostOs
import org.koin.java.KoinJavaComponent.inject

/**
 * @author : jixiaoyong
 * @description ：setting info view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SettingInfoViewModel() : BaseViewModel() {
    private val repository: SettingPreferencesRepository by inject(SettingPreferencesRepository::class.java)

    private val uiStateFlow: MutableStateFlow<SettingInfoUiState> = MutableStateFlow(SettingInfoUiState())
    val uiState = uiStateFlow.asStateFlow()

    init{

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
        }.onEach {
            uiStateFlow.emit(it)
        }
            .launchIn(viewModelScope)

        uiStateFlow.update {
            val logFile = Logger.getLogFileDirectory().toString()
            it.copy(logFileDirectory = logFile)
        }
    }

    fun toggleResetDialog() {
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = uiStateFlow.value.resetInfo.copy(showResetDialog = !uiStateFlow.value.resetInfo.showResetDialog)
        )
    }

    fun toggleLanguageDialog() {
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = uiStateFlow.value.resetInfo.copy(showChangeLanguageDialog = !uiStateFlow.value.resetInfo.showChangeLanguageDialog)
        )
    }

    /**
     *  Updates the reset configuration values based on the provided parameters.
     *  null 表示不修改现有值
     */
    fun updateResetConfig(
        resetSignInfo: Boolean? = null,
        resetApkTools: Boolean? = null,
        resetSignTypes: Boolean? = null,
        resetSignedDirectory: Boolean? = null,
    ) {
        val oldResetInfo = uiStateFlow.value.resetInfo
        uiStateFlow.value = uiStateFlow.value.copy(
            resetInfo = oldResetInfo.copy(
                resetSignInfo = resetSignInfo ?: oldResetInfo.resetSignInfo,
                resetApkTools = resetApkTools ?: oldResetInfo.resetApkTools,
                resetSignTypes = resetSignTypes ?: oldResetInfo.resetSignTypes,
                resetSignedDirectory = resetSignedDirectory ?: oldResetInfo.resetSignedDirectory
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

    fun setupBuildToolsConfig(buildToolDir: String) {
        val result = ApkSigner.init(buildToolDir)
        saveApkSigner(ApkSigner.apkSignerPath)
        saveZipAlign(ApkSigner.zipAlignPath)
        saveAapt(ApkSigner.aaptPath)
        showToast(result ?: "修改成功")
    }

    fun saveApkSigner(apkSigner: String?) {
        viewModelScope.launch { repository.saveApkSignPath(apkSigner) }
    }

    fun saveZipAlign(zipAlign: String?) {
        viewModelScope.launch { repository.saveZipAlignPath(zipAlign) }
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

    /**
     * @param isDarkMode null 表示跟随系统，true 表示深色模式，false 表示浅色模式
     */
    fun changeThemeMode(isDarkMode: Boolean?) {
        viewModelScope.launch { repository.changeThemeMode(isDarkMode) }
    }

    fun openLogDirectory(): Boolean {
        val path = uiStateFlow.value.logFileDirectory
        val command = when (hostOs) {
            MacOS -> "/usr/bin/open"
            Windows -> "explorer"
            Linux -> "/usr/bin/xdg-open"
            else -> return false
        }
        val cmd = arrayOf(command, path)
        val result = Runtime.getRuntime().exec(cmd).waitFor()
        Logger.log("open log directory($hostOs: ${result}): ${cmd.joinToString(" ")}")
        return result == 0
    }
}

data class SettingInfoUiState(
    val apkSign: String? = null,
    val zipAlign: String? = null,
    val aapt: String? = null,
    val isAutoMatchSignature: Boolean = false,
    val isDarkMode: Boolean? = null,
    val resetInfo: SettingInfoResetState = SettingInfoResetState(),
    val logFileDirectory: String? = null
)

data class SettingInfoResetState(
    val showResetDialog: Boolean = false,
    val resetSignInfo: Boolean = false,
    val resetApkTools: Boolean = false,
    val resetSignTypes: Boolean = false,
    val resetSignedDirectory: Boolean = false,
    val showChangeLanguageDialog: Boolean = false
)