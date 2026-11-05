import io.github.jixiaoyong.beans.CommandResult
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * @author : jixiaoyong
 * @description ：运行本机外部脚本与进程（支持安全命令参数列表、完整标准/错误流捕获、双端 PATH/Path 强力注入）
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/10/2023
 */
object RunCommandUtil {

    data class ProcessOutput(
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )

    /**
     * 执行命令列表（推荐，避免带空格路径被切碎）
     * @return 成功返回null，失败返回包含完整详细错误信息的 Exception
     */
    fun runCommand(
        command: List<String>,
        tag: String = "",
        printLog: Boolean = true,
        printError: Boolean = true,
        javaHome: String? = null
    ): Exception? {
        val logTag = if (tag.isBlank()) "" else "$tag: "
        Logger.info("$tag command: ${command.joinToString(" ")}")

        return try {
            val process = createProcessBuilder(command, javaHome).start()
            val output = handleProcessOutput(process, logTag, printLog, printError)
            if (output.exitCode == 0) {
                null
            } else {
                val detail = buildString {
                    append(logTag).append("exit code: ").append(output.exitCode)
                    if (output.stderr.isNotBlank()) {
                        append("\nerr: ").append(output.stderr.trim())
                    }
                    if (output.stdout.isNotBlank()) {
                        append("\nout: ").append(output.stdout.trim())
                    }
                }
                Exception(detail)
            }
        } catch (e: Exception) {
            if (printError) Logger.error("$tag error: $e")
            e
        }
    }

    /**
     * 执行命令（兼容旧调用，单个可执行文件路径即使带空格也会被安全保护）
     */
    fun runCommand(
        command: String,
        tag: String = "",
        printLog: Boolean = true,
        printError: Boolean = true,
        javaHome: String? = null
    ): Exception? {
        val cmdList = if (File(command).exists()) {
            listOf(command)
        } else {
            // 如果由引号或空格组合，安全切分
            parseCommandString(command)
        }
        return runCommand(cmdList, tag, printLog, printError, javaHome)
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
            val output = handleProcessOutput(process, logTag, printLog, printError)
            if (output.exitCode == 0) {
                CommandResult.Success(output.stdout)
            } else {
                val detail = buildString {
                    append(logTag).append("exit code: ").append(output.exitCode)
                    if (output.stderr.isNotBlank()) {
                        append("\nerr: ").append(output.stderr.trim())
                    }
                    if (output.stdout.isNotBlank()) {
                        append("\nout: ").append(output.stdout.trim())
                    }
                }
                CommandResult.Error(detail)
            }
        } catch (e: Exception) {
            if (printError) Logger.error("$tag error: $e")
            CommandResult.Error("$tag error: $e", e)
        }
    }

    private fun handleProcessOutput(
        process: Process,
        logTag: String,
        printLog: Boolean,
        printError: Boolean
    ): ProcessOutput {
        val stdoutLines = mutableListOf<String>()
        val stderrLines = mutableListOf<String>()

        // 并发或顺序安全读取流，避免子进程输出缓冲区填满阻塞
        val stdoutReader = Thread {
            try {
                BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        if (printLog) Logger.info("$logTag$line")
                        stdoutLines.add(line)
                    }
                }
            } catch (_: Exception) {}
        }

        val stderrReader = Thread {
            try {
                BufferedReader(InputStreamReader(process.errorStream)).useLines { lines ->
                    lines.forEach { line ->
                        if (printError) Logger.error("$logTag$line")
                        stderrLines.add(line)
                    }
                }
            } catch (_: Exception) {}
        }

        stdoutReader.start()
        stderrReader.start()

        val exitCode = process.waitFor()
        stdoutReader.join()
        stderrReader.join()

        return ProcessOutput(
            exitCode = exitCode,
            stdout = stdoutLines.joinToString("\n"),
            stderr = stderrLines.joinToString("\n")
        )
    }

    fun createProcessBuilder(command: List<String>? = null, javaHome: String? = null): ProcessBuilder {
        val processBuilder = ProcessBuilder(command ?: emptyList())
        processBuilder.directory(File(System.getProperty("user.dir")))
        
        if (!javaHome.isNullOrBlank()) {
            val env = processBuilder.environment()
            env["JAVA_HOME"] = javaHome
            val javaBin = File(javaHome, "bin").absolutePath
            val currentPath = env["PATH"] ?: env["Path"] ?: ""
            val newPath = "$javaBin${File.pathSeparator}$currentPath"
            env["PATH"] = newPath
            env["Path"] = newPath // 兼容 Windows 下的 Path 键名
        }
        return processBuilder
    }

    /**
     * 安全切分命令行参数，保留双引号或单引号包裹的带空格路径
     */
    private fun parseCommandString(command: String): List<String> {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return emptyList()

        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false

        for (ch in trimmed) {
            when (ch) {
                '\'' -> {
                    if (!inDoubleQuote) inSingleQuote = !inSingleQuote else current.append(ch)
                }
                '"' -> {
                    if (!inSingleQuote) inDoubleQuote = !inDoubleQuote else current.append(ch)
                }
                ' ', '\t' -> {
                    if (inSingleQuote || inDoubleQuote) {
                        current.append(ch)
                    } else if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.setLength(0)
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }
        return tokens
    }
}
