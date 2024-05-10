package io.github.jixiaoyong.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


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
inline fun <reified VM : BaseViewModel> viewModel(crossinline factory: @DisallowComposableCalls () -> VM): VM {
    return androidx.lifecycle.viewmodel.compose.viewModel { factory() }.apply { onInit() }
}