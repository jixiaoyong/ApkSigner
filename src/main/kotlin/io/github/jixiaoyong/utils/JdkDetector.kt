package io.github.jixiaoyong.utils

import io.github.jixiaoyong.pages.settings.JdkInfo
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * 智能 JDK / JBR 探测器
 * 优先顺序：
 * 1. 显式指定的路径
 * 2. 环境变量 JAVA_HOME
 * 3. 当前运行 JVM 自身的 java.home (System.getProperty("java.home"))
 * 4. macOS /usr/libexec/java_home 命令探测
 * 5. 常见 Android Studio JBR / 系统 JDK 安装目录探测
 */
object JdkDetector {

    fun detectJdk(preferredPath: String? = null): JdkInfo? {
        // 1. 如果有外部推荐/指定的路径，优先校验
        if (!preferredPath.isNullOrBlank()) {
            val info = validateAndGetInfo(preferredPath)
            if (info != null) return info
        }

        // 2. 检查环境变量 JAVA_HOME
        val envJavaHome = System.getenv("JAVA_HOME")
        if (!envJavaHome.isNullOrBlank()) {
            val info = validateAndGetInfo(envJavaHome)
            if (info != null) return info
        }

        // 3. 检查当前 JVM 运行时自身的 java.home
        val runtimeJavaHome = System.getProperty("java.home")
        if (!runtimeJavaHome.isNullOrBlank()) {
            val info = validateAndGetInfo(runtimeJavaHome)
            if (info != null) return info
        }

        // 4. macOS 专用：通过 /usr/libexec/java_home 探测
        val macDetected = detectMacJavaHome()
        if (macDetected != null) {
            val info = validateAndGetInfo(macDetected)
            if (info != null) return info
        }

        // 5. 常见 IDE 与系统路径自动探测（如 Android Studio 内置 jbr）
        val candidatePaths = getCommonJdkCandidates()
        for (candidate in candidatePaths) {
            val info = validateAndGetInfo(candidate)
            if (info != null) return info
        }

        return null
    }

    /**
     * 校验给定的目录是否为有效 JDK/JRE，并提取其真实版本号
     */
    fun validateAndGetInfo(javaHomePath: String?): JdkInfo? {
        if (javaHomePath.isNullOrBlank()) return null
        val dir = File(javaHomePath)
        if (!dir.exists() || !dir.isDirectory) return null

        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val javaExe = if (isWindows) {
            File(dir, "bin${File.separator}java.exe")
        } else {
            File(dir, "bin${File.separator}java")
        }

        if (!javaExe.exists() || !javaExe.isFile) {
            // macOS 兼容：某些路径如 /Library/Java/JavaVirtualMachines/xxx.jdk，需要定位到 Contents/Home
            val homeSubDir = File(dir, "Contents${File.separator}Home")
            if (homeSubDir.exists() && homeSubDir.isDirectory) {
                return validateAndGetInfo(homeSubDir.absolutePath)
            }
            return null
        }

        val version = getJdkVersion(javaExe)
        return JdkInfo(javaHome = dir.absolutePath, jdkVersion = version)
    }

    private fun getJdkVersion(javaExe: File): String? {
        return try {
            val pb = ProcessBuilder(javaExe.absolutePath, "-version")
            pb.redirectErrorStream(true)
            val process = pb.start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            process.waitFor()
            extractVersion(output)
        } catch (_: Exception) {
            null
        }
    }

    fun extractVersion(output: String?): String? {
        if (output.isNullOrBlank()) return null
        // 匹配形如 version "17.0.14" 或 version "1.8.0_292" 或 openjdk 17.0.2
        val regex = Regex("""(?:version|openjdk)\s+["']?([0-9]+(?:\.[0-9]+)*(?:_[0-9]+)?)["']?""", RegexOption.IGNORE_CASE)
        val match = regex.find(output)
        return match?.groupValues?.get(1) ?: output.lines().firstOrNull()?.trim()
    }

    private fun detectMacJavaHome(): String? {
        val os = System.getProperty("os.name")
        if (!os.contains("Mac", ignoreCase = true)) return null

        val tool = File("/usr/libexec/java_home")
        if (!tool.exists() || !tool.canExecute()) return null

        return try {
            val pb = ProcessBuilder(tool.absolutePath)
            val process = pb.start()
            val path = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() }?.trim()
            process.waitFor()
            if (!path.isNullOrBlank() && File(path).exists()) path else null
        } catch (_: Exception) {
            null
        }
    }

    private fun getCommonJdkCandidates(): List<String> {
        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val candidates = mutableListOf<String>()

        if (isWindows) {
            candidates.add("""C:\Program Files\Android\Android Studio\jbr""")
            candidates.add("""C:\Program Files\Android\Android Studio\jre""")
            candidates.add("""C:\Program Files (x86)\Android\Android Studio\jbr""")
            val javaDir = File("""C:\Program Files\Java""")
            if (javaDir.exists()) {
                javaDir.listFiles()?.filter { it.isDirectory }?.forEach { candidates.add(it.absolutePath) }
            }
        } else {
            // macOS / Linux
            candidates.add("/Applications/Android Studio.app/Contents/jbr/Contents/Home")
            candidates.add("/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home")
            val userJvmDir = File(System.getProperty("user.home"), "Library/Java/JavaVirtualMachines")
            if (userJvmDir.exists()) {
                userJvmDir.listFiles()?.forEach { jvm ->
                    candidates.add(File(jvm, "Contents/Home").absolutePath)
                }
            }
            val sysJvmDir = File("/Library/Java/JavaVirtualMachines")
            if (sysJvmDir.exists()) {
                sysJvmDir.listFiles()?.forEach { jvm ->
                    candidates.add(File(jvm, "Contents/Home").absolutePath)
                }
            }
        }
        return candidates
    }
}
