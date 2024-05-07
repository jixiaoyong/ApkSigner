package io.github.jixiaoyong.data

import com.google.gson.reflect.TypeToken
import io.github.jd1378.otphelper.data.local.PreferenceDataStoreConstants
import io.github.jd1378.otphelper.data.local.PreferenceDataStoreHelper
import io.github.jixiaoyong.beans.SignInfoBean
import io.github.jixiaoyong.beans.SignType
import io.github.jixiaoyong.utils.gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * @author : jixiaoyong
 * @description ：获取和更新页面配置信息
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2024/5/7
 */
class SettingPreferencesRepository(private val datastoreHelper: PreferenceDataStoreHelper) {

    suspend fun getIsInitialized(): Boolean =
        datastoreHelper.getFirstPreference(PreferenceDataStoreConstants.IS_INITIALED_PREFERENCE, false)

    suspend fun setIsInitialized(value: Boolean) =
        datastoreHelper.putPreference(PreferenceDataStoreConstants.IS_INITIALED_PREFERENCE, value)

    suspend fun getLanguage(systemLanguage: String): String =
        datastoreHelper.getFirstPreference(PreferenceDataStoreConstants.LANGUAGE, systemLanguage)

    suspend fun setLanguage(language: String) =
        datastoreHelper.putPreference(PreferenceDataStoreConstants.LANGUAGE, language)

    val apkSigner: Flow<String?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.APK_SIGNER_PATH)
    val aapt: Flow<String?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.AAPT_PATH)
    val language: Flow<String?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.LANGUAGE)
    val zipAlign: Flow<String?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.ZIP_ALIGN_PATH)
    val isZipAlign: Flow<Boolean>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.ALIGN_ENABLE, false)
    val isAutoMatchSignature: Flow<Boolean>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.AUTO_MATCH_SIGNATURE, false)
    val signedDirectory: Flow<String?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.SIGNED_DIRECTORY)

    val signTypeList: Flow<Set<Int>>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.SIGN_TYPE_LIST).map {
            return@map if (it.isNullOrEmpty()) SignType.DEF_SIGN_TYPES.map { it.type }.toSet()
            else it.map { it.toInt() }.toSet()
        }

    suspend fun setSignTypeList(signTypeList: Set<Int>) {
        datastoreHelper.putPreference(PreferenceDataStoreConstants.SIGN_TYPE_LIST, signTypeList.map { "$it" }.toSet())
    }


    val selectedSignInfoBean: Flow<SignInfoBean?>
        get() = datastoreHelper.getPreference(PreferenceDataStoreConstants.SIGN_INFO_SELECT).map {
            return@map if (it.isNullOrBlank()) null else gson.fromJson(it, SignInfoBean::class.java)
        }

    val signInfoBeans: Flow<List<SignInfoBean>>
        get() {
            val listType: TypeToken<List<SignInfoBean>> = object : TypeToken<List<SignInfoBean>>() {}
            return datastoreHelper.getPreference(PreferenceDataStoreConstants.SIGN_INFO_LIST)
                .filterNotNull().map {
                    gson.fromJson(it, listType)
                }
        }

    val apkSignatureMap: Flow<Map<String, Long>>
        get() {
            val listType: TypeToken<Map<String, Long>> = object : TypeToken<Map<String, Long>>() {}
            return datastoreHelper.getPreference(PreferenceDataStoreConstants.APK_SIGNATURE_MAP)
                .filterNotNull().map {
                    gson.fromJson(it, listType)
                }
        }
}