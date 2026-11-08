package io.github.jixiaoyong.utils

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 日志保留策略与兜底脱敏的单元测试。
 *
 * 覆盖的都是修复前的实际缺陷：
 * - 清理时把日志目录里的非日志文件（截图、压缩包）一并参与排序与删除；
 * - 按 creationTime 排序在部分文件系统上失效；
 * - delete() 失败后旧实现 while(true) 永不退出（这里用只读目录复现"删不掉"，验证能正常返回）。
 */
class LoggerRetentionTest {

    private val tempDirs = mutableListOf<File>()

    private fun newTempDir(): File {
        val dir = Files.createTempDirectory("apksigner-log-test").toFile()
        tempDirs.add(dir)
        return dir
    }

    private fun logFile(dir: File, name: String, content: String = "x"): File {
        return File(dir, name).apply { writeText(content) }
    }

    @AfterTest
    fun cleanup() {
        tempDirs.forEach { dir ->
            dir.setWritable(true)
            dir.walkBottomUp().forEach { it.setWritable(true); it.delete() }
        }
        tempDirs.clear()
    }

    @Test
    fun testIsLogFileName_acceptsOldAndNewFormatOnly() {
        assertTrue(Logger.isLogFileName("2026-11-05-192912"), "旧格式（无后缀）应被识别")
        assertTrue(Logger.isLogFileName("2026-11-05-192912.log"), "新格式应被识别")
        assertTrue(Logger.isLogFileName("2026-11-05-192912-123.log"), "带毫秒的新格式应被识别")

        assertFalse(Logger.isLogFileName("screenshot.png"), "用户放进去的截图不是日志")
        assertFalse(Logger.isLogFileName("log-2026-11-05.zip"), "压缩包不是日志")
        assertFalse(Logger.isLogFileName("2026-11-05"), "日期目录名不是日志文件")
        assertFalse(Logger.isLogFileName("2026-11-05-192912.bak"), "改过后缀的备份不是日志")
    }

    @Test
    fun testReduce_keepsNonLogFilesAndDeletesOldestLogsByName() {
        val dir = newTempDir()
        // 5 个旧格式 + 1 个新格式，文件名即时间序
        listOf(
            "2026-11-01-100000", "2026-11-02-100000", "2026-11-03-100000",
            "2026-11-04-100000", "2026-11-05-100000", "2026-11-06-100000.log"
        ).forEach { logFile(dir, it) }
        // 干扰项：扩展名不符、目录名像日志
        val png = logFile(dir, "screenshot.png", "PNG")
        val zip = logFile(dir, "log-archive.zip", "ZIP")
        val dirLikeLog = File(dir, "2026-11-07-000000").apply { mkdirs() }

        Logger.reduceLogFileCountAndSize(dir, maxCount = 2, maxSize = Long.MAX_VALUE)

        val left = dir.listFiles()!!.filter { it.isFile && Logger.isLogFileName(it.name) }.map { it.name }.sorted()
        assertEquals(
            listOf("2026-11-06-100000.log"),
            left,
            "maxCount 是目录内最终文件数（含即将新建的那个），故 maxCount=2 时只剩最新 1 个；" +
                "其余按文件名时间序（= 时间序）从旧到新删除"
        )
        assertTrue(png.exists(), "非日志文件 screenshot.png 不得被删除")
        assertTrue(zip.exists(), "非日志文件 log-archive.zip 不得被删除")
        assertTrue(dirLikeLog.isDirectory, "名字像日志的目录不得被删除")
    }

    @Test
    fun testReduce_respectsTotalSizeLimit() {
        val dir = newTempDir()
        // 每个 100 字节，共 5 个 = 500 字节；上限 250 字节 → 只能留 2 个
        repeat(5) { index ->
            logFile(dir, "2026-11-0${index + 1}-100000", "a".repeat(100))
        }

        Logger.reduceLogFileCountAndSize(dir, maxCount = 100, maxSize = 250L)

        val left = dir.listFiles()!!.filter { Logger.isLogFileName(it.name) }.sortedBy { it.name }
        assertTrue(left.size <= 2, "总量超限时应从最旧开始删除，实际剩余 ${left.size} 个")
        assertTrue(left.none { it.name == "2026-11-01-100000" }, "最旧的日志应先被删除")
        assertTrue(left.any { it.name == "2026-11-05-100000" }, "最新的日志必须保留")
    }

    @Test
    fun testReduce_deletionFailureDoesNotHangOrThrow() {
        val dir = newTempDir()
        repeat(3) { index -> logFile(dir, "2026-11-0${index + 1}-100000") }
        // 目录设为只读：文件删不掉（非 root 用户），旧实现会在这里 while(true) 空转不返回
        dir.setWritable(false)

        Logger.reduceLogFileCountAndSize(dir, maxCount = 1, maxSize = Long.MAX_VALUE)

        dir.setWritable(true)
        // 不断言文件是否删掉（不同系统/权限模型下有差异），只要求：能返回、不抛异常
        assertEquals(3, dir.listFiles()!!.count { Logger.isLogFileName(it.name) })
    }

    @Test
    fun testRedactSensitiveText_masksLiteralPasswordsOnlyOnce() {
        val line = "apksigner sign --key-pass pass:keySecret --ks-pass pass:storeSecret --out out.apk in.apk"
        val redacted = Logger.redactSensitiveText(line)

        assertFalse(redacted.contains("keySecret"), "私钥密码不应留在日志行里：$redacted")
        assertFalse(redacted.contains("storeSecret"), "密钥库密码不应留在日志行里：$redacted")
        assertTrue(redacted.contains("--key-pass pass:***"), "应脱敏为 pass:***：$redacted")
        assertTrue(redacted.contains("--ks-pass pass:***"), "应脱敏为 pass:***：$redacted")
        assertTrue(redacted.contains("--out out.apk"), "非密码参数应原样保留：$redacted")

        assertEquals(redacted, Logger.redactSensitiveText(redacted), "对已脱敏文本必须幂等")
    }

    @Test
    fun testRedactSensitiveText_keepsNonLiteralPasswordReferences() {
        // file:/env:/stdin 是引用而不是密码本身，保留原样才便于排查
        val line = "apksigner sign --ks-pass file:/tmp/pass.txt --key-pass env:MY_KEY_PASS --ks-pass stdin"
        assertEquals(line, Logger.redactSensitiveText(line))
    }
}
