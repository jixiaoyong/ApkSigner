import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author : jixiaoyong
 * @description ：运行本机脚本
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/10/2023
 */
object RunCommandUtil {

    fun runCommand(command: String, tag: String = "", printLog: Boolean = true, printError: Boolean = true): Int {
        val logTag = if (tag.isBlank()) "" else "$tag: "

        Logger.info("$tag command: $command")

        return try {
            val process = Runtime.getRuntime().exec(command)

            if (printLog) {
                BufferedReader(InputStreamReader(process.inputStream)).useLines {
                    it.forEach { line ->
                        Logger.info("${logTag}$line")
                    }
                }
            }

            process.waitFor()
        } catch (e: Exception) {
            -1
        }
    }
}