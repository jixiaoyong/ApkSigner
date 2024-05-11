package io.github.jixiaoyong.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KClass


/**
 * @author : jixiaoyong
 * @description ：basic view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
abstract class BaseViewModel(protected val viewModelScope: CoroutineScope = CoroutineScope(Job())) :
    ViewModel(viewModelScope) {

    open fun onInit() {}
}

@Composable
inline fun <reified VM : BaseViewModel> viewModel(crossinline  factory: @DisallowComposableCalls () -> VM): VM {
    return androidx.lifecycle.viewmodel.compose.viewModel(VM::class, factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return factory() as T
        }
    }).apply { onInit() }
}