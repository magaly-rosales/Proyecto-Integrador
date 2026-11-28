package com.tecsup.usuario_kotlin.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.usuario_kotlin.data.model.AppNotification
import com.tecsup.usuario_kotlin.data.model.NotificationType
import com.tecsup.usuario_kotlin.data.repository.NotificationRepository
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun NotificationsScreen(
    repository: NotificationRepository,
    onBackClick: () -> Unit
) {
    val notifications by repository.notifications
        .collectAsStateWithLifecycle()

    val unreadCount = notifications.count {
        !it.leida
    }

    var showClearDialog by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(onBack = onBackClick)

    // Confirmación para eliminar todas
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
            },
            containerColor = ProfileColors.CardDark,
            title = {
                Text(
                    text = "Eliminar notificaciones",
                    color = ProfileColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar todas tus notificaciones? " +
                            "Tu progreso y tus preferencias se conservarán.",
                    color = ProfileColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.eliminarTodas()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = ProfileColors.ErrorColor
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = ProfileColors.TextSecondary
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 20.dp
            )
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = "Notificaciones",
                    onBackClick = onBackClick
                )
            }

            // Contador y acción de lectura
            item(key = "actions") {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (unreadCount) {
                            0 -> "No tienes notificaciones sin leer"
                            1 -> "1 notificación sin leer"
                            else -> "$unreadCount notificaciones sin leer"
                        },
                        color = ProfileColors.TextSecondary,
                        fontSize = 13.sp
                    )

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                repository.marcarTodasComoLeidas()
                            },
                            modifier = Modifier.align(
                                Alignment.End
                            )
                        ) {
                            Text(
                                text = "Marcar todas como leídas",
                                color = ProfileColors.AccentCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Estado vacío
            if (notifications.isEmpty()) {
                item(key = "empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ProfileColors.CardDark,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            ProfileColors.CardElevated
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = ProfileColors.TextSecondary,
                                modifier = Modifier.size(46.dp)
                            )

                            Text(
                                text = "No tienes notificaciones",
                                color = ProfileColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = "Aquí aparecerán las novedades " +
                                        "y los avisos de tus prácticas.",
                                color = ProfileColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = notifications,
                    key = { it.id }
                ) { notification ->

                    NotificationCard(
                        notification = notification,
                        onRead = {
                            repository.marcarComoLeida(
                                notification.id
                            )
                        },
                        onDelete = {
                            repository.eliminar(
                                notification.id
                            )
                        }
                    )
                }

                item(key = "clear_all") {
                    TextButton(
                        onClick = {
                            showClearDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = ProfileColors.ErrorColor
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Eliminar todas",
                            color = ProfileColors.ErrorColor
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta individual de notificación
@Composable
private fun NotificationCard(
    notification: AppNotification,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (notification.tipo) {
        NotificationType.WELCOME ->
            Icons.Outlined.Notifications

        NotificationType.PRACTICE ->
            Icons.Outlined.EmojiEvents
    }

    val iconColor = when (notification.tipo) {
        NotificationType.WELCOME ->
            ProfileColors.AccentCyan

        NotificationType.PRACTICE ->
            ProfileColors.SuccessColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            if (notification.leida) {
                ProfileColors.CardElevated
            } else {
                ProfileColors.AccentCyan.copy(alpha = 0.6f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            iconColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onRead),
                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = notification.titulo,
                        color = ProfileColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = notification.mensaje,
                        color = ProfileColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Text(
                        text = notification.fechaFormateada,
                        color = ProfileColors.TextSecondary,
                        fontSize = 11.sp
                    )

                    if (!notification.leida) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        ProfileColors.AccentCyan
                                    )
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = "Sin leer",
                                color = ProfileColors.AccentCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Eliminar notificación",
                        tint = ProfileColors.TextSecondary
                    )
                }
            }

            if (!notification.leida) {
                TextButton(
                    onClick = onRead,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Marcar como leída",
                        color = ProfileColors.AccentCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}