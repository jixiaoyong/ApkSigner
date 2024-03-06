package io.github.jixiaoyong.beans

/**
 * @author : jixiaoyong
 * @description ：APP的几种状态
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 17/1/2024
 */
sealed class AppState {

    /**
     * 初始状态
     */
    object Idle : AppState()

    /**
     * 加载中，监测必要配置
     */
    object Loading : AppState()

    /**
     * 正常状态，可以启动
     */
    object Success : AppState()

    /**
     * 已经存在，是否还要启动
     */
    object AlreadyExists : AppState()
}