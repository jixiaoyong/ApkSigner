// versionInfo.gradle.kts
gradle.taskGraph.whenReady {
    val className = "BuildConfig"
    val packageName = project.extra["packageName"] as String
    val packageVersion = project.extra["packageVersion"] as String
    val group = project.group.toString()
    val ktFile = file("src/main/kotlin/${group.replace('.', '/')}/$className.kt")

    ktFile.writeText(
        """package $group
/**
 * @author : jixiaoyong
 * @description ：APP基础信息,由versionInfo.gradle.kts脚本自动生成
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2024/05/07
 */
object $className {
    const val PACKAGE_NAME = "$group.$packageName"
    const val PACKAGE_VERSION = "$packageVersion"
}"""
    )
}
