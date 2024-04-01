package io.github.jixiaoyong.pages.signInfos

import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.beans.SignInfoBean
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.StorageKeys
import io.github.jixiaoyong.utils.gson
import kotlinx.coroutines.flow.*

/**
 * @author : jixiaoyong
 * @description ：签名信息 view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SignInfoViewModel(private val settings: SettingsTool) : BaseViewModel() {

    private val uiStateFlow: MutableStateFlow<SignInfoUiState> = MutableStateFlow(SignInfoUiState())
    val uiState = uiStateFlow.asStateFlow()

    override fun onInit() {

        combine(settings.signInfoBeans, settings.selectedSignInfoBean) { signInfoBeans, selectedSignInfoBean ->
            uiStateFlow.value.copy(
                signInfoList = signInfoBeans,
                selectedSignInfo = selectedSignInfoBean
            )
        }.onEach { uiStateFlow.emit(it) }.launchIn(viewModelScope)

    }


    fun updateNewSignInfo(
        keyNickName: String? = null,
        keyStorePath: String? = null,
        keyStorePassword: String? = null,
        keyAlias: String? = null,
        keyPassword: String? = null,
    ) {
        val oldSignInfo = uiStateFlow.value.newSignInfo
        updateNewSignInfo(
            oldSignInfo.copy(
                keyNickName = keyNickName ?: oldSignInfo.keyNickName,
                keyStorePath = keyStorePath ?: oldSignInfo.keyStorePath,
                keyStorePassword = keyStorePassword ?: oldSignInfo.keyStorePassword,
                keyAlias = keyAlias ?: oldSignInfo.keyAlias,
                keyPassword = keyPassword ?: oldSignInfo.keyPassword
            )
        )
    }

    fun updateNewSignInfo(signInfoBean: SignInfoBean) {
        uiStateFlow.value = uiStateFlow.value.copy(newSignInfo = signInfoBean)
    }

    fun saveSelectedSignInfo(signInfoBean: SignInfoBean?) {
        val json = if (null == signInfoBean) null else gson.toJson(signInfoBean)
        settings.save(StorageKeys.SIGN_INFO_SELECT, json)
    }

    fun removeSignInfo(signInfo: SignInfoBean) {
        val tempList = uiStateFlow.value.signInfoList.toMutableList()
        tempList.remove(signInfo)
        settings.save(StorageKeys.SIGN_INFO_LIST, gson.toJson(tempList))
        if (signInfo == uiStateFlow.value.selectedSignInfo) {
            saveSelectedSignInfo(null)
        }
    }

    // save sign info to local storage
    fun saveNewSignInfo() {
        val newSignInfo = uiStateFlow.value.newSignInfo
        val newSignInfos = mutableListOf<SignInfoBean>()
        newSignInfos.addAll(uiStateFlow.value.signInfoList)
        val indexOfSignInfo = newSignInfos.indexOfFirst { it.isSameOne(newSignInfo) }
        if (-1 != indexOfSignInfo) {
            newSignInfos[indexOfSignInfo] = newSignInfo
            saveSelectedSignInfo(newSignInfo)
        } else {
            newSignInfos.add(newSignInfo)
        }
        settings.save(StorageKeys.SIGN_INFO_LIST, gson.toJson(newSignInfos))

        // 每次保存新签名之后，重置签名的id，避免在此点保存的时候还是覆盖已经保存的签名
        // 对于需要编辑已有签名的，需要从列表中选择并编辑
        updateNewSignInfo(newSignInfo.copy())
    }
}

data class SignInfoUiState(
    val newSignInfo: SignInfoBean = SignInfoBean(),
    val signInfoList: List<SignInfoBean> = listOf(),
    val selectedSignInfo: SignInfoBean? = null,
)