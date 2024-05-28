package io.github.jixiaoyong.base

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
    ViewModel(viewModelScope)