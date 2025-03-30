package io.github.jixiaoyong.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock

/**
 * @author : jixiaoyong
 * @description ：判断APP是否同时运行了多个实例
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 17/1/2024
 */
object AppInstanceChecker {

    private var lockFile: File? = null
    private var fileLock: FileLock? = null

    /**
     * 检查是否已经有其他应用实例在运行
     * @return true 如果已经有其他实例在运行，false 否则
     */
    fun isAppRunning(): Boolean {
        println("Checking app instance...")
        try {
            lockFile = File(System.getProperty("user.home"), ".apksigner.lock")
            val raf = RandomAccessFile(lockFile, "rw")
            fileLock = raf.channel.tryLock()

            if (fileLock == null) {
                return true
            }

            Runtime.getRuntime().addShutdownHook(Thread {
                releaseLock()
            })

            return false
        } catch (e: Exception) {
            println("Error checking app instance: ${e.message}")
            return true
        }
    }

    /**
     * 释放文件锁并删除锁文件
     */
    private fun releaseLock() {
        try {
            fileLock?.release()
            lockFile?.delete()
        } catch (e: Exception) {
            println("Error releasing lock: ${e.message}")
        }
    }
}