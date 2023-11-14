import io.github.jixiaoyong.pages.signapp.SignType
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

    private lateinit var apkSignerCmdPath: String
    private lateinit var zipAlignCmdPath: String

    val apkSignerPath get() = if (::apkSignerCmdPath.isInitialized) apkSignerCmdPath else null
    val zipAlignPath get() = if (::zipAlignCmdPath.isInitialized) zipAlignCmdPath else null

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

        val apkSignerPath = "$androidBuildToolsDir/apksigner"
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
        return null
    }

    fun setupApkSigner(apkSignerPath: String): String? {
        return if (RunCommandUtil.runCommand(
                "$apkSignerPath version",
                "apk signer",
                true,
                false
            ) != 0
        ) {
            "apkSigner命令不存在，请重新选择。"
        } else {
            apkSignerCmdPath = apkSignerPath
            null
        }
    }

    fun setupZipAlign(zipAlignPath: String): String? {
        // check os is mac/linux or windows
        if (System.getProperties().getProperty("os.name").contains("Windows")) {
            return if (!File(zipAlignPath).exists()) {
                "zipAlign命令不存在，请重新选择。"
            } else {
                zipAlignCmdPath = zipAlignPath
                null
            }
        }
        val command = "command -v $zipAlignPath"
        return if (RunCommandUtil.runCommand(command, "zip align", true, false) != 0) {
            "zipAlign命令不存在，请重新选择。"
        } else {
            zipAlignCmdPath = zipAlignPath
            null
        }
    }

    /**
     * 对齐并签名APK文件
     * @param apkFilePath 待签名的apk文件绝对路径
     * @param keyStorePath 签名文件路径
     * @param keyAlias 表示 signer 在密钥库中的私钥和证书数据的别名的名称
     * @param keyStorePwd 包含 signer 私钥和证书的密钥库的密码
     * @param keyPwd signer 私钥的密码。
     * @param zipAlign 是否需要对齐
     * @param signedApkPath 签名之后的文件输出路径，默认为apkFilePath对应的x.apk添加_signed后缀,即x_signed.apk
     * @return 返回结果 CommandResult 成功或失败，及信息
     */
    fun alignAndSignApk(
        apkFilePath: String, keyStorePath: String, keyAlias: String,
        keyStorePwd: String, keyPwd: String, signedApkPath: String? = null,
        zipAlign: Boolean = true,
        signVersions: List<SignType> = SignType.DEF_SIGN_TYPES,
        onProgress: (String) -> Unit
    ): CommandResult {

        if (!isInitialized()) {
            return CommandResult.Error("请先初始化相关配置")
        }

        val outPutFilePath = signedApkPath ?: apkFilePath.replace(".apk", "_signed.apk")
        val alignedApkFilePath = if (zipAlign) {
            zipAlignApk(apkFilePath, onProgress = onProgress)
        } else {
            apkFilePath
        }
        if (alignedApkFilePath != null) {
            try {
                // 创建ProcessBuilder对象并设置相关属性
                val signVersionParams = signVersions.flatMap {
                    arrayListOf("--v${it.type}-signing-enabled","true")
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
                while (reader.readLine().also {
                        if (null != it) {
                            line = it
                        }
                    } != null) {
                    line?.let {
                        Logger.info(it)
                        onProgress(it)
                    }
                }

                // 等待子进程结束并获取退出值
                val exitCode = process.waitFor()
                Logger.log("Exited with code: $exitCode")
                return if (0 == exitCode) {
                    CommandResult.Success(outPutFilePath)
                } else {
                    CommandResult.Error("$line")
                }
            } catch (e: Exception) {
                Logger.error("签名失败", e)
                return CommandResult.Error("${e.message}", e)
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

sealed class CommandResult {
    class Success<T>(val result: T) : CommandResult()
    class Error<T>(val message: T, val error: Exception? = null) : CommandResult()

    object NOT_EXECUT : CommandResult()
}