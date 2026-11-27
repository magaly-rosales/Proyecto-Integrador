package com.tecsup.usuario_kotlin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.TextSize
import com.tecsup.usuario_kotlin.ui.components.ProfileAvatar as SharedProfileAvatar
import com.tecsup.usuario_kotlin.ui.home.components.TextSizeDialog
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.translation.SenasBottomBar

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    notificationCount: Int = 0,
    onNavigateToTraducir: () -> Unit,
    onNavigateToSenas: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    userPhoto: String? = null,
    darkModeEnabled: Boolean = true,
    onDarkModeChange: (Boolean) -> Unit = {},
    textSize: TextSize = TextSize.NORMAL,
    onTextSizeChange: (TextSize) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToGuide: () -> Unit = {},
    userRole: String? = null
) {
    val displayName = userName.ifBlank { "Usuario" }

    val displayEmail = userEmail.ifBlank {
        "Correo no disponible"
    }

    // Comprobar el rol recibido del servidor
    val isAdmin =
        userRole?.trim().equals("admin", ignoreCase = true) ||
                userRole?.trim().equals(
                    "administrador",
                    ignoreCase = true
                )

    var showTextSizeDialog by remember {
        mutableStateOf(false)
    }

    var showAdminDialog by remember {
        mutableStateOf(false)
    }

    // Diálogo de tamaño de texto
    if (showTextSizeDialog) {
        TextSizeDialog(
            selectedSize = textSize,
            onConfirm = { size ->
                onTextSizeChange(size)
                showTextSizeDialog = false
            },
            onDismiss = {
                showTextSizeDialog = false
            }
        )
    }

    // Información administrativa según el rol
    if (showAdminDialog && isAdmin) {
        AlertDialog(
            onDismissRequest = {
                showAdminDialog = false
            },
            containerColor = ProfileColors.CardDark,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = ProfileColors.AccentCyan
                )
            },
            title = {
                Text(
                    text = "Panel de administración",
                    color = ProfileColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Tu cuenta tiene un rol administrativo. " +
                            "Las funciones de administración se " +
                            "realizan desde la plataforma web de SeñasApp.",
                    color = ProfileColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAdminDialog = false
                    }
                ) {
                    Text(
                        text = "Entendido",
                        color = ProfileColors.AccentCyan
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = ProfileColors.BgDark,
        bottomBar = {
            SenasBottomBar(
                selectedTab = 2,
                onSelectTraducir = onNavigateToTraducir,
                onSelectSenas = onNavigateToSenas,
                onSelectPerfil = {}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi cuenta",
                    color = ProfileColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ProfileColors.TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Información personal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                SharedProfileAvatar(
                    userName = displayName,
                    foto = userPhoto,
                    avatarSize = 64.dp,
                    fontSize = 26.sp
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        color = ProfileColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = displayEmail,
                        color = ProfileColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Menú del perfil
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ProfileColors.CardDark,
                border = BorderStroke(
                    1.dp,
                    ProfileColors.CardElevated
                )
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "Mi perfil",
                        onClick = onNavigateToEditProfile
                    )

                    ProfileDivider()

                    ProfileMenuItem(
                        icon = Icons.Outlined.Tune,
                        title = "Configuración",
                        onClick = onNavigateToSettings
                    )

                    ProfileDivider()

                    ProfileMenuItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificaciones",
                        trailing = {
                            if (notificationCount > 0) {
                                NotificationBadge(notificationCount)
                            }
                        },
                        onClick = onNavigateToNotifications
                    )

                    ProfileDivider()

                    ProfileMenuItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "Modo oscuro",
                        trailing = {
                            Switch(
                                checked = darkModeEnabled,
                                onCheckedChange = onDarkModeChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor =
                                        ProfileColors.BgDark,
                                    checkedTrackColor =
                                        ProfileColors.AccentCyan,
                                    uncheckedThumbColor =
                                        ProfileColors.TextSecondary,
                                    uncheckedTrackColor =
                                        ProfileColors.CardElevated
                                )
                            )
                        },
                        onClick = {
                            onDarkModeChange(!darkModeEnabled)
                        }
                    )

                    ProfileDivider()

                    ProfileMenuItem(
                        icon = Icons.Outlined.TextFields,
                        title = "Tamaño del texto",
                        trailing = {
                            Text(
                                text = textSize.label,
                                color = ProfileColors.AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            showTextSizeDialog = true
                        }
                    )

                    ProfileDivider()

                    // Visible únicamente para administradores
                    if (isAdmin) {
                        ProfileMenuItem(
                            icon = Icons.Outlined.Security,
                            title = "Panel de administración",
                            trailing = {
                                AdminBadge()
                            },
                            onClick = {
                                if (isAdmin) {
                                    showAdminDialog = true
                                }
                            }
                        )

                        ProfileDivider()
                    }

                    // Guía de uso
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = "Guía de uso",
                        onClick = onNavigateToGuide
                    )

                    ProfileDivider()

                    // Cerrar sesión
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "Cerrar sesión",
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        color = ProfileColors.CardElevated,
        thickness = 1.dp
    )
}

@Composable
private fun NotificationBadge(count: Int) {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = 24.dp,
                minHeight = 24.dp
            )
            .clip(CircleShape)
            .background(ProfileColors.BadgeYellow)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = ProfileColors.BgDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdminBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(
                1.dp,
                ProfileColors.AccentCyan.copy(alpha = 0.6f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Solo admin",
            color = ProfileColors.AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = title,
                color = ProfileColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }

        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing.invoke()
        }
    }
}