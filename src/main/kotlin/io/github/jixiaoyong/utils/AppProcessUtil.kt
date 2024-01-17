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

    fun isDualAppRunning(appName: String): Boolean {
        val appName = appName.toLowerCase()
        val osName = System.getProperty("os.name").toLowerCase()
        val command = when {
            osName.contains("win") -> "tasklist"
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
            if (runningCount > 1) {
                return true
            }
        }
        return false
    }

}