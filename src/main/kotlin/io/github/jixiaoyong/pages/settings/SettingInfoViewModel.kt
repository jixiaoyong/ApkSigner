package io.github.jixiaoyong.pages.settings

import ApkSigner
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.StorageKeys
import io.github.jixiaoyong.utils.showToast
import kotlinx.coroutines.flow.*

/**
 * @author : jixiaoyong
 * @description ：setting info view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SettingInfoViewModel(private val settings: SettingsTool) : BaseViewModel() {

    private val uiStateFlow: MutableStateFlow<SettingInfoUiState> = MutableStateFlow(SettingInfoUiState())
    val uiState = uiStateFlow.asStateFlow()

    override fun onInit() {
        // 用 gradle runDistributable 或者 packageReleaseDistributionForCurrentOS 等运行应用程序才会有值
        System.getProperty("jpackage.app-version")?.let {
            uiStateFlow.value = uiStateFlow.value.copy(version = it)
        }

        combine(
            settings.apkSigner,
            settings.zipAlign,
            settings.aapt,
            settings.isAutoMatchSignature
        ) { apkSigner, zipAlign, aapt, isAutoMatchSignature ->
            uiStateFlow.value.copy(
                apkSign = apkSigner,
                zipAlign = zipAlign,
                aapt = aapt,
                isAutoMatchSignature = isAutoMatchSignature
            )
        }.onEach {
            uiStateFlow.emit(it)
        }
            .launchIn(viewModelScope)

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
        toggleResetDialog()

        val resetConfig = uiStateFlow.value.resetInfo

        if (resetConfig.resetSignInfo) {
            settings.save(StorageKeys.SIGN_INFO_LIST, null)
            settings.save(StorageKeys.SIGN_INFO_SELECT, null)
        }
        if (resetConfig.resetApkTools) {
            settings.save(StorageKeys.APK_SIGNER_PATH, null)
            settings.save(StorageKeys.ZIP_ALIGN_PATH, null)
        }
        if (resetConfig.resetSignTypes) {
            settings.save(StorageKeys.SIGN_TYPE_LIST, null)
        }
        if (resetConfig.resetSignedDirectory) {
            settings.save(StorageKeys.SIGNED_DIRECTORY, null)
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
        settings.save(StorageKeys.APK_SIGNER_PATH, apkSigner)
    }

    fun saveZipAlign(zipAlign: String?) {
        settings.save(StorageKeys.ZIP_ALIGN_PATH, zipAlign)
    }

    fun saveAapt(aapt: String?) {
        settings.save(StorageKeys.AAPT_PATH, aapt)
    }

    fun saveAutoMatchSignature(autoMatch: Boolean) {
        settings.save(StorageKeys.AUTO_MATCH_SIGNATURE, autoMatch)
    }

    fun changeLanguage(currentLanguage: String) {
        settings.save(StorageKeys.LANGUAGE, currentLanguage)
    }
}

data class SettingInfoUiState(
    val apkSign: String? = null,
    val zipAlign: String? = null,
    val aapt: String? = null,
    val isAutoMatchSignature: Boolean = false,
    val version: String = "未知",
    val resetInfo: SettingInfoResetState = SettingInfoResetState()
)

data class SettingInfoResetState(
    val showResetDialog: Boolean = false,
    val resetSignInfo: Boolean = false,
    val resetApkTools: Boolean = false,
    val resetSignTypes: Boolean = false,
    val resetSignedDirectory: Boolean = false,
    val showChangeLanguageDialog: Boolean = false
)