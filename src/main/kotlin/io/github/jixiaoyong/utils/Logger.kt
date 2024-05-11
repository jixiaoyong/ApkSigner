import io.github.jixiaoyong.BuildConfig
import kotlinx.coroutines.*
import me.sujanpoudel.utils.paths.appDataDirectory
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author : jixiaoyong
 * @description ：日志打印&本地缓存
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/8/2023
 */
object Logger {

    const val MAX_LOG_FILE_SIZE = 50 * 1024 * 1024 // 50Mb
    const val MAX_LOG_FILE_COUNT = 50 // 50 个文件

    val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")
    val DATE_FORMATTER_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    var IS_PRINT: Boolean = true
    private var scope: CoroutineScope? = null

    fun init(coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope = coroutineScope
    }

    fun onDispose() {
        scope?.cancel()
        scope = null
    }

    private val logFile: File? by lazy {
        return@lazy runBlocking {
            val fileName = scope?.async {
                val current = LocalDateTime.now()
                val currentLogName = DATE_FORMATTER.format(current)
                val parentPath = getLogFileDirectory()
                reduceLogFileCountAndSize(parentPath)
                getLogFileDirectory().resolve(currentLogName)
            }
            return@runBlocking fileName?.await()?.toFile()
        }
    }

    private fun reduceLogFileCountAndSize(parent: Path?) {
        if (null == parent) {
            return
        }
        scope?.launch {
            while (true) {
                val files = parent.toFile().listFiles()?.sortedBy {
                    Files.readAttributes(it.toPath(), BasicFileAttributes::class.java).creationTime().toMillis()
                } ?: emptyList()

                if (files.size > MAX_LOG_FILE_COUNT || files.sumOf { it.length() } > MAX_LOG_FILE_SIZE) {
                    if (files.isNotEmpty()) {
                        files.first().delete()
                    }
                } else {
                    break
                }
            }
        }
    }

    fun warn(message: String) {
        log("\u001B[33m${message}\u001B[0m")
    }

    fun error(message: String, error: Exception? = null) {
        log("\u001B[31m${message}\n${error}\u001B[0m")
    }

    fun success(message: String) {
        log("\u001B[32m${message}\u001B[0m")
    }

    fun info(message: String) {
        log("\u001B[90m${message}\u001B[0m")
    }

    fun log(message: String?) {
        if (IS_PRINT) {
            println(message)
            message?.let { cacheToFile(it) }
        }
    }

    private fun cacheToFile(msg: String) {
        scope?.launch {
            val file = logFile ?: return@launch
            val current = DATE_FORMATTER_MS.format(LocalDateTime.now())
            val newMsg = current + "  " + removeAnsiCodes(msg) + "\n"
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.appendText(newMsg)
        }
    }

    private fun removeAnsiCodes(input: String): String {
        return input.replace(Regex("\u001B\\[[;\\d]*m"), "")
    }

    suspend fun getCurrentLogHistory(): List<String>? {
        return scope?.async { logFile?.readLines() }?.await()
    }

    fun getCurrentLogFile() = logFile

    fun getLogFileDirectory(): Path {
        val dataDirectory = appDataDirectory(BuildConfig.PACKAGE_NAME)
        return dataDirectory.toString().toPath().resolve("log")
    }
}