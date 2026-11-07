package io.github.jixiaoyong.utils

import Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoggerMaskTest {

    @Test
    fun testMaskSensitiveArgs_masksKeyAndKeystorePasswords() {
        val masked = Logger.maskSensitiveArgs(
            listOf(
                "apksigner", "sign", "-v",
                "--ks", "/path/to/keystore.jks",
                "--ks-key-alias", "myalias",
                "--key-pass", "pass:s3cr3t-key",
                "--ks-pass", "pass:s3cr3t-store",
                "--out", "/tmp/out.apk", "/tmp/in.apk"
            )
        )
        assertFalse(masked.contains("s3cr3t-key"), "日志里不应出现私钥密码明文：$masked")
        assertFalse(masked.contains("s3cr3t-store"), "日志里不应出现密钥库密码明文：$masked")
        assertTrue(masked.contains("--key-pass pass:***"), "私钥密码应被脱敏为 pass:***：$masked")
        assertTrue(masked.contains("--ks-pass pass:***"), "密钥库密码应被脱敏为 pass:***：$masked")
        // 其它参数原样保留
        assertEquals(
            "apksigner sign -v --ks /path/to/keystore.jks --ks-key-alias myalias " +
                "--key-pass pass:*** --ks-pass pass:*** --out /tmp/out.apk /tmp/in.apk",
            masked
        )
    }

    @Test
    fun testMaskSensitiveArgs_passwordWithSpaceIsFullyMasked() {
        // 密码本身含空格时仍是单个命令参数，整个参数都要遮住；
        // 对拼好的字符串做正则（pass:\S+）会漏掉空格后的部分，所以按参数逐个处理
        val masked = Logger.maskSensitiveArgs(
            listOf("apksigner", "sign", "--key-pass", "pass:my secret pwd")
        )
        assertFalse(masked.contains("secret"), "含空格的密码不应有任何部分留在日志里：$masked")
        assertEquals("apksigner sign --key-pass pass:***", masked)
    }

    @Test
    fun testMaskSensitiveArgs_leavesNonPasswordArgsAlone() {
        // 含 pass 子串但不是 pass: 参数的不应被改动
        val masked = Logger.maskSensitiveArgs(
            listOf("zipalign", "-f", "/path/bypass/app.apk", "--out", "compass.apk")
        )
        assertEquals("zipalign -f /path/bypass/app.apk --out compass.apk", masked)
        assertEquals("", Logger.maskSensitiveArgs(emptyList()))
    }
}
