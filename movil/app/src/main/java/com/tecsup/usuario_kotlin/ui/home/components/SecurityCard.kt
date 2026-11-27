package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun SecurityCard(
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Seguridad",
                color = ProfileColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Protege el acceso a tu cuenta.",
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ProfileColors.CardElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = enabled,
                            onClick = onChangePassword
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cambiar contraseña",
                            color = ProfileColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Actualiza tu contraseña de acceso",
                            color = ProfileColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}