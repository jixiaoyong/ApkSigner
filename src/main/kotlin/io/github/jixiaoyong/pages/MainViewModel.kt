package io.github.jixiaoyong.pages

import ApkSigner
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.data.SettingPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author : jixiaoyong
 * @description ：main view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class MainViewModel(private val settingsRepository: SettingPreferencesRepository) : BaseViewModel() {

    val isDarkTheme = settingsRepository.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val selectedSignatureInfo = settingsRepository.selectedSignInfoBean.take(1).map { null != it }.stateIn(
        viewModelScope,
        SharingStarted.Lazily, false
    )

    override fun onInit() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.apkSigner.first()?.let { ApkSigner.setupApkSigner(it) }
            settingsRepository.zipAlign.first()?.let { ApkSigner.setupZipAlign(it) }
            settingsRepository.aapt.first()?.let { ApkSigner.setupAapt(it) }
            settingsRepository.javaHome.first()?.let { ApkSigner.setupJavaHome(it.javaHome) }
        }
    }
}

object Routes {
    const val SignInfo = "signInfo"
    const val SignApp = "signApp"
    const val SettingInfo = "settingInfo"
}
