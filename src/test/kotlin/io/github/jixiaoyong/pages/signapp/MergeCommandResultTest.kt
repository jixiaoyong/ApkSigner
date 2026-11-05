package io.github.jixiaoyong.pages.signapp

import io.github.jixiaoyong.beans.CommandResult
import kotlin.test.Test
import kotlin.test.assertTrue

class MergeCommandResultTest {

    @Test
    fun testMergeWithMixedResults() {
        val results = listOf(
            CommandResult.Success("/path/to/app1_signed.apk"),
            CommandResult.Error("签名失败: 密码错误")
        )
        val paths = listOf("/path/to/app1.apk", "/path/to/app2.apk")

        val merged = SignAppViewModel.mergeCommandResult(results, paths)
        assertTrue(merged is CommandResult.Success<*>, "含有 Success 时应合并为 Success")
        val content = merged.result.toString()
        assertTrue(content.contains("app1_signed.apk"))
        assertTrue(content.contains("签名失败: 密码错误"))
    }

    @Test
    fun testMergeWithAllErrors() {
        val results = listOf(
            CommandResult.Error("错误 1"),
            CommandResult.Error("错误 2")
        )
        val paths = listOf("/path/to/app1.apk", "/path/to/app2.apk")

        val merged = SignAppViewModel.mergeCommandResult(results, paths)
        assertTrue(merged is CommandResult.Error<*>, "全为 Error 时应返回 Error")
        val content = merged.message.toString()
        assertTrue(content.contains("错误 1"))
        assertTrue(content.contains("错误 2"))
    }

    @Test
    fun testMergeWithNotExecutedOrNullStatesDoesNotCrash() {
        // 彻底复现并验证 Issue #8：列表中混杂 NOT_EXECUT 或非 Error/Success 对象，绝不抛出 ClassCastException
        val results = listOf(
            CommandResult.NOT_EXECUT,
            CommandResult.Error("文件不存在")
        )
        val paths = listOf("/path/to/app1.apk", "/path/to/app2.apk")

        val merged = SignAppViewModel.mergeCommandResult(results, paths)
        assertTrue(merged is CommandResult.Error<*>, "无 Success 时应返回 Error 且安全无崩溃")
        val content = merged.message.toString()
        assertTrue(content.contains("未执行"))
        assertTrue(content.contains("文件不存在"))
    }
}
