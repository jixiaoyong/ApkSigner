package io.github.jixiaoyong.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * @author : jixiaoyong
 * @description ：basic view model
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 25/3/2024
 */
abstract class BaseViewModel {
    private val job = Job()

    protected val viewModelScope = CoroutineScope(job)

    abstract fun onInit()

    open fun onCleared() {
        job.cancel()
    }
}

@Composable
inline fun <reified VM : BaseViewModel> viewModel(crossinline factory: @DisallowComposableCalls () -> VM): VM {
    return remember { factory() }.apply {
        onInit()
        DisposableEffect(Unit) {
            onDispose { onCleared() }
        }
    }
}
