package com.tecsup.usuario_kotlin.ui.translation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun SenasTopBar(
    userInitial: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit = {}
) {
    val textPrimary = ProfileColors.TextPrimary
    val accentCyan = ProfileColors.AccentCyan

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo y nombre de la aplicación
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ProfileColors.CardElevated)
                    .border(
                        1.5.dp,
                        accentCyan,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PanTool,
                    contentDescription = "Logo SeñasApp",
                    tint = accentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Señas")
                    }

                    withStyle(
                        SpanStyle(
                            color = accentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("App")
                    }
                },
                fontSize = 21.sp
            )
        }

        // Notificaciones e inicial del usuario
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNotificationsClick
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.Notifications,
                        contentDescription =
                            "Notificaciones: $notificationCount sin leer",
                        tint = textPrimary,
                        modifier = Modifier.size(26.dp)
                    )

                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(
                                    x = 4.dp,
                                    y = (-4).dp
                                )
                                .sizeIn(
                                    minWidth = 16.dp,
                                    minHeight = 16.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    ProfileColors.BadgeYellow
                                )
                                .padding(
                                    horizontal = 4.dp,
                                    vertical = 1.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = notificationCount.toString(),
                                color = ProfileColors.BgDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Inicial del usuario
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitial,
                    color = ProfileColors.BgDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}