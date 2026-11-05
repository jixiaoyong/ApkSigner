package io.github.jixiaoyong.utils

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdkDetectorTest {

    @Test
    fun testDetectCurrentRuntimeJava() {
        // 当前运行测试的 JVM 自身必定有一个有效的 java.home
        val detected = JdkDetector.detectJdk()
        assertNotNull(detected, "JdkDetector 应该能够成功探测到当前机器上的有效 JDK")
        assertTrue(File(detected.javaHome).exists(), "探测出的 javaHome 目录必须存在")
        assertNotNull(detected.jdkVersion, "探测出的 jdkVersion 必须不为空")
        println("自动探测到的 JDK: ${detected.javaHome}, 版本: ${detected.jdkVersion}")
    }

    @Test
    fun testValidateCustomJavaHome() {
        val currentJavaHome = System.getProperty("java.home")
        val info = JdkDetector.validateAndGetInfo(currentJavaHome)
        assertNotNull(info, "当前 JVM 的 java.home 必须能通过校验")
        assertEquals(currentJavaHome, info.javaHome)
        assertNotNull(info.jdkVersion)
    }

    @Test
    fun testInvalidJavaHomeReturnsNull() {
        val invalidPath = "/non/existent/jdk/path"
        val info = JdkDetector.validateAndGetInfo(invalidPath)
        assertEquals(null, info, "无效的 JDK 路径校验应该返回 null")
    }
}
