//import brut.androlib.ApkBuilder
//import brut.androlib.ApkDecoder
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
///**
// * @author : jixiaoyong
// * @description ： TODO
// * @email : jixiaoyong1995@gmail.com
// * @date : 3/8/2023
// */
//object ApkTool {
//
//    const val APK_TOOL = "apktool_2.7.0.jar"
//
//    private var apkToolCommand: String = "java -jar ${libsPath}${APK_TOOL}"
//
//    fun decodeApkFile(apkFilePath: String, outputFilePath: String? = null): String? {
//        var realOutPutFilePath: String? = outputFilePath ?: apkFilePath.toLowerCase().substringBeforeLast(".apk")
//        val command =
//            "$apkToolCommand d $apkFilePath" + (if (outputFilePath != null) "-o $outputFilePath" else "") + " -o $realOutPutFilePath"
//        try {
//            val process = Runtime.getRuntime().exec(command)
//            process.waitFor()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            realOutPutFilePath = null
//        }
//        return realOutPutFilePath
//    }
//
//    fun buildApkFile(decodeFilePath: String, apkFileOutputPath: String? = null): String? {
//        val newApkFilePath = apkFileOutputPath ?: "${decodeFilePath}.apk"
//        val command = "$apkToolCommand b $decodeFilePath -o $newApkFilePath"
//        val success = try {
//            val process = Runtime.getRuntime().exec(command)
//
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//            var line: String?
//            while (reader.readLine().also { line = it } != null) {
//                Logger.info("buildApkFile:$line")
//            }
//
//            val error = BufferedReader(InputStreamReader(process.errorStream))
//            var eline: String?
//            while (error.readLine().also { eline = it } != null) {
//                Logger.error("buildApkFile:$eline")
//            }
//
//            process.waitFor()
//            process.exitValue() == 0
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//        return if (success) newApkFilePath else null
//    }
//}