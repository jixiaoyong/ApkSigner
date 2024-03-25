package io.github.jixiaoyong.utils

import com.google.gson.reflect.TypeToken
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import io.github.jixiaoyong.beans.SignInfoBean
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
    val aapt: Flow<String?>
    val isZipAlign: Flow<Boolean>
    val isAutoMatchSignature: Flow<Boolean>
    val signedDirectory: Flow<String?>
    val signTypeList: Flow<Set<Int>>
    val selectedSignInfoBean: Flow<SignInfoBean?>
    val signInfoBeans: Flow<List<SignInfoBean>>
    val apkSignatureMap: Flow<Map<String, Long>> // apk和对应签名信息的map，key为apk包名，value为SignInfoBean.id
    fun cleanStorage()
    fun save(key: StorageKeys, value: Any?)
}


enum class StorageKeys {
    APK_SIGNER_PATH, // 签名工具路径
    ZIP_ALIGN_PATH, // 压缩工具路径
    AAPT_PATH, // aapt工具路径
    SIGN_INFO_SELECT, // 选中的签名信息
    SIGN_INFO_LIST, // 签名信息（密钥/密码等）列表
    SIGNED_DIRECTORY, // 签名后文件保存路径
    SIGN_TYPE_LIST, // 签名类型列表
    ALIGN_ENABLE, // 是否开启zipalign压缩
    APK_SIGNATURE_MAP, // apk包名和对应签名id的map
    AUTO_MATCH_SIGNATURE; // 是否自动匹配签名信息

    val key get() = this.name
}

@OptIn(ExperimentalSettingsApi::class)
class SettingsTool(private val scope: CoroutineScope) : KeyValueStorage {
    private val settings: Settings by lazy { Settings() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    override val apkSigner: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.APK_SIGNER_PATH.key)
    override val aapt: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.AAPT_PATH.key)
    override val zipAlign: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.ZIP_ALIGN_PATH.key)
    override val isZipAlign: Flow<Boolean>
        get() = observableSettings.getBooleanFlow(StorageKeys.ALIGN_ENABLE.key, false)
    override val isAutoMatchSignature: Flow<Boolean>
        get() = observableSettings.getBooleanFlow(StorageKeys.AUTO_MATCH_SIGNATURE.key, false)
    override val signedDirectory: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGNED_DIRECTORY.key)
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
            val listType: TypeToken<List<SignInfoBean>> = object : TypeToken<List<SignInfoBean>>() {}
            return observableSettings.getStringOrNullFlow(StorageKeys.SIGN_INFO_LIST.key)
                .filterNotNull().map {
                    gson.fromJson(it, listType)
                }
        }

    override val apkSignatureMap: Flow<Map<String, Long>>
        get() {
            val listType: TypeToken<Map<String, Long>> = object : TypeToken<Map<String, Long>>() {}
            return observableSettings.getStringOrNullFlow(StorageKeys.APK_SIGNATURE_MAP.key)
                .filterNotNull().map {
                    gson.fromJson(it, listType)
                }
        }

    override fun save(key: StorageKeys, value: Any?) {
        if (value != null) {
            settings[key.key] = value.toString()
        } else {
            settings.remove(key.key)
        }
    }

    // clean all the stored values
    override fun cleanStorage() {
        settings.clear()
    }
}