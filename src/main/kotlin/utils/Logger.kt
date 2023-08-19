/**
 * @author : jixiaoyong
 * @description ：日志打印
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/8/2023
 */
object Logger {

    var IS_PRINT: Boolean = true

    fun warn(message: String) {
        log("\u001B[33m${message}\u001B[0m")
    }

    fun error(message: String) {
        log("\u001B[31m${message}\u001B[0m")
    }

    fun success(message: String) {
        log("\u001B[32m${message}\u001B[0m")
    }

    fun info(message: String) {
        log("\u001B[90m${message}\u001B[0m")
    }

    fun log(message: String) {
        if (IS_PRINT) {
            println(message)
        }
    }
}