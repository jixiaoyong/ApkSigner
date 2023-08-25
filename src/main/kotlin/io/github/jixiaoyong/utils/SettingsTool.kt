package io.github.jixiaoyong.utils

import com.google.gson.reflect.TypeToken
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import io.github.jixiaoyong.pages.signInfos.SignInfoBean
import io.github.jixiaoyong.pages.signapp.SignType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


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
    val signTypeList: Flow<Set<Int>>
    val selectedSignInfoBean: Flow<SignInfoBean?>
    val signInfoBeans: Flow<List<SignInfoBean>>
    fun cleanStorage()
    fun save(key: StorageKeys, value: String?)
}


enum class StorageKeys {
    APK_SIGNER_PATH,
    ZIP_ALIGN_PATH,
    SIGN_INFO_SELECT,
    SIGN_INFO_LIST,
    SIGN_TYPE_LIST;

    val key get() = this.name
}

class SettingsTool(private val scope: CoroutineScope) : KeyValueStorage {
    private val settings: Settings by lazy { Settings() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    override val apkSigner: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.APK_SIGNER_PATH.key)
    override val zipAlign: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.ZIP_ALIGN_PATH.key)
    override var signTypeList: Flow<Set<Int>>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGN_TYPE_LIST.key).map {
            return@map if (it.isNullOrBlank()) SignType.DEF_SIGN_TYPES.map { it.type }.toSet()
            else it.split(",").map { it.toInt() }.toSet()
        }
        set(value) {
            scope.launch {
                settings[StorageKeys.SIGN_TYPE_LIST.key] = value.lastOrNull()?.joinToString(",")
            }
        }

    override val selectedSignInfoBean: Flow<SignInfoBean?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGN_INFO_SELECT.key).map {
            return@map if (it.isNullOrBlank()) null else gson.fromJson(it, SignInfoBean::class.java)
        }

    override val signInfoBeans: Flow<List<SignInfoBean>>
        get() {
            val listType: TypeToken<List<SignInfoBean>> =
                object : TypeToken<List<SignInfoBean>>() {}
            return observableSettings.getStringOrNullFlow(StorageKeys.SIGN_INFO_LIST.key)
                .filterNotNull().map {
                    gson.fromJson(it, listType)
                }
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