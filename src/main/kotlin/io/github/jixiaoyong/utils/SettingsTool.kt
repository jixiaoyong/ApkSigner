package io.github.jixiaoyong.utils

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.github.jixiaoyong.pages.signInfos.SignInfoBean

/**
 * @author : jixiaoyong
 * @description ：读写配置信息
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */

interface KeyValueStorage {

    val apkSigner: Flow<String?>
    val zipAlign: Flow<String?>
    val selectedSignInfoBean: Flow<SignInfoBean?>
    val signInfoBeans: Flow<List<SignInfoBean>>
    fun cleanStorage()
    fun save(key: StorageKeys, value: String?)
}


enum class StorageKeys {
    APK_SIGNER_PATH,
    ZIP_ALIGN_PATH,
    SIGN_INFO_SELECT,
    SIGN_INFO_LIST;

    val key get() = this.name
}

class SettingsTool : KeyValueStorage {
    private val settings: Settings by lazy { Settings() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    override val apkSigner: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.APK_SIGNER_PATH.key)
    override val zipAlign: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.ZIP_ALIGN_PATH.key)

    override val selectedSignInfoBean: Flow<SignInfoBean?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGN_INFO_SELECT.key).map {
            return@map if (it.isNullOrBlank()) null else Json.decodeFromString(it)
        }

    override val signInfoBeans: Flow<List<SignInfoBean>>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGN_INFO_LIST.key).filterNotNull().map {
            Json.decodeFromString(it)
        }

    override fun save(key: StorageKeys, value: String?) {
        if (value != null) {
            settings[key.key] = value
        } else {
            settings.remove(key.key)
        }
    }

    // clean all the stored values
    override fun cleanStorage() {
        settings.clear()
    }
}