package io.github.jixiaoyong.utils

import io.github.jixiaoyong.beans.SignType

/**
 * 根据 Android build-tools 版本解析 apksigner 所支持的签名版本参数，
 * 避免在低版本 build-tools 下因传入不支持的参数（如 --v4-signing-enabled）而报错。
 */
object SignVersionResolver {

    /**
     * 解析 build-tools 目录、版本字符串或命令完整路径中的主版本号。
     * 例如：
     * - "34.0.0" -> 34
     * - "/path/to/build-tools/28.0.3" -> 28
     * - "/path/to/build-tools/28.0.3/apksigner" -> 28
     * - "C:\\sdk\\build-tools\\30.0.3\\apksigner.bat" -> 30
     */
    fun parseMajorVersion(buildToolsDirOrVersion: String?): Int? {
        if (buildToolsDirOrVersion.isNullOrBlank()) return null
        val normalized = buildToolsDirOrVersion.trim().replace('\\', '/').trimEnd('/')

        // 1. 优先检查路径是否包含 build-tools/<version> 片段
        val buildToolsRegex = Regex("""build-tools/([0-9]+)(?:\.[0-9]+)*""", RegexOption.IGNORE_CASE)
        val match = buildToolsRegex.find(normalized)
        if (match != null) {
            return match.groupValues[1].toIntOrNull()
        }

        // 2. 依次检查路径中由后向前的各段，是否符合纯数字或语义化版本号格式（如 34.0.0 或 34）
        val segments = normalized.split('/').filter { it.isNotBlank() }
        val versionRegex = Regex("""^([0-9]+)(?:\.[0-9]+)*$""")
        for (seg in segments.reversed()) {
            val vMatch = versionRegex.find(seg)
            if (vMatch != null) {
                return vMatch.groupValues[1].toIntOrNull()
            }
        }

        return null
    }

    /**
     * 根据指定的 build-tools 主版本号，获取当前 apksigner 支持的 SignType
     * build-tools >= 30: 支持 V1, V2, V3, V4
     * build-tools == 29: 支持 V1, V2, V3
     * build-tools < 29: 支持 V1, V2
     * 未指定/null: 默认支持所有版本 V1~V4
     */
    fun supportedSignTypes(majorVersion: Int?): List<SignType> {
        return when {
            majorVersion == null -> SignType.ALL_SIGN_TYPES
            majorVersion >= 30 -> listOf(SignType.V1, SignType.V2, SignType.V3, SignType.V4)
            majorVersion == 29 -> listOf(SignType.V1, SignType.V2, SignType.V3)
            else -> listOf(SignType.V1, SignType.V2)
        }
    }

    /**
     * 生成传给 apksigner 命令的参数列表
     */
    fun resolveSignParams(
        selectedVersions: List<SignType>,
        buildToolsVersionOrDir: String? = null
    ): List<String> {
        val major = parseMajorVersion(buildToolsVersionOrDir)
        val supported = supportedSignTypes(major)
        val params = mutableListOf<String>()
        for (type in supported) {
            val enabled = selectedVersions.contains(type)
            params.add("--v${type.type}-signing-enabled")
            params.add(if (enabled) "true" else "false")
        }
        return params
    }
}
