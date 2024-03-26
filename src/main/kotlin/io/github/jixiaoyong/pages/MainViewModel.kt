package io.github.jixiaoyong.pages

import ApkSigner
import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.utils.SettingsTool
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
class MainViewModel(private val settings: SettingsTool) : BaseViewModel() {
    val routes =
        listOf(
            Pair("\uD83D\uDD10 签名信息", Routes.SignInfo),
            Pair("\uD83D\uDD12 签名APP", Routes.SignApp),
            Pair("\uD83D\uDEE0 设置信息", Routes.SettingInfo),
        )

    val currentIndex = MutableStateFlow(Routes.SignInfo)
    val isDarkTheme = mutableStateOf(false)

    override fun onInit() {
        viewModelScope.launch {
            val selectedSignInfo = settings.selectedSignInfoBean.first()
            if (selectedSignInfo != null) {
                changePage(Routes.SignApp)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settings.apkSigner.first()?.let { ApkSigner.setupApkSigner(it) }
            settings.zipAlign.first()?.let { ApkSigner.setupZipAlign(it) }
            settings.aapt.first()?.let { ApkSigner.setupAapt(it) }
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
