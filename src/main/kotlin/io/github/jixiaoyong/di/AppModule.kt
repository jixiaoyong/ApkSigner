package io.github.jixiaoyong.di

import io.github.jd1378.otphelper.data.local.PreferenceDataStoreHelper
import io.github.jixiaoyong.data.SettingPreferencesRepository
import io.github.jixiaoyong.data.getDataStore
import io.github.jixiaoyong.pages.MainViewModel
import io.github.jixiaoyong.utils.SettingsTool
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * @author : jixiaoyong
 * @description ：koin di module
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/4/2024
 */

val appModule = module {
    single {
        SettingsTool()
    }
    single {
        PreferenceDataStoreHelper(getDataStore())
    }
    single { SettingPreferencesRepository() }
    viewModel { MainViewModel()}
}