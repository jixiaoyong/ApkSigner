import io.github.jixiaoyong.BuildConfig
import kotlinx.coroutines.*
import me.sujanpoudel.utils.paths.appDataDirectory
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author : jixiaoyong
 * @description ：日志打印&本地缓存
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/8/2023
 */
object Logger {

    /** 日志目录内所有日志文件的总量上限 */
    const val MAX_LOG_FILE_SIZE = 50L * 1024 * 1024 // 50Mb

    /** 日志目录内保留的日志文件个数上限 */
    const val MAX_LOG_FILE_COUNT = 50 // 50 个文件

    /** 日志保留天数：清理时，文件名时间早于「现在 - N 天」的日志一并删除（默认 7 天） */
    const val MAX_LOG_FILE_KEEP_DAYS = 7L

    /** 单个日志文件的硬上限：运行期间超限即轮转到新文件，避免一次长时间运行把当次文件写到几百 MB */
    const val MAX_SINGLE_LOG_FILE_SIZE = 10L * 1024 * 1024 // 10Mb

    private const val LOG_FILE_SUFFIX = ".log"

    /**
     * 只处理自己写出的日志文件（yyyy-MM-dd-HHmmss / yyyy-MM-dd-HHmmss-SSS，可带 .log 后缀）。
     * 日志目录可由设置页一键打开，用户可能把截图、压缩包等放进该目录；
     * 早期实现用 listFiles() 全量参与清理，会把这些文件当成"最旧日志"删掉。
     */
    private val LOG_FILE_NAME_REGEX = Regex("""^\d{4}-\d{2}-\d{2}-\d{6}(-\d{3})?(\.log)?$""")

    /** 整行兜底脱敏：--key-pass pass:xxx / --ks-pass pass:xxx */
    private val SENSITIVE_TEXT_REGEX = Regex("""(--(?:key|ks)-pass\s+)pass:\S+""")

    val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")
    val DATE_FORMATTER_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    /** 日志文件名格式：带毫秒，避免同一秒内多次启动写同一个文件 */
    private val DATE_FORMATTER_LOG_FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss-SSS")

    var IS_PRINT: Boolean = true

    /** 是否把日志写入本地文件；与 IS_PRINT 解耦，可做到"只写文件、不刷控制台" */
    var IS_CACHE: Boolean = true

    private var scope: CoroutineScope? = null

    /** 落盘串行化：同一条日志的时间戳与行内容必须在同一临界区，否则并发协程会写出乱序行 */
    private val writeLock = Any()

    /** 当前正在写的日志文件；超过单文件上限时会被替换 */
    @Volatile
    private var currentLogFile: File? = null

    fun init(coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope = coroutineScope
    }

    fun onDispose() {
        scope?.cancel()
        scope = null
    }

    val logFile: File?
        get() = currentLogFile ?: synchronized(writeLock) { currentLogFile ?: openNewLogFile() }

    /**
     * 打开一个新的日志文件（调用方需持有 writeLock 或处于初始化临界区）。
     * 集中在这里做两件事：按文件名（时间序）清理超出保留策略的老日志、把新文件名登记为当前文件。
     * 注意：不再用 lazy —— 单文件超过 MAX_SINGLE_LOG_FILE_SIZE 时要能换文件。
     */
    private fun openNewLogFile(): File? {
        val directory = getLogFileDirectory().toFile()
        directory.mkdirs()
        runCatching { reduceLogFileCountAndSize(directory) }

        val file = File(directory, DATE_FORMATTER_LOG_FILE.format(LocalDateTime.now()) + LOG_FILE_SUFFIX)
        runCatching { if (!file.exists()) file.createNewFile() }
        currentLogFile = file
        return file
    }

    /**
     * 按"文件个数 + 总量"清理老日志。
     *
     * 与旧实现的差异（都是实际会出事的地方）：
     * 1. 旧实现在 while(true) 里删最旧文件，但 delete() 的返回值被丢弃——一旦删不掉
     *    （目录、只读文件、Windows 上被占用或被杀软锁定），判断条件永远成立 → 协程永久空转并疯狂 IO。
     *    这里改成单遍遍历、删不掉就跳过，结构上不可能死循环。
     * 2. 旧实现把目录里所有文件都当作日志参与排序与删除，用户放进日志目录的截图、压缩包会被删掉。
     *    这里只认 LOG_FILE_NAME_REGEX 命中的文件。
     * 3. 旧实现按 creationTime 排序：部分文件系统（如某些 Linux 实现）该值为 0，排序退化为任意序，
     *    可能先删掉最新日志。文件名本身即时间序（yyyy-MM-dd-HHmmss），按名字排序即可，还省掉 N 次 syscall。
     */
    internal fun reduceLogFileCountAndSize(
        directory: File,
        maxCount: Int = MAX_LOG_FILE_COUNT,
        maxSize: Long = MAX_LOG_FILE_SIZE,
        maxAgeDays: Long = MAX_LOG_FILE_KEEP_DAYS,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val logs = (directory.listFiles() ?: return)
            .filter { it.isFile && isLogFileName(it.name) }
            .sortedBy { it.name }

        val expireBefore = now.minusDays(maxAgeDays)
        var count = logs.size
        var total = logs.sumOf { it.length() }
        for (old in logs) {
            // 正在写的那个文件永远不删（正常情况下它是最新的，这里只是兜底时钟回拨）
            if (old == currentLogFile) break
            val expired = parseLogFileTime(old.name)?.isBefore(expireBefore) == true
            // +1 预留给即将创建的新文件，避免"清理完刚好塞满、再写一个就超额"
            val overLimit = count + 1 > maxCount || total > maxSize
            // 既没超期、也没超额，说明后面的更年轻更小，直接结束
            if (!expired && !overLimit) break
            val length = old.length()
            if (old.delete()) {
                count--
                total -= length
            }
        }
    }

    /**
     * 从日志文件名解析出该日志的创建时间。
     * 用文件名而不是 lastModified：文件被复制/触碰后 mtime 会变，而文件名是写入时的时间戳，
     * 且旧格式（无 .log、无毫秒）与新格式都要能解析，避免历史日志永远不被清理。
     */
    private fun parseLogFileTime(fileName: String): LocalDateTime? {
        val base = fileName.removeSuffix(LOG_FILE_SUFFIX)
        return runCatching { LocalDateTime.parse(base, DATE_FORMATTER_LOG_FILE) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(base, DATE_FORMATTER) }.getOrNull()
    }

    /** 是否为本应用写出的日志文件名（旧格式无后缀、新格式带 .log 都算，便于平滑清理存量） */
    internal fun isLogFileName(name: String): Boolean = LOG_FILE_NAME_REGEX.matches(name)

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

    /**
     * 日志脱敏：apksigner 签名命令里含明文密码（--key-pass pass:<密码> / --ks-pass pass:<密码>），
     * 直接记日志会把密码写进磁盘日志文件（设置页可一键打开日志目录）。
     * 记日志前把 pass: 开头的参数值替换为 ***，其它参数原样保留。
     * 按参数逐个处理（而不是对拼好的字符串做正则），含空格的密码也能整个遮住。
     */
    fun maskSensitiveArgs(args: List<String>): String {
        return args.joinToString(" ") { if (it.startsWith("pass:")) "pass:***" else it }
    }

    /**
     * 整行兜底脱敏，供落盘与"导出日志给他人排查"时使用。
     * 与 maskSensitiveArgs 的区别：那个是按命令参数处理（能整段遮住含空格的密码），
     * 这个是对任意文本行做正则替换，用于兜住异常栈、外部进程回显等无法按参数处理的场景。
     * 只处理 pass:<字面量>；file:/env:/stdin 是引用不是密码，保留原样便于排查。
     * 对已脱敏过的文本幂等（pass:*** 再洗一次仍是 pass:***）。
     */
    fun redactSensitiveText(text: String): String {
        return text.replace(SENSITIVE_TEXT_REGEX, "$1pass:***")
    }

    fun log(message: String?) {
        if (IS_PRINT) {
            println(message)
        }
        if (IS_CACHE && message != null) {
            cacheToFile(message)
        }
    }

    private fun cacheToFile(msg: String) {
        scope?.launch {
            // 兜底脱敏：除了记命令的三处显式调用 maskSensitiveArgs，任何日志行（含异常栈、
            // 外部进程回显）在落盘前再整体清洗一次，防止将来新增日志点漏调。
            val safeMsg = redactSensitiveText(removeAnsiCodes(msg))
            synchronized(writeLock) {
                var file = currentLogFile ?: openNewLogFile() ?: return@launch
                if (file.length() >= MAX_SINGLE_LOG_FILE_SIZE) {
                    file = openNewLogFile() ?: return@launch
                }
                val line = DATE_FORMATTER_MS.format(LocalDateTime.now()) + "  " + safeMsg + "\n"
                runCatching { file.appendText(line) }
            }
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