package com.tecsup.usuario_kotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun ErrorMessage(
    message: String?,
    modifier: Modifier = Modifier,
    topSpacing: Dp = 16.dp
) {
    if (message.isNullOrBlank()) return

    Column(modifier = modifier.fillMaxWidth()) {

        Spacer(modifier = Modifier.height(topSpacing))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ProfileColors.ErrorBackground,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
                1.dp,
                ProfileColors.ErrorBorder
            )
        ) {
            Text(
                text = message,
                color = ProfileColors.ErrorColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}