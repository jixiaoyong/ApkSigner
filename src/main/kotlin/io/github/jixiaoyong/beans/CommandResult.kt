package io.github.jixiaoyong.beans

/**
 * 执行terminal指令的结果
 */
sealed class CommandResult {

    class Success<T>(val result: T) : CommandResult()

    class Error<T>(val message: T, val error: Exception? = null) : CommandResult()

    object NOT_EXECUT : CommandResult()

    object EXECUTING : CommandResult()
}