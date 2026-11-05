package io.github.jixiaoyong.utils

import io.github.jixiaoyong.beans.CommandResult
import io.github.jixiaoyong.beans.SignType
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApkSignerE2ETest {

    /**
     * 自动在当前运行环境中发现可用的 Android build-tools 目录
     */
    private fun findAvailableBuildTools(): File? {
        val candidates = mutableListOf<File>()

        // 1. 显式环境变量或系统属性
        System.getenv("ANDROID_BUILD_TOOLS_DIR")?.let { candidates.add(File(it)) }
        System.getProperty("android.build.tools.dir")?.let { candidates.add(File(it)) }

        // 2. ANDROID_HOME / ANDROID_SDK_ROOT
        listOfNotNull(System.getenv("ANDROID_HOME"), System.getenv("ANDROID_SDK_ROOT")).forEach { sdkHome ->
            val btDir = File(sdkHome, "build-tools")
            if (btDir.exists() && btDir.isDirectory) {
                btDir.listFiles { f -> f.isDirectory }?.sortedDescending()?.forEach { candidates.add(it) }
            }
        }

        // 3. 用户常用开发路径 (Mac / Linux)
        val userDevPath = File("/Users/jixiaoyong/dev/sdks/android/build-tools")
        if (userDevPath.exists() && userDevPath.isDirectory) {
            userDevPath.listFiles { f -> f.isDirectory }?.sortedDescending()?.forEach { candidates.add(it) }
        }

        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val apksignerName = if (isWindows) "apksigner.bat" else "apksigner"
        val zipalignName = if (isWindows) "zipalign.exe" else "zipalign"

        return candidates.firstOrNull { dir ->
            File(dir, apksignerName).exists() && File(dir, zipalignName).exists()
        }
    }

    @Test
    fun testRealApkSignAndVerifyEndToEnd() {
        val buildTools = findAvailableBuildTools()
        assumeTrue(buildTools != null, "当前环境未检测到 Android build-tools，跳过真实签名端到端测试")

        // 准备测试资源
        val keystoreFile = File("src/test/resources/test.jks")
        val sampleApkFile = File("src/test/resources/sample-unsigned.apk")
        assumeTrue(keystoreFile.exists() && sampleApkFile.exists(), "测试所需资源文件不存在")

        // 准备临时工作区
        val tempDir = Files.createTempDirectory("apksigner_e2e_test").toFile()
        tempDir.deleteOnExit()
        val targetApk = File(tempDir, "app.apk")
        Files.copy(sampleApkFile.toPath(), targetApk.toPath(), StandardCopyOption.REPLACE_EXISTING)

        // 1. 初始化 ApkSigner
        val initError = ApkSigner.init(buildTools!!.absolutePath)
        assertEquals(null, initError, "ApkSigner 初始化应当成功: $initError")
        assertTrue(ApkSigner.isInitialized(), "ApkSigner 应当处于初始化完成状态")

        // 2. 执行真实签名
        val logs = mutableListOf<String>()
        val result = ApkSigner.alignAndSignApk(
            apkFilePath = targetApk.absolutePath,
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePwd = "123456",
            keyPwd = "123456",
            signedApkDirectory = tempDir.absolutePath,
            zipAlign = true,
            signVersions = listOf(SignType.V1, SignType.V2),
            onProgress = { logs.add(it) }
        )

        assertTrue(result is CommandResult.Success<*>, "签名应当成功，实际结果: $result")
        val signedApkPath = (result as CommandResult.Success<*>).result as String
        val signedApkFile = File(signedApkPath)
        assertTrue(signedApkFile.exists(), "签名后的 APK 文件应当真实生成于磁盘")
        assertTrue(signedApkFile.length() > 0, "签名后的 APK 大小应当大于 0")

        // 3. 运行 apksigner verify 验证签名合规性
        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val apksignerExe = File(buildTools, if (isWindows) "apksigner.bat" else "apksigner").absolutePath
        val verifyProcess = ProcessBuilder(apksignerExe, "verify", "-v", signedApkFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        val verifyOutput = BufferedReader(InputStreamReader(verifyProcess.inputStream)).readText()
        val exitCode = verifyProcess.waitFor()

        assertEquals(0, exitCode, "apksigner verify 命令退出码应为 0，输出: $verifyOutput")
        assertTrue(verifyOutput.contains("Verifies"), "验签输出必须包含 'Verifies'")
        assertTrue(verifyOutput.contains("Verified using v1 scheme (JAR signing): true"), "V1 签名应当验证成功")
        assertTrue(verifyOutput.contains("Verified using v2 scheme (APK Signature Scheme v2): true"), "V2 签名应当验证成功")
        assertTrue(verifyOutput.contains("Verified using v3 scheme (APK Signature Scheme v3): false"), "V3 签名应当被明确禁用 (Issue #16)")
    }

    @Test
    fun testOnlyV1SchemeSigningDisablesV2AndV3() {
        val buildTools = findAvailableBuildTools()
        assumeTrue(buildTools != null, "当前环境未检测到 Android build-tools，跳过真实签名端到端测试")

        val keystoreFile = File("src/test/resources/test.jks")
        val sampleApkFile = File("src/test/resources/sample-unsigned.apk")
        assumeTrue(keystoreFile.exists() && sampleApkFile.exists(), "测试所需资源文件不存在")

        val tempDir = Files.createTempDirectory("apksigner_v1_only_test").toFile()
        tempDir.deleteOnExit()
        val targetApk = File(tempDir, "app_v1_only.apk")
        Files.copy(sampleApkFile.toPath(), targetApk.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val initError = ApkSigner.init(buildTools!!.absolutePath)
        assertEquals(null, initError, "ApkSigner 初始化应当成功")

        // 仅传递 V1 签名方案 (精确对应 Issue #16 场景)
        val logs = mutableListOf<String>()
        val result = ApkSigner.alignAndSignApk(
            apkFilePath = targetApk.absolutePath,
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePwd = "123456",
            keyPwd = "123456",
            signedApkDirectory = tempDir.absolutePath,
            zipAlign = false,
            signVersions = listOf(SignType.V1),
            onProgress = { logs.add(it) }
        )

        assertTrue(result is CommandResult.Success<*>, "仅 V1 签名应当成功，实际结果: $result")
        val signedApkPath = (result as CommandResult.Success<*>).result as String
        val signedApkFile = File(signedApkPath)
        assertTrue(signedApkFile.exists(), "签名后的 APK 文件应当生成")

        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val apksignerExe = File(buildTools, if (isWindows) "apksigner.bat" else "apksigner").absolutePath
        val verifyProcess = ProcessBuilder(apksignerExe, "verify", "-v", signedApkFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        val verifyOutput = BufferedReader(InputStreamReader(verifyProcess.inputStream)).readText()
        val exitCode = verifyProcess.waitFor()

        assertEquals(0, exitCode, "apksigner verify 命令退出码应为 0，输出: $verifyOutput")
        assertTrue(verifyOutput.contains("Verifies"), "验签输出必须包含 'Verifies'")
        assertTrue(verifyOutput.contains("Verified using v1 scheme (JAR signing): true"), "仅勾选 V1 时，V1 必须为 true")
        assertTrue(verifyOutput.contains("Verified using v2 scheme (APK Signature Scheme v2): false"), "仅勾选 V1 时，V2 必须被明确关闭，不得自动签名 (Issue #16)")
        assertTrue(verifyOutput.contains("Verified using v3 scheme (APK Signature Scheme v3): false"), "仅勾选 V1 时，V3 必须被明确关闭，不得自动签名 (Issue #16)")
    }

    @Test
    fun testRealApkSignWithSpacePath() {
        val buildTools = findAvailableBuildTools()
        assumeTrue(buildTools != null, "当前环境未检测到 Android build-tools，跳过测试")

        val keystoreFile = File("src/test/resources/test.jks")
        val sampleApkFile = File("src/test/resources/sample-unsigned.apk")
        assumeTrue(keystoreFile.exists() && sampleApkFile.exists(), "测试所需资源文件不存在")

        // 重点：创建路径中包含空格的临时目录和文件名 (Issue #24)
        val spaceTempDir = File(System.getProperty("java.io.tmpdir"), "apk test space dir").apply { mkdirs() }
        spaceTempDir.deleteOnExit()
        val targetApk = File(spaceTempDir, "my space app.apk")
        Files.copy(sampleApkFile.toPath(), targetApk.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val initError = ApkSigner.init(buildTools!!.absolutePath)
        assertEquals(null, initError, "ApkSigner 初始化应当成功")

        val result = ApkSigner.alignAndSignApk(
            apkFilePath = targetApk.absolutePath,
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePwd = "123456",
            keyPwd = "123456",
            signedApkDirectory = spaceTempDir.absolutePath,
            zipAlign = false,
            signVersions = listOf(SignType.V1, SignType.V2),
            onProgress = {}
        )

        assertTrue(result is CommandResult.Success<*>, "含空格路径的 APK 签名应当成功: $result")
        val signedApkPath = (result as CommandResult.Success<*>).result as String
        val signedApkFile = File(signedApkPath)
        assertTrue(signedApkFile.exists(), "签名后的 APK 文件应当生成在含空格的路径下")

        // 使用 getApkSignInfo 验签
        val verifyResult = ApkSigner.getApkSignInfo(signedApkPath)
        assertTrue(verifyResult is CommandResult.Success<*>, "对含空格路径的已签名 APK 进行验签应当成功")
        val verifyOutput = (verifyResult as CommandResult.Success<*>).result.toString()
        assertTrue(verifyOutput.contains("Verifies"), "验签结果必须包含 Verifies")
    }

    @Test
    fun testSignWithWrongPasswordOrAliasIntercepted() {
        val buildTools = findAvailableBuildTools()
        assumeTrue(buildTools != null, "当前环境未检测到 Android build-tools，跳过测试")

        val keystoreFile = File("src/test/resources/test.jks").let {
            if (it.exists()) it else File("test-sample/test.jks")
        }
        val sampleApkFile = File("src/test/resources/sample-unsigned.apk").let {
            if (it.exists()) it else File("test-sample/sample-unsigned.apk")
        }
        assumeTrue(keystoreFile.exists() && sampleApkFile.exists(), "测试所需资源文件不存在")

        ApkSigner.init(buildTools!!.absolutePath)

        // 场景 1：错误别名被前置拦截
        val resultAlias = ApkSigner.alignAndSignApk(
            apkFilePath = sampleApkFile.absolutePath,
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "non_existent_alias",
            keyStorePwd = "123456",
            keyPwd = "123456",
            onProgress = {}
        )
        assertTrue(resultAlias is CommandResult.Error<*>, "错误别名应当被安全拦截")
        val errorMsgAlias = (resultAlias as CommandResult.Error<*>).message.toString()
        assertTrue(errorMsgAlias.contains("未在签名文件中找到别名") || errorMsgAlias.contains("别名"), "错误信息应当准确指出别名问题: $errorMsgAlias")

        // 场景 2：错误密钥库密码被前置拦截
        val resultPass = ApkSigner.alignAndSignApk(
            apkFilePath = sampleApkFile.absolutePath,
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePwd = "wrong_password_999",
            keyPwd = "123456",
            onProgress = {}
        )
        assertTrue(resultPass is CommandResult.Error<*>, "错误密码应当被安全拦截")
        val errorMsgPass = (resultPass as CommandResult.Error<*>).message.toString()
        assertTrue(errorMsgPass.contains("密码错误"), "错误信息应当准确指出密码错误: $errorMsgPass")
    }

    @Test
    fun testExtractPackageNameWithoutGrep() = kotlinx.coroutines.runBlocking {
        val buildTools = findAvailableBuildTools()
        assumeTrue(buildTools != null, "当前环境未检测到 Android build-tools，跳过测试")

        val sampleApkFile = File("src/test/resources/sample-unsigned.apk").let {
            if (it.exists()) it else File("test-sample/sample-unsigned.apk")
        }
        assumeTrue(sampleApkFile.exists(), "测试所需资源文件不存在")

        ApkSigner.init(buildTools!!.absolutePath)
        val packageName = ApkSigner.getApkPackageName(sampleApkFile.absolutePath)
        assertTrue(!packageName.isNullOrBlank(), "提取出的包名不应为空")
        assertEquals("io.github.jixiaoyong.test", packageName, "包名应当能被内存正则准确匹配解析")
    }
}
