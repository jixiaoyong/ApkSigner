package io.github.jixiaoyong.beans

/**
 * 签名信息bean类
 */
data class SignInfoBean(
    var keyNickName: String = "",
    var keyStorePath: String = "",
    var keyStorePassword: String = "",
    var keyAlias: String = "",
    var keyPassword: String = "",
    val id: Long = System.currentTimeMillis()
) {
    fun isValid(): Boolean {
        return keyNickName.isNotEmpty() && keyStorePath.isNotEmpty() && keyStorePassword.isNotEmpty()
                && keyAlias.isNotEmpty() && keyPassword.isNotEmpty()
    }

    override fun toString(): String {
        return "$keyNickName | $keyStorePath"
    }

    override fun equals(other: Any?): Boolean {
        return other is SignInfoBean && keyNickName == other.keyNickName && keyStorePath == other.keyStorePath
                && keyStorePassword == other.keyStorePassword && keyAlias == other.keyAlias && keyPassword == other.keyPassword
    }

    fun isSameOne(other: SignInfoBean?) = id == other?.id

    override fun hashCode(): Int {
        return super.hashCode()
    }
}