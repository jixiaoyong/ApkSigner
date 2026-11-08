package io.github.jixiaoyong.utils

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * zipalign / aapt 路径校验的跨平台回归测试。
 *
 * 原实现在 macOS/Linux 上执行 `command -v <路径>`。command 是 shell 内建，
 * ProcessBuilder 不经过 shell，只有恰好提供 /usr/bin/command 兼容脚本的系统能用：
 *   - macOS：有 FreeBSD wrapper，正常
 *   - RHEL 系：有 builtin 包装脚本，正常
 *   - Debian 系（Ubuntu、ubuntu-latest）：没有，检查恒失败，签名被挡
 *
 * 这组用例在 macOS 上用旧实现也能过，在 ubuntu-latest 上必红；
 * 改为 File.isFile && canExecute() 之后三平台一致通过。
 */
class PlatformToolingTest {

    private val tempDirs = mutableListOf<File>()

    private fun fakeTool(name: String, executable: Boolean = true): File {
        val dir = Files.createTempDirectory("apksigner-tool-test").toFile()
        tempDirs.add(dir)
        return File(dir, name).apply {
            writeText("#!/bin/sh\nexit 0\n")
            setExecutable(executable)
        }
    }

    private val isWindows: Boolean
        get() = System.getProperty("os.name").contains("Windows", ignoreCase = true)

    @AfterTest
    fun cleanup() {
        tempDirs.forEach { dir -> dir.walkBottomUp().forEach { it.delete() } }
        tempDirs.clear()
    }

    @Test
    fun testSetupZipAlign_acceptsExistingExecutableFile() {
        val tool = fakeTool("zipalign")
        assertNull(
            ApkSigner.setupZipAlign(tool.absolutePath),
            "存在的可执行文件必须通过校验；旧实现在 Debian/Ubuntu 上会失败"
        )
    }

    @Test
    fun testSetupAapt_acceptsExistingExecutableFile() {
        val tool = fakeTool("aapt")
        assertNull(
            ApkSigner.setupAapt(tool.absolutePath),
            "存在的可执行文件必须通过校验；旧实现在 Debian/Ubuntu 上会失败"
        )
    }

    @Test
    fun testSetupZipAlign_rejectsMissingFile() {
        val missing = File(System.getProperty("java.io.tmpdir"), "definitely-not-exist-${System.nanoTime()}")
        assertNotNull(ApkSigner.setupZipAlign(missing.absolutePath), "不存在的文件必须被拒绝")
    }

    @Test
    fun testSetupZipAlign_rejectsDirectory() {
        val dir = Files.createTempDirectory("apksigner-dir-test").toFile()
        tempDirs.add(dir)
        assertNotNull(ApkSigner.setupZipAlign(dir.absolutePath), "目录不能当成可执行文件")
    }

    @Test
    fun testSetupAapt_rejectsDirectory() {
        val dir = Files.createTempDirectory("apksigner-dir-test").toFile()
        tempDirs.add(dir)
        assertNotNull(ApkSigner.setupAapt(dir.absolutePath), "目录不能当成可执行文件（旧 Windows 分支恒通过）")
    }

    @Test
    fun testSetupZipAlign_acceptsPathWithSpaces() {
        val tool = fakeTool("zip align tool")
        assertNull(
            ApkSigner.setupZipAlign(tool.absolutePath),
            "含空格的路径必须可用；旧 setupAapt 的字符串重载会按空格切分命令导致误判"
        )
    }

    @Test
    fun testSetupZipAlign_rejectsNonExecutableFile() {
        if (isWindows) return // Windows 无 POSIX 执行位，此用例不适用
        val tool = fakeTool("zipalign", executable = false)
        assertNotNull(
            ApkSigner.setupZipAlign(tool.absolutePath),
            "丢失执行位的文件应提前被拒绝，而不是等到签名时才抛 EACCES"
        )
    }
}
