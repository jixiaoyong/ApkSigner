package io.github.jixiaoyong.beans

import io.github.jixiaoyong.i18n.Strings

/**
 * @author : jixiaoyong
 * @description ：签名类型
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 2023/8/25
 */
sealed class SignType(val type: Int, val name: String) {
    object V1 : SignType(1, "V1")
    object V2 : SignType(2, "V2")
    object V3 : SignType(3, "V3")
    object V4 : SignType(4, "V4")
    object NONE : SignType(0, "NONE")

    override fun toString(): String {
        return "$type"
    }

    fun description(i18nString: Strings):String? {
        return if(V1.type ==type){
            // api 24+ can't sign v1: https://issuetracker.google.com/issues/133325048#comment9
            i18nString.signType1Desc
        }else{
            null
        }
    }

    companion object {

        val DEF_SIGN_TYPES = listOf(V1, V2)
        val ALL_SIGN_TYPES = listOf(V1, V2, V3, V4)

        fun valueOf(it: Int): SignType {
            return when (it) {
                1 -> V1
                2 -> V2
                3 -> V3
                4 -> V4
                else -> NONE
            }
        }
    }
}