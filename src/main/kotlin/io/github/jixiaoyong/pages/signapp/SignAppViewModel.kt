package io.github.jixiaoyong.pages.signapp

import ApkSigner
import io.github.jixiaoyong.base.BaseViewModel
import io.github.jixiaoyong.beans.CommandResult
import io.github.jixiaoyong.beans.SignInfoBean
import io.github.jixiaoyong.utils.SettingsTool
import io.github.jixiaoyong.utils.StorageKeys
import io.github.jixiaoyong.utils.gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author : jixiaoyong
 * @description ：sign app view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
class SignAppViewModel(private val settings: SettingsTool) : BaseViewModel() {

    private val uiStateFlow = MutableStateFlow(SignAppState())
    val uiState = uiStateFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SignAppState())

    init {
        combine(
            settings.signTypeList,
            settings.selectedSignInfoBean,
            settings.signInfoBeans,
            settings.apkSignatureMap,
            settings.signedDirectory,
        ) { signTypeList, selectedSignInfoBean, signInfoBeans, apkSignatureMap, signedDirectory ->
            uiStateFlow.value.copy(
                apkSignType = signTypeList,
                globalSelectedSignInfo = selectedSignInfoBean,
                signInfoBeans = signInfoBeans,
                apkSignatureMap = apkSignatureMap,
                signedDirectory = signedDirectory
            )
        }
            .combine(settings.isZipAlign) { state, isZipAlign -> state.copy(isZipAlign = isZipAlign) }
            .combine(settings.isAutoMatchSignature) { state, isAutoMatchSignature -> state.copy(isAutoMatchSignature = isAutoMatchSignature) }
            .onEach {
                uiStateFlow.value = it
            }.launchIn(viewModelScope)

    }

    suspend fun changeApkFilePath(apkFilePath: List<String>) {
        // 每次重新选择apk的时候重置此标志
        val apkPackageName = if (1 != apkFilePath.size) {
            null
        } else {
            ApkSigner.getApkPackageName(apkFilePath.firstOrNull())
        }
        uiStateFlow.emit(
            uiStateFlow.value.copy(
                currentApkFilePath = apkFilePath,
                currentSingleApkPackageName = apkPackageName
            )
        )
    }

    fun removeApkSignature(packageName: String?) {
        if (null == packageName) {
            return
        }
        val newMap = uiState.value.apkSignatureMap.toMutableMap()
        newMap.remove(packageName)
        settings.save(StorageKeys.APK_SIGNATURE_MAP, gson.toJson(newMap))
    }

    fun updateApkSignatureMap(packageName: String?, localSelectedSignInfo: SignInfoBean) {
        if (null == packageName || !localSelectedSignInfo.isValid()) {
            return
        }

        val newMap = uiState.value.apkSignatureMap.toMutableMap()
        newMap[packageName] = localSelectedSignInfo.id
        settings.save(StorageKeys.APK_SIGNATURE_MAP, gson.toJson(newMap))
    }


    fun changeSignInfo(signInfoResult: CommandResult) {
        viewModelScope.launch { uiStateFlow.emit(uiStateFlow.value.copy(signInfoResult = signInfoResult)) }
    }

    fun saveSignedOutputDirectory(signedDirectory: String) {
        viewModelScope.launch { settings.save(StorageKeys.SIGNED_DIRECTORY, signedDirectory) }
    }

    fun changeZipAlign(isZipAlign: Boolean) {
        viewModelScope.launch { settings.save(StorageKeys.ALIGN_ENABLE, isZipAlign) }
    }

    fun changeApkSignType(newTypes: MutableSet<Int>) {
        settings.signTypeList = flowOf(newTypes)
    }

    private val TITLE_CONTENT_DIVIDER = "-------------------------------------------------------"

    /**
     * 根据给定的 [pathList] 将给定的 [CommandResult] 列表合并为单个 [CommandResult]。
     *
     * 如果给定的[resultList]至少包含一个[CommandResult.Success]，则返回一个[CommandResult.Success]，
     * 其中结果是所有成功结果的串联字符串，以换行符分隔。
     *
     * 如果给定的[resultList]不包含[CommandResult.Success]，则返回[CommandResult.Error]，
     * 其中结果是所有错误消息的串联字符串，以换行符分隔。
     *
     * @param resultList 要合并的 [CommandResult] 列表
     * @param pathList [resultList]中每个[CommandResult]对应的路径列表
     * @return 单个 [CommandResult]，它是合并给定 [resultList] 的结果
     */
    fun mergeCommandResult(resultList: List<CommandResult>, pathList: List<String>) =

        if (resultList.filter { it is CommandResult.Success<*> }.isNotEmpty()) {
            val message = List(resultList.size) { index ->
                val path = pathList.getOrNull(index)
                val result = resultList.getOrNull(index)
                "$TITLE_CONTENT_DIVIDER\n[${index + 1}] $path\n$TITLE_CONTENT_DIVIDER\n" +
                        (if (result is CommandResult.Error<*>) result.message.toString()
                        else (result as CommandResult.Success<*>).result.toString())
            }.joinToString("\n\n")

            CommandResult.Success(resultList.joinToString { message })
        } else {
            val message = List(resultList.size) { index ->
                val path = pathList.getOrNull(index)
                val result = resultList.getOrNull(index)
                "$TITLE_CONTENT_DIVIDER\n[${index + 1}] $path\n$TITLE_CONTENT_DIVIDER\n" +
                        ((result as CommandResult.Error<*>).message.toString())
            }.joinToString("\n\n")

            CommandResult.Error(message)
        }
}

data class SignAppState(
    val currentApkFilePath: List<String> = emptyList(),
    val currentSingleApkPackageName: String? = null,
    val signLogs: List<String> = emptyList(),
    val apkSignType: Set<Int> = emptySet(),
    val globalSelectedSignInfo: SignInfoBean? = null,
    val signInfoBeans: List<SignInfoBean>? = null,
    val apkSignatureMap: Map<String, Long> = emptyMap(),
    val signedDirectory: String? = null,
    val signApkResult: CommandResult = CommandResult.NOT_EXECUT,
    val isZipAlign: Boolean = false,
    val isAutoMatchSignature: Boolean = false,
    val currentSignInfo: SignInfoBean? = null,
    val signInfoResult: CommandResult = CommandResult.NOT_EXECUT
)