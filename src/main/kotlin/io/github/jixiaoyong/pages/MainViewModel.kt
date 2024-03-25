package io.github.jixiaoyong.pages

import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel

/**
 * @author : jixiaoyong
 * @description ：main view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class MainViewModel : BaseViewModel() {
    val routes = listOf(
        Pair("\uD83D\uDD10 签名信息", Routes.SignInfo),
        Pair("\uD83D\uDD12 签名APP", Routes.SignApp),
        Pair("\uD83D\uDEE0 设置信息", Routes.SettingInfo)
    )

    val currentIndex = mutableStateOf(Routes.SignInfo)
    val isDarkTheme = mutableStateOf(false)

    override fun onInit() {

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