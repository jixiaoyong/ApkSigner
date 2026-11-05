package io.github.jixiaoyong.utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyStore
import java.security.UnrecoverableKeyException

/**
 * KeyStore 签名配置预校验结果
 */
sealed class KeyStoreValidationResult {
    object Success : KeyStoreValidationResult() {
        override fun toString(): String = "签名参数校验通过"
    }

    data class FileNotFound(val path: String) : KeyStoreValidationResult() {
        override fun toString(): String = "签名文件不存在: $path"
    }

    data class StorePasswordError(val message: String = "密钥库密码错误，请检查 storePassword") : KeyStoreValidationResult() {
        override fun toString(): String = message
    }

    data class AliasNotFound(
        val alias: String,
        val availableAliases: List<String> = emptyList()
    ) : KeyStoreValidationResult() {
        override fun toString(): String {
            return if (availableAliases.isNotEmpty()) {
                "未在签名文件中找到别名 '$alias'，当前文件中的可用别名为：${availableAliases.joinToString(prefix = "[", postfix = "]")}"
            } else {
                "未在签名文件中找到别名 '$alias'，且未检索到任何可用别名"
            }
        }
    }

    data class KeyPasswordError(val message: String = "私钥密码错误，请检查 keyPassword") : KeyStoreValidationResult() {
        override fun toString(): String = message
    }

    data class OtherError(val message: String) : KeyStoreValidationResult() {
        override fun toString(): String = message
    }
}

/**
 * 纯 Java 原生零外部依赖的 KeyStore 校验工具
 */
object KeyStoreValidator {

    /**
     * 校验 KeyStore 文件、别名、密钥库密码及私钥密码
     */
    fun validate(
        keyStorePath: String,
        keyAlias: String,
        keyStorePassword: String,
        keyPassword: String
    ): KeyStoreValidationResult {
        if (keyStorePath.isBlank()) {
            return KeyStoreValidationResult.FileNotFound("路径不能为空")
        }
        val file = File(keyStorePath)
        if (!file.exists() || !file.isFile) {
            return KeyStoreValidationResult.FileNotFound(file.absolutePath)
        }

        // 尝试加载 KeyStore（兼顾 JKS 与 PKCS12）
        val keyStoreTypes = listOf("JKS", "PKCS12")
        var loadedKeyStore: KeyStore? = null
        var lastException: Exception? = null

        for (type in keyStoreTypes) {
            try {
                val ks = KeyStore.getInstance(type)
                FileInputStream(file).use { fis ->
                    ks.load(fis, keyStorePassword.toCharArray())
                }
                loadedKeyStore = ks
                break
            } catch (e: Exception) {
                lastException = e
            }
        }

        if (loadedKeyStore == null) {
            val msg = lastException?.message ?: ""
            return if (msg.contains("password", ignoreCase = true) ||
                msg.contains("tampered", ignoreCase = true) ||
                lastException is UnrecoverableKeyException ||
                lastException is IOException
            ) {
                KeyStoreValidationResult.StorePasswordError("密钥库密码错误，请检查 storePassword")
            } else {
                KeyStoreValidationResult.OtherError("无法加载签名文件: ${lastException?.message}")
            }
        }

        // 校验别名是否存在
        val containsAlias = try {
            loadedKeyStore.containsAlias(keyAlias)
        } catch (e: Exception) {
            false
        }

        if (!containsAlias) {
            val available = try {
                loadedKeyStore.aliases().toList()
            } catch (e: Exception) {
                emptyList()
            }
            return KeyStoreValidationResult.AliasNotFound(keyAlias, available)
        }

        // 校验私钥密码
        try {
            val key = loadedKeyStore.getKey(keyAlias, keyPassword.toCharArray())
            if (key == null) {
                return KeyStoreValidationResult.KeyPasswordError("未找到对应别名的私钥或私钥密码错误")
            }
        } catch (e: UnrecoverableKeyException) {
            return KeyStoreValidationResult.KeyPasswordError("私钥密码错误，请检查 keyPassword")
        } catch (e: Exception) {
            val msg = e.message ?: ""
            return if (msg.contains("password", ignoreCase = true)) {
                KeyStoreValidationResult.KeyPasswordError("私钥密码错误，请检查 keyPassword")
            } else {
                KeyStoreValidationResult.OtherError("获取私钥失败: ${e.message}")
            }
        }

        return KeyStoreValidationResult.Success
    }
}
