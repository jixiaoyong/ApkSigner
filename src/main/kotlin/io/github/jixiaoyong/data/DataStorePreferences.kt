package io.github.jixiaoyong.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.jixiaoyong.BuildConfig
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import me.sujanpoudel.utils.paths.appDataDirectory
import okio.Path.Companion.toPath
import java.io.File

/**
 * @author : jixiaoyong
 * @description ：使用datastore存储数据
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2024/5/4
 */

internal const val SETTINGS_PREFERENCES = "settings_preferences.preferences_pb"

private lateinit var dataStore: DataStore<Preferences>

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

/**
 * Gets the singleton DataStore instance, creating it if necessary.
 */
fun getDataStore(): DataStore<Preferences> =
    synchronized(lock) {
        if (::dataStore.isInitialized) {
            dataStore
        } else {
            PreferenceDataStoreFactory.createWithPath(produceFile = {
                val dataDirectory = appDataDirectory(BuildConfig.PACKAGE_NAME)
                dataDirectory.toString().toPath().resolve("data_store" + File.separator + SETTINGS_PREFERENCES)
            })
                .also { dataStore = it }
        }
    }

