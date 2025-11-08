package io.github.jixiaoyong.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.jixiaoyong.BuildConfig
import me.sujanpoudel.utils.paths.appDataDirectory
import okio.Path.Companion.toPath

/**
 * @author : jixiaoyong
 * @description ：使用datastore存储数据
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2024/5/4
 */

internal const val SETTINGS_PREFERENCES = "settings_preferences.preferences_pb"

 val dataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(produceFile = {
        appDataDirectory(BuildConfig.PACKAGE_NAME).toString().toPath()
            .resolve("data_store")
            .resolve(SETTINGS_PREFERENCES)
    })
}

