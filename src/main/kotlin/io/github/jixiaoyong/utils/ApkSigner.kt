import io.github.jixiaoyong.beans.CommandResult
import io.github.jixiaoyong.beans.SignType
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * @author : jixiaoyong
 * @description ： 使用ADB build tools中的zipalign和apksigner.jar对齐并签名APK
 * 上述两个文件可以替换为你自己的文件
 * 官方限制：API 24以上即使开启V1签名也无法生效
 * 他们的使用可以参考官网文章：
 * 签名apk https://developer.android.com/studio/command-line/apksigner?hl=zh-cn#options-sign
 * apksigner：https://developer.android.com/studio/command-line/apksigner?hl=zh-cn#usage-sign
 * zipalign：https://developer.android.com/studio/command-line/zipalign?hl=zh-cn
 * @email : jixiaoyong1995@gmail.com
 * @date : 3/8/2023
 */
object ApkSigner {

    private const val ANDROID_BUILD_TOOLS_DIR_EXAMPLE =
        "/some/directory/to/your/android/sdk/build-tools/34.0.0/"
    private const val AOSP_NAME = "The Android Open Source Project"

    private lateinit var apkSignerCmdPath: String
    private lateinit var zipAlignCmdPath: String
    private lateinit var aaptCmdPath: String

    val apkSignerPath get() = if (::apkSignerCmdPath.isInitialized) apkSignerCmdPath else null
    val zipAlignPath get() = if (::zipAlignCmdPath.isInitialized) zipAlignCmdPath else null
    val aaptPath get() = if (::aaptCmdPath.isInitialized) aaptCmdPath else null

    fun isInitialized(): Boolean {
        return (::apkSignerCmdPath.isInitialized && ::zipAlignCmdPath.isInitialized)
    }

    /**
     * 使用此类之前必须先初始化相关配置
     * @param androidBuildToolsDir android build tools的目录
     * @return 返回失败原因，可对用户展示，返回null则表示正常初始化
     */
    fun init(androidBuildToolsDir: String?): String? {
        val buildTools =
            if (androidBuildToolsDir.isNullOrBlank()) return "" else File(androidBuildToolsDir)
        if (!buildTools.exists() || !buildTools.isDirectory) {
            return "指定的Android SDK中build-tools目录无效或不存在，请重新选择。\n该目录一般为${ANDROID_BUILD_TOOLS_DIR_EXAMPLE}"
        }

        val apkSignerPath = "$androidBuildToolsDir${File.separator}apksigner"
        // windows追加bat
        val apkSignerPathWithBat = if (System.getProperties().getProperty("os.name").contains("Windows")) {
            "$apkSignerPath.bat"
        } else {
            apkSignerPath
        }
        val apkSignerResult = setupApkSigner(apkSignerPathWithBat)
        if (null != apkSignerResult) {
            return apkSignerResult
        }

        val zipAlignPath = "$androidBuildToolsDir${File.separator}zipalign"
        // windows追加exe
        val zipAlignPathWithExe = if (System.getProperties().getProperty("os.name").contains("Windows")) {
            "$zipAlignPath.exe"
        } else {
            zipAlignPath
        }
        val zipAlignResult = setupZipAlign(zipAlignPathWithExe)
        if (null != zipAlignResult) {
            return zipAlignResult
        }

        val aaptPath = "$androidBuildToolsDir${File.separator}aapt"
        // windows追加exe
        val aaptPathExe = if (System.getProperties().getProperty("os.name").contains("Windows")) {
            "$aaptPath.exe"
        } else {
            aaptPath
        }
        val aaptResult = setupAapt(aaptPathExe)
        if (null != aaptResult) {
            return aaptResult
        }
        return null
    }

    fun setupApkSigner(apkSignerPath: String): String? {
        // 校验文件是否为AOSP提供的apk signer，而非本APP避免误操作导致无限循环启动
        val apkSignerFile = File(apkSignerPath)
        if (!apkSignerFile.exists()) {
            return "apkSigner命令不存在，请重新选择。"
        } else if (!apkSignerFile.readText().contains(AOSP_NAME)) {
            return "apkSigner命令不是${AOSP_NAME}官方提供的，请重新选择。"
        }

        val result = RunCommandUtil.runCommand("$apkSignerPath version", "apk signer")

        return if (result != null) {
            "apkSigner命令检查失败，请重试（${result.message}）"
        } else {
            apkSignerCmdPath = apkSignerPath
            null
        }
    }

    fun setupZipAlign(zipAlignPath: String): String? {
        // check os is mac/linux or windows
        val result = if (System.getProperties().getProperty("os.name").contains("Windows")) {
            File(zipAlignPath).exists()
            null
        } else {
            RunCommandUtil.runCommand("command -v $zipAlignPath", "zip align")
        }

        return if (null != result) {
            "zipAlign命令检查失败，请重试（${result.message}）"
        } else {
            zipAlignCmdPath = zipAlignPath
            null
        }
    }

    fun setupAapt(aaptPath: String): String? {
        // check os is mac/linux or windows
        val result = if (System.getProperties().getProperty("os.name").contains("Windows")) {
            File(aaptPath).exists()
            null
        } else {
            RunCommandUtil.runCommand("command -v $aaptPath", "aapt")
        }

        return if (null != result) {
            "aapt命令检查失败，请重试（${result.message}）"
        } else {
            aaptCmdPath = aaptPath
            null
        }
    }


    /**
     * 获取当前apk文件的包名
     */
    suspend fun getApkPackageName(apkFilePath: String?): String?{
        if (apkFilePath.isNullOrBlank()) {
            return null
        }
        val result = withContext(Dispatchers.IO) {
            RunCommandUtil.runCommandWithResult(
                "$aaptPath dump badging $apkFilePath | grep 'package: name'",
                "apk signer"
            )
        }
        return if (result is CommandResult.Success<*>) {
            val commandResult = result.result?.toString()
            val regex = "package: name='(.*?)'".toRegex()
            val matchResult = commandResult?.let { regex.find(it) }
            matchResult?.groups?.get(1)?.value
        } else {
            null
        }
    }

    /**
     * 对齐并签名APK文件，签名后的文件添加_signed后缀,即x_signed.apk
     * @param apkFilePathList 待签名的apk文件绝对路径的列表
     * @param keyStorePath 签名文件路径
     * @param keyAlias 表示 signer 在密钥库中的私钥和证书数据的别名的名称
     * @param keyStorePwd 包含 signer 私钥和证书的密钥库的密码
     * @param keyPwd signer 私钥的密码。
     * @param zipAlign 是否需要对齐
     * @param signedApkDirectory 签名之后的文件输出路径，默认为apkFilePath对应的x.apk所在的文件夹
     * @return 返回结果 io.github.jixiaoyong.beans.CommandResult 成功或失败，及信息
     */
    suspend fun alignAndSignApk(
        apkFilePathList: List<String>,
        keyStorePath: String,
        keyAlias: String,
        keyStorePwd: String,
        keyPwd: String,
        signedApkDirectory: String? = null,
        zipAlign: Boolean = true,
        signVersions: List<SignType> = SignType.DEF_SIGN_TYPES,
        onProgress: (String) -> Unit
    ): List<CommandResult> = coroutineScope {
        val deferredList = apkFilePathList.map {
            async {
                alignAndSignApk(
                    it,
                    keyStorePath,
                    keyAlias,
                    keyStorePwd,
                    keyPwd,
                    signedApkDirectory,
                    zipAlign,
                    signVersions,
                    onProgress
                )
            }
        }

        deferredList.awaitAll()
    }

    /**
     * 对齐并签名APK文件，签名后的文件添加_signed后缀,即x_signed.apk
     * @param apkFilePath 待签名的apk文件绝对路径
     * @param keyStorePath 签名文件路径
     * @param keyAlias 表示 signer 在密钥库中的私钥和证书数据的别名的名称
     * @param keyStorePwd 包含 signer 私钥和证书的密钥库的密码
     * @param keyPwd signer 私钥的密码。
     * @param zipAlign 是否需要对齐
     * @param signedApkDirectory 签名之后的文件输出路径，默认为apkFilePath对应的x.apk所在的文件夹
     * @return 返回结果 io.github.jixiaoyong.beans.CommandResult 成功或失败，及信息
     */
    fun alignAndSignApk(
        apkFilePath: String,
        keyStorePath: String,
        keyAlias: String,
        keyStorePwd: String,
        keyPwd: String,
        signedApkDirectory: String? = null,
        zipAlign: Boolean = true,
        signVersions: List<SignType> = SignType.DEF_SIGN_TYPES,
        onProgress: (String) -> Unit
    ): CommandResult {

        if (!isInitialized()) {
            return CommandResult.Error("请先初始化相关配置")
        }

        val outputDirectory = signedApkDirectory ?: apkFilePath.substringBeforeLast(File.separator)
        val outPutFilePath = outputDirectory + File.separator + apkFilePath.substringAfterLast(File.separator)
            .replace(".apk", "_signed.apk")
        val alignedApkFilePath = if (zipAlign) {
            zipAlignApk(apkFilePath, onProgress = onProgress)
        } else {
            apkFilePath
        }
        if (alignedApkFilePath != null) {
            try {
                // 创建ProcessBuilder对象并设置相关属性
                val signVersionParams = signVersions.flatMap {
                    arrayListOf("--v${it.type}-signing-enabled", "true")
                }.toTypedArray()
                val processBuilder = ProcessBuilder()
                processBuilder.command(
                    apkSignerCmdPath,
                    "sign",
                    "-v",
                    "--ks",// signer 的私钥和证书链包含在给定的基于 Java 的密钥库文件中
                    keyStorePath,
                    "--ks-key-alias",// 表示 signer 在密钥库中的私钥和证书数据的别名的名称
                    keyAlias,
                    "--key-pass",// signer 私钥的密码。
                    "pass:$keyPwd",
                    "--ks-pass",// 包含 signer 私钥和证书的密钥库的密码。
                    "pass:$keyStorePwd",
                    *signVersionParams,
                    "--out",
                    outPutFilePath,
                    alignedApkFilePath
                )
                processBuilder.redirectErrorStream(true)

                Logger.log(processBuilder.command().joinToString(" "))

                // 启动子进程并获取输出流
                val process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                // 读取输出流并打印到控制台
                var line: String? = null
                val totalResult = StringBuffer()
                while (reader.readLine().also {
                        if (null != it) {
                            line = it
                        }
                    } != null) {
                    line?.let {
                        Logger.info(it)
                        onProgress(it)
                        totalResult.append(it)
                    }
                }

                // 等待子进程结束并获取退出值
                val exitCode = process.waitFor()
                Logger.log("Exited with code: $exitCode")
                return if (0 == exitCode) {
                    CommandResult.Success(outPutFilePath)
                } else {
                    CommandResult.Error(totalResult.toString())
                }
            } catch (e: Exception) {
                Logger.error("签名失败", e)
                return CommandResult.Error("${e.message}", e)
            } finally {
                if (zipAlign) {
                    try {
                        File(alignedApkFilePath).delete()
                    } catch (e: Exception) {
                        Logger.error("删除文件失败", e)
                    }
                }
            }
        }
        return CommandResult.Success(outPutFilePath)
    }


    fun zipAlignApk(
        apkFilePath: String,
        alignedApkPath: String? = null,
        onProgress: (String) -> Unit
    ): String? {
        val outPutFilePath = alignedApkPath ?: apkFilePath.replace(".apk", "_aligned.apk")
        try {
// 创建ProcessBuilder对象并设置相关属性
            val processBuilder = ProcessBuilder()
            processBuilder.command(
                zipAlignCmdPath,
                "-v",
                "-f",
                "-p",
                "4",
                apkFilePath,
                outPutFilePath
            )
            processBuilder.redirectErrorStream(true)

// 启动子进程并获取输出流
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

// 读取输出流并打印到控制台
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                Logger.log(line)
                line?.let { onProgress(it) }
            }

// 等待子进程结束并获取退出值
            val exitCode = process.waitFor()
            Logger.log("Exited with code: $exitCode")
        } catch (e: Exception) {
            return null
        }
        return outPutFilePath
    }

    /**
     * 获取Apk文件使用的签名
     * @param apkFilePath 签名的apk文件绝对路径
     * @return 返回结果 Pair(是否成功，失败原因或输出文件路径)
     */
    fun getApkSignInfo(
        apkFilePath: String,
    ): CommandResult {

        if (!isInitialized()) {
            return CommandResult.Error("请先初始化相关配置")
        }

        if (apkFilePath.isNotEmpty()) {
            try {
                // 创建ProcessBuilder对象并设置相关属性
                val processBuilder = ProcessBuilder()
                processBuilder.command(
                    apkSignerCmdPath,
                    "verify",
                    "-v",
                    "--print-certs",
                    apkFilePath,
                )
                processBuilder.redirectErrorStream(true)

                // 启动子进程并获取输出流
                val process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                // 读取输出流并打印到控制台
                val result = reader.readLines().joinToString("\n")

                // 等待子进程结束并获取退出值
                val exitCode = process.waitFor()
                Logger.log("Exited with code: $exitCode")
                return if (0 == exitCode) {
                    CommandResult.Success(result)
                } else {
                    CommandResult.Error(result)
                }
            } catch (e: Exception) {
                Logger.error("查询签名失败", e)
                return CommandResult.Error("${e.message}", e)
            }
        } else {
            return CommandResult.Error("apk文件路径不能为空")
        }
    }
}

