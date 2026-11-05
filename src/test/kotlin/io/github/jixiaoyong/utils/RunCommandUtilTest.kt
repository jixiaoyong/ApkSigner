package io.github.jixiaoyong.utils

import RunCommandUtil
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RunCommandUtilTest {

    @Test
    fun testErrorOutputCapturedOnFailure() {
        // 创建一个会向 stderr 打印特定错误并以 1 退出的命令
        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val tempDir = File(System.getProperty("java.io.tmpdir"), "test_err_dir")
        tempDir.mkdirs()
        val script = if (isWindows) {
            File(tempDir, "fail_test.bat").apply {
                writeText("@echo off\r\necho CUSTOM_ERROR_DETAILS 1>&2\r\nexit /b 1")
            }
        } else {
            File(tempDir, "fail_test.sh").apply {
                writeText("#!/bin/sh\necho 'CUSTOM_ERROR_DETAILS' >&2\nexit 1")
                setExecutable(true)
            }
        }

        val err = RunCommandUtil.runCommand(script.absolutePath)
        assertNotNull(err, "执行失败脚本应该返回非空的 Exception")
        println("捕获到的错误信息: ${err.message}")

        // 核心验证：错误信息中必须包含子进程真实的错误输出 "CUSTOM_ERROR_DETAILS"
        assertTrue(
            err.message?.contains("CUSTOM_ERROR_DETAILS") == true,
            "错误信息中必须包含子进程真实输出，当前输出为: ${err.message}"
        )
    }

    @Test
    fun testJavaHomeInjectedIntoPath() {
        val customJavaHome = "/dummy/custom/jdk"
        val pb = RunCommandUtil.createProcessBuilder(listOf("echo", "test"), javaHome = customJavaHome)

        // 核心验证 1: JAVA_HOME 必须被正确设置
        assertEquals(customJavaHome, pb.environment()["JAVA_HOME"])

        // 核心验证 2: JAVA_HOME/bin 必须被前置拼接入 PATH 环境变量
        val path = pb.environment()["PATH"] ?: pb.environment()["Path"]
        assertNotNull(path, "环境变量 PATH 或 Path 不能为空")
        val expectedBin = File(customJavaHome, "bin").absolutePath
        assertTrue(
            path.startsWith(expectedBin) || path.contains(expectedBin),
            "PATH 环境变量必须前置或包含 Java bin 路径，当前为: $path"
        )
    }

    @Test
    fun testSpacePathCommandExecution() {
        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val spaceDir = File(System.getProperty("java.io.tmpdir"), "dir with spaces")
        spaceDir.mkdirs()
        val script = if (isWindows) {
            File(spaceDir, "space test.bat").apply {
                writeText("@echo off\r\necho OK")
            }
        } else {
            File(spaceDir, "space_test.sh").apply {
                writeText("#!/bin/sh\necho OK")
                setExecutable(true)
            }
        }

        // 当使用 List 形式传入安全参数时，包含空格的路径必须能正常执行成功
        val err = RunCommandUtil.runCommand(script.absolutePath)
        println("空格路径执行结果错误: $err")
        // 如果旧代码用 command.split("\\s+".toRegex())，空格路径会被直接切裂导致失败
        // 期望无异常成功返回 null
        assertEquals(null, err, "带空格路径的脚本应该能顺利执行")
    }
}
