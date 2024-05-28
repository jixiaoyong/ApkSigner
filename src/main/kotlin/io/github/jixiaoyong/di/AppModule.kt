package io.github.jixiaoyong.di

import io.github.jd1378.otphelper.data.local.PreferenceDataStoreHelper
import io.github.jixiaoyong.data.SettingPreferencesRepository
import io.github.jixiaoyong.data.getDataStore
import io.github.jixiaoyong.pages.MainViewModel
import io.github.jixiaoyong.pages.settings.SettingInfoViewModel
import io.github.jixiaoyong.pages.signInfos.SignInfoViewModel
import io.github.jixiaoyong.pages.signapp.SignAppViewModel
import io.github.jixiaoyong.utils.SettingsTool
import org.koin.compose.viewmodel.dsl.viewModelOf
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

    // viewmodel
    viewModelOf(::MainViewModel)
    viewModelOf(::SignAppViewModel)
    viewModelOf(::SignInfoViewModel)
    viewModelOf(::SettingInfoViewModel)
}