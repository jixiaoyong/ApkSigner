package io.github.jixiaoyong.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

/**
 * @author : jixiaoyong
 * @description ：判断APP是否同时运行了多个实例
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 17/1/2024
 */
object AppInstanceChecker {

    private var fileLock: FileLock? = null
    private var lockChannel: FileChannel? = null
    private var lockFile: File? = null

    /**
     * 检查是否已经有其他应用实例在运行.
     * 如果没有其他实例在运行，则锁定文件并返回 false.
     * @return true 如果已经有其他实例在运行，否则返回 false .
     */
    fun isAppRunning(): Boolean {
        println("Checking app instance...")
        try {
            lockFile = File(System.getProperty("user.home"), ".apksigner.lock")
            // "rw"模式会在文件不存在时创建它
            val raf = RandomAccessFile(lockFile, "rw")
            lockChannel = raf.channel

            // 尝试获取文件锁，如果获取不到（返回null），说明已有实例在运行
            fileLock = lockChannel?.tryLock()
            if (fileLock == null) {
                // 确保关闭通道和文件流
                lockChannel?.close()
                raf.close()
                return true
            }

            // 成功获取锁，注册一个关闭钩子，在程序退出时释放锁
            Runtime.getRuntime().addShutdownHook(Thread {
                releaseLock()
            })

            return false
        } catch (e: Exception) {
            println("Error checking app instance: ${e.message}")
            // 出现任何异常都保守地认为有实例在运行
            return true
        }
    }

    /**
     * 释放文件锁并删除锁文件.
     */
    private fun releaseLock() {
        try {
            fileLock?.release()
            lockChannel?.close()
            // 在Windows上，必须在关闭所有相关流之后才能删除文件
            lockFile?.delete()
        } catch (e: Exception) {
            println("Error releasing lock: ${e.message}")
        }
    }
}
