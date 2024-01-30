package io.github.jixiaoyong.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @author : jixiaoyong
 * @description ：toast
 *
 * @email : jixiaoyong1995@gmail.com
 * @date : 30/1/2024
 */
object ToasterUtil {

    private var toastConfig: MutableState<ToastConfig?> = mutableStateOf(null)

    @Composable
    fun init(isDarkTheme: Boolean) {
        val backgroundColor = if (isDarkTheme) Color.White else Color.Black
        val fontColor = if (isDarkTheme) Color.Black else Color.White
        val config = toastConfig.value
        if (null != config && config.msg.isNotBlank()) {
            Popup(
                alignment = Alignment.BottomCenter,
                properties = PopupProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    clippingEnabled = false
                )
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 100.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .background(color = backgroundColor.copy(alpha = 0.8f), shape = RoundedCornerShape(5.dp))
                        .padding(horizontal = 15.dp, vertical = 10.dp),
                ) {
                    Text(config.msg, color = fontColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        LaunchedEffect(toastConfig.value) {
            val config = toastConfig.value
            if (null != config) {
                delay(config.duration.duration)
                toastConfig.value = null
            }
        }
    }

    fun show(msg: String, duration: ToastConfig.DURATION = ToastConfig.DURATION.Short) {
        toastConfig.value = ToastConfig(msg, duration)
    }

}

data class ToastConfig(val msg: String, val duration: DURATION, val id: UUID = UUID.randomUUID()) {

    sealed class DURATION(val duration: Duration) {
        object Short : DURATION(1.seconds)
        object Long : DURATION(3.5.seconds)
        data class Custom(val customDuration: Duration) : DURATION(customDuration)
    }
}

fun showToast(msg: String, duration: ToastConfig.DURATION = ToastConfig.DURATION.Short) {
    ToasterUtil.show(msg, duration)
}