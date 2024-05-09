package io.github.jixiaoyong.pages

import ApkSigner
import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.data.SettingPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @author : jixiaoyong
 * @description ：main view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class MainViewModel(private val settingsRepository: SettingPreferencesRepository) : BaseViewModel() {

    val currentIndex = MutableStateFlow(Routes.SignInfo)
    val isDarkTheme = mutableStateOf(false)

    override fun onInit() {
        viewModelScope.launch {
            val selectedSignInfo = settingsRepository.selectedSignInfoBean.first()
            if (selectedSignInfo != null) {
                changePage(Routes.SignApp)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.apkSigner.first()?.let { ApkSigner.setupApkSigner(it) }
            settingsRepository.zipAlign.first()?.let { ApkSigner.setupZipAlign(it) }
            settingsRepository.aapt.first()?.let { ApkSigner.setupAapt(it) }
        }
    }

    fun changePage(route: String) {
        currentIndex.value = route
    }
}

object Routes {
    const val SignInfo = "signInfo"
    const val SignApp = "signApp"
    const val SettingInfo = "settingInfo"
}
