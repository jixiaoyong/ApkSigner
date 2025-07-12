import io.github.jixiaoyong.beans.CommandResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

/**
 * @author : jixiaoyong
 * @description ：运行本机脚本
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/10/2023
 */
object RunCommandUtil {

    /**
     * 执行bash命令
     * @return 成功返回null，失败返回Exception类型错误原因
     */
    fun runCommand(
        command: String,
        tag: String = "",
        printLog: Boolean = true,
        printError: Boolean = true,
        javaHome: String? = null
    ): Exception? {
        val logTag = if (tag.isBlank()) "" else "$tag: "
        Logger.info("$tag command: $command")

        return try {
            val process = createProcessBuilder(command.split("\\s+".toRegex()), javaHome).start()
            val result = handleProcessOutput(process, logTag, printLog, printError)
            if (result == 0) {
                null
            } else {
                Exception(
                    "${logTag}exit code: $result\n${
                        process.inputStream.bufferedReader().readText()
                    }\nerr:${process.errorStream.bufferedReader().readText()}"
                )
            }
        } catch (e: Exception) {
            if (printError) Logger.error("$tag error: $e")
            e
        }
    }

    fun runCommandWithResult(
        vararg command: String,
        tag: String = "",
        printLog: Boolean = true,
        printError: Boolean = true,
        javaHome: String? = null
    ): CommandResult {
        val logTag = if (tag.isBlank()) "" else "$tag: "
        Logger.info("$tag command: ${command.joinToString(" ")}")

        return try {
            val process = createProcessBuilder(command.toList(), javaHome).start()
            val result = handleProcessOutput(process, logTag, printLog, printError)
            if (result == 0) {
                CommandResult.Success(process.inputStream.bufferedReader().readText())
            } else {
                CommandResult.Error(
                    "${logTag}exit code: $result\nerr:${
                        process.errorStream.bufferedReader().readText()
                    }\n${process.inputStream.bufferedReader().readText()}"
                )
            }
        } catch (e: Exception) {
            if (printError) Logger.error("$tag error: $e")
            CommandResult.Error("$tag error: $e", e)
        }
    }

    private fun handleProcessOutput(process: Process, logTag: String, printLog: Boolean, printError: Boolean): Int {
        val logBuffer = StringBuffer()
        val errBuffer = StringBuffer()

        if (printLog) {
            BufferedReader(InputStreamReader(process.inputStream)).useLines {
                it.forEach { line ->
                    Logger.info("${logTag}$line")
                    logBuffer.append(line).append("\n")
                }
            }
        }

        if (printError) {
            BufferedReader(InputStreamReader(process.errorStream)).useLines {
                it.forEach { line ->
                    Logger.error("${logTag}$line")
                    errBuffer.append(line).append("\n")
                }
            }
        }

        return process.waitFor()
    }

    fun createProcessBuilder(command: List<String>? = null, javaHome: String? = null): ProcessBuilder {
        val processBuilder = ProcessBuilder(command ?: emptyList())
        processBuilder.directory(File(System.getProperty("user.dir")))
        processBuilder.redirectErrorStream(true)
        if (javaHome != null) {
            processBuilder.environment()["JAVA_HOME"] = javaHome
        }
        return processBuilder
    }
}