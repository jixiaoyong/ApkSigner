package io.github.jixiaoyong.pages.settings

import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel

/**
 * @author : jixiaoyong
 * @description ：setting info view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SettingInfoViewModel : BaseViewModel() {

    val version = mutableStateOf("未知")

    override fun onInit() {

    }
}