package io.github.jixiaoyong.utils

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeyStoreValidatorTest {

    private val keystoreFile = File("src/test/resources/test.jks").let {
        if (it.exists()) it else File("test-sample/test.jks")
    }

    @Test
    fun testValidateSuccess() {
        assertTrue(keystoreFile.exists(), "测试密钥库文件必须存在")
        val result = KeyStoreValidator.validate(
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePassword = "123456",
            keyPassword = "123456"
        )
        assertEquals(KeyStoreValidationResult.Success, result, "全部正确时校验应通过")
    }

    @Test
    fun testValidateFileNotFound() {
        val result = KeyStoreValidator.validate(
            keyStorePath = "path/to/non_existent.jks",
            keyAlias = "testkey",
            keyStorePassword = "123456",
            keyPassword = "123456"
        )
        assertTrue(result is KeyStoreValidationResult.FileNotFound, "文件不存在时应返回 FileNotFound")
    }

    @Test
    fun testValidateStorePasswordError() {
        assertTrue(keystoreFile.exists(), "测试密钥库文件必须存在")
        val result = KeyStoreValidator.validate(
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePassword = "wrong_store_password",
            keyPassword = "123456"
        )
        assertTrue(result is KeyStoreValidationResult.StorePasswordError, "密钥库密码错误时应精准拦截: $result")
    }

    @Test
    fun testValidateAliasNotFound() {
        assertTrue(keystoreFile.exists(), "测试密钥库文件必须存在")
        val result = KeyStoreValidator.validate(
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "unknown_alias",
            keyStorePassword = "123456",
            keyPassword = "123456"
        )
        assertTrue(result is KeyStoreValidationResult.AliasNotFound, "别名不存在时应拦截")
        val aliasResult = result
        assertEquals("unknown_alias", aliasResult.alias)
        assertTrue(aliasResult.availableAliases.contains("testkey"), "应能提取出真实存在的候选别名")
        assertTrue(aliasResult.toString().contains("testkey"), "错误信息中应呈现候选别名")
    }

    @Test
    fun testValidateKeyPasswordError() {
        assertTrue(keystoreFile.exists(), "测试密钥库文件必须存在")
        val result = KeyStoreValidator.validate(
            keyStorePath = keystoreFile.absolutePath,
            keyAlias = "testkey",
            keyStorePassword = "123456",
            keyPassword = "wrong_key_password"
        )
        assertTrue(result is KeyStoreValidationResult.KeyPasswordError, "私钥密码错误时应精准拦截: $result")
    }
}
