package io.github.jixiaoyong.utils

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author : jixiaoyong
 * @description ：判断APP是否同时运行了多个实例
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 17/1/2024
 */
object AppProcessUtil {

    // APP的进程数，macOS上面是1，windows上面是2（app启动一次会打开两个进程）
    const val DEFAULT_PROCESS_COUNT = 1
    const val DEFAULT_PROCESS_COUNT_WIN = 2
    fun isDualAppRunning(appName: String): Boolean {
        val appName = appName.toLowerCase()
        val osName = System.getProperty("os.name").toLowerCase()
        var processCount = DEFAULT_PROCESS_COUNT
        val command = when {
            osName.contains("win") -> {
                processCount = DEFAULT_PROCESS_COUNT_WIN
                "tasklist"
            }

            osName.contains("nix") || osName.contains("nux") || osName.contains("mac") -> "ps -e"
            else -> return false
        }
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        var runningCount = 0
        while (reader.readLine().also { line = it } != null) {
            if (line!!.toLowerCase().contains(appName)) {
                runningCount++
            }
            if (runningCount > processCount) {
                return true
            }
        }
        return false
    }

}