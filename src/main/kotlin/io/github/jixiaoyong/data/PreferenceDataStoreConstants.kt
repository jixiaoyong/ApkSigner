package io.github.jd1378.otphelper.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferenceDataStoreConstants {
    val APK_SIGNER_PATH = stringPreferencesKey("APK_SIGNER_PATH") // 签名工具路径
    val ZIP_ALIGN_PATH = stringPreferencesKey("ZIP_ALIGN_PATH") // 压缩工具路径
    val AAPT_PATH = stringPreferencesKey("AAPT_PATH") // aapt工具路径
    val SIGN_INFO_SELECT = stringPreferencesKey("SIGN_INFO_SELECT") // 选中的签名信息
    val SIGN_INFO_LIST = stringPreferencesKey("SIGN_INFO_LIST") // 签名信息（密钥/密码等）列表
    val SIGNED_DIRECTORY = stringPreferencesKey("SIGNED_DIRECTORY") // 签名后文件保存路径
    val SIGN_TYPE_LIST = stringSetPreferencesKey("SIGN_TYPE_LIST") // 签名类型列表
    val ALIGN_ENABLE = booleanPreferencesKey("ALIGN_ENABLE") // 是否开启zipalign压缩
    val APK_SIGNATURE_MAP = stringPreferencesKey("APK_SIGNATURE_MAP") // apk包名和对应签名id的map
    val LANGUAGE = stringPreferencesKey("LANGUAGE") // 当前的语言
    val AUTO_MATCH_SIGNATURE = booleanPreferencesKey("AUTO_MATCH_SIGNATURE") // 是否自动匹配签名信息

    // 是否已经初始化过Preference（从老的SettingsTool读取配置）
    val IS_INITIALED_PREFERENCE = booleanPreferencesKey("IS_INITIALED_PREFERENCE")
}
