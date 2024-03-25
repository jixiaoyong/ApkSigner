package io.github.jixiaoyong.pages.signInfos

import Logger
import androidx.compose.runtime.mutableStateOf
import io.github.jixiaoyong.base.BaseViewModel

/**
 * @author : jixiaoyong
 * @description ：签名信息 view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SignInfoViewModel : BaseViewModel() {
    val newSignInfo = mutableStateOf(SignInfoBean())

    override fun onInit() {

    }

    override fun onCleared() {
        super.onCleared()
        Logger.log("$this  cleared")
    }

}