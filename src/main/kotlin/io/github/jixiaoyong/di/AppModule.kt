package io.github.jixiaoyong.di

import io.github.jixiaoyong.utils.SettingsTool
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

}