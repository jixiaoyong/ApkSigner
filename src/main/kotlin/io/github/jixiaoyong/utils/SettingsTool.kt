package io.github.jixiaoyong.utils

import com.google.gson.reflect.TypeToken
import com.russhwolf.settings.*
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import io.github.jd1378.otphelper.data.local.PreferenceDataStoreConstants
import io.github.jd1378.otphelper.data.local.PreferenceDataStoreHelper
import io.github.jixiaoyong.beans.SignInfoBean
import io.github.jixiaoyong.beans.SignType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map


/**
 * @author : jixiaoyong
 * @description ：读写配置信息
 * todo 计划在下个大版本中移除此库，使用[io.github.jixiaoyong.data.SettingPreferencesRepository]替代
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/19
 */

interface KeyValueStorage {

    val apkSigner: Flow<String?>
    val zipAlign: Flow<String?>
    val aapt: Flow<String?>
    val language: Flow<String?>
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
    LANGUAGE, // 当前的语言
    AUTO_MATCH_SIGNATURE; // 是否自动匹配签名信息

    val key get() = this.name
}

@Deprecated("Please use the {@link io.github.jixiaoyong.data.SettingPreferencesRepository} instead")
@OptIn(ExperimentalSettingsApi::class)
class SettingsTool() : KeyValueStorage {
    private val settings: Settings by lazy { Settings() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    override val apkSigner: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.APK_SIGNER_PATH.key)
    override val aapt: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.AAPT_PATH.key)
    override val language: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.LANGUAGE.key)
    override val zipAlign: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.ZIP_ALIGN_PATH.key)
    override val isZipAlign: Flow<Boolean>
        get() = observableSettings.getBooleanFlow(StorageKeys.ALIGN_ENABLE.key, false)
    override val isAutoMatchSignature: Flow<Boolean>
        get() = observableSettings.getBooleanFlow(StorageKeys.AUTO_MATCH_SIGNATURE.key, false)
    override val signedDirectory: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGNED_DIRECTORY.key)
    override val signTypeList: Flow<Set<Int>>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.SIGN_TYPE_LIST.key).map {
            return@map if (it.isNullOrBlank()) SignType.DEF_SIGN_TYPES.map { it.type }.toSet()
            else it.split(",").map { it.toInt() }.toSet()
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


    suspend fun saveToDatastore(help: PreferenceDataStoreHelper) {
        settings.get<String>(StorageKeys.APK_SIGNER_PATH.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.APK_SIGNER_PATH, it) }
        settings.get<String>(StorageKeys.ZIP_ALIGN_PATH.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.ZIP_ALIGN_PATH, it) }
        settings.get<String>(StorageKeys.AAPT_PATH.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.AAPT_PATH, it) }
        settings.get<String>(StorageKeys.SIGN_INFO_SELECT.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.SIGN_INFO_SELECT, it) }
        settings.get<String>(StorageKeys.SIGN_INFO_LIST.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.SIGN_INFO_LIST, it) }
        settings.get<String>(StorageKeys.SIGNED_DIRECTORY.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.SIGNED_DIRECTORY, it) }
        settings.get<String>(StorageKeys.SIGN_TYPE_LIST.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.SIGN_TYPE_LIST, it.split(",").toSet()) }
        settings.get<Boolean>(StorageKeys.ALIGN_ENABLE.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.ALIGN_ENABLE, it) }
        settings.get<String>(StorageKeys.APK_SIGNATURE_MAP.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.APK_SIGNATURE_MAP, it) }
        settings.get<String>(StorageKeys.LANGUAGE.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.LANGUAGE, it) }
        settings.get<Boolean>(StorageKeys.AUTO_MATCH_SIGNATURE.key)
            ?.let { help.putPreference(PreferenceDataStoreConstants.AUTO_MATCH_SIGNATURE, it) }
    }
}