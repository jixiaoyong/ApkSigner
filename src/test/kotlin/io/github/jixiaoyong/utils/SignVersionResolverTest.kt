package io.github.jixiaoyong.utils

import io.github.jixiaoyong.beans.SignType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignVersionResolverTest {

    @Test
    fun testParseMajorVersion() {
        assertEquals(34, SignVersionResolver.parseMajorVersion("34.0.0"))
        assertEquals(28, SignVersionResolver.parseMajorVersion("28.0.3"))
        assertEquals(29, SignVersionResolver.parseMajorVersion("/Users/user/Library/Android/sdk/build-tools/29.0.2"))
        assertEquals(30, SignVersionResolver.parseMajorVersion("C:\\Android\\sdk\\build-tools\\30.0.3\\"))
        assertEquals(28, SignVersionResolver.parseMajorVersion("/Users/user/Library/Android/sdk/build-tools/28.0.3/apksigner"))
        assertEquals(30, SignVersionResolver.parseMajorVersion("C:\\Android\\sdk\\build-tools\\30.0.3\\apksigner.bat"))
        assertEquals(null, SignVersionResolver.parseMajorVersion(""))
        assertEquals(null, SignVersionResolver.parseMajorVersion(null))
        assertEquals(null, SignVersionResolver.parseMajorVersion("invalid_version_without_numbers"))
    }

    @Test
    fun testSupportedSignTypesForLegacySdk() {
        val types28 = SignVersionResolver.supportedSignTypes(28)
        assertEquals(listOf(SignType.V1, SignType.V2), types28)

        val types29 = SignVersionResolver.supportedSignTypes(29)
        assertEquals(listOf(SignType.V1, SignType.V2, SignType.V3), types29)

        val types30 = SignVersionResolver.supportedSignTypes(30)
        assertEquals(listOf(SignType.V1, SignType.V2, SignType.V3, SignType.V4), types30)

        val types34 = SignVersionResolver.supportedSignTypes(34)
        assertEquals(listOf(SignType.V1, SignType.V2, SignType.V3, SignType.V4), types34)

        val typesNull = SignVersionResolver.supportedSignTypes(null)
        assertEquals(SignType.ALL_SIGN_TYPES, typesNull)
    }

    @Test
    fun testResolveSignParamsExcludesUnsupportedOptions() {
        // Build-tools 28 路径: 仅支持 V1, V2，绝不应包含 --v3 或 --v4
        val params28 = SignVersionResolver.resolveSignParams(
            selectedVersions = listOf(SignType.V1, SignType.V2, SignType.V3, SignType.V4),
            buildToolsVersionOrDir = "/opt/android/build-tools/28.0.3/apksigner"
        )
        assertTrue(params28.contains("--v1-signing-enabled"))
        assertTrue(params28.contains("--v2-signing-enabled"))
        assertFalse(params28.contains("--v3-signing-enabled"), "Build-tools 28 should not contain --v3-signing-enabled")
        assertFalse(params28.contains("--v4-signing-enabled"), "Build-tools 28 should not contain --v4-signing-enabled")

        // Build-tools 29 路径: 支持 V1, V2, V3，绝不应包含 --v4
        val params29 = SignVersionResolver.resolveSignParams(
            selectedVersions = listOf(SignType.V1, SignType.V3),
            buildToolsVersionOrDir = "C:\\Android\\sdk\\build-tools\\29.0.2\\apksigner.bat"
        )
        assertTrue(params29.contains("--v1-signing-enabled"))
        assertTrue(params29.contains("--v2-signing-enabled"))
        assertTrue(params29.contains("--v3-signing-enabled"))
        assertFalse(params29.contains("--v4-signing-enabled"), "Build-tools 29 should not contain --v4-signing-enabled")

        // 检查布尔值映射是否准确（V1 勾选为 true，V2 未勾选为 false，V3 勾选为 true）
        val v1Index = params29.indexOf("--v1-signing-enabled")
        assertEquals("true", params29[v1Index + 1])
        val v2Index = params29.indexOf("--v2-signing-enabled")
        assertEquals("false", params29[v2Index + 1])
        val v3Index = params29.indexOf("--v3-signing-enabled")
        assertEquals("true", params29[v3Index + 1])

        // Build-tools 34: 支持全部 V1~V4
        val params34 = SignVersionResolver.resolveSignParams(
            selectedVersions = listOf(SignType.V1),
            buildToolsVersionOrDir = "34.0.0"
        )
        assertTrue(params34.contains("--v1-signing-enabled"))
        assertTrue(params34.contains("--v2-signing-enabled"))
        assertTrue(params34.contains("--v3-signing-enabled"))
        assertTrue(params34.contains("--v4-signing-enabled"))
        val v4Index = params34.indexOf("--v4-signing-enabled")
        assertEquals("false", params34[v4Index + 1])
    }
}
