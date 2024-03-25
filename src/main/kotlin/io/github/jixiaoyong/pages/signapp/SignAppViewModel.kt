package io.github.jixiaoyong.pages.signapp

import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel

/**
 * @author : jixiaoyong
 * @description ：sign app view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SignAppViewModel : BaseViewModel() {
    val currentApkFilePath = mutableStateOf<List<String>>(emptyList())
    val currentSingleApkPackageName = mutableStateOf<String?>(null)


    override fun onInit() {

    }
}