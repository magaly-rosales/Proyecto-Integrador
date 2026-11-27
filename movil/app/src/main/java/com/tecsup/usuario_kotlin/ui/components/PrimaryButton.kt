package com.tecsup.usuario_kotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Cargando...",
    height: Dp = 52.dp
) {

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ProfileColors.AccentCyan,
            contentColor = ProfileColors.BgDark,
            disabledContainerColor = ProfileColors.DisabledAccent,
            disabledContentColor = ProfileColors.BgDark
        )
    ) {

        if (isLoading) {

            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = ProfileColors.BgDark,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = loadingText,
                fontWeight = FontWeight.Bold
            )

        } else {

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}